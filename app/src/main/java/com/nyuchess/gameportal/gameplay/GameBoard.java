package com.nyuchess.gameportal.gameplay;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by Jordan on 10/14/2017.
 */

public class GameBoard implements IGameElement {

    private static final String TAG = "GameBoard";

    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;

    private String mGameId;

    private String imageId;
    private String backgroundColor;
    private long maxScale;

    private Bitmap image;

    private int mHeight;
    private int mWidth;

    private boolean initialized;

    GameBoard(String gameId) {
        mGameId = gameId;
        initialized = false;
    }

    void init() {
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();
        getFirebaseData();
    }

    //Get data from Firebase and call finishInit() once we have it
    private void getFirebaseData(){
        Log.d(TAG, "getGameBoard: " + mGameId);
        mDatabase.getReference("gameBuilder/gameSpecs").child(mGameId).child("board")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "Got data snapshot for game board");
                    Log.d(TAG, mGameId);
                    final String imageId = dataSnapshot.child("imageId").getValue().toString();
                    Log.d(TAG, "imageId: " + imageId);
                    final String backgroundColor = dataSnapshot.child("backgroundColor").getValue().toString();
                    final long maxScale = (Long) dataSnapshot.child("maxScale").getValue();
                    //now get the image
                    mDatabase.getReference("gameBuilder/images/" + imageId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

//                            String cloudStoragePath = dataSnapshot.child("cloudStoragePath").getValue().toString();
//                            Log.d(TAG, "cloudStoragePath: " + cloudStoragePath);
                            String downloadURL = dataSnapshot.child("downloadURL").getValue().toString();
                            Log.d(TAG, "downloadURL: " + downloadURL);
                            new DownloadImageTask().execute(downloadURL);
//                            Bitmap image = ImageLoader.getInstance().loadImageSync(downloadURL);
//                            finishInit(image, backgroundColor, maxScale);
//                            long sizeInBytes = (Long) dataSnapshot.child("sizeInBytes").getValue();
//                            Log.d(TAG, "sizeInBytes: " + sizeInBytes);
//
//                            StorageReference storageRef = mStorage.getReference(cloudStoragePath);
//
//                            storageRef.getBytes(sizeInBytes).addOnSuccessListener(new OnSuccessListener<byte[]>() {
//                                @Override
//                                public void onSuccess(byte[] bytes) {
//                                    Log.d(TAG, "Image read success");
//                                    Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                                    finishInit(image, backgroundColor, maxScale);
//                                }
//                            }).addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception exception) {
//                                    Log.d(TAG, "Game board image read failed: " + exception.getMessage());
//                                }
//                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(TAG, "Game board image read failed: " + databaseError.getMessage());
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "Game board read failed: " + databaseError.getMessage());
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
            finishInit(bitmap);
        }
    }

    //Set the data and set the initialized flag to show we can start drawing
//    private void finishInit(Bitmap image, String backgroundColor, long maxScale){
    private void finishInit(Bitmap image){
        //get actual image using id
        Log.d(TAG, "finishInit");
        this.image = image;
        this.mHeight = image.getHeight();
        this.mWidth = image.getWidth();
//        this.backgroundColor = backgroundColor;
//        this.maxScale = maxScale;
//        Log.d(TAG, "imageId: " + imageId);
//        Log.d(TAG, "backgroundColor " + backgroundColor);
//        Log.d(TAG, "maxScale: " + maxScale);
        initialized = true;
        Log.d(TAG, "Initialization done");
    }

    @Override
    public void draw(Canvas canvas) {
        //wait until initialization is done to avoid NullPointerExceptions
        if (!initialized){
            Log.v(TAG, "not drawing board yet");
            return;
        }
        Log.v(TAG, "drawing board");
        canvas.drawBitmap(image, 0, 0, null);
        // draw shit
    }

    String getGameId() {
        return mGameId;
    }

    int getHeight(){
        return mHeight;
    }

    int getWidth(){
        return mWidth;
    }
}
