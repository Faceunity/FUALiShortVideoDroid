package com.aliyun.svideo.editor.effects.control;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.aliyun.svideo.editor.editor.AliyunBasePasterController;
import com.aliyun.svideo.editor.editor.thumblinebar.OverlayThumbLineBar;
import com.aliyun.svideo.editor.editor.thumblinebar.ThumbLineBar;
import com.aliyun.svideo.editor.view.AlivcEditView;

/**
 * Created by cross_ly on 2018/8/27.
 * <p>描述:
 */
public abstract class BaseChooser extends FrameLayout {

    protected final static String TAG = BaseChooser.class.getName();

    public static final int CAPTION_REQUEST_CODE = 1001;
    public static final int IMV_REQUEST_CODE = 1002;
    public static final int PASTER_REQUEST_CODE = 1003;
    public static final int ANIMATION_FILTER_REQUEST_CODE = 1004;
    public static final int TRANSITION_EFFECT_REQUEST_CODE = 1005;
    public static final int ROLL_CAPTION_REQUEST_CODE = 1006;

    public EditorService mEditorService;
    public OnEffectChangeListener mOnEffectChangeListener;
    protected OverlayThumbLineBar mThumbLineBar;
    protected OnEffectActionLister mOnEffectActionLister;
    protected FrameLayout mThumbContainer;
    protected int mCurrID = 0;
    protected AlivcEditView.PlayerListener mPlayerListener;
    private View mTransparentView;

    public BaseChooser(@NonNull Context context) {
        super(context);
    }

    public BaseChooser(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseChooser(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        mThumbContainer = getThumbContainer();
    }

    /**
     * 需要显示缩略图滑动条的子类选择实现
     * @return FrameLayout
     */
    protected FrameLayout getThumbContainer() {
        return null;
    }

    /**
     * 初始化
     */
    protected abstract void init();

    public void setEditorService(EditorService editorService) {
        this.mEditorService = editorService;
    }

    public void setOnEffectChangeListener(OnEffectChangeListener onEffectChangeListener) {
        mOnEffectChangeListener = onEffectChangeListener;
    }

    public void setOnEffectActionLister(OnEffectActionLister onEffectActionLister) {
        mOnEffectActionLister = onEffectActionLister;
    }

    /**
     * 添加缩略图滑动条
     *
     * @param thumbLineBar SimpleThumbLineBar
     */
    @SuppressLint("ResourceType")
    public void addThumbView(OverlayThumbLineBar thumbLineBar) {

        if (thumbLineBar == null) {
            //因为View初始化和thumbLineBar初始化之间的时间差，在monkey的情况下可能会出现thumbLineBar = null的情况
            Log.e(TAG, "addThumbView error , thumbLineBar = null");
            return;
        }

        ViewParent parent = thumbLineBar.getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(thumbLineBar);
        }
        if (mThumbContainer != null) {
            mThumbContainer.removeAllViews();
            mThumbContainer.addView(thumbLineBar);
            //显示指定的overlay覆盖
            thumbLineBar.showOverlay(getUIEditorPage());
            thumbLineBar.setBackgroundResource(Color.TRANSPARENT);
            thumbLineBar.show();
            this.mThumbLineBar = thumbLineBar;
        } else {
            Log.w(TAG, "addThumbView error , mThumbContainer = null");
        }

    }

    /**
     * 子类继承实现 用于显示指定的缩略图覆盖效果
     *
     * @return UIEditorPage
     */
    protected UIEditorPage getUIEditorPage() {
        return null;
    }

    public void removeOwn() {
        ViewParent parent = this.getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(this);
        }
        onRemove();
    }

    /**
     * 设置整个容器是否可点击
     *
     * @param clickable 默认可点击
     */
    @Override
    public void setClickable(boolean clickable) {
        setClickable(this, clickable);
    }

    private void setClickable(ViewGroup viewGroup, boolean clickable) {
        if (mTransparentView == null) {
            mTransparentView = new ImageButton(getContext());

            ViewGroup.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            mTransparentView.setLayoutParams(layoutParams);
            mTransparentView.setBackgroundColor(0x00000000);
        }
        ViewParent parent = mTransparentView.getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(mTransparentView);
        }
        viewGroup.addView(mTransparentView);
        if (clickable) {
            mTransparentView.setVisibility(GONE);
        } else {
            mTransparentView.setVisibility(VISIBLE);
        }
    }

    public void setThumbScrollEnable(boolean enable) {
        if (mThumbContainer != null) {
            setClickable(mThumbContainer, enable);
        }
    }

    /**
     * remove this 的时候调用，用于销毁资源
     */
    protected void onRemove() {
        Log.d(TAG, "---------------- onRemove -------------");
        if (mThumbContainer != null) {
            View childAt = mThumbContainer.getChildAt(0);
            if (childAt != null) {
                ((ThumbLineBar) childAt).hide();
            }
        }
    }

    public int getCalculateHeight() {

        int measuredHeight = getMeasuredHeight();
        if (measuredHeight == 0) {
            measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
            measuredHeight = getMeasuredHeight();
        }
        return measuredHeight;
    }

    /**
     * 是否需要缩放播放界面
     *
     * @return boolean
     */
    public abstract boolean isPlayerNeedZoom();

    /**
     * 是否显示确认界面
     *
     * @return boolean
     */
    public boolean isShowSelectedView() {
        return true;
    }

    public void onBackPressed() {
    }

    /**
     * ˙
     * 用于点击屏幕是否恢复已渲染贴纸的判断
     * 判断是否支持此贴纸的操作
     * 默认返回false，在字幕、动图等支持贴纸的子类中做自己的实现
     *
     * @param uic PasterUISimpleImpl
     * @return boolean
     */
    public boolean isHostPaster(AliyunBasePasterController uic) {
        return false;
    }

    public void setPlayerListener(AlivcEditView.PlayerListener playerListener) {
        this.mPlayerListener = playerListener;
    }
}
