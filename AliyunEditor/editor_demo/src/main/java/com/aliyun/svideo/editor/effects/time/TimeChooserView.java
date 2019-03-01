package com.aliyun.svideo.editor.effects.time;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.aliyun.svideo.editor.effects.control.EffectInfo;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.aliyun.svideo.base.utils.DensityUtil;
import com.aliyun.editor.TimeEffectType;
import com.aliyun.svideo.base.utils.FastClickUtil;

public class TimeChooserView extends BaseChooser implements View.OnClickListener {

    private ImageView mTimeNone, mTimeSlow, mTimeFast, mTimeRepeat2Invert, mTimeRepeat;
    private FrameLayout flThumblinebar;
    private ImageView mCancel;
    private TextView mTvEffectTitle;
    private ImageView mIvEffectIcon;
    private ImageView mComplete;
    private boolean mIsMoment = true;
    private boolean isFirstShow = true;
    private View mView;

    public TimeChooserView(@NonNull Context context) {
        this(context, null);
    }

    public TimeChooserView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeChooserView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        mView = LayoutInflater.from(getContext()).inflate(R.layout.aliyun_svideo_time_view, null);
        addView(mView);
        mTimeNone = (ImageView)findViewById(R.id.time_effect_none);
        mTimeNone.setOnClickListener(this);
        mTimeSlow = (ImageView)findViewById(R.id.time_effect_slow);
        mTimeSlow.setOnClickListener(this);
        mTimeFast = (ImageView)findViewById(R.id.time_effect_speed_up);
        mTimeFast.setOnClickListener(this);
        mTimeRepeat2Invert = (ImageView)findViewById(R.id.time_effect_repeat_invert);
        mTimeRepeat2Invert.setOnClickListener(this);
        mTimeRepeat = findViewById(R.id.time_effect_repeat);
        mTimeRepeat.setOnClickListener(this);
        flThumblinebar = findViewById(R.id.fl_thumblinebar);

        mCancel = (ImageView)findViewById(R.id.cancel);
        mTvEffectTitle = (TextView)findViewById(R.id.tv_effect_title);
        mIvEffectIcon = (ImageView)findViewById(R.id.iv_effect_icon);
        mComplete = (ImageView)findViewById(R.id.complete);
        mIvEffectIcon.setImageResource(R.mipmap.alivc_svideo_icon_effect_time);
        mTvEffectTitle.setText(R.string.alivc_svideo_time_effect);
        mComplete.setOnClickListener(this);
        mCancel.setOnClickListener(this);
    }

    private void resetBtn() {
        mTimeNone.setSelected(false);
        mTimeSlow.setSelected(false);
        mTimeFast.setSelected(false);
        mTimeRepeat.setSelected(false);
        mTimeRepeat2Invert.setSelected(false);
    }

    private EffectInfo lastSelectEffectInfo;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        //初始化界面
        if (mEditorService != null) {
            resetBtn();
            switch (mEditorService.getLastTimeEffectInfo().timeEffectType) {
                case TIME_EFFECT_TYPE_NONE:
                    mTimeNone.setSelected(true);
                    break;
                case TIME_EFFECT_TYPE_INVERT:
                    mTimeRepeat2Invert.setSelected(true);
                    break;
                case TIME_EFFECT_TYPE_RATE:
                    if (mEditorService.getLastTimeEffectInfo().timeParam>1){
                        mTimeFast.setSelected(true);
                    }else {
                        mTimeSlow.setSelected(true);
                    }
                    break;
                case TIME_EFFECT_TYPE_REPEAT:
                    mTimeRepeat.setSelected(true);
                    break;
                default:
                    mTimeNone.setSelected(true);
                    break;
            }
        }
        if (isFirstShow){
            View contentView=LayoutInflater.from(getContext()).inflate(R.layout.alivc_svideo_tip, null, false);
            TextView textView  = contentView.findViewById(R.id.alivc_svideo_tip_first);
            textView.setText(R.string.alivc_svideo_time_tip);
            PopupWindow window=new PopupWindow( contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            window.setContentView(contentView);
            window.setOutsideTouchable(true);
            // 设置PopupWindow的背景
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int yoff =- DensityUtil.dip2px(getContext(),95 );
            window.showAsDropDown(mTimeNone,0,yoff);
            isFirstShow = false;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (mThumbLineBar.isScrolling()){
            return;
        }
        if (FastClickUtil.isFastClick()){
            return;
        }
        resetBtn();
        v.setSelected(true);
        if (id == R.id.time_effect_none) {
            if (mOnEffectChangeListener != null) {
                lastSelectEffectInfo = new EffectInfo();
                lastSelectEffectInfo.type = UIEditorPage.TIME;
                lastSelectEffectInfo.timeEffectType = TimeEffectType.TIME_EFFECT_TYPE_NONE;
                lastSelectEffectInfo.isMoment = true;
                mOnEffectChangeListener.onEffectChange(lastSelectEffectInfo);
            }
        } else if (id == R.id.time_effect_slow) {//慢速
            if (mOnEffectChangeListener != null) {
                lastSelectEffectInfo = new EffectInfo();
                lastSelectEffectInfo.type = UIEditorPage.TIME;
                lastSelectEffectInfo.timeEffectType = TimeEffectType.TIME_EFFECT_TYPE_RATE;
                lastSelectEffectInfo.timeParam = 0.5f;
                lastSelectEffectInfo.isMoment = true;
                mOnEffectChangeListener.onEffectChange(lastSelectEffectInfo);
            }
        } else if (id == R.id.time_effect_speed_up) {//快速
            if (mOnEffectChangeListener != null) {
                lastSelectEffectInfo = new EffectInfo();
                lastSelectEffectInfo.type = UIEditorPage.TIME;
                lastSelectEffectInfo.timeEffectType = TimeEffectType.TIME_EFFECT_TYPE_RATE;
                lastSelectEffectInfo.timeParam = 2.0f;
                lastSelectEffectInfo.isMoment = true;
                mOnEffectChangeListener.onEffectChange(lastSelectEffectInfo);
            }
        } else if (id == R.id.time_effect_repeat_invert) {//倒放
            if (mOnEffectChangeListener != null) {
                lastSelectEffectInfo = new EffectInfo();
                lastSelectEffectInfo.type = UIEditorPage.TIME;
                lastSelectEffectInfo.isMoment = false;
                lastSelectEffectInfo.timeEffectType = TimeEffectType.TIME_EFFECT_TYPE_INVERT;
                mOnEffectChangeListener.onEffectChange(lastSelectEffectInfo);
            }
        } else if (id == R.id.time_effect_repeat) {//重复
            lastSelectEffectInfo = new EffectInfo();
            lastSelectEffectInfo.type = UIEditorPage.TIME;
            lastSelectEffectInfo.isMoment = true;
            lastSelectEffectInfo.timeEffectType = TimeEffectType.TIME_EFFECT_TYPE_REPEAT;
            mOnEffectChangeListener.onEffectChange(lastSelectEffectInfo);
        } else if (id == R.id.cancel) {
           onBackPressed();
        } else if (id == R.id.complete) {
            //设置最后一次应用的
            if (mEditorService != null) {
                mEditorService.setLastTimeEffectInfo(lastSelectEffectInfo);
            }
            if (mOnEffectActionLister != null) {
                mOnEffectActionLister.onComplete();
            }
        }

    }

    @Override
    protected UIEditorPage getUIEditorPage() {
        return UIEditorPage.TIME;
    }

    @Override
    public void onBackPressed() {
        if (mEditorService != null&&lastSelectEffectInfo!=null) {
            mOnEffectChangeListener.onEffectChange(mEditorService.getLastTimeEffectInfo());
        }
        if (mOnEffectActionLister != null) {
            mOnEffectActionLister.onCancel();
        }
    }

    @Override
    protected FrameLayout getThumbContainer() {
        return flThumblinebar;
    }

    @Override
    public boolean isPlayerNeedZoom() {
        return true;
    }


    public void setFirstShow(boolean firstShow) {
        isFirstShow = firstShow;
    }
}

