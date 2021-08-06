package com.aliyun.svideo.editor.bean;


import com.aliyun.svideosdk.common.struct.effect.ValueTypeEnum;


/**
 * 保存转场动画bean
 */
public class AlivcTransBean {

  private ValueTypeEnum mType;
  private Float mFloatValue;
  private int mIntergerValue;

    public ValueTypeEnum getmType() {
        return mType;
    }

    public void setmType(ValueTypeEnum mType) {
        this.mType = mType;
    }

    public Float getmFloatValue() {
        return mFloatValue;
    }

    public void setmFloatValue(Float mFloatValue) {
        this.mFloatValue = mFloatValue;
    }

    public int getmIntergerValue() {
        return mIntergerValue;
    }

    public void setmIntergerValue(int mIntergerValue) {
        this.mIntergerValue = mIntergerValue;
    }
}
