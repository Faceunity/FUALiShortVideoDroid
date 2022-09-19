package com.aliyun.svideo.editor.contant;

import java.util.HashMap;
import java.util.Map;

public class EditorConstants {
    public static final String EFFECT_FILTER_LOCAL_LUT_ADD = "effect_filter_local_lut_add";
    public static final String EFFECT_FILTER_LOCAL_LUT_CLEAR = "effect_filter_local_lut_clear";
    public static final int EFFECT_FILTER_LOCAL_LUT_REQUEST_CODE = 300;
    public static final String LUT_FILE_DIR = "lut_effect";
    public static final Map<String,String> LUT_FILE_SEQ_TO_NAME = new HashMap<String,String>(){
        {
            put("1","复古");
            put("2","经典");
            put("3","优雅");
        }
    };
}
