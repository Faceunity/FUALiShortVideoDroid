package com.aliyun.svideo.base.ui;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.aliyun.common.global.Version;
import com.aliyun.svideo.base.R;

/**
 * 显示sdk的版本信息
 * 短视频的三个版本都会使用
 */
public class SdkVersionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sdk_version);
        showVersionInfo();
    }

    @SuppressLint("SetTextI18n")
    private void showVersionInfo() {
        ((TextView) findViewById(R.id.tv_version)).setText("VERSION :" + Version.VERSION);
        ((TextView) findViewById(R.id.tv_module)).setText("MODULE :" + Version.MODULE);
        ((TextView) findViewById(R.id.tv_build_id)).setText("BUILD_ID :" + Version.BUILD_ID);
        ((TextView) findViewById(R.id.tv_src_commit_id)).setText("SRC_COMMIT_ID :" + Version.SRC_COMMIT_ID);
        ((TextView) findViewById(R.id.tv_alivc_commit_id)).setText("ALIVC_COMMIT_ID :" + Version.ALIVC_COMMIT_ID);
        ((TextView) findViewById(R.id.tv_android_commit_id)).setText("ANDROID_COMMIT_ID :" + Version.ANDROID_COMMIT_ID);
    }
}
