package com.nyuchess.gameportal.gameplay;

import android.graphics.Canvas;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nyuchess.gameportal.R;
import com.nyuchess.gameportal.groups.User;

public class GameActivity extends AppCompatActivity implements View.OnTouchListener {

    // The activity where an actual game is played.
    // The game should be drawn on one big SurfaceView, and allow dragging pieces around
    // and/or touching to select and then touching where to put them, whichever is easier.

    private static final String TAG = "GameActivity";

    private static final String KEY_ID = "GAME_ID";

    private List<User> mPlayers;
    private Canvas mCanvas;
    private GameView mGameView;

    private String gameId;
    private String GROUP_ID;
    private String MATCH_ID;
    private Game mGame;

    //the piece currently being acted on with a touch event, if any
    private GamePiece target;

    private final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Log.d(TAG, "onCreate");

        // Get the images and other information from Firebase
        gameId = getIntent().getStringExtra(KEY_ID);
        MATCH_ID = getIntent().getStringExtra("MATCH_ID");
        GROUP_ID = getIntent().getStringExtra("GROUP_ID");
        mGame = new Game(gameId, MATCH_ID, GROUP_ID);
        mGame.init();
        mGameView = findViewById(R.id.game_view);
        mGameView.setGame(mGame);
        mGameView.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //screen touched, check which piece is being touched
                for (GamePiece piece : mGame.getPieces()) {
                    if (x <= piece.getCurrentState().getX() + piece.getWidth()
                            && x >= piece.getCurrentState().getX()
                            && y >= piece.getCurrentState().getY()
                            && y <= piece.getCurrentState().getY() + piece.getHeight()) {
                        target = piece;
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //if a piece is being dragged, move it
                if (target != null) {
                    int dx = x - target.getCurrentState().getX();
                    int dy = y - target.getCurrentState().getY();
                    target.getCurrentState().setX(target.getCurrentState().getX() + dx);
                    target.getCurrentState().setY(target.getCurrentState().getY() + dy);
                    Log.d(TAG, "ACTION_MOVE");
                    Log.d(TAG, x + ":" + y);
                }
                break;

            case MotionEvent.ACTION_UP:
                //let go, update piece's location
                Log.d(TAG, "ACTION_UP");
                view.performClick();
                if(target != null) {
                    Map<String, Object> loc = new HashMap<>();
                    int dx = x - target.getPreviousState().getX();
                    int dy = y - target.getPreviousState().getY();
                    int newX = (int) ((((double) target.getPreviousState().getX() + dx) /
                            (double) mGame.getBoard().getWidth()) * 100);
                    int newY = (int) ((((double) target.getPreviousState().getY() + dy) /
                            (double) mGame.getBoard().getHeight()) * 100);
                    loc.put("x", newX);
                    loc.put("y", newY);
                    Log.d(TAG, "moving piece to " + newX + ":" + newY);
//                    Log.d(TAG, ((target.getCurrentState().getX() + dx) / target.getWidth()) + "");
//                    Log.d(TAG, ((target.getCurrentState().getX() + dx) / target.getHeight()) + "");
                    FirebaseDatabase.getInstance().getReference(
                            "gamePortal/groups/" + GROUP_ID + "/matches/" + MATCH_ID +
                                    "/pieces/" + target.getPieceIndex() + "/currentState")
                            .updateChildren(loc);
                    target = null;
                }
                break;
        }
        if (target != null){
            target.getCurrentState().setX(x);
            target.getCurrentState().setY(y);
        }
        return true;
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
