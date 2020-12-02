package com.aliyun.race.sample.bean;

import java.util.List;

/**
 * 记录美型参数json的bean
 */
public class RememberBeautyShapeBean {
    private List<BeautyShapeParams> mBeautyList;

    public List<BeautyShapeParams> getBeautyList() {
        return mBeautyList;
    }

    public void setBeautyList(List<BeautyShapeParams> beautyList) {
        this.mBeautyList = beautyList;
    }
}
