package com.aliyun.svideo.base.widget.beauty;

import android.annotation.SuppressLint;

import com.aliyun.svideo.base.widget.beauty.sharp.BeautyShapeParams;

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
        beautyParams0.beautyCutFace = 9;
        beautyParams0.beautyThinFace = 5;
        beautyParams0.beautyLongFace = 4;
        beautyParams0.beautyLowerJaw = 17;
        beautyParams0.beautyBigEye = 4;
        beautyParams0.beautyThinNose = 0;
        beautyParams0.beautyMouthWidth = 20;
        beautyParams0.beautyThinMandible = 0;
        beautyParams0.beautyCutCheek = 0;

        /**
         * 优雅
         */
        final BeautyShapeParams beautyParams1 = new BeautyShapeParams();
        beautyParams1.beautyCutFace = 33;
        beautyParams1.beautyThinFace = 22;
        beautyParams1.beautyLongFace = 17;
        beautyParams1.beautyLowerJaw = 7;
        beautyParams1.beautyBigEye = 33;
        beautyParams1.beautyThinNose = 0;
        beautyParams1.beautyMouthWidth = 18;
        beautyParams1.beautyThinMandible = 0;
        beautyParams1.beautyCutCheek = 0;


        /**
         * 精致
         */
        final BeautyShapeParams beautyParams2 = new BeautyShapeParams();
        beautyParams2.beautyCutFace = 6;
        beautyParams2.beautyThinFace = 22;
        beautyParams2.beautyLongFace = 10;
        beautyParams2.beautyLowerJaw = 33;
        beautyParams2.beautyBigEye = 0;
        beautyParams2.beautyThinNose = 0;
        beautyParams2.beautyMouthWidth = 0;
        beautyParams2.beautyThinMandible = 0;
        beautyParams2.beautyCutCheek = 0;


        /**
         * 网红
         */
        final BeautyShapeParams beautyParams3 = new BeautyShapeParams();
        beautyParams3.beautyCutFace = 33;
        beautyParams3.beautyThinFace = 5;
        beautyParams3.beautyLongFace = 2;
        beautyParams3.beautyLowerJaw = 2;
        beautyParams3.beautyBigEye = 16;
        beautyParams3.beautyThinNose = 0;
        beautyParams3.beautyMouthWidth = 12;
        beautyParams3.beautyThinMandible = 0;
        beautyParams3.beautyCutCheek = 0;


        /**
         * 可爱
         */

        final BeautyShapeParams beautyParams4 = new BeautyShapeParams();
        beautyParams4.beautyCutFace = 17;
        beautyParams4.beautyThinFace = 22;
        beautyParams4.beautyLongFace = 16;
        beautyParams4.beautyLowerJaw = -3;
        beautyParams4.beautyBigEye = 33;
        beautyParams4.beautyThinNose = 0;
        beautyParams4.beautyMouthWidth = -8;
        beautyParams4.beautyThinMandible = 0;
        beautyParams4.beautyCutCheek = 0;


        /**
         * 婴儿
         */
        final BeautyShapeParams beautyParams5 = new BeautyShapeParams();
        beautyParams5.beautyCutFace = 15;
        beautyParams5.beautyThinFace = 6;
        beautyParams5.beautyLongFace = 27;
        beautyParams5.beautyLowerJaw = -10;
        beautyParams5.beautyBigEye = 16;
        beautyParams5.beautyThinNose = 0;
        beautyParams5.beautyMouthWidth = -8;
        beautyParams5.beautyThinMandible = 0;
        beautyParams5.beautyCutCheek = 0;



        BEAUTY_MAP.put(0, beautyParams0);
        BEAUTY_MAP.put(1, beautyParams1);
        BEAUTY_MAP.put(2, beautyParams2);
        BEAUTY_MAP.put(3, beautyParams3);
        BEAUTY_MAP.put(4, beautyParams4);
        BEAUTY_MAP.put(5, beautyParams5);

    }
}
