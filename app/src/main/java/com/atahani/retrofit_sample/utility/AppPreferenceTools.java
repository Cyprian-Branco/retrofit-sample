package com.atahani.retrofit_sample.utility;

import android.content.Context;
import android.content.SharedPreferences;

import com.atahani.retrofit_sample.R;
import com.atahani.retrofit_sample.models.AuthenticationResponseModel;

/**
 * for better management of preference in application
 * like authentication information
 */
public class AppPreferenceTools {

    private SharedPreferences mPreference;
    private Context mContext;
    public static final String STRING_PREF_UNAVAILABLE = "string preference unavailable";

    public AppPreferenceTools(Context context) {
        this.mContext = context;
        this.mPreference = this.mContext.getSharedPreferences("app_preference", Context.MODE_PRIVATE);
    }

    /**
     * save the user authentication model to pref at sing up || sign in
     *
     * @param authModel
     */
    public void saveUserAuthenticationInfo(AuthenticationResponseModel authModel) {
        mPreference.edit()
                .putString(this.mContext.getString(R.string.pref_access_token), authModel.token.access_token)
                .putLong(this.mContext.getString(R.string.pref_expire_in_sec), authModel.token.expire_in_sec)
                .putLong(this.mContext.getString(R.string.pref_expire_at), authModel.token.expire_at.getTime())
                .putString(this.mContext.getString(R.string.pref_refresh_token), authModel.token.refresh_token)
                .putString(this.mContext.getString(R.string.pref_app_id), authModel.token.app_id)
                .putString(this.mContext.getString(R.string.pref_user_id), authModel.user_profile.id)
                .putString(this.mContext.getString(R.string.pref_user_email), authModel.user_profile.email)
                .putString(this.mContext.getString(R.string.pref_user_name), authModel.user_profile.name)
                .putString(this.mContext.getString(R.string.pref_user_image_url), authModel.user_profile.imageUrl)
                .apply();
    }

    /**
     * get access token
     *
     * @return
     */
    public String getAccessToken() {
        return mPreference.getString(this.mContext.getString(R.string.pref_access_token), STRING_PREF_UNAVAILABLE);
    }

    /**
     * detect is user sign in
     *
     * @return
     */
    public boolean isAuthorized() {
        return !getAccessToken().equals(STRING_PREF_UNAVAILABLE);
    }


    /**
     * get user name
     *
     * @return
     */
    public String getUserName() {
        return mPreference.getString(this.mContext.getString(R.string.pref_user_name), "");
    }

    public String getImageProfileUrl() {
        return mPreference.getString(this.mContext.getString(R.string.pref_user_image_url), "");
    }
}
