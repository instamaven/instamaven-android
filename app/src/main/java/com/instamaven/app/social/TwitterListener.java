package com.instamaven.app.social;

public interface TwitterListener {

    void onTwitterSignIn(String token, String secret, long userId);

}
