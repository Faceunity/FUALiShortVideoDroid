/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.alivcsolution;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.alibaba.ha.adapter.AliHaAdapter;
import com.alibaba.ha.adapter.AliHaConfig;
import com.alibaba.ha.adapter.Sampling;
import com.aliyun.common.crash.CrashHandler;
import com.aliyun.demo.recorder.faceunity.FaceUnityManager;
import com.aliyun.downloader.DownloaderManager;
import com.squareup.leakcanary.AndroidExcludedRefs;
import com.squareup.leakcanary.DisplayLeakService;
import com.squareup.leakcanary.ExcludedRefs;
import com.squareup.leakcanary.LeakCanary;

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

        FaceUnityManager.getInstance(this).setUp(this);
        com.aliyun.vod.common.httpfinal.QupaiHttpFinal.getInstance().initOkHttpFinal();
        //Logger.setDebug(true);
        initDownLoader();
        initLeakCanary();
        //initHa();

        //        localCrashHandler();
        //        new NativeCrashHandler().registerForNativeCrash(this);

    }

    private void initDownLoader() {
        DownloaderManager.getInstance().init(this);
    }

    private void localCrashHandler() {
        CrashHandler catchHandler = CrashHandler.getInstance();
        catchHandler.init(getApplicationContext());
    }

    /**
     * 阿里云移动高可用SDK----奔溃分析初始化
     * 在接入阿里云移动高可用SDK之前，请明确:
     * 您的App尚未接入阿里云其他移动服务
     * 您已在阿里云移动研发平台(EMAS)上建立相关产品，并获得对应的appId和appSecret
     */
    private void initHa() {
        //这里必须启动，否则服务端收不到数据
        AliHaAdapter.getInstance().openPublishEmasHa();
        //指定启动Activity
        AliHaAdapter.getInstance().telescopeService.setBootPath(new String[]{"MainActivity"}, System.currentTimeMillis());
        //ha init
        AliHaConfig config = new AliHaConfig();
        config.appKey = "25045303"; //appKey
        try {
            config.appVersion = getApplicationContext().getPackageManager().getPackageInfo(this.getPackageName(),0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        config.channel = null;
        config.userNick = null;
        config.application = this;
        config.context = getApplicationContext();
        config.isAliyunos = false; //是否yunos
        AliHaAdapter.getInstance().start(config);
        AliHaAdapter.getInstance().utAppMonitor.changeSampling(Sampling.All); //指定数据上报
        Log.e("ha", "init");
    }

    private void initLeakCanary() {
        //排除一些Android Sdk引起的泄漏
        ExcludedRefs excludedRefs = AndroidExcludedRefs.createAppDefaults()
            .instanceField("android.view.inputmethod.InputMethodManager", "sInstance")
            .instanceField("android.view.inputmethod.InputMethodManager", "mLastSrvView")
            .instanceField("com.android.internal.policy.PhoneWindow$DecorView", "mContext")
            .instanceField("android.support.v7.widget.SearchView$SearchAutoComplete", "mContext")
            .instanceField("android.app.ActivityThread$ActivityClientRecord", "activity")
            .instanceField("android.media.MediaScannerConnection", "mContext")
            .build();

        LeakCanary.refWatcher(this)
            .listenerServiceClass(DisplayLeakService.class)
            .excludedRefs(excludedRefs)
            .buildAndInstall();
    }
}
