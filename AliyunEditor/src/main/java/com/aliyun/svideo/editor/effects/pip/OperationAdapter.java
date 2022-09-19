/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.pip;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.base.widget.CircularImageView;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideosdk.common.internal.videoaugment.VideoAugmentationType;

import java.util.ArrayList;
import java.util.List;

public class OperationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
    implements View.OnClickListener {

    private Context mContext;
    private OnItemClickListener mItemClick;
    private int mSelectedPos = 0;
    private OperationViewHolder mSelectedHolder;

    public OperationAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.alivc_editor_item_pip, parent, false);
        OperationViewHolder operationViewHolder = new OperationViewHolder(view);
        return operationViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final OperationViewHolder operationViewHolder = (OperationViewHolder) holder;
        String name = Operation.values()[position].name;

        if (mSelectedPos > Operation.values().length) {
            mSelectedPos = 0;
        }
        if (position == 0) {
            mSelectedHolder = operationViewHolder;
        }
        operationViewHolder.mName.setSelected(mSelectedPos == position || Operation.values()[position].allwaysSelected);
        operationViewHolder.mName.setText(name);
        operationViewHolder.itemView.setTag(holder);
        operationViewHolder.itemView.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return Operation.values().length;
    }


    private static class OperationViewHolder extends RecyclerView.ViewHolder {

        TextView mName;

        public OperationViewHolder(View itemView) {
            super(itemView);
            mName = (TextView) itemView.findViewById(R.id.resource_name);
        }

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClick = listener;
    }


    @Override
    public void onClick(View view) {
        if (mItemClick != null) {
            OperationViewHolder viewHolder = (OperationViewHolder) view.getTag();
            int position = viewHolder.getAdapterPosition();
            if (mSelectedHolder != null) {
                mSelectedHolder.mName.setSelected(false);
            }
            Operation operation = Operation.values()[position];
            if (mSelectedPos != position || operation.allwaysSelected) {
                if (mSelectedHolder != null) {
                    boolean select = false;
                    if (Operation.values()[mSelectedPos].allwaysSelected) {
                        select = true;
                    }
                    mSelectedHolder.mName.setSelected(select);
                }
                if (viewHolder != null) {
                    viewHolder.mName.setSelected(true);
                }
                mSelectedPos = position;
                mSelectedHolder = viewHolder;

                mItemClick.onItemClick(getOperation(position), position);
            }
        }
    }

    private Operation getOperation(int position) {
        return Operation.values()[position];
    }


    public interface OnItemClickListener {
        boolean onItemClick(Operation type, int index);
    }
}
