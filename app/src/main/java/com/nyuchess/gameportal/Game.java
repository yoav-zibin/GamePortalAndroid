package com.nyuchess.gameportal;

/**
 * Created by Vicsta on 10/23/2017.
 */

class Game {

    private String gameName;
    private String id;

    Game(String name, String id){
        gameName = name;
        this.id = id;
    }

    String getGameName(){
        return gameName;
    }
    String getId() { return id; }

}
