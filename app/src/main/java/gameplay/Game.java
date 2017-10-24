package gameplay;

import android.graphics.Canvas;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jordan on 10/22/2017.
 */

public class Game {

    private String mGameId;
    private GameFirebaseAdapter mAdapter;

    private List<GamePiece> mGamePieces;
    private GameBoard mGameBoard;

    public Game(String gameId){
        mGameId = gameId;
        mGameBoard = new GameBoard(gameId);
        mGamePieces = new ArrayList<>();
//        mAdapter = new GameFirebaseAdapter();
//        init();
    }

//    private void init(){
//        mAdapter.getGameBoard(mGameId, mGameBoard);
//        mAdapter.getGamePieces(mGameId, mGamePieces);
//    }

    public void draw(Canvas canvas){
        mGameBoard.draw(canvas);
        for (GamePiece piece: mGamePieces){
            piece.draw(canvas);
        }
    }
}
