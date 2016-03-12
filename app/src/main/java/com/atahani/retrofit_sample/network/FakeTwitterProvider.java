package com.atahani.retrofit_sample.network;

import com.atahani.retrofit_sample.utility.ClientConfigs;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * this class make Retrofit API Service
 */
public class FakeTwitterProvider {

    private FakeTwitterService mTService;

    /**
     * config Retrofit in initialization
     */
    public FakeTwitterProvider() {
        OkHttpClient httpClient = new OkHttpClient();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ClientConfigs.REST_API_BASE_URL) // set Base URL , should end with '/'
                .client(httpClient) // add http client
                .addConverterFactory(GsonConverterFactory.create())//add default converter we use from Gson
                .build();
        mTService = retrofit.create(FakeTwitterService.class);
    }

    /**
     * can get Retrofit Service
     *
     * @return
     */
    public FakeTwitterService getTService() {
        return mTService;
    }
}
