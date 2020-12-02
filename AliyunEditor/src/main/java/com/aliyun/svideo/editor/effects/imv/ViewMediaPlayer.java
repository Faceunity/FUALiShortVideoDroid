/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.imv;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;

import java.io.IOException;


public class ViewMediaPlayer extends TextureView implements TextureView.SurfaceTextureListener,
    MediaPlayer.OnSeekCompleteListener,
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnVideoSizeChangedListener {

    public MediaPlayer mMediaPlayer;
    private Surface mSurface;
    private SurfaceTexture mSurfaceTexture;
    private Uri mUri;
    private final int[] mLock;
    private View mView;

    private boolean mIsVideoReadyToBePlayed = false;

    public ViewMediaPlayer(Context context, AttributeSet attrs, Uri uri, View view) {
        super(context, attrs);
        this.mMediaPlayer = null;
        this.mUri = uri;
        mLock = new int[0];
        this.mView = view;

        setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        setSurfaceTextureListener(this);
    }

    private void loadVideo() {
        setScaleX(1.0001f);
        setScaleY(1.0001f);

        mSurfaceTexture = getSurfaceTexture();
        if (mSurfaceTexture == null) {
            return;
        }
        release();
        mMediaPlayer = new MediaPlayer();
        mSurface = new Surface(mSurfaceTexture);
        if (mUri != null) {
            try {
                mMediaPlayer.setDataSource(mUri.toString());
                mMediaPlayer.setSurface(mSurface);
                mMediaPlayer.setOnSeekCompleteListener(ViewMediaPlayer.this);
                mMediaPlayer.setOnPreparedListener(ViewMediaPlayer.this);
                mMediaPlayer.setOnErrorListener(null);
                mMediaPlayer.setOnVideoSizeChangedListener(ViewMediaPlayer.this);
                mMediaPlayer.setAudioStreamType(3);
                mMediaPlayer.setLooping(true);
                mMediaPlayer.prepareAsync();
            } catch (IOException localIOException) {
                localIOException.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public void startVideoPlay() {
        if (getVisibility() != View.VISIBLE) {
            setVisibility(View.VISIBLE);
        }

        if (!mIsVideoReadyToBePlayed) {
            return;
        }

        try {
            this.mMediaPlayer.start();
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public View getShowView() {
        return this;
    }

    private void release() {
        synchronized (this.mLock) {
            if (this.mMediaPlayer != null) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                this.mMediaPlayer.reset();
                this.mMediaPlayer.release();
                this.mMediaPlayer = null;
            }
            return;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mIsVideoReadyToBePlayed = true;
        startVideoPlay();
        mView.setVisibility(View.GONE);
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        loadVideo();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        release();
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();

        }
        if (mSurface != null) {
            mSurface.release();
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
