/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.utils.db.users;

import android.provider.BaseColumns;

final class UsersDbContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private UsersDbContract() {
    }

    /* Inner class that defines the table contents */
    static class UsersEntry implements BaseColumns {
        static final String TABLE_NAME = "users";
        static final String USERNAME = "username";
        static final String USER_AUTH = "user_auth";
        static final String USER = "user";
        static final String BUILDING = "building";
    }

}
