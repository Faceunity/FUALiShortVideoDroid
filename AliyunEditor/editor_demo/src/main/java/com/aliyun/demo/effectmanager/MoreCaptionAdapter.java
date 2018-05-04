/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.effectmanager;

import java.util.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.aliyun.common.utils.DensityUtil;
import com.aliyun.demo.editor.R;
import com.aliyun.demo.effects.overlay.PasterPreviewDialog;
import com.aliyun.quview.CircleProgressBar;
import com.aliyun.struct.form.ResourceForm;

public class MoreCaptionAdapter extends RecyclerView.Adapter<MoreCaptionAdapter.CaptionViewHolder> {
    private static final String TAG = MoreCaptionAdapter.class.getName();
    private static final int VIEW_TYPE_LOCAL = 1;
    private static final int VIEW_TYPE_REMOTE = 2;
    private List<EffectBody<ResourceForm>> mRemoteDataList;
    private List<EffectBody<ResourceForm>> mLocalDataList;
    private Context mContext;

    private OnItemRightButtonClickListener mRightBtnClickListener;

    public MoreCaptionAdapter(List<EffectBody<ResourceForm>> remoteList,
                              List<EffectBody<ResourceForm>> localList,
                              Context context) {
        this.mRemoteDataList = remoteList;
        this.mLocalDataList = localList;
        this.mContext = context;
    }

    public synchronized void changeToLocal(EffectBody<ResourceForm> remoteData) {
        remoteData.setLocal(true);
        mRemoteDataList.remove(remoteData);
        mLocalDataList.add(remoteData);
        notifyDataSetChanged();
    }

    private int convert2RemotePosition(int position) {
        if (mLocalDataList == null || mLocalDataList.size() == 0) {
            return position;
        } else {
            return position - mLocalDataList.size();
        }
    }

    class CaptionViewHolder extends RecyclerView.ViewHolder {
        private TextView mTvName, mTvDesc, mTvRightButton;
        private ImageView mIvIcon;
        private EffectBody<ResourceForm> mData;
        private int mPosition;
        private CircleProgressBar downloadProgress;

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
            mTvName = (TextView) itemView.findViewById(R.id.tv_name);
            mTvDesc = (TextView) itemView.findViewById(R.id.tv_desc);
            mTvRightButton = (TextView) itemView.findViewById(R.id.tv_right_button);
            mIvIcon = (ImageView) itemView.findViewById(R.id.iv_icon);
            downloadProgress = (CircleProgressBar) itemView.findViewById(R.id.download_progress);
            int width = DensityUtil.dip2px(itemView.getContext(),25);
            downloadProgress.setBackgroundWidth(width, width);
            downloadProgress.setProgressWidth(width);
            downloadProgress.isFilled(true);
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
                    if(mData.getData().getPreviewUrl() != null && !"".equals(mData.getData().getPreviewUrl())) {
                        PasterPreviewDialog dialog = PasterPreviewDialog.newInstance(mData.getData().getPreviewUrl(),
                                mData.getData().getName(), mData.getData().getId());
                        dialog.show(((MoreCaptionActivity) mContext).getSupportFragmentManager(), "aliyun_svideo_caption");
                    }
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(mLocalDataList == null || mLocalDataList.size() == 0) {
            return VIEW_TYPE_REMOTE;
        }else if(position < mLocalDataList.size()) {
            return VIEW_TYPE_LOCAL;
        }else {
            return VIEW_TYPE_REMOTE;
        }
    }

    @Override
    public CaptionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.aliyun_svideo_layout_effect_manager_list_item, parent, false);
        CaptionViewHolder holder = new CaptionViewHolder(view);
        switch (viewType) {
            case VIEW_TYPE_LOCAL:
                holder.mTvRightButton.setText(R.string.use_effect_edit);
                holder.mTvRightButton.setBackgroundResource(R.drawable.aliyun_svideo_shape_more_paster_use_bg);
                break;
            case VIEW_TYPE_REMOTE:
                holder.mTvRightButton.setText(R.string.download_effect_edit);
                holder.mTvRightButton.setBackgroundResource(R.drawable.aliyun_svideo_shape_more_paster_download_bg);
                break;
            default:
                break;
        }

        return holder;
    }


    @Override
    public void onBindViewHolder(CaptionViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        holder.mTvRightButton.setVisibility(View.VISIBLE);
        holder.downloadProgress.setVisibility(View.GONE);
        switch (viewType) {
            case VIEW_TYPE_LOCAL:
                holder.updateData(position, mLocalDataList.get(position));
                break;
            case VIEW_TYPE_REMOTE:
                holder.updateData(position, mRemoteDataList.get(convert2RemotePosition(position)));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mLocalDataList == null ? mRemoteDataList.size() : mLocalDataList.size()+mRemoteDataList.size();
    }

    public void updateProcess(CaptionViewHolder viewHolder, int process, int position) {
        if(viewHolder != null && viewHolder.mPosition == position) {
            viewHolder.mTvRightButton.setVisibility(View.GONE);
            viewHolder.downloadProgress.setVisibility(View.VISIBLE);
            viewHolder.downloadProgress.setProgress(process);
        }
    }

    public void setRemoteData(List<EffectBody<ResourceForm>> dataList) {
        this.mRemoteDataList = dataList;
        notifyDataSetChanged();
    }

    public void setLocalData(List<EffectBody<ResourceForm>> dataList) {
        this.mLocalDataList = dataList;
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
