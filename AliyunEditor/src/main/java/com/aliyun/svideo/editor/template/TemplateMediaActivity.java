package com.aliyun.svideo.editor.template;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.widget.Toast;

import com.aliyun.common.utils.StringUtils;
import com.aliyun.svideo.common.utils.FastClickUtil;
import com.aliyun.svideo.common.utils.ToastUtils;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.media.MediaInfo;
import com.aliyun.svideo.media.MutiMediaView;
import com.aliyun.svideosdk.common.struct.common.AliyunClip;
import com.aliyun.svideosdk.common.struct.common.AliyunImageClip;
import com.aliyun.svideosdk.common.struct.common.AliyunVideoClip;
import com.aliyun.svideosdk.common.struct.project.AliyunEditorProject;
import com.aliyun.svideosdk.common.struct.template.AliyunTemplate;
import com.aliyun.svideosdk.common.struct.template.AliyunTemplateParam;
import com.aliyun.svideosdk.editor.template.AliyunTemplateFactory;
import com.aliyun.svideosdk.transcode.NativeParser;

import java.util.ArrayList;
import java.util.List;

/**
 * 编辑模块的media选择Activity
 */
public class TemplateMediaActivity extends Activity {
    public static final String TEMPLATE_PATH = "Template_Path";
    public static final String CLIP_DURATION = "Clip_Duration";
    public static final String RESULT_PATH = "Result_Path";
    private MutiMediaView mMutiMediaView;
    private AliyunTemplate template;
    private String mTemplatePath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alivc_editor_media);
        init();
    }

    private void init() {
        mTemplatePath = getIntent().getStringExtra(TEMPLATE_PATH);
        mMutiMediaView = findViewById(R.id.media_view);
        mMutiMediaView.setMode(MutiMediaView.MODE_TEMPLATE_IMPORT);
        List<Long> list = new ArrayList<>();
        if (StringUtils.isEmpty(mTemplatePath)) {
            long duration = getIntent().getLongExtra(CLIP_DURATION, -1);
            if (duration == -1) {
                Toast.makeText(TemplateMediaActivity.this, "参数异常", Toast.LENGTH_SHORT).show();
                finish();
            }
            list.add(duration);
            mMutiMediaView.enableTemplateReplace(duration);
        } else {
            template = AliyunTemplateFactory.getAliyunTemplate(Uri.parse(mTemplatePath));
            if (template != null) {
                List<AliyunTemplateParam> params = template.getImportParams();
                for (AliyunTemplateParam param : params) {
                    list.add((long) ((param.getTimelineOut() - param.getTimelineIn()) * 1000));
                }
            }
            mMutiMediaView.enableTemplateImportView(list);
        }


        mMutiMediaView.setOnTemplateActionListener(new MutiMediaView.OnTemplateActionListener() {
            @Override
            public void onTemplateImport(List<MediaInfo> data) {
                if (FastClickUtil.isFastClick()) {
                    return;
                }
                List<AliyunClip> clips = new ArrayList<>();
                for (MediaInfo mediaInfo : data) {
                    if (mediaInfo.mimeType.startsWith("video")) {
                        clips.add(new AliyunVideoClip.Builder()
                                .source(mediaInfo.filePath)
                                .startTime(mediaInfo.startTime)
                                .endTime(mediaInfo.startTime + mediaInfo.duration)
                                .duration(mediaInfo.duration)
                                .build());
                    } else if (mediaInfo.mimeType.startsWith("image")) {
                        clips.add(new AliyunImageClip.Builder()
                                .source(mediaInfo.filePath)
                                .duration(mediaInfo.duration)
                                .build());
                    }
                }
                if (template != null) {
                    AliyunEditorProject project = template.createEditorProject(TemplateMediaActivity.this, clips);
                    Intent intent = new Intent();
                    intent.putExtra(RESULT_PATH, project.getProjectFile().getAbsolutePath());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }

            @Override
            public void onBack() {
                finish();
            }
        });

        mMutiMediaView.setOnMediaClickListener(new MutiMediaView.OnMediaClickListener() {
            @Override
            public void onClick(MediaInfo info) {
                MediaInfo infoCopy = new MediaInfo();
                infoCopy.addTime = info.addTime;
                infoCopy.mimeType = info.mimeType;
                if (info.mimeType.startsWith("image")) {
                    if (info.filePath.endsWith("gif") || info.filePath.endsWith("GIF")) {
                        NativeParser parser = new NativeParser();
                        parser.init(info.filePath);
                        int frameCount;

                        try {
                            frameCount = Integer.parseInt(parser.getValue(NativeParser.VIDEO_FRAME_COUNT));
                        } catch (Exception e) {
                            ToastUtils.show(TemplateMediaActivity.this, R.string.alivc_editor_error_tip_play_video_error);
                            parser.release();
                            parser.dispose();
                            return;
                        }
                        //当gif动图为一帧的时候当作图片处理，否则当作视频处理
                        if (frameCount > 1) {
                            int duration;
                            try {
                                duration = Integer.parseInt(parser.getValue(NativeParser.VIDEO_DURATION)) / 1000;
                            } catch (Exception e) {
                                ToastUtils.show(TemplateMediaActivity.this, R.string.alivc_editor_error_tip_play_video_error);
                                parser.release();
                                parser.dispose();
                                return;
                            }
                            infoCopy.mimeType = "video";
                            infoCopy.duration = duration;
                        }
                        parser.release();
                        parser.dispose();

                    }
                } else {
                    infoCopy.duration = info.duration;
                }
                infoCopy.filePath = info.filePath;
                infoCopy.fileUri = info.fileUri;
                infoCopy.id = info.id;
                infoCopy.isSquare = info.isSquare;
                infoCopy.thumbnailPath = info.thumbnailPath;
                infoCopy.thumbnailUri = info.thumbnailUri;
                infoCopy.title = info.title;
                infoCopy.type = info.type;

                if (StringUtils.isEmpty(mTemplatePath)) {
                    Intent data = new Intent();
                    data.putExtra("MediaInfo", infoCopy);
                    setResult(Activity.RESULT_OK, data);
                    finish();
                    return;
                }

                mMutiMediaView.addSelectMedia(infoCopy);
            }
        });
        mMutiMediaView.loadMedia();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMutiMediaView.onDestroy();
    }

}
