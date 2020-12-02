package com.aliyun.svideo.base.widget.beauty;

import android.annotation.SuppressLint;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Akira on 2018/5/30.
 */

public class BeautyConstants {
    /**
     * ******************************************
     * BUFFING:     磨皮
     * WHITENING:   美白
     * RUDDY:       红润
     * SHORTEN_JAW: 缩下巴
     * BIG_EYE:     大眼
     * THIN_FACE:   瘦脸
     * RED_LIPS:    腮红
     * ******************************************
     */

    public final static int BUFFING = 0;
    public final static int WHITENING = 1;
    public final static int RUDDY = 2;
    public final static int SHORTEN_JAW = 3;
    public final static int BIG_EYE = 4;
    public final static int THIN_FACE = 5;
    public final static int RED_LIPS = 6;

    /** 美白 */
    public static final String KEY_WHITE = "white";
    /** 磨皮 */
    public static final String KEY_BUFFING = "buffing";
    /** 红润 */
    public static final String KEY_RUDDY = "ruddy";
    /** 腮红 */
    public static final String KEY_CHEEKPINK = "checkpink";
    /** 瘦脸 */
    public static final String KEY_SLIMFACE = "slimface";
    /** 缩下巴 */
    public static final String KEY_SHORTENFACE = "shortenface";
    /** 大眼 */
    public static final String KEY_BIGEYE = "bigeye";

    @SuppressLint("UseSparseArrays")
    public final static Map<Integer, BeautyParams> BEAUTY_MAP = new HashMap<>();

    static {
        final BeautyParams beautyParams0 = new BeautyParams();
        beautyParams0.beautyBuffing = 0;
        beautyParams0.beautyWhite = 0;
        beautyParams0.beautyRuddy = 0;
        beautyParams0.beautyBigEye = 0;
        beautyParams0.beautySlimFace = 0;

        // 1级: 磨皮 = 12, 美白 = 20, 红润 = 20, 大眼 = 20, 瘦脸 = 20
        final BeautyParams beautyParams1 = new BeautyParams();
        beautyParams1.beautyBuffing = 12;
        beautyParams1.beautyWhite = 20;
        beautyParams1.beautyRuddy = 20;
        beautyParams1.beautyBigEye = 20;
        beautyParams1.beautySlimFace = 20;

        // 2级: 磨皮 = 24, 美白 = 40, 红润 = 40, 大眼 = 40, 瘦脸 = 40
        final BeautyParams beautyParams2 = new BeautyParams();
        beautyParams2.beautyBuffing = 24;
        beautyParams2.beautyWhite = 40;
        beautyParams2.beautyRuddy = 40;
        beautyParams2.beautyBigEye = 40;
        beautyParams2.beautySlimFace = 40;

        // 3级: 磨皮 = 36, 美白 = 60, 红润 = 60, 大眼 = 40, 瘦脸 = 40
        final BeautyParams beautyParams3 = new BeautyParams();
        beautyParams3.beautyBuffing = 36;
        beautyParams3.beautyWhite = 60;
        beautyParams3.beautyRuddy = 60;
        beautyParams3.beautyBigEye = 60;
        beautyParams3.beautySlimFace = 60;

        // 4级: 磨皮 = 48, 美白 = 80, 红润 = 80, 大眼 = 40, 瘦脸 = 40
        final BeautyParams beautyParams4 = new BeautyParams();
        beautyParams4.beautyBuffing = 48;
        beautyParams4.beautyWhite = 80;
        beautyParams4.beautyRuddy = 80;
        beautyParams4.beautyBigEye = 80;
        beautyParams4.beautySlimFace = 80;

        // 5级: 磨皮 = 60, 美白 = 100, 红润 = 100, 大眼 = 40, 瘦脸 = 40
        final BeautyParams beautyParams5 = new BeautyParams();
        beautyParams5.beautyBuffing = 60;
        beautyParams5.beautyWhite = 100;
        beautyParams5.beautyRuddy = 100;
        beautyParams5.beautyBigEye = 100;
        beautyParams5.beautySlimFace = 100;

        BEAUTY_MAP.put(0, beautyParams0);
        BEAUTY_MAP.put(1, beautyParams1);
        BEAUTY_MAP.put(2, beautyParams2);
        BEAUTY_MAP.put(3, beautyParams3);
        BEAUTY_MAP.put(4, beautyParams4);
        BEAUTY_MAP.put(5, beautyParams5);

    }
}
