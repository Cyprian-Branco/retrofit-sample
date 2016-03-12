package com.atahani.retrofit_sample.ui;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.atahani.retrofit_sample.R;
import com.atahani.retrofit_sample.models.TweetModel;
import com.atahani.retrofit_sample.network.FakeTwitterProvider;
import com.atahani.retrofit_sample.network.FakeTwitterService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateOrEditTweet extends AppCompatActivity {

    private String mSelectedMode = "";
    private AppCompatEditText mETxTweetBody;
    private ImageButton mImHappy;
    private ImageButton mImLoveMode;
    private ImageButton mImUnHappyMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_or_edit_tweet);
        Toolbar toolbar = (Toolbar) findViewById(R.id.default_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mImHappy = (ImageButton) findViewById(R.id.im_happy_mode);
        mImLoveMode = (ImageButton) findViewById(R.id.im_love_mode);
        mImUnHappyMode = (ImageButton) findViewById(R.id.im_unhappy_mode);
        mETxTweetBody = (AppCompatEditText) findViewById(R.id.etx_tweet_body);
        //the default mode is happy so we selected
        mImHappy.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.mode_background_when_selected));
        mSelectedMode = getStringOfEmojiCode(getResources().getInteger(R.integer.mode_happy));
        mImHappy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImHappy.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.mode_background_when_selected));
                mImLoveMode.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.mode_background));
                mImUnHappyMode.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.mode_background));
                mSelectedMode = getStringOfEmojiCode(getResources().getInteger(R.integer.mode_happy));
            }
        });
        mImLoveMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImLoveMode.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.mode_background_when_selected));
                mImHappy.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.mode_background));
                mImUnHappyMode.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.mode_background));
                mSelectedMode = getStringOfEmojiCode(getResources().getInteger(R.integer.mode_love));
            }
        });
        mImUnHappyMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImUnHappyMode.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.mode_background_when_selected));
                mImHappy.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.mode_background));
                mImLoveMode.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.mode_background));
                mSelectedMode = getStringOfEmojiCode(getResources().getInteger(R.integer.mode_unhappy));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_send, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_send) {
            //Send tweet
            //first create new instant of FakeTwitterProvider
            FakeTwitterProvider twitterProvider = new FakeTwitterProvider();
            //get the FakeTwitterService interface to call API routes
            FakeTwitterService mTService = twitterProvider.getTService();
            //create Tweet Model
            TweetModel tweetModel = new TweetModel();
            //assign tweet model values
            tweetModel.body = mETxTweetBody.getText().toString();
            tweetModel.feel = mSelectedMode;

            //create call generic class to send request to server
            Call<TweetModel> call = mTService.createNewTweet(tweetModel);
            //Async request
            //NOTE: you should always send Async request since the sync request cause crash in your application
            call.enqueue(new Callback<TweetModel>() {
                @Override
                public void onResponse(Call<TweetModel> call, Response<TweetModel> response) {
                    if (response.isSuccess()) {
                        Toast.makeText(getBaseContext(), "Successfully post tweet", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getBaseContext(), "HTTP request fail with code : " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<TweetModel> call, Throwable t) {
                    Toast.makeText(getBaseContext(), "Fail it >> " + t.getCause(), Toast.LENGTH_LONG).show();
                }
            });

        } else if (id == android.R.id.home) {
            //back to main activity
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * get String from emoji hex unicode
     *
     * @param emojiCode
     * @return
     */
    private String getStringOfEmojiCode(int emojiCode) {
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toChars(emojiCode));
        return sb.toString();
    }
}
