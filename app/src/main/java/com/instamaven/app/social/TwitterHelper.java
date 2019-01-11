package com.instamaven.app.social;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

public class TwitterHelper {

    private TwitterAuthClient mAuthClient;
    @NonNull
    private final TwitterListener mListener;
    private FragmentActivity mContext;

    public Callback<TwitterSession> clickListener() {
        return new Callback<TwitterSession>() {
            public void success(Result<TwitterSession> result) {
                TwitterSession session = result.data;
                TwitterHelper.this.mListener.onTwitterSignIn((session.getAuthToken()).token, (session.getAuthToken()).secret, session.getUserId());
            }

            public void failure(TwitterException e) {
                onTwitterError(e.getMessage());
            }
        };
    }

    public TwitterHelper(@NonNull TwitterListener twitterListener, @NonNull FragmentActivity context) {
        mListener = twitterListener;
        mContext = context;
        mAuthClient = new TwitterAuthClient();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mAuthClient != null) {
            mAuthClient.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onTwitterError(String errorMessage) {
        Toast.makeText(this.mContext, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
    }

}
