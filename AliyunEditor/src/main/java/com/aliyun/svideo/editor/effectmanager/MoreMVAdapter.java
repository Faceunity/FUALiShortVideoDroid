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

import com.aliyun.svideo.base.Form.IMVForm;
import com.aliyun.svideo.common.utils.LanguageUtils;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.imv.IMVPreviewDialog;
import com.aliyun.svideo.common.utils.image.ImageLoaderImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * MV更多 adapter
 */
public class MoreMVAdapter extends RecyclerView.Adapter<MoreMVAdapter.ImvViewHolder> {
    private static final String TAG = MoreMVAdapter.class.getName();

    private static final int VIEW_TYPE_NO = 0;
    private static final int VIEW_TYPE_LOCAL = 1;
    private static final int VIEW_TYPE_REMOTE = 2;
    private static final int VIEW_TYPE_DOWNLOADING = 3;

    private Context mContext;
    private List<EffectBody<IMVForm>> mDataList = new ArrayList<>();
    private ArrayList<IMVForm> mLoadingMV = new ArrayList<>(); //正在下载的的mv
    private Comparator<EffectBody<IMVForm>> mMVCompator = new IMVFormCompator();
    private OnItemRightButtonClickListener mRightBtnClickListener;

    public MoreMVAdapter(Context context) {
        this.mContext = context;
    }


    class ImvViewHolder extends RecyclerView.ViewHolder {
        private TextView mTvName, mTvDesc;
        private TextView mTvRightButton;
        private ImageView mIvIcon;
        private ImageView downloadFinish;
        private EffectBody<IMVForm> mData;
        private int mPosition;
        private ProgressBar progressView;

        public void updateData(int position, EffectBody<IMVForm> data) {
            this.mData = data;
            this.mPosition = position;
            IMVForm paster = data.getData();
            String name = paster.getName();
            if (!LanguageUtils.isCHEN(mContext) && paster.getNameEn() != null) {
                name = paster.getNameEn();
            }
            mTvName.setText(name);
            mTvDesc.setText(paster.getTag());
            new ImageLoaderImpl().loadImage(mIvIcon.getContext(), paster.getIcon()).into(mIvIcon);
        }

        public ImvViewHolder(View itemView) {
            super(itemView);
            mTvName = (TextView)itemView.findViewById(R.id.tv_name);
            mTvDesc = (TextView)itemView.findViewById(R.id.tv_desc);
            mIvIcon = (ImageView)itemView.findViewById(R.id.iv_icon);
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
                    if (mData.getData().getPreviewMp4() != null && !"".equals(mData.getData().getPreviewMp4())) {
                        IMVPreviewDialog imvDialog = IMVPreviewDialog.newInstance(mData.getData().getPreviewMp4(),
                                                     mData.getData().getPreviewPic());
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
            EffectBody<IMVForm> data = mDataList.get(position);
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

    public void notifyDownloadingStart(EffectBody<IMVForm> mvBody) {
        if (!mLoadingMV.contains(mvBody.getData())) {
            mLoadingMV.add(mvBody.getData());
            mvBody.setLoading(true);
        }
    }

    public synchronized void notifyDownloadingComplete(EffectBody<IMVForm> mvBody, int position) {
        mvBody.setLocal(true);
        mvBody.setLoading(false);
        mLoadingMV.remove(mvBody.getData());
        notifyDataSetChanged();
        //Collections.sort(mDataList, mMVCompator);
        //notifyItemChanged(position);
    }

    public void notifyDownloadingFailure(
        EffectBody<IMVForm> mvBody) {
        mvBody.setLocal(false);
        mvBody.setLoading(false);
        mLoadingMV.remove(mvBody.getData());
        notifyDataSetChanged();
    }

    public synchronized void syncData(List<EffectBody<IMVForm>> syncDataList) {
        if (mLoadingMV.size() != 0 ) {
            //在没有下载任务的时候才进行同步，避免出现任务进度更新时的position错乱
            return;
        }
        if (syncDataList == null) {
            return;
        }
        ArrayList<EffectBody<IMVForm>> delList = new ArrayList<>();
        for (EffectBody<IMVForm> item : mDataList) {
            if (!syncDataList.contains(item)) {
                delList.add(item);
            }
        }
        mDataList.removeAll(delList);
        for (EffectBody<IMVForm> item : syncDataList) {
            if (!mDataList.contains(item)) {
                mDataList.add(item);
            }
        }
        Collections.sort(mDataList, mMVCompator);
        notifyDataSetChanged();
    }

    interface OnItemRightButtonClickListener {
        void onRemoteItemClick(int position, EffectBody<IMVForm> data);

        void onLocalItemClick(int position, EffectBody<IMVForm> data);
    }

    public void setRightBtnClickListener(OnItemRightButtonClickListener listener) {
        this.mRightBtnClickListener = listener;
    }

    class IMVFormCompator implements Comparator<EffectBody<IMVForm>> {
        @Override
        public int compare(EffectBody<IMVForm> o1, EffectBody<IMVForm> o2) {
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
                        IMVForm ep1 = o1.getData();
                        IMVForm ep2 = o2.getData();
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
