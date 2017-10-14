package gameplay;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by Jordan on 10/14/2017.
 */

public class GameBoard implements IGameElement {

    private Bitmap mImage;

    GameBoard(Bitmap image){
        mImage = image;
    }

    @Override
    public void draw(Canvas canvas) {

    }
}
