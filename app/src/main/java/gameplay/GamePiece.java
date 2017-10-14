package gameplay;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by Jordan on 10/14/2017.
 */

public class GamePiece implements IGameElement {

    private Bitmap mImage;

    GamePiece(Bitmap image){
        mImage = image;
    }

    @Override
    public void draw(Canvas canvas) {

    }
}
