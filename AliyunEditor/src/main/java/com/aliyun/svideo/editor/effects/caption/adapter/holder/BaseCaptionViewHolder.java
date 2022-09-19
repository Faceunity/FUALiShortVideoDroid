package com.aliyun.svideo.editor.effects.caption.adapter.holder;

import android.content.Context;
import android.view.View;

import com.aliyun.svideo.editor.effects.caption.listener.OnCaptionChooserStateChangeListener;

public abstract class BaseCaptionViewHolder {
    private View mItemView;
    private String mTitle;
    private final Context mContext;
    private OnCaptionChooserStateChangeListener mOnCaptionChooserStateChangeListener;

    public BaseCaptionViewHolder(Context context, String title,
                                 OnCaptionChooserStateChangeListener onCaptionChooserStateChangeListener) {
        mContext = context;
        this.mTitle = title;
        this.mOnCaptionChooserStateChangeListener = onCaptionChooserStateChangeListener;
        this.mItemView = onCreateView(context);

    }

    public String getTitle() {
        return mTitle;
    }

    public Context getContext() {
        return mContext;
    }

    public OnCaptionChooserStateChangeListener getCaptionChooserStateChangeListener() {
        return mOnCaptionChooserStateChangeListener;
    }

    public View getItemView() {
        return mItemView;
    }

    public abstract View onCreateView(Context context);

    public abstract void onBindViewHolder();

    public  void onTabClick(){}

    public void notifyDataSetChanged() {
    }
    public void resourceChanged(){

    }


}
