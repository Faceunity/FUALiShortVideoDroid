/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.importer.media;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.aliyun.demo.importer.R;


public class GalleryDirChooser {

    private PopupWindow popupWindow;
    private View anchor;
    private boolean isShowGalleryDir;
    private GalleryDirAdapter adapter;

    public GalleryDirChooser(Context context, View anchor,
                             ThumbnailGenerator thumbnailGenerator,
                             final MediaStorage storage){
        this.anchor = anchor;
        RecyclerView recyclerView = (RecyclerView)View.inflate(context,
                R.layout.aliyun_svideo_import_layout_qupai_effect_container_normal, null);
        adapter = new GalleryDirAdapter(thumbnailGenerator);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(adapter);
        adapter.setData(storage.getDirs());
        popupWindow = new PopupWindow(recyclerView,
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(new ColorDrawable(
                context.getResources().getColor(android.R.color.white)));
        popupWindow.setOutsideTouchable(true);

        anchor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(storage.isActive()){
                    showOrHideGalleryDir();
                }
            }
        });

        storage.setOnMediaDirUpdateListener(
                new MediaStorage.OnMediaDirUpdate() {
                    @Override
                    public void onDirUpdate(MediaDir dir) {
                        GalleryDirChooser.this.anchor.post(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                        );

                    }
                }
        );

        adapter.setOnItemClickListener(new GalleryDirAdapter.OnItemClickListener() {
            @Override
            public boolean onItemClick(GalleryDirAdapter adapter, int adapter_position) {
                MediaDir dir = adapter.getItem(adapter_position);
                showOrHideGalleryDir();
                storage.setCurrentDir(dir);
                return false;
            }
        });
    }

    public void setAllGalleryCount(int count){
        adapter.setAllFileCount(count);
    }

    public void showOrHideGalleryDir(){
        if(isShowGalleryDir){
            popupWindow.dismiss();
        }else{
            if (Build.VERSION.SDK_INT < 24) {
                popupWindow.showAsDropDown(anchor);
            }
            else {
                // 适配 android 7.0
                int[] location = new int[2];
                anchor.getLocationOnScreen(location);
                int x = location[0];
                int y = location[1];
                popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, 0, y + anchor.getHeight());
            }
        }
        isShowGalleryDir = !isShowGalleryDir;
        anchor.setActivated(isShowGalleryDir);
    }

    public boolean isShowGalleryDir() {
        return isShowGalleryDir;
    }

}
