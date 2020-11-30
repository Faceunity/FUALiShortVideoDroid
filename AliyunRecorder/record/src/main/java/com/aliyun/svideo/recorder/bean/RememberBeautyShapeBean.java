package com.aliyun.svideo.recorder.bean;

import com.aliyun.svideo.base.widget.beauty.BeautyParams;
import com.aliyun.svideo.base.widget.beauty.sharp.BeautyShapeParams;

import java.util.List;

/**
 * 记录美型参数json的bean
 */
public class RememberBeautyShapeBean {
    private List<BeautyShapeParams> beautyList;

    public List<BeautyShapeParams> getBeautyList() {
        return beautyList;
    }

    public void setBeautyList(List<BeautyShapeParams> beautyList) {
        this.beautyList = beautyList;
    }
}
