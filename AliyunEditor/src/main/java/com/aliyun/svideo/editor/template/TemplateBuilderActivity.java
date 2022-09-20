package com.aliyun.svideo.editor.template;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.aliyun.common.utils.FileUtils;
import com.aliyun.common.utils.StringUtils;
import com.aliyun.common.utils.ToastUtil;
import com.aliyun.svideo.common.utils.DateTimeUtils;
import com.aliyun.svideo.common.widget.AlivcCircleLoadingDialog;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.draft.CloudDraftConfigDialogFragment;
import com.aliyun.svideo.editor.util.AlivcResUtil;
import com.aliyun.svideosdk.common.AliyunErrorCode;
import com.aliyun.svideosdk.common.struct.project.AliyunEditorProject;
import com.aliyun.svideosdk.common.struct.project.Source;
import com.aliyun.svideosdk.common.struct.template.AliyunTemplateParam;
import com.aliyun.svideosdk.editor.EditorCallBack;
import com.aliyun.svideosdk.editor.resource.AliyunResTask;
import com.aliyun.svideosdk.editor.template.AliyunTemplateSourceHandleCallback;
import com.aliyun.svideosdk.editor.template.AliyunTemplateBuilder;
import com.aliyun.svideosdk.editor.template.AliyunTemplateFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模板创建
 */
public class TemplateBuilderActivity extends FragmentActivity implements View.OnClickListener {
    public static final String KEY_PARAM_CONFIG = "project_json_path";
    public static final String KEY_PARAM_OUTPUT_PATH = "OutputPath";
    private FrameLayout mSurfaceLayout;
    private SurfaceView mSurfaceView;
    private ImageView mPlayView;
    private TextView mTvTime;
    private SeekBar mSeekBar;
    private TextView mTvDuration;
    private RecyclerView mRecyclerView;

    private TemplateInputAdapter mTemplateInputAdapter;

    private AliyunTemplateBuilder mAliyunTemplateBuilder;
    private boolean isNeedResume = true;

    private String mOutputPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aliyun_svideo_activity_template_builder);
        mSurfaceLayout = findViewById(R.id.layout_surface);
        mSurfaceView = findViewById(R.id.surface_view);
        mPlayView = findViewById(R.id.aliyun_template_btn_play);
        mTvTime = findViewById(R.id.aliyun_template_tv_time);
        mSeekBar = findViewById(R.id.aliyun_template_play_seekbar);
        mTvDuration = findViewById(R.id.aliyun_template_tv_duration);
        mRecyclerView = findViewById(R.id.recycler_view);
        TextView tvCenter = (TextView) findViewById(R.id.tv_center);
        tvCenter.setText(R.string.alivc_editor_template_build_title);
        tvCenter.setVisibility(View.VISIBLE);
        TextView tvRight = (TextView) findViewById(R.id.tv_right);
        ImageView ivLeft = (ImageView) findViewById(R.id.iv_left);
        ivLeft.setOnClickListener(this);
        ivLeft.setVisibility(View.VISIBLE);
        ivLeft.setImageResource(R.mipmap.aliyun_svideo_icon_back);
        tvRight.setText(R.string.alivc_editor_template_build_btn);
        tvRight.setVisibility(View.VISIBLE);
        tvRight.setOnClickListener(this);
        mSurfaceLayout.post(new Runnable() {
            @Override
            public void run() {
                init();
            }
        });
    }

    private void init() {
        mOutputPath = getIntent().getStringExtra(KEY_PARAM_OUTPUT_PATH);
        String path = getIntent().getStringExtra(KEY_PARAM_CONFIG);
        mAliyunTemplateBuilder = AliyunTemplateFactory.createAliyunTemplateBuilder(Uri.parse(path));
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mSurfaceLayout.getLayoutParams();
        DisplayMetrics d = getResources().getDisplayMetrics();
        int outputWidth = mAliyunTemplateBuilder.getEditorProject().getConfig().getOutputWidth();
        int outputHeight = mAliyunTemplateBuilder.getEditorProject().getConfig().getOutputHeight();
        int layoutHeight = mSurfaceLayout.getHeight();
        int width = d.widthPixels;
        int height = Math.round((float) d.widthPixels * outputHeight / outputWidth);
        if (height > layoutHeight) {
            height = layoutHeight;
            width = Math.round((float) height * outputWidth / outputHeight);
        }
        layoutParams.width = width;
        layoutParams.height = height;
        mSurfaceLayout.setLayoutParams(layoutParams);
        mAliyunTemplateBuilder.setPlayerCallBack(new EditorCallBack() {
            @Override
            public void onEnd(int state) {
                mTvTime.post(new Runnable() {
                    @Override
                    public void run() {
                        mPlayView.setImageResource(R.mipmap.aliyun_svideo_play);
                    }
                });
            }

            @Override
            public void onError(int rv) {

            }

            @Override
            public int onCustomRender(int srcTextureID, int width, int height) {
                return 0;
            }

            @Override
            public int onTextureRender(int srcTextureID, int width, int height) {
                return 0;
            }

            @Override
            public void onPlayProgress(final long currentPlayTime, long currentStreamPlayTime) {
                mTvTime.post(new Runnable() {
                    @Override
                    public void run() {
                        mTvTime.setText(DateTimeUtils.formatMs(currentPlayTime / 1000));
                        mSeekBar.setProgress((int) (currentPlayTime / 1000));
                    }
                });
            }

            @Override
            public void onDataReady() {

            }
        });
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mAliyunTemplateBuilder.seek(progress * 1000L);
                    mPlayView.setImageResource(R.mipmap.aliyun_svideo_play);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mPlayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAliyunTemplateBuilder.isPlaying()) {
                    mAliyunTemplateBuilder.pause();
                    mPlayView.setImageResource(R.mipmap.aliyun_svideo_play);
                } else {
                    mAliyunTemplateBuilder.play();
                    mPlayView.setImageResource(R.mipmap.aliyun_svideo_pause);
                }
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        int result = mAliyunTemplateBuilder.init(mSurfaceView, layoutParams.width, layoutParams.height);
        if (result != AliyunErrorCode.ALIVC_COMMON_RETURN_SUCCESS) {
            //没有增值服务权限，无法使用
            ToastUtil.showToast(getApplicationContext(), R.string.alivc_editor_toast_no_authorization);
            finish();
            return;
        }
        mTvDuration.setText(DateTimeUtils.formatMs(mAliyunTemplateBuilder.getDuration() / 1000));
        mSeekBar.setMax((int) (mAliyunTemplateBuilder.getDuration() / 1000));
        mAliyunTemplateBuilder.play();
        mPlayView.setImageResource(R.mipmap.aliyun_svideo_pause);
        List<AliyunTemplateParam> list = mAliyunTemplateBuilder.getAllParams();
        mTemplateInputAdapter = new TemplateInputAdapter();
        mTemplateInputAdapter.setData(list);
        mRecyclerView.setAdapter(mTemplateInputAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAliyunTemplateBuilder != null) {
            if (isNeedResume) {
                mAliyunTemplateBuilder.play();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAliyunTemplateBuilder != null) {
            isNeedResume = mAliyunTemplateBuilder.isPlaying();
            if (isNeedResume) {
                mAliyunTemplateBuilder.pause();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAliyunTemplateBuilder != null) {
            mAliyunTemplateBuilder.onDestroy();
            mAliyunTemplateBuilder = null;
        }
    }

    @Override
    public void onClick(final View view) {
        if (view.getId() == R.id.iv_left) {
            finish();
        } else if (view.getId() == R.id.tv_right) {
            if (mAliyunTemplateBuilder != null) {
                TemplateTitleDialogFragment templateTitleDialogFragment = new TemplateTitleDialogFragment();
                templateTitleDialogFragment.setOnTitleListener(new TemplateTitleDialogFragment.OnTitleListener() {
                    @Override
                    public void onTitle(String title) {
                        if (!StringUtils.isEmpty(title)) {
                            File appFilesDir = getExternalFilesDir(null);
                            File templateDir = new File(appFilesDir.getAbsolutePath() + File.separator + TemplateManager.TEMPLATE_LIST_DIR + File.separator + System.currentTimeMillis());
                            if (!templateDir.exists()) {
                                templateDir.mkdirs();
                            }
                            try {
                                //复制示例视频到模板目录下
                                File videoFile = new File(mOutputPath);
                                File videoDestFile = new File(templateDir, videoFile.getName());
                                FileUtils.copyFile(videoFile, videoDestFile);
                                Source videoSource = new Source(videoDestFile.getPath());
                                videoSource.setURL(AlivcResUtil.getRelationResUri(videoFile.getName()));

                                //复制示例封面到模板目录下
                                File coverFile = new File(mAliyunTemplateBuilder.getEditorProject().getCover().getPath());
                                File coverDestFile = new File(templateDir, coverFile.getName());
                                FileUtils.copyFile(coverFile, coverDestFile);
                                Source coverSource = new Source(coverDestFile.getPath());
                                coverSource.setURL(mAliyunTemplateBuilder.getEditorProject().getCover().getURL());
                                if (StringUtils.isEmpty(coverSource.getURL())) {
                                    coverSource.setURL(AlivcResUtil.getRelationResUri(coverFile.getName()));
                                }

                                File projectFile = new File(templateDir, AliyunEditorProject.PROJECT_FILENAME);
                                Source projectSource = new Source(projectFile.getPath());
                                projectSource.setURL(AlivcResUtil.getRelationResUri(AliyunEditorProject.PROJECT_FILENAME));

                                final Context context = view.getContext();
                                final AlivcCircleLoadingDialog dialog = new AlivcCircleLoadingDialog(context, 0);
                                dialog.show();
                                mAliyunTemplateBuilder.build(templateDir, title, videoSource, coverSource,projectSource, mTemplateInputAdapter.getData(), new AliyunTemplateSourceHandleCallback() {
                                    @Override
                                    public void onHandleResourceTasks(String templateDir, List<AliyunResTask> tasks) {
                                        Log.d("template","exportTemplate->onHandleResourceTasks");
                                        HashMap<String, List<AliyunResTask>> map = new HashMap<>();
                                        //过虑重复资源
                                        for (AliyunResTask task : tasks) {
                                            if (task.getSource() == null || task.getSource().getPath() == null) {
                                                task.onIgnore();
                                                continue;
                                            }
                                            //不以alivc_resource开头需要做处理
                                            String url = task.getSource().getURL();
                                            if (StringUtils.isEmpty(url) || !url.startsWith("alivc_resource")) {
                                                if (map.containsKey(task.getSource().getPath())) {
                                                    map.get(task.getSource().getPath()).add(task);
                                                } else {
                                                    List<AliyunResTask> list = new ArrayList<>();
                                                    list.add(task);
                                                    map.put(task.getSource().getPath(), list);
                                                }
                                            } else {
                                                //忽略出错
                                                task.onIgnore();
                                            }
                                        }
                                        for (Map.Entry<String, List<AliyunResTask>> entry : map.entrySet()) {
                                            try {
                                                String path = entry.getKey();
                                                if (path == null) {
                                                    continue;
                                                }
                                                File srcFile = new File(path);
                                                File destFile = new File(templateDir, srcFile.getName());
                                                if (!path.contains(templateDir)) {
                                                    FileUtils.copyFile(srcFile, destFile);
                                                }
                                                List<AliyunResTask> list = entry.getValue();
                                                for (AliyunResTask task:list){
                                                    Source source = task.getSource();
                                                    source.setPath(destFile.getPath());
                                                    source.setURL(AlivcResUtil.getRelationResUri(srcFile.getName()));
                                                    task.onHandleCallback(source);
                                                }
                                            } catch (Exception e) {
                                                //忽略出错
                                                List<AliyunResTask> list = entry.getValue();
                                                for (AliyunResTask item:list){
                                                    item.onIgnore();
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onSuccess() {
                                        dialog.dismiss();
                                        finish();
                                    }

                                    @Override
                                    public void onFailure(String msg) {
                                        dialog.dismiss();
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                templateTitleDialogFragment.show(getSupportFragmentManager(), CloudDraftConfigDialogFragment.class.getSimpleName());
            }
        }
    }
}
