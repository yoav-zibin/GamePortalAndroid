package com.nyuchess.gameportal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import gameplay.Game;

/**
 * Created by Jordan on 10/23/2017.
 */

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "GameView'";

    private Game mGame;

    public GameView(Context context, AttributeSet attrs){
        super(context, attrs);
        getHolder().addCallback(this);
        setFocusable(true);
        setWillNotDraw(false);

        Log.d(TAG, "constructor called");
    }

    void setGame(Game game){
        mGame = game;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.v(TAG, "draw canvas");
        canvas.drawColor(Color.WHITE);
        mGame.draw(canvas);
        postInvalidate();
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}
