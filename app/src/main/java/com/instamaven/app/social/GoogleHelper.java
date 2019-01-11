package com.instamaven.app.social;

/**
 * Created by main on 3/22/18.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.io.IOException;

public class GoogleHelper implements GoogleApiClient.OnConnectionFailedListener {
    private final String SCOPES = "oauth2:profile email";
    private final int RC_SIGN_IN = 100;
    private FragmentActivity mContext;
    private GoogleListener mListener;
    private GoogleApiClient mGoogleApiClient;

    public GoogleHelper(@NonNull GoogleListener listener, FragmentActivity context) {
        this.mListener = listener;
        this.mContext = context;
    }

    public void performSignIn(Activity activity) {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this.mContext)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        final Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        activity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RC_SIGN_IN) {
            final GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()) {
                AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
                    protected String doInBackground(Void... params) {
                        String token = null;

                        try {
                            token = GoogleAuthUtil.getToken(GoogleHelper.this.mContext, result.getSignInAccount().getAccount(), SCOPES);
                        } catch (GoogleAuthException | IOException e) {
                            e.printStackTrace();
                        }

                        return token;
                    }

                    protected void onPostExecute(String token) {
                        GoogleSignInAccount acct = result.getSignInAccount();
                        GoogleHelper.this.mListener.onGoogleSignIn(token, acct.getId());
                    }
                };
                task.execute(new Void[0]);
            } else {
                if (result.getStatus().getStatusCode() == 10) {
                    onSignInFailed("DEVELOPER MODE");
                } else {
                    onSignInFailed(result.getStatus().getStatusMessage());
                }
            }
        }

    }

    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        onSignInFailed(connectionResult.getErrorMessage());
    }

    public void performSignOut() {
        Auth.GoogleSignInApi.signOut(this.mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            public void onResult(@NonNull Status status) {
                onSignOut();
            }
        });
    }

    public void onSignInFailed(String errorMessage) {
        Toast.makeText(this.mContext, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
    }


    public void onSignOut() {
    }

}
