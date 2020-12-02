package com.aliyun.svideo.base.widget.beauty.enums;

/**
 * 美肌和美颜级别 0 ~ 5
 */
public enum BeautyLevel {
    // level0 无效果
    BEAUTY_LEVEL_ZERO(0),
    // level1
    BEAUTY_LEVEL_ONE(20),
    // level2
    BEAUTY_LEVEL_TWO(40),
    // level3
    BEAUTY_LEVEL_THREE(60),
    // level4
    BEAUTY_LEVEL_FOUR(80),
    // level5
    BEAUTY_LEVEL_FIVE(100),
    // level6
    BEAUTY_LEVEL_CUSTOM(70);

    private int value;

    BeautyLevel(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

}
