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

    private final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private final FirebaseStorage mStorage = FirebaseStorage.getInstance();

    private boolean initialized;

    private final String pieceElementId;

    private PieceState initialState;
    private PieceState currentState;

    private long deckPieceIndex;
    private List<Bitmap> images;
    private String pieceIndex;
    private int currentImageIndex;
    private int height;
    private int width;
    private String GROUP_ID;
    private String MATCH_ID;

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

    GamePiece(DataSnapshot dataSnapshot, String mGameId, String mMatchId, String mGroupId){
        initialized = false;
        images = new ArrayList<>();
        pieceElementId = dataSnapshot.child("pieceElementId").getValue().toString();
        deckPieceIndex = (Long) dataSnapshot.child("deckPieceIndex").getValue();
        initialState = dataSnapshot.child("initialState").getValue(PieceState.class);
        Log.d(TAG, "initial state: " + initialState);
        currentState = initialState;
        this.pieceIndex = dataSnapshot.getKey();
        GROUP_ID = mGroupId;
        MATCH_ID = mMatchId;
        Log.d(TAG, "why");
        mDatabase.getReference("gameBuilder/gameSpecs").child(mGameId).child("board")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "first");
                        final String imageId = dataSnapshot.child("imageId").getValue().toString();
                        //now get the image
                        mDatabase.getReference("gameBuilder/images/" + imageId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot image) {
                                Log.d(TAG, "second");
                                heightScreen = Integer.parseInt(image.child("height").getValue().toString());
                                widthScreen = Integer.parseInt(image.child("width").getValue().toString());
                                getFirebaseData();
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
        getFirebaseData();
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
                                                init(images, height, width);
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            Log.d(TAG, "Game board image read failed: " + exception.getMessage());
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

        FirebaseDatabase.getInstance().getReference("gamePortal/groups/" + GROUP_ID + "/matches/" + MATCH_ID + "/pieces/" + pieceIndex).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentState.setX((int) ((Float.parseFloat(dataSnapshot.child("currentState").child("x").getValue().toString())) / 100 * widthScreen));
                currentState.setY((int)((Float.parseFloat(dataSnapshot.child("currentState").child("y").getValue().toString())) / 100 * heightScreen));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void init(List<Bitmap> images, int height, int width){
        Log.d(TAG, "initializing");
        this.images = images;
        this.height = height;
        this.width = width;
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

    public void setCurrentState(PieceState state) { this.currentState = state; }

    public void setInitialState(PieceState state) { this.initialState = state; }

    public PieceState getCurrentState() { return currentState; }

    public int getHeight() { return height; }

    public int getWidth() {return width; }

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
            //TODO: update status here and on firebase, also listen for changes
        }

        public String toString(){
            return String.format(Locale.US, "%d %d %d %s", x, y, zDepth, currentImageIndex);
        }
    }

}
