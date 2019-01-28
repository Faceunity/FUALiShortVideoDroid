/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.effectmanager;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aliyun.demo.editor.R;
import com.aliyun.demo.effects.overlay.PasterPreviewDialog;
import com.aliyun.svideo.sdk.external.struct.form.ResourceForm;
import com.bumptech.glide.Glide;

import java.util.List;

public class MoreCaptionAdapter extends RecyclerView.Adapter<MoreCaptionAdapter.CaptionViewHolder> {
    private static final String TAG = MoreCaptionAdapter.class.getName();
    private static final int VIEW_TYPE_LOCAL = 1;
    private static final int VIEW_TYPE_REMOTE = 2;
    private static final int VIEW_TYPE_DOWNLOADING = 3;
    private List<EffectBody<ResourceForm>> mDataList;
    private Context mContext;

    private OnItemRightButtonClickListener mRightBtnClickListener;

    public MoreCaptionAdapter(List<EffectBody<ResourceForm>> localList,
                              Context context) {
        this.mDataList = localList;
        this.mContext = context;
    }

    public synchronized void changeToLocal(EffectBody<ResourceForm> remoteData) {
        for (EffectBody<ResourceForm> effectBody : mDataList) {
            if (effectBody.equals(remoteData)){
                effectBody.setLocal(true);
                effectBody.setLoading(false);
            }
        }
        notifyDataSetChanged();
    }
    class CaptionViewHolder extends RecyclerView.ViewHolder {
        private ImageView downloadFinish;
        private TextView mTvName, mTvDesc, mTvRightButton;
        private ImageView mIvIcon;
        private EffectBody<ResourceForm> mData;
        private int mPosition;
        private ProgressBar downloadProgress;

        public void updateData(int position, EffectBody<ResourceForm> data) {
            this.mData = data;
            this.mPosition = position;
            ResourceForm form = data.getData();
            mTvName.setText(form.getName());
            mTvDesc.setText(form.getDescription());
            Glide.with(mIvIcon.getContext()).load(form.getIcon()).into(mIvIcon);
        }

        public CaptionViewHolder(View itemView) {
            super(itemView);
            mTvName = (TextView)itemView.findViewById(R.id.tv_name);
            mTvDesc = (TextView)itemView.findViewById(R.id.tv_desc);
            mTvRightButton = (TextView)itemView.findViewById(R.id.tv_right_button);
            mIvIcon = (ImageView)itemView.findViewById(R.id.iv_icon);
            downloadProgress = (ProgressBar)itemView.findViewById(R.id.download_progress);
            downloadFinish = (ImageView)itemView.findViewById(R.id.iv_download_finish);
            mTvRightButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mRightBtnClickListener != null) {
                        if (mData.isLocal()) {
                            mRightBtnClickListener.onLocalItemClick(mPosition, mData);
                        } else {
                            mRightBtnClickListener.onRemoteItemClick(mPosition, mData);
                        }
                    }
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mData.getData().getPreviewUrl() != null && !"".equals(mData.getData().getPreviewUrl())) {
                        PasterPreviewDialog dialog = PasterPreviewDialog.newInstance(mData.getData().getPreviewUrl(),
                            mData.getData().getName(), mData.getData().getId());
                        if (dialog != null){
                            dialog.show(((MoreCaptionActivity)mContext).getSupportFragmentManager(), "aliyun_svideo_caption");
                        }
                    }
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        int type = VIEW_TYPE_LOCAL;
        if(position >= 0 && position < mDataList.size()) {
            EffectBody<ResourceForm> data = mDataList.get(position);
            if(data.isLocal()) {
                return VIEW_TYPE_LOCAL;
            }else if(data.isLoading()) {
                return VIEW_TYPE_DOWNLOADING;
            }else {
                return VIEW_TYPE_REMOTE;
            }
        }
        return type;
    }

    @Override
    public CaptionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
            R.layout.aliyun_svideo_layout_effect_caption_list_item, parent, false);
        CaptionViewHolder holder = new CaptionViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(CaptionViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case VIEW_TYPE_LOCAL:
                holder.mTvRightButton.setVisibility(View.VISIBLE);
                holder.downloadFinish.setVisibility(View.VISIBLE);
                holder.mTvRightButton.setBackgroundColor(Color.TRANSPARENT);
                holder.downloadProgress.setVisibility(View.GONE);
                break;
            case VIEW_TYPE_REMOTE:
                holder.mTvRightButton.setVisibility(View.VISIBLE);
                holder.mTvRightButton.setText(R.string.download_effect_edit);
                holder.mTvRightButton.setBackgroundResource(R.drawable.aliyun_svideo_shape_caption_manager_bg);
                holder.downloadFinish.setVisibility(View.GONE);
                holder.downloadProgress.setVisibility(View.GONE);
                break;
            case VIEW_TYPE_DOWNLOADING:
                holder.downloadFinish.setVisibility(View.GONE);
                holder.downloadProgress.setVisibility(View.VISIBLE);
                holder.mTvRightButton.setVisibility(View.VISIBLE);
                holder.mTvRightButton.setText(R.string.downloading_effect_edit);
                holder.mTvRightButton.setBackgroundColor(Color.TRANSPARENT);
                break;
            default:
                break;
        }
        holder.updateData(position, mDataList.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public void updateProcess(CaptionViewHolder viewHolder, int process, int position) {

        if (viewHolder != null && viewHolder.mPosition == position) {
            viewHolder.mTvRightButton.setBackgroundColor(Color.TRANSPARENT);
            viewHolder.downloadProgress.setVisibility(View.VISIBLE);
            viewHolder.downloadProgress.setProgress(process);
            if (viewHolder.mData!=null){
                viewHolder.mData.setLoading(true);
            }
        }
        //if (viewHolder != null) {
        //    if (process > 0) {
        //        viewHolder.mTvRightButton.setText(mContext.getResources().getString(R.string.downloading_effect_edit));
        //    } else {
        //        viewHolder.mTvRightButton.setText(mContext.getResources().getString(R.string.download_effect_edit));
        //    }
        //}
    }

    public synchronized void setRemoteData(List<EffectBody<ResourceForm>> dataList) {
        List<Integer> localIds = null;
        //if (dataList!=null&&dataList.size()>0){
        //    localIds = new ArrayList<>(dataList.size());
        //    for (EffectBody<ResourceForm> effectBody : dataList) {
        //        localIds.add(effectBody.getData().getId());
        //    }
        //}
        //if(localIds != null && localIds.size() > 0) {
        //    for (EffectBody<ResourceForm> effectBody : mDataList) {
        //        if (localIds.contains(effectBody.getData().getId())){
        //            mDataList.remove(effectBody);
        //        }
        //    }
        //}
        for (EffectBody<ResourceForm> effectBody : dataList) {
            if (mDataList.contains(effectBody)){
                mDataList.remove(effectBody);
            }
        }
        mDataList.addAll(dataList);
        notifyDataSetChanged();
    }

    interface OnItemRightButtonClickListener {
        void onRemoteItemClick(int position, EffectBody<ResourceForm> data);

        void onLocalItemClick(int position, EffectBody<ResourceForm> data);
    }

    public void setRightBtnClickListener(OnItemRightButtonClickListener listener) {
        this.mRightBtnClickListener = listener;
    }

}
