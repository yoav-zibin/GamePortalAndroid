package com.nyuchess.gameportal.gameplay;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Jordan on 10/23/2017.
 */

class GamePieces extends ArrayList<GamePiece> {

    private static final String TAG = "GamePieces";
    private FirebaseDatabase mDatabase;

    private String mMatchId;
    private String mGroupId;
    private String mGameId;

    GamePieces(String gameId, String MatchId, String GroupId){
        super();
        Log.d(TAG, "constructor");
        mMatchId = MatchId;
        mGroupId = GroupId;
        mGameId = gameId;
    }

    void init(){
        mDatabase = FirebaseDatabase.getInstance();
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
                            GamePiece gamePiece = new GamePiece(mGameId, mMatchId, mGroupId);
                            gamePiece.startInit(piece);
                            add(gamePiece);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "Game pieces read failed: " + databaseError.getMessage());
                    }
                });
    }

    String getGameId() {
        return mGameId;
    }
}
