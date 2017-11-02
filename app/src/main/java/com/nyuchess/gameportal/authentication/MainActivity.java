package com.nyuchess.gameportal.authentication;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.nyuchess.gameportal.util.FontManager;
import com.nyuchess.gameportal.R;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        printhashkey();
        setContentView(R.layout.activity_main);
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.email_sign_in_button), iconFont);
        FontManager.markAsIconContainer(findViewById(R.id.phone_sign_in_button), iconFont);
        FontManager.markAsIconContainer(findViewById(R.id.facebook_sign_in_button), iconFont);
        FontManager.markAsIconContainer(findViewById(R.id.twitter_sign_in_button), iconFont);
        FontManager.markAsIconContainer(findViewById(R.id.gplus_sign_in_button), iconFont);

    }

    public void printhashkey(){

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.nyuchess.gameportal",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

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