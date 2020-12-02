package com.aliyun.race.sample.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.aliyun.race.sample.R;
import com.aliyun.race.sample.bean.RaceMode;
import com.aliyun.race.sample.cameraView.CameraBeautifyView;
import com.aliyun.race.sample.cameraView.CameraInterface;
import com.aliyun.race.sample.utils.FastClickUtil;
import com.aliyun.race.sample.utils.PermissionUtils;

public class AliyunBeautifyActivity extends AppCompatActivity {
    private static final String TAG = "RaceSample";
    private CameraBeautifyView mBeautifyView = null;
    private ImageView mSwitchCamrea;
    private ImageView mRaceParam;
    private Switch mRaceSwitch;
    private Switch mPointSwitch;
    private RadioGroup mRadioGroup;
    private TextView mRaceOpenTv;
    private TextView mPointOpenTv;
    private TextView mFpsTv;
    private static final int PERMISSION_REQUEST_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aliyun_race_beautify_view_layout);
        boolean checkResult = PermissionUtils.checkPermissionsGroup(this, PermissionUtils.PERMISSION_CAMERA);
        if (!checkResult) {
            PermissionUtils.requestPermissions(this, PermissionUtils.PERMISSION_CAMERA, PERMISSION_REQUEST_CODE);
        }
        mBeautifyView = findViewById(R.id.camera_textureview);
        mSwitchCamrea = findViewById(R.id.switch_camera);
        mRaceSwitch = findViewById(R.id.open_race);
        mPointSwitch = findViewById(R.id.open_face_point);
        mRaceOpenTv = findViewById(R.id.open_race_text);
        mPointOpenTv = findViewById(R.id.open_face_point_text);
        mRadioGroup = findViewById(R.id.camera_radio);
        mRaceParam = findViewById(R.id.camera_race_param);
        mFpsTv = findViewById(R.id.camera_fps);
        initRaceView();
        mSwitchCamrea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastClickUtil.isFastClick()) {
                    return;
                }
                if(mBeautifyView != null){
                    mBeautifyView.onCameraSwitch();
                }
            }
        });
        mRaceParam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBeautifyView != null){
                    mBeautifyView.onBeautyFaceClick(AliyunBeautifyActivity.this.getSupportFragmentManager());
                }
            }
        });
        mRaceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mBeautifyView != null){
                    mBeautifyView.onOpenRace(isChecked);
                }
            }
        });
        mPointSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mBeautifyView != null){
                    mBeautifyView.onOpenFacePoint(isChecked);
                }
            }
        });
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(mBeautifyView != null){
                    if (checkedId == R.id.camera_process) {
                        mBeautifyView.switchRaceMode(RaceMode.MIX);
                    } else if(checkedId == R.id.camera_bufffer){
                        mBeautifyView.switchRaceMode(RaceMode.BUFFER);
                    } else if(checkedId == R.id.camera_texture){
                        mBeautifyView.switchRaceMode(RaceMode.TEXTURE);
                    }
                }
            }
        });
    }

    private void initRaceView(){
        mBeautifyView.setFpsTv(mFpsTv);
        mPointSwitch.setVisibility(View.INVISIBLE);
        mPointOpenTv.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean isAllGranted = true;

            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (isAllGranted) {
                // 如果所有的权限都授予了
                //Toast.makeText(this, "get All Permisison", Toast.LENGTH_SHORT).show();
            } else {
                // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
                showPermissionDialog();
            }
        }
    }

    //系统授权设置的弹框
    AlertDialog openAppDetDialog = null;
    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.app_name) + "需要访问 \"摄像头\" 和 \"外部存储器\",否则会影响绝大部分功能使用, 请到 \"应用信息 -> 权限\" 中设置！");
        builder.setPositiveButton("去设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });
        builder.setCancelable(false);
        builder.setNegativeButton("暂不设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //finish();
            }
        });
        if (null == openAppDetDialog) {
            openAppDetDialog = builder.create();
        }
        if (null != openAppDetDialog && !openAppDetDialog.isShowing()) {
            openAppDetDialog.show();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        //更改视图在树中的z顺序，因此它位于其他同级视图之上。
//        mFrameLayout.bringToFront();
        if(mBeautifyView != null){
            mBeautifyView.onResume();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(mBeautifyView != null){
            mBeautifyView.onPause();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        if(mBeautifyView != null){
            mBeautifyView.release();
            mBeautifyView = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}