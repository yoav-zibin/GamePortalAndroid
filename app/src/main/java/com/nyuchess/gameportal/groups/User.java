package com.nyuchess.gameportal.groups;

/**
 * Created by Jordan on 10/5/2017.
 */

public class User {

    private String displayName;
    private String uid;

    User(String name, String id){
        displayName = name;
        uid = id;
    }

    String getDisplayName(){
        return displayName;
    }
    String getUid() { return uid; }

}
