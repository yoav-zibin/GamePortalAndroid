package gameplay;

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

/**
 * Created by Jordan on 10/14/2017.
 */

public class GameBoard implements IGameElement {

    private static final String TAG = "GameBoard";

    private final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private final FirebaseStorage mStorage = FirebaseStorage.getInstance();

    private String mGameId;

    private String imageId;
    private String backgroundColor;
    private long maxScale;

    private Bitmap image;

    private boolean initialized;

    GameBoard(String gameId) {
        mGameId = gameId;
        initialized = false;
        getFirebaseData();
    }

    //Get data from Firebase and call init() once we have it
    private void getFirebaseData(){
        Log.d(TAG, "getGameBoard: " + mGameId);
        mDatabase.getReference("gameBuilder/gameSpecs").child(mGameId).child("board")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "Got data snapshot for game board");
                    final String imageId = dataSnapshot.child("imageId").getValue().toString();
                    Log.d(TAG, "imageId: " + imageId);
                    final String backgroundColor = dataSnapshot.child("backgroundColor").getValue().toString();
                    final long maxScale = (Long) dataSnapshot.child("maxScale").getValue();
                    //now get the image
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
                                    init(image, backgroundColor, maxScale);
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

    //Set the data and set the initialized flag to show we can start drawing
    private void init(Bitmap image, String backgroundColor, long maxScale){
        //get actual image using id
        Log.d(TAG, "init");
        this.image = image;
        this.backgroundColor = backgroundColor;
        this.maxScale = maxScale;
//        Log.d(TAG, "imageId: " + imageId);
        Log.d(TAG, "backgroundColor " + backgroundColor);
        Log.d(TAG, "maxScale: " + maxScale);
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

}
