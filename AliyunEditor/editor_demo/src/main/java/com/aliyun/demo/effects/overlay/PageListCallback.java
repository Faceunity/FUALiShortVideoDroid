/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.effects.overlay;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;
import com.aliyun.demo.editor.R;
import com.aliyun.quview.CircularImageView;
import com.aliyun.quview.pagerecyclerview.PageRecyclerView;
import com.aliyun.struct.form.PasterForm;
import com.aliyun.struct.form.ResourceForm;

import java.util.ArrayList;

public abstract class PageListCallback implements PageRecyclerView.CallBack<PageViewHolder> {
    private static final int NO_SELECTED = -1;
    private ArrayList<PasterForm> mListData = new ArrayList<>();
    private ResourceForm mResourceForm;
    private int mCurrSelectedPos = NO_SELECTED;
    private Context mContext;

    public PageListCallback(Context context) {
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.aliyun_svideo_overlay_item_view, parent, false);
        PageViewHolder holder = new PageViewHolder(view);
        holder.mImageView = (CircularImageView) view.findViewById(R.id.overlayout_image_source);
        return holder;
    }

    @Override
    public void onBindViewHolder(final PageViewHolder holder, int position) {
        if (mListData != null
                && position < mListData.size()
                && position >= 0) {
            PasterForm model = mListData.get(position);

            Glide.with(mContext).load(model.getIcon()).into(new ViewTarget<CircularImageView, GlideDrawable>(holder.mImageView) {
                @Override
                public void onResourceReady(GlideDrawable glideDrawable, GlideAnimation<? super GlideDrawable> glideAnimation) {
                    holder.mImageView.setImageBitmap(((GlideBitmapDrawable) glideDrawable).getBitmap());
                }
            });
        }
    }

    @Override
    public void onItemClickListener(View view, int position) {
        int prePos = -1;
        if (mCurrSelectedPos == position) {//当前已经选中，不需要再操作
            return;
        }
        if (mCurrSelectedPos == NO_SELECTED) {//从来没选过
            mCurrSelectedPos = position;
        } else {//当前选中的不是同一个
            prePos = mCurrSelectedPos;
            mCurrSelectedPos = position;
        }
        notifySelected(mCurrSelectedPos, prePos);
    }

    public abstract void notifySelected(int selectedPos, int prePos);


    public PasterForm getSelectedItem() {
        if (mCurrSelectedPos == NO_SELECTED) {
            return null;
        } else {
            if (mCurrSelectedPos >= 0 && mCurrSelectedPos < mListData.size()) {
                return mListData.get(mCurrSelectedPos);
            }
            return null;
        }
    }

    @Override
    public void onItemLongClickListener(View view, int position) {

    }

    public void resetSelected() {
        mCurrSelectedPos = NO_SELECTED;
    }

    public void setData(ResourceForm data) {
        if (data == null || data.getPasterList() == null) {
            return;
        }
        mResourceForm = data;
        this.mListData = (ArrayList<PasterForm>) data.getPasterList();
    }

}


class PageViewHolder extends RecyclerView.ViewHolder {
    CircularImageView mImageView;

    public PageViewHolder(View itemView) {
        super(itemView);
    }
}

