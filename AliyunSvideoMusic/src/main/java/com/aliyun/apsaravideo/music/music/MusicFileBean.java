package com.aliyun.apsaravideo.music.music;

public class MusicFileBean {
    public MusicFileBean(String title, String artist, String musicId) {
        this.title = title;
        this.artist = artist;
        this.musicId = musicId;
    }

    public MusicFileBean() {
    }

    public int id; //id标识
    public String title; // 显示名称
    public String displayName; // 文件名称
    public String path; // 音乐文件的路径
    public int duration; // 媒体播放总时间
    public String artist; // 艺术家
    public String musicId;//音乐id
    public long size;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getMusicId() {
        return musicId;
    }

    public void setMusicId(String musicId) {
        this.musicId = musicId;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
