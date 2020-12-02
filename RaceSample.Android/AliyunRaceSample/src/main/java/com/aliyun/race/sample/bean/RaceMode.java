package com.aliyun.race.sample.bean;

/**
 * 美颜模式
 */
public enum RaceMode {
    /**
     * 混合
     */
    MIX(0),

    /**
     * buffer
     */
    BUFFER(1),

    /**
     * texture
     */
    TEXTURE(2);



    private int value;

    RaceMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
