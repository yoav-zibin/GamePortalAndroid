package com.nyuchess.gameportal.authentication;

import android.app.Activity;
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

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nyuchess.gameportal.util.FontManager;
import com.nyuchess.gameportal.R;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        printhashkey();
        printKeyHash(this);
        setContentView(R.layout.activity_main);
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.email_sign_in_button), iconFont);
        FontManager.markAsIconContainer(findViewById(R.id.phone_sign_in_button), iconFont);
        FontManager.markAsIconContainer(findViewById(R.id.facebook_sign_in_button), iconFont);
        FontManager.markAsIconContainer(findViewById(R.id.twitter_sign_in_button), iconFont);
        FontManager.markAsIconContainer(findViewById(R.id.gplus_sign_in_button), iconFont);

        //set image loader to cache images by default
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true).cacheOnDisk(true).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config);

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

    public static String printKeyHash(Activity context) {
        PackageInfo packageInfo;
        String key = null;
        try {
            //getting application package name, as defined in manifest
            String packageName = context.getApplicationContext().getPackageName();

            //Retriving package info
            packageInfo = context.getPackageManager().getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES);

            Log.e("Package Name=", context.getApplicationContext().getPackageName());

            for (Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                key = new String(Base64.encode(md.digest(), 0));

                // String key = new String(Base64.encodeBytes(md.digest()));
                Log.d("Key Hash=", key);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("Name not found", e1.toString());
        }
        catch (NoSuchAlgorithmException e) {
            Log.e("No such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }

        return key;
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