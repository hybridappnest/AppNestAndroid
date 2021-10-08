package com.ymy.im.down.retrofit;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * Created by taochen on 18-6-21.
 */

public class RetrofitRequest {

    private static final String API_HOST = "http://api.u-launcher.com";
    private static final int DEFAULT_TIMEOUT = 6;

    private static RetrofitRequest mInstance;

    private RetrofitApi mRetrofitApi;

    private RetrofitRequest() {
        OkHttpClient client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_HOST)
                .client(client)
                .build();
        mRetrofitApi = retrofit.create(RetrofitApi.class);
    }

    public static synchronized RetrofitRequest getInstance() {
        if (null == mInstance) {
            mInstance = new RetrofitRequest();
        }
        return mInstance;
    }

    public RetrofitApi getRetrofitApi() {
        return mRetrofitApi;
    }
}
