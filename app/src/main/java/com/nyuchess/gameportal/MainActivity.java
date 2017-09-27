package com.nyuchess.gameportal;

import android.content.Intent;
import android.hardware.camera2.params.Face;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void doEmailLogin(View view){
        Intent intent = new Intent(this, EmailLoginActivity.class);
        startActivity(intent);
    }

    public void doPhoneLogin(View view){
        Intent intent = new Intent(this, PhoneLoginActivity.class);
        startActivity(intent);
    }

    public void doFacebookLogin(View view){
        Intent intent = new Intent(this, FacebookLoginActivity.class);
        startActivity(intent);
    }

    public void doTwitterLogin(View view){
        Intent intent = new Intent(this, TwitterLoginActivity.class);
        startActivity(intent);
    }

    public void doGooglePlusLogin(View view){
        Intent intent = new Intent(this, GooglePlusLoginActivity.class);
        startActivity(intent);
    }

    public void doAnonymousLogin(View view){
        Intent intent = new Intent(this, AnonymousLoginActivity.class);
        startActivity(intent);
    }

}
