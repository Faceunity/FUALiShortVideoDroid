/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.caption.adapter;

import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.svideo.base.widget.CircularImageView;
import com.aliyun.svideo.common.utils.image.AbstractImageLoaderTarget;
import com.aliyun.svideo.common.utils.image.ImageLoaderImpl;
import com.aliyun.svideo.downloader.DownloaderManager;
import com.aliyun.svideo.downloader.FileDownloaderCallback;
import com.aliyun.svideo.downloader.FileDownloaderModel;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.contant.CaptionConfig;
import com.aliyun.svideo.editor.effects.caption.manager.CaptionManager;
import com.aliyun.svideo.editor.effects.caption.listener.OnCaptionChooserStateChangeListener;
import com.aliyun.svideo.editor.util.AlivcResUtil;
import com.aliyun.svideosdk.common.struct.project.Source;

import java.util.ArrayList;
import java.util.List;

public class CaptionFontAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<FileDownloaderModel> mFontData = new ArrayList<>();
    private OnCaptionChooserStateChangeListener mOnCaptionColorItemClickListener;
    private int mSelectPosition = -1;

    public void setData(List<FileDownloaderModel> fontData) {
        if (fontData == null) {
            return;
        }
        mFontData.clear();
        mFontData.addAll(fontData);
        notifyDataSetChanged();
    }

    public List<FileDownloaderModel> getFontData() {
        return mFontData;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alivc_editor_caption_font_item_paster, parent, false);
        return new CaptionViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mSelectPosition) {
            return CaptionConfig.VIEW_TYPE_SELECTED;
        } else {
            return CaptionConfig.VIEW_TYPE_UNSELECTED;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final CaptionViewHolder captionViewHolder = (CaptionViewHolder) holder;
        position = captionViewHolder.getAdapterPosition();
        int viewType = captionViewHolder.getItemViewType();
        switch (viewType) {
            case CaptionConfig.VIEW_TYPE_SELECTED:
                captionViewHolder.selectedview.setVisibility(View.VISIBLE);
                break;
            case CaptionConfig.VIEW_TYPE_UNSELECTED:
                captionViewHolder.selectedview.setVisibility(View.GONE);
                break;
            default:
                break;
        }
        String iconPath = mFontData.get(position).getIcon();
        if (CaptionConfig.SYSTEM_FONT.equals(iconPath)) {
            //系统字体
            captionViewHolder.mImage.setImageResource(R.mipmap.aliyun_svideo_system_font_icon);
        } else {
            new ImageLoaderImpl().loadImage(captionViewHolder.mImage.getContext(), iconPath)
                    .into(captionViewHolder.mImage, new AbstractImageLoaderTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource) {
                            captionViewHolder.mImage.setImageDrawable(resource);
                        }
                    });
        }
        final int finalPosition = position;
        captionViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (notifySelectedView(finalPosition)){
                    final FileDownloaderModel form = mFontData.get(finalPosition);
                    if (CaptionConfig.SYSTEM_FONT.equals(form.getIcon())) {
                        //系统字体
                        callBack(new Source(CaptionConfig.SYSTEM_FONT));
                    } else {
                        String path = DownloaderManager.getInstance().getDbController().getPathByUrl(form.getUrl());
                        if (path != null && !path.isEmpty()) {
                            Source fontSource = new Source(path);
                            fontSource.setURL(AlivcResUtil.getCloudResUri(AlivcResUtil.TYPE_FONT, String.valueOf(form.getId())));
                            callBack(fontSource);
                        } else {
                            CaptionManager.downloadFont(form, new FileDownloaderCallback() {
                                @Override
                                public void onFinish(int downloadId, String path) {
                                    super.onFinish(downloadId, path);
                                    Source fontSource = new Source(path);
                                    fontSource.setURL(AlivcResUtil.getCloudResUri(AlivcResUtil.TYPE_FONT, String.valueOf(form.getId())));
                                    callBack(fontSource);

                                }
                            });
                        }
                    }
                }

            }
        });
    }

    public void setmCurrectSelectIndex(int postion) {
        notifySelectedView(postion);
    }

    private void callBack(Source fontSource) {
        if (mOnCaptionColorItemClickListener != null) {
            mOnCaptionColorItemClickListener.onCaptionTextFontTtfChanged(fontSource);
        }
    }

    @Override
    public int getItemCount() {
        return mFontData != null ? mFontData.size() : 0;
    }

    public void showFontData() {
        mFontData.clear();
        CaptionManager.getFontFromLocal();
        notifyDataSetChanged();
    }

    private static class CaptionViewHolder extends RecyclerView.ViewHolder {

        CircularImageView mImage;
        View selectedview;

        public CaptionViewHolder(View itemView) {
            super(itemView);
            mImage = itemView.findViewById(R.id.resource_image_view);
            selectedview = itemView.findViewById(R.id.selected_view);
        }
    }


    public void setOnItemClickListener(OnCaptionChooserStateChangeListener listener) {
        mOnCaptionColorItemClickListener = listener;
    }

    private boolean notifySelectedView(int adapterPosition) {
        if (adapterPosition != mSelectPosition) {
            int last = mSelectPosition;
            mSelectPosition = adapterPosition;
            notifyItemChanged(last);
            notifyItemChanged(mSelectPosition);
            return true;
        } else {
            return false;
        }
    }

}
