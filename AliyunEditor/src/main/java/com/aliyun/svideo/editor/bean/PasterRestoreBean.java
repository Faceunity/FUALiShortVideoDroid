package com.aliyun.svideo.editor.bean;

import com.aliyun.svideosdk.common.struct.effect.ActionBase;
import com.aliyun.svideosdk.common.struct.effect.EffectBase;

/**
 * 动图撤销恢复相关bean
 */
public class PasterRestoreBean {

    private EffectBase mEffectBase;
    private int mFrameSelectedPosition;
    private ActionBase mFrameAction;
    private ActionBase mTempFrameAction;

    public EffectBase getEffectBase() {
        return mEffectBase;
    }

    public void setEffectBase(EffectBase effectBase) {
        mEffectBase = effectBase;
    }

    public int getFrameSelectedPosition() {
        return mFrameSelectedPosition;
    }

    public void setFrameSelectedPosition(int frameSelectedPosition) {
        mFrameSelectedPosition = frameSelectedPosition;
    }

    public ActionBase getFrameAction() {
        return mFrameAction;
    }

    public void setFrameAction(ActionBase frameAction) {
        mFrameAction = frameAction;
    }

    public ActionBase getTempFrameAction() {
        return mTempFrameAction;
    }

    public void setTempFrameAction(ActionBase mTempFrameAction) {
        this.mTempFrameAction = mTempFrameAction;
    }
}
