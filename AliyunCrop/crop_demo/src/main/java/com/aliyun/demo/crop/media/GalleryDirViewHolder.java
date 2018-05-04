/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.crop.media;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.aliyun.demo.crop.R;



public class GalleryDirViewHolder extends RecyclerView.ViewHolder {

    private LinearLayout sortVideoLayout;
    private ImageView thumbImage;
    private TextView sortDirTxt;
    private TextView sortFileNum;
    private ThumbnailGenerator thumbnailGenerator;

    public GalleryDirViewHolder(View itemView, ThumbnailGenerator thumbnailGenerator) {
        super(itemView);
        sortVideoLayout = (LinearLayout) itemView.findViewById(R.id.aliyun_sort_video_layout);
        thumbImage = (ImageView) itemView.findViewById(R.id.aliyun_thumb_image);
        sortDirTxt = (TextView) itemView.findViewById(R.id.aliyun_video_dir_name);
        sortFileNum = (TextView) itemView.findViewById(R.id.aliyun_video_file_count);

        this.thumbnailGenerator = thumbnailGenerator;

        itemView.setTag(this);
    }

    public void setData(final MediaDir dir){
        String dir_name;
        if(dir.id == -1){
            dir_name = sortDirTxt.getResources().getString(R.string.aliyun_gallery_all_media);
        }else{
            dir_name = dir.dirName;
        }

        sortDirTxt.setText(dir_name);

        final int videoNum = dir.fileCount;
        sortFileNum.setText(String.valueOf(videoNum));

        if(dir.thumbnailUrl != null){
            String uri = "file://" + dir.thumbnailUrl;
            Glide.with(thumbImage.getContext()).load(uri).into(thumbImage);
//            ImageLoader.getInstance().displayImage(uri, thumbImage, UILOptions.DISK);
        }else{
            thumbImage.setImageDrawable(new ColorDrawable(Color.GRAY));
            thumbnailGenerator.generateThumbnail(dir.type, dir.id,dir.resId,
                    new ThumbnailGenerator.OnThumbnailGenerateListener() {
                        @Override
                        public void onThumbnailGenerate(int key, Bitmap thumbnail) {
                            int currentKey = ThumbnailGenerator.generateKey(dir.type, dir.id );
                            if(key == currentKey){
                                thumbImage.setImageBitmap(thumbnail);
                            }
                        }
                    });

//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inDither = false;
//            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//            Bitmap bitmap;
//            if(dir.type == Project.TYPE_VIDEO){
//                bitmap = MediaStore.Video.Thumbnails.getThumbnail(itemView.getContext().getContentResolver(),
//                        dir.id, MediaStore.Images.Thumbnails.MICRO_KIND, options);
//            }else{
//                bitmap = MediaStore.Images.Thumbnails.getThumbnail(itemView.getContext().getContentResolver(),
//                        dir.id, MediaStore.Images.Thumbnails.MICRO_KIND, options);
//            }
//
//            thumbImage.setImageBitmap(bitmap);
        }
    }

    public void setFileCountWhenCompletion(int count){
        sortFileNum.setText(String.valueOf(count));
    }

}
