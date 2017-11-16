package com.nyuchess.gameportal.gameplay;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Jordan on 10/14/2017.
 */

public class GamePiece implements IGameElement, Comparable {

    private static final String TAG = "GamePiece";

    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;

    private boolean initialized;

    private String pieceElementId;

    private PieceState initialState;
    //for keeping track of a piece's state while it's being changed but before it's finalized
    //and sent to Firebase
    private PieceState previousState;
    private PieceState currentState;

    private long deckPieceIndex;
    private List<Bitmap> images;
    private String pieceIndex;
    private int mHeight;
    private int mWidth;
    private String mGroupId;
    private String mMatchId;
    private String mGameId;
    private int angle;
    private String type;
    private int heightScreen = 1;
    private int widthScreen = 1;
    private boolean canSee = false;

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
        angle = 0;
        images = new ArrayList<>();
    }

    void startInit(DataSnapshot dataSnapshot){
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();
        pieceElementId = dataSnapshot.child("pieceElementId").getValue().toString();
        deckPieceIndex = (Long) dataSnapshot.child("deckPieceIndex").getValue();
        Log.d("WHAT", dataSnapshot.toString());
        initialState = dataSnapshot.child("initialState").getValue(PieceState.class);
                Log.d(TAG, "initial state: " + initialState);
        if(dataSnapshot.child("initialState").child("cardVisibility") != null) {
            final DataSnapshot children = dataSnapshot;
            DatabaseReference pIndex = mDatabase.getReference("gamePortal/groups/" + mGroupId + "/participants/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
            pIndex.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot2) {
                    String index = dataSnapshot2.child("participantIndex").getValue().toString();
                    for(DataSnapshot child : children.child("initialState").child("cardVisibility").getChildren()) {
                        if(child.getKey().equals(index)) {
                            canSee = true;
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        currentState = dataSnapshot.child("initialState").getValue(PieceState.class);
        Log.w(TAG, "" + currentState);
        previousState = dataSnapshot.child("initialState").getValue(PieceState.class);
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
                                initialState.setX((int) (initialState.getX() / 100.0 * widthScreen));
                                initialState.setY((int) (initialState.getY() / 100.0 * heightScreen));
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

    public void getFirebaseData() {
        mDatabase.getReference("gameBuilder/elements").child(pieceElementId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "Got data snapshot for piece element " + pieceElementId);
                        //now get the actual images
                        type = dataSnapshot.child("elementKind").getValue().toString();
                        if(!type.equals("card")) {
                            canSee = true;
                        }
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
                                                finishInit(images);
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

    private void finishInit(List<Bitmap> images){
        Log.d(TAG, "initializing");
        this.images = images;
        mHeight = images.get(0).getHeight();
        mWidth = images.get(0).getWidth();
        Log.d(TAG, "Height x Width: " + mHeight + " x " + mWidth);

        //add listener for changing state
        FirebaseDatabase.getInstance().getReference(
                "gamePortal/groups/" + mGroupId + "/matches/" + mMatchId + "/pieces/" + pieceIndex)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot1) {
                        final DataSnapshot dataSnapshot = dataSnapshot1;
                        DatabaseReference pIndex = mDatabase.getReference("gamePortal/groups/" + mGroupId + "/participants/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
                        pIndex.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot2) {
                                if(dataSnapshot.child("currentState").child("cardVisibility") != null) {
                                    String index = dataSnapshot2.child("participantIndex").getValue().toString();
                                    for (DataSnapshot child : dataSnapshot.child("currentState").child("cardVisibility").getChildren()) {
                                        if(child.getKey().equals(index)) {
                                            canSee = true;
                                        }
                                    }
                                }
                                int x = (int) ((Float.parseFloat(dataSnapshot.child("currentState")
                                        .child("x").getValue().toString())) / 100 * widthScreen);
                                int y = (int)((Float.parseFloat(dataSnapshot.child("currentState")
                                        .child("y").getValue().toString())) / 100 * heightScreen);
                                int zDepth = ((Long) dataSnapshot.child("currentState").child("zDepth").getValue()).intValue();
                                int currentImageIndex = currentState.getCurrentImageIndex();
                                if(type.equals("card") && canSee) {
                                    currentImageIndex = ((Long) dataSnapshot.child("currentState").child("currentImageIndex").getValue()).intValue();
                                } else if(!type.equals("card")) {
                                    currentImageIndex = ((Long) dataSnapshot.child("currentState").child("currentImageIndex").getValue()).intValue();
                                }
                                updatePreviousState();
                                currentState.update(x, y, zDepth, currentImageIndex);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        initialized = true;
    }

    int getNextImageIndex(){
            int next = (currentState.currentImageIndex + 1) % images.size();
            Log.d(TAG, "Next image is " + (next + 1) + "/" + images.size());
            return (currentState.currentImageIndex + 1) % images.size();
    }

    @Override
    public void draw(Canvas canvas) {
        if (!initialized) {
            //Log.v(TAG, "not yet drawing piece " + pieceElementId);
            return;
        }
        Matrix matrix = new Matrix();

        matrix.reset();
        matrix.postTranslate(-images.get(currentState.getCurrentImageIndex()).getWidth() / 2, -images.get(currentState.getCurrentImageIndex()).getHeight() / 2); // Centers image
        matrix.postRotate(angle);
        matrix.postTranslate(currentState.getX(), currentState.getY());
        canvas.drawBitmap(images.get(currentState.getCurrentImageIndex()), matrix, null);

        //Log.v(TAG, "drawing piece " + pieceElementId + " at x:y " + currentState.getX() + ":" + currentState.getY());
        //canvas.drawBitmap(images.get(currentState.getCurrentImageIndex()),
        //      currentState.getX(), currentState.getY(), null);
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

    PieceState getPreviousState() {
        return previousState;
    }

    void updatePreviousState(){
        previousState.update(currentState.x, currentState.y, currentState.zDepth, currentState.currentImageIndex);
    }

    public int getHeight() { return mHeight; }

    public int getWidth() {return mWidth; }

    public String getPieceIndex() { return pieceIndex; }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public int getAngle() {
        return angle;
    }

    public String getType() {
        return type;
    }

    @Override
    public int compareTo(Object o) {
        return this.getCurrentState().getzDepth() - ((GamePiece)o).getCurrentState().getzDepth();
    }

    public void setCanSee(boolean canSee) {
        this.canSee = canSee;
    }

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
