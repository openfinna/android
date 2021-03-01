/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.push;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class KirkesFCMPushService extends FirebaseMessagingService {

    public static final String SENDER_ID = "701692030799";
    private String TAG = this.getClass().getSimpleName();

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
      /*  log("Message received!");
        log("Sender: " + remoteMessage.getFrom());
        log("Data: " + new Gson().toJson(remoteMessage.getData().toString()));

        // If message is not originated from the server, it gets rejected
        if (!Objects.equals(remoteMessage.getFrom(), SENDER_ID))
            return;
        UsersDb db = new UsersDb(this);
        if (remoteMessage.getData().containsKey("data_refresh") && remoteMessage.getData().containsKey("user")) {
            String user = remoteMessage.getData().get("user");
            try {
                LoginUser loginUser = db.getUserUsingTokenHash(user);
                if (loginUser != null) {
                    checkForNotification(loginUser);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/

    }

   /* private void checkForNotification(final LoginUser loginUser) {
        final HomepageSavedResources resources = AuthUtils.getHomepage(this, loginUser);
        final List<Loan> loans = new ArrayList<>();
        final List<Hold> holds = new ArrayList<>();
        APIUtils.getAPIService(this).loans(loginUser.getResponse().getToken(), APIUtils.getKirkesLangCode()).enqueue(new Callback<LoansResponse>() {
            @Override
            public void onResponse(Call<LoansResponse> call, Response<LoansResponse> response) {
                if (response.isSuccessful()) {
                    final LoansResponse loansResponse = response.body();
                    log("Got loans");

                    loans.clear();
                    loans.addAll(loansResponse.getLoans());
                    APIUtils.getAPIService(KirkesFCMPushService.this).holds(loginUser.getResponse().getToken(), APIUtils.getKirkesLangCode()).enqueue(new Callback<HoldsResponse>() {
                        @Override
                        public void onResponse(Call<HoldsResponse> call, Response<HoldsResponse> response) {
                            if (response.isSuccessful()) {
                                log("Got all items, starting to notify");
                                HoldsResponse holdsResponse = response.body();
                                holds.clear();
                                holds.addAll(holdsResponse.getHolds());
                                List<Hold> statusChanges = new ArrayList<>();
                                for (Hold h1 : resources.getPickupBooks().getHolds()) {
                                    for (Hold h2 : holds) {
                                        if (h1.getId().equals(h2.getId())) {
                                            if (h1.getStatus() != h2.getStatus()) {
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
                                        if (date == null || DateUtils.getCountOfDays(new Date(), date) >= 1) {
                                            expiredLoans.add(l2);
                                            AuthUtils.updateLastLoanExpireNotified(KirkesFCMPushService.this, loginUser, l2);
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

                                resources.setReservedBooks(loansResponse);
                                resources.setPickupBooks(holdsResponse);
                                new SaveItemsTask(loginUser).execute(resources);
                            }
                        }

                        @Override
                        public void onFailure(Call<HoldsResponse> call, Throwable t) {
                            t.printStackTrace();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<LoansResponse> call, Throwable t) {
                t.printStackTrace();
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


        notification.setSmallIcon(R.drawable.ic_kirkes_app_logo);
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
        Picasso.get().load(loan.getResource().getImageURL()).into(contentView, iconId, notificationId, notification1);

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


        notification.setSmallIcon(R.drawable.ic_kirkes_app_logo);
        notification.setColor(ContextCompat.getColor(this, R.color.colorPrimary));

        notification.setContentTitle(getString(R.string.hold_status_change, hold.getResource().getTitle()));
        notification.setContentIntent(pendingIntent);
        notification.setAutoCancel(true);
        String msg = getString(R.string.hold_status_change_msg, StatusUtils.statusToString(KirkesFCMPushService.this, hold.getStatus()));
        notification.setContentText(msg);
        inboxStyle.bigText(msg);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        Notification notification1 = notification.build();
        notificationManager.notify(notificationId, notification1);

        final RemoteViews contentView = notification1.contentView;
        final int iconId = android.R.id.icon;
        Picasso.get().load(hold.getResource().getImageURL()).into(contentView, iconId, notificationId, notification1);

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


        notification.setSmallIcon(R.drawable.ic_kirkes_app_logo);
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
        Picasso.get().load(loan.getResource().getImageURL()).into(contentView, iconId, notificationId, notification1);

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
                APIUtils.getAPIService(KirkesFCMPushService.this).changePushKey(loginUser.getResponse().getToken(), s).enqueue(new Callback<BasicResponse>() {
                    @Override
                    public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {

                    }

                    @Override
                    public void onFailure(Call<BasicResponse> call, Throwable t) {

                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}
