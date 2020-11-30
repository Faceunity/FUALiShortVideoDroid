/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.alivcsolution;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.aliyun.common.httpfinal.QupaiHttpFinal;
import com.aliyun.svideo.base.http.EffectService;
import com.aliyun.svideo.downloader.DownloaderManager;
import com.aliyun.sys.AlivcSdkCore;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

/**
 * Created by Mulberry on 2018/2/24.
 */
public class MutiApplication extends Application {
    /**
     * 友盟数据统计key值
     */
    private static final String UM_APP_KEY = "5c6e4e6cb465f5ff4700120e";
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

//        initFaceUnity();
        QupaiHttpFinal.getInstance().initOkHttpFinal();
        initDownLoader();
        /**
         * 注意: 即使您已经在AndroidManifest.xml中配置过appkey和channel值，也需要在App代码中调
         * 用初始化接口（如需要使用AndroidManifest.xml中配置好的appkey和channel值，
         * UMConfigure.init调用中appkey和channel参数请置为null）。
         */
        UMConfigure.init(this, UM_APP_KEY, "Aliyun", UMConfigure.DEVICE_TYPE_PHONE, null);
        // 选用AUTO页面采集模式
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
        AlivcSdkCore.register(getApplicationContext());
        AlivcSdkCore.setLogLevel(AlivcSdkCore.AlivcLogLevel.AlivcLogWarn);
        AlivcSdkCore.setDebugLoggerLevel(AlivcSdkCore.AlivcDebugLoggerLevel.AlivcDLClose);

        
        
        EffectService.setAppInfo(getResources().getString(R.string.app_name), getPackageName(), BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
    }

    private void initFaceUnity() {
        //以下为faceunity高级美颜接入代码，如果未集成faceunity，可以把此回调方法注释掉。以避免产生额外的license校验请求，影响您的产品性能。
        //如果购买使用faceUnity将判断条件删除
        if ("com.aliyun.apsaravideo".equals(getPackageName())) {
//            FaceUnityManager.getInstance().setUp(this);
        }
    }

    private void initDownLoader() {
        DownloaderManager.getInstance().init(this);
    }

}
