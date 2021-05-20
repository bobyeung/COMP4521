package com.example.musicplayerx;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.Serializable;

public class Audio implements Serializable {

    private String data;
    private String title = "No title";
    private String album = "No album";
    private String artist = "No artist";
    public byte[] albumArt = null;
    private int duration = 0;

    public Audio(String data, String title, String album, String artist, byte[] albumArt, int duration) {
        this.data = data;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.albumArt = albumArt;
        this.duration = duration;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public byte[] getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(byte[] albumArt) {
        this.albumArt = albumArt;
    }

    public Bitmap getAlbumArtBitmap() { return BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length); }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
