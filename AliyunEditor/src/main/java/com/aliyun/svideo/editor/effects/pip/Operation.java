package com.aliyun.svideo.editor.effects.pip;

import com.aliyun.Visible;

@Visible
public enum Operation {
    ADD("增加", true),
    SCALE("缩放", false),
    ANGLE("旋转", false),
    MOVE("移动", false),
    RADIUS("圆角", false),
    BORDER("边框", false),
    ALPHA("透明度", false),
    BRIGHTNESS("亮度", false),
    CONTRAST("对比度", false),
    SATURATION("饱和度", false),
    VIGNETTE("暗角", false),
    SHARPNESS("锐度", false),
    VOLUME("音量", false),
    DENOISE("降噪", false),
    EFFECT("音效", true),
    FRAME_ANIMATION("帧动画", true),
    DELETE("删除", true);

    public String name;
    public boolean allwaysSelected;

    Operation(String name, boolean allwaysSelected) {
        this.name = name;
        this.allwaysSelected = allwaysSelected;
    }

}
