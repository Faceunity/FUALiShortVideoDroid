/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.caption.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.common.utils.StringUtils;
import com.aliyun.svideo.common.utils.image.ImageLoaderImpl;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.contant.CaptionConfig;
import com.aliyun.svideo.editor.effects.caption.listener.OnCaptionChooserStateChangeListener;
import com.aliyun.svideo.editor.util.AlivcResUtil;
import com.aliyun.svideosdk.common.struct.project.Source;

import java.io.File;
import java.util.List;

public class CaptionCoolTextAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String> mFontEffectList;
    private OnCaptionChooserStateChangeListener mOnCaptionChooserStateChangeListener;
    private int mCurrentSelectIndex = 0;
    private final ImageLoaderImpl imageLoader;

    public CaptionCoolTextAdapter() {
        imageLoader = new ImageLoaderImpl();
    }

    public void setData(List<String> fontEffectList) {
        if (fontEffectList == null) {
            return;
        }
        mFontEffectList = fontEffectList;
        notifyDataSetChanged();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType ==0 ){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alivc_editor_caption_bubble_index_item_paster, parent, false);
        }else {
             view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alivc_editor_caption_bubble_item_paster, parent, false);
        }
        return new CaptionViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0?0:1;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final CaptionViewHolder captionViewHolder = (CaptionViewHolder) holder;
        position = captionViewHolder.getAdapterPosition();
        final String dir = mFontEffectList.get(position);
        if (dir == null) {
            imageLoader.loadImage(captionViewHolder.mImage.getContext().getApplicationContext(), R.mipmap.alivc_svideo_caption_style_clear).into(captionViewHolder.mImage);
        } else {
            String iconPath = dir + File.separator + CaptionConfig.COOL_TEXT_FILE_ICON_NAME;
            imageLoader.loadImage(captionViewHolder.mImage.getContext().getApplicationContext(), iconPath).into(captionViewHolder.mImage);
        }

        if (position == mCurrentSelectIndex) {
            captionViewHolder.selectedview.setVisibility(View.VISIBLE);
        } else {
            captionViewHolder.selectedview.setVisibility(View.GONE);
        }

        final int finalPosition = position;
        captionViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (notifySelectedView(finalPosition)) {
                    if (mOnCaptionChooserStateChangeListener != null) {
                        Source source = new Source(dir);
                        if (!StringUtils.isEmpty(dir) && source.getPath().contains(File.separator)) {
                            String name = dir.substring(dir.lastIndexOf(File.separator) + 1);
                            source.setURL(AlivcResUtil.getAppResUri(AlivcResUtil.TYPE_CAPTION, name));
                        }
                        mOnCaptionChooserStateChangeListener.onFontEffectTemplateChanged(source);
                    }
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return mFontEffectList != null ? mFontEffectList.size() : 0;
    }


    public void setSelectPosition(int currentIndex) {
        if (mFontEffectList != null && mFontEffectList.size() > currentIndex) {
            notifySelectedView(currentIndex);
        }

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

    private static class CaptionViewHolder extends RecyclerView.ViewHolder {

        ImageView mImage;
        View selectedview;

        public CaptionViewHolder(View itemView) {
            super(itemView);
            mImage = itemView.findViewById(R.id.resource_image_view);
            selectedview = itemView.findViewById(R.id.selected_view);
        }
    }


    public void setOnItemClickListener(OnCaptionChooserStateChangeListener listener) {
        mOnCaptionChooserStateChangeListener = listener;
    }

}
