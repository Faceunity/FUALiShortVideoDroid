package com.aliyun.svideo.editor.widget;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aliyun.svideo.editor.R;

/**
 * download progress view
 * 显示下载状态文字的progress view
 *
 * @author xlx
 */
public class DownloadProgressView extends FrameLayout {

    private ProgressBar downloadProgress;
    private TextView tvDownloadState;

    private static final int VIEW_TYPE_LOCAL = 1;
    private static final int VIEW_TYPE_REMOTE = 2;
    private static final int VIEW_TYPE_DOWNLOADING = 3;
    private ImageView downloadFinish;
    private OnDownloadBtnClickListener onDownloadBtnClickListener;

    public DownloadProgressView(@NonNull Context context) {
        this(context, null);
    }

    public DownloadProgressView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.alivc_editor_view_download_progress, this,
                    true);
        downloadProgress = view.findViewById(R.id.download_progress_bg);
        tvDownloadState = view.findViewById(R.id.tv_download_state);
        downloadFinish = view.findViewById(R.id.iv_download_finish);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDownloadBtnClickListener != null) {
                    onDownloadBtnClickListener.onClick();
                    downloadProgress.setProgress(0);
                }
            }
        });
    }

    public void setDownloadState(int state) {
        switch (state) {
        case VIEW_TYPE_REMOTE:
            // 未下载
            tvDownloadState.setText(getResources().getString(R.string.alivc_common_download));
            downloadProgress.setProgress(Integer.MAX_VALUE);
            downloadFinish.setVisibility(INVISIBLE);
            tvDownloadState.setBackgroundResource(R.drawable.alivc_svideo_shape_effect_download_normal);
            break;
        case VIEW_TYPE_DOWNLOADING:
            // 下载中
            tvDownloadState.setText(getResources().getString(R.string.alivc_editor_more_downloading));
            downloadFinish.setVisibility(INVISIBLE);
            break;
        case VIEW_TYPE_LOCAL:
            // 已完成
            tvDownloadState.setText("");
            downloadFinish.setVisibility(VISIBLE);
            downloadFinish.setImageResource(R.mipmap.alivc_icon_download_finish);
            break;
        default:
            break;
        }
    }

    /**
     * 设置下载按钮 click listener
     * @param listener OnDownloadBtnClickListener
     */
    public void setOnDownloadBtnClickListener(OnDownloadBtnClickListener listener) {
        this.onDownloadBtnClickListener = listener;
    }

    /**
     * 更新下载进度
     * @param process
     */
    public synchronized void updateProgress(final int process) {
        downloadProgress.setProgress(process);
    }

    /**
     * 下载按钮click listener
     */
    public interface OnDownloadBtnClickListener {
        /**
         * click
         */
        void onClick();
    }
}
