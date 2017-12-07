package com.nyuchess.gameportal.groups;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by Jordan on 10/5/2017.
 */

public class User {

    private String displayName;
    private String uid;
    private Bitmap image;
    private FirebaseDatabase mDatabase;

    private static final String TAG = "User";

    User(String name, String id){
        displayName = name;
        uid = id;
        mDatabase = FirebaseDatabase.getInstance();
        getFirebaseImage();
    }

    String getDisplayName(){
        return displayName;
    }
    String getUid() { return uid; }

    Bitmap getImage(){
        return image;
    }

    private void getFirebaseImage(){
        Log.d(TAG, "Getting image for " + displayName);
        mDatabase.getReference("users/" + uid + "/publicFields/avatarImageUrl")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String imageUrl = dataSnapshot.getValue().toString();
                        Log.d(TAG, imageUrl);
                        new DownloadImageTask().execute(imageUrl);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "Avatar image read failed: " + databaseError.getMessage());
                    }
                });
    }

    private class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            return ImageLoader.getInstance().loadImageSync(strings[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            Log.d(TAG, "got image for " + displayName);
            image = bitmap;
        }
    }

}
