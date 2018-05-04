/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.importer.media;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;



public class DragMoveAdapter extends RecyclerView.Adapter<DragMoveAdapter.DragViewHolder>{


    @Override
    public DragViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(DragViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    abstract class ItemLongClickListener implements View.OnLongClickListener {
        private RecyclerView.ViewHolder holder;
        private int position;

        public ItemLongClickListener(RecyclerView.ViewHolder holder) {
            this.holder = holder;
        }

        @Override
        public boolean onLongClick(View v) {
            return onItemLongClickListener(holder, position);
        }

        public abstract boolean onItemLongClickListener(RecyclerView.ViewHolder holder, int position);

        public void reset(int position) {
            this.position = position;
        }
    }

    public static class DragViewHolder extends RecyclerView.ViewHolder {

        private ItemLongClickListener mItemLongClickListener;
        public DragViewHolder(View itemView) {
            super(itemView);
        }

        public void setItemLongClickListener(ItemLongClickListener itemLongClickListener) {
            mItemLongClickListener = itemLongClickListener;
        }

        public ItemLongClickListener getItemLongClickListener() {
            return mItemLongClickListener;
        }
    }



}
