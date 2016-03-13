package com.atahani.retrofit_sample.models;

import com.atahani.retrofit_sample.utility.ClientConfigs;

/**
 * Sign up Request model used in sign up Request
 */
public class SignUpRequestModel {
    public String client_id;
    public String client_key;
    public String name;
    public String email;
    public String password;

    public SignUpRequestModel() {
        this.client_id = ClientConfigs.CLIENT_ID;
        this.client_key = ClientConfigs.CLIENT_KEY;
    }
}
