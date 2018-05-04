/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.crop.media;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;



import java.util.List;

/**
 * Created by Administrator on 2016/5/18.
 */
public class GalleryMediaChooser {

    private RecyclerView mGallery;
    private GalleryAdapter adapter;
    private MediaStorage mStorage;
    public static final String RECORD_TYPE = "record";

    public GalleryMediaChooser(RecyclerView gallery,
                               final GalleryDirChooser dirChooser,
                               MediaStorage storage, ThumbnailGenerator thumbnailGenerator,boolean needRecord){
        this.mGallery = gallery;
        mGallery.addItemDecoration(new GalleryItemDecoration());
        this.mStorage = storage;
        adapter = new GalleryAdapter(thumbnailGenerator,needRecord);
        gallery.setLayoutManager(new WrapContentGridLayoutManager(gallery.getContext(),
                4, GridLayoutManager.VERTICAL, false));
        gallery.setAdapter(adapter);
//        adapter.addDraftItem();
        adapter.setData(storage.getMedias());
        storage.setOnMediaDataUpdateListener(new MediaStorage.OnMediaDataUpdate() {
            @Override
            public void onDataUpdate(List<MediaInfo> list) {
                    int count = adapter.getItemCount();
                    int size = list.size();
                    int insert = count - size;
                    adapter.notifyItemRangeInserted(insert, size);

                    if (size == MediaStorage.FIRST_NOTIFY_SIZE
                            || mStorage.getMedias().size() < MediaStorage.FIRST_NOTIFY_SIZE) {
                        selectedFirstMediaOnAll(list);
                    }
                dirChooser.setAllGalleryCount(mStorage.getMedias().size());

            }
        });

        adapter.setOnItemClickListener(new GalleryAdapter.OnItemClickListener() {
            @Override
            public boolean onItemClick(GalleryAdapter adapter, int adapter_position) {
                MediaInfo info = adapter.getItem(adapter_position);
//                if (info == null) {
////                    mStorage.onDraftItemClicked();
//                } else {
                    mStorage.setCurrentDisplayMediaData(info);
//                }

                return true;
            }
        });



        mGallery.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                GridLayoutManager layout = (GridLayoutManager) recyclerView.getLayoutManager();
                int first = layout.findFirstCompletelyVisibleItemPosition();

            }
        });

        mGallery.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
            }
        });
    }

    public void setCurrentMediaInfoActived(){
        int pos = adapter.setActiveDataItem(mStorage.getCurrentMedia());
        mGallery.smoothScrollToPosition(pos);
    }

    public void setDraftCount(int draftCount){
        adapter.setDraftCount(draftCount);
        adapter.notifyItemChanged(0);
    }

    private void selectedFirstMediaOnAll(List<MediaInfo> list){
        if(list.size() == 0){
            return ;
        }
        MediaInfo info = list.get(0);
//        mStorage.setCurrentDisplayMediaData(info);
        adapter.setActiveDataItem(info);
    }

    public void changeMediaDir(MediaDir dir){
        if(dir.id == -1){
            List<MediaInfo> data = mStorage.getMedias();
            adapter.setData(data);
            selectedFirstMediaOnAll(mStorage.getMedias());
        }else{
            List<MediaInfo> data = mStorage.findMediaByDir(dir);
            adapter.setData(data);
            selectedFirstMediaOnAll(mStorage.findMediaByDir(dir));
        }
    }

    public RecyclerView getGallery(){
        return mGallery;
    }

}
