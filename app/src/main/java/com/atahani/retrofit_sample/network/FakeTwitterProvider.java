package com.atahani.retrofit_sample.network;

import com.atahani.retrofit_sample.utility.ClientConfigs;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * this class make Retrofit API Service
 */
public class FakeTwitterProvider {

    private FakeTwitterService mTService;
    private Retrofit mRetrofitClient;

    /**
     * config Retrofit in initialization
     */
    public FakeTwitterProvider() {
        OkHttpClient httpClient = new OkHttpClient();
        //create new gson object to define custom converter on Date type
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new UTCDateTypeAdapter())
                .create();

        mRetrofitClient = new Retrofit.Builder()
                .baseUrl(ClientConfigs.REST_API_BASE_URL) // set Base URL , should end with '/'
                .client(httpClient) // add http client
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
