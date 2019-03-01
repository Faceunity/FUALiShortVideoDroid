/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.media;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.aliyun.svideo.base.MediaInfo;
import com.aliyun.demo.importer.R;
import com.aliyun.video.common.utils.image.ImageLoaderImpl;
import com.aliyun.video.common.utils.image.ImageLoaderOptions;

import java.io.File;

/**
 * Created by Administrator on 2016/5/18.
 */
public class GalleryItemViewHolder extends RecyclerView.ViewHolder {

    private ImageView thumbImage;
    private TextView duration;
    private View durationLayoput;
    private ThumbnailGenerator thumbnailGenerator;
    private int mScreenWidth;

    public GalleryItemViewHolder(View itemView, ThumbnailGenerator thumbnailGenerator) {
        super(itemView);
        DisplayMetrics displayMetrics = itemView.getContext().getResources().getDisplayMetrics();
        mScreenWidth = displayMetrics.widthPixels;
        this.thumbnailGenerator = thumbnailGenerator;
        thumbImage = (ImageView) itemView.findViewById(R.id.draft_thumbnail);
        duration = (TextView) itemView.findViewById(R.id.draft_duration);
        durationLayoput = itemView.findViewById(R.id.duration_layoput);

        itemView.setTag(this);
    }

    public void setData(final MediaInfo info) {
        if (info == null) {
            return;
        }
        if (info.thumbnailPath != null
                && onCheckFileExsitence(info.thumbnailPath)) {
            String uri = "file://" + info.thumbnailPath;
            new ImageLoaderImpl().loadImage(thumbImage.getContext(), uri,
                                            new ImageLoaderOptions.Builder().override(mScreenWidth / 5, mScreenWidth / 5)
                                            .skipMemoryCache()
                                            .build()
                                           ).into(thumbImage);
        } else {
            thumbImage.setImageDrawable(new ColorDrawable(Color.GRAY));
            thumbnailGenerator.generateThumbnail(info.type, info.id, 0,
            new ThumbnailGenerator.OnThumbnailGenerateListener() {
                @Override
                public void onThumbnailGenerate(int key, Bitmap thumbnail) {
                    int currentKey = ThumbnailGenerator.generateKey(info.type, info.id);
                    if (key == currentKey) {
                        thumbImage.setImageBitmap(thumbnail);
                    }
                }
            });
        }

        int du = info.duration;
        if (du == 0) {
            durationLayoput.setVisibility(View.GONE);
        } else {
            durationLayoput.setVisibility(View.VISIBLE);
            onMetaDataUpdate(duration, du);
        }

    }

    public void onBind(MediaInfo info, boolean actived) {
        setData(info);
        itemView.setActivated(actived);
    }

    private boolean onCheckFileExsitence(String path) {
        boolean res = false;
        if (path == null) {
            return res;
        }

        File file = new File(path);
        if (file.exists()) {
            res = true;
        }

        return res;
    }


    private void onMetaDataUpdate(TextView view, int duration) {
        if (duration == 0) {
            return;
        }

        int sec = Math.round((float) duration / 1000);
        int min = sec / 60;
        sec %= 60;

        view.setText(String.format(String.format("%d:%02d", min, sec)));
    }

}
