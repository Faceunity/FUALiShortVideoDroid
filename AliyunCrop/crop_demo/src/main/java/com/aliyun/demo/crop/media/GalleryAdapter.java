/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.crop.media;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.aliyun.demo.crop.R;

import java.util.List;


public class GalleryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
                            implements View.OnClickListener{

    public interface OnItemClickListener {
        boolean onItemClick(GalleryAdapter adapter, int adapter_position);
    }

    private static final int TYPE_ITEM_RECORD = 0;
    private static final int TYPE_ITEM_MEDIA = 1;
    private List<MediaInfo> medias;
    private boolean needRecord = false;
    private int draftCount;
    private ThumbnailGenerator thumbnailGenerator;
    private OnItemClickListener onItemClickListener;

    public GalleryAdapter(ThumbnailGenerator thumbnailGenerator,boolean needRecord){
        this.thumbnailGenerator = thumbnailGenerator;
        this.needRecord = needRecord;
    }

    public void setData(List<MediaInfo> list){
        medias = list;
        notifyDataSetChanged();
    }

    public void setDraftCount(int draftCount) {
        this.draftCount = draftCount;
    }

//    public void addDraftItem(){
//        hasDraft = true;
//    }
//
//    public boolean isHasDraft() {
//        return hasDraft;
//    }
//
//    public void removeDraftItem(){
//        hasDraft = false;
//    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder;

        holder = new GalleryItemViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.aliyun_svideo_item_qupai_gallery_media, parent, false), thumbnailGenerator);

        holder.itemView.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        boolean actived = activeAdapterPosition == position;

        ((GalleryItemViewHolder)holder).onBind(getItem(position), actived);

    }

    public MediaInfo getItem(int position){
//        if(medias.size() > 0){
        if(getItemViewType(position) == TYPE_ITEM_RECORD){
            return null;
        }else if(getItemViewType(position) == TYPE_ITEM_MEDIA){
            if(needRecord){
                return medias.get(position - 1);
            }else{
                return medias.get(position);
            }
        }
        return medias.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        if(needRecord && position == 0){
            return TYPE_ITEM_RECORD;
        }
        return TYPE_ITEM_MEDIA;
    }

    public int setActiveDataItem(MediaInfo info){
        return setActiveDataItem(info == null ? -1 : info.id);
    }

    public int setActiveDataItem(int id){
        int data_pos = findDataPosition(id);
        setActiveAdapterItem(data_pos);
        return data_pos;
    }

    private int activeAdapterPosition = 0;
    private void setActiveAdapterItem(int adapter_pos) {
        int old_adapter_pos = activeAdapterPosition;
        if (old_adapter_pos == adapter_pos) {
            return;
        }

        activeAdapterPosition = adapter_pos;
        notifyItemChanged(adapter_pos);
        notifyItemChanged(old_adapter_pos);
    }

    public int getActiveAdapterPosition(){
        return activeAdapterPosition;
    }

    public int findDataPosition(int id){
        for(int i = 0; i < medias.size(); i++){
            MediaInfo info = medias.get(i);
            if(info.id == id){
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        if(needRecord){
            return medias.size()+1;
        }
         return medias.size();
    }

    @Override
    public void onClick(View v) {
        RecyclerView.ViewHolder holder = (RecyclerView.ViewHolder) v.getTag();
        int adapter_pos = holder.getAdapterPosition();

        if (onItemClickListener != null) {
            Log.d("active", "onItemClick");
            if (!onItemClickListener.onItemClick(this, adapter_pos)) {
                Log.d("active","onItemClick1");
                return;
            }
        }
        setActiveAdapterItem(adapter_pos);
    }
}
