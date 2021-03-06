package com.nyuchess.gameportal.gameplay;

import android.content.res.AssetFileDescriptor;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.nyuchess.gameportal.R;
import com.nyuchess.gameportal.groups.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameActivity extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener {

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

    private ArrayList<Integer> history;
    private ArrayList<GamePiece> historyRef;
    private GamePiece lastLinesTarget;
    private int lastLines;
    private float linePX;
    private float linePY;
    private int fontSize = 1;

    private int onScreen = 0;
    private int rotate;
    private int cardVis = 1;

    private boolean draw;
    private boolean clear;
    private boolean setCard = false;

    //the piece currently being acted on with a touch event, if any
    private GamePiece target;

    private final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    private MediaPlayer diceRoll = null;
    private MediaPlayer cardDraw = null;
    private MediaPlayer cardPlace = null;
    private MediaPlayer pickPiece = null;
    private MediaPlayer setPiece = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        diceRoll = MediaPlayer.create(this, R.raw.diceroll);
        cardDraw = MediaPlayer.create(this, R.raw.cardslide);
        cardPlace = MediaPlayer.create(this, R.raw.cardplace);
        pickPiece = MediaPlayer.create(this, R.raw.pickpiece);
        setPiece = MediaPlayer.create(this, R.raw.setpiece);

        Log.d(TAG, "onCreate");
        final SlidingMenu menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.RIGHT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        //menu.setBehindOffsetRes(R.dimen.);
        menu.setFadeDegree(0.5f);
        menu.attachToActivity(GameActivity.this, SlidingMenu.SLIDING_CONTENT);
        menu.setMenu(R.layout.activity_menu);

        Button mButton = (Button) findViewById(R.id.pullOutVisiblity);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //menu.showMenu();
                //or
                menu.toggle();
            }
        });

        Button mButton2 = (Button) findViewById(R.id.backButton);
        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //menu.showMenu();
                //or
                menu.toggle();
            }
        });

        findViewById(R.id.visNone).setOnClickListener(this);
        findViewById(R.id.visAllBut).setOnClickListener(this);
        findViewById(R.id.visEvery).setOnClickListener(this);
        findViewById(R.id.visMe).setOnClickListener(this);
        findViewById(R.id.setAll).setOnClickListener(this);
        findViewById(R.id.setCard).setOnClickListener(this);

        // Get the images and other information from Firebase
        gameId = getIntent().getStringExtra(KEY_ID);
        MATCH_ID = getIntent().getStringExtra("MATCH_ID");
        GROUP_ID = getIntent().getStringExtra("GROUP_ID");
        mGame = new Game(gameId, MATCH_ID, GROUP_ID, this);
        mGame.init();
        mGameView = findViewById(R.id.game_view);
        mGameView.setGame(mGame);
        mGameView.setOnTouchListener(this);
        draw = false;
        clear = false;
        history = new ArrayList<>();
        historyRef = new ArrayList<>();
        findViewById(R.id.drawButton).setOnClickListener(this);
        findViewById(R.id.undoButton).setOnClickListener(this);
        findViewById(R.id.clearButton).setOnClickListener(this);
        SeekBar fontSize = (SeekBar) findViewById(R.id.fontSize);
        fontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "" + seekBar.getProgress());
                setFontSize(seekBar.getProgress());
            }
        });
    }

    public void setFontSize(int x) {
        fontSize = (int)(1 + ((x / 100.0) * 9));
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        //Log.w(TAG, "GROUP " + GROUP_ID + " " + gameId);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pressStartTime = System.currentTimeMillis();
                pressedX = event.getX();
                pressedY = event.getY();
                //screen touched, check which piece is being touched
                for (int p = mGame.getPieces().size()-1; p >= 0; p --) {
                    GamePiece piece = mGame.getPieces().get(p);
                    if (x <= piece.getCurrentState().getX() + (piece.getWidth() / 2)
                            && x >= piece.getCurrentState().getX() - (piece.getWidth() / 2)
                            && y >= piece.getCurrentState().getY() - (piece.getHeight() / 2)
                            && y <= piece.getCurrentState().getY() + (piece.getHeight() / 2)) {

                        if(piece.getType().equals("cardsDeck")) {
                            for(int i = 0 ; i < mGame.getPieces().size(); i ++) {
                                GamePiece log = mGame.getPieces().get(i);
                                Log.w(TAG, log.getOverrideInitX() + " " + log.getOverrideInitY());
                                Log.w(TAG, log.getOverrideCurX() + " " + log.getOverrideCurY());
                                if((int)mGame.getPieces().get(i).getDeckPieceIndex() == Integer.parseInt(piece.getPieceIndex())
                                        && ((mGame.getPieces().get(i).getOverrideInitX().equals(mGame.getPieces().get(i).getOverrideCurX())
                                        && mGame.getPieces().get(i).getOverrideInitY().equals(mGame.getPieces().get(i).getOverrideCurY())) || (mGame.getPieces().get(i).getCurrentState().getX() == mGame.getPieces().get(i).getInitialState().getX()
                                        && mGame.getPieces().get(i).getCurrentState().getY() == mGame.getPieces().get(i).getInitialState().getY()))) {
                                    target = mGame.getPieces().get(i);
                                    rotate = target.getAngle();
                                    if(target != null && draw) {
                                        historyRef.add(target);
                                        lastLines = 0;
                                        linePX = event.getX();
                                        linePY = event.getY();
                                    }
                                    Log.d(TAG, "PICKED UP A CARD");
                                    break;
                                }
                            }

                            if(target == null) {
                                Toast.makeText(this, "No more Cards!", Toast.LENGTH_SHORT).show();
                            } else {
                                cardDraw.start();
                                target.getCurrentState().setzDepth(999999);
                                DatabaseReference pIndex = mDatabase.getReference("gamePortal/groups/" + GROUP_ID + "/participants/");
                                Log.w(TAG, gameId + " " + GROUP_ID);
                                pIndex.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        //target.setCanSee(true);
                                        Map<String, Object> see = new HashMap<>();
                                        mDatabase.getReference("gamePortal/groups/" + GROUP_ID + "/matches/" + MATCH_ID +
                                                "/pieces/" + target.getPieceIndex() + "/currentState/cardVisibility").setValue(see);
                                        if(cardVis == 0) {
                                            Log.w(TAG, "Card vis is 0");
                                        } else if(cardVis == 1) {
                                            Log.w(TAG, "Card vis is 1");
                                            see.put(dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("participantIndex").getValue().toString(), true);
                                        } else if(cardVis == 2) {
                                            Log.w(TAG, "Card vis is 2");
                                            Log.w(TAG, dataSnapshot.toString());
                                            for(DataSnapshot child : dataSnapshot.getChildren()) {
                                                if(!child.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                                    see.put(child.child("participantIndex").getValue().toString(), true);
                                                }
                                            }
                                        } else if(cardVis == 3) {
                                            Log.w(TAG, "Card vis is 3");
                                            for(DataSnapshot child : dataSnapshot.getChildren()) {
                                                see.put(child.child("participantIndex").getValue().toString(), true);
                                            }
                                        }
                                        mDatabase.getReference("gamePortal/groups/" + GROUP_ID + "/matches/" + MATCH_ID +
                                                "/pieces/" + target.getPieceIndex() + "/currentState/cardVisibility").updateChildren(see);
                                        Log.w(TAG, "lol what");
                                        Log.w(TAG, target.getPieceIndex() + " " + see.toString());
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        } else {
                            Log.d("PICKED", piece.getPieceElementId());
                            target = piece;
                            rotate = target.getAngle();
                            target.getCurrentState().setzDepth(999999);
                            if(target.getType().equals("card")) {
                                cardDraw.start();
                            } else {
                                pickPiece.start();
                            }

                            if(target != null && draw) {
                                historyRef.add(target);
                                lastLines = 0;
                                linePX = event.getX();
                                linePY = event.getY();
                            }

                            Collections.sort(mGame.getPieces());

                            break;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(clear) {
                    break;
                } else
                //if a piece is being dragged, move it
                if (target != null) {
                    if(setCard) {

                    } else
                    if(draw) {
                        if(x <= target.getCurrentState().getX() + (target.getWidth() / 2)
                                && x >= target.getCurrentState().getX() - (target.getWidth() / 2)
                                && y >= target.getCurrentState().getY() - (target.getHeight() / 2)
                                && y <= target.getCurrentState().getY() + (target.getHeight() / 2)) {
                            Log.d(TAG, "Trying to draw");
                            Log.d(TAG, event.getX() + " " + event.getY());
                            EditText getColor = (EditText)findViewById(R.id.hexColor);
                            String color = getColor.getText().toString();
                            if(color.length() != 6) {
                                color = "000000";
                            }
                            DatabaseReference ref = mDatabase.getReference("gamePortal/groups/" + GROUP_ID + "/matches/" + MATCH_ID +
                                    "/pieces/" + target.getPieceIndex() + "/currentState/drawing/");
                            String push = ref.push().toString().replace("https://universalgamemaker.firebaseio.com/gamePortal/groups/" +
                                    GROUP_ID + "/matches/" + MATCH_ID + "/pieces/" + target.getPieceIndex() + "/currentState/drawing/", "");
                            int hex = (int) Long.parseLong("FF" + color, 16);
                            float fromX = (linePX - target.getCurrentState().getX() + (target.getWidth()/2)) * 100 / target.getWidth();
                            float toX = (x - target.getCurrentState().getX() + (target.getWidth()/2)) * 100 / target.getWidth();
                            float fromY = (linePY - target.getCurrentState().getY() + (target.getHeight()/2)) * 100 / target.getHeight();
                            float toY = (y - target.getCurrentState().getY() + (target.getHeight()/2)) * 100 / target.getHeight();
                            FingerLine line = new FingerLine(fromX, toX, fromY, toY, hex, fontSize, push);

                            Map<String, Object> draw = new HashMap<>();
                            draw.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                            draw.put("timestamp", ServerValue.TIMESTAMP);
                            draw.put("color", color);
                            draw.put("lineThickness", fontSize);
                            draw.put("fromX", (int) fromX);
                            draw.put("fromY", (int) fromY);
                            draw.put("toX", (int) toX);
                            draw.put("toY", (int) toY);

                            ref.push().setValue(draw);

                            target.getDrawings().add(line);
                            Log.d(TAG, target.getDrawings().size() + "");
                            Log.d(TAG, line.getFromX() + "");
                            lastLines++;
                            linePX = event.getX();
                            linePY = event.getY();
                        }
                    } else {
                        //add half the piece's dimensions so it looks like you're moving them by
                        //the center instead of the corner
                        if (event.getPointerCount() == 2) {

                            if (prevY == Float.MIN_VALUE) {
                                prevY = event.getY(1);
                                Log.d(TAG, "SETTING VAL1");
                            }

                            if (prevX == Float.MIN_VALUE) {
                                prevX = event.getX(1);
                                Log.d(TAG, "SETTING VAL2");
                            }

                            float x1 = event.getX(0);
                            float y1 = event.getY(0);

                            float x2 = event.getX(1);
                            float y2 = event.getY(1);

                            float x3 = prevX;
                            float y3 = prevY;

                            double angle = Math.toDegrees(Math.atan2(x2 - x1, y2 - y1) -
                                    Math.atan2(x3 - x1, y3 - y1));

                            //Log.w("WHAT", "" + angle);

                            if(angle < 0) {
                                //angle += 360;
                            }

                            int check = (rotate - (int) angle);
                            while(check < 0) {
                                check += 360;
                            }
                            while(check >= 360) {
                                check -= 360;
                            }

                            if (check < target.getMaxRotate()) {
                                target.setAngle(check);
                                DatabaseReference ref = mDatabase.getReference("gamePortal/groups/" + GROUP_ID + "/matches/" + MATCH_ID +
                                        "/pieces/" + target.getPieceIndex() + "/currentState");
                                Map<String, Object> degrees = new HashMap<>();
                                degrees.put("rotationDegrees", check);
                                ref.updateChildren(degrees);
                            }


                        } else {
                            int dx = (x - target.getCurrentState().getX());
                            int dy = (y - target.getCurrentState().getY());
                            target.getCurrentState().setX(target.getCurrentState().getX() + dx);
                            target.getCurrentState().setY(target.getCurrentState().getY() + dy);
                            Log.d(TAG, "ACTION_MOVE");

                            /*for(int i = 0; i < target.getDrawings().size(); i++) {
                                target.getDrawings().get(i).changeXBy(dx);
                                target.getDrawings().get(i).changeYBy(dy);
                            } */
                            //Log.d(TAG, dx + ":" + dy);
                        }
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
                if(setCard) {
                    if(target != null) {
                        if(target.getType().equals("card")) {
                            DatabaseReference pIndex = mDatabase.getReference("gamePortal/groups/" + GROUP_ID + "/participants/");
                            //Log.w(TAG, gameId + " " + GROUP_ID);
                            pIndex.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    //target.setCanSee(true);
                                    Map<String, Object> see = new HashMap<>();
                                    mDatabase.getReference("gamePortal/groups/" + GROUP_ID + "/matches/" + MATCH_ID +
                                            "/pieces/" + target.getPieceIndex() + "/currentState/cardVisibility").setValue(see);
                                    if (cardVis == 0) {
                                        Log.w(TAG, "Card vis is 0");
                                    } else if (cardVis == 1) {
                                        Log.w(TAG, "Card vis is 1");
                                        see.put(dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("participantIndex").getValue().toString(), true);
                                    } else if (cardVis == 2) {
                                        Log.w(TAG, "Card vis is 2");
                                        Log.w(TAG, dataSnapshot.toString());
                                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                                            if (!child.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                                see.put(child.child("participantIndex").getValue().toString(), true);
                                            }
                                        }
                                    } else if (cardVis == 3) {
                                        Log.w(TAG, "Card vis is 3");
                                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                                            see.put(child.child("participantIndex").getValue().toString(), true);
                                        }
                                    }
                                    mDatabase.getReference("gamePortal/groups/" + GROUP_ID + "/matches/" + MATCH_ID +
                                            "/pieces/" + target.getPieceIndex() + "/currentState/cardVisibility").updateChildren(see);
                                    Toast.makeText(getBaseContext(), "Changed card status!", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                } else
                if(clear) {
                    if(target != null) {
                        DatabaseReference ref = mDatabase.getReference("gamePortal/groups/" + GROUP_ID + "/matches/" + MATCH_ID +
                                "/pieces/" + target.getPieceIndex() + "/currentState/drawing");
                        if(target.getDrawings().size() > 0) {
                            history = new ArrayList<>();
                            historyRef = new ArrayList<>();
                        }
                        for(int i = 0; i < target.getDrawings().size(); i++) {
                            ref.child(target.getDrawings().get(i).getPushId()).removeValue();
                        }
                    }
                } else if (pressDuration < MAX_CLICK_DURATION &&
                        distance(pressedX, pressedY, event.getX(), event.getY()) < MAX_CLICK_DISTANCE) {
                    if (target != null) {
                        if(draw) {
                            Log.d(TAG, "dot");
                            history.add(lastLines);
                            target = null;
                        } else {
                            Log.d(TAG, "ACTION_UP_CLICK");
                            int newImageIndex = target.getNextImageIndex();
                            if(target.getType().equals("dice")) {
                                diceRoll.start();
                            }
                            Map<String, Object> newState = new HashMap<>();
                            newState.put("currentImageIndex", newImageIndex);
                            Log.d(TAG, "Updating image index to " + newImageIndex);
                            FirebaseDatabase.getInstance().getReference(
                                    "gamePortal/groups/" + GROUP_ID + "/matches/" + MATCH_ID +
                                            "/pieces/" + target.getPieceIndex() + "/currentState")
                                    .updateChildren(newState);
                            target = null;
                        }
                    }
                }
                // Drag event
                else {
                    if (target != null && event.getPointerCount() == 1) {
                        if(draw) {
                            history.add(lastLines);
                            target = null;
                        } else {
                            if(target.getType().equals("card")) {
                                cardPlace.start();
                            } else {
                                setPiece.start();
                            }
                            Log.d(TAG, "ACTION_UP_DRAG");
                            Map<String, Object> loc = new HashMap<>();
                            double nX = x - (target.getWidth()/2);
                            double nY = y - (target.getHeight()/2);
                            int newX = (int) (nX / (double) mGame.getBoard().getWidth() * 100);
                            int newY = (int) (nY / (double) mGame.getBoard().getHeight() * 100);
                            loc.put("x", newX);
                            loc.put("y", newY);
                            Log.d(TAG, "moving piece to " + newX + ":" + newY);
                            FirebaseDatabase.getInstance().getReference(
                                    "gamePortal/groups/" + GROUP_ID + "/matches/" + MATCH_ID +
                                            "/pieces/" + target.getPieceIndex() + "/currentState")
                                    .updateChildren(loc);
                            for(int i = 0; i < mGame.getPieces().size(); i ++) {
                                mGame.getPieces().get(i).getCurrentState().setzDepth((i+1));
                            }
                            target.getCurrentState().setzDepth(mGame.getPieces().size() + 2);
                            Collections.sort(mGame.getPieces());
                            for(int i = 0; i < mGame.getPieces().size(); i ++) {
                                FirebaseDatabase.getInstance().getReference(
                                        "gamePortal/groups/" + GROUP_ID + "/matches/" + MATCH_ID +
                                                "/pieces/" + mGame.getPieces().get(i).getPieceIndex() + "/currentState/zDepth").setValue(mGame.getPieces().get(i).getCurrentState().getzDepth());
                            }

                            target = null;
                        }
                    } else {
                        Log.d(TAG, "LIFTED UP A POINTER");
                    }
                }

                break;
        }
        if (event.getAction() == 262) {
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

    private float pxToDp(float px) {
        return px / getResources().getDisplayMetrics().density;
    }

    @Override
    public void onClick(View view) {
        int v = view.getId();
        if(v == R.id.drawButton) {
            final int sdk = android.os.Build.VERSION.SDK_INT;
            if(draw) {
                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    findViewById(R.id.drawButton).setBackgroundDrawable(getResources().getDrawable(R.drawable.drawbuttonoff));
                } else {
                    findViewById(R.id.drawButton).setBackground(getResources().getDrawable(R.drawable.drawbuttonoff));
                }
            } else {
                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    findViewById(R.id.clearButton).setBackgroundDrawable(getResources().getDrawable(R.drawable.drawbuttonoff));
                    findViewById(R.id.drawButton).setBackgroundDrawable(getResources().getDrawable(R.drawable.drawbuttonon));
                } else {
                    findViewById(R.id.clearButton).setBackground(getResources().getDrawable(R.drawable.drawbuttonoff));
                    findViewById(R.id.drawButton).setBackground(getResources().getDrawable(R.drawable.drawbuttonon));
                }
                clear = false;
            }
            draw = !draw;
        } else if(v == R.id.clearButton) {
            final int sdk = android.os.Build.VERSION.SDK_INT;
            if(clear) {
                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    findViewById(R.id.clearButton).setBackgroundDrawable(getResources().getDrawable(R.drawable.drawbuttonoff));
                } else {
                    findViewById(R.id.clearButton).setBackground(getResources().getDrawable(R.drawable.drawbuttonoff));
                }
            } else {
                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    findViewById(R.id.clearButton).setBackgroundDrawable(getResources().getDrawable(R.drawable.drawbuttonon));
                    findViewById(R.id.drawButton).setBackgroundDrawable(getResources().getDrawable(R.drawable.drawbuttonoff));
                } else {
                    findViewById(R.id.clearButton).setBackground(getResources().getDrawable(R.drawable.drawbuttonon));
                    findViewById(R.id.drawButton).setBackground(getResources().getDrawable(R.drawable.drawbuttonoff));
                }
                draw = false;
            }
            clear = !clear;
        } else if(v == R.id.undoButton) {
            if(history.size() > 0) {
                int tbr = history.get(history.size() - 1);
                GamePiece ref = historyRef.get(historyRef.size() - 1);
                for(int i = 0; i < tbr; i ++) {
                    Log.d(TAG, "Removing lines");
                    mDatabase.getReference("gamePortal/groups/" + GROUP_ID + "/matches/" + MATCH_ID +
                            "/pieces/" + ref.getPieceIndex() + "/currentState/drawing").child(ref.getDrawings().get(ref.getDrawings().size() - 1).getPushId()).removeValue();
                    ref.getDrawings().remove(ref.getDrawings().size() - 1);
                }
                history.remove(history.size() - 1);
                historyRef.remove(historyRef.size() - 1);
            }
        } else if(v == R.id.visAllBut) {
            cardVis = 2;
            final int sdk = android.os.Build.VERSION.SDK_INT;
            if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                findViewById(v).setBackgroundDrawable(getResources().getDrawable(R.drawable.genericbuttonon));
                findViewById(R.id.visMe).setBackgroundDrawable(getResources().getDrawable(R.drawable.genericbuttonoff));
                findViewById(R.id.visEvery).setBackgroundDrawable(getResources().getDrawable(R.drawable.genericbuttonoff));
                findViewById(R.id.visNone).setBackgroundDrawable(getResources().getDrawable(R.drawable.genericbuttonoff));
            } else {
                findViewById(v).setBackground(getResources().getDrawable(R.drawable.genericbuttonon));
                findViewById(R.id.visMe).setBackground(getResources().getDrawable(R.drawable.genericbuttonoff));
                findViewById(R.id.visEvery).setBackground(getResources().getDrawable(R.drawable.genericbuttonoff));
                findViewById(R.id.visNone).setBackground(getResources().getDrawable(R.drawable.genericbuttonoff));
            }
        } else if(v == R.id.visEvery) {
            cardVis = 3;
            final int sdk = android.os.Build.VERSION.SDK_INT;
            if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                findViewById(v).setBackgroundDrawable(getResources().getDrawable(R.drawable.genericbuttonon));
                findViewById(R.id.visMe).setBackgroundDrawable(getResources().getDrawable(R.drawable.genericbuttonoff));
                findViewById(R.id.visAllBut).setBackgroundDrawable(getResources().getDrawable(R.drawable.genericbuttonoff));
                findViewById(R.id.visNone).setBackgroundDrawable(getResources().getDrawable(R.drawable.genericbuttonoff));
            } else {
                findViewById(v).setBackground(getResources().getDrawable(R.drawable.genericbuttonon));
                findViewById(R.id.visMe).setBackground(getResources().getDrawable(R.drawable.genericbuttonoff));
                findViewById(R.id.visAllBut).setBackground(getResources().getDrawable(R.drawable.genericbuttonoff));
                findViewById(R.id.visNone).setBackground(getResources().getDrawable(R.drawable.genericbuttonoff));
            }
        } else if(v == R.id.visMe) {
                cardVis = 1;
            final int sdk = android.os.Build.VERSION.SDK_INT;
            if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                findViewById(v).setBackgroundDrawable(getResources().getDrawable(R.drawable.genericbuttonon));
                findViewById(R.id.visEvery).setBackgroundDrawable(getResources().getDrawable(R.drawable.genericbuttonoff));
                findViewById(R.id.visAllBut).setBackgroundDrawable(getResources().getDrawable(R.drawable.genericbuttonoff));
                findViewById(R.id.visNone).setBackgroundDrawable(getResources().getDrawable(R.drawable.genericbuttonoff));
            } else {
                findViewById(v).setBackground(getResources().getDrawable(R.drawable.genericbuttonon));
                findViewById(R.id.visAllBut).setBackground(getResources().getDrawable(R.drawable.genericbuttonoff));
                findViewById(R.id.visEvery).setBackground(getResources().getDrawable(R.drawable.genericbuttonoff));
                findViewById(R.id.visNone).setBackground(getResources().getDrawable(R.drawable.genericbuttonoff));
            }
        } else if(v == R.id.visNone) {
            cardVis = 0;
            final int sdk = android.os.Build.VERSION.SDK_INT;
            if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                findViewById(v).setBackgroundDrawable(getResources().getDrawable(R.drawable.genericbuttonon));
                findViewById(R.id.visMe).setBackgroundDrawable(getResources().getDrawable(R.drawable.genericbuttonoff));
                findViewById(R.id.visEvery).setBackgroundDrawable(getResources().getDrawable(R.drawable.genericbuttonoff));
                findViewById(R.id.visAllBut).setBackgroundDrawable(getResources().getDrawable(R.drawable.genericbuttonoff));
            } else {
                findViewById(v).setBackground(getResources().getDrawable(R.drawable.genericbuttonon));
                findViewById(R.id.visMe).setBackground(getResources().getDrawable(R.drawable.genericbuttonoff));
                findViewById(R.id.visEvery).setBackground(getResources().getDrawable(R.drawable.genericbuttonoff));
                findViewById(R.id.visAllBut).setBackground(getResources().getDrawable(R.drawable.genericbuttonoff));
            }
        } else if(v == R.id.setAll) {
            for(int i = 0; i < mGame.getPieces().size(); i++) {
                final GamePiece target = mGame.getPieces().get(i);
                if(target.getType().equals("card")) {
                    DatabaseReference pIndex = mDatabase.getReference("gamePortal/groups/" + GROUP_ID + "/participants/");
                    Log.w(TAG, gameId + " " + GROUP_ID);
                    pIndex.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //target.setCanSee(true);
                            Map<String, Object> see = new HashMap<>();
                            mDatabase.getReference("gamePortal/groups/" + GROUP_ID + "/matches/" + MATCH_ID +
                                    "/pieces/" + target.getPieceIndex() + "/currentState/cardVisibility").setValue(see);
                            if (cardVis == 0) {
                                Log.w(TAG, "Card vis is 0");
                            } else if (cardVis == 1) {
                                Log.w(TAG, "Card vis is 1");
                                see.put(dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("participantIndex").getValue().toString(), true);
                            } else if (cardVis == 2) {
                                Log.w(TAG, "Card vis is 2");
                                Log.w(TAG, dataSnapshot.toString());
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    Log.w(TAG, child.child("participantIndex").getValue().toString());
                                    if (!child.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                        see.put(child.child("participantIndex").getValue().toString(), true);
                                    }
                                }
                            } else if (cardVis == 3) {
                                Log.w(TAG, "Card vis is 3");
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    see.put(child.child("participantIndex").getValue().toString(), true);
                                }
                            }
                            mDatabase.getReference("gamePortal/groups/" + GROUP_ID + "/matches/" + MATCH_ID +
                                    "/pieces/" + target.getPieceIndex() + "/currentState/cardVisibility").updateChildren(see);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
            Toast.makeText(getBaseContext(), "YOU'VE CHANGED ALL CARDS", Toast.LENGTH_SHORT).show();
        } else if(v == R.id.setCard) {
            final int sdk = android.os.Build.VERSION.SDK_INT;
            if(setCard) {
                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    findViewById(v).setBackgroundDrawable(getResources().getDrawable(R.drawable.genericbuttonoff));
                } else {
                    findViewById(v).setBackground(getResources().getDrawable(R.drawable.genericbuttonoff));
                }
            } else {
                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    findViewById(v).setBackgroundDrawable(getResources().getDrawable(R.drawable.genericbuttonon));
                } else {
                    findViewById(v).setBackground(getResources().getDrawable(R.drawable.genericbuttonon));
                }
            }
            setCard = !setCard;
        }
    }
}
