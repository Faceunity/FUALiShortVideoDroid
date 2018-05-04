/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.importer.media;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.aliyun.common.utils.DensityUtil;


public class GalleryItemDecoration extends RecyclerView.ItemDecoration {
    private int mLineSpace = DensityUtil.dip2px(1.0f);
    private int offset = DensityUtil.dip2px(1.0f);




    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect,view,parent,state);




        int position = parent.getChildLayoutPosition(view);


        outRect.bottom = mLineSpace;
        if(position%4 == 0){
            outRect.right = offset;
        }else if(position%4 == 1 || position % 4 == 2){
            outRect.left = offset/2;
            outRect.right = offset/2;
        }else{
            outRect.left = offset;
        }

    }
}
