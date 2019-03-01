/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.alivcsolution;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;


import com.aliyun.alivcsolution.utils.ThreadHelper;
import com.aliyun.common.httpfinal.QupaiHttpFinal;
import com.aliyun.demo.recorder.faceunity.FaceUnityManager;
import com.aliyun.downloader.DownloaderManager;
import com.aliyun.sys.AlivcSdkCore;
import com.aliyun.video.common.aliha.AliHaUtils;
import com.faceunity.greendao.GreenDaoUtils;
import com.faceunity.utils.FileUtils;

/**
 * Created by Mulberry on 2018/2/24.
 */
public class MutiApplication extends Application {
    private static Context sContext;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        FaceUnityManager.getInstance(this).setUp(this);
        QupaiHttpFinal.getInstance().initOkHttpFinal();
        //Logger.setDebug(true);
        initDownLoader();

        //初始化阿⾥云移动高可⽤SDK接⼊——崩溃分析
        AliHaUtils.initHa(this, null);
        //        localCrashHandler();
        //        new NativeCrashHandler().registerForNativeCrash(this);
        AlivcSdkCore.register(getApplicationContext());
        AlivcSdkCore.setLogLevel(AlivcSdkCore.AlivcLogLevel.AlivcLogDebug);

        ThreadHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                // 拷贝 assets 资源
                FileUtils.copyAssetsMagicPhoto(sContext);
                FileUtils.copyAssetsTemplate(sContext);
                // 初始化数据库，一定在拷贝文件之后
                GreenDaoUtils.initGreenDao(sContext);
            }
        });
    }

    private void initDownLoader() {
        DownloaderManager.getInstance().init(this);
    }

}
