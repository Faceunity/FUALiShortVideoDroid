package com.aliyun.svideo.base.widget.beauty;

import android.annotation.SuppressLint;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Akira on 2018/5/30.
 */

public class BeautyRaceConstants {
    /**
     * ******************************************
     * BUFFING:     磨皮
     * WHITENING:   美白
     * SHARP:       锐化
     * ******************************************
     */

    public final static int BUFFING = 0;
    public final static int WHITENING = 1;
    public final static int SHARP = 2;

    /** 美白 */
    public static final String KEY_WHITE = "white";
    /** 磨皮 */
    public static final String KEY_BUFFING = "buffing";
    /** 锐化 */
    public static final String KEY_SHARP = "sharp";


    @SuppressLint("UseSparseArrays")
    public final static Map<Integer, BeautyParams> BEAUTY_MAP = new HashMap<>();

    static {
        final BeautyParams beautyParams0 = new BeautyParams();
        beautyParams0.beautyBuffing = 0;
        beautyParams0.beautyWhite = 0;
        beautyParams0.beautyRuddy = 0;

        // 1级: 磨皮 = 12, 美白 = 20, 锐化 = 20
        final BeautyParams beautyParams1 = new BeautyParams();
        beautyParams1.beautyBuffing = 12;
        beautyParams1.beautyWhite = 20;
        beautyParams1.beautyRuddy = 20;

        // 2级: 磨皮 = 24, 美白 = 40, 锐化 = 40
        final BeautyParams beautyParams2 = new BeautyParams();
        beautyParams2.beautyBuffing = 24;
        beautyParams2.beautyWhite = 40;
        beautyParams2.beautyRuddy = 40;

        // 3级: 磨皮 = 36, 美白 = 60, 锐化 = 60
        final BeautyParams beautyParams3 = new BeautyParams();
        beautyParams3.beautyBuffing = 36;
        beautyParams3.beautyWhite = 60;
        beautyParams3.beautyRuddy = 60;

        // 4级: 磨皮 = 48, 美白 = 80, 锐化 = 80
        final BeautyParams beautyParams4 = new BeautyParams();
        beautyParams4.beautyBuffing = 48;
        beautyParams4.beautyWhite = 80;
        beautyParams4.beautyRuddy = 80;

        // 5级: 磨皮 = 60, 美白 = 100, 锐化 = 100
        final BeautyParams beautyParams5 = new BeautyParams();
        beautyParams5.beautyBuffing = 60;
        beautyParams5.beautyWhite = 100;
        beautyParams5.beautyRuddy = 100;

        BEAUTY_MAP.put(0, beautyParams0);
        BEAUTY_MAP.put(1, beautyParams1);
        BEAUTY_MAP.put(2, beautyParams2);
        BEAUTY_MAP.put(3, beautyParams3);
        BEAUTY_MAP.put(4, beautyParams4);
        BEAUTY_MAP.put(5, beautyParams5);

    }
}
