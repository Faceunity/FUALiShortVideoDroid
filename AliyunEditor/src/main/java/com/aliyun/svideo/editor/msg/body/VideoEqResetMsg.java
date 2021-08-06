package com.aliyun.svideo.editor.msg.body;

import com.aliyun.svideosdk.common.internal.videoaugment.VideoAugmentationType;

public class VideoEqResetMsg {
    private VideoAugmentationType mType;

    public void setType(VideoAugmentationType type) {
        this.mType = type;
    }

    public VideoAugmentationType getType() {
        return mType;
    }
}
