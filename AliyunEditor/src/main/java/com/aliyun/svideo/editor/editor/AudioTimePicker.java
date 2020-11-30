package com.aliyun.svideo.editor.editor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.editor.thumblinebar.OverlayThumbLineBar;
import com.aliyun.svideo.editor.editor.thumblinebar.ThumbLineOverlay;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;

/**
 * Created by aa on 2017/12/14.
 */

public class AudioTimePicker {

    private View picker;
    private OverlayThumbLineBar timelineBar;
    private ThumbLineOverlay audioOverlay;
    private long videoDuration;
    private long start;
    private long end;
    private ThumbLineOverlay.ThumbLineOverlayView overlayView;

    AudioTimePicker(Context ctx, View v, OverlayThumbLineBar b, long duration) {
        picker = v;
        timelineBar = b;
        videoDuration = end = duration;
        final View rootView = LayoutInflater.from(ctx).inflate(R.layout.alivc_editor_view_timeline_overlay, null);
        overlayView = new ThumbLineOverlay.ThumbLineOverlayView() {

            @Override
            public ViewGroup getContainer() {
                return (ViewGroup) rootView;
            }

            @Override
            public View getHeadView() {
                return rootView.findViewById(R.id.head_view);
            }

            @Override
            public View getTailView() {
                return rootView.findViewById(R.id.tail_view);
            }

            @Override
            public View getMiddleView() {
                return rootView.findViewById(R.id.middle_view);
            }
        };
    }

    void showAudioTimePicker() {
        picker.setVisibility(View.VISIBLE);
        if (audioOverlay == null) {
            audioOverlay = timelineBar.addOverlay(0, videoDuration, overlayView, 0, false, UIEditorPage.AUDIO_MIX
            , new ThumbLineOverlay.OnSelectedDurationChangeListener() {
                @Override
                public void onDurationChange(long startTime, long endTime, long duration) {
                    start = startTime;
                    end = endTime;
                }
            });

        }

        audioOverlay.switchState(ThumbLineOverlay.STATE_ACTIVE);

    }

    void hideAudioTimePicker() {
        picker.setVisibility(View.GONE);
        if (audioOverlay != null) {
            audioOverlay.switchState(ThumbLineOverlay.STATE_FIX);
        }
    }

    void removeAudioTimePicker() {
        picker.setVisibility(View.GONE);
        if (audioOverlay != null) {
            timelineBar.removeOverlay(audioOverlay);
            audioOverlay = null;
        }
    }

    long getStart() {
        return start;
    }

    long getEnd() {
        return end;
    }

}
