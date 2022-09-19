package com.aliyun.svideo.editor.effects.caption.manager;

import androidx.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.caption.component.CaptionEditorPanelView;
import com.aliyun.svideo.editor.effects.caption.listener.OnCaptionChooserStateChangeListener;
import com.aliyun.svideo.editor.widget.AliyunPasterCaptionBorderView;

public class AlivcEditorViewFactory {
    private static final String TAG = "AliEditorViewFactory";


    /**
     * 创建字幕边框
     *
     * @param pasterContainer
     * @return
     */
    @Nullable
    public static AliyunPasterCaptionBorderView obtainCaptionBorderView(ViewGroup pasterContainer) {
        if (pasterContainer == null) {
            return null;
        }
        AliyunPasterCaptionBorderView captionView = pasterContainer.findViewById(R.id.aliyun_edit_overlay);
        if (captionView == null) {
            captionView = (AliyunPasterCaptionBorderView) View.inflate(pasterContainer.getContext(),
                    R.layout.alivc_editor_view_caption_controller, null);
            pasterContainer.addView(captionView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        return captionView;
    }


    /**
     * 创建字幕样式面板
     *
     * @param rootview
     * @param onCaptionChooserStateChangeListener
     * @return
     */
    public static CaptionEditorPanelView obtainCaptionEditorPanelView(ViewGroup rootview, OnCaptionChooserStateChangeListener onCaptionChooserStateChangeListener) {
        if (rootview == null) {
            Log.e(TAG, "showCaptionEditorPanelView: params is null");
            return null;
        }
        CaptionEditorPanelView captionEditorPanelView = findCaptionEditorPanelView(rootview);
        if (captionEditorPanelView == null) {
            captionEditorPanelView = new CaptionEditorPanelView(rootview.getContext(), onCaptionChooserStateChangeListener);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            captionEditorPanelView.setTag("CaptionEditorPanelView");
            rootview.addView(captionEditorPanelView, layoutParams);
        }
        return captionEditorPanelView;
    }

    public static CaptionEditorPanelView findCaptionEditorPanelView(ViewGroup rootview) {
        if (rootview == null) {
            Log.e(TAG, "findCaptionEditorPanelView: params is null");
            return null;
        }
        return rootview.findViewWithTag("CaptionEditorPanelView");
    }


}
