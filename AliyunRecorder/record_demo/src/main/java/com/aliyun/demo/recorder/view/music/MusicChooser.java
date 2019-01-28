package com.aliyun.demo.recorder.view.music;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.apsaravideo.music.music.MusicChooseView;
import com.aliyun.apsaravideo.music.music.MusicFileBean;
import com.aliyun.demo.recorder.view.dialog.BaseChooser;

/**
 * @author zsy_18 data:2018/8/29
 */
public class MusicChooser extends BaseChooser{
    private MusicSelectListener musicSelectListener;
    //视频录制时长
    private int mRecordTime = 10*1000;
    private MusicChooseView musicChooseView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (musicChooseView==null){
            musicChooseView = new MusicChooseView(getContext());

            musicChooseView.setRecordTime(mRecordTime);
            musicChooseView.setMusicSelectListener(new com.aliyun.apsaravideo.music.music.MusicSelectListener() {
                @Override
                public void onMusicSelect(MusicFileBean musicFileBean, long startTime) {
                    dismiss();
                    musicSelectListener.onMusicSelect(musicFileBean, startTime);
                }

                @Override
                public void onCancel() {
                    dismiss();
                }
            });
        }else {
            if (musicChooseView.getParent()!=null){
                ((ViewGroup)musicChooseView.getParent()).removeView(musicChooseView);
            }
        }
        return musicChooseView;
    }

    public void setMusicSelectListener(MusicSelectListener musicSelectListener) {

        this.musicSelectListener = musicSelectListener;
    }

    public void setRecordTime(int mRecordTime) {
        this.mRecordTime = mRecordTime;
        if (musicChooseView!=null){
            musicChooseView.setRecordTime(mRecordTime);
        }
    }
    /**
     * 设置view的可见状态, 会在activity的onStart和onStop中调用
     * @param visibleStatus true: 可见, false: 不可见
     */
    public void setVisibleStatus(boolean visibleStatus) {
        if (musicChooseView != null) {
            musicChooseView.setVisibleStatus(visibleStatus);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setVisibleStatus(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        setVisibleStatus(false);
    }
}
