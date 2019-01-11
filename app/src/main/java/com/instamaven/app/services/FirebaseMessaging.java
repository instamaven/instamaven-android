/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.instamaven.app.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.instamaven.app.activities.BadgeDetailsActivity;
import com.instamaven.app.activities.IMApp;
import com.instamaven.app.R;
import com.instamaven.app.activities.MainActivity;
import com.instamaven.app.activities.NotificationsActivity;
import com.instamaven.app.activities.VideoChatActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class FirebaseMessaging extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // For more see: https://firebase.google.com/docs/cloud-messaging/concept-options

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            handleMessage(remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            createNotification(remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleMessage(Map<String, String> data) {
        if (data.get("signal") != null) {
            switch (data.get("signal")) {
                case IMApp.REQUEST_VIDEO:
                    if (IMApp.isConnection) {
                        break;
                    }
                    // start callee session
                    String session = data.get("session");
                    String token = data.get("token");
                    if (!session.isEmpty() && !token.isEmpty()) {
                        Intent intent = new Intent(this, VideoChatActivity.class);
                        intent.putExtra("session", session);
                        intent.putExtra("token", token);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    break;
            }
        } else if (data.get("category") != null) {
            switch (data.get("category")) {
                case "badges":
                    String message = data.get("message");
                    String badgeIdString = data.get("badgeId");
                    if (badgeIdString != null && !badgeIdString.isEmpty()) {
                        int badgeId = Integer.parseInt(badgeIdString);
                        if (badgeId > 0) {
                            createBadgeNotification(badgeId, message);
                        }
                    }
                    break;
            }
        }
    }

    /**
     * @param message FCM message body received.
     */
    private void createNotification(String message) {
        String search = getString(R.string.api_url).concat("/badges/");
        int index = message.indexOf(search);
        if (index != -1) {
            // Create and show notification containing badge URL
            int badgeId = Integer.parseInt(message.substring(index + search.length()));
            if (badgeId > 0) {
                createBadgeNotification(badgeId, message);
            }
        } else {
            // Create and show simple push notification
            createTextNotification(message);
        }
    }

    private void createBadgeNotification(int badgeId, String message) {
        Intent intent = new Intent(this, BadgeDetailsActivity.class);
        intent.putExtra("badgeId", badgeId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0 /* Request code */,
                intent,
                PendingIntent.FLAG_ONE_SHOT
        );

        doNotify(message, pendingIntent, IMApp.FCM_BADGES_CHANNEL);
    }

    private void createTextNotification(String message) {
        Intent intent = new Intent(this, NotificationsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0 /* Request code */,
                intent,
                PendingIntent.FLAG_ONE_SHOT
        );
        doNotify(message, pendingIntent, IMApp.FCM_MESSAGES_CHANNEL);
    }

    private void doNotify(String message, PendingIntent pendingIntent, String messageChannel) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, messageChannel)
                        .setSmallIcon(R.drawable.ic_instamaven_logo_round)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(messageChannel,
                    message,
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // 0 - ID of notification
        notificationManager.notify(0, notificationBuilder.build());
    }
}