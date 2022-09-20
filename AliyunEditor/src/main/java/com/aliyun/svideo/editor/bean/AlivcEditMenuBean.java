package com.aliyun.svideo.editor.bean;

import androidx.annotation.Nullable;

public class AlivcEditMenuBean {
   @Nullable
   public String menuName;
    public int resourceId;
    public AlivcEditMenus menuType;

    public AlivcEditMenuBean(String menuName, int resourceId, AlivcEditMenus menuType) {
        this.menuName = menuName;
        this.resourceId = resourceId;
        this.menuType = menuType;
    }
}
