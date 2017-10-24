package gameplay;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

/**
 * Created by Jordan on 10/14/2017.
 */

public class GamePiece implements IGameElement {

    private static final String TAG = "GamePiece";

    private final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    private boolean initialized;

    private PieceState initialState;
    private final String pieceElementId;
    private long deckPieceIndex;

    private PieceState currentState;
    // private int currentImageIndex;
//    private Bitmap mImage;
    private int height;
    private int width;

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
//    private int[] cardVisibility;


//    GamePiece(){    }

//    void setInitialState(PieceState state){
//        initialState = state;
////        currentState = state;
//    }
//
//    public void setHeight(int height) {
//        this.height = height;
//    }
//
//    public void setWidth(int width) {
//        this.width = width;
//    }

    GamePiece(DataSnapshot dataSnapshot){
        initialized = false;
        pieceElementId = dataSnapshot.child("pieceElementId").getValue().toString();
        deckPieceIndex = (Long) dataSnapshot.child("deckPieceIndex").getValue();
        initialState = dataSnapshot.child("initialState").getValue(PieceState.class);
        Log.d(TAG, "initial state: " + initialState);
        currentState = initialState;
        getFirebaseData(dataSnapshot);
    }

    private void getFirebaseData(DataSnapshot dataSnapshot){
        final String id = dataSnapshot.getKey();
        mDatabase.getReference("gameBuilder/elements").child(pieceElementId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int height = dataSnapshot.child("height").getValue(Integer.class);
                int width = dataSnapshot.child("width").getValue(Integer.class);
                Log.d(TAG, "Height x Width: " + height + " x " + width);
                Log.d(TAG, "Got data snapshot for piece element " + pieceElementId);
                //now get the actual image

                init(height, width);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Game piece + " + id + " read failed: " + databaseError.getMessage());
            }
        });
    }

    private void init(int height, int width){
        this.height = height;
        this.width = width;
        initialized = true;
    }

    @Override
    public void draw(Canvas canvas) {
        if (!initialized){
            return;
        }
    }

    static class PieceState {
        private int x;
        private int y;
        private int zDepth;
        private int currentImageIndex;

//        public PieceState(){}
//
//        public PieceState(int x, int y, int zDepth, int currentImageIndex){
//            this.x = x;
//            this.y = y;
//            this.zDepth = zDepth;
//            this.currentImageIndex = currentImageIndex;
//        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getzDepth() {
            return zDepth;
        }

        public int getCurrentImageIndex() {
            return currentImageIndex;
        }

        public String toString(){
            return String.format(Locale.US, "%d %d %d %s", x, y, zDepth, currentImageIndex);
        }
    }

}
