package com.atahani.retrofit_sample.models;

import com.google.gson.annotations.SerializedName;

/**
 * User Model
 */
public class UserModel {
    public String id;
    public String email;
    public String name;
    @SerializedName("image")
    public String imageUrl;
}
