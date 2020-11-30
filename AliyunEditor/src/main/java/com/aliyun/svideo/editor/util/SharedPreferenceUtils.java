package com.aliyun.svideo.editor.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author zsy_18 data:2018/9/6
 */
public class SharedPreferenceUtils {
    private static final String SHAREDPRE_FILE = "svideo";
    private static final String EFFECT_TIME = "effect_time";
    private static final String EFFECT_ANIMATION = "effect_animation";
    private static final String EFFECT_COVER = "effect_cover";

    /**
     * 是否首次展示封面选择页面
     * @param context
     * @return
     */
    public static boolean isCoverViewFirstShow(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        return sharedPreferences.getBoolean(EFFECT_COVER, true);
    }
    public static boolean isTimeEffectFirstShow(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        return sharedPreferences.getBoolean(EFFECT_TIME, true);
    }
    public static boolean isAnimationEffectFirstShow(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        return sharedPreferences.getBoolean(EFFECT_ANIMATION, true);
    }
    public static void setTimeEffectFirstShow(Context context, boolean data) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putBoolean(EFFECT_TIME, data);
        editor.commit();
    }
    public static void setAnimationEffectFirstShow(Context context, boolean data) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putBoolean(EFFECT_ANIMATION, data);
        editor.commit();
    }
    public static void setCoverViewFirstShow(Context context, boolean data) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putBoolean(EFFECT_COVER, data);
        editor.commit();
    }
}
