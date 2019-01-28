package com.aliyun.demo.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author zsy_18 data:2018/9/6
 */
public class SharedPreferenceUtils {
    private static final String SHAREDPRE_FILE = "svideo";
    private static final String EFFECT_TIME = "effect_time";
    private static final String EFFECT_Animation = "effect_animation";
    public static boolean isTimeEffectFirstShow(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
            Activity.MODE_PRIVATE);
        return sharedPreferences.getBoolean(EFFECT_TIME, true);
    }
    public static boolean isAnimationEffectFirstShow(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
            Activity.MODE_PRIVATE);
        return sharedPreferences.getBoolean(EFFECT_Animation, true);
    }
    public static void setTimeEffectFirstShow(Context context,boolean data){
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putBoolean(EFFECT_TIME, data);
        editor.commit();
    }
    public static void setAnimationEffectFirstShow(Context context,boolean data){
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putBoolean(EFFECT_Animation, data);
        editor.commit();
    }
}
