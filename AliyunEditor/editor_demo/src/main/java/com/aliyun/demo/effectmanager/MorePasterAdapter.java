/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.effectmanager;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MorePasterAdapter extends RecyclerView.Adapter<MorePasterAdapter.PasterViewHolder> {
    private static final String TAG = MorePasterAdapter.class.getName();

	private static final int VIEW_TYPE_NO = 0;
    private static final int VIEW_TYPE_LOCAL = 1;
    private static final int VIEW_TYPE_REMOTE = 2;
    private static final int VIEW_TYPE_DOWNLOADING = 3;

    private Context mContext;
    private List<EffectBody<ResourceForm>> mDataList = new ArrayList<>();
    private ArrayList<ResourceForm> mLoadingPaster = new ArrayList<>(); //正在下载的的paster
    private Comparator<EffectBody<ResourceForm>> mPasterCompator = new ResourceFormCompator();
    private OnItemRightButtonClickListener mRightBtnClickListener;

    public MorePasterAdapter(Context context) {
        this.mContext = context;
    }

    public class PasterViewHolder extends RecyclerView.ViewHolder {
        private TextView mTvName, mTvDesc, mTvRightButton;
        private ImageView mIvIcon;
        private EffectBody<ResourceForm> mData;
        private int mPosition;
        private CircleProgressBar downloadProgress;

        public void updateData(int position, EffectBody<ResourceForm> data) {
            this.mData = data;
            this.mPosition = position;
            ResourceForm paster = data.getData();
            mTvName.setText(paster.getName());
            mTvDesc.setText(paster.getDescription());
            Glide.with(mIvIcon.getContext()).load(paster.getIcon()).into(mIvIcon);
        }

        public PasterViewHolder(final View itemView) {
            super(itemView);
            mTvName = (TextView) itemView.findViewById(R.id.tv_name);
            mTvDesc = (TextView) itemView.findViewById(R.id.tv_desc);
            mTvRightButton = (TextView) itemView.findViewById(R.id.tv_right_button);
            mIvIcon = (ImageView) itemView.findViewById(R.id.iv_icon);
            downloadProgress = (CircleProgressBar) itemView.findViewById(R.id.download_progress);
            int width = DensityUtil.dip2px(itemView.getContext(), 25);
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
                        dialog.show(((MorePasterActivity) mContext).getSupportFragmentManager(), "aliyun_svideo_overlay");
                    }
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        int type = VIEW_TYPE_NO;
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
    public PasterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.aliyun_svideo_layout_effect_manager_list_item, parent, false);
        PasterViewHolder holder = new PasterViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(PasterViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        holder.mTvRightButton.setVisibility(View.VISIBLE);
        holder.downloadProgress.setVisibility(View.GONE);
        switch (viewType) {
            case VIEW_TYPE_LOCAL:
                holder.mTvRightButton.setText(R.string.use_effect_edit);
                holder.mTvRightButton.setBackgroundResource(R.drawable.aliyun_svideo_shape_more_paster_use_bg);
                holder.updateData(position, mDataList.get(position));
                break;
            case VIEW_TYPE_REMOTE:
                holder.mTvRightButton.setText(R.string.download_effect_edit);
                holder.mTvRightButton.setBackgroundResource(R.drawable.aliyun_svideo_shape_more_paster_download_bg);
                holder.updateData(position, mDataList.get(position));
                break;
            case VIEW_TYPE_DOWNLOADING:
                holder.mTvRightButton.setVisibility(View.GONE);
                holder.downloadProgress.setVisibility(View.VISIBLE);
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

    public void updateProcess(PasterViewHolder viewHolder, int process, int position) {
        if(viewHolder != null && viewHolder.mPosition == position) {
            viewHolder.mTvRightButton.setVisibility(View.GONE);
            viewHolder.downloadProgress.setVisibility(View.VISIBLE);
            viewHolder.downloadProgress.setProgress(process);
        }
    }

    public void notifyDownloadingStart(EffectBody<ResourceForm> pasterBody) {
        if(!mLoadingPaster.contains(pasterBody.getData())) {
            mLoadingPaster.add(pasterBody.getData());
            pasterBody.setLoading(true);
        }
    }

    public synchronized void notifyDownloadingComplete(EffectBody<ResourceForm> pasterBody, int position) {
        pasterBody.setLocal(true);
        pasterBody.setLoading(false);
        //Collections.sort(mDataList, mPasterCompator);
        mLoadingPaster.remove(pasterBody.getData());
        //notifyDataSetChanged();
        notifyItemChanged(position);
    }

    public synchronized void syncData(List<EffectBody<ResourceForm>> syncDataList) {
        if(syncDataList == null) { return ;}
        ArrayList<EffectBody<ResourceForm>> delList = new ArrayList<>();
        for(EffectBody<ResourceForm> item:mDataList) {
            if(!syncDataList.contains(item)) {
                delList.add(item);
            }
        }
        mDataList.removeAll(delList);
        for(EffectBody<ResourceForm> item:syncDataList) {
            if(!mDataList.contains(item)) {
                mDataList.add(item);
            }
        }
        Collections.sort(mDataList, mPasterCompator);
        notifyDataSetChanged();
    }

    interface OnItemRightButtonClickListener {
        void onRemoteItemClick(int position, EffectBody<ResourceForm> data);

        void onLocalItemClick(int position, EffectBody<ResourceForm> data);
    }

    public void setRightBtnClickListener(OnItemRightButtonClickListener listener) {
        this.mRightBtnClickListener = listener;
    }

    class ResourceFormCompator implements Comparator<EffectBody<ResourceForm>> {
        @Override
        public int compare(EffectBody<ResourceForm> o1, EffectBody<ResourceForm> o2) {
            if(o1 == null && o2 == null) {
                return 0; // o1 = o2
            }else if(o1 == null && o2 != null) {
                return -1; // o1 < o2
            }else if(o1 != null && o2 == null) {
                return 1; //o1 > o2
            }else {
                if(o1.isLocal() && !o2.isLocal()) {
                    return -1; // o1 < o2
                }else if(!o1.isLocal() && o2.isLocal()) {
                    return 1;   //o1 > o2
                }else {//o1 is local && o2 is local
                    if(o1.getData() == null && o2.getData() == null) {
                        return 0; // o1 = o2
                    }else if(o1.getData() != null && o2.getData() == null) {
                        return 1;   //o1 > o2
                    }else if(o1.getData() == null && o2.getData() != null) {
                        return -1; //o1 < o2
                    }else {
                        ResourceForm ep1 = o1.getData();
                        ResourceForm ep2 = o2.getData();
                        if(ep1.getId() < ep2.getId()) {
                            return -1; // o1 < o2
                        }else if(ep1.getId() == ep2.getId()) {
                            return 0;   // o1 = o2
                        }else {
                            return 1;   // o1 > o2
                        }
                    }
                }
            }
        }
    }

}
