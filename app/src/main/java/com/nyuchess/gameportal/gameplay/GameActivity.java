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

    //For differentiating between a click and a drag.
    //Max allowed duration for a "click", in milliseconds.
    private static final int MAX_CLICK_DURATION = 1000;
    // Max allowed distance to move during a "click", in DP.
    private static final int MAX_CLICK_DISTANCE = 15;
    private long pressStartTime;
    private float pressedX;
    private float pressedY;

    private float prevX = Float.MIN_VALUE;
    private float prevY = Float.MIN_VALUE;

    private int onScreen = 0;

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
        final int x = (int) event.getX();
        final int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pressStartTime = System.currentTimeMillis();
                pressedX = event.getX();
                pressedY = event.getY();
                //screen touched, check which piece is being touched
                for (GamePiece piece : mGame.getPieces()) {
                    if (x <= piece.getCurrentState().getX() + (piece.getWidth()/2)
                            && x >= piece.getCurrentState().getX() - (piece.getWidth()/2)
                            && y >= piece.getCurrentState().getY() - (piece.getHeight()/2)
                            && y <= piece.getCurrentState().getY() + (piece.getHeight()/2)) {
                        if(piece.getDeckPieceIndex() != -1) {
                            Log.d(TAG,"TRYING TO DRAG A CARD OUT");
                            FirebaseDatabase.getInstance().getReference("/gameBuilder/elements").child(piece.getPieceElementId()).child("deckElements").child(piece.getDeckPieceIndex() + "").child("deckMemberElementId").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    GamePiece card = new GamePiece(gameId, MATCH_ID, GROUP_ID, dataSnapshot.getValue().toString(), x, y, mGame.getPieces().size(), mGame.getBoard().getHeight(), mGame.getBoard().getWidth());
                                    mGame.getPieces().add(card);
                                    target = card;
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            piece.nextCard();
                        } else {
                            Log.d("PICKED", piece.getPieceElementId());
                            target = piece;
                        }
                        Log.w(TAG, "Picked at " + x + "," + y + " and real bounds are " + piece.getCurrentState().getX() + " " + piece.getCurrentState().getY() + " " + piece.getWidth() + " " + piece.getHeight());
                        Log.w(TAG, "" + piece.getCurrentState().getX() + " " + (piece.getCurrentState().getX() + piece.getWidth()) + " " + piece.getCurrentState().getY() + " " + (piece.getCurrentState().getY() + piece.getHeight()));
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //if a piece is being dragged, move it
                if (target != null) {
                    //add half the piece's dimensions so it looks like you're moving them by
                    //the center instead of the corner
                    if(event.getPointerCount() == 2) {

                        if(prevY == Float.MIN_VALUE) {
                            prevY = event.getY(1);
                            Log.d(TAG, "SETTING VAL1");
                        }

                        if(prevX == Float.MIN_VALUE) {
                            prevX = event.getX(1);
                            Log.d(TAG, "SETTING VAL2");
                        }

                        float x1 = event.getX(0);
                        float y1 = event.getY(0);

                        float x2 = event.getX(1);
                        float y2 = event.getY(1);

                        float x3 = prevX;
                        float y3 = prevY;

                        double angle = Math.toDegrees(Math.atan2(x2 - x1, y2 - y1)-
                                Math.atan2(x3 - x1, y3 - y1));

                        target.setAngle(target.getAngle() + (int) -angle);


                    } else {
                        int dx = target.getCurrentState().getX() + (target.getCurrentState().getX() - x);
                        int dy = target.getCurrentState().getX() + (y - target.getCurrentState().getY());
                        target.getCurrentState().setX(x);
                        target.getCurrentState().setY(y);
                        Log.d(TAG, "ACTION_MOVE");
                        Log.d(TAG, dx + ":" + dy);
                    }
                } else {

                }
                break;

            case MotionEvent.ACTION_UP:
                // Vic test = -KyrfBOT-F2mPKdMEvzX
                //let go, update piece's location if it was moved
                //if it was not moved, toggle the piece's image
                Log.d(TAG, "ACTION_UP");
                view.performClick();
                long pressDuration = System.currentTimeMillis() - pressStartTime;
                // Click event
                if (pressDuration < MAX_CLICK_DURATION &&
                        distance(pressedX, pressedY, event.getX(), event.getY()) < MAX_CLICK_DISTANCE) {
                    if (target != null){
                        Log.d(TAG, "ACTION_UP_CLICK");
                        int newImageIndex = target.getNextImageIndex();
                        Map<String, Object> newState = new HashMap<>();
                        newState.put("currentImageIndex", newImageIndex);
                        Log.d(TAG, "Updating image index to "  + newImageIndex);
                        FirebaseDatabase.getInstance().getReference(
                                "gamePortal/groups/" + GROUP_ID + "/matches/" + MATCH_ID +
                                        "/pieces/" + target.getPieceIndex() + "/currentState")
                                .updateChildren(newState);
                        target = null;
                    }
                }
                // Drag event
                else {
                    if(target != null && event.getPointerCount() == 1) {
                        Log.d(TAG, "ACTION_UP_DRAG");
                        Map<String, Object> loc = new HashMap<>();
                        double nX = x;
                        double nY = y;
                        int newX = (int) (nX / (double) mGame.getBoard().getWidth() * 100);
                        int newY = (int) (nY / (double) mGame.getBoard().getHeight() * 100);
                        loc.put("x", newX);
                        loc.put("y", newY);
                        Log.d(TAG, "moving piece to " + newX + ":" + newY);
                        FirebaseDatabase.getInstance().getReference(
                                "gamePortal/groups/" + GROUP_ID + "/matches/" + MATCH_ID +
                                        "/pieces/" + target.getPieceIndex() + "/currentState")
                                .updateChildren(loc);
                        Log.d("YO1", target.getPieceElementId() + " x " + target.getCurrentState().getX());
                        target = null;
                    } else {
                        Log.d(TAG, "LIFTED UP A POINTER");
                    }
                }

                break;
        }
        if(event.getAction() == 262) {
            Log.d(TAG, "LIFTED UP POINTER");
            prevX = Float.MIN_VALUE;
            prevY = Float.MIN_VALUE;
        }
        return true;
    }

    private float distance(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        float distanceInPx = (float) Math.sqrt(dx * dx + dy * dy);
        return pxToDp(distanceInPx);
    }

    private  float pxToDp(float px) {
        return px / getResources().getDisplayMetrics().density;
    }

}
