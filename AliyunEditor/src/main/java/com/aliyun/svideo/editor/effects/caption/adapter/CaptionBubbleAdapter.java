/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.caption.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.common.logger.Logger;
import com.aliyun.svideo.base.Form.PasterForm;
import com.aliyun.svideo.common.utils.ThreadUtils;
import com.aliyun.svideo.common.utils.image.ImageLoaderImpl;
import com.aliyun.svideo.downloader.DownloaderManager;
import com.aliyun.svideo.downloader.FileDownloaderCallback;
import com.aliyun.svideo.downloader.FileDownloaderModel;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.contant.CaptionConfig;
import com.aliyun.svideo.editor.effects.caption.listener.OnCaptionChooserStateChangeListener;
import com.aliyun.svideo.editor.effects.caption.manager.CaptionManager;
import com.aliyun.svideo.editor.util.AlivcResUtil;
import com.aliyun.svideosdk.common.struct.project.Source;

import java.util.ArrayList;
import java.util.List;

public class CaptionBubbleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<PasterForm> mPasterFormList = new ArrayList<>();
    private int mGroupId;
    private OnCaptionChooserStateChangeListener mOnCaptionChooserStateChangeListener;
    private int mCurrentSelectIndex = -1;
    private FileDownloaderModel mFileDownloaderModel;
    private List<FileDownloaderModel> mFontFromLocal;
    private final ImageLoaderImpl imageLoader;
    public CaptionBubbleAdapter() {
        imageLoader = new ImageLoaderImpl();
    }

    public void setData(FileDownloaderModel fileDownloaderModel, List<PasterForm> pasterFormList, int groupId) {
        if (pasterFormList == null) {
            return;
        }
        mFileDownloaderModel = fileDownloaderModel;
        mCurrentSelectIndex = -1;
        mGroupId = groupId;
        mPasterFormList.clear();
        mPasterFormList.addAll(pasterFormList);
        notifyDataSetChanged();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alivc_editor_caption_bubble_item_paster, parent, false);
        return new CaptionViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return CaptionConfig.FONT_TYPE;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final CaptionViewHolder captionViewHolder = (CaptionViewHolder) holder;
        position = captionViewHolder.getAdapterPosition();
        String iconPath = mPasterFormList.get(position).getIcon();
        imageLoader.loadImage(captionViewHolder.mImage.getContext().getApplicationContext(), iconPath).into(captionViewHolder.mImage);
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
                    ThreadUtils.runOnSubThread(new Runnable() {
                        @Override
                        public void run() {
                            final PasterForm pasterInfo = mPasterFormList.get(finalPosition);
                            final int fontId = pasterInfo.getFontId();
                            String fontPath = null;
                            if (mFontFromLocal == null) {
                                //获取字体资源
                                mFontFromLocal = CaptionManager.getFontFromLocal();
                            }
                            if (mFontFromLocal.size() > 0) {
                                for (FileDownloaderModel fileDownloaderModel : mFontFromLocal) {
                                    if (fileDownloaderModel.getId() == fontId){
                                        fontPath = fileDownloaderModel.getPath();
                                        break;
                                    }
                                }
                            }
                            final String bubblePath = DownloaderManager.getInstance().getDbController().getPathByUrl(pasterInfo.getDownloadUrl());
                            if (bubblePath != null && !bubblePath.isEmpty()) {
                                Source bubbleSource = new Source(bubblePath);
                                bubbleSource.setId(String.valueOf(pasterInfo.getId()));
                                boolean isApp = bubbleSource.getPath().contains("aliyun_svideo_caption/");
                                bubbleSource.setURL(AlivcResUtil.getResUri(isApp ? "app": "cloud", AlivcResUtil.TYPE_BUBBLE, String.valueOf(mGroupId), String.valueOf(pasterInfo.getId())));
                                Source fontSource = new Source(fontPath);
                                fontSource.setURL(AlivcResUtil.getCloudResUri(AlivcResUtil.TYPE_FONT, String.valueOf(fontId)));
                                callBack(bubbleSource,fontSource);
                            } else {
                                final String finalFontPath = fontPath;
                                CaptionManager.downloadPaster(mFileDownloaderModel, new FileDownloaderCallback() {
                                    @Override
                                    public void onFinish(int downloadId, String path) {
                                        super.onFinish(downloadId, path);
                                        Source bubbleSource = new Source(path);
                                        bubbleSource.setId(String.valueOf(pasterInfo.getId()));
                                        boolean isApp = bubbleSource.getPath().contains("aliyun_svideo_caption/");
                                        bubbleSource.setURL(AlivcResUtil.getResUri(isApp ? "app": "cloud", AlivcResUtil.TYPE_BUBBLE, String.valueOf(mGroupId), String.valueOf(pasterInfo.getId())));
                                        Source fontSource = new Source(finalFontPath);
                                        fontSource.setURL(AlivcResUtil.getCloudResUri(AlivcResUtil.TYPE_FONT, String.valueOf(fontId)));
                                        callBack(bubbleSource,fontSource);
                                        Logger.getDefaultLogger().d("downloadId..." + downloadId + "  bubblePath..." + path);
                                    }
                                });
                            }
                        }
                    });

                }
            }
        });
    }

    private void callBack(final Source bubbleSource, final Source fontSource) {
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mOnCaptionChooserStateChangeListener != null) {
                    mOnCaptionChooserStateChangeListener.onBubbleEffectTemplateChanged(bubbleSource, fontSource);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPasterFormList != null ? mPasterFormList.size() : 0;
    }

    public void setSelectPosition(int currentIndex) {
        if (mPasterFormList != null && mPasterFormList.size() > currentIndex) {
            notifySelectedView(currentIndex);
        }

    }


    public void clearSelectedView() {
        if (mCurrentSelectIndex > -1) {
            notifyItemChanged(mCurrentSelectIndex);
            mCurrentSelectIndex = -1;
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
