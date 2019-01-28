package com.aliyun.demo.effects.audiomix;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.aliyun.apsaravideo.music.music.MusicChooseView;
import com.aliyun.apsaravideo.music.music.MusicFileBean;
import com.aliyun.apsaravideo.music.music.MusicSelectListener;
import com.aliyun.demo.effects.control.BaseChooser;
import com.aliyun.demo.effects.control.EffectInfo;
import com.aliyun.demo.effects.control.UIEditorPage;

/**
 * @author zsy_18 data:2018/8/29
 */
public class MusicChooser extends BaseChooser {
    private long mRecordTime = 10 * 1000;
    private MusicChooseView musicChooseView;

    public MusicChooser(@NonNull Context context) {
        this(context, null);
    }

    public MusicChooser(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MusicChooser(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setRecordTime(long mRecordTime) {
        this.mRecordTime = mRecordTime;
        if (musicChooseView != null) {
            musicChooseView.setRecordTime((int)mRecordTime);
        }
    }

    @Override
    protected void init() {
        musicChooseView = new MusicChooseView(getContext());
        addView(musicChooseView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        musicChooseView.setMusicSelectListener(new MusicSelectListener() {
            @Override
            public void onMusicSelect(MusicFileBean musicFileBean, long startTime) {

                EffectInfo effectInfo = new EffectInfo();
                effectInfo.id = musicFileBean.id;
                effectInfo.setPath(musicFileBean.getPath());
                effectInfo.musicWeight = 50;
                effectInfo.startTime = 0;
                effectInfo.type = UIEditorPage.AUDIO_MIX;
                effectInfo.endTime = mRecordTime;
                effectInfo.streamStartTime = startTime;
                effectInfo.streamEndTime = startTime + mRecordTime;

                mOnEffectChangeListener.onEffectChange(effectInfo);
                if (mOnEffectActionLister != null) {
                    mOnEffectActionLister.onComplete();
                }
            }

            @Override
            public void onCancel() {
                onBackPressed();
            }
        });
    }

    @Override
    protected UIEditorPage getUIEditorPage() {
        return UIEditorPage.AUDIO_MIX;
    }

    @Override
    public boolean isPlayerNeedZoom() {
        return false;
    }

    @Override
    public void onBackPressed() {
        if (mOnEffectActionLister != null) {
            mOnEffectActionLister.onCancel();
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

}
