package com.aliyun.svideo.editor.msg.body;

import com.aliyun.svideo.editor.effects.control.EffectInfo;
import com.aliyun.svideosdk.common.struct.effect.EffectConfig;

public class LongClickAnimationFilter {
    private EffectInfo mEffectInfo;
    private int mIndex;
    private EffectConfig mEffectConfig;

    private LongClickAnimationFilter(Builder builder) {
        mEffectInfo = builder.mEffectInfo;
        mIndex = builder.mIndex;
        mEffectConfig = builder.mEffectConfig;
    }

    public EffectInfo getEffectInfo() {
        return mEffectInfo;
    }

    public EffectConfig getEffectConfig(){
        return mEffectConfig;
    }

    public int getIndex() {
        return mIndex;
    }

    public static final class Builder {
        private EffectInfo mEffectInfo;
        private int mIndex;
        private EffectConfig mEffectConfig;

        public Builder() {
        }

        public Builder effectInfo(EffectInfo val) {
            mEffectInfo = val;
            return this;
        }

        public Builder index(int val) {
            mIndex = val;
            return this;
        }
        public Builder effectConfig(EffectConfig val) {
            mEffectConfig = val;
            return this;
        }

        public LongClickAnimationFilter build() {
            return new LongClickAnimationFilter(this);
        }
    }
}
