package com.aliyun.svideo.recorder.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.aliyun.svideo.base.widget.beauty.enums.BeautyMode;

public class SharedPreferenceUtils {

    private static final String SHAREDPRE_FILE = "svideo";
    private static final String WHITE = "white";
    private static final String BUFFING = "buffing";
    private static final String RUDDY = "ruddy";
    private static final String CHEEKPINK = "cheekpink";
    private static final String BRIGHTNESS = "brightness";
    private static final String SLIMFACE = "slimface";
    private static final String SHORTENFACE = "shortenface";
    private static final String BIGEYE = "bigeye";
    private static final String BEAUTY_LEVEL = "beauty_level";
    private static final String BEAUTY_FACE_LEVEL = "beauty_face_level";
    private static final String BEAUTY_FACE_NORMAL_LEVEL = "beauty_face_normal_level";
    private static final String BEAUTY_SKIN_LEVEL = "beauty_skin_level";
    private static final String BEAUTY_SHARP_LEVEL = "beauty_sharp_level";
    private static final String BEAUTY_PARAMS = "beauty_params";
    private static final String RACE_BEAUTY_PARAMS = "race_beauty_params";
    private static final String BEAUTY_SHAPE_PARAMS = "beauty_shape_params";
    private static final String AUTOFOCUS = "autofocus";
    private static final String PREVIEW_MIRROR = "previewmirror";
    private static final String PUSH_MIRROR = "pushmirror";
    private static final String TARGET_BIT = "target_bit";
    private static final String MIN_BIT = "min_bit";
    private static final String SHOWGUIDE = "guide";
    private static final String BEAUTYON = "beautyon";
    private static final String HINT_TARGET_BIT = "hint_target_bit";
    private static final String HINT_MIN_BIT = "hint_min_bit";
    private static final String BEAUTY_FINE_TUNING_TIPS = "beauty_fine_tuning_tips";

    private static final String ROLE_AUDIENCE_USER = "role_audience";
    private static final String ROLE_HOST_USER = "role_host";
    private static final String FORBID_USER = "forbid_user";
    private static final String USER_INFO = "user_info";

    private static final String NETCONFIG = "netConfig";
    private static final int DEFAULT_VALUE_INT_BEAUTY_BITEYE = 0;
    private static final int DEFAULT_VALUE_INT_BEAUTY_BRIGHTNESS = 0;
    private static final int DEFAULT_VALUE_INT_BEAUTY_BUFFING = 0;
    private static final int DEFAULT_VALUE_INT_BEAUTY_CHEEKPINK = 0;
    private static final int DEFAULT_VALUE_INT_BEAUTY_RUDDY = 0;
    private static final int DEFAULT_VALUE_INT_BEAUTY_SLIMFACE = 0;
    private static final int DEFAULT_VALUE_INT_BEAUTY_WHITE = 0;
    private static final boolean DEFAULT_VALUE_PREVIEW_MIRROR = false;
    private static final boolean DEFAULT_VALUE_AUTO_FOCUS = false;
    private static final boolean DEFAULT_VALUE_PUSH_MIRROR = false;
    private static final String BEAUTY_FACE_MODE = "beauty_face_mode";
    private static final String IS_RACE_MODE = "is_race_mode";

    public static void setNetconfig(Context context, int netConfig) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putInt(NETCONFIG, netConfig);
        editor.commit();
    }

    public static int getNetconfig(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        int config = sharedPreferences.getInt(NETCONFIG, 0);
        return config;
    }
    public static void setIsRaceMode(Context context, boolean isRaceMode) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putBoolean(IS_RACE_MODE, isRaceMode);
        editor.commit();
    }

    public static boolean getIsRaceMode(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        boolean isRaceMode = sharedPreferences.getBoolean(IS_RACE_MODE, false);
        return isRaceMode;
    }

    public static void setBeautyParams(Context context, String data) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putString(BEAUTY_PARAMS, data);
        editor.commit();
    }

    public static String getBeautyParams(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        String data = sharedPreferences.getString(BEAUTY_PARAMS, "");
        return data;
    }
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

    public static void setBeautyLevel(Context context, int level) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putInt(BEAUTY_LEVEL, level);
        editor.commit();
    }

    public static int getBeautyLevel(Context context) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        int white = sharedPreferences.getInt(BEAUTY_LEVEL, 3);
        return white;
    }

    public static void setWhiteValue(Context context, int white) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putInt(WHITE, white);
        editor.commit();
    }

    public static int getWhiteValue(Context context) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        int white = sharedPreferences.getInt(WHITE, DEFAULT_VALUE_INT_BEAUTY_WHITE);
        return white;
    }

    public static void setBuffing(Context context, int buffing) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putInt(BUFFING, buffing);
        editor.commit();
    }

    public static int getBuffing(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        int buffing = sharedPreferences.getInt(BUFFING, DEFAULT_VALUE_INT_BEAUTY_BUFFING);
        return buffing;
    }

    public static void setRuddy(Context context, int ruddy) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putInt(RUDDY, ruddy);
        editor.commit();
    }

    public static int getRuddy(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        int ruddy = sharedPreferences.getInt(RUDDY, DEFAULT_VALUE_INT_BEAUTY_RUDDY);
        return ruddy;
    }

    public static void setBrightness(Context context, int brightness) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putInt(BRIGHTNESS, brightness);
        editor.commit();
    }

    public static int getBrightness(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        int brightness = sharedPreferences.getInt(BRIGHTNESS, DEFAULT_VALUE_INT_BEAUTY_BRIGHTNESS);
        return brightness;
    }

    public static void setCheekPink(Context context, int cheekpink) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putInt(CHEEKPINK, cheekpink);
        editor.commit();
    }

    public static int getCheekpink(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        int cheekpink = sharedPreferences.getInt(CHEEKPINK, DEFAULT_VALUE_INT_BEAUTY_CHEEKPINK);
        return cheekpink;
    }

    public static void setSlimFace(Context context, int slimface) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putInt(SLIMFACE, slimface);
        editor.commit();
    }

    public static int getSlimFace(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        int slimface = sharedPreferences.getInt(SLIMFACE, DEFAULT_VALUE_INT_BEAUTY_SLIMFACE);
        return slimface;
    }

    public static void setShortenFace(Context context, int shortenface) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putInt(SHORTENFACE, shortenface);
        editor.commit();
    }

    public static int getShortenFace(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        int shortenface = sharedPreferences.getInt(SHORTENFACE, DEFAULT_VALUE_INT_BEAUTY_SLIMFACE);
        return shortenface;
    }

    public static void setBigEye(Context context, int saturation) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putInt(BIGEYE, saturation);
        editor.commit();
    }

    public static int getBigEye(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        int bigeye = sharedPreferences.getInt(BIGEYE, DEFAULT_VALUE_INT_BEAUTY_BITEYE);
        return bigeye;
    }

    public static void setPreviewMirror(Context context, boolean previewMirror) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putBoolean(PREVIEW_MIRROR, previewMirror);
        editor.commit();
    }

    public static boolean isPreviewMirror(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        boolean previewMirror = sharedPreferences.getBoolean(PREVIEW_MIRROR, DEFAULT_VALUE_PREVIEW_MIRROR);
        return previewMirror;
    }

    public static void setPushMirror(Context context, boolean pushMirror) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putBoolean(PUSH_MIRROR, pushMirror);
        editor.commit();
    }

    public static boolean isPushMirror(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        boolean pushMirror = sharedPreferences.getBoolean(PUSH_MIRROR, DEFAULT_VALUE_PUSH_MIRROR);
        return pushMirror;
    }

    public static void setAutofocus(Context context, boolean autofocus) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putBoolean(AUTOFOCUS, autofocus);
        editor.commit();
    }

    public static boolean isAutoFocus(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        boolean autoFocus = sharedPreferences.getBoolean(AUTOFOCUS, DEFAULT_VALUE_AUTO_FOCUS);
        return autoFocus;
    }

    public static void setTargetBit(Context context, int target) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putInt(TARGET_BIT, target);
        editor.commit();
    }

    public static int getTargetBit(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        int target = sharedPreferences.getInt(TARGET_BIT, 0);
        return target;
    }

    public static void setMinBit(Context context, int min) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putInt(MIN_BIT, min);
        editor.commit();
    }

    public static int getMinBit(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        int min = sharedPreferences.getInt(MIN_BIT, 0);
        return min;
    }

    public static void setHintTargetBit(Context context, int hintTarget) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putInt(HINT_TARGET_BIT, hintTarget);
        editor.commit();
    }

    public static int getHintTargetBit(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        int hintTarget = sharedPreferences.getInt(HINT_TARGET_BIT, 0);
        return hintTarget;
    }

    public static void setHintMinBit(Context context, int hintMin) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putInt(HINT_MIN_BIT, hintMin);
        editor.commit();
    }

    public static int getHintMinBit(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        int hintMin = sharedPreferences.getInt(HINT_MIN_BIT, 0);
        return hintMin;
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

    public static void setGuide(Context context, boolean guide) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putBoolean(SHOWGUIDE, guide);
        editor.commit();
    }

    public static boolean isGuide(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        boolean autoFocus = sharedPreferences.getBoolean(SHOWGUIDE, true);
        return autoFocus;
    }

    public static void setBeautyOn(Context context, boolean beautyOn) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putBoolean(BEAUTYON, beautyOn);
        editor.commit();
    }

    public static boolean isBeautyOn(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        boolean beautyOn = sharedPreferences.getBoolean(BEAUTYON, true);
        return beautyOn;
    }


    public static void setAudienceUser(Context context, int userid) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putInt(ROLE_AUDIENCE_USER, userid);
        editor.commit();
    }

    public static int getAudienceUser(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        int audienceUser = sharedPreferences.getInt(ROLE_AUDIENCE_USER, -1);
        return audienceUser;
    }


    public static String getUser(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        String userInfo = sharedPreferences.getString(USER_INFO, "");
        return userInfo;
    }

    public static void setForbidUser(Context context, String users) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putString(FORBID_USER, users);
        editor.commit();
    }

    public static String getForbidUser(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        String forbidUser = sharedPreferences.getString(FORBID_USER, "");
        return forbidUser;
    }

    public static void clear(Context context) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.remove(WHITE);
        editor.remove(BUFFING);
        editor.remove(RUDDY);
        editor.remove(BRIGHTNESS);
        editor.remove(CHEEKPINK);
        editor.remove(AUTOFOCUS);
        editor.remove(PREVIEW_MIRROR);
        editor.remove(PUSH_MIRROR);
        editor.remove(TARGET_BIT);
        editor.remove(MIN_BIT);
        editor.remove(BEAUTYON);
        editor.remove(HINT_MIN_BIT);
        editor.remove(HINT_TARGET_BIT);
        editor.remove(NETCONFIG);
        editor.commit();
    }

    public static int getBeautyFaceLevel(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        return sharedPreferences.getInt(BEAUTY_FACE_LEVEL, 3);
    }

    public static int getBeautySkinLevel(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE,
                                              Activity.MODE_PRIVATE);
        return sharedPreferences.getInt(BEAUTY_SKIN_LEVEL, 3);
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


    public static void setBeautySkinLevel(Context context, int level) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHAREDPRE_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.remove(BEAUTY_SKIN_LEVEL);
        editor.putInt(BEAUTY_SKIN_LEVEL, level);
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
