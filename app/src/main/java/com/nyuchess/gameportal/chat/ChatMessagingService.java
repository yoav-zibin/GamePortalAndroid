package com.nyuchess.gameportal.chat;

import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Jordan on 11/28/2017.
 */

public class ChatMessagingService extends FirebaseMessagingService {

    private static final String TAG = "ChatMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a body.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data: " + remoteMessage.getData());
            String fromUserId = remoteMessage.getData().get("fromUserId");
            String toUserId = remoteMessage.getData().get("toUserId");
            String groupId = remoteMessage.getData().get("groupId");
            String timestamp = remoteMessage.getData().get("timestamp");
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message notification: " + remoteMessage.getNotification());
            Toast.makeText(getApplicationContext(), remoteMessage.getNotification().getBody(),
                    Toast.LENGTH_SHORT).show();
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
}
