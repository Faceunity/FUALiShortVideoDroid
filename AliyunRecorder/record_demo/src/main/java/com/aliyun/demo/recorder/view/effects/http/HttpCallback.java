/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.recorder.view.effects.http;

public interface HttpCallback<T> {
    void onSuccess(T result);
    void onFailure(Throwable e);
}
