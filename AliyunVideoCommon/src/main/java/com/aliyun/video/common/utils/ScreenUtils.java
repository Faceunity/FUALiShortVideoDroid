package com.aliyun.video.common.utils;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;

/**
 * @author cross_ly
 * @date 2018/11/28
 * <p>描述:获取当前设备的屏幕参数
 *
 */
public class ScreenUtils {

    /**
     * 获取屏幕的宽高 单位px
     * x = width
     * y = height
     * @param context Context
     * @return Point
     */
    public static Point getScreenPoint(Context context){

        Point screenPoint = new Point();
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        screenPoint.x = displayMetrics.widthPixels;
        screenPoint.y = displayMetrics.heightPixels;
        return screenPoint;
    }

    /**
     * 获取屏幕宽
     *
     * @param context 上下文
     * @return int ，单位px
     */
    public static int getWidth(Context context) {

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    /**
     * 获取屏幕高
     *
     * @param context 上下文
     * @return int ，单位px
     */
    public static int getHeight(Context context) {

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }

    /**
     * 是否在屏幕右侧
     *
     * @param mContext 上下文
     * @param xPos     位置的x坐标值
     * @return true：是。
     */
    public static boolean isInRight(Context mContext, int xPos) {
        return (xPos > getWidth(mContext) / 2);
    }

    /**
     * 是否在屏幕左侧
     *
     * @param mContext 上下文
     * @param xPos     位置的x坐标值
     * @return true：是。
     */
    public static boolean isInLeft(Context mContext, int xPos) {
        return (xPos < getWidth(mContext) / 2);
    }
}
