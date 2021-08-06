package com.aliyun.svideo.editor.publish;

import com.aliyun.svideosdk.editor.impl.AliyunComposeFactory;
import com.aliyun.svideosdk.editor.impl.AliyunVodCompose;

/**
 * Created by apple on 2017/11/14.
 */

public enum ComposeFactory {
    /**
     * 合成及上传
     */
    INSTANCE;

    private AliyunVodCompose aliyunVodCompose;
    ComposeFactory() {
        aliyunVodCompose = AliyunComposeFactory.createAliyunVodCompose();
    }

    public AliyunVodCompose getAliyunVodCompose() {
        return aliyunVodCompose;
    }
}
