/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.utils.db.users;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.openfinna.android.classes.LoginUser;
import org.openfinna.android.ui.utils.OpenFinnaStorageVault;
import org.openfinna.android.ui.utils.db.HashingUtils;
import org.openfinna.android.ui.utils.db.users.UsersDbContract.UsersEntry;
import org.openfinna.java.connector.classes.UserAuthentication;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class UsersDb {

    private UsersDbHelper examsHelper;
    private SQLiteDatabase database;
    private OpenFinnaStorageVault openFinnaStorageVault;

    public UsersDb(Context context) {
        this.examsHelper = new UsersDbHelper(context);
        this.openFinnaStorageVault = OpenFinnaStorageVault.getInstance(context);
    }

    private void open() {
        database = examsHelper.getWritableDatabase();
    }

    private void close() {
        database.close();
    }

    /**
     * Getting all users
     *
     * @return LoginUser ArrayList
     */
    public List<LoginUser> getUsers() throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, UnsupportedEncodingException {
        open();
        Cursor cursor = database.query(
                UsersEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );

        List<LoginUser> users = new ArrayList<>();
        while (cursor.moveToNext()) {
            users.add(UsersDbConverter.userFromCursor(this.openFinnaStorageVault, cursor));
        }
        cursor.close();
        close();
        return users;
    }


    /**
     * Getting single user
     *
     * @return LoginUser
     */
    public LoginUser getUser(String username) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, UnsupportedEncodingException {
        open();
        String selection = UsersEntry.USERNAME + " = ?";
        String[] selectionArgs = {this.openFinnaStorageVault.encrypt(username)};
        Cursor cursor = database.query(
                UsersEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        LoginUser loginUser = null;
        if (cursor.moveToFirst()) {
            loginUser = UsersDbConverter.userFromCursor(this.openFinnaStorageVault, cursor);
        }
        cursor.close();
        close();
        return loginUser;
    }

    public boolean userSaved(String username) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, UnsupportedEncodingException {
        String selection = UsersEntry.USERNAME + " = ?";
        String[] selectionArgs = {this.openFinnaStorageVault.encrypt(username)};
        open();
        Cursor cursor = database.query(
                UsersEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        boolean saved = (cursor.getCount() > 0);
        cursor.close();
        close();
        return saved;
    }

    public boolean usersSaved() {
        open();
        Cursor cursor = database.query(
                UsersEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );
        boolean saved = (cursor.getCount() > 0);
        cursor.close();
        close();
        return saved;
    }

    public void addAccount(LoginUser loginUser) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, UnsupportedEncodingException {
        open();
        addUser(loginUser);
        close();
    }

    /**
     * Adding single account into database
     *
     * @param loginUser User to be added
     */
    private void addUser(LoginUser loginUser) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, UnsupportedEncodingException {
        database.insert(UsersEntry.TABLE_NAME, null, UsersDbConverter.convertUserToContentValue(this.openFinnaStorageVault, loginUser.getUserAuthentication(), loginUser.getUser(), loginUser.getBuilding()));
    }

    /**
     * Updating single account into database
     *
     * @param loginUser User to be added
     */
    private void updateUser(LoginUser loginUser) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, UnsupportedEncodingException {
        String selection = UsersEntry.USERNAME + " = ?";
        String[] selectionArgs = {this.openFinnaStorageVault.encrypt(loginUser.getUserAuthentication().getUsername())};
        database.update(UsersEntry.TABLE_NAME, UsersDbConverter.convertUserToContentValue(this.openFinnaStorageVault, loginUser.getUserAuthentication(), loginUser.getUser(), loginUser.getBuilding()), selection, selectionArgs);
    }

    public void deleteAccount(LoginUser loginUser) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, UnsupportedEncodingException {
        open();
        deleteUser(loginUser);
        close();
    }

    public void updateAccount(LoginUser loginUser) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, UnsupportedEncodingException {
        open();
        updateUser(loginUser);
        close();
    }

    public void deleteAllAccounts() {
        open();
        deleteAll();
        close();
    }

    private void deleteUser(LoginUser loginUser) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, UnsupportedEncodingException {
        String selection = UsersEntry.USERNAME + " = ?";
        String[] selectionArgs = {this.openFinnaStorageVault.encrypt(loginUser.getUserAuthentication().getUsername())};
        database.delete(UsersEntry.TABLE_NAME, selection, selectionArgs);
    }

    private void deleteAll() {
        database.delete(UsersEntry.TABLE_NAME, null, null);
    }
}
