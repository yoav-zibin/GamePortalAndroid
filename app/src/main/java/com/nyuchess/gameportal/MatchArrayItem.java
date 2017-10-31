package com.nyuchess.gameportal;

/**
 * Created by Jordan on 10/30/2017.
 */

public class MatchArrayItem {
    private String gameName;
    private String gameId;
    private String matchId;

    MatchArrayItem(String gameName, String gameId, String matchId){
        this.gameName = gameName;
        this.gameId = gameId;
        this.matchId = matchId;
    }

    String getGameName(){
        return gameName;
    }
    String getgameId() {
        return gameId;
    }
    String getMatchId() {
        return matchId;
    }
}
