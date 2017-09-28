package com.nyuchess.gameportal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class WelcomeActivity extends AppCompatActivity {

    private static final String TAG = "WelcomeActivity";
    TextView mWelcomeTextView;
    private FirebaseAuth mAuth;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        String username = getIntent().getStringExtra("USERNAME");

        mWelcomeTextView = (TextView) findViewById(R.id.welcome);
        mWelcomeTextView.setText("Welcome, " + username);

        mAuth = FirebaseAuth.getInstance();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        StorageReference pubInfo = storageRef.child("/user/$user_id/public_fields");
        StorageReference privInfo = storageRef.child("/user/$user_id/private_fields");

        String[] data = username.split(" ");

        DatabaseReference pubRef = database.getReference("/user/" + data[1] + "/public_fields");
        DatabaseReference privRef = database.getReference("/user/" + data[1] + "/private_fields");

        pubRef.child("Email").setValue(data[0]);
        privRef.child("UID").setValue(data[1]);

        String path = storageRef.getPath();
        Log.d(TAG, path);
        Log.d(TAG, data[0]);
        Log.d(TAG, data[1]);
        Log.d(TAG, "END OF PATHS");
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    public void SignOut(View v) {
        mAuth.signOut();

        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(intent);
    }
}
