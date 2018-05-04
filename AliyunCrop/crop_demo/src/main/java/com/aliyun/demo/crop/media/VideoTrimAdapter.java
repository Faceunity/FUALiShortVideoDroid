/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.crop.media;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.aliyun.demo.crop.R;
import com.aliyun.quview.SquareFrameLayout;
import com.aliyun.quview.VideoSliceSeekBar;

import java.util.concurrent.TimeUnit;



public class VideoTrimAdapter extends BaseAdapter {
    private Context mContext;
    private float mDuration;
    private int mDurationLimit;
    private FrameExtractor10 mKFrame;
    private VideoSliceSeekBar mSeekBar;
    private float itemWidth = 0;

    private int screenWidth;
    private int right;
    private int maxRight;
    private float perSecond;

    private boolean thumbFlag;

    private int fraction;

    public VideoTrimAdapter(Context context, long duration, int durationLimit,
                            FrameExtractor10 kFrame, VideoSliceSeekBar seekBar) {
        mContext = context;
        mDuration = duration;
        mDurationLimit = durationLimit;
        mKFrame = kFrame;
        mSeekBar = seekBar;
        thumbFlag = false;

        WindowManager wm = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);

        screenWidth = wm.getDefaultDisplay().getWidth();
        right = screenWidth / mDurationLimit;
    }

    public float getItemWidth() {
        return itemWidth;
    }

    public float getPerItemOfSecond() {
        return perSecond;
    }

    public int getCurrentLeftItem(int left) {
        int item = -1;
        float position = (float) left / itemWidth;
        String str = String.valueOf(position);
        String intStr = str.substring(0, str.lastIndexOf("."));
        String pointStr = str.substring(str.lastIndexOf("."));

        if (!intStr.equals("0") && !pointStr.equals("0")) {
            item = Integer.parseInt(intStr) + 1;
        } else {
            item = Integer.parseInt(intStr);
        }

        return item;
    }

    public int getCurrentRightItem(int right) {
        int item = -1;
        float position = (float) right / itemWidth;
        String str = String.valueOf(position);
        String intStr = str.substring(0, str.lastIndexOf("."));

        item = Integer.parseInt(intStr) - 1;

        return item;
    }

    private int getItemCount() {
//        float count;
//        float c = (mDuration / mDurationLimit) * 8;
//        count = c / 1000;
//        return (int) Math.round(count);
        if((int)(mDuration/1000) > mDurationLimit){
            return  Math.round((mDuration /1000/ mDurationLimit) * 8);
        }else{
            return 8;
        }

    }
    public void setTimeLimit(int timeLimit){
        mDurationLimit = timeLimit;
    }

    @Override
    public int getCount() {
        return getItemCount();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();

            convertView = LayoutInflater.from(mContext).inflate( R.layout.aliyun_svideo_item_qupai_trim_video_thumbnail, parent, false);
            holder.thumblayout = (SquareFrameLayout) convertView.findViewById(R.id.aliyun_video_tailor_frame);
            holder.thumbImage = (ImageView) convertView.findViewById(R.id.aliyun_video_tailor_img_item);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            holder.task.cancel(false);
            holder.thumbImage.setImageBitmap(null);
            if (holder.mBitmap != null) {
                holder.mBitmap.release();
                holder.mBitmap = null;
            }
        }

//        float perWidth = screenWidth / mDurationLimit;

//        BigDecimal bd = new BigDecimal(mDuration / 1000f);
//        float second = bd.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
        perSecond = mDuration / getItemCount()/1000;

        ViewGroup.LayoutParams lParam = holder.thumblayout.getLayoutParams();

        lParam.width = screenWidth / 8;
        itemWidth = lParam.width;

        maxRight = Math.round((getItemCount() * itemWidth));

        holder.thumblayout.setLayoutParams(lParam);
        holder.task = mKFrame.newTask(holder, TimeUnit.SECONDS.toNanos((long) (position * perSecond)));

        return convertView;
    }

    class ViewHolder implements FrameExtractor10.Callback {
        public SquareFrameLayout thumblayout;
        public ImageView thumbImage;
        public ShareableBitmap mBitmap;
        public AsyncTask<?, ?, ?> task;

        @Override
        public void onFrameExtracted(ShareableBitmap bitmap, long timestamp_nano) {
            if (bitmap != null) {
                mBitmap = bitmap;
                thumbImage.setImageBitmap(bitmap.getData());
            }

            //if (right < screenWidth + screenWidth / mDurationLimit) {
            //    if (mDuration > mDurationLimit * 1000) {
            //        if (!thumbFlag) {
            //            thumbFlag = true;
            //
            //            mSeekBar.setLeftProgress(0);
            //            mSeekBar.setRightProgress(100);
            //            mSeekBar.setThumbMaxSliceRightx(screenWidth);
            //        }
            //    } else {
            //        if (!thumbFlag) {
            //            thumbFlag = true;
            //
            //            mSeekBar.setLeftProgress(0);
            //            int progress = Math.round(maxRight * 100f / screenWidth);
            //            mSeekBar.setRightProgress(progress);
            //            mSeekBar.setThumbMaxSliceRightx(maxRight);
            //        }
            //    }
            //
            //    right += screenWidth / mDurationLimit;
            //}
        }
    }
}
