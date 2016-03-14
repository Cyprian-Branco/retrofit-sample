package com.atahani.retrofit_sample.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.atahani.retrofit_sample.R;
import com.atahani.retrofit_sample.TApplication;
import com.atahani.retrofit_sample.adapter.OperationResultModel;
import com.atahani.retrofit_sample.adapter.TweetAdapter;
import com.atahani.retrofit_sample.models.ErrorModel;
import com.atahani.retrofit_sample.models.TweetModel;
import com.atahani.retrofit_sample.models.UserModel;
import com.atahani.retrofit_sample.network.FakeTwitterProvider;
import com.atahani.retrofit_sample.network.FakeTwitterService;
import com.atahani.retrofit_sample.utility.AndroidUtilities;
import com.atahani.retrofit_sample.utility.AppPreferenceTools;
import com.atahani.retrofit_sample.utility.Constants;
import com.atahani.retrofit_sample.utility.CropCircleTransformation;
import com.atahani.retrofit_sample.utility.ErrorUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TweetAdapter mAdapter;
    private FakeTwitterService mTService;
    private RecyclerView mRyTweets;
    private AppCompatTextView mTxDisplayName;
    private ImageView mImImageProfile;
    private AppPreferenceTools mAppPreferenceTools;
    private ProgressDialog mProgressDialog;
    private PermissionEventListener mPermissionEventListener;

    private ImageButton mImHappy;
    private ImageButton mImLoveMode;
    private ImageButton mImUnHappyMode;
    private int mCurrentCodePointMode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        //check is user logged it or not
        mAppPreferenceTools = new AppPreferenceTools(this);
        if (mAppPreferenceTools.isAuthorized()) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.default_toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("");
            //bind user image and name to toolbar
            mTxDisplayName = (AppCompatTextView) toolbar.findViewById(R.id.tx_display_name);
            mImImageProfile = (ImageView) toolbar.findViewById(R.id.im_image_profile);
            mTxDisplayName.setText(mAppPreferenceTools.getUserName());
            //load user image with Picasso
            Picasso.with(this).load(mAppPreferenceTools.getImageProfileUrl())
                    .transform(new CropCircleTransformation()).into(mImImageProfile);
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

            //these lines for modes
            mImHappy = (ImageButton) findViewById(R.id.im_happy_mode);
            mImLoveMode = (ImageButton) findViewById(R.id.im_love_mode);
            mImUnHappyMode = (ImageButton) findViewById(R.id.im_unhappy_mode);
            mImHappy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickOnModeBtn(getResources().getInteger(R.integer.mode_happy));
                }
            });
            mImLoveMode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickOnModeBtn(getResources().getInteger(R.integer.mode_love));
                }
            });
            mImUnHappyMode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickOnModeBtn(getResources().getInteger(R.integer.mode_unhappy));
                }
            });
        } else {
            //the user is not logged in so should navigate to sing in activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        }
    }

    private void onClickOnModeBtn(int modeCodePoint) {
        if (mCurrentCodePointMode != modeCodePoint) {
            selectTheFeel(modeCodePoint);
            getTweetsByMode(modeCodePoint);
        } else {
            //de select all of mode and get tweet without any query
            deSelectAllOfMode();
            getTweetsFromServer();
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

    private void getTweetsByMode(int modeCodePoint) {
        //make call
        Call<List<TweetModel>> call = mTService.getTweetsByFeel(getStringOfEmojiCode(modeCodePoint));
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
        } else if (requestCode == Constants.UPDATE_USER_PROFILE_REQUEST_CODE && resultCode == RESULT_OK) {
            //update the value of name in toolbar and re create the tweets Adapter
            mTxDisplayName.setText(mAppPreferenceTools.getUserName());
            mAdapter.notifyDataSetChanged();
        } else if (requestCode == Constants.GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                Uri selectedImageUri = data.getData();
                String extractUriFrom = selectedImageUri.toString();
                //check is from google photos or google drive
                if (extractUriFrom.contains("com.google.android.apps.photos.contentprovider") || extractUriFrom.contains("com.google.android.apps.docs.storage")) {
                    final int chunkSize = 1024;  // We'll read in one kB at a time
                    byte[] imageData = new byte[chunkSize];
                    File imageFile = AndroidUtilities.generateImagePath();
                    InputStream in = null;
                    OutputStream out = null;
                    mProgressDialog.setMessage("Loading ...");
                    mProgressDialog.show();
                    try {
                        in = getContentResolver().openInputStream(selectedImageUri);
                        out = new FileOutputStream(imageFile);
                        int bytesRead;
                        while ((bytesRead = in.read(imageData)) > 0) {
                            out.write(Arrays.copyOfRange(imageData, 0, Math.max(0, bytesRead)));
                        }
                        uploadImageProfile(imageFile.getAbsolutePath());
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
//                        navigateToPhotoCropActivity();
                    } catch (Exception ex) {
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        Toast.makeText(getBaseContext(), "can not get this image :|", Toast.LENGTH_SHORT).show();
                        Log.e("Something went wrong.", ex.toString());
                    } finally {
                        if (in != null) {
                            in.close();
                        }
                        if (out != null) {
                            out.close();
                        }
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                    }
                } else {
                    uploadImageProfile(AndroidUtilities.getPath(selectedImageUri));
                }
            } catch (Exception ex) {
                Toast.makeText(getBaseContext(), "something wrong :|", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * upload image profile and is success load it into ImageView
     *
     * @param imagePath
     */
    private void uploadImageProfile(String imagePath) {
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            //create request body
            Map<String, RequestBody> map = new HashMap<>();
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
            map.put("photo\"; filename=\"" + imageFile.getName() + "\"", requestBody);
            //make call
            Call<UserModel> call = mTService.uploadUserProfileImage("bearer " + mAppPreferenceTools.getAccessToken(), map);
            call.enqueue(new Callback<UserModel>() {
                @Override
                public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                    if (response.isSuccess()) {
                        //save the user model to pref
                        mAppPreferenceTools.saveUserModel(response.body());
                        //load new image
                        Picasso.with(getBaseContext()).load(mAppPreferenceTools.getImageProfileUrl())
                                .transform(new CropCircleTransformation())
                                .into(mImImageProfile);
                        //reload the adapter since the user image profile changed
                        mAdapter.notifyDataSetChanged();
                    } else {
                        ErrorModel errorModel = ErrorUtils.parseError(response);
                        Toast.makeText(getBaseContext(), "Error type is " + errorModel.type + " , description " + errorModel.description, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<UserModel> call, Throwable t) {
                    //occur when fail to deserialize || no network connection || server unavailable
                    Toast.makeText(getBaseContext(), "Fail it >> " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(getBaseContext(), "can not upload file since the file is not exist :|", Toast.LENGTH_SHORT).show();
        }
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
        } else if (id == R.id.action_log_out) {
            //send request to server to terminate this application
            Call<OperationResultModel> call = mTService.terminateApp();
            call.enqueue(new Callback<OperationResultModel>() {
                @Override
                public void onResponse(Call<OperationResultModel> call, Response<OperationResultModel> response) {
                    if (response.isSuccess()) {
                        //remove all authentication information such as accessToken and others
                        mAppPreferenceTools.removeAllPrefs();
                        //navigate to sign in activity
                        startActivity(new Intent(getBaseContext(), SignInActivity.class));
                        //finish this
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<OperationResultModel> call, Throwable t) {
                    //occur when fail to deserialize || no network connection || server unavailable
                    Toast.makeText(getBaseContext(), "Fail it >> " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else if (id == R.id.action_update_profile) {
            //navigate to update user profile activity
            startActivityForResult(new Intent(getBaseContext(), EditUserProfile.class), Constants.UPDATE_USER_PROFILE_REQUEST_CODE);
        } else if (id == R.id.action_upload_image) {
            //open gallery to select single picture as image profile
            //before that check runtime permission
            if (checkRunTimePermissionIsGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                startGalleryIntent();
            } else {
                //request write external permission for open camera intent
                requestRunTimePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Constants.FOR_OPEN_GALLERY_REQUEST_WRITE_EXTERNAL_STORAGE_PER, new PermissionEventListener() {
                    @Override
                    public void onGranted(int requestCode, String[] permissions) {
                        startGalleryIntent();
                    }

                    @Override
                    public void onFailure(int requestCode, String[] permissions) {
                        Toast.makeText(getBaseContext(), "Can not pick photo without this permission", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else if (id == R.id.action_expire_access_tokens) {
            //send request to remove all access tokens
            //make call
            Call<OperationResultModel> call = mTService.removeAllAccessToken();
            call.enqueue(new Callback<OperationResultModel>() {
                @Override
                public void onResponse(Call<OperationResultModel> call, Response<OperationResultModel> response) {
                    //
                    if (response.isSuccess()) {
                        Toast.makeText(getBaseContext(), "all access token removed", Toast.LENGTH_SHORT).show();
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
        return super.onOptionsItemSelected(item);
    }

    /**
     * start gallery intent to pick photo
     */
    private void startGalleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.GALLERY_REQUEST_CODE);
    }

    private void requestRunTimePermission(String permissionType, int requestCode, PermissionEventListener permissionEventListener) {
        ActivityCompat.requestPermissions(this, new String[]{permissionType}, requestCode);
        mPermissionEventListener = permissionEventListener;
    }

    private boolean checkRunTimePermissionIsGranted(String permissionType) {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(TApplication.applicationContext, permissionType);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (mPermissionEventListener != null) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mPermissionEventListener.onGranted(requestCode, permissions);
            } else {
                mPermissionEventListener.onFailure(requestCode, permissions);
            }
        }

    }

    private String getStringOfEmojiCode(int emojiCode) {
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toChars(emojiCode));
        return sb.toString();
    }

    private void selectTheFeel(int emojiCodePoint) {
        mCurrentCodePointMode = emojiCodePoint;
        if (emojiCodePoint == getResources().getInteger(R.integer.mode_happy)) {
            mImHappy.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.mode_background_when_selected));
            mImLoveMode.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.mode_background));
            mImUnHappyMode.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.mode_background));
        } else if (emojiCodePoint == getResources().getInteger(R.integer.mode_love)) {
            mImLoveMode.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.mode_background_when_selected));
            mImHappy.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.mode_background));
            mImUnHappyMode.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.mode_background));
        } else if (emojiCodePoint == getResources().getInteger(R.integer.mode_unhappy)) {
            mImUnHappyMode.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.mode_background_when_selected));
            mImHappy.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.mode_background));
            mImLoveMode.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.mode_background));
        }
    }

    private void deSelectAllOfMode() {
        mCurrentCodePointMode = 0;
        mImHappy.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.mode_background));
        mImLoveMode.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.mode_background));
        mImUnHappyMode.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.mode_background));
    }


    public interface PermissionEventListener {
        void onGranted(int requestCode, String[] permissions);

        void onFailure(int requestCode, String[] permissions);
    }
}
