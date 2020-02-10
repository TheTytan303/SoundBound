package com.soundbound;

import android.graphics.Bitmap;

import com.spotify.android.appremote.api.ImagesApi;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.Track;

public class SimpleSong {
    public enum Type {SPOTIFY, PHONE, YOUTUBE};
    public String title,author,album,id;
    public Bitmap cover;
    public long duration;
    public Type type;

    public SimpleSong(){
        //this.type = Type.PHONE;
    }
    public SimpleSong(String title, String author, String album, String id, Bitmap cover, long duration, Type type) {
        this.title = title;
        this.author = author;
        this.album = album;
        this.id = id;
        this.cover = cover;
        this.duration = duration;
        this.type = type;
    }
    public SimpleSong(Track track, ImagesApi imagesApi){

        this.type = Type.SPOTIFY;
        this.title  = track.name;
        this.id = track.uri;
        this.author = track.artist.name;
        this.album = track.album.name;
        imagesApi.getImage(track.imageUri, Image.Dimension.SMALL).setResultCallback(bitmap -> cover = bitmap);
    }
}
