/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.media;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.svideo.base.MediaInfo;
import com.aliyun.demo.importer.R;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
                            implements View.OnClickListener{

    public interface OnItemClickListener {
        /**
         * item click listener
         * @param adapter GalleryAdapter
         * @param adapterPosition adapterPosition
         * @return
         */
        boolean onItemClick(GalleryAdapter adapter, int adapterPosition);
    }

    private static final int TYPE_ITEM_DRAFT = 0;
    private static final int TYPE_ITEM_MEDIA = 1;
    private List<MediaInfo> medias;
//    private boolean hasDraft;
    private int draftCount;
    private ThumbnailGenerator thumbnailGenerator;
    private OnItemClickListener onItemClickListener;

    public GalleryAdapter(ThumbnailGenerator thumbnailGenerator){
        this.thumbnailGenerator = thumbnailGenerator;
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
                            R.layout.aliyun_svideo_import_item_qupai_gallery_media, parent, false), thumbnailGenerator);

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
        return medias.get(position);
//        }else {
//            return null;
//        }
    }

    @Override
    public int getItemViewType(int position) {
//        if(hasDraft && position == 0){
//            return TYPE_ITEM_DRAFT;
//        }
        return TYPE_ITEM_MEDIA;
    }

    public int setActiveDataItem(MediaInfo info){
        return setActiveDataItem(info == null ? -1 : info.id);
    }

    public int setActiveDataItem(int id){
        int dataPos = findDataPosition(id);
        setActiveAdapterItem(dataPos);
        return dataPos;
    }

    private int activeAdapterPosition = 0;
    private void setActiveAdapterItem(int adapterPos) {
        int oldAdapterPos = activeAdapterPosition;
        if (oldAdapterPos == adapterPos) {
            return;
        }

        activeAdapterPosition = adapterPos;
        notifyItemChanged(adapterPos);
//        notifyItemChanged(old_adapter_pos);
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
         return medias.size();
    }

    @Override
    public void onClick(View v) {
        RecyclerView.ViewHolder holder = (RecyclerView.ViewHolder) v.getTag();
        int adapterPos = holder.getAdapterPosition();

        if (onItemClickListener != null) {
            Log.d("active", "onItemClick");
            if (!onItemClickListener.onItemClick(this, adapterPos)) {
                Log.d("active","onItemClick1");
                return;
            }
        }
        setActiveAdapterItem(adapterPos);
    }
}
