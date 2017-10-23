package gameplay;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by Jordan on 10/14/2017.
 */

public class GameBoard implements IGameElement {

    private String imageId;
    private String backgroundColor;
    private int maxScale;

    private Bitmap mImage;

    GameBoard() {}

//    GameBoard(String imageId, String backgroundColor, int maxScale){
//        this.imageId = imageId;
//        this.backgroundColor = backgroundColor;
//        this.maxScale = maxScale;
//    }
//
//    public String getImageId(){
//        return imageId;
//    }
//
//    public String getBackgroundColor() {
//        return backgroundColor;
//    }
//
//    public int getMaxScale() {
//        return maxScale;
//    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setMaxScale(int maxScale) {
        this.maxScale = maxScale;
    }

    @Override
    public void draw(Canvas canvas) {

    }
}
