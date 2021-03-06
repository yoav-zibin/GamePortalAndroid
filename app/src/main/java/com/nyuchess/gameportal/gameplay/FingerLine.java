package com.nyuchess.gameportal.gameplay;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by Victor on 11/26/2017.
 */

public class FingerLine {
    private float fromX, toX, fromY, toY, thickness;
    private int color;
    private String userId, pushId;

    public FingerLine(float fromX, float toX, float fromY, float toY, int color, float thickness, String pushId) {
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.pushId = pushId;
        this.fromX = fromX;
        this.toX = toX;
        this.fromY = fromY;
        this.toY = toY;
        this.color = color;
        this.thickness = thickness;
    }

    public float getThickness() {
        return thickness;
    }

    public int getColor() {
        return color;
    }

    public float getY() {
        return toY;
    }

    public float getFromY() {
        return fromY;
    }

    public float getToX() {
        return toX;
    }

    public float getToY() {
        return toY;
    }

    public float getFromX() {
        return fromX;
    }

    public String getUserId() {
        return userId;
    }

    public void changeXBy(int dx) {
        this.fromX += dx;
        this.toX += dx;
    }

    public void changeYBy (int dy) {
        this.fromY += dy;
        this.toY += dy;
    }

    public String getPushId() {
        return pushId;
    }
}
