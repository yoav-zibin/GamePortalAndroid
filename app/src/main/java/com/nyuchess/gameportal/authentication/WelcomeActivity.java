package com.nyuchess.gameportal.authentication;

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
import com.nyuchess.gameportal.R;
import com.nyuchess.gameportal.chat.ChatsListActivity;
import com.nyuchess.gameportal.groups.UsersActivity;
import com.twitter.sdk.android.core.TwitterCore;
import com.nyuchess.gameportal.groups.MatchActivity;

import java.util.ArrayList;
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
    private String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        provider = getIntent().getStringExtra("PROVIDER");
        if(provider == null) {
            provider = "";
        }
        mWelcomeTextView = (TextView) findViewById(R.id.welcome);
        mOnlineViewerCountTextView = (TextView) findViewById(R.id.users_online);

        findViewById(R.id.group_button).setOnClickListener(this);
        findViewById(R.id.people_button).setOnClickListener(this);
        findViewById(R.id.sign_out).setOnClickListener(this);
        findViewById(R.id.start_match).setOnClickListener(this);


        mAuth = FirebaseAuth.getInstance();

        UID = mAuth.getCurrentUser().getUid();

        DatabaseReference ref = database.getReference("/users");

        Map<String, Object> pub = new HashMap<>();

        String pic = "https://st2.depositphotos.com/6541572/12372/v/950/depositphotos_123726864-stock-illustration-anonymous-male-profile-picture-emotion.jpg";
        pub.put("avatarImageUrl", (mAuth.getCurrentUser().getPhotoUrl() == null) ? pic : mAuth.getCurrentUser().getPhotoUrl().toString());
        pic = null;
        pub.put("displayName", (mAuth.getCurrentUser().getDisplayName() == null || mAuth.getCurrentUser().getDisplayName().length() < 1 ) ? "TBD" : mAuth.getCurrentUser().getDisplayName());
        pub.put("isConnected", true);
        pub.put("lastSeen", ServerValue.TIMESTAMP);

        Map<String, Object> priv = new HashMap<>();
        priv.put("email", (mAuth.getCurrentUser().getEmail() == null) ? "" : mAuth.getCurrentUser().getEmail());
        priv.put("createdOn", ServerValue.TIMESTAMP);
        priv.put("phoneNumber", (mAuth.getCurrentUser().getPhoneNumber() == null) ? "" : mAuth.getCurrentUser().getPhoneNumber());
        priv.put("facebookId", "");
        priv.put("googleId", "");
        priv.put("twitterId", "");
        priv.put("githubId", "");
        priv.put("pushNotificationsToken", "");

        Log.d(TAG, "AUSSIE AUSSIE AUSSIE");
        Log.d(TAG, mAuth.getCurrentUser().getProviderData().toString());
        Log.d(TAG, mAuth.getCurrentUser().getProviderId());
        Log.d(TAG, "OI OI OI");

        Map<String, Object> fields = new HashMap<>();
        fields.put("publicFields", pub);
        fields.put("privateFields", priv);

        ref.child(mAuth.getCurrentUser().getUid()).updateChildren(fields);

        username = mAuth.getCurrentUser().getDisplayName();
        mWelcomeTextView.setText("Welcome, " + pub.get("displayName"));
        initialiseOnlinePresence();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if(i == R.id.group_button) {
            Intent intent = new Intent(this, ChatsListActivity.class);
            startActivity(intent);
        } else if(i == R.id.people_button) {
            Intent intent = new Intent(this, UsersActivity.class);
            startActivity(intent);
        } else if(i == R.id.sign_out) {
            signOut();
        } else if(i == R.id.start_match) {
            Intent intent = new Intent(this, MatchActivity.class);
            intent.putExtra("QUICKSTART", "true");
            startActivity(intent);
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

        DatabaseReference pubRef = database.getReference("/users/" + UID + "/publicFields");

        pubRef.child("isConnected").setValue(false);
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
        if(provider.equals("TWITTER")) {
            TwitterCore.getInstance().getSessionManager().clearActiveSession();
        }
    }

    private void initialiseOnlinePresence() {
        mOnlineViewerCount = 0;
        final DatabaseReference databaseReference = database.getReference();

        final DatabaseReference onlineRef = databaseReference.child(".info/connected");
        final DatabaseReference statusRef = databaseReference.child("/users/" + UID + "/publicFields/isConnected");
        final DatabaseReference lastSeenRef = databaseReference.child("/users/" + UID + "/publicFields/lastSeen");

        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                Log.d(TAG, "DataSnapshot:" + dataSnapshot);
                if (dataSnapshot.getValue(Boolean.class)) {
                    lastSeenRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
                    statusRef.onDisconnect().setValue(false);
                    //currentUserRef.onDisconnect().removeValue();
                    //currentUserRef.setValue(true);
                }
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                Log.d(TAG, "DatabaseError:" + databaseError);
            }
        });

        final DatabaseReference onlineViewersCountRef = databaseReference.child("/gamePortal/recentlyConnected");
        onlineViewersCountRef.addValueEventListener(new ValueEventListener() {
            private ArrayList<String> online;
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                online = new ArrayList<>();
                Log.d(TAG, " NUMBER USERS " + dataSnapshot.getChildrenCount() + " " + mOnlineViewerCount);
                for (final DataSnapshot user: dataSnapshot.getChildren()) {
                    final String userid = (String) user.child("userId").getValue();
                    if(!userid.equals(UID)) {
                        Log.d(TAG, UID + " " + userid);
                        DatabaseReference isOnlineRef = databaseReference.child("/users/" + userid + "/publicFields");
                        isOnlineRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child("isConnected").getValue() != null) {
                                    Log.d(TAG, dataSnapshot.child("isConnected").getValue().toString());
                                    dataSnapshot.child("isConnected").getRef().addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Log.d(TAG, dataSnapshot.toString());
                                            addPeople();
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                Log.d(TAG, "DatabaseError:" + databaseError);
            }
        });

        final DatabaseReference recentRef = databaseReference.child("/gamePortal/recentlyConnected");
        Map<String, Object> me = new HashMap<>();
        me.put("userId", UID);
        me.put("timestamp", ServerValue.TIMESTAMP);
        recentRef.child(UID).updateChildren(me);

    }

    private void addPeople() {
        final DatabaseReference databaseReference = database.getReference();
        final DatabaseReference onlineViewersCountRef = databaseReference.child("/gamePortal/recentlyConnected");
        onlineViewersCountRef.addValueEventListener(new ValueEventListener() {
            private ArrayList<String> online;
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                online = new ArrayList<>();
                Log.d(TAG, " NUMBER USERS " + dataSnapshot.getChildrenCount() + " " + mOnlineViewerCount);
                for (final DataSnapshot user: dataSnapshot.getChildren()) {
                    final String userid = (String) user.child("userId").getValue();
                    if(!userid.equals(UID)) {
                        Log.d(TAG, UID + " " + userid);
                        DatabaseReference isOnlineRef = databaseReference.child("/users/" + userid + "/publicFields");
                        isOnlineRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child("isConnected").getValue() != null) {
                                    Log.d(TAG, dataSnapshot.child("isConnected").getValue().toString());

                                    if (dataSnapshot.child("isConnected").getValue().toString().equals("true")) {
                                        if(!online.contains(userid)) {
                                            online.add(userid);
                                        }
                                        mOnlineViewerCountTextView.setText("Users Online: " + online.size());
                                        Log.d(TAG, ""+ online.size());
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                Log.d(TAG, "DatabaseError:" + databaseError);
            }
        });
    }
}
