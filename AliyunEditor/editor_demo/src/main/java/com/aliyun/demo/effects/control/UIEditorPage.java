/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.effects.control;
/**
 * NOTE: item order must match
 */
public enum UIEditorPage {
    FILTER_EFFECT,
    OVERLAY,
    CAPTION,
    MV,
    AUDIO_MIX,
    PAINT,
    FONT
    ;

    public static
    UIEditorPage get(int index) {
        return values()[index];
    }

    public int index() {
        return ordinal();
    }
}
