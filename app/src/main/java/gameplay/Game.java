package gameplay;

import android.graphics.Canvas;

import java.util.List;

/**
 * Created by Jordan on 10/22/2017.
 */

public class Game {

    private String mGameId;

    private List<GamePiece> mGamePieces;
    private GameBoard mGameBoard;

    public Game(String gameId){
        mGameId = gameId;
        mGameBoard = new GameBoard(gameId);
        mGamePieces = new GamePieces(gameId);
    }

    public void draw(Canvas canvas){
        mGameBoard.draw(canvas);
        for (GamePiece piece: mGamePieces){
            piece.draw(canvas);
        }
    }
}
