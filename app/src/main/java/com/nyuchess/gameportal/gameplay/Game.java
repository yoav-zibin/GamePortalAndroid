package com.nyuchess.gameportal.gameplay;

import android.app.Activity;
import android.graphics.Canvas;
import android.util.Log;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Jordan on 10/22/2017.
 */

public class Game {

    private String mGameId;

    private GamePieces mGamePieces;
    private GameBoard mGameBoard;

    private Activity activity;

    Game(String gameId, String matchId, String groupId, Activity activity){
        mGameId = gameId;
        mGameBoard = new GameBoard(gameId);
        mGamePieces = new GamePieces(gameId, matchId, groupId, activity);
    }

    void init(){
        mGameBoard.init();
        mGamePieces.init();
    }

    void draw(Canvas canvas){
        Collections.sort(mGamePieces);
        mGameBoard.draw(canvas);
        for (GamePiece piece: mGamePieces){
            piece.draw(canvas);
        }
    }

    String getGameId() {
        return mGameId;
    }

    GameBoard getBoard() {
        return mGameBoard;
    }

    GamePieces getPieces() {
        return mGamePieces;
    }
}
