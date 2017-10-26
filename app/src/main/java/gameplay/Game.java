package gameplay;

import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

import java.security.acl.Group;
import java.util.List;

/**
 * Created by Jordan on 10/22/2017.
 */

public class Game {

    private String mGameId;

    private List<GamePiece> mGamePieces;
    private GameBoard mGameBoard;

    public Game(String gameId, String MatchId, String GroupId){
        mGameId = gameId;
        mGameBoard = new GameBoard(gameId);
        mGamePieces = new GamePieces(gameId, MatchId, GroupId);
    }

    public void draw(Canvas canvas){
        mGameBoard.draw(canvas);
        for (GamePiece piece: mGamePieces){
            piece.draw(canvas);
        }
    }

    public List<GamePiece> getPieces() {
        return mGamePieces;
    }
}
