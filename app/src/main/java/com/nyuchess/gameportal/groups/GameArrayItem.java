package com.nyuchess.gameportal.groups;

/**
 * Created by Vicsta on 10/23/2017.
 */

class GameArrayItem {

    private String gameName;
    private String id;

    GameArrayItem(String name, String id){
        gameName = name;
        this.id = id;
    }

    String getGameName(){
        return gameName;
    }
    String getId() { return id; }

}
