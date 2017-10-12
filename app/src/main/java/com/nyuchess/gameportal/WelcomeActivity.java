package com.nyuchess.gameportal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "WelcomeActivity";
    TextView mWelcomeTextView;
    private FirebaseAuth mAuth;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private String username;
    private GoogleApiClient mGoogleApiClient;

    private String UID;
    private TextView mOnlineViewerCountTextView;
    private int mOnlineViewerCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        username = getIntent().getStringExtra("USERNAME");

        mWelcomeTextView = (TextView) findViewById(R.id.welcome);
        mOnlineViewerCountTextView = (TextView) findViewById(R.id.users_online);

        findViewById(R.id.chat_button).setOnClickListener(this);
        findViewById(R.id.people_button).setOnClickListener(this);
        findViewById(R.id.sign_out).setOnClickListener(this);

        mWelcomeTextView.setText("Welcome, " + username);

        mAuth = FirebaseAuth.getInstance();

        UID = mAuth.getCurrentUser().getUid();

        DatabaseReference ref = database.getReference("/users");

        Map<String, Object> pub = new HashMap<>();
        pub.put("avatarImageUrl", "https://i0.wp.com/vanillicon.com/f6562c80843af5711653fc76170ddfd6_200.png?ssl=1");
        String disp = mAuth.getCurrentUser().getDisplayName();
        if(disp == null || disp.length() < 1) {
            disp = "TBD";
        }
        pub.put("displayName", disp);
        pub.put("isConnected", true);
        pub.put("lastSeen", ServerValue.TIMESTAMP);

        Map<String, Object> priv = new HashMap<>();
        priv.put("email", mAuth.getCurrentUser().getEmail());
        priv.put("createdOn", ServerValue.TIMESTAMP);


        Map<String, Object> fields = new HashMap<>();
        fields.put("publicFields", pub);
        fields.put("privateFields", priv);

        ref.child(mAuth.getCurrentUser().getUid()).updateChildren(fields);

        initialiseOnlinePresence();

        //Add to global chat
        DatabaseReference chats = database.getReference("/chats");

        Map<String, Object> inChat = new HashMap<>();
        inChat.put(UID, true);

        Map<String, Object> chat = new HashMap<>();
        chat.put("participants", inChat);
        chat.put("groupName", "Global");
        chat.put("createdOn", ServerValue.TIMESTAMP);

        chats.child("GlobalChat").updateChildren(chat);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if(i == R.id.chat_button) {
            Intent intent = new Intent(this, ChatsListActivity.class);
            startActivity(intent);
        } else if(i == R.id.people_button) {
            Intent intent = new Intent(this, UsersActivity.class);
            startActivity(intent);
        } else if(i == R.id.sign_out) {
            signOut();
        }
    }

    @Override
    protected void onStart() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        super.onStart();
    }

    public void signOut() {
        mAuth.signOut();

        String[] data = username.split(" ");

        DatabaseReference pubRef = database.getReference("/users/" + data[1] + "/publicFields");

        pubRef.child("isConnected").setValue("false");
        pubRef.child("lastSeen").setValue(ServerValue.TIMESTAMP);

        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(intent);

        if ( mGoogleApiClient.isConnected() ) {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            // ...
                        }
                    });
        }
    }

    private void initialiseOnlinePresence() {
        mOnlineViewerCount = 0;
        final DatabaseReference databaseReference = database.getReference();

        final DatabaseReference onlineRef = databaseReference.child(".info/connected");
        final DatabaseReference currentUserRef = databaseReference.child("/recentlyConnected/" + UID);
        final DatabaseReference statusRef = databaseReference.child("/users/" + UID + "/publicFields/isConnected");
        final DatabaseReference lastSeenRef = databaseReference.child("/users/" + UID + "/publicFields/lastSeen");

        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                Log.d(TAG, "DataSnapshot:" + dataSnapshot);
                if (dataSnapshot.getValue(Boolean.class)) {
                    lastSeenRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
                    //statusRef.onDisconnect().setValue("false");
                    //currentUserRef.onDisconnect().removeValue();
                    //currentUserRef.setValue(true);
                }
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                Log.d(TAG, "DatabaseError:" + databaseError);
            }
        });
        final DatabaseReference onlineViewersCountRef = databaseReference.child("/recentlyConnected");
        onlineViewersCountRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                mOnlineViewerCount = 0;
                Log.d(TAG, " NUMBER USERS " + dataSnapshot.getChildrenCount() + " " + mOnlineViewerCount);
                for (DataSnapshot user: dataSnapshot.getChildren()){
                    if(((int) dataSnapshot.getChildrenCount()) > 20) {
                        user.getRef().removeValue();
                        return;
                    }
                    final String userid = (String) user.child("uid").getValue();
                    DatabaseReference isOnlineRef = databaseReference.child("/users/" + userid + "/publicFields");
                    isOnlineRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.child("isConnected").getValue() != null) {
                                Log.d(TAG, dataSnapshot.child("isConnected").getValue().toString());
                                if(dataSnapshot.child("isConnected").getValue().toString().equals("true")) {
                                    addPeople();
                                    Log.d(TAG, "adding " + mOnlineViewerCount + " " + userid);
                                    mOnlineViewerCountTextView.setText("Users Online: " + mOnlineViewerCount);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                Log.d(TAG, "DatabaseError:" + databaseError);
            }
        });

        final DatabaseReference recentRef = databaseReference.child("/recentlyConnected");
        Map<String, Object> me = new HashMap<>();
        me.put("uid", UID);
        me.put("timestamp", ServerValue.TIMESTAMP);
        recentRef.push().setValue(me);

    }

    private void addPeople() {
        mOnlineViewerCount++;
    }
}
