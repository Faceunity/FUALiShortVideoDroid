/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.alivcsolution;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.aliyun.common.httpfinal.QupaiHttpFinal;
import com.aliyun.downloader.DownloaderManager;
import com.aliyun.sys.AlivcSdkCore;
import com.aliyun.video.common.aliha.AliHaUtils;

/**
 * Created by Mulberry on 2018/2/24.
 */
public class MutiApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        QupaiHttpFinal.getInstance().initOkHttpFinal();
        //Logger.setDebug(true);
        initDownLoader();

        //初始化阿⾥云移动高可⽤SDK接⼊——崩溃分析
        AliHaUtils.initHa(this, null);
        //        localCrashHandler();
        //        new NativeCrashHandler().registerForNativeCrash(this);
        AlivcSdkCore.register(getApplicationContext());
        AlivcSdkCore.setLogLevel(AlivcSdkCore.AlivcLogLevel.AlivcLogDebug);
    }

    private void initDownLoader() {
        DownloaderManager.getInstance().init(this);
    }

}
