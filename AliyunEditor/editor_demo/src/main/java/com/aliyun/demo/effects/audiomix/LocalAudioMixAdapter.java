/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.effects.audiomix;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.demo.editor.R;
import com.aliyun.demo.effects.control.EffectInfo;
import com.aliyun.demo.effects.control.OnItemClickListener;
import com.aliyun.demo.effects.control.UIEditorPage;

import java.util.ArrayList;


public class LocalAudioMixAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener{
    private Context mContext;
    private OnItemClickListener mItemClick;
    private int selectedIndex = Integer.MAX_VALUE;
    private ArrayList<MusicQuery.MediaEntity> data = new ArrayList<>();

    public LocalAudioMixAdapter(Context context) {
        mContext = context;
    }

    public void setData(ArrayList<MusicQuery.MediaEntity> data){
        this.data = data;
        MusicQuery.MediaEntity mediaEntity = new MusicQuery.MediaEntity();
        data.add(0,mediaEntity);
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
        audioMixViewHolder.musicType.setVisibility(View.VISIBLE);
        audioMixViewHolder.selectFlag = (ImageView) view.findViewById(R.id.selected_flag);
        audioMixViewHolder.downloadBtn = (ImageView) view.findViewById(R.id.download_btn);
        audioMixViewHolder.downloadBtn.setVisibility(View.GONE);
        return audioMixViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        AudioMixViewHolder audioMixViewHolder = (AudioMixViewHolder) holder;
        MusicQuery.MediaEntity mediaEntity = data.get(position);
        if(audioMixViewHolder != null){
            audioMixViewHolder.itemView.setOnClickListener(this);
            audioMixViewHolder.itemView.setTag(audioMixViewHolder);
            if(mediaEntity.display_name == null && mediaEntity.path == null){
                audioMixViewHolder.musicName.setText(R.string.empty_music);
            }else{
                audioMixViewHolder.musicName.setText(mediaEntity.display_name);
            }
            if(position == selectedIndex){
                audioMixViewHolder.selectFlag.setSelected(true);
            }else{
                audioMixViewHolder.selectFlag.setSelected(false);
            }
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onClick(View v) {
        //WeakReference<Activity> weak = new WeakReference<Activity>((EditorActivity)mContext);
        //EditorActivity activity = (EditorActivity) weak.get();
        //
        //if(activity != null) {
        //    if(activity.getEditor().getMVLastApplyId() != 0) {
        //        activity.showMessage(R.string.mv_message);
        //        return;
        //    }
        //}
        AudioMixViewHolder holder = (AudioMixViewHolder) v.getTag();
        int position = holder.getAdapterPosition();
        selectedIndex = position;
        String path = data.get(position).path;
        EffectInfo info = new EffectInfo();
        info.type = UIEditorPage.AUDIO_MIX;
        info.setPath(path);
        info.isLocalMusic = true;
        info.id = data.get(position).display_name == null ? 0:data.get(position).display_name.hashCode();
        if(mItemClick != null){
            mItemClick.onItemClick(info,position);
        }
        notifyDataSetChanged();
    }

    public void clearSelect(){
        int lastIndex = selectedIndex;
        selectedIndex = Integer.MAX_VALUE;
        notifyItemChanged(lastIndex);
    }

    public void setSelectedIndex(int index){
        selectedIndex = index;
        notifyDataSetChanged();
    }

    private static class AudioMixViewHolder extends RecyclerView.ViewHolder{

        ImageView selectFlag;
        TextView musicName;
        TextView musicType;
        ImageView downloadBtn;

        public AudioMixViewHolder(View itemView) {
            super(itemView);
        }
    }
}
