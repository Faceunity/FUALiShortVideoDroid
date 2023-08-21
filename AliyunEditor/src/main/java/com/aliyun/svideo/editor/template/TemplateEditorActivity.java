package com.aliyun.svideo.editor.template;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyun.common.utils.FileUtils;
import com.aliyun.common.utils.StringUtils;
import com.aliyun.common.utils.ToastUtil;
import com.aliyun.svideo.base.Constants;
import com.aliyun.svideo.common.utils.DateTimeUtils;
import com.aliyun.svideo.common.widget.AlivcCircleLoadingDialog;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.bean.AlivcEditOutputParam;
import com.aliyun.svideo.editor.editor.EditorActivity;
import com.aliyun.svideo.editor.publish.PublishActivity;
import com.aliyun.svideo.media.MediaInfo;
import com.aliyun.svideosdk.common.AliyunErrorCode;
import com.aliyun.svideosdk.common.struct.common.AliyunClip;
import com.aliyun.svideosdk.common.struct.common.AliyunImageClip;
import com.aliyun.svideosdk.common.struct.common.AliyunVideoClip;
import com.aliyun.svideosdk.common.struct.common.AliyunVideoParam;
import com.aliyun.svideosdk.common.struct.common.VideoDisplayMode;
import com.aliyun.svideosdk.common.struct.common.VideoQuality;
import com.aliyun.svideosdk.common.struct.encoder.VideoCodecs;
import com.aliyun.svideosdk.common.struct.project.AliyunEditorProject;
import com.aliyun.svideosdk.common.struct.project.CaptionTrack;
import com.aliyun.svideosdk.common.struct.project.PipVideoTrackClip;
import com.aliyun.svideosdk.common.struct.project.Source;
import com.aliyun.svideosdk.common.struct.project.VideoTrackClip;
import com.aliyun.svideosdk.common.struct.template.AliyunTemplateParam;
import com.aliyun.svideosdk.common.struct.template.AliyunTemplateTextParam;
import com.aliyun.svideosdk.common.struct.template.AliyunTemplateVideoParam;
import com.aliyun.svideosdk.editor.EditorCallBack;
import com.aliyun.svideosdk.editor.draft.AliyunDraft;
import com.aliyun.svideosdk.editor.draft.AliyunDraftManager;
import com.aliyun.svideosdk.editor.draft.AliyunDraftResTask;
import com.aliyun.svideosdk.editor.draft.AliyunDraftResourceDownloader;
import com.aliyun.svideosdk.editor.template.AliyunTemplateEditor;
import com.aliyun.svideosdk.editor.template.AliyunTemplateFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模板编辑
 */
public class TemplateEditorActivity extends FragmentActivity implements View.OnClickListener {
    private static final int REQUEST_MEDIA = 1001;
    public static final String KEY_PARAM_CONFIG = "project_json_path";
    private static final int TAB_VIDEO = 0;
    private static final int TAB_TEXT = 1;
    private FrameLayout mSurfaceLayout;
    private SurfaceView mSurfaceView;
    private ImageView mPlayView;
    private TextView mTvTime;
    private SeekBar mSeekBar;
    private TextView mTvDuration;
    private TextView mTvEditorVideo;
    private TextView mTvEditorText;
    private RecyclerView mRecyclerView;
    private View mVFocus;
    private Point mPasterContainerPoint;

    private TemplateEditorAdapter mTemplateEditorAdapter;

    private AliyunTemplateEditor mAliyunTemplateEditor;
    private List<AliyunTemplateParam> mVideoParamList = new ArrayList<>();
    private List<AliyunTemplateParam> mTextParamList = new ArrayList<>();
    private boolean isNeedResume = true;

    private AliyunTemplateParam mReplaceParam;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aliyun_svideo_activity_template_editor);
        mSurfaceLayout = findViewById(R.id.layout_surface);
        mSurfaceView = findViewById(R.id.surface_view);
        mPlayView = findViewById(R.id.aliyun_template_btn_play);
        mTvTime = findViewById(R.id.aliyun_template_tv_time);
        mSeekBar = findViewById(R.id.aliyun_template_play_seekbar);
        mTvDuration = findViewById(R.id.aliyun_template_tv_duration);
        mTvEditorVideo = findViewById(R.id.tv_editor_video);
        mTvEditorVideo.setOnClickListener(this);
        mTvEditorText = findViewById(R.id.tv_editor_text);
        mTvEditorText.setOnClickListener(this);
        findViewById(R.id.tv_draft).setOnClickListener(this);
        mRecyclerView = findViewById(R.id.recycler_view);
        mVFocus = findViewById(R.id.v_focus);
        TextView tvRight = (TextView) findViewById(R.id.tv_right);
        ImageView ivLeft = (ImageView) findViewById(R.id.iv_left);
        ivLeft.setOnClickListener(this);
        ivLeft.setVisibility(View.VISIBLE);
        ivLeft.setImageResource(R.mipmap.aliyun_svideo_icon_back);
        tvRight.setText(R.string.alivc_base_svideo_next);
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
        String path = getIntent().getStringExtra(KEY_PARAM_CONFIG);
        try {
            mAliyunTemplateEditor = AliyunTemplateFactory.createAliyunTemplateEditor(Uri.parse(path));
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.alivc_editor_template_editor_load_failed, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mSurfaceLayout.getLayoutParams();
        DisplayMetrics d = getResources().getDisplayMetrics();
        int outputWidth = mAliyunTemplateEditor.getEditorProject().getConfig().getOutputWidth();
        int outputHeight = mAliyunTemplateEditor.getEditorProject().getConfig().getOutputHeight();
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
        mPasterContainerPoint = new Point(width, height);
        mAliyunTemplateEditor.setPlayerCallBack(new EditorCallBack() {
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
            public int onCustomRender(int srcTextureID, int width, int height, long frameTime) {
                return 0;
            }

            @Override
            public int onTextureRender(int srcTextureID, int width, int height, long frameTime) {
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
                    mAliyunTemplateEditor.seek(progress * 1000L);
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
                if (mAliyunTemplateEditor.isPlaying()) {
                    mAliyunTemplateEditor.pause();
                    mPlayView.setImageResource(R.mipmap.aliyun_svideo_play);
                } else {
                    mAliyunTemplateEditor.play();
                    mPlayView.setImageResource(R.mipmap.aliyun_svideo_pause);
                    mVFocus.setVisibility(View.GONE);
                }
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        int result = mAliyunTemplateEditor.init(mSurfaceView, layoutParams.width, layoutParams.height);
        if (result != AliyunErrorCode.ALIVC_COMMON_RETURN_SUCCESS) {
            //没有增值服务权限，无法使用
            ToastUtil.showToast(getApplicationContext(), R.string.alivc_editor_toast_no_authorization);
            finish();
            return;
        }
        mTvDuration.setText(DateTimeUtils.formatMs(mAliyunTemplateEditor.getDuration() / 1000));
        mSeekBar.setMax((int) (mAliyunTemplateEditor.getDuration() / 1000));
        mAliyunTemplateEditor.play();
        mPlayView.setImageResource(R.mipmap.aliyun_svideo_pause);
        List<AliyunTemplateParam> paramList = mAliyunTemplateEditor.getAllParams();
        for (AliyunTemplateParam item : paramList) {
            if (item.getType() == AliyunTemplateParam.Type.video) {
                mVideoParamList.add(item);
            } else if (item.getType() == AliyunTemplateParam.Type.text) {
                mTextParamList.add(item);
            }
        }
        if (mTextParamList.isEmpty()) {
            mTvEditorText.setVisibility(View.GONE);
        }
        mTemplateEditorAdapter = new TemplateEditorAdapter();
        mTemplateEditorAdapter.setOnItemClickCallback(new TemplateEditorAdapter.OnItemClickCallback() {
            @Override
            public void onEdit(final AliyunTemplateParam param) {
                if (param.getTarget() instanceof CaptionTrack) {
                    final CaptionTrack pasterTrack = (CaptionTrack) param.getTarget();
                    TemplateTextEditDialogFragment templateTextEditDialogFragment = new TemplateTextEditDialogFragment();
                    templateTextEditDialogFragment.setText(pasterTrack.getText());
                    templateTextEditDialogFragment.setOnResultListener(new TemplateTextEditDialogFragment.OnResultListener() {
                        @Override
                        public void onResult(String text) {
                            mAliyunTemplateEditor.updateCaption((AliyunTemplateTextParam) param, text);
                            ((AliyunTemplateTextParam) param).setText(text);
                            onSelect(param);
                        }
                    });
                    templateTextEditDialogFragment.show(getSupportFragmentManager(), TemplateTextEditDialogFragment.class.getSimpleName());
                } else if (param.getTarget() instanceof VideoTrackClip) {
                    mReplaceParam = param;
                    Intent intent = new Intent(TemplateEditorActivity.this, TemplateMediaActivity.class);
                    VideoTrackClip clip = (VideoTrackClip) param.getTarget();
                    intent.putExtra(TemplateMediaActivity.CLIP_DURATION, (long) ((clip.getTimelineOut() - clip.getTimelineIn()) * 1000));
                    startActivityForResult(intent, REQUEST_MEDIA);
                }
            }

            @Override
            public void onSelect(AliyunTemplateParam param) {
                if (param.getTarget() instanceof VideoTrackClip) {
                    VideoTrackClip clip = (VideoTrackClip) param.getTarget();
                    if (param.getTarget() instanceof PipVideoTrackClip) {
                        mAliyunTemplateEditor.seek((long) (clip.getTimelineIn() * 1000000));
                        PipVideoTrackClip pipVideoTrackClip = (PipVideoTrackClip) param.getTarget();
                        int screenWidth = mSurfaceView.getWidth();
                        int screenHeight = mSurfaceView.getHeight();
                        int videoWidth = pipVideoTrackClip.getWidth();
                        int videoHeight = pipVideoTrackClip.getHeight();
                        float ratioW = 1.0F * videoWidth / screenWidth;
                        float ratioH = 1.0F * videoHeight / screenHeight;
                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mVFocus.getLayoutParams();
                        if (ratioW > ratioH) {
                            layoutParams.width = (int) (screenWidth * pipVideoTrackClip.getScale());
                            layoutParams.height = (int) (layoutParams.width * 1.0f * videoHeight / videoWidth);
                        } else {
                            layoutParams.height = (int) (screenHeight * pipVideoTrackClip.getScale());
                            layoutParams.width = (int) (layoutParams.height * 1.0f * videoWidth / videoHeight);
                        }
                        mVFocus.setLayoutParams(layoutParams);
                        int fX = (int) (pipVideoTrackClip.getCenterX() * screenWidth);
                        int fY = (int) (pipVideoTrackClip.getCenterY() * screenHeight);
                        mVFocus.setTranslationX(fX - (layoutParams.width / 2));
                        mVFocus.setTranslationY(fY - (layoutParams.height / 2));
                        if (pipVideoTrackClip.getRotation() != -1) {
                            mVFocus.setRotation((float) Math.toDegrees(pipVideoTrackClip.getRotation()));
                        }
                    } else {
                        int clipIndex = mAliyunTemplateEditor.getEditorProject().getTimeline().getPrimaryTrack().getVideoTrackClips().indexOf(clip);
                        if (clipIndex > -1) {
                            long clipStart = mAliyunTemplateEditor.getMainClipStartTime(clipIndex);
                            mAliyunTemplateEditor.seek(clipStart);
                        }
                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mVFocus.getLayoutParams();
                        layoutParams.width = mSurfaceView.getWidth();
                        layoutParams.height = mSurfaceView.getHeight();
                        mVFocus.setLayoutParams(layoutParams);
                        mVFocus.setTranslationX(0);
                        mVFocus.setTranslationY(0);
                        mVFocus.setRotation(0);
                    }

                } else if (param.getTarget() instanceof CaptionTrack) {
                    int width = mSurfaceView.getWidth();
                    int height = mSurfaceView.getHeight();
                    CaptionTrack captionTrack = (CaptionTrack) param.getTarget();
                    captionTrack.updateInfo();
                    float scale = captionTrack.getScale();
                    int fWidth = (int) (captionTrack.getWidth() * width * scale);
                    int fHeight = (int) (captionTrack.getHeight() * height * scale);
                    int fX = (int) (captionTrack.getX() * width);
                    int fY = (int) (captionTrack.getY() * height);
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mVFocus.getLayoutParams();
                    layoutParams.width = fWidth;
                    layoutParams.height = fHeight;
                    mVFocus.setLayoutParams(layoutParams);
                    mVFocus.setRotation((float) Math.toDegrees(-captionTrack.getRotation()));
                    mVFocus.setTranslationX(fX - (fWidth / 2));
                    mVFocus.setTranslationY(fY - (fHeight / 2));
                    mVFocus.postInvalidate();
                    mAliyunTemplateEditor.getEditorProject().getConfig().getOutputWidth();
                    mAliyunTemplateEditor.seek((long) (captionTrack.getTimelineIn() * 1000000));
                }
                mPlayView.setImageResource(R.mipmap.aliyun_svideo_play);
                mVFocus.setVisibility(View.VISIBLE);
            }
        });
        onTabChanged(TAB_VIDEO);
        mRecyclerView.setAdapter(mTemplateEditorAdapter);


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAliyunTemplateEditor != null) {
            if (isNeedResume) {
                mAliyunTemplateEditor.play();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAliyunTemplateEditor != null) {
            isNeedResume = mAliyunTemplateEditor.isPlaying();
            if (isNeedResume) {
                mAliyunTemplateEditor.pause();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAliyunTemplateEditor != null) {
            mAliyunTemplateEditor.onDestroy();
            mAliyunTemplateEditor = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_MEDIA == requestCode && mReplaceParam != null) {
            if (resultCode == Activity.RESULT_OK) {
                MediaInfo mediaInfo = data.getParcelableExtra("MediaInfo");
                AliyunClip aliyunClip = null;
                long duration = (long) ((mReplaceParam.getTimelineOut() - mReplaceParam.getTimelineIn()) * 1000);
                if (mediaInfo.mimeType.startsWith("video")) {
                    aliyunClip = new AliyunVideoClip.Builder()
                            .source(mediaInfo.filePath)
                            .startTime(mediaInfo.startTime)
                            .endTime(mediaInfo.startTime + duration)
                            .duration(mediaInfo.duration)
                            .build();
                } else if (mediaInfo.mimeType.startsWith("image")) {
                    aliyunClip = new AliyunImageClip.Builder()
                            .source(mediaInfo.filePath)
                            .duration(duration)
                            .build();
                }
                mAliyunTemplateEditor.updateMediaClip((AliyunTemplateVideoParam) mReplaceParam, aliyunClip);
                ((AliyunTemplateVideoParam) mReplaceParam).setSource(new Source(aliyunClip.getSource()));
                mTemplateEditorAdapter.notifyDataSetChanged();
            }
            mReplaceParam = null;
        }
    }

    @Override
    public void onClick(final View view) {
        if (view.getId() == R.id.iv_left) {
            finish();
        } else if (view.getId() == R.id.tv_right) {
            jumpToNextActivity();
        } else if (view.getId() == R.id.tv_editor_video) {
            if (!view.isSelected()) {
                onTabChanged(TAB_VIDEO);
            }
        } else if (view.getId() == R.id.tv_editor_text) {
            if (!view.isSelected()) {
                onTabChanged(TAB_TEXT);
            }
        } else if (view.getId() == R.id.tv_draft) {
            final Context context = view.getContext();
            final AlivcCircleLoadingDialog dialog = new AlivcCircleLoadingDialog(context, 0);
            dialog.show();
            final String srcFileDir = mAliyunTemplateEditor.getEditorProject().getProjectDir().getAbsolutePath();
            AliyunDraftManager.getInstance(context).downloadDraft(mAliyunTemplateEditor.getEditorProject().getProjectFile(), new AliyunDraftResourceDownloader() {
                @Override
                public void onHandleResourceTasks(String projectDir, List<AliyunDraftResTask> tasks) {
                    HashMap<String, List<AliyunDraftResTask>> map = new HashMap<>();
                    //过虑重复资源
                    for (AliyunDraftResTask task : tasks) {
                        if (task.getSource() != null && StringUtils.isEmpty(task.getSource().getPath())) {
                            task.onIgnore();
                        } else if (map.containsKey(task.getSource().getPath())) {
                            map.get(task.getSource().getPath()).add(task);
                        } else {
                            List<AliyunDraftResTask> list = new ArrayList<>();
                            list.add(task);
                            map.put(task.getSource().getPath(), list);
                        }
                    }
                    for (final Map.Entry<String, List<AliyunDraftResTask>> entry : map.entrySet()) {
                        final List<AliyunDraftResTask> list = entry.getValue();
                        try {
                            //复制原目录资源到新草稿目录下
                            File sourceFile = new File(entry.getKey());
                            if (sourceFile.getParentFile().getAbsolutePath().equals(srcFileDir)) {
                                File destFile = new File(projectDir, sourceFile.getName());
                                FileUtils.copyFile(sourceFile, destFile);
                                for (AliyunDraftResTask task : list) {
                                    Source source = task.getSource();
                                    source.setPath(destFile.getAbsolutePath());
                                    task.onHandleCallback(source);
                                }
                            } else {
                                for (AliyunDraftResTask item : list) {
                                    item.onIgnore();
                                }
                            }
                        } catch (Exception e) {
                            //出错
                            for (AliyunDraftResTask item : list) {
                                item.onIgnore();
                            }
                        }
                    }
                }

                @Override
                public void onSuccess(AliyunDraft draft) {
                    dialog.dismiss();
                    EditorActivity.startEdit(context, draft);
                }

                @Override
                public void onFailure(String msg) {
                    dialog.dismiss();
                    Toast.makeText(context, R.string.alivc_editor_template_editor_draft_failed, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void onTabChanged(int tab) {
        if (TAB_VIDEO == tab) {
            mTvEditorVideo.setSelected(true);
            mTvEditorText.setSelected(false);
            mTemplateEditorAdapter.setData(mVideoParamList);
        } else if (TAB_TEXT == tab) {
            mTvEditorVideo.setSelected(false);
            mTvEditorText.setSelected(true);
            mTemplateEditorAdapter.setData(mTextParamList);
        }
    }

    private void jumpToNextActivity() {
        AlivcEditOutputParam outputParam = new AlivcEditOutputParam();
        AliyunEditorProject project = mAliyunTemplateEditor.getEditorProject();
        outputParam.setConfigPath(project.getProjectFile().getAbsolutePath());
        outputParam.setOutputVideoHeight(project.getConfig().getOutputHeight());
        outputParam.setOutputVideoWidth(project.getConfig().getOutputWidth());
        outputParam.setVideoRatio(((float) mPasterContainerPoint.x) / mPasterContainerPoint.y);
        AliyunVideoParam param = new AliyunVideoParam.Builder()
                .frameRate(project.getConfig().getFps())
                .gop(project.getConfig().getGop())
                .crf(project.getConfig().getCrf())
                .videoQuality(VideoQuality.values()[project.getConfig().getVideoQuality()])
                .scaleMode(VideoDisplayMode.valueOf(project.getConfig().getDisplayMode()))
                .scaleRate(project.getConfig().getScale())
                .outputWidth(project.getConfig().getOutputWidth())
                .outputHeight(project.getConfig().getOutputHeight())
                .videoCodec(VideoCodecs.getInstanceByValue(project.getConfig().getVideoCodec()))
                .build();
        outputParam.setVideoParam(param);
        if (project.getCover() != null && !StringUtils.isEmpty(project.getCover().getPath()) && FileUtils.isFileExists(project.getCover().getPath())) {
            String path = project.getCover().getPath();
            outputParam.setThumbnailPath(path);
        } else {
            outputParam.setThumbnailPath(Constants.SDCardConstants.getDir(getApplicationContext()) + File.separator + "thumbnail.jpg");
        }

        //编辑完成跳转到其他界面
        Intent intent = new Intent();
        intent.setClassName(TemplateEditorActivity.this, EditorActivity.NEXT_ACTIVITY_CLASS_NAME);
        intent.putExtra(PublishActivity.KEY_PARAM_THUMBNAIL, outputParam.getThumbnailPath());
        intent.putExtra(PublishActivity.KEY_PARAM_CONFIG, outputParam.getConfigPath());
        intent.putExtra(PublishActivity.KEY_PARAM_VIDEO_WIDTH, outputParam.getOutputVideoWidth());
        intent.putExtra(PublishActivity.KEY_PARAM_VIDEO_HEIGHT, outputParam.getOutputVideoHeight());
        //传入视频比列
        intent.putExtra(PublishActivity.KEY_PARAM_VIDEO_RATIO, outputParam.getVideoRatio());
        intent.putExtra("videoParam", outputParam.getVideoParam());
        startActivityForResult(intent, PublishActivity.REQUEST_CODE);
    }
}
