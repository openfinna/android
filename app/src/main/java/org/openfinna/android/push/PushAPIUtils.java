/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.push;

import org.openfinna.android.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

public class PushAPIUtils {

    /**
     * Server URL
     */
    private static final String push_server = "https://kirkes.dfjapis.com/";

    private static Retrofit apiRetrofit() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.callTimeout(12, TimeUnit.SECONDS);
        builder.connectTimeout(12, TimeUnit.SECONDS);
        builder.readTimeout(20, TimeUnit.SECONDS);
        if (BuildConfig.DEBUG)
            builder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        return new Retrofit.Builder()
                .baseUrl(push_server)
                .client(builder.build())
                .build();
    }

    public static PushAPIService getAPIService() {
        return apiRetrofit().create(PushAPIService.class);
    }


}
