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
 * Created by Jordan on 10/30/2017.
 */

public class MatchArrayItem {
    private String gameName;
    private String gameId;
    private String matchId;
    private Bitmap image;
    private FirebaseDatabase mDatabase;

    private static final String TAG = "MatchArrayItem";

    MatchArrayItem(String gameName, String gameId, String matchId){
        this.gameName = gameName;
        this.gameId = gameId;
        this.matchId = matchId;
        mDatabase = FirebaseDatabase.getInstance();
        getFirebaseImage();
    }

    String getGameName(){
        return gameName;
    }
    String getgameId() {
        return gameId;
    }
    String getMatchId() {
        return matchId;
    }
    Bitmap getImage(){
        return image;
    }

    private void getFirebaseImage(){
        Log.d(TAG, "Getting image for " + gameName);
        mDatabase.getReference("gameBuilder/gameSpecs/" + gameId + "/gameIcon512x512")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() == null){
                            return;
                        }
                        String imageId = dataSnapshot.getValue().toString();
                        mDatabase.getReference("gameBuilder/images/" + imageId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String downloadURL = dataSnapshot.child("downloadURL").getValue().toString();
                                Log.d(TAG, "downloadURL: " + downloadURL);
                                new DownloadImageTask().execute(downloadURL);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d(TAG, "Game piece image read failed: " + databaseError.getMessage());
                            }
                        });
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
            Log.d(TAG, "got image for match " + gameName);
            image = bitmap;
        }
    }
}
