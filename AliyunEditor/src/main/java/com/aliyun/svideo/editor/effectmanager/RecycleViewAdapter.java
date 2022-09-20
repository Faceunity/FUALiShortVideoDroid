/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effectmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.common.utils.LanguageUtils;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.downloader.DownloaderManager;
import com.aliyun.svideo.downloader.FileDownloaderModel;
import com.aliyun.svideo.common.utils.image.ImageLoaderImpl;

import java.util.List;

public class RecycleViewAdapter extends StateController.StateAdapter<RecycleViewAdapter.PasterViewHolder> {

    private List<FileDownloaderModel> list;
    private Context mContext;
    public RecycleViewAdapter(Context context, List<FileDownloaderModel> list) {
        this.list = list;
        this.mContext = context;
    }

    class PasterViewHolder extends RecyclerView.ViewHolder {
        private ProgressBar progressView;
        private TextView mTvName, mTvDesc;
        private ImageView mIvIcon;
        private FileDownloaderModel mData;
        private int mPosition;
        private TextView tvDelete;

        public void updateData(int position, FileDownloaderModel data) {
            this.mData = data;
            this.mPosition = position;
            String name = mData.getName();
            String desc = mData.getDescription();
            if (!LanguageUtils.isCHEN(mContext)) {
                name = mData.getNameEn() == null ? name : mData.getNameEn();
                desc = mData.getDescriptionEn() == null ? desc : mData.getDescriptionEn();
            }
            mTvName.setText(name);
            mTvDesc.setText(desc);
            new ImageLoaderImpl().loadImage(mIvIcon.getContext(), mData.getIcon()).into(mIvIcon);
        }

        public PasterViewHolder(View itemView) {
            super(itemView);
            mTvName = (TextView) itemView.findViewById(R.id.tv_name);
            mTvDesc = (TextView) itemView.findViewById(R.id.tv_desc);
            progressView = itemView.findViewById(R.id.download_progress);
            tvDelete = itemView.findViewById(R.id.tv_right_button);
            mIvIcon = (ImageView) itemView.findViewById(R.id.iv_icon);
            tvDelete.setText(mContext.getResources().getString(R.string.alivc_editor_manager_delete));
            tvDelete.setBackgroundResource(R.drawable.aliyun_svideo_shape_effect_manager_delete_bg);
            tvDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FileDownloaderModel model = list.remove(mPosition);
                    notifyDataSetChanged();
                    //删除资源
                    DownloaderManager.getInstance().deleteTask(model.getId());
                }
            });
            progressView.setVisibility(View.GONE);
            tvDelete.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public PasterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alivc_editor_item_more_effect, parent, false);
        return new PasterViewHolder(view);
    }


    @Override
    public void onBindViewHolder(PasterViewHolder holder, int position) {
        FileDownloaderModel msg = list.get(position);
        holder.updateData(position, msg);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
