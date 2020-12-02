/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.control;

import android.view.View;
import android.view.View.OnClickListener;

import java.util.ArrayList;

public class TabGroup implements OnClickListener {
    private static final long MIN_CLICK_INTERVAL = 1000;
    public interface OnCheckedChangeListener {
        void onCheckedChanged(TabGroup control, int checkedIndex);
    }

    private OnCheckedChangeListener mOnCheckedChangeistener;
    private OnTabChangeListener mOnTabChangeListener;

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeistener = listener;
    }

    public void setOnTabChangeListener(OnTabChangeListener listener) {
        mOnTabChangeListener = listener;
    }

    private final ArrayList<View> mViewList = new ArrayList<>();

    public void addView(View view) {
        view.setOnClickListener(this);
        mViewList.add(view);
    }

    private int mCheckedIndex = -1;

    public int getCheckedIndex() {
        return mCheckedIndex;
    }
    public void setCheckedView(View item) {

        setCheckedIndex(mViewList.indexOf(item));
    }

    public void setCheckedIndex(int index) {
        if (mCheckedIndex >= 0) {
            mViewList.get(mCheckedIndex).setActivated(false);
        }
        mCheckedIndex = index;
        if (mCheckedIndex >= 0) {
            mViewList.get(mCheckedIndex).setActivated(true);
        }

        if (mOnCheckedChangeistener != null) {
            mOnCheckedChangeistener.onCheckedChanged(this, mCheckedIndex);
        }
    }

    @Override
    public void onClick(View v) {
        if (isTimeEnabled()) {
            setCheckedView(v);
            if (mOnTabChangeListener != null) {
                mOnTabChangeListener.onTabChange();
            }
        }

    }

    private long lastTimeMillis;

    /**
     * 防止同时按下多个控件，事件触发混乱的问题
     * @return
     */
    protected boolean isTimeEnabled() {
        long currentTimeMillis = System.currentTimeMillis();
        if ((currentTimeMillis - lastTimeMillis) > MIN_CLICK_INTERVAL) {
            lastTimeMillis = currentTimeMillis;
            return true;
        }
        return false;
    }


}
