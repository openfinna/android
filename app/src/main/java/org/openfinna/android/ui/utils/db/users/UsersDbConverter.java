/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.utils.db.users;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Build;

import org.openfinna.android.classes.LoginUser;
import org.openfinna.android.ui.utils.OpenFinnaStorageVault;
import org.openfinna.android.ui.utils.db.users.UsersDbContract.UsersEntry;
import org.openfinna.java.connector.classes.UserAuthentication;
import org.openfinna.java.connector.classes.models.User;
import org.openfinna.java.connector.classes.models.building.Building;

import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class UsersDbConverter {

    public static ContentValues convertUserToContentValue(OpenFinnaStorageVault openFinnaStorageVault, UserAuthentication userAuthentication, User user, Building building) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, UnsupportedEncodingException {
        ContentValues contentValues = new ContentValues();
        contentValues.put(UsersEntry.USER_AUTH, openFinnaStorageVault.encryptNotSynced(convertToJSON(userAuthentication)));
        contentValues.put(UsersEntry.USERNAME, openFinnaStorageVault.encryptNotSynced(userAuthentication.getUsername()));
        contentValues.put(UsersEntry.USER, openFinnaStorageVault.encryptNotSynced(convertToJSON(user)));
        contentValues.put(UsersEntry.BUILDING, openFinnaStorageVault.encryptNotSynced(convertToJSON(building)));
        return contentValues;
    }

    public static LoginUser userFromCursor(OpenFinnaStorageVault openFinnaStorageVault, Cursor cursor) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, UnsupportedEncodingException {
        User user = getObjectFromString(openFinnaStorageVault.decrypt(getString(cursor, UsersEntry.USER)), User.class);
        UserAuthentication userAuthentication = getObjectFromString(openFinnaStorageVault.decrypt(getString(cursor, UsersEntry.USER_AUTH)), UserAuthentication.class);
        Building building = getObjectFromString(openFinnaStorageVault.decrypt(getString(cursor, UsersEntry.BUILDING)), Building.class);
        return new LoginUser(userAuthentication, user, building);
    }

    private static <T> T getObjectFromString(String json, Class<T> classOfT) {
        try {
            return new Gson().fromJson(json, classOfT);
        } catch (Exception e) {
            return null;
        }
    }

    private static <T> T getObjectFromString(String json, Type classOfT) {
        try {
            return new Gson().fromJson(json, classOfT);
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean getBoolean(Cursor cursor, String key) {
        return Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(key)));
    }

    private static int getInt(Cursor cursor, String key) {
        return cursor.getInt(cursor.getColumnIndex(key));
    }

    private static String getString(Cursor cursor, String key) {
        int index = cursor.getColumnIndex(key);
        return cursor.getString(index);
    }


    private static SimpleDateFormat getDateConverter() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    }

    private static String convertToJSON(Object object) {
        return new Gson().toJson(object);
    }

}
