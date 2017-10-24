package gameplay;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/**
 * Created by Jordan on 10/22/2017.
 */

public class GameFirebaseAdapter {

    //Adapter to get data from Firebase and convert it into objects of the required classes

    private static final String TAG = "GameFirebaseAdapter";

    private final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    //Have to pass in a new GameBoard instead of returning a new one
    //because Java won't let you return from inside a void listener
    //and to access one that's outside the listener it has to be final
    //so you can't just use getValue(GameBoard.class) to make a new one
//    void getGameBoard(String gameId, final GameBoard board){
//
////        final GameBoard[] boardArray = new GameBoard[1];
//
//        Log.d(TAG, "getGameBoard: " + gameId);
//        mDatabase.getReference("gameBuilder/gameSpecs").child(gameId).child("board")
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Log.d(TAG, "Got data snapshot for game board");
//                String imageId = dataSnapshot.child("imageId").getValue().toString();
//                String backgroundColor = dataSnapshot.child("backgroundColor").getValue().toString();
//                long maxScale = (Long) dataSnapshot.child("maxScale").getValue();
//                board.setImageId(imageId);
//                board.setBackgroundColor(backgroundColor);
//                board.setMaxScale(maxScale);
//                board.init();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.d(TAG, "Game board read failed: " + databaseError.getMessage());
//            }
//        });
//    }


    void getGamePieces(String gameId, final List<GamePiece> pieces){
        Log.d(TAG, "getGamePieces: " + gameId);
        mDatabase.getReference("gameBuilder/gameSpecs").child(gameId).child("pieces")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "Got data snapshot for game pieces");
                        for (DataSnapshot piece: dataSnapshot.getChildren()) {
                            pieces.add(getGamePiece(piece));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "Game pieces read failed: " + databaseError.getMessage());
                    }
                });
    }

    private GamePiece getGamePiece(DataSnapshot dataSnapshot){

        final GamePiece piece = dataSnapshot.getValue(GamePiece.class);

        final String id = dataSnapshot.getKey();

        final String pieceElementId = dataSnapshot.child("pieceElementId").getValue().toString();
        mDatabase.getReference("gameBuilder/elements").child(pieceElementId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "Got data snapshot for piece element " + pieceElementId);
                        int height = dataSnapshot.child("height").getValue(Integer.class);
                        int width = dataSnapshot.child("width").getValue(Integer.class);
                        Log.d(TAG, "Height x Width: " + height + " x " + width);
                        piece.setHeight(height);
                        piece.setWidth(width);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "Game piece + " + id + " read failed: " + databaseError.getMessage());
                    }
                });

//        GamePiece.PieceState initialState = dataSnapshot.child("initialState")
//                .getValue(GamePiece.PieceState.class);
//        piece.setInitialState(initialState);

        return piece;
    }

    void saveGame(Game game){

    }
}
