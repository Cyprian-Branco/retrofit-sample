package com.atahani.retrofit_sample.models;

import com.atahani.retrofit_sample.utility.ClientConfigs;

/**
 * Sgin in Requst model used in sign in request
 */
public class SignInRequestModel {
    public String client_id;
    public String client_key;
    public String email;
    public String password;

    public SignInRequestModel() {
        this.client_id = ClientConfigs.CLIENT_ID;
        this.client_key = ClientConfigs.CLIENT_KEY;
    }
}
