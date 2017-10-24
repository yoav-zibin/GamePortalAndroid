package com.nyuchess.gameportal;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import java.util.List;

import gameplay.Game;
import gameplay.GameBoard;
import gameplay.GamePiece;

public class GameActivity extends AppCompatActivity {

    // The activity where an actual game is played.
    // The game should be drawn on one big SurfaceView, and allow dragging pieces around
    // and/or touching to select and then touching where to put them, whichever is easier.

    private static final String TAG = "GameActivity";

    private static final String KEY_ID = "GAME_ID";

    private List<User> mPlayers;
    private Canvas mCanvas;
    private GameView mGameView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Log.d(TAG, "onCreate");
        // Get the images and other information from Firebase
        String gameId = getIntent().getStringExtra(KEY_ID);
        Log.d(TAG, "Starting game id: " + gameId);
        Game game = new Game(gameId);

        mGameView = findViewById(R.id.game_view);
        mGameView.setGame(game);

    }


    private GameBoard getBoard(String gameId){
        return null;
    }

    private List<GamePiece> getPieces(String gameId){
        return null;
    }

//    private void draw(){
        // Draw all the game elements, in order from lowest to highest on Z-axis
//        Log.d(TAG, "Drawing game board");
//        mBoard.draw(mCanvas);
//        for (GamePiece piece: mGamePieces){
//            Log.d(TAG, "Drawing piece " + piece);
//            piece.draw(mCanvas);
//        }
//    }
}
