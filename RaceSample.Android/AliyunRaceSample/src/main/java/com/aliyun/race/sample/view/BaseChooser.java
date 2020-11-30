/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.race.sample.view;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.aliyun.race.sample.R;
import com.aliyun.race.sample.utils.NotchScreenUtil;


public class BaseChooser extends DialogFragment {

    private DialogVisibleListener mDismissListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.QUDemoFullStyle);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Window window = getDialog().getWindow();

        if (window != null) {
            //设置dialog动画
            window.getAttributes().windowAnimations = R.style.record_bottom_dialog_animation;
        }

    }


    @Override
    public void onStart() {
        super.onStart();

        Window window = getDialog().getWindow();
        Dialog dialog = getDialog();
        window.setGravity(Gravity.BOTTOM);

        DisplayMetrics dpMetrics = new DisplayMetrics();
        window.getWindowManager().getDefaultDisplay().getMetrics(dpMetrics);
        WindowManager.LayoutParams p = window.getAttributes();

        p.dimAmount = 0.0f;
        p.y = 100;
        window.setAttributes(p);

        if (dialog != null) {
            dialog.setCanceledOnTouchOutside(true);
            /*dpm获取的高度不准确，不要使用这种方式*/
            // dialog.getWindow().setLayout((int) (dpMetrics.widthPixels), (int) (dpMetrics.heightPixels ));

            // 适配传音CF8手机
            if (Build.MODEL.toUpperCase().contains("TECNO CF8")) {

                p.height = dpMetrics.heightPixels - NotchScreenUtil.getTECNOCF8NotchAndNaviHeight();
                dialog.getWindow().setLayout((int) (dpMetrics.widthPixels), p.height);

            }
        }
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        /*
            解决crash:java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
            原因:after onSaveInstanceState invoke commit,而 show 会触发 commit 操作
            fragment is added and its state has already been saved，
            Any operations that would change saved state should not be performed if this method returns true
         */
        if (isStateSaved()) {
            return;
        }
        super.show(manager, tag);
        if (mDismissListener != null) {
            mDismissListener.onDialogShow();
        }
    }

    @Override
    public int show(FragmentTransaction transaction, String tag) {
        if (mDismissListener != null) {
            mDismissListener.onDialogShow();
        }
        return super.show(transaction, tag);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mDismissListener != null) {
            mDismissListener.onDialogDismiss();
        }
    }

    public void setDismissListener(DialogVisibleListener dismissListener) {
        this.mDismissListener = dismissListener;
    }
}
