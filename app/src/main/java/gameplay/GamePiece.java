package gameplay;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by Jordan on 10/14/2017.
 */

public class GamePiece implements IGameElement {

    private int xInitial;

    private int yInitial;
    private int x;

    private int y;
    private int zDepth;

    // private int currentImageIndex;
    private Bitmap mImage;

    // which players can see this card
    private int[] cardVisibility;


    GamePiece(Bitmap image){
        mImage = image;
    }

    @Override
    public void draw(Canvas canvas) {

    }
}
