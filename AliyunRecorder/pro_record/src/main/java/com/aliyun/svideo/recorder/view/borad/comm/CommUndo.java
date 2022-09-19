package com.aliyun.svideo.recorder.view.borad.comm;

public interface CommUndo {
     void undo();
     void redo();
     boolean canUndo();
     boolean canRedo();
}
