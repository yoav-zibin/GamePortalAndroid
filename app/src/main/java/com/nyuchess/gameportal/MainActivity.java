package com.nyuchess.gameportal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button mEmailButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEmailButton = (Button) findViewById(R.id.email_sign_in_button);
    }

    public void doEmailLogin(View view){
        Intent intent = new Intent(this, EmailLoginActivity.class);
        startActivity(intent);
    }


}
