/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.effects.audiomix;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.common.logger.Logger;
import com.aliyun.common.utils.DensityUtil;
import com.aliyun.demo.editor.R;
import com.aliyun.demo.effects.control.EffectInfo;
import com.aliyun.demo.effects.control.OnItemClickListener;
import com.aliyun.demo.effects.control.UIEditorPage;
import com.aliyun.downloader.DownloaderManager;
import com.aliyun.downloader.FileDownloaderCallback;
import com.aliyun.downloader.FileDownloaderModel;
import com.aliyun.quview.CircleProgressBar;
import com.aliyun.struct.form.MusicForm;

import java.util.ArrayList;


public class OnlineAudioMixAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {
    private Context mContext;
    private OnItemClickListener mItemClick;
    private ArrayList<MusicForm> data = new ArrayList<>();
    private int selectedIndex = 0;
    private static final int MUSIC_TYPE = 5;
    private RecyclerView mRecyclerView;

    public OnlineAudioMixAdapter(Context context, RecyclerView recyclerView) {
        mContext = context;
        mRecyclerView = recyclerView;
    }

    public void setData(ArrayList<MusicForm> data) {
        if (data == null) {
            return;
        }
        this.data = data;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClick = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.aliyun_svideo_music_item_view, parent, false);
        AudioMixViewHolder audioMixViewHolder = new AudioMixViewHolder(view);
        audioMixViewHolder.musicName = (TextView) view.findViewById(R.id.music_name);
        audioMixViewHolder.musicType = (TextView) view.findViewById(R.id.music_type);
        audioMixViewHolder.musicType.setVisibility(View.GONE);
        audioMixViewHolder.selectFlag = (ImageView) view.findViewById(R.id.selected_flag);
        audioMixViewHolder.downloadBtn = (ImageView) view.findViewById(R.id.download_btn);
        audioMixViewHolder.downloadBtn.setVisibility(View.VISIBLE);
        audioMixViewHolder.downloadProgress = (CircleProgressBar) view.findViewById(R.id.download_progress);
        int width = DensityUtil.dip2px(view.getContext(), 25);
        audioMixViewHolder.downloadProgress.setBackgroundWidth(width, width);
        audioMixViewHolder.downloadProgress.setProgressWidth(width);
        audioMixViewHolder.downloadProgress.isFilled(true);
        return audioMixViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        AudioMixViewHolder audioMixViewHolder = (AudioMixViewHolder) holder;
        MusicForm musicForm = data.get(position);
        if (audioMixViewHolder != null) {
            audioMixViewHolder.itemView.setOnClickListener(this);
            audioMixViewHolder.itemView.setTag(audioMixViewHolder);
            if(musicForm.getName() == null && musicForm.getUrl() == null){
                audioMixViewHolder.musicName.setText(R.string.empty_music);
            }else{
                audioMixViewHolder.musicName.setText(musicForm.getName());
            }
            String path = DownloaderManager.getInstance().getDbController().getPathByUrl(data.get(position).getUrl());
            if (path != null && !path.isEmpty()) {
                audioMixViewHolder.downloadBtn.setVisibility(View.GONE);
                audioMixViewHolder.musicType.setVisibility(View.VISIBLE);
            } else {
                audioMixViewHolder.downloadBtn.setVisibility(View.VISIBLE);
                audioMixViewHolder.musicType.setVisibility(View.GONE);
            }
            if(musicForm.getUrl() == null){
                audioMixViewHolder.downloadBtn.setVisibility(View.GONE);
                audioMixViewHolder.musicType.setVisibility(View.GONE);
            }
            if(position == selectedIndex){
                audioMixViewHolder.selectFlag.setSelected(true);
            }else{
                audioMixViewHolder.selectFlag.setSelected(false);
            }
        }
    }
    public void clearSelect(){
        int lastIndex = selectedIndex;
        selectedIndex = Integer.MAX_VALUE;
        notifyItemChanged(lastIndex);
    }

    private void downloadMusic(final MusicForm form,final AudioMixViewHolder holder,final int position){
        FileDownloaderModel model = new FileDownloaderModel();
        model.setUrl(form.getUrl());
        model.setName(form.getName());
        model.setId(form.getId());
        model.setCategory(form.getCategory());
        model.setEffectType(MUSIC_TYPE);
        FileDownloaderModel fileMode = DownloaderManager.getInstance().addTask(model, model.getUrl());
        DownloaderManager.getInstance().startTask(fileMode.getTaskId(), new FileDownloaderCallback() {
            @Override
            public void onProgress(int downloadId, long soFarBytes, long totalBytes, long speed, final int progress) {
                Logger.getDefaultLogger().d("downloadId..." + downloadId + "  progress..." + progress);
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AudioMixViewHolder updateHolder = (AudioMixViewHolder)mRecyclerView.findViewHolderForAdapterPosition(position);
                        if(updateHolder != null) {
                            updateHolder.downloadProgress.setVisibility(View.VISIBLE);
                            updateHolder.downloadProgress.setProgress(progress);
                            updateHolder.downloadBtn.setVisibility(View.GONE);
                        }
                    }
                });
            }

            @Override
            public void onFinish(int downloadId, String path) {
                Logger.getDefaultLogger().d("downloadId..." + downloadId + "  path..." + path);
                AudioMixViewHolder updateHolder = (AudioMixViewHolder)mRecyclerView.findViewHolderForAdapterPosition(position);
                if(updateHolder != null) {
                    updateHolder.downloadProgress.setVisibility(View.GONE);
                    updateHolder.musicType.setVisibility(View.VISIBLE);
                    if (mItemClick != null) {
                        EffectInfo info = new EffectInfo();
                        info.type = UIEditorPage.AUDIO_MIX;
                        info.setPath(path);
                        info.id = form.getId();
                        selectedIndex = position;
                        notifyDataSetChanged();
                        mItemClick.onItemClick(info, position);
                    }
                }
            }
        });
    }

    public void setSelectedIndex(int index){
        selectedIndex = index;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onClick(View v) {
        //WeakReference<Activity> weak = new WeakReference<Activity>((EditorActivity)mContext);
        //EditorActivity activity = (EditorActivity) weak.get();

        //if(activity != null) {
        //    if(activity.getEditor().getMVLastApplyId() != 0) {
        //        activity.showMessage(R.string.mv_message);
        //        return;
        //    }
        //}
        final AudioMixViewHolder holder = (AudioMixViewHolder) v.getTag();
        final int position = holder.getAdapterPosition();
        if(position == -1){
            return;
        }
        selectedIndex = position;
        final MusicForm form = data.get(position);
        String path = DownloaderManager.getInstance().getDbController().getPathByUrl(data.get(position).getUrl());
        if(path == null){
            if(form.getUrl() == null){
                if (mItemClick != null) {
                    EffectInfo info = new EffectInfo();
                    info.type = UIEditorPage.AUDIO_MIX;
                    info.setPath(null);
                    mItemClick.onItemClick(info, position);
                }
            }else {
                downloadMusic(form,holder,position);
            }
        }else{
            if (mItemClick != null) {
                EffectInfo info = new EffectInfo();
                info.type = UIEditorPage.AUDIO_MIX;
                info.setPath(path);
                info.id = form.getId();
                mItemClick.onItemClick(info, position);
            }
        }
        notifyDataSetChanged();
    }

    private static class AudioMixViewHolder extends RecyclerView.ViewHolder {

        ImageView selectFlag;
        TextView musicName;
        TextView musicType;
        ImageView downloadBtn;
        CircleProgressBar downloadProgress;

        public AudioMixViewHolder(View itemView) {
            super(itemView);
        }
    }
}
