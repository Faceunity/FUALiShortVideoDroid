package com.aliyun.video.common.aliha;

import android.app.Application;
import android.content.pm.PackageManager;
import android.util.Log;

import com.alibaba.ha.adapter.AliHaAdapter;
import com.alibaba.ha.adapter.AliHaConfig;
import com.alibaba.ha.adapter.Sampling;
import com.taobao.onlinemonitor.OnLineMonitorApp;

/**
 * @author cross_ly
 * @date 2018/11/06
 * <p>描述:
 * 阿⾥云移动高可⽤SDK接⼊——崩溃分析
 *
 *  在接⼊入阿⾥云移动⾼可用SDK之前，请明确:
 *      您的App尚未接入阿里云其他移动服务
 *      您已在阿里云移动研发平台(EMAS)上建⽴相关产品，并获得对应的appId和appSecret
 */
public class AliHaUtils {

    //"com.aliyun.apsaravideo"集成的数据统计key
    private static final String APPKEY_APSARAVIDEO = "25045303";

    //"com.aliyun.apsara.svideo"集成的数据统计key
    public static final String APPKEY_APSARA_SVIDEO = "25278229";

    private static String sAppKey = APPKEY_APSARAVIDEO;

    /**
     * 在application中初始化
     * @param application Application
     * @param channel 渠道，需要先到控制台创建
     */
    public static void initHa(Application application,String channel){
        //这里必须启动，否则服务端收不到数据
        AliHaAdapter.getInstance().openPublishEmasHa();
        //指定启动Activity
        AliHaAdapter.getInstance().telescopeService.setBootPath(new String[]{"MainActivity"}, System.currentTimeMillis());
        //ha init
        AliHaConfig config = new AliHaConfig();
        config.appKey = sAppKey; //appKey
        try {
            config.appVersion = application.getApplicationContext().getPackageManager().getPackageInfo(application.getPackageName(),0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        config.channel = channel;
        config.userNick = null;
        config.application = application;
        config.context = application.getApplicationContext();
        config.isAliyunos = false; //是否yunos
        AliHaAdapter.getInstance().start(config);
        AliHaAdapter.getInstance().utAppMonitor.changeSampling(Sampling.All); //指定数据上报
        OnLineMonitorApp.sIsDebug = false; //OnLineMonitor模式，线上版本必须为false
        Log.e("ha", "init");
    }

    /**
     * 在application中初始化
     * @param application Application
     * @param channel 渠道，需要先到控制台创建
     * @param appkey 不同appPackageId数据分析绑定的key
     */
    public static void initHa(Application application,String channel,String appkey){
        sAppKey = appkey;
        initHa(application,channel);
    }
}
