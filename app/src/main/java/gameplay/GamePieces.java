package gameplay;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jordan on 10/23/2017.
 */

public class GamePieces extends ArrayList<GamePiece> {

    private static final String TAG = "GamePieces";
    private final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();


    private String mGameId;

    GamePieces(String gameId){
        super();
        Log.d(TAG, "constructor");
        mGameId = gameId;
        getFirebaseData();
    }

    private void getFirebaseData(){
        Log.d(TAG, "getGamePieces: " + mGameId);
        mDatabase.getReference("gameBuilder/gameSpecs").child(mGameId).child("pieces")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "Got data snapshot for game pieces");
                        for (DataSnapshot piece: dataSnapshot.getChildren()) {
//                            add(getGamePiece(piece));
                            add(new GamePiece(piece));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "Game pieces read failed: " + databaseError.getMessage());
                    }
                });
    }

//    private GamePiece getGamePiece(DataSnapshot dataSnapshot){
//
//        final GamePiece piece = dataSnapshot.getValue(GamePiece.class);
//
//        final String id = dataSnapshot.getKey();
//
//        final String pieceElementId = dataSnapshot.child("pieceElementId").getValue().toString();
//        mDatabase.getReference("gameBuilder/elements").child(pieceElementId)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        Log.d(TAG, "Got data snapshot for piece element " + pieceElementId);
//                        int height = dataSnapshot.child("height").getValue(Integer.class);
//                        int width = dataSnapshot.child("width").getValue(Integer.class);
//                        Log.d(TAG, "Height x Width: " + height + " x " + width);
//                        piece.setHeight(height);
//                        piece.setWidth(width);
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//                        Log.d(TAG, "Game piece + " + id + " read failed: " + databaseError.getMessage());
//                    }
//                });
//
////        GamePiece.PieceState initialState = dataSnapshot.child("initialState")
////                .getValue(GamePiece.PieceState.class);
////        piece.setInitialState(initialState);
//
//        return piece;
//    }

}
