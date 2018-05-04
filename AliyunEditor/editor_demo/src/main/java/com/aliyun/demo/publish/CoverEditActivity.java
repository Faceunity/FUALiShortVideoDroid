package com.aliyun.demo.publish;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aliyun.common.media.ShareableBitmap;
import com.aliyun.demo.editor.R;
import com.aliyun.qupai.editor.AliyunIThumbnailFetcher;
import com.aliyun.qupai.editor.AliyunThumbnailFetcherFactory;
import com.aliyun.qupai.editor.impl.AliyunThumbnailFetcher;
import com.aliyun.struct.common.ScaleMode;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by macpro on 2017/11/7.
 */

public class CoverEditActivity extends Activity implements View.OnClickListener{

    public static final String KEY_PARAM_VIDEO = "vidseo_path";
    public static final String KEY_PARAM_RESULT = "thumbnail";
    private ImageView mIvLeft, mIvRight;
    private ImageView mCoverImage;
    private TextView mTitle;
    private View mSlider;
    private LinearLayout mThumbnailList;

    private String mVideoPath;
    private AliyunIThumbnailFetcher mThumbnailFetcher;
    private AliyunIThumbnailFetcher mCoverFetcher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aliyun_svideo_activity_cover_edit);
        initView();
        mVideoPath = getIntent().getStringExtra(KEY_PARAM_VIDEO);
        mThumbnailFetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher();
        mCoverFetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher();
        mThumbnailFetcher.addVideoSource(mVideoPath, 0, Integer.MAX_VALUE);
        mCoverFetcher.addVideoSource(mVideoPath, 0, Integer.MAX_VALUE);
        mThumbnailList.post(mInitThumbnails);
        mCoverImage.post(new Runnable() {
            @Override
            public void run() {
                initCoverParameters();
                mCoverFetcher.requestThumbnailImage(new long[]{0}, mThumbnailCallback);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mThumbnailFetcher.release();
        mCoverFetcher.release();
    }

    private void initView(){
        mIvLeft = (ImageView) findViewById(R.id.iv_left);
        mIvRight = (ImageView) findViewById(R.id.iv_right);
        mTitle = (TextView) findViewById(R.id.tv_center);

        mIvLeft.setVisibility(View.VISIBLE);
        mIvRight.setVisibility(View.VISIBLE);
        mTitle.setVisibility(View.VISIBLE);

        mTitle.setText(R.string.edit_cover);
        mIvLeft.setImageResource(R.drawable.aliyun_svideo_icon_cancel);
        mIvRight.setImageResource(R.mipmap.aliyun_svideo_icon_confirm);
        mIvLeft.setOnClickListener(this);
        mIvRight.setOnClickListener(this);

        mSlider = findViewById(R.id.indiator);
        mSlider.setOnTouchListener(mSliderListener);
        mCoverImage = (ImageView) findViewById(R.id.cover_image);
        mThumbnailList = (LinearLayout) findViewById(R.id.cover_thumbnail_list);
        mThumbnailList.setOnTouchListener(mClickListener);
    }

    @Override
    public void onClick(View v) {
        if(v == mIvLeft){
            onBackPressed();
        }else if(v == mIvRight){
            ShareableBitmap sbmp = (ShareableBitmap) mCoverImage.getTag();
            if(sbmp != null || sbmp.getData() != null){
                String path = getExternalFilesDir(null) + "thumbnail.jpeg";
                try{
                    sbmp.getData().compress(Bitmap.CompressFormat.JPEG, 100,
                            new FileOutputStream(path));
                }catch (IOException e){
                    e.printStackTrace();
                }

                Intent data = new Intent();
                data.putExtra(KEY_PARAM_RESULT, path);
                setResult(RESULT_OK, data);
                finish();
            }
        }
    }

    private final View.OnTouchListener mClickListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int offset = mSlider.getLeft() - mSlider.getPaddingLeft();
            int vw = mSlider.getWidth() - mSlider.getPaddingRight() - mSlider.getPaddingLeft();
            int endOffset = mSlider.getLeft() + mThumbnailList.getWidth() - vw - mSlider.getPaddingLeft();
            if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                float x = motionEvent.getX();
                float px = x + mSlider.getLeft() - mSlider.getPaddingLeft();
                if(px >= endOffset){
                    px = endOffset;
                }
                if(px <= offset){
                    px = offset;
                }
                long time = (long)(mCoverFetcher.getTotalDuration() * px / mThumbnailList.getWidth());
                fetcheThumbnail(time);

                mSlider.setX(px);
            }
            return true;
        }
    };

    private final View.OnTouchListener mSliderListener = new View.OnTouchListener() {

        private float lastX;
        private float dx;
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            int offset = v.getLeft() - v.getPaddingLeft();
            int vw = v.getWidth() - v.getPaddingRight() - v.getPaddingLeft();
            int endOffset = v.getLeft() + mThumbnailList.getWidth() - vw - v.getPaddingLeft();
            long time = 0;
            switch (action){
                case MotionEvent.ACTION_DOWN:
                    lastX = event.getRawX();
                    dx =  lastX - v.getX();
                    break;
                case MotionEvent.ACTION_MOVE:
//                    float distance = event.getX() - lastX;
                    lastX = event.getRawX();
                    float nx = lastX - dx;
                    if(nx >= endOffset){
                        nx = endOffset;
                    }
                    if(nx <= offset){
                        nx = offset;
                    }

                    v.setX(nx);
                    time = (long)(mCoverFetcher.getTotalDuration() * (nx - offset) / mThumbnailList.getWidth());
                    fetcheThumbnail(time);
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    float x = v.getX() - offset;
                    time = (long)(mCoverFetcher.getTotalDuration() * x / mThumbnailList.getWidth());
                    fetcheThumbnail(time);
                    break;
            }
            return true;
        }
    };

    private int mFetchingThumbnailCount;
    private void fetcheThumbnail(long time){
        Log.d("FETCHER", "fetcher time : " + time + "  count : " + mFetchingThumbnailCount
        + " duration ：" + mCoverFetcher.getTotalDuration());
        if(time >= mCoverFetcher.getTotalDuration()){
            time = mCoverFetcher.getTotalDuration() - 500;
        }
        if(mFetchingThumbnailCount > 2){
            return;
        }
        mFetchingThumbnailCount++;
        mCoverFetcher.requestThumbnailImage(new long[]{time}, mThumbnailCallback);
    }

    private final Runnable mInitThumbnails = new Runnable() {
        @Override
        public void run() {
            mSlider.setX(mSlider.getX() - mSlider.getPaddingLeft());
            initThumbnails();
        }
    };

    private void initThumbnails(){
        int width = mThumbnailList.getWidth();
        int itemWidth= width / 8;
        mThumbnailFetcher.setParameters(itemWidth, itemWidth,
                AliyunIThumbnailFetcher.CropMode.Mediate, ScaleMode.LB, 8);
        long duration = mThumbnailFetcher.getTotalDuration();
        long itemTime = duration / 8;
//        long[] times = new long[8];
        for(int i = 0; i < 8; i++){
//            times[i] = itemTime * i;
            long time = itemTime * i;
            mThumbnailFetcher.requestThumbnailImage(new long[]{time},
                    new AliyunIThumbnailFetcher.OnThumbnailCompletion() {
                        @Override
                        public void onThumbnailReady(ShareableBitmap frameBitmap, long time) {
                            initThumbnails(frameBitmap.getData());
                        }

                        @Override
                        public void onError(int errorCode) {

                        }
                    });
        }

//        mThumbnailFetcher.requestThumbnailImage(times,
//                new AliyunIThumbnailFetcher.OnThumbnailCompletion() {
//                    private int count;
//                    List<Bitmap> thumbnails = new ArrayList<>();
//                    @Override
//                    public void onThumbnailReady(ShareableBitmap frameBitmap, long time) {
//                        count++;
//                        thumbnails.add(frameBitmap.getData());
//                        if(count == 8){
//                            initThumbnails(thumbnails);
//                        }
//                    }
//
//                    @Override
//                    public void onError(int errorCode) {
//                        count++;
//                        if(count == 8){
//                            initThumbnails(thumbnails);
//                        }
//                    }
//                });
    }

    private void initThumbnails(Bitmap thumbnail){
        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.FIT_XY);
        image.setImageBitmap(thumbnail);
        mThumbnailList.addView(image);
    }

    private void initCoverParameters(){
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(mVideoPath);
        String sw = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String sh = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        int w = Integer.parseInt(sw);
        int h = Integer.parseInt(sh);
        int maxWidth = getResources().getDisplayMetrics().widthPixels;
        float scale = (float) h / w;
        h = (int)(maxWidth * scale);
        int maxHeight = mCoverImage.getHeight();
        if(h > maxHeight){
            h = maxHeight;
            w = (int)(maxHeight / scale);
        }else{
            w = maxWidth;
        }

        mCoverFetcher.setParameters(w, h, AliyunIThumbnailFetcher.CropMode.Mediate, ScaleMode.LB, 2);
    }

    private final AliyunThumbnailFetcher.OnThumbnailCompletion mThumbnailCallback = new AliyunIThumbnailFetcher.OnThumbnailCompletion() {
        @Override
        public void onThumbnailReady(ShareableBitmap frameBitmap, long time) {
            mFetchingThumbnailCount--;
            if(mFetchingThumbnailCount < 0){
                mFetchingThumbnailCount = 0;
            }
            Log.d("FETCHER", "fetcher onThumbnailReady time : " + time + "  count : " + mFetchingThumbnailCount);
            ShareableBitmap sbmp = (ShareableBitmap) mCoverImage.getTag();
            if(sbmp != null /*&& sbmp != frameBitmap*/){
                sbmp.release();
            }
            mCoverImage.setImageBitmap(frameBitmap.getData());
            mCoverImage.setTag(frameBitmap);
        }

        @Override
        public void onError(int errorCode) {
            mFetchingThumbnailCount--;
            if(mFetchingThumbnailCount < 0){
                mFetchingThumbnailCount = 0;
            }
            Log.d("FETCHER", "fetcher onError  count : " + mFetchingThumbnailCount);
        }
    };

}
