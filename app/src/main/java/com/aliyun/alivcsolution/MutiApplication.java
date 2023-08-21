/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.alivcsolution;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.multidex.MultiDex;



import com.aliyun.common.httpfinal.QupaiHttpFinal;
import com.aliyun.svideo.base.http.EffectService;
import com.aliyun.svideo.downloader.DownloaderManager;
import com.aliyun.svideosdk.AlivcSdkCore;
import com.aliyun.svideosdk.AlivcSdkCore.AlivcDebugLoggerLevel;
import com.aliyun.svideosdk.AlivcSdkCore.AlivcLogLevel;
import com.faceunity.nama.FUConfig;
import com.faceunity.nama.utils.FuDeviceUtils;

import static com.aliyun.svideo.base.ui.SdkVersionActivity.DEBUG_DEVELOP_URL;
import static com.aliyun.svideo.base.ui.SdkVersionActivity.DEBUG_PARAMS;

/**
 * Created by Mulberry on 2018/2/24.
 */
public class MutiApplication extends Application {
    /**
     * 友盟数据统计key值
     */
    private static final String UM_APP_KEY = "5c6e4e6cb465f5ff4700120e";
    private String mLogPath;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FUConfig.DEVICE_LEVEL = FuDeviceUtils.judgeDeviceLevelGPU();
        QupaiHttpFinal.getInstance().initOkHttpFinal();
        initDownLoader();
        AlivcSdkCore.register(this);
        if (BuildConfig.isFinal) {
            AlivcSdkCore.setLogLevel(AlivcLogLevel.AlivcLogDebug);
            AlivcSdkCore.setDebugLoggerLevel(AlivcDebugLoggerLevel.AlivcDLAll);
        } else {
            AlivcSdkCore.setLogLevel(AlivcLogLevel.AlivcLogDebug);
            AlivcSdkCore.setDebugLoggerLevel(AlivcDebugLoggerLevel.AlivcDLAll);
        }
        setSdkDebugParams();
        if (TextUtils.isEmpty(mLogPath)) {
            //保证每次运行app生成一个新的日志文件
            mLogPath = getExternalFilesDir("Log").getAbsolutePath() + "/ShortVideo";
            AlivcSdkCore.setLogPath(mLogPath);
        }
        
        
        EffectService.setAppInfo(getResources().getString(R.string.ugc_app_name), getPackageName(), BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
    }


    private void initDownLoader() {
        DownloaderManager.getInstance().init(this);
    }

    private void setSdkDebugParams() {
        //Demo 调试用，外部客户请勿使用
        SharedPreferences mySharedPreferences = this.getSharedPreferences(DEBUG_PARAMS, Activity.MODE_PRIVATE);
        int hostType = mySharedPreferences.getInt(DEBUG_DEVELOP_URL, 0);
        //AlivcSdkCore.setDebugHostType(hostType);
    }

}
