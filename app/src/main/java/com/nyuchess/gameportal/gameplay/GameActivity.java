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

public class GameActivity extends AppCompatActivity {

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
    private Game game;

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
        game = new Game(gameId, MATCH_ID, GROUP_ID);
        game.init();
        mGameView = findViewById(R.id.game_view);
        mGameView.setGame(game);

        mDatabase.getReference("gameBuilder/gameSpecs").child(gameId).child("board")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String imageId = dataSnapshot.child("imageId").getValue().toString();
                        //now get the image
                        mDatabase.getReference("gameBuilder/images/" + imageId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot image) {
                                final int height = Integer.parseInt(image.child("height").getValue().toString());
                                final int width = Integer.parseInt(image.child("width").getValue().toString());
                                final Game ongoing = game;

                                mGameView = findViewById(R.id.game_view);
                                mGameView.setOnTouchListener(new View.OnTouchListener() {

                                    private int mPreviousX;
                                    private int mPreviousY;
                                    GamePiece target = null;

                                    @Override
                                    public boolean onTouch(View view, MotionEvent event) {
                                        int x = (int) event.getX();
                                        int y = (int) event.getY();

                                        switch (event.getAction()) {
                                            case MotionEvent.ACTION_DOWN:
                                                for (GamePiece piece : ongoing.getPieces()) {
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
                                                if (target != null) {
                                                    int dx = x - mPreviousX;
                                                    int dy = y - mPreviousY;
                                                    target.getCurrentState().setX(target.getCurrentState().getX() + dx);
                                                    target.getCurrentState().setY(target.getCurrentState().getY() + dy);
                                                    Log.d(TAG, "MOVING");
                                                    Log.d(TAG, x + "");
                                                    Log.d(TAG, y + "");
                                                }
                                                break;

                                            case MotionEvent.ACTION_UP:
                                                if(target != null) {
                                                    Map<String, Object> loc = new HashMap<>();
                                                    int dx = x - mPreviousX;
                                                    int dy = y - mPreviousY;
                                                    loc.put("x", (int)(((((double) target.getCurrentState().getX() + dx) / (double) width)) * 100));
                                                    loc.put("y", (int) ((((double) target.getCurrentState().getY() + dy) / (double) height) * 100));
                                                    Log.d(TAG, ((target.getCurrentState().getX() + dx) / width) + "");
                                                    Log.d(TAG, ((target.getCurrentState().getX() + dx) / height) + "");
                                                    FirebaseDatabase.getInstance().getReference("gamePortal/groups/" + GROUP_ID + "/matches/" + MATCH_ID + "/pieces/" + target.getPieceIndex() + "/currentState").updateChildren(loc);
                                                    target = null;
                                                }
                                                break;
                                        }

                                        mPreviousX = x;
                                        mPreviousY = y;
                                        return true;
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d(TAG, "GameArrayItem board image read failed: " + databaseError.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "GameArrayItem board read failed: " + databaseError.getMessage());
                    }
                });


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
