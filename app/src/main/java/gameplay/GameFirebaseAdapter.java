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

    List<GamePiece> getGamePieces(String gameId){
        return null;
    }


    //Have to pass in a new GameBoard instead of returning a new one
    //because Java won't let you return from inside a void listener
    //and to access one that's outside the listener it has to be final
    //so you can't just use getValue(GameBoard.class) to make a new one
    void getGameBoard(String gameId, final GameBoard board){

        Log.d(TAG, "getGameBoard: " + gameId);
        mDatabase.getReference("gameSpecs").child(gameId).child("board")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Got data snapshot for game board");
                String imageId = dataSnapshot.child("imageId").getValue().toString();
                String backgroundColor = dataSnapshot.child("backgroundColor").getValue().toString();
                int maxScale = (Integer) dataSnapshot.child("maxScale").getValue();
                board.setImageId(imageId);
                board.setBackgroundColor(backgroundColor);
                board.setMaxScale(maxScale);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void saveGame(Game game){

    }
}
