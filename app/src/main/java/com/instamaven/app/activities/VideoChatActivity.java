package com.instamaven.app.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.instamaven.app.R;
import com.instamaven.app.utils.ProfileHelper;
import com.instamaven.app.utils.RequestClient;
import com.instamaven.app.utils.SettingsHelper;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.Connection;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class VideoChatActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback, Chronometer.OnChronometerTickListener {

    protected RequestClient.Builder client;
    private static final int OT_AUDIO_VIDEO_PERMISSIONS = 1;
    private int call_timeout = 10; // in seconds
    private int min_seconds_to_comment = 10;
    private OpenTokSession mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;
    private ArrayList<Stream> mStreams;
    private Handler mHandler = new Handler();
    private Handler rHandler = new Handler();

    private FrameLayout mPublisherViewContainer;
    private FrameLayout mSubscriberViewContainer;

    // Spinning wheel for loading subscriber view
    private ProgressDialog mSessionDialog;
    private ProgressDialog mSubscriberDialog;
    private AlertDialog mPermissionsDialog;

    protected SessionListener mSessionListener;
    protected SubscriberListener mSubscriberListener;
    protected PublisherListener mPublisherListener;

    protected ToneGenerator toneGenerator;
    boolean isRequestProcess = false;

    private static String SESSION_ID = "";
    private static String TOKEN = "";
    private static String badgeTitle = "";
    private static String callerAvatar = "";
    private static final boolean SUBSCRIBE_TO_SELF = false;

    private ConstraintLayout loadingLayout;
    private ConstraintLayout callerLayout;
    private ConstraintLayout calleeLayout;
    private ConstraintLayout chatLayout;
    private TextView badgeTitleToShow;
    private SimpleDraweeView callerImage;

    protected Chronometer chronometer;
    protected Integer seconds = 0;

    private VideoView videoCalleeAnim, videoCallerAnim;

    // Caller FCM ID, If caller is not empty than it's my call_anim
    String caller = "";
    // Callee User ID
    String calleeId = "";
    // Current badge ID
    Integer badge_id;
    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        videoCalleeAnim = findViewById(R.id.videoViewCallee);
        videoCallerAnim
                = findViewById(R.id.videoViewCaller);

        Uri videoCallCallAnim = Uri.parse("android.resource://" + getPackageName() + "/" + String.format("%s" , R.raw.call_anim));
        videoCalleeAnim.setVideoURI(videoCallCallAnim);
        videoCallerAnim.setVideoURI(videoCallCallAnim);

        // Video Callee
        videoCalleeAnim.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
        // Video Caller
        videoCallerAnim.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });

        try {
            JSONObject settings = (JSONObject) IMApp.myProfile.get("settings");
            call_timeout = settings.getInt("call_timeout");
            min_seconds_to_comment = settings.getInt("min_call_length_to_comment");
        } catch (JSONException e) {
//            e.printStackTrace();
        }

        // Chronometer
        chronometer = findViewById(R.id.chronometer);
        chronometer.setOnChronometerTickListener(this);

        mPublisherViewContainer = findViewById(R.id.publisherview);
        mSubscriberViewContainer = findViewById(R.id.subscriberview);
        mSessionDialog = new ProgressDialog(VideoChatActivity.this);
        mSubscriberDialog = new ProgressDialog(VideoChatActivity.this);
        mPermissionsDialog = null;

        mStreams = new ArrayList<>();

        SESSION_ID = getIntent().getExtras().getString("session");
        TOKEN = getIntent().getExtras().getString("token");

        sessionInit();

        loadingLayout = findViewById(R.id.loadingPanel);
        callerLayout = findViewById(R.id.callerLayout);
        calleeLayout = findViewById(R.id.calleeLayout);
        chatLayout = findViewById(R.id.chatLayout);
        chatLayout.setVisibility(View.GONE);
        badgeTitleToShow = findViewById(R.id.badgeTitle);
        callerImage = findViewById(R.id.callerImage);

        client = new RequestClient.Builder(this);

        badge_id = getIntent().getExtras().getInt("badge_id", 0);
        caller = getIntent().getExtras().getString("caller", "");
        calleeId = getIntent().getExtras().getString("callee", "");
        if (!caller.isEmpty()) {
            loadingLayout.setVisibility(View.VISIBLE);
            Button callerRedBtn = findViewById(R.id.callerRedBtn);
            callerRedBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSession.sendSignal(IMApp.REJECT_VIDEO, "");
//                    finish();
                }
            });
            videoCallerAnim.start();

            // on connected to session we will send request to callee
        } else {
            // Check current session.
            if (!sendRequest(IMApp.CHECK_SESSION)) {
                finish();
            }
            badgeTitleToShow.setText(badgeTitle);
            videoCalleeAnim.start();

            // show callee layout (red/green buttons)
            callerLayout.setVisibility(View.GONE);
            calleeLayout.setVisibility(View.VISIBLE);

            // play ringtone sound
            Uri ring = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE);
            mp = MediaPlayer.create(this, ring);
            if (mp != null) {
                mp.setLooping(true);
                mp.start();
            }

            final Button calleeRedBtn = findViewById(R.id.calleeRedBtn);
            calleeRedBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
                    calleeRedBtn.startAnimation(anim);
                    mSession.sendSignal(IMApp.REJECT_VIDEO, "");
                    finish();
                }
            });

            Button calleeGreenBtn = findViewById(R.id.calleeGreenBtn);
            Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.call_scale);
            calleeGreenBtn.startAnimation(anim);
            calleeGreenBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mp != null) {
                        mp.stop();
                    }
                    callerLayout.setVisibility(View.GONE);
                    calleeLayout.setVisibility(View.GONE);
                    chatLayout.setVisibility(View.VISIBLE);
                    publish();
                }
            });
        }

        // Stop Call
        final Button redBtn = findViewById(R.id.stopCall);
        redBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
                redBtn.startAnimation(anim);
                mSession.sendSignal(IMApp.REJECT_VIDEO, "");
            }
        });

        // start auto disconnection handler
        IMApp.isConnection = true;
        isRequestProcess = true;
        rHandler.postDelayed(new Runnable() {
            public void run() {
                if (isRequestProcess) {
                    mSession.sendSignal(IMApp.REJECT_VIDEO, "");
                    finish();
                    //Toast.makeText(VideoChatActivity.this, "", Toast.LENGTH_LONG).show();
                }
            }
        }, call_timeout * 1000);
    }

    public void publish() {
        if (mPublisher == null) {
            if (toneGenerator != null) {
                toneGenerator.stopTone();
            }
            mPublisherListener = new PublisherListener();
            mPublisher = new Publisher(VideoChatActivity.this, "publisher");
            mPublisher.setPublisherListener(mPublisherListener);
            mPublisherViewContainer.addView(mPublisher.getView());
            mSession.publish(mPublisher);
            // startTimer
            chronometer.setBase(SystemClock.elapsedRealtime());
            //chronometer.setFormat("Time: %s");
            seconds = 0;
            chronometer.start();
        }
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                onBackPressed();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    @Override
    public void onPause() {
        super.onPause();

        if (mSession != null) {
            mSession.onPause();

            if (mSubscriber != null) {
                mSubscriberViewContainer.removeView(mSubscriber.getView());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mSession != null) {
            mSession.onResume();
            reloadInterface();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBatteryTracker, filter);
    }

    @Override
    public void onStop() {
        super.onStop();

        unregisterReceiver(mBatteryTracker);
        IMApp.isConnection = false;

        if (isFinishing()) {
            if (mSession != null) {
                mSession.disconnect();
            }
        }
    }

    @Override
    public void onDestroy() {
        if (mSession != null) {
            mSession.disconnect();
        }
        super.onDestroy();
        IMApp.isConnection = false;
        if (toneGenerator != null) {
            toneGenerator.stopTone();
        }
        if (mp != null) {
            mp.stop();
        }
        isRequestProcess = false;
        finish();
    }

    @Override
    public void onBackPressed() {
//        if (mSession != null) {
//            mSession.disconnect();
//        }
//        IMApp.isConnection = false;
        //super.onBackPressed();
    }

    public void reloadInterface() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSubscriber != null) {
                    attachSubscriberView(mSubscriber);
                }
            }
        }, 500);
    }


    private void sessionInit() {
        // check for permissions "android.permission.CAMERA", "android.permission.RECORD_AUDIO"
        int cameraPermissionCheck = ContextCompat.checkSelfPermission(VideoChatActivity.this,
                Manifest.permission.CAMERA);
        int audioPermissionCheck = ContextCompat.checkSelfPermission(VideoChatActivity.this,
                Manifest.permission.RECORD_AUDIO);

        if ((cameraPermissionCheck != PackageManager.PERMISSION_GRANTED)
                || (audioPermissionCheck != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(VideoChatActivity.this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    OT_AUDIO_VIDEO_PERMISSIONS);
        } else {
            sessionConnect();
        }
    }

    private void sessionConnect() {
        if (mSession == null) {
            mSessionListener = new SessionListener();
            mSession = new OpenTokSession(VideoChatActivity.this,
                    getString(R.string.opentok_api_key), SESSION_ID);
            mSession.setSessionListener(mSessionListener);
            mSession.setConnectionListener(mSessionListener);
            mSession.setSignalListener(mSessionListener);
            mSession.setReconnectionListener(mSessionListener);
            mSession.connect(TOKEN);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case OT_AUDIO_VIDEO_PERMISSIONS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    sessionConnect();
                } else {
                    // permission denied
                    showPermissionsDialog();
                }
            }
        }
    }

    private void subscribeToStream(Stream stream) {
        mSubscriberListener = new SubscriberListener();
        mSubscriber = new Subscriber(VideoChatActivity.this, stream);
        mSubscriber.setVideoListener(mSubscriberListener);
        mSubscriber.setStreamListener(mSubscriberListener);
        mSession.subscribe(mSubscriber);

        if (mSubscriber.getSubscribeToVideo()) {
            // start loading spinning
            callerLayout.setVisibility(View.GONE);
            calleeLayout.setVisibility(View.GONE);
            chatLayout.setVisibility(View.VISIBLE);
        }
    }

    private void unsubscribeFromStream(Stream stream) {
        mStreams.remove(stream);
        if (mSubscriber.getStream().equals(stream)) {
            mSubscriberViewContainer.removeView(mSubscriber.getView());
            mSubscriber = null;
            if (!mStreams.isEmpty()) {
                subscribeToStream(mStreams.get(0));
            }
        }
    }

    private void attachSubscriberView(Subscriber subscriber) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
        View view = subscriber.getView();
        mSubscriberViewContainer.removeView(view);
        mSubscriberViewContainer.addView(view, layoutParams);
        subscriber.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL);
    }

    private void showReconnectionDialog(boolean show) {
        if (show) {
            mSessionDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mSessionDialog.setMessage("Reconnecting. Please wait...");
            mSessionDialog.setIndeterminate(true);
            mSessionDialog.setCanceledOnTouchOutside(false);
            mSessionDialog.show();
        } else {
            mSessionDialog.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(VideoChatActivity.this);
            builder.setMessage("Session has been reconnected")
                    .setPositiveButton(android.R.string.ok, null);
            builder.create();
            builder.show();
        }
    }

    private void showSubscriberReconnectionDialog(boolean show) {
        if (show) {
            mSubscriberDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mSubscriberDialog.setMessage("Subscriber is reconnecting. Please wait...");
            mSubscriberDialog.setIndeterminate(true);
            mSubscriberDialog.setCanceledOnTouchOutside(false);
            mSubscriberDialog.show();
        } else {
            mSubscriberDialog.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(VideoChatActivity.this);
            builder.setMessage("Subscriber has been reconnected")
                    .setPositiveButton(android.R.string.ok, null);
            builder.create();
            builder.show();
        }
    }

    private void showPermissionsDialog() {
        if (mPermissionsDialog != null) {
            mPermissionsDialog.show();
            return;
        }

        mPermissionsDialog = new AlertDialog.Builder(
                VideoChatActivity.this).create();
        mPermissionsDialog.setTitle("Permissions Needed");
        mPermissionsDialog.setMessage("You need to grant this sample app both the Camera and Audio Recording Permissions for it to work.");
        mPermissionsDialog.setCancelable(false);
        mPermissionsDialog.setCanceledOnTouchOutside(false);
        mPermissionsDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mPermissionsDialog.dismiss();
                        sessionInit();
                    }
                });
        mPermissionsDialog.show();
    }


    /**
     * Converts dp to real pixels, according to the screen density.
     *
     * @param dp A number of density-independent pixels.
     * @return The equivalent number of real pixels.
     */
    private int dpToPx(int dp) {
        double screenDensity = this.getResources().getDisplayMetrics().density;
        return (int) (screenDensity * (double) dp);
    }

    /**
     * BroadcastReceiver is used for receiving intents from the BatteryManager when the battery changed
     */
    private BroadcastReceiver mBatteryTracker = new BroadcastReceiver() {

        public void onReceive(Context context, Intent batteryStatus) {

            String action = batteryStatus.getAction();

            // information received from BatteryManager
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {

                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                float batteryUsePct = level / (float) scale;

                // Send info about the battery consumption. if the session is reconnecting,
                // we will not send this info (retryAfterReconnection to false), because the
                // battery consumption will not be the same.
                if (mSession != null) {
                    mSession.sendSignal("signal", String.valueOf(batteryUsePct), false);
                }
            }
        }
    };

    public boolean sendRequest(String type) {
        String response;
        JSONObject resp;
        try {
            switch (type) {
                case IMApp.REQUEST_VIDEO:
                    response = client.setUrl(SettingsHelper.getUrl(this, "signal_request_video"))
                            .setMethod("POST")
                            .addField("session", SESSION_ID)
                            .addField("token", TOKEN)
                            .addField("id", calleeId)
                            .send();
                    resp = new JSONObject(response);
                    if (resp.opt("error") != null) {
                        String message = resp.optString("message");
                        if (message.isEmpty()) {
                            message = "Error: " + resp.opt("status");
                        }
                        throw new Exception(message);
                    }

                    // show caller layout (red button)
                    callerLayout.setVisibility(View.VISIBLE);
                    calleeLayout.setVisibility(View.GONE);
                    loadingLayout.setVisibility(View.GONE);

                    // play call_anim sound
                    toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_NETWORK_USA_RINGBACK, call_timeout * 1000);

                    // waiting the callee
                    break;
                case IMApp.CHECK_SESSION:
                    response = client.setUrl(SettingsHelper.getUrl(VideoChatActivity.this, "signal_check"))
                            .setMethod("POST")
                            .addField("session", SESSION_ID)
                            .addField("token", TOKEN)
                            .send();
                    resp = new JSONObject(response);
                    if (resp.opt("error") != null) {
                        String message = resp.optString("message");
                        if (message.isEmpty()) {
                            message = "Error: " + resp.opt("status");
                        }
                        throw new Exception(message);
                    } else {
                        JSONObject data = (JSONObject) resp.get("data");
                        badgeTitle = data.getString("title");
                        callerAvatar = data.getString("caller_avatar");
                        if (!callerAvatar.isEmpty()) {
                            callerImage.setImageURI(callerAvatar);
                        } else {
                            callerImage.setImageResource(R.mipmap.ic_launcher_round);
                        }
                    }
                    break;
                case IMApp.FINISH_VIDEO:
                    response = client.setUrl(SettingsHelper.getUrl(VideoChatActivity.this, "signal_completed"))
                            .setMethod("POST")
                            .addField("session", SESSION_ID)
                            .addField("token", TOKEN)
                            .addField("seconds", seconds.toString())
                            .addField("badge_id", badge_id.toString())
                            .send();
                    resp = new JSONObject(response);
                    if (resp.opt("error") != null) {
                        String message = resp.optString("message");
                        if (message.isEmpty()) {
                            message = "Error: " + resp.opt("status");
                        }
                        throw new Exception(message);
                    } else {
                        // get updated profile
                        ProfileHelper.getProfile(this);
                    }
                    break;
            }
            return true;
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
        return false;
    }

    @Override
    public void onChronometerTick(Chronometer chronometer) {
        seconds++;
    }

    private class OpenTokSession extends Session {
        public OpenTokSession(Context context, String apiKey, String sessionId) {
            super(context, apiKey, sessionId, new Session.SessionOptions() {
            });
        }

        public int getActivePublishers() {
            return activePublishers.size();
        }
    }

    private class SessionListener implements
            Session.ReconnectionListener,
            Session.SessionListener,
            Session.SignalListener,
            Session.ConnectionListener {

        @Override
        public void onConnected(Session session) {
            // save my own OpenTok connection ID
            try {
                client.setUrl(SettingsHelper.getUrl(VideoChatActivity.this, "post_connection"))
                        .setMethod("POST")
                        .addField("connection", mSession.getConnection().getConnectionId())
                        .send();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // caller send notification to callee on ready
            if (!calleeId.isEmpty()) {
                sendRequest(IMApp.REQUEST_VIDEO);
            }
        }

        @Override
        public void onDisconnected(Session session) {
            if (mPublisher != null) {
                mPublisherViewContainer.removeView(mPublisher.getView());
            }

            if (mSubscriber != null) {
                mSubscriberViewContainer.removeView(mSubscriber.getView());
            }

            mPublisher = null;
            mSubscriber = null;
            mStreams.clear();
            mSession = null;
        }

        @Override
        public void onReconnecting(Session session) {
            showReconnectionDialog(true);
        }

        @Override
        public void onReconnected(Session session) {
            showReconnectionDialog(false);
        }

        @Override
        public void onError(Session session, OpentokError exception) {
        }

        @Override
        public void onStreamReceived(Session session, Stream stream) {
            if (!SUBSCRIBE_TO_SELF) {
                isRequestProcess = false;
                mStreams.add(stream);
                if (mSubscriber == null) {
                    subscribeToStream(stream);
                }
                publish();
            }
        }

        @Override
        public void onStreamDropped(Session session, Stream stream) {
            if (!SUBSCRIBE_TO_SELF) {
                if (mSubscriber != null) {
                    unsubscribeFromStream(stream);
                    finish();
                }
            }
        }

        @Override
        public void onConnectionCreated(Session session, Connection connection) {
        }

        @Override
        public void onConnectionDestroyed(Session session, Connection connection) {
        }

        @Override
        public void onSignalReceived(Session session, String type, String data, Connection connection) {
            if (type.equals(IMApp.REJECT_VIDEO)) {
                if (!caller.isEmpty()) {
                    sendRequest(IMApp.FINISH_VIDEO);
                    if (seconds >= min_seconds_to_comment) {
                        Intent intent = new Intent(VideoChatActivity.this, AddCommentActivity.class);
                        intent.putExtra("badge_id", badge_id);
                        startActivity(intent);
                    }
                }
                finish();
            }
        }
    }

    private class PublisherListener implements Publisher.PublisherListener {

        @Override
        public void onStreamCreated(PublisherKit publisher, Stream stream) {
            if (SUBSCRIBE_TO_SELF) {
                mStreams.add(stream);
                if (mSubscriber == null) {
                    subscribeToStream(stream);
                }
            }
        }

        @Override
        public void onStreamDestroyed(PublisherKit publisher, Stream stream) {
            if ((SUBSCRIBE_TO_SELF && mSubscriber != null)) {
                unsubscribeFromStream(stream);
            }
        }

        @Override
        public void onError(PublisherKit publisher, OpentokError exception) {
        }
    }

    public class SubscriberListener implements Subscriber.VideoListener, Subscriber.StreamListener {

        @Override
        public void onVideoDataReceived(SubscriberKit subscriber) {

            // stop loading spinning
            isRequestProcess = false;
            attachSubscriberView(mSubscriber);
        }

        @Override
        public void onVideoDisabled(SubscriberKit subscriber, String reason) {
        }

        @Override
        public void onVideoEnabled(SubscriberKit subscriber, String reason) {
        }

        @Override
        public void onVideoDisableWarning(SubscriberKit subscriber) {
        }

        @Override
        public void onVideoDisableWarningLifted(SubscriberKit subscriber) {
        }

        @Override
        public void onReconnected(SubscriberKit subscriberKit) {
            showSubscriberReconnectionDialog(false);
        }

        @Override
        public void onDisconnected(SubscriberKit subscriberKit) {
            showSubscriberReconnectionDialog(true);
        }
    }
}