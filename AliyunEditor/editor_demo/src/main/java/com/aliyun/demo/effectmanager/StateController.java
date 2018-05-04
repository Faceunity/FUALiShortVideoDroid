/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.effectmanager;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class StateController {
    public abstract static class StateAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
        private OnStateChangeListener mStateChangeListener;

        public void switchState(int state){
            if(mStateChangeListener != null) {
            switch (state) {
                case STATE_COMPLETE:
                    mStateChangeListener.onCompleteState();
                    break;
                case STATE_EDIT:
                    mStateChangeListener.onEditState();
                    break;
            }
            }
        }

        public void setStateChangeListener(OnStateChangeListener listener) {
            this.mStateChangeListener = listener;
        }

        interface OnStateChangeListener {
            void onEditState();

            void onCompleteState();
        }
    }

    public static final int STATE_EDIT = 1, STATE_COMPLETE = 0;
    private int mCurrState = STATE_COMPLETE;
    private List<StateAdapter> mAdapters = new ArrayList<>();

    public void addAdatper(StateAdapter adapter) {
        mAdapters.add(adapter);
    }

    public void removeAdapter(StateAdapter adapter) {
        mAdapters.remove(adapter);
    }

    public void switchState() {
        if(mCurrState == STATE_COMPLETE) {
            mCurrState = STATE_EDIT;
        }else {
            mCurrState = STATE_COMPLETE;
        }
        for(StateAdapter adapter : mAdapters) {
            adapter.switchState(mCurrState);
        }
    }

}
