/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.recorder;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.aliyun.common.utils.FileUtils;
import com.aliyun.common.utils.ToastUtil;
import com.aliyun.demo.R;

import java.io.IOException;


public class VideoPlayActivity extends Activity implements SurfaceHolder.Callback,View.OnClickListener{
    public static final String VIDEO_PATH = "video_path";
    public static final String VIDEO_ROTATION = "video_rotation";
    private SurfaceView textureView;
    private ImageView back,next;
    private MediaPlayer player;
    private String videoPath;
    private int recordRotation;
    private Surface playSurface;
    private MediaScannerConnection msc;
    private MediaMetadataRetriever mmr = new MediaMetadataRetriever();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.aliyun_svideo_activity_video_play);
        getData();
        initView();
        msc = new MediaScannerConnection(this,null);
        msc.connect();
    }

    private void initView() {
        textureView = (SurfaceView) findViewById(R.id.aliyun_play_view);
        textureView.getHolder().addCallback(this);
        if(recordRotation == 90 || recordRotation == 270){
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) textureView.getLayoutParams();
            layoutParams.width = getResources().getDisplayMetrics().widthPixels;
            layoutParams.height = (int) (getResources().getDisplayMetrics().widthPixels / (float)getResources().getDisplayMetrics().heightPixels * layoutParams.width);
        }
        next = (ImageView) findViewById(R.id.aliyun_next);
        next.setOnClickListener(this);
        back = (ImageView) findViewById(R.id.aliyun_back);
        back.setOnClickListener(this);
    }

    private void getData() {
        videoPath = getIntent().getStringExtra(VIDEO_PATH);
        try {
            mmr.setDataSource(videoPath);
            recordRotation = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void initPlayer() {
        player = new MediaPlayer();
        try {
            player.setDataSource(videoPath);
            player.setSurface(playSurface);
            player.setLooping(false);
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    player.reset();
                    try {
                        player.setDataSource(videoPath);
                        player.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    player.start();
                }
            });
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        msc.disconnect();
        mmr.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(player != null){
            player.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(player != null){
            player.start();
        }
    }

    @Override
    public void onClick(View v) {
        if(v == next){
            ToastUtil.showToast(getApplicationContext(),R.string.aliyun_video_save_tip);
            scanFile();
            setResult(Activity.RESULT_OK);
            finish();
        }else if(v == back){
            deleteFile();
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }
    private void scanFile(){
        msc.scanFile(getApplicationContext(),
                new String[]{videoPath},new String[]{"video/mp4"},null);
    }

    private void deleteFile(){
        new AsyncTask(){
            @Override
            protected Object doInBackground(Object[] params) {
                FileUtils.deleteFile(videoPath);
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        playSurface = surfaceHolder.getSurface();
        initPlayer();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        playSurface = null;
        player.stop();
        player.release();
        player = null;
    }
}
