package com.aliyun.race.sample.utils;

import android.content.Context;

import com.squareup.leakcanary.AndroidExcludedRefs;
import com.squareup.leakcanary.DisplayLeakService;
import com.squareup.leakcanary.ExcludedRefs;
import com.squareup.leakcanary.LeakCanary;


public class LeakCanaryUtils {
    public static void initLeakCanary(Context context) {
        //排除一些Android Sdk引起的泄漏
        ExcludedRefs excludedRefs = AndroidExcludedRefs.createAppDefaults()
                                    .instanceField("android.view.inputmethod.InputMethodManager", "sInstance")
                                    .instanceField("android.view.inputmethod.InputMethodManager", "mLastSrvView")
                                    .instanceField("com.android.internal.policy.PhoneWindow$DecorView", "mContext")
                                    .instanceField("android.support.v7.widget.SearchView$SearchAutoComplete", "mContext")
                                    .instanceField("android.app.ActivityThread$ActivityClientRecord", "activity")
                                    .instanceField("android.media.MediaScannerConnection", "mContext")
                                    .build();

        LeakCanary.refWatcher(context)
        .listenerServiceClass(DisplayLeakService.class)
        .excludedRefs(excludedRefs)
        .buildAndInstall();
    }
}
