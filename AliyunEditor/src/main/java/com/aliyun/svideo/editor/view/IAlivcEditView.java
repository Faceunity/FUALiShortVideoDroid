package com.aliyun.svideo.editor.view;

import android.view.View;

import com.aliyun.svideo.editor.effects.caption.listener.OnVideoUpdateDurationListener;
import com.aliyun.svideosdk.editor.AliyunIEditor;

public interface IAlivcEditView {

    View getSufaceView();

    AlivcEditView getAlivcEditView();

    AliyunIEditor getAliyunIEditor();

    void addVideoUpdateListener(OnVideoUpdateDurationListener onVideoUpdateDurationListener);

    void removeVideoUpdateListener(OnVideoUpdateDurationListener onVideoUpdateDurationListener);

}
