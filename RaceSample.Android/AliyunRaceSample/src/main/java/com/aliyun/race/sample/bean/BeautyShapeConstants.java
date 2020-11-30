package com.aliyun.race.sample.bean;

import android.annotation.SuppressLint;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Akira on 2018/5/30.
 */

public class BeautyShapeConstants {
    /**
     * ******************************************
     * CUT_FACE:     窄脸
     * THIN_FACE:   瘦脸
     * LONG_FACE:    脸长
     * LOWER_JAW:    缩下巴
     * BIG_EYE:     大眼
     * THIN_NOSE:   瘦鼻
     * MOUTH_WIDTH:    唇宽
     * THIN_MANDIBLE:   下颌
     * CUT_CHEEK:    颧骨
     * ******************************************
     */




    public final static int CUT_FACE = 0;
    public final static int THIN_FACE = 1;
    public final static int LONG_FACE = 2;
    public final static int LOWER_JAW = 3;
    public final static int BIG_EYE = 4;
    public final static int THIN_NOSE = 5;
    public final static int MOUTH_WIDTH = 6;
    public final static int THIN_MANDIBLE = 7;
    public final static int CUT_CHEEK = 8;



    @SuppressLint("UseSparseArrays")
    public final static Map<Integer, BeautyShapeParams> BEAUTY_MAP = new HashMap<>();
    static {
        final BeautyShapeParams beautyParams0 = new BeautyShapeParams();
        /**
         * 自定义
         */
        beautyParams0.mBeautyCutFace = 9;
        beautyParams0.mBeautyThinFace = 5;
        beautyParams0.mBeautyLongFace = 4;
        beautyParams0.mBeautyLowerJaw = 17;
        beautyParams0.mBeautyBigEye = 4;
        beautyParams0.mBeautyThinNose = 0;
        beautyParams0.mBeautyMouthWidth = 20;
        beautyParams0.mBeautyThinMandible = 0;
        beautyParams0.mBeautyCutCheek = 0;

            /**
             * 优雅
             */
        final BeautyShapeParams beautyParams1 = new BeautyShapeParams();
        beautyParams1.mBeautyCutFace = 33;
        beautyParams1.mBeautyThinFace = 22;
        beautyParams1.mBeautyLongFace = 17;
        beautyParams1.mBeautyLowerJaw = 7;
        beautyParams1.mBeautyBigEye = 33;
        beautyParams1.mBeautyThinNose = 0;
        beautyParams1.mBeautyMouthWidth = 18;
        beautyParams1.mBeautyThinMandible = 0;
        beautyParams1.mBeautyCutCheek = 0;


        /**
         * 精致
         */
        final BeautyShapeParams beautyParams2 = new BeautyShapeParams();
        beautyParams2.mBeautyCutFace = 6;
        beautyParams2.mBeautyThinFace = 22;
        beautyParams2.mBeautyLongFace = 10;
        beautyParams2.mBeautyLowerJaw = 33;
        beautyParams2.mBeautyBigEye = 0;
        beautyParams2.mBeautyThinNose = 0;
        beautyParams2.mBeautyMouthWidth = 0;
        beautyParams2.mBeautyThinMandible = 0;
        beautyParams2.mBeautyCutCheek = 0;


        /**
         * 网红
         */
        final BeautyShapeParams beautyParams3 = new BeautyShapeParams();
        beautyParams3.mBeautyCutFace = 33;
        beautyParams3.mBeautyThinFace = 5;
        beautyParams3.mBeautyLongFace = 2;
        beautyParams3.mBeautyLowerJaw = 2;
        beautyParams3.mBeautyBigEye = 16;
        beautyParams3.mBeautyThinNose = 0;
        beautyParams3.mBeautyMouthWidth = 12;
        beautyParams3.mBeautyThinMandible = 0;
        beautyParams3.mBeautyCutCheek = 0;


        /**
         * 可爱
         */

        final BeautyShapeParams beautyParams4 = new BeautyShapeParams();
        beautyParams4.mBeautyCutFace = 17;
        beautyParams4.mBeautyThinFace = 22;
        beautyParams4.mBeautyLongFace = 16;
        beautyParams4.mBeautyLowerJaw = -3;
        beautyParams4.mBeautyBigEye = 33;
        beautyParams4.mBeautyThinNose = 0;
        beautyParams4.mBeautyMouthWidth = -8;
        beautyParams4.mBeautyThinMandible = 0;
        beautyParams4.mBeautyCutCheek = 0;


        /**
         * 婴儿
         */
        final BeautyShapeParams beautyParams5 = new BeautyShapeParams();
        beautyParams5.mBeautyCutFace = 15;
        beautyParams5.mBeautyThinFace = 6;
        beautyParams5.mBeautyLongFace = 27;
        beautyParams5.mBeautyLowerJaw = -10;
        beautyParams5.mBeautyBigEye = 16;
        beautyParams5.mBeautyThinNose = 0;
        beautyParams5.mBeautyMouthWidth = -8;
        beautyParams5.mBeautyThinMandible = 0;
        beautyParams5.mBeautyCutCheek = 0;



        BEAUTY_MAP.put(0, beautyParams0);
        BEAUTY_MAP.put(1, beautyParams1);
        BEAUTY_MAP.put(2, beautyParams2);
        BEAUTY_MAP.put(3, beautyParams3);
        BEAUTY_MAP.put(4, beautyParams4);
        BEAUTY_MAP.put(5, beautyParams5);

    }
}
