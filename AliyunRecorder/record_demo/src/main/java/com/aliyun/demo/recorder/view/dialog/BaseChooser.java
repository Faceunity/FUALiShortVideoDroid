/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.recorder.view.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.aliyun.apsaravideo.music.utils.NotchScreenUtil;
import com.aliyun.demo.R;

public class BaseChooser extends DialogFragment {

    public static final int CAPTION_REQUEST_CODE = 1001;
    public static final int IMV_REQUEST_CODE = 1002;
    public static final int PASTER_REQUEST_CODE = 1003;

    private DialogVisibleListener dismissListener;

    public ImageView mDismiss;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.QUDemoFullStyle);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.record_bottom_dialog_animation;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }

    @Override
    public void onResume() {
        getDialog().getWindow().setGravity(Gravity.BOTTOM);
        super.onResume();
        DisplayMetrics dpMetrics = new DisplayMetrics();
        getActivity().getWindow().getWindowManager().getDefaultDisplay()
        .getMetrics(dpMetrics);
        WindowManager.LayoutParams p = getDialog().getWindow().getAttributes();
        p.width = dpMetrics.widthPixels;

        // 检测是否是刘海屏, 如果是刘海屏, 并且不需要在缺口区域显示内容时, 需要减去缺口高度
        int[] notchSize = new int[2];
        if (NotchScreenUtil.checkNotchScreen(getContext())) {
            // 获取缺口的尺寸,返回一个int类型数组,
            // widht = int[0], height = int[1]
            notchSize = NotchScreenUtil.getNotchSize(getContext());
        }
        // 减去缺口高度, 如果不是刘海屏, 那么notchSize[1] = 0, 所以不会影响非刘海屏的尺寸
        // 适配传音CF8手机
        p.height = dpMetrics.heightPixels - notchSize[1] - NotchScreenUtil.getTECNOCF8NotchAndNaviHeight();

        getDialog().getWindow().setAttributes(p);
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
            return ;
        }
        super.show(manager, tag);
        if (dismissListener != null) {
            dismissListener.onDialogShow();
        }
    }

    @Override
    public int show(FragmentTransaction transaction, String tag) {
        if (dismissListener != null) {
            dismissListener.onDialogShow();
        }
        return super.show(transaction, tag);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (dismissListener != null) {
            dismissListener.onDialogDismiss();
        }
    }

    public void setDismissListener(DialogVisibleListener dismissListener) {
        this.dismissListener = dismissListener;
    }
}
