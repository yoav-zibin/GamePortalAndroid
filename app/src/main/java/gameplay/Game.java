package gameplay;

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
        mAdapter = new GameFirebaseAdapter();
        init();
    }

    private void init(){
        mGameBoard = mAdapter.getGameBoard(mGameId);
        mGamePieces = mAdapter.getGamePieces(mGameId);
    }
}
