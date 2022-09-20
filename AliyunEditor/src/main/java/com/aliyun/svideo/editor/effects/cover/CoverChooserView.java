package com.aliyun.svideo.editor.effects.cover;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.control.BaseChooser;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.aliyun.svideo.common.utils.DensityUtils;

/**
 * @author zsy_18 data:2018/12/25
 * 编辑界面底部弹出框，用于封面选择
 */
public class CoverChooserView extends BaseChooser implements View.OnClickListener {
    private FrameLayout mFlThumblinebar;
    private ImageView mCancel;
    private TextView mTvEffectTitle;
    private ImageView mIvEffectIcon;
    private ImageView mComplete;
    private View mView;
    /**
     * 是否第一次展示
     */
    private boolean isFirstShow;
    public CoverChooserView(@NonNull Context context) {
        this(context, null);
    }

    public CoverChooserView(@NonNull Context context,
                            @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoverChooserView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        mView = LayoutInflater.from(getContext()).inflate(R.layout.alivc_editor_view_chooser_cover, null);
        addView(mView);
        mFlThumblinebar = findViewById(R.id.fl_thumblinebar);
        mCancel = (ImageView) findViewById(R.id.cancel);
        mTvEffectTitle = (TextView) findViewById(R.id.tv_effect_title);
        mIvEffectIcon = (ImageView) findViewById(R.id.iv_effect_icon);
        mComplete = (ImageView) findViewById(R.id.complete);
        mIvEffectIcon.setImageResource(R.mipmap.alivc_svideo_icon_cover);
        mTvEffectTitle.setText(R.string.alivc_editor_effect_cover);
        mComplete.setOnClickListener(this);
        mCancel.setOnClickListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isFirstShow) {
            View contentView = LayoutInflater.from(getContext()).inflate(R.layout.alivc_editor_view_tip_first_show, null, false);
            TextView textView  = contentView.findViewById(R.id.alivc_svideo_tip_first);
            textView.setText(R.string.alivc_editor_dialog_cover_tip_applay);
            PopupWindow window = new PopupWindow( contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            window.setContentView(contentView);
            window.setOutsideTouchable(true);
            // 设置PopupWindow的背景
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int yoff = 0 - DensityUtils
                       .dip2px(getContext(), 80 );
            int xoff = DensityUtils.dip2px(getContext(), 5);
            window.showAsDropDown(mFlThumblinebar, xoff, yoff);
            isFirstShow = false;
        }
    }

    @Override
    public boolean isPlayerNeedZoom() {
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v == mComplete) {
            if (mOnEffectActionLister != null) {
                mOnEffectActionLister.onComplete();
            }
        } else if (v == mCancel) {
            onBackPressed();
        }
    }

    @Override
    protected FrameLayout getThumbContainer() {
        return mFlThumblinebar;
    }
    @Override
    public void onBackPressed() {
        if (mOnEffectActionLister != null) {
            mOnEffectActionLister.onCancel();
        }
    }
    public void setFirstShow(boolean firstShow) {
        isFirstShow = firstShow;
    }

    @Override
    protected UIEditorPage getUIEditorPage() {
        return UIEditorPage.COVER;
    }
}
