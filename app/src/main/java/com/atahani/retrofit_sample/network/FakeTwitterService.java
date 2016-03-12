package com.atahani.retrofit_sample.network;

import com.atahani.retrofit_sample.models.TweetModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * the interface implements REST API routes
 */
public interface FakeTwitterService {

    @POST("tweet")
    Call<TweetModel> createNewTweet(@Body TweetModel tweetModel);
}
