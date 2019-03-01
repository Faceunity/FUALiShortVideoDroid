package com.aliyun.svideo.editor.msg.body;

import com.aliyun.svideo.editor.effects.control.EffectInfo;

public class LongClickAnimationFilter {
    private EffectInfo mEffectInfo;
    private int mIndex;

    private LongClickAnimationFilter(Builder builder) {
        mEffectInfo = builder.mEffectInfo;
        mIndex = builder.mIndex;
    }

    public EffectInfo getEffectInfo() {
        return mEffectInfo;
    }

    public int getIndex() {
        return mIndex;
    }

    public static final class Builder {
        private EffectInfo mEffectInfo;
        private int mIndex;

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

        public LongClickAnimationFilter build() {
            return new LongClickAnimationFilter(this);
        }
    }
}
