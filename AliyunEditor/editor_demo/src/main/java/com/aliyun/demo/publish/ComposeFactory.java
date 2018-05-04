package com.aliyun.demo.publish;

import com.aliyun.qupai.editor.AliyunICompose;
import com.aliyun.qupai.editor.impl.AliyunComposeFactory;

/**
 * Created by apple on 2017/11/14.
 */

public enum ComposeFactory {
    INSTANCE;
    private AliyunICompose mInstance;
    ComposeFactory() {
        mInstance = AliyunComposeFactory.createAliyunCompose();
    }

    public AliyunICompose getInstance() {
        return mInstance;
    }
}
