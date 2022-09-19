/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effectmanager;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.base.Form.AnimationEffectForm;
import com.aliyun.svideo.common.utils.LanguageUtils;
import com.aliyun.svideo.common.utils.image.ImageLoaderImpl;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.imv.IMVPreviewDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 特效更多 adapter
 */
public class MoreTransitionEffectAdapter extends RecyclerView.Adapter<MoreTransitionEffectAdapter.ImvViewHolder> {
    private static final String TAG = MoreTransitionEffectAdapter.class.getName();

    private static final int VIEW_TYPE_NO = 0;
    private static final int VIEW_TYPE_LOCAL = 1;
    private static final int VIEW_TYPE_REMOTE = 2;
    private static final int VIEW_TYPE_DOWNLOADING = 3;

    private Context mContext;
    private List<EffectBody<AnimationEffectForm>> mDataList = new ArrayList<>();
    private ArrayList<AnimationEffectForm> mLoadingMV = new ArrayList<>(); //正在下载的的mv
    private Comparator<EffectBody<AnimationEffectForm>> mMVCompator = new AnimationFilterFormCompator();
    private OnItemRightButtonClickListener mRightBtnClickListener;

    public MoreTransitionEffectAdapter(Context context) {
        this.mContext = context;
    }


    class ImvViewHolder extends RecyclerView.ViewHolder {
        private TextView mTvName, mTvDesc;
        private TextView mTvRightButton;
        private ImageView mAfIcon;
        private ImageView downloadFinish;
        private EffectBody<AnimationEffectForm> mData;
        private int mPosition;
        private ProgressBar progressView;

        public void updateData(int position, EffectBody<AnimationEffectForm> data) {
            this.mData = data;
            this.mPosition = position;
            AnimationEffectForm afForm = data.getData();
            String name = afForm.getName();
            if (!LanguageUtils.isCHEN(mContext) && afForm.getNameEn() != null) {
                name = afForm.getNameEn();
            }
            mTvName.setText(name);

            if (!LanguageUtils.isCHEN(mContext) && afForm.getNameEn() != null) {
                mTvDesc.setText(afForm.getDescriptionEn());
            } else {
                mTvDesc.setText(afForm.getDescription());
            }
            new ImageLoaderImpl().loadImage(mAfIcon.getContext(), afForm.getIconUrl()).into(mAfIcon);
        }

        public ImvViewHolder(View itemView) {
            super(itemView);
            mTvName = (TextView)itemView.findViewById(R.id.tv_name);
            mTvDesc = (TextView)itemView.findViewById(R.id.tv_desc);
            mAfIcon = (ImageView)itemView.findViewById(R.id.iv_icon);
            downloadFinish = (ImageView)itemView.findViewById(R.id.iv_download_finish);
            progressView = itemView.findViewById(R.id.download_progress);
            mTvRightButton = (TextView)itemView.findViewById(R.id.tv_right_button);
            mTvRightButton.setText(mContext.getResources().getString(R.string.alivc_common_download));
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
            downloadFinish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mRightBtnClickListener != null) {
                        mRightBtnClickListener.onLocalItemClick(mPosition, mData);
                    }
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mData.getData().getPreviewMediaUrl() != null && !"".equals(mData.getData().getPreviewMediaUrl())) {
                        IMVPreviewDialog imvDialog = IMVPreviewDialog.newInstance(mData.getData().getPreviewMediaUrl(),
                                                     mData.getData().getPreviewImageUrl());
                        imvDialog.show(((MoreMVActivity)mContext).getSupportFragmentManager(), "iMV");
                    }
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        int type = VIEW_TYPE_NO;
        if (position >= 0 && position < mDataList.size()) {
            EffectBody<AnimationEffectForm> data = mDataList.get(position);
            if (data.isLocal()) {
                return VIEW_TYPE_LOCAL;
            } else if (data.isLoading()) {
                return VIEW_TYPE_DOWNLOADING;
            } else {
                return VIEW_TYPE_REMOTE;
            }
        }
        return type;
    }

    @Override
    public ImvViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.alivc_editor_item_more_effect, parent, false);
        ImvViewHolder holder = new ImvViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ImvViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        holder.mTvRightButton.setVisibility(View.VISIBLE);
        holder.progressView.setVisibility(View.GONE);
        switch (viewType) {
        case VIEW_TYPE_LOCAL:
            holder.mTvRightButton.setVisibility(View.INVISIBLE);
            holder.downloadFinish.setVisibility(View.VISIBLE);
            holder.updateData(position, mDataList.get(position));
            break;
        case VIEW_TYPE_REMOTE:
            holder.mTvRightButton.setBackgroundResource(R.drawable.aliyun_svideo_shape_caption_manager_bg);
            holder.updateData(position, mDataList.get(position));
            break;
        case VIEW_TYPE_DOWNLOADING:
            holder.mTvRightButton.setText(mContext.getResources().getString(R.string.alivc_editor_more_downloading));
            holder.updateData(position, mDataList.get(position));
            break;
        default:
            break;
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public void updateProcess(ImvViewHolder viewHolder, int process, int position) {
        if (viewHolder != null && viewHolder.mPosition == position) {
            viewHolder.mTvRightButton.setBackgroundColor(Color.TRANSPARENT);
            viewHolder.progressView.setVisibility(View.VISIBLE);
            viewHolder.progressView.setProgress(process);
        }
    }

    public void notifyDownloadingStart(EffectBody<AnimationEffectForm> mvBody) {
        if (!mLoadingMV.contains(mvBody.getData())) {
            mLoadingMV.add(mvBody.getData());
            mvBody.setLoading(true);
        }
    }

    public synchronized void notifyDownloadingComplete(EffectBody<AnimationEffectForm> mvBody, int position) {
        mvBody.setLocal(true);
        mvBody.setLoading(false);
        mLoadingMV.remove(mvBody.getData());
        notifyDataSetChanged();
        //Collections.sort(mDataList, mMVCompator);
        //notifyItemChanged(position);
    }

    public void notifyDownloadingFailure(
        EffectBody<AnimationEffectForm> mvBody) {
        mvBody.setLocal(false);
        mvBody.setLoading(false);
        mLoadingMV.remove(mvBody.getData());
        notifyDataSetChanged();
    }

    public synchronized void syncData(List<EffectBody<AnimationEffectForm>> syncDataList) {
        if (mLoadingMV.size() != 0 ) {
            //在没有下载任务的时候才进行同步，避免出现任务进度更新时的position错乱
            return;
        }
        if (syncDataList == null) {
            return;
        }
        ArrayList<EffectBody<AnimationEffectForm>> delList = new ArrayList<>();
        for (EffectBody<AnimationEffectForm> item : mDataList) {
            if (!syncDataList.contains(item)) {
                delList.add(item);
            }
        }
        mDataList.removeAll(delList);
        for (EffectBody<AnimationEffectForm> item : syncDataList) {
            if (!mDataList.contains(item)) {
                mDataList.add(item);
            }
        }
        Collections.sort(mDataList, mMVCompator);
        notifyDataSetChanged();
    }

    interface OnItemRightButtonClickListener {
        void onRemoteItemClick(int position, EffectBody<AnimationEffectForm> data);

        void onLocalItemClick(int position, EffectBody<AnimationEffectForm> data);
    }

    public void setRightBtnClickListener(OnItemRightButtonClickListener listener) {
        this.mRightBtnClickListener = listener;
    }

    class AnimationFilterFormCompator implements Comparator<EffectBody<AnimationEffectForm>> {
        @Override
        public int compare(EffectBody<AnimationEffectForm> o1, EffectBody<AnimationEffectForm> o2) {
            if (o1 == null && o2 == null) {
                return 0; // o1 = o2
            } else if (o1 == null && o2 != null) {
                return -1; // o1 < o2
            } else if (o1 != null && o2 == null) {
                return 1; //o1 > o2
            } else {
                if (o1.isLocal() && !o2.isLocal()) {
                    return -1; // o1 < o2
                } else if (!o1.isLocal() && o2.isLocal()) {
                    return 1;   //o1 > o2
                } else {//o1 is local && o2 is local
                    if (o1.getData() == null && o2.getData() == null) {
                        return 0; // o1 = o2
                    } else if (o1.getData() != null && o2.getData() == null) {
                        return 1;   //o1 > o2
                    } else if (o1.getData() == null && o2.getData() != null) {
                        return -1; //o1 < o2
                    } else {
                        AnimationEffectForm ep1 = o1.getData();
                        AnimationEffectForm ep2 = o2.getData();
                        if (ep1.getId() < ep2.getId()) {
                            return -1; // o1 < o2
                        } else if (ep1.getId() == ep2.getId()) {
                            return 0;   // o1 = o2
                        } else {
                            return 1;   // o1 > o2
                        }
                    }
                }
            }
        }
    }

}
