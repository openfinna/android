/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.push;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.openfinna.android.BuildConfig;
import org.openfinna.android.R;
import org.openfinna.android.classes.HomepageSavedResources;
import org.openfinna.android.classes.LoginUser;
import org.openfinna.android.ui.main.activities.MainActivity;
import org.openfinna.android.ui.utils.AuthUtils;
import org.openfinna.android.ui.utils.DateUtils;
import org.openfinna.android.ui.utils.StatusUtils;
import org.openfinna.android.ui.utils.db.users.UsersDb;
import org.openfinna.java.connector.FinnaClient;
import org.openfinna.java.connector.classes.UserAuthentication;
import org.openfinna.java.connector.classes.models.User;
import org.openfinna.java.connector.classes.models.building.Building;
import org.openfinna.java.connector.classes.models.holds.Hold;
import org.openfinna.java.connector.classes.models.loans.Loan;
import org.openfinna.java.connector.interfaces.HoldsInterface;
import org.openfinna.java.connector.interfaces.LoansInterface;
import org.openfinna.java.connector.interfaces.auth.AuthenticationChangeListener;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static androidx.core.app.NotificationCompat.VISIBILITY_PRIVATE;

public class KirkesFCMPushService extends FirebaseMessagingService {

    public static final String SENDER_ID = "784345285281";
    private String TAG = this.getClass().getSimpleName();

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        log("Message received!");
        log("Sender: " + remoteMessage.getFrom());
        log("Data: " + new Gson().toJson(remoteMessage.getData().toString()));

        // If message is not originated from the server, it gets rejected
        if (!Objects.equals(remoteMessage.getFrom(), SENDER_ID))
            return;
        UsersDb db = new UsersDb(this);
        if (remoteMessage.getData().containsKey("data_refresh")) {
            try {
                List<LoginUser> users = db.getUsers();
                for (LoginUser loginUser : users) {
                    checkForNotification(loginUser);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void checkForNotification(final LoginUser loginUser) {
        final HomepageSavedResources resources = AuthUtils.getHomepage(this, loginUser);
        final List<Loan> loans = new ArrayList<>();
        final List<Hold> holds = new ArrayList<>();
        FinnaClient finnaClient = new FinnaClient(loginUser.getUserAuthentication(), new AuthenticationChangeListener() {
            @Override
            public void onAuthenticationChange(UserAuthentication userAuthentication, User user, Building building) {
                loginUser.setUserAuthentication(userAuthentication);
                if (user != null) {
                    loginUser.setUser(user);
                }
                if (building != null)
                    loginUser.setBuilding(building);
                try {
                    AuthUtils.updateUser(loginUser, KirkesFCMPushService.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        finnaClient.getLoans(new LoansInterface() {
            @Override
            public void onGetLoans(List<Loan> loanList) {
                loans.clear();
                loans.addAll(loanList);
                finnaClient.getHolds(new HoldsInterface() {
                    @Override
                    public void onGetHolds(List<Hold> holdList) {
                        log("Got all items, starting to notify");
                        holds.clear();
                        holds.addAll(holdList);
                        List<Hold> statusChanges = new ArrayList<>();
                        for (Hold h1 : resources.getPickupBooks()) {
                            for (Hold h2 : holds) {
                                if (h1.getId().equals(h2.getId())) {
                                    if (h1.getHoldStatus() != h2.getHoldStatus()) {
                                        statusChanges.add(h2);
                                    }
                                }
                            }
                        }

                        List<Loan> expiringLoans = new ArrayList<>();
                        List<Loan> expiredLoans = new ArrayList<>();
                        for (Loan l2 : loans) {
                            Date date = AuthUtils.lastLoanExpireNotified(KirkesFCMPushService.this, loginUser, l2);
                            if (DateUtils.getCountOfDays(new Date(), l2.getDueDate()) < 0) {
                                // This loan is expired, notifying every day to avoid fines
                                if (l2.getRenewsTotal()-l2.getRenewsUsed() > 0) {
                                    // Auto-renew
                                    finnaClient.renewLoan(l2, new LoansInterface() {
                                        @Override
                                        public void onGetLoans(List<Loan> list) {

                                        }

                                        @Override
                                        public void onLoanRenew(Loan loan, String s) {
                                            notifyLoanExtended(loan, loginUser);
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            e.printStackTrace();
                                            if (date == null || DateUtils.getCountOfDays(new Date(), date) >= 1) {
                                                expiredLoans.add(l2);
                                                AuthUtils.updateLastLoanExpireNotified(KirkesFCMPushService.this, loginUser, l2);
                                            }
                                        }
                                    });
                                } else {
                                    if (date == null || DateUtils.getCountOfDays(new Date(), date) >= 1) {
                                        expiredLoans.add(l2);
                                        AuthUtils.updateLastLoanExpireNotified(KirkesFCMPushService.this, loginUser, l2);
                                    }
                                }
                            } else if (DateUtils.getCountOfDays(new Date(), l2.getDueDate()) < 3) {
                                // Very soon will expire, notifying every day
                                if (date == null || DateUtils.getCountOfDays(new Date(), date) >= 1) {
                                    expiringLoans.add(l2);
                                    AuthUtils.updateLastLoanExpireNotified(KirkesFCMPushService.this, loginUser, l2);
                                }
                            } else if (DateUtils.getCountOfDays(new Date(), l2.getDueDate()) < 12) {
                                // Near, but not close to the expiration date. Notifying every 5 days
                                if (date == null || DateUtils.getCountOfDays(new Date(), date) > 5) {
                                    expiringLoans.add(l2);
                                    AuthUtils.updateLastLoanExpireNotified(KirkesFCMPushService.this, loginUser, l2);
                                }
                            }
                        }
                        log("expiring_soon: " + expiringLoans.size());
                        log("expired: " + expiredLoans.size());
                        log("status_change: " + statusChanges.size());
                        for (Loan loan : expiredLoans) {
                            notifyExpiredLoan(loan, loginUser);
                        }

                        for (Loan loan : expiringLoans) {
                            notifyExpirationLoan(loan, loginUser);
                        }

                        for (Hold hold : statusChanges) {
                            notifyStatusHoldChange(hold, loginUser);
                        }

                        resources.setReservedBooks(loanList);
                        resources.setPickupBooks(holdList);
                        new SaveItemsTask(loginUser).execute(resources);
                    }

                    @Override
                    public void onChangePickupLocation(Hold hold) {

                    }

                    @Override
                    public void onMakeHold() {

                    }

                    @Override
                    public void onCancelHold() {

                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });

            }

            @Override
            public void onLoanRenew(Loan loan, String s) {

            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private class SaveItemsTask extends AsyncTask<HomepageSavedResources, String, String> {
        private LoginUser loginUser;

        public SaveItemsTask(LoginUser loginUser) {
            this.loginUser = loginUser;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(HomepageSavedResources... homepageSavedResources) {
            try {
                AuthUtils.saveHomepage(KirkesFCMPushService.this, loginUser, homepageSavedResources[0]);
            } catch (NoSuchPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | NoSuchProviderException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }


    private void log(String content) {
        if (BuildConfig.DEBUG)
            Log.e(TAG, content);
    }

    public void notifyExpirationLoan(Loan loan, LoginUser loginUser) {
        createNotificationChannel();
        NotificationCompat.Builder notification = new NotificationCompat.Builder(KirkesFCMPushService.this, "kirkes_loans");

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("user", loginUser);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) (Math.random() * 5 + 1), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        int notificationId = (int) (Math.random() * 10 + 1);
        NotificationCompat.BigTextStyle inboxStyle = new NotificationCompat.BigTextStyle();
        notification.setStyle(inboxStyle);


        notification.setSmallIcon(R.drawable.ic_notify_logo);
        notification.setColor(ContextCompat.getColor(this, R.color.colorPrimary));

        notification.setContentTitle(getString(R.string.loan_expiration_soon_notif));
        notification.setContentIntent(pendingIntent);
        notification.setAutoCancel(true);
        int days = DateUtils.getCountOfDays(new Date(), loan.getDueDate());
        String msg = getString(R.string.loan_expiration_soon_msg, loan.getResource().getTitle(), String.valueOf(days), getString(days == 1 ? R.string.day : R.string.days));
        notification.setContentText(msg);
        inboxStyle.bigText(msg);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        Notification notification1 = notification.build();
        notificationManager.notify(notificationId, notification1);

        final RemoteViews contentView = notification1.contentView;
        final int iconId = android.R.id.icon;
        if (contentView != null)
            Picasso.get().load(loan.getResource().getImage()).into(contentView, iconId, notificationId, notification1);
    }

    public void notifyLoanExtended(Loan loan, LoginUser loginUser) {
        createNotificationChannel();
        NotificationCompat.Builder notification = new NotificationCompat.Builder(KirkesFCMPushService.this, "kirkes_loans");

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("user", loginUser);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) (Math.random() * 5 + 1), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        int notificationId = (int) (Math.random() * 10 + 1);
        NotificationCompat.BigTextStyle inboxStyle = new NotificationCompat.BigTextStyle();
        notification.setStyle(inboxStyle);


        notification.setSmallIcon(R.drawable.ic_notify_logo);
        notification.setColor(ContextCompat.getColor(this, R.color.colorPrimary));

        notification.setContentTitle(getString(R.string.loan_renewed));
        notification.setContentIntent(pendingIntent);
        notification.setAutoCancel(true);
        int days = DateUtils.getCountOfDays(new Date(), loan.getDueDate());
        String msg = getString(R.string.loan_renewed_auto_msg, loan.getResource().getTitle(), String.valueOf(days), getString(days == 1 ? R.string.day : R.string.days));
        notification.setContentText(msg);
        inboxStyle.bigText(msg);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        Notification notification1 = notification.build();
        notificationManager.notify(notificationId, notification1);

        final RemoteViews contentView = notification1.contentView;
        final int iconId = android.R.id.icon;
        Picasso.get().load(loan.getResource().getImage()).into(contentView, iconId, notificationId, notification1);

    }

    public void notifyStatusHoldChange(Hold hold, LoginUser loginUser) {
        createNotificationChannel();
        NotificationCompat.Builder notification = new NotificationCompat.Builder(KirkesFCMPushService.this, "kirkes_holds");

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("user", loginUser);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) (Math.random() * 5 + 1), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        int notificationId = (int) (Math.random() * 10 + 1);
        NotificationCompat.BigTextStyle inboxStyle = new NotificationCompat.BigTextStyle();
        notification.setStyle(inboxStyle);


        notification.setSmallIcon(R.drawable.ic_notify_logo);
        notification.setColor(ContextCompat.getColor(this, R.color.colorPrimary));

        notification.setContentTitle(getString(R.string.hold_status_change, hold.getResource().getTitle()));
        notification.setContentIntent(pendingIntent);
        notification.setAutoCancel(true);
        String msg = getString(R.string.hold_status_change_msg, StatusUtils.statusToString(KirkesFCMPushService.this, hold.getHoldStatus()));
        notification.setContentText(msg);
        inboxStyle.bigText(msg);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        Notification notification1 = notification.build();
        notificationManager.notify(notificationId, notification1);

        final RemoteViews contentView = notification1.contentView;
        final int iconId = android.R.id.icon;
        Picasso.get().load(hold.getResource().getImage()).into(contentView, iconId, notificationId, notification1);

    }


    public void notifyExpiredLoan(Loan loan, LoginUser loginUser) {
        createNotificationChannel();
        NotificationCompat.Builder notification = new NotificationCompat.Builder(KirkesFCMPushService.this, "kirkes_loans");

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("user", loginUser);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) (Math.random() * 5 + 1), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        int notificationId = (int) (Math.random() * 10 + 1);
        NotificationCompat.BigTextStyle inboxStyle = new NotificationCompat.BigTextStyle();
        notification.setStyle(inboxStyle);


        notification.setSmallIcon(R.drawable.ic_notify_logo);
        notification.setColor(ContextCompat.getColor(this, R.color.colorPrimary));

        notification.setContentTitle(getString(R.string.loan_expired_notif));
        notification.setContentIntent(pendingIntent);
        notification.setAutoCancel(true);
        String msg = getString(R.string.loan_expired_msg, loan.getResource().getTitle());
        notification.setContentText(msg);
        inboxStyle.bigText(msg);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        Notification notification1 = notification.build();
        notificationManager.notify(notificationId, notification1);

        final RemoteViews contentView = notification1.contentView;
        final int iconId = android.R.id.icon;
        Picasso.get().load(loan.getResource().getImage()).into(contentView, iconId, notificationId, notification1);

    }

    public void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            {
                CharSequence name = getString(R.string.holds_chname);
                String description = getString(R.string.holds_chdesc);
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel("kirkes_holds", name, importance);
                channel.setDescription(description);
                channel.setLockscreenVisibility(VISIBILITY_PRIVATE);
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                assert notificationManager != null;
                notificationManager.createNotificationChannel(channel);
            }

            {
                CharSequence name = getString(R.string.loans_chname);
                String description = getString(R.string.loans_chdesc);
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel("kirkes_loans", name, importance);
                channel.setDescription(description);
                channel.setLockscreenVisibility(VISIBILITY_PRIVATE);
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                assert notificationManager != null;
                notificationManager.createNotificationChannel(channel);
            }

        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        UsersDb db = new UsersDb(this);
        try {
            for (LoginUser loginUser : db.getUsers()) {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
