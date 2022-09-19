/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.caption.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.caption.listener.OnCaptionChooserStateChangeListener;
import com.aliyun.svideosdk.common.AliyunColor;

import java.util.ArrayList;
import java.util.List;


public class CaptionColorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private int mCurrentSelectIndex = -1;
    private List<Integer> mColorList = new ArrayList<>();
    private OnCaptionChooserStateChangeListener mOnCaptionColorItemClickListener;
    /**
     * 0 颜色 1 边框 2 阴影 3 背景
     */
    private int model;


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alivc_editor_caption_item_color, parent, false);
        return new ColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ColorViewHolder viewHolder = (ColorViewHolder) holder;
        position = holder.getAdapterPosition();
        if (position == mCurrentSelectIndex) {
            viewHolder.selectedView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.selectedView.setVisibility(View.GONE);
        }
        final Integer intColor = mColorList.get(position);
        viewHolder.colorView.setBackgroundColor(intColor);

        final int finalPosition = position;
        viewHolder.colorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (notifySelectedView(finalPosition)) {
                    if (mOnCaptionColorItemClickListener != null) {
                        AliyunColor aliyunColor = new AliyunColor(intColor);
                        if (model == 0) {
                            mOnCaptionColorItemClickListener.onCaptionTextColorChanged(aliyunColor);
                        } else if (model == 1) {
                            mOnCaptionColorItemClickListener.onCaptionTextStrokeColorChanged(aliyunColor);
                        } else if (model == 2) {
                            mOnCaptionColorItemClickListener.onCaptionTextShandowColorChanged(aliyunColor);
                        } else if (model == 3) {
                            mOnCaptionColorItemClickListener.onCaptionTextBackgroundColorChanged(aliyunColor);
                        }
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mColorList == null ? 0 : mColorList.size();
    }

    public List<Integer> getData() {
        return mColorList;
    }


    private static class ColorViewHolder extends RecyclerView.ViewHolder {

        public View colorView;
        public View selectedView;

        public ColorViewHolder(View itemView) {
            super(itemView);
            colorView = itemView.findViewById(R.id.paint_color_view);
            selectedView = itemView.findViewById(R.id.selected_view);
        }
    }


    public void setColorList(List<Integer> mColorList) {
        this.mColorList = mColorList;
        notifyDataSetChanged();
    }

    public void setOnCaptionColorItemClickListener(OnCaptionChooserStateChangeListener onCaptionColorItemClickListener) {
        mOnCaptionColorItemClickListener = onCaptionColorItemClickListener;
    }

    public void setSelectPosition(int currentIndex) {
        notifySelectedView(currentIndex);
    }

    private boolean notifySelectedView(int adapterPosition) {
        if (adapterPosition != mCurrentSelectIndex) {
            int last = mCurrentSelectIndex;
            mCurrentSelectIndex = adapterPosition;
            notifyItemChanged(last);
            notifyItemChanged(mCurrentSelectIndex);
            return true;
        } else {
            return false;
        }
    }


    public void setModel(int model) {
        this.model = model;
    }
}
