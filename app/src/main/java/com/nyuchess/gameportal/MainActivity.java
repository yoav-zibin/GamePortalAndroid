package com.nyuchess.gameportal;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.nyuchess.gameportal.authentication.AnonymousLoginActivity;
import com.nyuchess.gameportal.authentication.EmailLoginActivity;
import com.nyuchess.gameportal.authentication.FacebookLoginActivity;
import com.nyuchess.gameportal.authentication.GooglePlusLoginActivity;
import com.nyuchess.gameportal.authentication.PhoneLoginActivity;
import com.nyuchess.gameportal.authentication.TwitterLoginActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.email_sign_in_button), iconFont);
        FontManager.markAsIconContainer(findViewById(R.id.phone_sign_in_button), iconFont);
        FontManager.markAsIconContainer(findViewById(R.id.facebook_sign_in_button), iconFont);
        FontManager.markAsIconContainer(findViewById(R.id.twitter_sign_in_button), iconFont);
        FontManager.markAsIconContainer(findViewById(R.id.gplus_sign_in_button), iconFont);

    }

    public void doEmailLogin(View view){
        Log.d("Main", "Doing email login");
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