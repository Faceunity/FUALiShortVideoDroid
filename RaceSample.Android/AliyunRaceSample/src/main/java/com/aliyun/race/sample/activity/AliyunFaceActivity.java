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
import android.widget.Switch;
import android.widget.TextView;

import com.aliyun.race.sample.R;
import com.aliyun.race.sample.cameraView.CameraFaceView;
import com.aliyun.race.sample.cameraView.CameraInterface;
import com.aliyun.race.sample.utils.FastClickUtil;
import com.aliyun.race.sample.utils.PermissionUtils;

public class AliyunFaceActivity extends AppCompatActivity {
    private static final String TAG = "RACE";
    private CameraFaceView mCameraView = null;
    private TextView mFpsTv;
    private static final int PERMISSION_REQUEST_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aliyun_race_face_view_layout);
        boolean checkResult = PermissionUtils.checkPermissionsGroup(this, PermissionUtils.PERMISSION_CAMERA);
        if (!checkResult) {
            PermissionUtils.requestPermissions(this, PermissionUtils.PERMISSION_CAMERA, PERMISSION_REQUEST_CODE);
        }
        mCameraView = findViewById(R.id.camera_textureview);
        ImageView switchCamera = findViewById(R.id.switch_camera);
        Switch pointSwitch = findViewById(R.id.open_face_point);
        mFpsTv = findViewById(R.id.camera_fps);
        initViewParams();
        initRaceView();
        switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastClickUtil.isFastClick()) {
                    return;
                }
                if(mCameraView != null){
                    mCameraView.onCameraSwitch();
                }
            }
        });
        pointSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mCameraView != null){
                    mCameraView.onOpenFacePoint(isChecked);
                }
            }
        });
    }

    private void initRaceView(){
        mCameraView.setFpsTv(mFpsTv);
    }

    private void initViewParams(){
        ViewGroup.LayoutParams params = mCameraView.getLayoutParams();
        Point p = getScreenMetrics(this);
        params.width = p.x; //view宽
        params.height = p.y; //view高
        //设置GLSurfaceView的宽和高
        mCameraView.setLayoutParams(params);
    }

    private Point getScreenMetrics(Context context){
        DisplayMetrics dm =context.getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        return new Point(screenWidth, screenHeight);
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
//        mBeautifyView.bringToFront();
        CameraInterface.getInstance().startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CameraInterface.getInstance().stopPreview();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        if(mCameraView != null){
            mCameraView.release();
            mCameraView = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}