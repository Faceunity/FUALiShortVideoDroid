package com.aliyun.race.sample.application;

import android.app.Application;

import com.aliyun.race.sample.utils.LeakCanaryUtils;

public class MutiApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        LeakCanaryUtils.initLeakCanary(this);
    }
}
