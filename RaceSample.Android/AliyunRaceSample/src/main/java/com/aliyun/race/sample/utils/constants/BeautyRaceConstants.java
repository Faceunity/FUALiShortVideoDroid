package com.aliyun.race.sample.utils.constants;

import android.annotation.SuppressLint;

import com.aliyun.race.sample.bean.BeautyParams;

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
        beautyParams0.mBeautyBuffing = 0;
        beautyParams0.mBeautyWhite = 0;
        beautyParams0.mBeautyRuddy = 0;

        // 1级: 磨皮 = 12, 美白 = 20, 锐化 = 20
        final BeautyParams beautyParams1 = new BeautyParams();
        beautyParams1.mBeautyBuffing = 12;
        beautyParams1.mBeautyWhite = 20;
        beautyParams1.mBeautyRuddy = 20;

        // 2级: 磨皮 = 24, 美白 = 40, 锐化 = 40
        final BeautyParams beautyParams2 = new BeautyParams();
        beautyParams2.mBeautyBuffing = 24;
        beautyParams2.mBeautyWhite = 40;
        beautyParams2.mBeautyRuddy = 40;

        // 3级: 磨皮 = 36, 美白 = 60, 锐化 = 60
        final BeautyParams beautyParams3 = new BeautyParams();
        beautyParams3.mBeautyBuffing = 36;
        beautyParams3.mBeautyWhite = 60;
        beautyParams3.mBeautyRuddy = 60;

        // 4级: 磨皮 = 48, 美白 = 80, 锐化 = 80
        final BeautyParams beautyParams4 = new BeautyParams();
        beautyParams4.mBeautyBuffing = 48;
        beautyParams4.mBeautyWhite = 80;
        beautyParams4.mBeautyRuddy = 80;

        // 5级: 磨皮 = 60, 美白 = 100, 锐化 = 100
        final BeautyParams beautyParams5 = new BeautyParams();
        beautyParams5.mBeautyBuffing = 60;
        beautyParams5.mBeautyWhite = 100;
        beautyParams5.mBeautyRuddy = 100;

        BEAUTY_MAP.put(0, beautyParams0);
        BEAUTY_MAP.put(1, beautyParams1);
        BEAUTY_MAP.put(2, beautyParams2);
        BEAUTY_MAP.put(3, beautyParams3);
        BEAUTY_MAP.put(4, beautyParams4);
        BEAUTY_MAP.put(5, beautyParams5);

    }
}
