package com.aliyun.video.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import com.aliyun.video.common.utils.FileUtils;
import com.aliyun.video.common.utils.PermissionUtils;

/**
 * @author cross_ly DATE 2019/01/24
 * <p>描述:
 */
public class SdcardUtils {


    private static final String TAG = SdcardUtils.class.getSimpleName();
    private static final String HINT_STRING = "设备内存不足，请及时清理";

    /**
     * 检测sdcard剩余有效内存，低于参数时弹出内存不足提示
     * @param tagSize 单位M
     */
    public static void checkAvailableSize(Context context, int tagSize) {

        boolean isHasPermission = PermissionUtils.checkPermissionsGroup(context, PermissionUtils.PERMISSION_STORAGE);
        if (isHasPermission) {
            long availableSize = FileUtils.getSdcardAvailableSize() / 1024 / 1024;
            Log.e(TAG, "log_common_SdcardUtils_availableSize : " + availableSize);
            if (availableSize < tagSize) {
                showAlertDialog(context);
            }
        }
    }

    /**
     * 显示警告框
     * @param context Context
     */
    private static void showAlertDialog(Context context) {

        final AlertDialog.Builder alertDialog =
            new AlertDialog.Builder(context);
        alertDialog.setTitle("提示");
        alertDialog.setMessage(HINT_STRING);
        alertDialog.setPositiveButton("确定",
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //nothing to do
            }
        });
        alertDialog.setCancelable(false);
        // 显示
        alertDialog.show();
    }
}
