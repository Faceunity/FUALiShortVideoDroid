/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.recorder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.aliyun.demo.R;

public class SettingActivity extends Activity {
    private EditText width,height;
    private boolean isBeautyOpen = true,isFaceOpen = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.aliyun_svideo_activity_setting);
        width = (EditText) findViewById(R.id.aliyun_width);
        height = (EditText) findViewById(R.id.aliyun_height);

    }
    public void start(View view){
        int w = Integer.parseInt(width.getText().toString());
        int h = Integer.parseInt(height.getText().toString());
        Intent intent = new Intent(this,CameraDemo.class);
        intent.putExtra("width",w);
        intent.putExtra("height",h);
        intent.putExtra("beauty",isBeautyOpen);
        intent.putExtra("face",isFaceOpen);
        if(w == 0 || h == 0){
            return;
        }
        startActivity(intent);
    }

    public void openBeauty(View view){
        if(isBeautyOpen){
            ((Button)view).setText("美颜关");
            isBeautyOpen = false;
        }else{
            ((Button)view).setText("美颜开");
            isBeautyOpen = true;
        }
    }

    public void openFaceDetect(View view){
        if(isFaceOpen){
            ((Button)view).setText("人脸检测关");
            isFaceOpen = false;
        }else{
            ((Button)view).setText("人脸检测开");
            isFaceOpen = true;
        }
    }

}
