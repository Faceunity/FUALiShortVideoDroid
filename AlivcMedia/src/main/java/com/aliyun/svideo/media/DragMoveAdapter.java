/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.media;

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

    abstract class AbstractItemLongClickListener implements View.OnLongClickListener {
        private RecyclerView.ViewHolder holder;
        private int position;

        public AbstractItemLongClickListener(RecyclerView.ViewHolder holder) {
            this.holder = holder;
        }

        @Override
        public boolean onLongClick(View v) {
            return onItemLongClickListener(holder, position);
        }

        /**
         * item long click listener
         * @param holder RecyclerView.ViewHolder
         * @param position position
         * @return
         */
        public abstract boolean onItemLongClickListener(RecyclerView.ViewHolder holder, int position);

        public void reset(int position) {
            this.position = position;
        }
    }

    public static class DragViewHolder extends RecyclerView.ViewHolder {

        private AbstractItemLongClickListener mItemLongClickListener;
        public DragViewHolder(View itemView) {
            super(itemView);
        }

        public void setItemLongClickListener(AbstractItemLongClickListener itemLongClickListener) {
            mItemLongClickListener = itemLongClickListener;
        }

        public AbstractItemLongClickListener getItemLongClickListener() {
            return mItemLongClickListener;
        }
    }



}
