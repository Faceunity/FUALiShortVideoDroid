package com.aliyun.race.sample.view.listener;

import com.aliyun.race.sample.bean.BeautyMode;

/**
 * Created by Akira on 2018/5/30.
 */

public interface OnBeautyTableItemSeletedListener {
    /**
     * 美颜、美肌、美型tab选中监听
     * @param beautyMode 选中下标
     */
    void onNormalSelected(BeautyMode beautyMode);

}
