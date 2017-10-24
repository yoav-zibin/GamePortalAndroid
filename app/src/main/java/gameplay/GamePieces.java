package gameplay;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

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

}
