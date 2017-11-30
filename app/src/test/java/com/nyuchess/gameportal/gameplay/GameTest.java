package com.nyuchess.gameportal.gameplay;

import android.app.Activity;

import com.google.firebase.database.DataSnapshot;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.*;

import static org.mockito.Mockito.*;
import static org.junit.runners.JUnit4.*;
import static org.hamcrest.MatcherAssert.*;

/**
 * Created by Jordan on 11/2/2017.
 */

@RunWith(MockitoJUnitRunner.class)
public class GameTest {

    @Test
    public void testCreateGame(){
        Game game = new Game("gameid", "matchid", "groupid", new Activity());
        assertThat(game.getGameId(), is("gameid"));
        assertThat(game.getBoard().getGameId(), is("gameid"));
        assertThat(game.getPieces().getGameId(), is("gameid"));
        assertThat(game.getPieces().size(), is(0));
    }

    @Test
    public void testCreateGamePieces(){
        Activity a = new Activity();
        GamePieces pieces = new GamePieces("gameid", "matchid", "groupid", a);
        assertThat(pieces.getGameId(), is("gameid"));
        assertThat(pieces.size(), is(0));
        pieces.add(new GamePiece("gameid", "matchid", "groupid", a));
        assertThat(pieces.size(), is(1));
    }

    @Test
    public void testCreateGamePiece(){
        Activity a = new Activity();
//        when(mMockGamePieceSnapshot.child("pieceElementId").getValue()).thenReturn("12345");
//        when(mMockGamePieceSnapshot.child("deckPieceIndex").getValue()).thenReturn("1");
//        when(mMockGamePieceSnapshot.child("initialState").getValue()).thenReturn(
//                new GamePiece.PieceState(1, 2, 3, 4));
//        when(mMockGamePieceSnapshot.getKey()).thenReturn("mockKey");
        GamePiece piece = new GamePiece("gameid", "matchid", "groupid", a);
//        piece.startInit();
        assertThat(piece.getGameId(), is("gameid"));
        assertThat(piece.getMatchId(), is("matchid"));
        assertThat(piece.getGroupId(), is("groupid"));
    }

    @Test
    public void testCreateGameBoard(){
        GameBoard board = new GameBoard("gameid");
        assertThat(board.getGameId(), is("gameid"));
    }
}
