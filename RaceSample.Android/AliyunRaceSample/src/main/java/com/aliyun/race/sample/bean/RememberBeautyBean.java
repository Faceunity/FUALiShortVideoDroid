package com.aliyun.race.sample.bean;

import java.util.List;

/**
 * 记录美颜参数json的bean
 */
public class RememberBeautyBean {
    private List<BeautyParams> mBeautyList;

    public List<BeautyParams> getBeautyList() {
        return mBeautyList;
    }

    public void setBeautyList(List<BeautyParams> beautyList) {
        this.mBeautyList = beautyList;
    }
}
