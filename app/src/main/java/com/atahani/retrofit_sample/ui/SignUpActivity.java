package com.atahani.retrofit_sample.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.widget.Toast;

import com.atahani.retrofit_sample.R;
import com.atahani.retrofit_sample.models.AuthenticationResponseModel;
import com.atahani.retrofit_sample.models.ErrorModel;
import com.atahani.retrofit_sample.models.SignUpRequestModel;
import com.atahani.retrofit_sample.network.FakeTwitterProvider;
import com.atahani.retrofit_sample.network.FakeTwitterService;
import com.atahani.retrofit_sample.utility.AppPreferenceTools;
import com.atahani.retrofit_sample.utility.ErrorUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    private AppCompatEditText mETxName;
    private AppCompatEditText mETxEmail;
    private AppCompatEditText mETxPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mETxName = (AppCompatEditText) findViewById(R.id.etx_name);
        mETxEmail = (AppCompatEditText) findViewById(R.id.etx_email);
        mETxPassword = (AppCompatEditText) findViewById(R.id.etx_password);
        AppCompatButton btnSignUp = (AppCompatButton) findViewById(R.id.btn_sign_up);
        if (btnSignUp != null) {
            btnSignUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //check fields not empty
                    if (mETxName.getText().toString().trim().length() > 0 && mETxEmail.getText().toString().trim().length() > 0 && mETxPassword.getText().toString().trim().length() > 3) {
                        //sign up the new user
                        SignUpRequestModel requestModel = new SignUpRequestModel();
                        requestModel.email = mETxEmail.getText().toString();
                        requestModel.name = mETxName.getText().toString();
                        requestModel.password = mETxPassword.getText().toString();
                        //provide service
                        FakeTwitterProvider provider = new FakeTwitterProvider();
                        FakeTwitterService tService = provider.getTService();
                        //make call
                        Call<AuthenticationResponseModel> call = tService.signUp(requestModel);
                        call.enqueue(new Callback<AuthenticationResponseModel>() {
                            @Override
                            public void onResponse(Call<AuthenticationResponseModel> call, Response<AuthenticationResponseModel> response) {
                                if (response.isSuccess()) {
                                    //save the authorization information into pref
                                    AppPreferenceTools appPreferenceTools = new AppPreferenceTools(getBaseContext());
                                    appPreferenceTools.saveUserAuthenticationInfo(response.body());
                                    //navigate to main activity
                                    startActivity(new Intent(getBaseContext(),MainActivity.class));
                                    //finish this activity
                                    finish();
                                } else {
                                    ErrorModel errorModel = ErrorUtils.parseError(response);
                                    Toast.makeText(getBaseContext(), "Error type is " + errorModel.type + " , description " + errorModel.description, Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<AuthenticationResponseModel> call, Throwable t) {
                                //occur when fail to deserialize || no network connection || server unavailable
                                Toast.makeText(getBaseContext(), "Fail it >> " + t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        Toast.makeText(getBaseContext(), "fields are empty, password should 4 char", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
