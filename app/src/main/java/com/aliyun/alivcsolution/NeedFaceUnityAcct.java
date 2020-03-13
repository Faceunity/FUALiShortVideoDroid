package com.aliyun.alivcsolution;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.aliyun.demo.R;
import com.aliyun.demo.recorder.util.PreferenceUtil;

public class NeedFaceUnityAcct extends AppCompatActivity {
    // 是否使用 FaceUnity 美颜
    private boolean isOn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_faceunity);

        final Button button = (Button) findViewById(R.id.btn_set);
        String isOn = PreferenceUtil.getString(this, PreferenceUtil.KEY_FACEUNITY_IS_ON);
        this.isOn = !TextUtils.isEmpty(isOn) && PreferenceUtil.VALUE_ON.equals(isOn);
        button.setText(this.isOn ? "On" : "Off");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NeedFaceUnityAcct.this.isOn = !NeedFaceUnityAcct.this.isOn;
                button.setText(NeedFaceUnityAcct.this.isOn ? "On" : "Off");
            }
        });

        Button btnToMain = (Button) findViewById(R.id.btn_to_main);
        btnToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceUtil.persistString(NeedFaceUnityAcct.this, PreferenceUtil.KEY_FACEUNITY_IS_ON,
                        NeedFaceUnityAcct.this.isOn ? PreferenceUtil.VALUE_ON : PreferenceUtil.VALUE_OFF);
                Intent intent = new Intent(NeedFaceUnityAcct.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

}
