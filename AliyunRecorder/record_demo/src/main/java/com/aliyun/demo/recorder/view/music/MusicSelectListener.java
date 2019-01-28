package com.aliyun.demo.recorder.view.music;

import com.aliyun.apsaravideo.music.music.MusicFileBean;

public interface MusicSelectListener {
    void onMusicSelect(MusicFileBean musicFileBean, long startTime);
}
