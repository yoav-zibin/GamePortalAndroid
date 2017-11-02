package com.nyuchess.gameportal.gameplay;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Jordan on 10/14/2017.
 */

public class GamePiece implements IGameElement {

    private static final String TAG = "GamePiece";

    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;

    private boolean initialized;

    private String pieceElementId;

    private PieceState initialState;
    private PieceState currentState;

    private long deckPieceIndex;
    private List<Bitmap> images;
    private String pieceIndex;
    private int currentImageIndex;
    private int mHeight;
    private int mWidth;
    private String mGroupId;
    private String mMatchId;
    private String mGameId;

    private int heightScreen = 1;
    private int widthScreen = 1;

    public PieceState getInitialState() {
        return initialState;
    }

    public String getPieceElementId() {
        return pieceElementId;
    }

    public long getDeckPieceIndex() {
        return deckPieceIndex;
    }
    // which players can see this card
    // private int[] cardVisibility;

    GamePiece(String gameId, String matchId, String groupId){
        initialized = false;
        mGroupId = groupId;
        mMatchId = matchId;
        mGameId = gameId;
        images = new ArrayList<>();
    }

    void startInit(DataSnapshot dataSnapshot){
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();
        pieceElementId = dataSnapshot.child("pieceElementId").getValue().toString();
        deckPieceIndex = (Long) dataSnapshot.child("deckPieceIndex").getValue();
        initialState = dataSnapshot.child("initialState").getValue(PieceState.class);
        Log.d(TAG, "initial state: " + initialState);
        currentState = initialState;
        this.pieceIndex = dataSnapshot.getKey();
        mDatabase.getReference("gameBuilder/gameSpecs").child(mGameId).child("board")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String imageId = dataSnapshot.child("imageId").getValue().toString();
                        //now get the image
                        mDatabase.getReference("gameBuilder/images/" + imageId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot image) {
                                heightScreen = Integer.parseInt(image.child("height").getValue().toString());
                                widthScreen = Integer.parseInt(image.child("width").getValue().toString());
                                //get the actual image itself
                                getFirebaseData();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d(TAG, "Game piece image read failed: " + databaseError.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "Game piece read failed: " + databaseError.getMessage());
                    }
                });
    }

    private void getFirebaseData() {
        mDatabase.getReference("gameBuilder/elements").child(pieceElementId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final int height = dataSnapshot.child("height").getValue(Integer.class);
                        final int width = dataSnapshot.child("width").getValue(Integer.class);
                        Log.d(TAG, "Height x Width: " + height + " x " + width);
                        Log.d(TAG, "Got data snapshot for piece element " + pieceElementId);
                        //now get the actual images
                        final int imageCount = (int) dataSnapshot.child("images").getChildrenCount();
                        for (DataSnapshot imageIdDs : dataSnapshot.child("images").getChildren()) {
                            String imageId = imageIdDs.child("imageId").getValue().toString();
                            mDatabase.getReference("gameBuilder/images/" + imageId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String cloudStoragePath = dataSnapshot.child("cloudStoragePath").getValue().toString();
                                    Log.d(TAG, "cloudStoragePath: " + cloudStoragePath);
                                    long sizeInBytes = (Long) dataSnapshot.child("sizeInBytes").getValue();
                                    Log.d(TAG, "sizeInBytes: " + sizeInBytes);

                                    StorageReference storageRef = mStorage.getReference(cloudStoragePath);
                                    storageRef.getBytes(sizeInBytes).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                        @Override
                                        public void onSuccess(byte[] bytes) {
                                            Log.d(TAG, "Image read success");
                                            Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                            images.add(image);
                                            Log.d(TAG, "got " + images.size() + " images");
                                            //check to see if all images have been gotten before initializing
                                            if (images.size() == imageCount) {
                                                finishInit(images, height, width);
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            Log.d(TAG, "Game piece image read failed: " + exception.getMessage());
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.d(TAG, "Game piece image read failed: " + databaseError.getMessage());
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "Game piece read failed: " + databaseError.getMessage());
                    }
                });

    }

    private void finishInit(List<Bitmap> images, int height, int width){
        Log.d(TAG, "initializing");
        this.images = images;
        this.mHeight = height;
        this.mWidth = width;

        //add listener for changing state
        FirebaseDatabase.getInstance().getReference(
                "gamePortal/groups/" + mGroupId + "/matches/" + mMatchId + "/pieces/" + pieceIndex)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int x = (int) ((Float.parseFloat(dataSnapshot.child("currentState")
                                .child("x").getValue().toString())) / 100 * widthScreen);
                        int y = (int)((Float.parseFloat(dataSnapshot.child("currentState")
                                .child("y").getValue().toString())) / 100 * heightScreen);
                        int zDepth = (int)((Float.parseFloat(dataSnapshot.child("currentState")
                                .child("zDepth").getValue().toString())) / 100 * heightScreen);
                        int currentImageIndex = (int)((Float.parseFloat(dataSnapshot.child("currentState")
                                .child("currentImageIndex").getValue().toString())) / 100 * heightScreen);
                        currentState.update(x, y, zDepth, currentImageIndex);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        initialized = true;
    }

    @Override
    public void draw(Canvas canvas) {
        if (!initialized){
            Log.v(TAG, "not yet drawing piece " + pieceElementId);
            return;
        }
        Log.v(TAG, "drawing piece " + pieceElementId + " at x:y " + currentState.getX() + ":" + currentState.getY());
        canvas.drawBitmap(images.get(currentImageIndex), currentState.getX(), currentState.getY(), null);
    }

    public String getGroupId() {
        return mGroupId;
    }

    public String getMatchId() {
        return mMatchId;
    }

    public String getGameId() {
        return mGameId;
    }

    public void setCurrentState(PieceState state) { this.currentState = state; }

    public void setInitialState(PieceState state) { this.initialState = state; }

    public PieceState getCurrentState() { return currentState; }

    public int getHeight() { return mHeight; }

    public int getWidth() {return mWidth; }

    public String getPieceIndex() { return pieceIndex; }

    static public class PieceState {
        private int x;
        private int y;
        private int zDepth;
        private int currentImageIndex;

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public void setX(int x) { this.x = x; }

        public void setY(int y) { this.y = y; }

        public int getzDepth() {
            return zDepth;
        }

        public int getCurrentImageIndex() {
            return currentImageIndex;
        }

        public void update(int x, int y, int zDepth, int imageIndex){
            this.x = x;
            this.y = y;
            this.zDepth = zDepth;
            this.currentImageIndex = imageIndex;
        }

        public String toString(){
            return String.format(Locale.US, "%d %d %d %s", x, y, zDepth, currentImageIndex);
        }

        PieceState(){
            //no-arg constructor for Firebase
        }

        public PieceState(int x, int y, int zDepth, int currentImageIndex){
            this.x = x;
            this.y = y;
            this.zDepth = zDepth;
            this.currentImageIndex = currentImageIndex;
        }
    }

}
