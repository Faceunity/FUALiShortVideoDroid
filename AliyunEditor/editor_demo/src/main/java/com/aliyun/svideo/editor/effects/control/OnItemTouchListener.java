package com.aliyun.svideo.editor.effects.control;

public interface OnItemTouchListener {
    int EVENT_DOWN = 1;
    int EVENT_UP = 2;
    int EVENT_MOVE = 3;
    void onTouchEvent(int motionEvent, int index, EffectInfo info);
}
