/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.effects.imv;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.aliyun.quview.SquareFrameLayout;
import com.aliyun.demo.editor.R;

public class IMVPreviewDialog extends DialogFragment {

    private static final String KEY_VIDEO_URL = "video_url";
    private static final String KEY_THUMB_URL = "thumb_url";
    private ViewMediaPlayer mPlayer;
    private String mVideoUrl;

    public static IMVPreviewDialog newInstance(String videoUrl, String thumbUrl){
        IMVPreviewDialog dialog = new IMVPreviewDialog();
        Bundle args=new Bundle();
        args.putString(KEY_VIDEO_URL, videoUrl);
        args.putString(KEY_THUMB_URL, thumbUrl);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.ResourcePreviewStyle);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = super.onCreateDialog(savedInstanceState);
        d.setCanceledOnTouchOutside(true);
        d.setCancelable(true);
        return d;
    }

    private Context mContext;
    private SquareFrameLayout mVideoView;
    private ImageView thumbView;
    private ImageView mCloseBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.aliyun_svideo_imv_preview_dialog_layout, container);
        mContext = view.getContext();
        mVideoView = (SquareFrameLayout) view.findViewById(R.id.imv_video_view);
        mCloseBtn = (ImageView) view.findViewById(R.id.iv_imv_close_btn);
        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }

    private void initView() {
        mVideoUrl = getArguments().getString(KEY_VIDEO_URL);
        thumbView = new ImageView(mContext);
        mVideoView.addView(thumbView, FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        thumbView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Glide.with(mContext).load(getArguments().getString(KEY_THUMB_URL)).into(thumbView);

        startPlayVideo();
    }

    private ViewMediaPlayer createPlayer(Context context, Uri uri) {
        ViewMediaPlayer viewMediaPlayer = new ViewMediaPlayer(context, null, uri, thumbView);
        return viewMediaPlayer;
    }

    private void startPlayVideo() {
        mPlayer = createPlayer(mContext,Uri.parse(mVideoUrl));
        if(mPlayer != null) {
            View videoView = mVideoView.findViewById(Math.abs(mVideoUrl.hashCode()));
            if (videoView != null) {
                mVideoView.removeView(videoView);
            }

            View paramView = mPlayer.getShowView();

            if (paramView != null) {
                ViewGroup vg = (ViewGroup) paramView.getParent();
                if (vg != null) {
                    vg.removeView(paramView);
                }
                mVideoView.addView(paramView, 0);
                paramView.setId(Math.abs(mVideoUrl.hashCode()));
            }

            mPlayer.startVideoPlay();
//            thumbView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        getDialog().getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        initView();
        super.onResume();
    }
}
