package com.soundbound.outer.API.songProviders.Models;

import java.io.Serializable;

public class Room implements Serializable {
    private String name, owner, token;
    private int id;

    public Room(String name, int id) {
        this.name = name;
        this.id = id;
    }


    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }
    public String getOwner() {
        return owner;
    }
    public int getId() {
        return id;
    }
    public String getToken(){
        return token;}
    public void setToken(String token){
        this.token = token;}
}
