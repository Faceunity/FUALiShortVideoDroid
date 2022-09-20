/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.caption.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.base.widget.CircularImageView;
import com.aliyun.svideo.common.utils.LanguageUtils;
import com.aliyun.svideo.downloader.FileDownloaderModel;
import com.aliyun.svideo.editor.R;

import java.util.ArrayList;
import java.util.List;

public class CaptionCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private OnItemClickListener mItemClick;
    private List<FileDownloaderModel> mData = new ArrayList<>();
    private int mSelectedPosition = 0;
    private static final int VIEW_TYPE_SELECTED = 1;
    private static final int VIEW_TYPE_UNSELECTED = 2;

    public CaptionCategoryAdapter() {

    }

    public void setData(List<FileDownloaderModel> data) {
        if (data == null) {
            return;
        }
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alivc_editor_item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final CategoryViewHolder categoryViewHolder = (CategoryViewHolder) holder;
        FileDownloaderModel fileDownloaderModel = mData.get(position);
        int viewType = holder.getItemViewType();
        if (fileDownloaderModel == null){
            categoryViewHolder.mName.setVisibility(View.GONE);
            categoryViewHolder.mImage.setVisibility(View.VISIBLE);
            categoryViewHolder.mImage.setImageResource(R.mipmap.alivc_svideo_caption_style_clear);
        }else if (fileDownloaderModel.getCategory() == -1) {
            categoryViewHolder.mName.setVisibility(View.GONE);
            categoryViewHolder.mImage.setVisibility(View.VISIBLE);
            categoryViewHolder.mImage.setImageResource(R.mipmap.aliyun_svideo_more);
        } else {
            categoryViewHolder.mImage.setVisibility(View.GONE);
            categoryViewHolder.mName.setVisibility(View.VISIBLE);
            String name = fileDownloaderModel.getName();
            if (!LanguageUtils.isCHEN(categoryViewHolder.mName.getContext()) && fileDownloaderModel.getNameEn() != null) {
                name = fileDownloaderModel.getNameEn();
            }
            categoryViewHolder.mName.setText(name);
        }
        categoryViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int position = holder.getAdapterPosition();
                FileDownloaderModel currentData = mData.get(position);
                if(currentData == null){
                    if (mItemClick != null) {
                        mItemClick.onItemClick(currentData);
                    }
                } else if (currentData.getCategory() == -1) {
                    if (mItemClick != null) {
                        mItemClick.onMoreClick();
                    }
                } else {
                    if (mItemClick != null) {
                        mItemClick.onItemClick(currentData);
                    }
                }
                selectPosition(position);
            }
        });
        switch (viewType) {
            case VIEW_TYPE_SELECTED:
                categoryViewHolder.itemView.setSelected(true);
                break;
            case VIEW_TYPE_UNSELECTED:
                categoryViewHolder.itemView.setSelected(false);
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public void selectPosition(int categoryIndex) {
        int lasPos = mSelectedPosition;
        notifyItemChanged(lasPos);
        mSelectedPosition = categoryIndex;
        notifyItemChanged(mSelectedPosition);
    }

    private static class CategoryViewHolder extends RecyclerView.ViewHolder {

        CircularImageView mImage;
        TextView mName;

        public CategoryViewHolder(View itemView) {
            super(itemView);
            mImage = itemView.findViewById(R.id.category_image_source);
            mName = itemView.findViewById(R.id.tv_category_name_source);
        }
    }




    @Override
    public int getItemViewType(int position) {
        if (position == mSelectedPosition) {
            return VIEW_TYPE_SELECTED;
        } else {
            return VIEW_TYPE_UNSELECTED;
        }
    }


    public interface OnItemClickListener{
        void onMoreClick();
        void onItemClick(FileDownloaderModel fileDownloaderModel);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClick = listener;
    }

}
