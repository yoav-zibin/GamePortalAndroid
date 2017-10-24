package gameplay;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by Jordan on 10/14/2017.
 */

public class GamePiece implements IGameElement {

    private PieceState initialState;
    private String pieceElementId;
    private int deckPieceIndex;

//    private PieceState currentState;
    // private int currentImageIndex;
//    private Bitmap mImage;
    private int height;
    private int width;

    public PieceState getInitialState() {
        return initialState;
    }

    public String getPieceElementId() {
        return pieceElementId;
    }

    public int getDeckPieceIndex() {
        return deckPieceIndex;
    }
// which players can see this card
//    private int[] cardVisibility;


//    GamePiece(){    }

//    void setInitialState(PieceState state){
//        initialState = state;
////        currentState = state;
//    }
//
    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public void draw(Canvas canvas) {

    }

    static class PieceState {
        private int x;
        private int y;
        private int zDepth;
        private int currentImageIndex;

//        public PieceState(){}
//
//        public PieceState(int x, int y, int zDepth, int currentImageIndex){
//            this.x = x;
//            this.y = y;
//            this.zDepth = zDepth;
//            this.currentImageIndex = currentImageIndex;
//        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getzDepth() {
            return zDepth;
        }

        public int getCurrentImageIndex() {
            return currentImageIndex;
        }
    }

}
