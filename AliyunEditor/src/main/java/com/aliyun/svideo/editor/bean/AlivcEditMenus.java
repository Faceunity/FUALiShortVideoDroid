package com.aliyun.svideo.editor.bean;

import com.aliyun.svideo.editor.effects.control.UIEditorPage;

public enum AlivcEditMenus {
    BACK,
    AddText;


    public static AlivcEditMenus get(int index) {
        return values()[index];
    }

    public int index() {
        return ordinal();
    }
}
