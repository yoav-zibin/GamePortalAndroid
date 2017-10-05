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

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "WelcomeActivity";
    TextView mWelcomeTextView;
    private FirebaseAuth mAuth;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private String username;
    private GoogleApiClient mGoogleApiClient;

    private String UID;
    private TextView onlineViewerCountTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        username = getIntent().getStringExtra("USERNAME");

        mWelcomeTextView = (TextView) findViewById(R.id.welcome);
        onlineViewerCountTextView = (TextView) findViewById(R.id.users_online);

        findViewById(R.id.chat_button).setOnClickListener(this);
        findViewById(R.id.people_button).setOnClickListener(this);
        findViewById(R.id.sign_out).setOnClickListener(this);

        mWelcomeTextView.setText("Welcome, " + username);

        mAuth = FirebaseAuth.getInstance();

        String[] data = username.split(" ");

        UID = data[1];

        DatabaseReference pubRef = database.getReference("/users/" + UID + "/public_fields");
        DatabaseReference privRef = database.getReference("/users/" + UID + "/private_fields");

        pubRef.child("avatarImageUrl").setValue("");
        pubRef.child("displayName").setValue(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        pubRef.child("isConnected").setValue("True");
        pubRef.child("lastSeen").setValue("");

        privRef.child("email").setValue(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        initialiseOnlinePresence();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if(i == R.id.chat_button) {
            Intent intent = new Intent(this, ChatActivity.class);
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

        DatabaseReference pubRef = database.getReference("/users/" + data[1] + "/public_fields");

        pubRef.child("isConnected").setValue("False");
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

        database.getReference().child("/presence/" + UID).removeValue();
    }

    private void initialiseOnlinePresence() {
        DatabaseReference databaseReference = database.getReference();

        final DatabaseReference onlineRef = databaseReference.child(".info/connected");
        final DatabaseReference currentUserRef = databaseReference.child("/presence/" + UID);
        final DatabaseReference statusRef = databaseReference.child("/users/" + UID + "/public_fields/isConnected");
        final DatabaseReference lastSeenRef = databaseReference.child("/users/" + UID + "/public_fields/lastSeen");
        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                Log.d(TAG, "DataSnapshot:" + dataSnapshot);
                if (dataSnapshot.getValue(Boolean.class)) {
                    lastSeenRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
                    statusRef.onDisconnect().setValue("False");
                    currentUserRef.onDisconnect().removeValue();
                    currentUserRef.setValue(true);
                }
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                Log.d(TAG, "DatabaseError:" + databaseError);
            }
        });
        final DatabaseReference onlineViewersCountRef = databaseReference.child("/presence");
        onlineViewersCountRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                Log.d(TAG, "DataSnapshot:" + dataSnapshot);
                onlineViewerCountTextView.setText("Users Online: " + String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                Log.d(TAG, "DatabaseError:" + databaseError);
            }
        });
    }
}
