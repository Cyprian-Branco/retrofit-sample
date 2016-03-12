package com.atahani.retrofit_sample.models;

import java.util.Date;

/**
 * Tweet Model to send new tweet for request body and get in in response
 * NOTE: all of the attr should define as public and also the name should match in REST API
 */
public class TweetModel {
    public String feel;
    public String body;
    public Date created_at;
    public Date updated_at;
}
