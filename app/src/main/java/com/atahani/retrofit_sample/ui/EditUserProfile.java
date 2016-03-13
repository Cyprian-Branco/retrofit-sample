package com.atahani.retrofit_sample.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.atahani.retrofit_sample.R;
import com.atahani.retrofit_sample.models.ErrorModel;
import com.atahani.retrofit_sample.models.UserModel;
import com.atahani.retrofit_sample.network.FakeTwitterProvider;
import com.atahani.retrofit_sample.network.FakeTwitterService;
import com.atahani.retrofit_sample.utility.AppPreferenceTools;
import com.atahani.retrofit_sample.utility.Constants;
import com.atahani.retrofit_sample.utility.ErrorUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditUserProfile extends AppCompatActivity {

    private AppCompatEditText mETxName;
    private AppPreferenceTools mAppPreferenceTools;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile);
        mAppPreferenceTools = new AppPreferenceTools(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.default_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mETxName = (AppCompatEditText) findViewById(R.id.etx_name);
        //bind the name to edit text
        mETxName.setText(mAppPreferenceTools.getUserName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        } else if (id == R.id.action_edit) {
            //check is name is not empty
            if (mETxName.getText().toString().trim().length() > 0) {
                //should update the name
                UserModel userModel = new UserModel();
                userModel.name = mETxName.getText().toString();
                //provide the service
                FakeTwitterProvider provider = new FakeTwitterProvider();
                FakeTwitterService tService = provider.getTService();
                //make call
                Call<UserModel> call = tService.updateUserProfile(userModel);
                call.enqueue(new Callback<UserModel>() {
                    @Override
                    public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                        //
                        if (response.isSuccess()) {
                            //update the value in pref
                            mAppPreferenceTools.saveUserModel(response.body());
                            //navigate to parent activity set result to update name value
                            setResult(RESULT_OK);
                            //finish this activity
                            finish();
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
                Toast.makeText(getBaseContext(), "name field is empty", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onContextItemSelected(item);
    }
}
