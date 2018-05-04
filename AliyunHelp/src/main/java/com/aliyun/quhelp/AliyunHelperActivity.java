/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.quhelp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class AliyunHelperActivity extends Activity implements View.OnClickListener {

    private ImageView mBackBtn;
    private TextView mTextView;
    private TextView mTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aliyun_svideo_activity_layout);
        initView();
    }

    private void initView() {
        mBackBtn = (ImageView) findViewById(R.id.back);
        mBackBtn.setOnClickListener(this);
        mTextView = (TextView) findViewById(R.id.copy_right);
        mTextView.setText(R.string.copy_right);
        mTextView.setOnClickListener(this);
        mTitle = (TextView) findViewById(R.id.actionbar_title);
        mTitle.setText(R.string.app_help);
    }

    @Override
    public void onClick(View v) {
        if(v == mBackBtn) {
            finish();
        } else if (v == mTextView) {
            Intent intent = new Intent(AliyunHelperActivity.this, CopyRightActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        NativeLeakTracer.nativeSaveLeakInfo("/sdcard/leaks.txt");
    }
}
