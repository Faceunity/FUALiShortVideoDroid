package com.aliyun.svideo.editor.publish;

import com.aliyun.qupai.editor.AliyunICompose;
import com.aliyun.qupai.editor.impl.AliyunComposeFactory;
import com.aliyun.qupai.editor.impl.AliyunVodCompose;

/**
 * Created by apple on 2017/11/14.
 */

public enum ComposeFactory {
    /**
     * 普通合成及上传
     */
    INSTANCE,
    /**
     * 点播凭证的方式合成及上传
     */
    VODCOMPOSE;
    private AliyunICompose mInstance;
    private AliyunVodCompose aliyunVodCompose;
    ComposeFactory() {
        mInstance = AliyunComposeFactory.createAliyunCompose();
        aliyunVodCompose = AliyunComposeFactory.createAliyunVodCompose();
    }

    public AliyunICompose getInstance() {
        return mInstance;
    }

    public AliyunVodCompose getAliyunVodCompose() {
        return aliyunVodCompose;
    }
}
