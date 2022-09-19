/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.videoeq;

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

public class FilterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
    implements View.OnClickListener {

    private Context mContext;
    private OnItemClickListener mItemClick;
    private int mSelectedPos = 0;
    private FilterViewHolder mSelectedHolder;
    private List<String> mNameList = new ArrayList<>();
    private static final int[] ICONS = new int[]{
            R.drawable.alivc_svideo_augmentation_brightness,
            R.drawable.alivc_svideo_augmentation_contrast,
            R.drawable.alivc_svideo_augmentation_saturation,
            R.drawable.alivc_svideo_augmentation_sharpness,
            R.drawable.alivc_svideo_augmentation_vignette
    };

    public FilterAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.alivc_editor_item_effect, parent, false);
        FilterViewHolder filterViewHolder = new FilterViewHolder(view);
        filterViewHolder.frameLayout = (FrameLayout) view.findViewById(R.id.resource_image);
        return filterViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final FilterViewHolder filterViewHolder = (FilterViewHolder) holder;
        String name = mContext.getString(R.string.alivc_editor_dialog_reset);
        String path = mNameList.get(position);
        if (path == null || "".equals(path)) {
            filterViewHolder.mImage.setImageResource(R.drawable.alivc_svideo_effect_none);
        } else {
            name = path;
            if (filterViewHolder != null) {
                filterViewHolder.mImage.setImageResource(ICONS[position - 1]);
            }
        }

        if (mSelectedPos > mNameList.size()) {
            mSelectedPos = 0;
        }

        if (mSelectedPos == position) {
            filterViewHolder.mImage.setVisibility(View.GONE);
            filterViewHolder.mIvSelectState.setVisibility(View.VISIBLE);
            mSelectedHolder = filterViewHolder;
        } else {
            filterViewHolder.mImage.setVisibility(View.VISIBLE);
            filterViewHolder.mIvSelectState.setVisibility(View.GONE);
        }
        filterViewHolder.mName.setText(name);
        filterViewHolder.itemView.setTag(holder);
        filterViewHolder.itemView.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return mNameList.size();
    }


    private static class FilterViewHolder extends RecyclerView.ViewHolder {

        FrameLayout frameLayout;
        ImageView mIvSelectState;
        CircularImageView mImage;
        TextView mName;

        public FilterViewHolder(View itemView) {
            super(itemView);
            mImage = (CircularImageView) itemView.findViewById(R.id.resource_image_view);
            mName = (TextView) itemView.findViewById(R.id.resource_name);
            mIvSelectState = itemView.findViewById(R.id.iv_select_state);
        }

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClick = listener;
    }


    @Override
    public void onClick(View view) {
        if (mItemClick != null) {
            FilterViewHolder viewHolder = (FilterViewHolder) view.getTag();
            int position = viewHolder.getAdapterPosition();
            if (mSelectedPos != position) {
                if (mSelectedHolder != null && mSelectedHolder.mImage != null) {
                    mSelectedHolder.mImage.setVisibility(View.VISIBLE);
                    mSelectedHolder.mIvSelectState.setVisibility(View.GONE);
                }
                viewHolder.mImage.setVisibility(View.GONE);
                viewHolder.mIvSelectState.setVisibility(View.VISIBLE);
                mSelectedPos = position;
                mSelectedHolder = viewHolder;

                mItemClick.onItemClick(getEqType(position), position);
            }
        }
    }

    public void setDataList(List<String> list) {
        mNameList.clear();
        mNameList.add(null);
        mNameList.addAll(list);
    }

    private VideoAugmentationType getEqType(int position){
        switch (position){
            case 1:
                return VideoAugmentationType.BRIGHTNESS;
            case 2:
                return VideoAugmentationType.CONTRAST;
            case 3:
                return VideoAugmentationType.SATURATION;
            case 4:
                return VideoAugmentationType.SHARPNESS;
            case 5:
                return VideoAugmentationType.VIGNETTE;
            default:
                return null;
        }
    }

    public interface OnItemClickListener {
        boolean onItemClick(VideoAugmentationType type, int index);
    }
}
