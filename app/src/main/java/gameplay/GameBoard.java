package gameplay;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Jordan on 10/14/2017.
 */

public class GameBoard implements IGameElement {

    private static final String TAG = "GameBoard";

    private final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    private String mGameId;

    private String imageId;
    private String backgroundColor;
    private long maxScale;

    private Bitmap mImage;

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
                        String imageId = dataSnapshot.child("imageId").getValue().toString();
                        String backgroundColor = dataSnapshot.child("backgroundColor").getValue().toString();
                        long maxScale = (Long) dataSnapshot.child("maxScale").getValue();
                        init(imageId, backgroundColor, maxScale);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "Game board read failed: " + databaseError.getMessage());
                    }
        });
    }

    //Set the data and set the initialized flag to show we can start drawing
    private void init(String imageId, String backgroundColor, long maxScale){
        //get actual image using id
        Log.d(TAG, "init");
        this.imageId = imageId;
        this.backgroundColor = backgroundColor;
        this.maxScale = maxScale;
        Log.d(TAG, "imageId: " + imageId);
        Log.d(TAG, "backgroundColor " + backgroundColor);
        Log.d(TAG, "maxScale: " + maxScale);
        initialized = true;
        Log.d(TAG, "Initialization done");
    }

    @Override
    public void draw(Canvas canvas) {
        //wait until initialization is done to avoid NullPointerExceptions
        if (!initialized){
            return;
        }
        // draw shit
    }


//    GameBoard(String imageId, String backgroundColor, int maxScale){
//        this.imageId = imageId;
//        this.backgroundColor = backgroundColor;
//        this.maxScale = maxScale;
//    }
//
//    public String getImageId(){
//        return imageId;
//    }
//
//    public String getBackgroundColor() {
//        return backgroundColor;
//    }
//
//    public long getMaxScale() {
//        return maxScale;
//    }
//
//    public void setImageId(String imageId) {
//        this.imageId = imageId;
//    }
//
//    public void setBackgroundColor(String backgroundColor) {
//        this.backgroundColor = backgroundColor;
//    }
//
//    public void setMaxScale(long maxScale) {
//        this.maxScale = maxScale;
//    }
}
