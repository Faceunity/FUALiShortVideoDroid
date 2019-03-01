/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.paint;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.aliyun.svideo.editor.R;

import java.util.ArrayList;
import java.util.List;


public class ColorAdapter extends RecyclerView.Adapter<ViewHolder> {

    private Context mContext;
    private List<Integer> mColorList = new ArrayList<>();
    private PaintChooserView.PaintSelect mPaintSelect;
    private int mSelectedPos = 1;
    private ColorViewHolder mSelectHolder;
    public ColorAdapter(Context context) {
        mContext = context;
        mColorList = initColors();
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.aliyun_svideo_paint_item_view, parent, false);
        ColorViewHolder colorViewHolder = new ColorViewHolder(view);
        return colorViewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final ColorViewHolder viewHolder = (ColorViewHolder) holder;
        if (position==0){
            viewHolder.colorImage.setImageResource(R.mipmap.alivc_svideo_icon_effect_revoke);
            viewHolder.colorImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mPaintSelect != null) {
                        mPaintSelect.onUndo();
                    }
                }
            });
        }else {
            viewHolder.colorImage.setColorFilter(mColorList.get(position));
            viewHolder.colorImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mPaintSelect != null) {
                        mPaintSelect.onColorSelect(mColorList.get(position));
                        viewHolder.colorImage.setSelected(true);
                        if(mSelectHolder != null) {
                            mSelectHolder.colorImage.setSelected(false);
                        }
                        mSelectedPos = position;
                        mSelectHolder = viewHolder;
                    }
                }
            });
        }


        if(mSelectedPos == position) {
            viewHolder.colorImage.setSelected(true);
            mSelectHolder = viewHolder;
        } else {
            viewHolder.colorImage.setSelected(false);
        }

    }

    @Override
    public int getItemCount() {
        return mColorList.size();
    }

    private static class ColorViewHolder extends  ViewHolder {

        private ImageView colorImage;
        public ColorViewHolder(View itemView) {
            super(itemView);
            colorImage = (ImageView) itemView.findViewById(R.id.paint_color_image);
        }
    }

    private List<Integer> initColors() {
        List<Integer> list = new ArrayList<>();
        TypedArray colors = mContext.getResources().obtainTypedArray(R.array.paint_colors);

        int size = colors.length();
        for (int i = 0; i < size; i++) {
            int color = colors.getColor(i, Color.WHITE);
            list.add(color);
        }
        list.add(0,0);
        colors.recycle();
        return list;
    }

    public void setPaintSelect(PaintChooserView.PaintSelect paintSelect) {
        this.mPaintSelect = paintSelect;
    }

    public void setSelectedPos(int color) {
        int r = mColorList.indexOf(color);
        if(r < 0) {
            return;
        }
        mSelectedPos = r;
    }

    public int getSelectedPos() {
        return this.mSelectedPos;
    }
}
