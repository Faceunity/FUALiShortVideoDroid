package com.aliyun.demo.editor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.demo.editor.timeline.TimelineBar;
import com.aliyun.demo.editor.timeline.TimelineOverlay;

/**
 * Created by aa on 2017/12/14.
 */

public class AudioTimePicker {

    private View picker;
    private TimelineBar timelineBar;
    private TimelineOverlay audioOverlay;
    private long videoDuration;
    private long start;
    private long end;
    private TimelineOverlay.TimelineOverlayView overlayView;

    AudioTimePicker(Context ctx, View v, TimelineBar b, long duration){
        picker = v;
        timelineBar = b;
        videoDuration = end = duration;
        final View rootView = LayoutInflater.from(ctx).inflate(R.layout.aliyun_svideo_layout_timeline_overlay, null);
        overlayView = new TimelineOverlay.TimelineOverlayView() {

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

    void showAudioTimePicker(){
        picker.setVisibility(View.VISIBLE);
        if(audioOverlay == null){
            audioOverlay = timelineBar.addOverlay(0, videoDuration, overlayView, 0);
            audioOverlay.setOnSelectedDurationChangeListener(new TimelineOverlay.OnSelectedDurationChangeListener() {
                @Override
                public void onDurationChange(long startTime, long endTime, long duration) {
                    start = startTime;
                    end = endTime;
                }
            });
        }

        audioOverlay.switchState(TimelineOverlay.STATE_ACTIVE);

    }

    void hideAudioTimePicker(){
        picker.setVisibility(View.GONE);
        if(audioOverlay != null){
            audioOverlay.switchState(TimelineOverlay.STATE_FIX);
        }
    }

    void removeAudioTimePicker(){
        picker.setVisibility(View.GONE);
        if(audioOverlay != null){
            timelineBar.removeOverlay(audioOverlay);
            audioOverlay = null;
        }
    }

    long getStart(){
        return start;
    }

    long getEnd(){
        return end;
    }

}
