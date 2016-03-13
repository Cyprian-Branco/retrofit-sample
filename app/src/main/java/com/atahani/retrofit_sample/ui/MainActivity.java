package com.atahani.retrofit_sample.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.atahani.retrofit_sample.R;
import com.atahani.retrofit_sample.adapter.OperationResultModel;
import com.atahani.retrofit_sample.adapter.TweetAdapter;
import com.atahani.retrofit_sample.models.ErrorModel;
import com.atahani.retrofit_sample.models.TweetModel;
import com.atahani.retrofit_sample.network.FakeTwitterProvider;
import com.atahani.retrofit_sample.network.FakeTwitterService;
import com.atahani.retrofit_sample.utility.AppPreferenceTools;
import com.atahani.retrofit_sample.utility.Constants;
import com.atahani.retrofit_sample.utility.CropCircleTransformation;
import com.atahani.retrofit_sample.utility.ErrorUtils;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TweetAdapter mAdapter;
    private FakeTwitterService mTService;
    private RecyclerView mRyTweets;
    private AppPreferenceTools mAppPreferenceTools;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //check is user logged it or not
        mAppPreferenceTools = new AppPreferenceTools(this);
        if (mAppPreferenceTools.isAuthorized()) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.default_toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("");
            //bind user image and name to toolbar
            AppCompatTextView txDisplayName = (AppCompatTextView) toolbar.findViewById(R.id.tx_display_name);
            ImageView imUserImageProfile = (ImageView) toolbar.findViewById(R.id.im_image_profile);
            txDisplayName.setText(mAppPreferenceTools.getUserName());
            //load user image with Picasso
            Picasso.with(this).load(mAppPreferenceTools.getImageProfileUrl())
                    .transform(new CropCircleTransformation()).into(imUserImageProfile);
            Log.d("ahmad", mAppPreferenceTools.getImageProfileUrl());
            //get the provider
            FakeTwitterProvider provider = new FakeTwitterProvider();
            mTService = provider.getTService();
            //config recycler view
            mRyTweets = (RecyclerView) findViewById(R.id.ry_tweets);
            mRyTweets.setLayoutManager(new LinearLayoutManager(this));
            mAdapter = new TweetAdapter(this, new TweetAdapter.TweetEventHandler() {
                @Override
                public void onEditTweet(String tweetId, int position) {
                    //start activity to edit tweet
                    Intent editTweetIntent = new Intent(getBaseContext(), CreateOrEditTweet.class);
                    editTweetIntent.putExtra(Constants.ACTION_TO_DO_KEY, Constants.EDIT_TWEET);
                    editTweetIntent.putExtra(Constants.TWEET_ID_KEY, tweetId);
                    startActivityForResult(editTweetIntent, Constants.CREATE_OR_EDIT_TWEET_REQUEST_CODE);
                }

                @Override
                public void onDeleteTweet(String tweetId, final int position) {
                    Call<OperationResultModel> call = mTService.deleteTweetById(tweetId);
                    //async request
                    call.enqueue(new Callback<OperationResultModel>() {
                        @Override
                        public void onResponse(Call<OperationResultModel> call, Response<OperationResultModel> response) {
                            if (response.isSuccess()) {
                                //get tweets from server just for test
                                getTweetsFromServer();
                            } else {
                                ErrorModel errorModel = ErrorUtils.parseError(response);
                                Toast.makeText(getBaseContext(), "Error type is " + errorModel.type + " , description " + errorModel.description, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<OperationResultModel> call, Throwable t) {
                            //occur when fail to deserialize || no network connection || server unavailable
                            Toast.makeText(getBaseContext(), "Fail it >> " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
            mRyTweets.setAdapter(mAdapter);
            //get tweets in load
            getTweetsFromServer();
        } else {
            //the user is not logged in so should navigate to sing up activity
            startActivity(new Intent(this, SignUpActivity.class));
            finish();
        }
    }

    /**
     * get tweets from server
     */
    private void getTweetsFromServer() {
        Call<List<TweetModel>> call = mTService.getTweets();
        call.enqueue(new Callback<List<TweetModel>>() {
            @Override
            public void onResponse(Call<List<TweetModel>> call, Response<List<TweetModel>> response) {

                if (response.isSuccess()) {
                    //update the adapter data
                    mAdapter.updateAdapterData(response.body());
                    mAdapter.notifyDataSetChanged();
                } else {
                    ErrorModel errorModel = ErrorUtils.parseError(response);
                    Toast.makeText(getBaseContext(), "Error type is " + errorModel.type + " , description " + errorModel.description, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<TweetModel>> call, Throwable t) {
                //occur when fail to deserialize || no network connection || server unavailable
                Toast.makeText(getBaseContext(), "Fail it >> " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Constants.CREATE_OR_EDIT_TWEET_REQUEST_CODE && resultCode == RESULT_OK) {
            getTweetsFromServer();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_default, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_tweet) {
            //start new activity to send tweet
            Intent postNewTweetIntent = new Intent(this, CreateOrEditTweet.class);
            postNewTweetIntent.putExtra(Constants.ACTION_TO_DO_KEY, Constants.NEW_TWEET);
            startActivityForResult(postNewTweetIntent, Constants.CREATE_OR_EDIT_TWEET_REQUEST_CODE);
        }
        return super.onOptionsItemSelected(item);
    }
}
