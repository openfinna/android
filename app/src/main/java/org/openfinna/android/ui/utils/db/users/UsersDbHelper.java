/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.utils.db.users;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.openfinna.android.ui.utils.db.users.UsersDbContract.UsersEntry;

public class UsersDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "kirkes_users.db";

    private static final String SQL_CREATE_EXAM_ENTRIES =
            "CREATE TABLE " + UsersEntry.TABLE_NAME + " (" +
                    UsersEntry._ID + " INTEGER PRIMARY KEY," +
                    UsersEntry.USERNAME + " TEXT," +
                    UsersEntry.USER_AUTH + " TEXT," +
                    UsersEntry.BUILDING + " TEXT," +
                    UsersEntry.USER + " TEXT" +
                    ")";


    UsersDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_EXAM_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
