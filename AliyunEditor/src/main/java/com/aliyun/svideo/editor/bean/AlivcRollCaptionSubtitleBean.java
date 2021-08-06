package com.aliyun.svideo.editor.bean;

import android.graphics.Color;

import java.io.Serializable;

/**
 * 翻转字幕
 */
public class AlivcRollCaptionSubtitleBean implements Serializable {

    private String showTime;
    private String content;
    private boolean isChecked;
    private int color = Color.WHITE;
    private boolean inEdit;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isInEdit() {
        return inEdit;
    }

    public void setInEdit(boolean inEdit) {
        this.inEdit = inEdit;
    }

    public String getShowTime() {
        return showTime;
    }

    public void setShowTime(String showTime) {
        this.showTime = showTime;
    }
}
