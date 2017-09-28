package com.nyuchess.gameportal;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class WelcomeActivity extends AppCompatActivity {

    TextView mWelcomeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        String username = savedInstanceState.getString("username");

        mWelcomeTextView = (TextView) findViewById(R.id.welcome);
        mWelcomeTextView.setText("Welcome, " + username);
    }
}
