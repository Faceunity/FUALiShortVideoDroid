package com.aliyun.svideo.editor.msg.body;

public class FilterTabClick {
    public static final int POSITION_COLOR_FILTER = 0;
    public static final int POSITION_ANIMATION_FILTER = 1;
    private int position;

    public FilterTabClick(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
