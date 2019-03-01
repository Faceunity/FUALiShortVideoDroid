/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effectmanager;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.overlay.PasterPreviewDialog;
import com.aliyun.svideo.sdk.external.struct.form.ResourceForm;
import com.aliyun.video.common.utils.image.ImageLoaderImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 更多动图页面adapter
 */
public class MorePasterAdapter extends RecyclerView.Adapter<MorePasterAdapter.PasterViewHolder> {
    private static final String TAG = MorePasterAdapter.class.getName();


	public static final String DOWNLOAD_START = "download_start";
	public static final String DOWNLOAD_FINISH = "download_finish";
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

    @Override
    public int getItemViewType(int position) {
        int type = VIEW_TYPE_NO;
        if(position >= 0 && position < mDataList.size()) {
            EffectBody<ResourceForm> data = mDataList.get(position);
            if(data.isLocal()) {
                type = VIEW_TYPE_LOCAL;
            }else if(data.isLoading()) {
                type = VIEW_TYPE_DOWNLOADING;
            }else {
                type = VIEW_TYPE_REMOTE;
            }
        }
        return type;
    }

    @Override
    public PasterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.aliyun_svideo_layout_effect_manager_list_item, parent, false);

        return new PasterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PasterViewHolder holder, int position, List<Object> payloads) {
        synchronized (MorePasterAdapter.class){
            if (payloads == null || payloads.size() == 0) {
                super.onBindViewHolder(holder, position, payloads);
                return;
            }

            //当修改指定的条目时，调用此方法
            for (Object payload : payloads) {
                if (DOWNLOAD_START.equals(payload)) {
                    //开始下载
                    holder.mTvRightButton.setBackgroundResource(R.color.alivc_transparent);
                    holder.progressView.setVisibility(View.VISIBLE);
                    holder.progressView.setProgress(0);
                }else if(DOWNLOAD_FINISH.equals(payload)){
                    //完成下载
                    holder.mTvRightButton.setVisibility(View.INVISIBLE);
                    holder.downloadFinish.setVisibility(View.VISIBLE);
                    holder.progressView.setVisibility(View.INVISIBLE);
                    holder.progressView.setProgress(0);
                }
            }
        }
    }

    @Override
    public void onBindViewHolder(PasterViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        EffectBody<ResourceForm> effectBody = mDataList.get(position);
        switch (viewType) {
            case VIEW_TYPE_LOCAL://本地->完成
                holder.mTvRightButton.setVisibility(View.INVISIBLE);
                holder.downloadFinish.setVisibility(View.VISIBLE);
                holder.progressView.setVisibility(View.INVISIBLE);
                break;
            case VIEW_TYPE_REMOTE://服务器->下载
                holder.mTvRightButton.setText(R.string.download_effect_edit);
                holder.mTvRightButton.setVisibility(View.VISIBLE);
                holder.progressView.setVisibility(View.INVISIBLE);
                holder.downloadFinish.setVisibility(View.INVISIBLE);
                holder.mTvRightButton.setBackgroundResource(R.drawable.aliyun_svideo_shape_caption_manager_bg);

                break;
            case VIEW_TYPE_DOWNLOADING://下载中
                holder.mTvRightButton.setText(R.string.downloading_effect_edit);
                holder.mTvRightButton.setVisibility(View.VISIBLE);
                holder.mTvRightButton.setBackgroundResource(R.color.alivc_transparent);
                holder.progressView.setVisibility(View.VISIBLE);
                holder.downloadFinish.setVisibility(View.INVISIBLE);
                break;
            default:
                break;
        }
        holder.progressView.setProgress(0);
        if (position != holder.mPosition){
            //更新条目左边视图，以及mPosition和data
            holder.updateData(position, effectBody);
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public void updateProcess(PasterViewHolder viewHolder, int process, int position) {
        if(viewHolder != null && viewHolder.mPosition == position) {

            int progress = viewHolder.progressView.getProgress();
            if (progress > process){
                //Log.e(TAG, "updateProcess: xxxx : error  position " + progress + " -> " +process);
                return;
            }
            if (viewHolder.mData.isLocal()){
                //下载完成之后，progress继续乱跳的事件不响应
                return;
            }
            viewHolder.mTvRightButton.setText(R.string.downloading_effect_edit);
            viewHolder.mTvRightButton.setBackgroundColor(Color.TRANSPARENT);
            viewHolder.progressView.setVisibility(View.VISIBLE);
            viewHolder.progressView.setProgress(process);
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
            mLoadingPaster.remove(pasterBody.getData());
            notifyItemChanged(position,DOWNLOAD_FINISH);
    }

    public synchronized void syncData(List<EffectBody<ResourceForm>> syncDataList) {
        if (mLoadingPaster.size() != 0 ){
            //在没有下载任务的时候才进行同步，避免出现任务进度更新时的position错乱
            return;
        }
        if(syncDataList == null) {
            return ;
        }
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

    /**
     * 动图recyclerView holder
     */
    public class PasterViewHolder extends RecyclerView.ViewHolder {
        private TextView mTvName, mTvDesc;
        private TextView mTvRightButton;
        private ImageView mIvIcon;
        private ImageView downloadFinish;
        private EffectBody<ResourceForm> mData;
        private int mPosition = -1;
        private ProgressBar progressView;

        /**
         * 设置数据
         * @param position 角标
         * @param data 数据
         */
        public void updateData(int position, EffectBody<ResourceForm> data) {
            this.mData = data;
            this.mPosition = position;
            ResourceForm paster = data.getData();
            mTvName.setText(paster.getName());
            mTvDesc.setText(paster.getDescription());
            new ImageLoaderImpl().loadImage(mIvIcon.getContext(),paster.getIcon()).into(mIvIcon);
        }

        public PasterViewHolder(final View itemView) {
            super(itemView);
            mTvName = (TextView) itemView.findViewById(R.id.tv_name);
            mTvDesc = (TextView) itemView.findViewById(R.id.tv_desc);
            mIvIcon = (ImageView) itemView.findViewById(R.id.iv_icon);
            progressView = itemView.findViewById(R.id.download_progress);
            downloadFinish = (ImageView)itemView.findViewById(R.id.iv_download_finish);
            mTvRightButton = (TextView)itemView.findViewById(R.id.tv_right_button);
            mTvRightButton.setText(mContext.getResources().getString(R.string.download_effect_edit));
            //点击下载按钮
            mTvRightButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    if (mData.isLocal()) {
                        if (mRightBtnClickListener != null) {
                            mRightBtnClickListener.onLocalItemClick(mPosition, mData);
                        }
                    } else {
                        if (mRightBtnClickListener != null) {
                            mRightBtnClickListener.onRemoteItemClick(mPosition, mData);
                        }

                    }
                }
            });
            //点击下载完成按钮
            downloadFinish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mRightBtnClickListener != null) {
                        mRightBtnClickListener.onLocalItemClick(mPosition, mData);
                    }
                }
            });
            //显示预览
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mData.getData().getPreviewUrl() != null && !"".equals(mData.getData().getPreviewUrl())) {
                        PasterPreviewDialog dialog = PasterPreviewDialog.newInstance(mData.getData().getPreviewUrl(),
                            mData.getData().getName(), mData.getData().getId());
                        if (dialog != null){
                            dialog.show(((MorePasterActivity) mContext).getSupportFragmentManager(), "aliyun_svideo_overlay");
                        }
                    }
                }
            });
        }
    }

}
