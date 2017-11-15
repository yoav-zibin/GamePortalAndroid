package com.nyuchess.gameportal.gameplay;

import android.graphics.Canvas;
import android.util.Log;

import java.util.List;

/**
 * Created by Jordan on 10/22/2017.
 */

public class Game {

    private String mGameId;

    private GamePieces mGamePieces;
    private GameBoard mGameBoard;

    Game(String gameId, String matchId, String groupId){
        mGameId = gameId;
        mGameBoard = new GameBoard(gameId);
        mGamePieces = new GamePieces(gameId, matchId, groupId);
    }

    void init(){
        mGameBoard.init();
        mGamePieces.init();
    }

    void draw(Canvas canvas){
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
