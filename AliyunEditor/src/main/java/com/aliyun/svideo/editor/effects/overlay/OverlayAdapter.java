/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.overlay;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.svideo.editor.R;

public class OverlayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;

    public OverlayAdapter(Context context) {
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.alivc_editor_item_paster, parent, false);
        OverlayViewHolder overlayViewHolder = new OverlayViewHolder(view);
//        FilterAdapter.CaptionViewHolder filterViewHolder = new FilterAdapter.CaptionViewHolder(view);
//        filterViewHolder.frameLayout = (FrameLayout) view.findViewById(R.id.filter_image);
        return overlayViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 10;
    }


    private static class OverlayViewHolder extends RecyclerView.ViewHolder {
        public OverlayViewHolder(View itemView) {
            super(itemView);
        }
    }
}
