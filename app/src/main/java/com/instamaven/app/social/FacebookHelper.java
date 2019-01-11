package com.instamaven.app.social;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.CallbackManager.Factory;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import java.util.Arrays;

public class FacebookHelper {
    private FacebookListener mListener;
    private CallbackManager mCallBackManager;
    private FragmentActivity mContext;

    public FacebookHelper(@NonNull FacebookListener facebookListener, @NonNull FragmentActivity context) {
        this.mListener = facebookListener;
        this.mContext = context;
        this.mCallBackManager = Factory.create();
        FacebookCallback<LoginResult> mCallBack = new FacebookCallback<LoginResult>() {
            public void onSuccess(LoginResult loginResult) {
                FacebookHelper.this.mListener.onFacebookSignIn(loginResult.getAccessToken().getToken(), loginResult.getAccessToken().getUserId());
            }

            public void onCancel() {
                onSignInFail("User cancelled operation");
            }

            public void onError(FacebookException e) {
                onSignInFail(e.getMessage());
            }
        };
        LoginManager.getInstance().registerCallback(this.mCallBackManager, mCallBack);
    }

    @NonNull
    @CheckResult
    public CallbackManager getCallbackManager() {
        return this.mCallBackManager;
    }

    public void performSignIn(Activity activity) {
        LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList(new String[]{"public_profile", "user_friends", "email"}));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.mCallBackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void performSignOut() {
        LoginManager.getInstance().logOut();
        this.onSignOut();
    }

    public void onSignInFail(String errorMessage) {
        Toast.makeText(this.mContext, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
    }

    public void onSignOut() {
        Toast.makeText(this.mContext, "You Signed Out Facebook!", Toast.LENGTH_LONG).show();
    }

}
