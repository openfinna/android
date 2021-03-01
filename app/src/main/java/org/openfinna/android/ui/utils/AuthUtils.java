/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.utils;

import android.content.Context;
import android.util.Log;

import org.openfinna.android.classes.HomepageSavedResources;
import org.openfinna.android.classes.LoginUser;
import org.openfinna.android.ui.utils.db.users.UsersDb;
import org.openfinna.java.connector.classes.UserAuthentication;
import org.openfinna.java.connector.classes.models.User;
import org.openfinna.java.connector.classes.models.building.Building;
import org.openfinna.java.connector.classes.models.loans.Loan;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class AuthUtils {

    private static final String homepage_prefix = "kirkes_homepage";
    private static final String fav_lib_refreshed_prefix = "kirkes__lib_favorite_refreshed";
    private static final String homepage_refreshed_prefix = "kirkes_home_refreshed";
    private static final String loan_refreshed_prefix = "kirkes_loan_refreshed";
    private static final String hold_refreshed_prefix = "kirkes_hold_refreshed";
    private static final String prefix_separator = "_";

    public static boolean alreadyLoggedIn(Context context) {
        UsersDb usersDb = new UsersDb(context);
        return usersDb.usersSaved();
    }

    public static void saveAuthentication(UserAuthentication userAuthentication, User user, Building building, Context context) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, IOException, BadPaddingException, IllegalBlockSizeException {
        OpenFinnaStorageVault storageVault = OpenFinnaStorageVault.getInstance(context);
        UsersDb usersDb = new UsersDb(context);
        usersDb.addAccount(new LoginUser(userAuthentication, user, building));
        storageVault.encryptAndSave("activeUser", userAuthentication.getUsername());
    }

    public static void deleteUser(LoginUser user, Context context) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, IOException, BadPaddingException, IllegalBlockSizeException {
        UsersDb usersDb = new UsersDb(context);
        usersDb.deleteAccount(user);
    }

    public static void updateUser(LoginUser user, Context context) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, IOException, BadPaddingException, IllegalBlockSizeException {
        UsersDb usersDb = new UsersDb(context);
        usersDb.updateAccount(user);
    }


    public static void swapUser(LoginUser loginUser, Context context) {
        OpenFinnaStorageVault storageVault = OpenFinnaStorageVault.getInstance(context);
        storageVault.encryptAndSave("activeUser", loginUser.getUserAuthentication().getUsername());
    }

    public static void removeAll(Context context) {
        OpenFinnaStorageVault storageVault = OpenFinnaStorageVault.getInstance(context);
        UsersDb usersDb = new UsersDb(context);
        usersDb.deleteAllAccounts();
        storageVault.clearVault();
    }

    public static List<LoginUser> getAllUsers(Context context, boolean removeOwn) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, UnsupportedEncodingException {
        UsersDb usersDb = new UsersDb(context);
        List<LoginUser> users = usersDb.getUsers();
        Iterator<LoginUser> user_iterator = users.iterator();
        // Getting currently selected user
        LoginUser myUser = getAuthentication(context);

        while (user_iterator.hasNext()) {
            LoginUser user = user_iterator.next();
            if (removeOwn && myUser != null) {
                if (myUser.getUserAuthentication().getUsername().equals(user.getUserAuthentication().getUsername())) {
                    user_iterator.remove();
                }
            }
        }
        return users;
    }



    private static String generatePrefix(String prefix) {
        return prefix + prefix_separator;
    }


    public static LoginUser getAuthentication(Context context)  {
        OpenFinnaStorageVault storageVault = OpenFinnaStorageVault.getInstance(context);
        UsersDb usersDb = new UsersDb(context);

        String activeUser = storageVault.getEncryptedInfo("activeUser");
        try {
            if (alreadyLoggedIn(context)) {
                if (activeUser == null) {
                    return usersDb.getUsers().get(0);
                } else {
                    return usersDb.getUser(activeUser);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveHomepage(Context context, HomepageSavedResources homepageSavedResources) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, UnsupportedEncodingException {
        OpenFinnaStorageVault storageVault = OpenFinnaStorageVault.getInstance(context);
        LoginUser user = getAuthentication(context);
        if (user != null) {
            storageVault.encryptAndSave(generatePrefix(homepage_prefix) + user.getUserAuthentication().getUsername(), new Gson().toJson(homepageSavedResources));
        }
    }

    public static void saveHomepage(Context context, LoginUser user, HomepageSavedResources homepageSavedResources) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, UnsupportedEncodingException {
        OpenFinnaStorageVault storageVault = OpenFinnaStorageVault.getInstance(context);
        if (user != null) {
            storageVault.encryptAndSave(generatePrefix(homepage_prefix) + user.getUserAuthentication().getUsername(), new Gson().toJson(homepageSavedResources));
        }
    }

    public static HomepageSavedResources getHomepage(Context context) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, UnsupportedEncodingException {
        OpenFinnaStorageVault storageVault = OpenFinnaStorageVault.getInstance(context);
        LoginUser user = getAuthentication(context);
        if (user != null) {
            if (storageVault.paramStored(context, generatePrefix(homepage_prefix) + user.getUserAuthentication().getUsername()))
                return new Gson().fromJson(storageVault.getEncryptedInfo(generatePrefix(homepage_prefix) + user.getUserAuthentication().getUsername()), HomepageSavedResources.class);
            else
                return HomepageSavedResources.getDefault();
        } else
            return HomepageSavedResources.getDefault();
    }

    public static HomepageSavedResources getHomepage(Context context, LoginUser user) {
        OpenFinnaStorageVault storageVault = OpenFinnaStorageVault.getInstance(context);
        if (user != null) {
            if (storageVault.paramStored(context, generatePrefix(homepage_prefix) + user.getUserAuthentication().getUsername()))
                return new Gson().fromJson(storageVault.getEncryptedInfo(generatePrefix(homepage_prefix) + user.getUserAuthentication().getUsername()), HomepageSavedResources.class);
            else
                return HomepageSavedResources.getDefault();
        } else
            return HomepageSavedResources.getDefault();
    }

    public static Date lastHomepageLibraryUpdated(Context context) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, UnsupportedEncodingException {
        OpenFinnaStorageVault storageVault = OpenFinnaStorageVault.getInstance(context);
        LoginUser user = getAuthentication(context);
        if (user != null) {
            if (storageVault.paramStored(context, generatePrefix(fav_lib_refreshed_prefix) + user.getUserAuthentication().getUsername()))
                return new Date(Long.parseLong(storageVault.getEncryptedInfo(generatePrefix(fav_lib_refreshed_prefix) + user.getUserAuthentication().getUsername())));
        }
        return null;
    }

    public static Date lastHomepageItemsUpdated(Context context) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, UnsupportedEncodingException {
        OpenFinnaStorageVault storageVault = OpenFinnaStorageVault.getInstance(context);
        LoginUser user = getAuthentication(context);
        if (user != null) {
            if (storageVault.paramStored(context, generatePrefix(homepage_refreshed_prefix) + user.getUserAuthentication().getUsername()))
                return new Date(Long.parseLong(storageVault.getEncryptedInfo(generatePrefix(homepage_refreshed_prefix) + user.getUserAuthentication().getUsername())));

        }
        return null;
    }

    public static Date lastLoanExpireNotified(Context context, Loan loan) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, UnsupportedEncodingException {
        OpenFinnaStorageVault storageVault = OpenFinnaStorageVault.getInstance(context);
        LoginUser user = getAuthentication(context);
        if (user != null) {
            if (storageVault.paramStored(context, loan.getId() + generatePrefix(loan_refreshed_prefix) + user.getUserAuthentication().getUsername()))
                return new Date(Long.parseLong(storageVault.getEncryptedInfo(loan.getId() + generatePrefix(loan_refreshed_prefix) + user.getUserAuthentication().getUsername())));

        }
        return null;
    }

    public static void updateLastLoanExpireNotified(Context context, Loan loan) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, UnsupportedEncodingException {
        OpenFinnaStorageVault storageVault = OpenFinnaStorageVault.getInstance(context);
        LoginUser user = getAuthentication(context);
        if (user != null) {
            storageVault.encryptAndSave(loan.getId() + generatePrefix(loan_refreshed_prefix) + user.getUserAuthentication().getUsername(), String.valueOf(new Date().getTime()));
        }
    }

    public static Date lastLoanExpireNotified(Context context, LoginUser user, Loan loan) {
        OpenFinnaStorageVault storageVault = OpenFinnaStorageVault.getInstance(context);
        if (user != null) {
            if (storageVault.paramStored(context, loan.getId() + generatePrefix(loan_refreshed_prefix) + user.getUserAuthentication().getUsername()))
                return new Date(Long.parseLong(storageVault.getEncryptedInfo(loan.getId() + generatePrefix(loan_refreshed_prefix) + user.getUserAuthentication().getUsername())));

        }
        return null;
    }

    public static void updateLastLoanExpireNotified(Context context, LoginUser user, Loan loan) {
        OpenFinnaStorageVault storageVault = OpenFinnaStorageVault.getInstance(context);
        if (user != null) {
            storageVault.encryptAndSave(loan.getId() + generatePrefix(loan_refreshed_prefix) + user.getUserAuthentication().getUsername(), String.valueOf(new Date().getTime()));
        }
    }

    public static void updateLastHomepageItemsUpdate(Context context) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, UnsupportedEncodingException {
        OpenFinnaStorageVault storageVault = OpenFinnaStorageVault.getInstance(context);
        LoginUser user = getAuthentication(context);
        if (user != null) {
            storageVault.encryptAndSave(generatePrefix(homepage_refreshed_prefix) + user.getUserAuthentication().getUsername(), String.valueOf(new Date().getTime()));
        }
    }

    public static void updateLastHomepageLibraryUpdate(Context context) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, UnsupportedEncodingException {
        OpenFinnaStorageVault storageVault = OpenFinnaStorageVault.getInstance(context);
        LoginUser user = getAuthentication(context);
        if (user != null) {
            storageVault.encryptAndSave(generatePrefix(fav_lib_refreshed_prefix) + user.getUserAuthentication().getUsername(), String.valueOf(new Date().getTime()));
        }
    }

}
