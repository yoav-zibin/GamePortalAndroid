package com.nyuchess.gameportal;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import java.util.List;

import gameplay.GameBoard;
import gameplay.GamePiece;

public class GameActivity extends AppCompatActivity {

    // The activity where an actual game is played.
    // The game should be drawn on one big SurfaceView, and allow dragging pieces around
    // and/or touching to select and then touching where to put them, whichever is easier.

    private static final String TAG = "GameActivity";

    private static final String KEY_ID = "GAME_ID";

    private List<User> mPlayers;
    private List<GamePiece> mGamePieces;
    private GameBoard mBoard;
    private Canvas mCanvas;
    private SurfaceView mSurfaceView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Get the images and other information from Firebase
        //String gameId = savedInstanceState.getString(KEY_ID);
        //mBoard = getBoard(gameId);
        //mGamePieces = getPieces(gameId);
    }



    private GameBoard getBoard(String gameId){
        return null;
    }

    private List<GamePiece> getPieces(String gameId){
        return null;
    }

    private void draw(){
        // Draw all the game elements, in order from lowest to highest on Z-axis
        Log.d(TAG, "Drawing game board");
        mBoard.draw(mCanvas);
        for (GamePiece piece: mGamePieces){
            Log.d(TAG, "Drawing piece " + piece);
            piece.draw(mCanvas);
        }
    }
}
