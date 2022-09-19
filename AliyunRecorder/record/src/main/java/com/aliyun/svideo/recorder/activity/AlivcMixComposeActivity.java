/*
 * Copyright (C) 2010-2021 Alibaba Group Holding Limited.
 *
 */

package com.aliyun.svideo.recorder.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.aliyun.common.utils.MediaUtil;
import com.aliyun.common.utils.ToastUtil;
import com.aliyun.svideo.record.R;
import com.aliyun.svideosdk.common.struct.common.VideoDisplayMode;
import com.aliyun.svideosdk.common.struct.common.VideoQuality;
import com.aliyun.svideosdk.mixrecorder.AliyunIMixComposer;
import com.aliyun.svideosdk.mixrecorder.AliyunMixCallback;
import com.aliyun.svideosdk.mixrecorder.AliyunMixOutputParam;
import com.aliyun.svideosdk.mixrecorder.AliyunMixStream;
import com.aliyun.svideosdk.mixrecorder.AliyunMixTrack;
import com.aliyun.svideosdk.mixrecorder.AliyunMixTrackLayoutParam;
import com.aliyun.svideosdk.mixrecorder.impl.AliyunMixComposerCreator;

import java.util.List;

/**
 * 视频拼接
 */
public class AlivcMixComposeActivity extends AppCompatActivity {
    private final int REQUEST_TRACK1_STREAM = 1001;
    private final int REQUEST_TRACK2_STREAM = 1002;

    private AliyunIMixComposer mMixComposer = null;

    private AliyunMixTrack mVideoTrack1 = null;
    private AliyunMixTrack mVideoTrack2 = null;

    private long mVideoTrack1Duration = 0L;
    private long mVideoTrack2Duration = 0L;
    private boolean mOpenMixAudio = false;
    private TextView mMixStatus = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mix);

        mMixStatus = this.findViewById(R.id.tvMixStatus);
        mMixStatus.setText("未初始化");
        this.findViewById(R.id.btnReset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.btnMix).setEnabled(false);

                if (mMixComposer != null) {
                    mMixComposer.release();
                    mMixComposer = null;
                }


                init();
            }
        });

        this.findViewById(R.id.btnPauseMix).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMixComposer != null) {
                    mMixComposer.pause();
                }
            }
        });

        this.findViewById(R.id.btnResumeMix).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMixComposer != null) {
                    mMixComposer.resume();
                }
            }
        });


        findViewById(R.id.btnAddTrack1Stream).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_TRACK1_STREAM);
            }
        });

        findViewById(R.id.btnAddTrack2Stream).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_TRACK2_STREAM);
            }
        });

        CheckBox lCheckBoxOpenMixAudio = findViewById(R.id.cbOpenMix);
        lCheckBoxOpenMixAudio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mOpenMixAudio = isChecked;

                if (mVideoTrack1 != null) {
                    mVideoTrack1.setIsOutputAudioTrack(mOpenMixAudio);
                    mVideoTrack1.setMixAudioWeight(50);
                }

                if (mVideoTrack2 != null) {
                    mVideoTrack2.setIsOutputAudioTrack(mOpenMixAudio);
                    mVideoTrack2.setMixAudioWeight(50);
                }

            }
        });



        findViewById(R.id.btnMix).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //配置输出参数
                AliyunMixOutputParam.Builder outputParamBuilder = new AliyunMixOutputParam.Builder();
                outputParamBuilder
                .outputPath("/storage/emulated/0/DCIM/Camera/svideo_mix_demo.mp4")
                .crf(6)
                .videoQuality(VideoQuality.HD)
                .outputWidth(720)
                .outputHeight(1280)
                .fps(30)
                .gopSize(30);
                if (mVideoTrack1Duration > mVideoTrack2Duration) {
                    outputParamBuilder.outputAudioReferenceTrack(mVideoTrack1);
                    outputParamBuilder.outputDurationReferenceTrack(mVideoTrack1);
                } else {
                    outputParamBuilder.outputAudioReferenceTrack(mVideoTrack2);
                    outputParamBuilder.outputDurationReferenceTrack(mVideoTrack2);
                }

                mMixComposer.setOutputParam(outputParamBuilder.build());
                mMixComposer.start(new AliyunMixCallback() {
                    @Override
                    public void onProgress(long progress) {
                        mMixStatus.setText("当前合成进度: " + progress);
                        Log.e("MixActivity", "onProgress " + progress);
                    }

                    @Override
                    public void onComplete() {
                        mMixStatus.setText("合成完成");
                        Log.e("MixActivity", "onComplete");
                    }

                    @Override
                    public void onError(int errorCode) {
                        mMixStatus.setText("合成错误:" + errorCode);
                        Log.e("MixActivity", "onError" + errorCode);
                    }
                });


            }
        });

        init();

    }

    private void init() {
        mMixComposer = AliyunMixComposerCreator.createMixComposerInstance();
        //创建轨道1
        AliyunMixTrackLayoutParam track1Layout = new AliyunMixTrackLayoutParam.Builder()
        .centerX(0.25f)
        .centerY(0.25f)
        .widthRatio(0.5f)
        .heightRatio(0.5f)
        .build();

        mVideoTrack1 = mMixComposer.createTrack(track1Layout);
        mVideoTrack1.setIsOutputAudioTrack(mOpenMixAudio);
        mVideoTrack1.setMixAudioWeight(50);

        //创建轨道2
        AliyunMixTrackLayoutParam track2Layout = new AliyunMixTrackLayoutParam.Builder()
        .centerX(0.75f)
        .centerY(0.75f)
        .widthRatio(0.5f)
        .heightRatio(0.5f)
        .build();

        //设置背景颜色
//        mMixComposer.setBackgroundColor(Color.RED);
        //设置背景图片 , 0 : crop , 1 : fill ,2 : exact fit
//        mMixComposer.setBackgroundImage("/storage/emulated/0/Movies/2021-04-19-202458449-crop.png", 1);

        mVideoTrack2 = mMixComposer.createTrack(track2Layout);
        mVideoTrack2.setIsOutputAudioTrack(mOpenMixAudio);
        mVideoTrack2.setMixAudioWeight(50);

//        test();
    }

    private void test() {
        String videoPath = "/storage/emulated/0/Movies/2021-04-21-142714389-compose.mp4";
//        videoPath = "/storage/emulated/0/Movies/2021-04-21-142441591-compose.mp4";
//        long duration = MediaUtil.getVideoDuration(videoPath);
        long duration = 3000;
        Log.e("MixActivity", "select video:" + videoPath);

        mVideoTrack1Duration = 0;
        // 创建轨道1的第一个视频流1
        AliyunMixStream stream1 = new AliyunMixStream.Builder()
        .displayMode(VideoDisplayMode.FILL)
        .filePath(videoPath)
        .streamStartTimeMills(mVideoTrack1Duration)
        .streamEndTimeMills(duration)
        .build();
        mVideoTrack1Duration += duration;
        if (mVideoTrack1.addStream(stream1) == 0) {
            ToastUtil.showToast(this, "添加轨道1视频流成功");
        }

        videoPath = "/storage/emulated/0/Movies/2021-04-19-202458449-crop.mp4";
//        duration = MediaUtil.getVideoDuration(videoPath);
        duration = 3000;

        mVideoTrack2Duration = 0L;
        // 创建轨道1的第一个视频流1
        AliyunMixStream stream2 = new AliyunMixStream.Builder()
        .displayMode(VideoDisplayMode.FILL)
        .filePath(videoPath)
        .streamStartTimeMills(mVideoTrack2Duration)
        .streamEndTimeMills(duration)
        .build();
        mVideoTrack2Duration += duration;
        if (mVideoTrack2.addStream(stream2) == 0) {
            ToastUtil.showToast(this, "添加轨道1视频流成功");
        }

        if (mVideoTrack1Duration > 0 && mVideoTrack2Duration > 0) {
            findViewById(R.id.btnMix).setEnabled(true);
        }
        findViewById(R.id.btnMix).setEnabled(true);
        mMixStatus.setText("已初始化");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMixComposer != null) {
            mMixComposer.resume();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMixComposer != null) {
            mMixComposer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMixComposer != null) {
            mMixComposer.release();
            mMixComposer = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            String videoPath = null;
            long duration = 0;

            Uri uri = data.getData();

            ContentResolver cr = this.getContentResolver();
            /** 数据库查询操作。
             * 第一个参数 uri：为要查询的数据库+表的名称。
             * 第二个参数 projection ： 要查询的列。
             * 第三个参数 selection ： 查询的条件，相当于SQL where。
             * 第三个参数 selectionArgs ： 查询条件的参数，相当于 ？。
             * 第四个参数 sortOrder ： 结果排序。
             */
            Cursor cursor = cr.query(uri, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    videoPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    // 视频时长：MediaStore.Audio.Media.DURATION
//                    duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                    duration = MediaUtil.getVideoDuration(videoPath);
                }
                cursor.close();
            }
            Log.e("MixActivity", "select video: duration :" + duration + ",path :" + videoPath);


            switch (requestCode) {
            case REQUEST_TRACK1_STREAM : {
                // onResult Callback

                mVideoTrack1Duration = 0;
                // 创建轨道1的第一个视频流1
                AliyunMixStream stream1 = new AliyunMixStream.Builder()
                .displayMode(VideoDisplayMode.FILL)
                .filePath(videoPath)
                .streamStartTimeMills(mVideoTrack1Duration)
                .streamEndTimeMills(duration)
                .build();
                mVideoTrack1Duration += duration;
                if (mVideoTrack1.addStream(stream1) == 0) {
                    ToastUtil.showToast(this, "添加轨道1视频流成功");
                }
                break;
            }
            case REQUEST_TRACK2_STREAM : {
                // onResult Callback

                mVideoTrack2Duration = 0L;
                // 创建轨道1的第一个视频流1
                AliyunMixStream stream1 = new AliyunMixStream.Builder()
                .displayMode(VideoDisplayMode.FILL)
                .filePath(videoPath)
                .streamStartTimeMills(mVideoTrack2Duration)
                .streamEndTimeMills(duration)
                .build();
                mVideoTrack2Duration += duration;
                if (mVideoTrack2.addStream(stream1) == 0) {
                    ToastUtil.showToast(this, "添加轨道1视频流成功");
                }
                break;
            }
            }
        }
        if (mVideoTrack1Duration > 0 && mVideoTrack2Duration > 0) {
            findViewById(R.id.btnMix).setEnabled(true);
        }
    }
}
