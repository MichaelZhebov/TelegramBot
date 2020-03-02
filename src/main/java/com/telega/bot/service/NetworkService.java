package com.telega.bot.service;

import com.telega.bot.rest.JSONPlaceHolderApi;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class NetworkService {
    private static NetworkService mInstance;
    private static final String BASE_URL = "https://androidcrudtest.herokuapp.com";
    private Retrofit mRetrofit;

    private NetworkService() {
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        mRetrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static NetworkService getInstance() {
        NetworkService result = mInstance;
        if (result != null) {
            return mInstance;
        }
        synchronized (NetworkService.class) {
            if (mInstance == null) {
                mInstance = new NetworkService();
            }
            return mInstance;
        }
    }

    public JSONPlaceHolderApi getJSONApi() {
        return mRetrofit.create(JSONPlaceHolderApi.class);
    }
}
