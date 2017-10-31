package gameplay;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.acl.Group;
import java.util.ArrayList;

/**
 * Created by Jordan on 10/23/2017.
 */

public class GamePieces extends ArrayList<GamePiece> {

    private static final String TAG = "GamePieces";
    private final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();


    private String mMatchId;
    private String mGroupId;
    private String mGameId;

    private int sHeight;
    private int sWidth;

    GamePieces(String gameId, String MatchId, String GroupId){
        super();
        Log.d(TAG, "constructor");
        mMatchId = MatchId;
        mGroupId = GroupId;
        mGameId = gameId;
        getFirebaseData();
    }

    private void getFirebaseData(){
        Log.d(TAG, "getGamePieces: " + mMatchId);
        mDatabase.getReference("gameBuilder/gameSpecs").child(mGameId).child("pieces")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "Got data snapshot for game pieces");
                        Log.d(TAG, mGameId);
                        for (DataSnapshot piece: dataSnapshot.getChildren()) {
//                            add(getGamePiece(piece));
                            add(new GamePiece(piece, mGameId, mMatchId, mGroupId));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "Game pieces read failed: " + databaseError.getMessage());
                    }
                });
    }

}
