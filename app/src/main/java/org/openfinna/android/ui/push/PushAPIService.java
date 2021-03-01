/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.push;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PushAPIService {


    /**
     * API Requests to push server.
     * Find out more about the push server here: https://github.com/developerfromjokela/open-kirkes
     */


    @GET("api/push_notification/finna/")
    Call<Void> sendPushKey(@Query("key") String fcm_key);

}