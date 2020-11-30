package com.aliyun.svideo.recorder.view.music;

import com.aliyun.svideo.base.http.MusicFileBean;

public interface MusicSelectListener {
    void onMusicSelect(MusicFileBean musicFileBean, long startTime);
}
