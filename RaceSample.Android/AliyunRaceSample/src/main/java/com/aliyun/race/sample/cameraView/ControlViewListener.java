package com.aliyun.race.sample.cameraView;

import android.support.v4.app.FragmentManager;

import com.aliyun.race.sample.bean.RaceMode;

/**
 * @author zsy_18 data:2018/7/31
 */
public interface ControlViewListener {
    /**
     * 显示美颜选择view
     */
    void onBeautyFaceClick(FragmentManager manager);
    /**
     * 摄像头转换事件
     */
    void onCameraSwitch();
    /**
     * 美颜开关
     */
    void onOpenRace(boolean open);
    /**
     * 人脸点位开关
     */
    void onOpenFacePoint(boolean open);

    void switchRaceMode(RaceMode mode);
}