package com.aliyun.svideo.editor.effects.audiomix;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.aliyun.svideo.music.music.MusicChooseView;
import com.aliyun.svideo.base.http.MusicFileBean;
import com.aliyun.svideo.music.music.MusicSelectListener;
import com.aliyun.svideo.editor.effects.control.BaseChooser;
import com.aliyun.svideo.editor.effects.control.EffectInfo;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;

/**
 * @author zsy_18 data:2018/8/29
 */
public class MusicChooser extends BaseChooser {
    private long mStreamDuration = 10 * 1000;
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

    public void setStreamDuration(long streamDuration) {
        this.mStreamDuration = streamDuration;
        if (musicChooseView != null) {
            musicChooseView.setStreamDuration((int)mStreamDuration);
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
                if (musicFileBean != null) {
                    effectInfo.id = musicFileBean.id;
                    effectInfo.setPath(musicFileBean.getPath());
                }
                effectInfo.musicWeight = 50;
                effectInfo.startTime = 0;
                effectInfo.type = UIEditorPage.AUDIO_MIX;
                effectInfo.endTime = mStreamDuration;
                effectInfo.streamStartTime = startTime;
                effectInfo.streamEndTime = startTime + mStreamDuration;

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
