package com.nyuchess.gameportal.chat;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jordan on 11/28/2017.
 */

public class ChatInstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG = "ChatInstanceIdService";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token){
        Log.d(TAG, "Sending token: " + token);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null){
            Log.d(TAG, "No current user");
            return;
        }
        String uid = user.getUid();
        DatabaseReference tokenRef = database.
                getReference("users/" + uid + "privateFields/fcmTokens").child(token);
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("platform", "android");
        tokenMap.put("app", "GamePortalAndroid");
        tokenMap.put("createdOn", ServerValue.TIMESTAMP);
        tokenMap.put("lastTimeReceived", ServerValue.TIMESTAMP);
        tokenRef.setValue(tokenMap);
    }

}
