/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package qupai.aliyun.qusdkdemo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.common.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class DemoActivity extends Activity {
    private RecyclerView demoView;
    private DemoAdapter demoAdapter = new DemoAdapter();
    private int[] demoRes = new int[]{R.mipmap.demo_camera_icon, R.mipmap.demo_crop_icon, R.mipmap.demo_vedio_icon, R.mipmap.demo_edit_icon, R.mipmap.demo_ui_icon};
    private int[] demoTitles = new int[]{R.string.magic_camera, R.string.crop, R.string.record, R.string.edit, R.string.help};
//    private static final int REQUEST_CODE_CAMERA = 1001;
//    private static final int REQUEST_CODE_AUDIO = 1002;
//    private static final int REQUEST_CODE_READ_EXTERNAL = 1003;
//    private static final int REQUEST_CODE_WRITE_EXTERNAL = 1004;

    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static final int PERMISSION_CODES = 1001;

    private boolean permissionGranted = true;


    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermission();
        }
        setContentView(R.layout.activity_demo);
        initView();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermission() {
        List<String> p = new ArrayList<>();
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                p.add(permission);
            }
        }
        if (p.size() > 0) {
            requestPermissions(p.toArray(new String[p.size()]), PERMISSION_CODES);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODES:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    ToastUtil.showToast(this, getString(R.string.need_permission));
                    permissionGranted = false;
                } else {
                    permissionGranted = true;
                }
                break;
            default:
                break;
        }

    }

    private void initView() {
        demoView = (RecyclerView) findViewById(R.id.demo_view);
        demoView.setAdapter(demoAdapter);
        demoView.setLayoutManager(new GridLayoutManager(this, 2));
        demoAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position < demoRes.length) {
                    int key = demoRes[position];
                    if (key == R.mipmap.demo_camera_icon) {
                        if (permissionGranted) {
//                                Intent camera = new Intent(DemoActivity.this, SettingActivity.class);
//                                startActivity(camera);
                            Intent camera = new Intent("com.duanqu.qupai.action.camera");
                            startActivity(camera);
                        } else {
                            ToastUtil.showToast(DemoActivity.this, getString(R.string.need_permission));
                        }
                    } else if (key == R.mipmap.demo_crop_icon) {
                        Intent crop = new Intent("com.duanqu.qupai.action.crop.setting");
                        startActivity(crop);
                    } else if (key == R.mipmap.demo_edit_icon) {
                        Intent edit = new Intent("com.duanqu.qupai.action.import.setting");
                        startActivity(edit);
                    } else if (key == R.mipmap.demo_vedio_icon) {
                        Intent recorder = new Intent("com.duanqu.qupai.action.recorder.setting");
                        startActivity(recorder);
                    } else if (key == R.mipmap.demo_ui_icon) {
                        Intent recorder = new Intent("com.duanqu.qupai.action.help");
                        startActivity(recorder);
                    }
                }
            }
        });
    }


    private class DemoAdapter extends RecyclerView.Adapter<DemoAdapter.DemoHolder> {
        private OnItemClickListener listener;

        @Override
        public DemoAdapter.DemoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            DemoHolder holder = new DemoHolder(View.inflate(DemoActivity.this, R.layout.layout_demo, null), listener);
            return holder;
        }

        @Override
        public void onBindViewHolder(DemoAdapter.DemoHolder holder, int position) {
            holder.demoIcon.setImageResource(demoRes[position]);
            holder.demoTitle.setText(demoTitles[position]);
        }

        @Override
        public int getItemCount() {
            return demoRes.length;
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }

        class DemoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView demoIcon;
            TextView demoTitle;
            private OnItemClickListener listener;

            public DemoHolder(View itemView, OnItemClickListener listener) {
                super(itemView);
                this.listener = listener;
                demoIcon = (ImageView) itemView.findViewById(R.id.demo_icon);
                demoTitle = (TextView) itemView.findViewById(R.id.demo_title);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(v, getAdapterPosition());
                }
            }
        }
    }
}
