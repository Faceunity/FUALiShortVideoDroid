package com.aliyun.demo.recorder.view.dialog;

public interface IPageTab {
    /**
     *获取Tab名称
     * @return tab名称
     */
    String getTabTitle();

    /**
     *获取Tab图标
     * @return tab icon, type: resource
     */
    int getTabIcon();
}
