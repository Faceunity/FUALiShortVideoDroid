/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.util;

import android.annotation.TargetApi;
import android.widget.TextView;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN;

public class CompatUtil {

    @TargetApi(JELLY_BEAN)
    public static void generateSpacingmultAndSpacingadd(float[] args, TextView textview) {
        if (SDK_INT >= JELLY_BEAN) {
            args[0] = textview.getLineSpacingMultiplier();
            args[1] = textview.getLineSpacingExtra();
        } else {
            args[0] = 1f;
            args[1] = 0f;
        }
    }

}
