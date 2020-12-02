package com.aliyun.race.sample.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.aliyun.race.sample.bean.BeautyMode;


public class SharedPreferenceUtils {

    private static final String SHAREDPRE_FILE = "race_sample";
    private static final String BEAUTY_FACE_LEVEL = "race_sample_beauty_face_level";
    private static final String BEAUTY_FACE_NORMAL_LEVEL = "race_sample_beauty_face_normal_level";
    private static final String BEAUTY_SHARP_LEVEL = "race_sample_beauty_sharp_level";
    private static final String RACE_BEAUTY_PARAMS = "race_sample_beauty_params";
    private static final String BEAUTY_SHAPE_PARAMS = "race_sample_beauty_shape_params";
    private static final String BEAUTY_FINE_TUNING_TIPS = "race_sample_beauty_fine_tuning_tips";


    private static final String BEAUTY_FACE_MODE = "race_sample_beauty_face_mode";


    public static void setRaceBeautyParams(Context context, String data) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putString(RACE_BEAUTY_PARAMS, data);
        editor.commit();
    }

    public static String getRaceBeautyParams(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        String data = sharedPreferences.getString(RACE_BEAUTY_PARAMS, "");
        return data;
    }

    public static void setBeautyShapeParams(Context context, String data) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putString(BEAUTY_SHAPE_PARAMS, data);
        editor.commit();
    }

    public static String getBeautyShapeParams(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        String data = sharedPreferences.getString(BEAUTY_SHAPE_PARAMS, "");
        return data;
    }



    public static void setBeautyFineTuningTips(Context context, boolean show) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putBoolean(BEAUTY_FINE_TUNING_TIPS, show);
        editor.commit();
    }

    public static boolean getBeautyFineTuningTips(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        boolean autoFocus = sharedPreferences.getBoolean(BEAUTY_FINE_TUNING_TIPS, true);
        return autoFocus;
    }

    public static int getBeautyFaceLevel(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        return sharedPreferences.getInt(BEAUTY_FACE_LEVEL, 3);
    }


    public static int getBeautyShapeLevel(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        return sharedPreferences.getInt(BEAUTY_SHARP_LEVEL, 0);
    }

    public static void setBeautyFaceLevel(Context context, int level) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.remove(BEAUTY_FACE_LEVEL);
        editor.putInt(BEAUTY_FACE_LEVEL, level);
        editor.apply();
    }

    public static void setBeautyShapeLevel(Context context, int level) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.remove(BEAUTY_SHARP_LEVEL);
        editor.putInt(BEAUTY_SHARP_LEVEL, level);
        editor.apply();
    }


    public static void setBeautyNormalFaceLevel(Context context, int level) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.remove(BEAUTY_FACE_NORMAL_LEVEL);
        editor.putInt(BEAUTY_FACE_NORMAL_LEVEL, level);
        editor.apply();
    }

    public static int getBeautyNormalFaceLevel(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        return sharedPreferences.getInt(BEAUTY_FACE_NORMAL_LEVEL, 3);
    }

    public static void setBeautyMode(Context context, BeautyMode beautyMode) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.remove(BEAUTY_FACE_MODE);
        editor.putInt(BEAUTY_FACE_MODE, beautyMode.getValue());
        editor.apply();
    }

    public static BeautyMode getBeautyMode(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                Activity.MODE_PRIVATE);
        return sharedPreferences.getInt(BEAUTY_FACE_MODE, 1) == 0 ? BeautyMode.Normal : BeautyMode.Advanced;
    }

}
