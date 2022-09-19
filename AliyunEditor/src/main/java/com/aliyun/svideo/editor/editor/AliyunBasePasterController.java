package com.aliyun.svideo.editor.editor;

import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.aliyun.svideosdk.common.struct.effect.ActionBase;
import com.aliyun.svideosdk.common.struct.effect.EffectBase;
import com.aliyun.svideosdk.editor.AliyunPasterBaseView;

public abstract class AliyunBasePasterController implements AliyunPasterBaseView {

    public abstract boolean isEditCompleted();

    public abstract boolean isVisibleInTime(long duration);

    public abstract void setPasterViewVisibility(int visibility);

    public abstract void showTimeEdit();

    public abstract void hideOverlayView();

    public abstract void editTimeCompleted();

    public abstract void removePaster();

    public abstract void editTimeStart();

    public abstract void showTextEdit(boolean mUseInvert);

    public abstract void moveContent(float dx, float dy);

    public abstract boolean contentContains(float x, float y);

    public abstract boolean isAddedAnimation();

    public abstract UIEditorPage getEditorPage();

    public boolean canDrag() {
        return true;
    }

    public abstract void moveToCenter();

    public abstract boolean isPasterExists();

    public abstract boolean isPasterRemoved();

    public abstract void setOnlyApplyUI(boolean b);
}
