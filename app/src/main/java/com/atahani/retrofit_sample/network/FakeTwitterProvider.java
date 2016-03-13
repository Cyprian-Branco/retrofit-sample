package com.atahani.retrofit_sample.network;


import android.util.Log;

import com.atahani.retrofit_sample.TApplication;
import com.atahani.retrofit_sample.utility.AppPreferenceTools;
import com.atahani.retrofit_sample.utility.ClientConfigs;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.Date;
import java.util.jar.Pack200;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * this class make Retrofit API Service
 */
public class FakeTwitterProvider {

    private FakeTwitterService mTService;
    private Retrofit mRetrofitClient;
    private AppPreferenceTools mAppPreferenceTools;

    /**
     * config Retrofit in initialization
     */
    public FakeTwitterProvider() {
        this.mAppPreferenceTools = new AppPreferenceTools(TApplication.applicationContext);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        //add http interceptor to add headers to each request
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                if (original.url().url().getPath().endsWith("user/profile/image")) {
                    return chain.proceed(original);
                } else {
                    //build request
                    Request.Builder requestBuilder = original.newBuilder();
                    //add header for all of the request
                    requestBuilder.addHeader("Accept", "application/json");
                    //check is user logged in , if yes should add authorization header to every request
                    if (mAppPreferenceTools.isAuthorized()) {
                        requestBuilder.addHeader("Authorization", "bearer " + mAppPreferenceTools.getAccessToken());
                    }
                    requestBuilder.method(original.method(), original.body());
                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            }
        });
        //create new gson object to define custom converter on Date type
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new UTCDateTypeAdapter())
                .create();

        mRetrofitClient = new Retrofit.Builder()
                .baseUrl(ClientConfigs.REST_API_BASE_URL) // set Base URL , should end with '/'
                .client(httpClient.build()) // add http client
                .addConverterFactory(GsonConverterFactory.create(gson))//add gson converter
                .build();
        mTService = mRetrofitClient.create(FakeTwitterService.class);
    }

    /**
     * can get Retrofit Service
     *
     * @return
     */
    public FakeTwitterService getTService() {
        return mTService;
    }

    /**
     * get Retrofit client
     * used in ErrorUtil class
     *
     * @return
     */
    public Retrofit getRetrofitClient() {
        return mRetrofitClient;
    }
}
