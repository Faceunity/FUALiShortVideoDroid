package com.aliyun.race.sample.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.aliyun.race.sample.R;
import com.aliyun.race.sample.adapter.HomeViewPagerAdapter;
import com.aliyun.race.sample.adapter.MultilayerGridAdapter;
import com.aliyun.race.sample.bean.ScenesModel;
import com.aliyun.race.sample.utils.FastClickUtil;
import com.aliyun.race.sample.utils.FileCommon;
import com.aliyun.race.sample.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mulberry
 */
public class RaceMainActivity extends AppCompatActivity {


    /**
     *小圆点指示器
     */
    private ViewGroup mPoints;
    /**
     * 小圆点图片集合
     */
    private ImageView[] mIvPoints;
    private ViewPager mViewPager;
    /**
     * 总的页数
     */
    private int mTotalPage;
    /**
     * 每页显示的最大数量
     */
    private int mPageSize = 6;
    /**
     * 总的数据源
     */
    private List<ScenesModel> mListDatas;
    /**
     * GridView作为一个View对象添加到ViewPager集合中
     */
    private List<View> mViewPagerList;
    /**
     * module数据，
     */
    private int[] mModules = new int[] {
        R.string.solution_recorder_face_beayty,
        R.string.solution_recorder_face_point
    };
    private int[] mHomeicon = {
        R.mipmap.icon_home_svideo_record,
        R.mipmap.icon_home_svideo_edit

    };

    private static final int PERMISSION_REQUEST_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_race_solution_main);
        boolean checkResult = PermissionUtils.checkPermissionsGroup(this, PermissionUtils.PERMISSION_CAMERA);
        if (!checkResult) {
            PermissionUtils.requestPermissions(this, PermissionUtils.PERMISSION_CAMERA, PERMISSION_REQUEST_CODE);
        }
        FileCommon.copyRace(this);
        iniViews();
        setDatas();
        buildHomeItem();
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

    private void iniViews() {
        mViewPager = (ViewPager) findViewById(R.id.home_viewPager);
        mPoints = (ViewGroup) findViewById(R.id.points);
    }

    private void setDatas() {
        mListDatas = new ArrayList<>();
        for (int i = 0; i < mModules.length; i++) {
            mListDatas.add(new ScenesModel(getResources().getString(mModules[i]), mHomeicon[i]));
        }
    }

    private void buildHomeItem() {
        LayoutInflater inflater = LayoutInflater.from(this);
        mTotalPage = (int) Math.ceil(mListDatas.size() * 1.0 / mPageSize);
        mViewPagerList = new ArrayList<>();


        for (int i = 0; i < mTotalPage; i++) {
            //每个页面都是inflate出一个新实例
            GridView gridView = (GridView) inflater.inflate(R.layout.alivc_race_home_girdview, mViewPager, false);
            gridView.setAdapter(new MultilayerGridAdapter(this, mListDatas, i, mPageSize));
            //添加item点击监听
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (FastClickUtil.isFastClick()) {
                        return;
                    }
                    switch (position) {
                    case 0:
                        // race美颜
                        Intent record = new Intent(RaceMainActivity.this, AliyunBeautifyActivity.class);
                        startActivity(record);

                        break;
                    case 1:
                        // 人脸检测
                        Intent point = new Intent(RaceMainActivity.this, AliyunFaceActivity.class);
                        startActivity(point);
                        break;
                    default:
                        break;
                    }
                }
            });
            //每一个GridView作为一个View对象添加到ViewPager集合中
            mViewPagerList.add(gridView);
        }

        //设置ViewPager适配器
        mViewPager.setAdapter(new HomeViewPagerAdapter(mViewPagerList));

        //小圆点指示器
        if (mTotalPage > 1) {
            mIvPoints = new ImageView[mTotalPage];
            for (int i = 0; i < mIvPoints.length; i++) {
                ImageView imageView = new ImageView(this);
                //设置图片的宽高
                imageView.setLayoutParams(new ViewGroup.LayoutParams(10, 10));
                if (i == 0) {
                    imageView.setBackgroundResource(R.mipmap.page_selected_indicator);
                } else {
                    imageView.setBackgroundResource(R.mipmap.page_normal_indicator);
                }
                mIvPoints[i] = imageView;
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                layoutParams.leftMargin = (int)getResources().getDimension(R.dimen.alivc_common_padding_2);//设置点点点view的左边距
                layoutParams.rightMargin = (int)getResources().getDimension(R.dimen.alivc_common_padding_2);;//设置点点点view的右边距
                mPoints.addView(imageView, layoutParams);
            }
            mPoints.setVisibility(View.VISIBLE);
        } else {
            mPoints.setVisibility(View.GONE);
        }


        //设置ViewPager滑动监听
        mViewPager.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //改变小圆圈指示器的切换效果
                setImageBackground(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setImageBackground(int selectItems) {
        for (int i = 0; i < mIvPoints.length; i++) {
            if (i == selectItems) {
                mIvPoints[i].setBackgroundResource(R.mipmap.page_selected_indicator);
            } else {
                mIvPoints[i].setBackgroundResource(R.mipmap.page_normal_indicator);
            }
        }
    }

}
