package com.aliyun.svideo.editor.effects.pip;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.control.BaseChooser;
import com.aliyun.svideo.editor.effects.control.SpaceItemDecoration;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.aliyun.svideo.editor.effects.pip.msg.PipAddMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipAlphaMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipAngleMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipBorderMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipBrighnessMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipContrastMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipDeleteMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipDenoiseMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipEffectMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipFrameAnimationMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipMoveMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipRadiusMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipSaturationMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipScaleMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipSharpnessMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipVignetteMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipVolumeMsg;
import com.aliyun.svideo.editor.effects.videoeq.FilterAdapter;
import com.aliyun.svideo.editor.msg.Dispatcher;
import com.aliyun.svideo.editor.msg.body.BrightnessProgressMsg;
import com.aliyun.svideo.editor.util.EditorCommon;
import com.aliyun.svideosdk.common.internal.videoaugment.VideoAugmentationType;

import java.util.ArrayList;
import java.util.List;

public class PipSettingView extends BaseChooser implements OperationAdapter.OnItemClickListener, SeekBar.OnSeekBarChangeListener{

    private RecyclerView mListView;
    private OperationAdapter mOperationAdapter;
    private SeekBar mSeekBar;
    private Operation mType = Operation.SCALE;
    private View mProgressBar;
    private static final int MAXPROGRESS = 100;
    private View mRest;
    int mScale = 30;
    int mAngle = 50;
    int mMove = 50;
    int mCorner = 0;
    int mBoarder = 0;
    int mVolume = 100;
    int mDenoise = 0;
    int mAlpha = 100;
    int mBrightness = 50;
    int mContrast = 25;
    int mVignette = 0;
    int mSaturation = 50;
    int mSharpness = 0;

    public PipSettingView(@NonNull Context context) {
        this(context, null);

    }

    public PipSettingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PipSettingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.alivc_editor_view_setting_pip, this);
        mListView = (RecyclerView) findViewById(R.id.effect_list_filter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mListView.setLayoutManager(layoutManager);
        if (mOperationAdapter == null) {
            mOperationAdapter = new OperationAdapter(getContext());
            mOperationAdapter.setOnItemClickListener(this);
        }
        mListView.setAdapter(mOperationAdapter);
        mListView.addItemDecoration(new SpaceItemDecoration(getContext().getResources().getDimensionPixelSize(R.dimen.list_item_space)));
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setMax(MAXPROGRESS);
        mProgressBar = findViewById(R.id.seek_bar_area);
        mRest = findViewById(R.id.iv_reset);
        mRest.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                resetEq(mType);
            }
        });
    }

    @Override
    public boolean isPlayerNeedZoom() {
        return true;
    }

    private void dealCancel() {
        if (mOnEffectActionLister != null) {
            mOnEffectActionLister.onCancel();
        }
    }

    @Override
    protected UIEditorPage getUIEditorPage() {
        return UIEditorPage.PIP;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (mType) {
            case SCALE:
                mScale = progress;
                Dispatcher.getInstance().postMsg(new PipScaleMsg().progress(mScale / 100.f));
                break;
            case ANGLE:
                mAngle = progress;
                float radian = 0;
                float pi = 3.14f;
                if (mAngle < 50) {
                    radian = - 1.0f * mAngle / 50 * pi;
                } else {
                    radian = 1.0f * mAngle / 50 * pi;
                }
                Dispatcher.getInstance().postMsg(new PipAngleMsg().progress(radian));
                break;
            case VOLUME:
                mVolume = progress;
                Dispatcher.getInstance().postMsg(new PipVolumeMsg().progress(mVolume));
                break;
            case DENOISE:
                mDenoise = progress;
                Dispatcher.getInstance().postMsg(new PipDenoiseMsg().progress(mDenoise));
                break;
            case RADIUS:
                mCorner = progress;
                Dispatcher.getInstance().postMsg(new PipRadiusMsg().progress(mCorner));
                break;
            case BORDER:
                mBoarder = progress;
                Dispatcher.getInstance().postMsg(new PipBorderMsg().progress(mBoarder));
                break;
            case MOVE:
                mMove = progress;
                Dispatcher.getInstance().postMsg(new PipMoveMsg().progress(mMove / 100.f));
                break;
            case ALPHA:
                mAlpha = progress;
                Dispatcher.getInstance().postMsg(new PipAlphaMsg().progress(mAlpha / 100.f));
                break;
            case VIGNETTE:
                mVignette = progress;
                Dispatcher.getInstance().postMsg(new PipVignetteMsg().progress(mVignette / 100.f));
                break;
            case SATURATION:
                mSaturation = progress;
                Dispatcher.getInstance().postMsg(new PipSaturationMsg().progress(mSaturation / 100.f));
                break;
            case BRIGHTNESS:
                mBrightness = progress;
                Dispatcher.getInstance().postMsg(new PipBrighnessMsg().progress(mBrightness / 100.f));
                break;
            case SHARPNESS:
                mSharpness = progress;
                Dispatcher.getInstance().postMsg(new PipSharpnessMsg().progress(mSharpness / 100.f));
                break;
            case CONTRAST:
                mContrast = progress;
                Dispatcher.getInstance().postMsg(new PipContrastMsg().progress(mContrast / 100.f));
                break;
            default:
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    @Override
    public boolean onItemClick(Operation type, int index) {
        mType = type;
        configProgress();
        return true;
    }

    private void hideProgressBar(){
        mProgressBar.setVisibility(GONE);
    }

    private void showProgressBar(){
        mProgressBar.setVisibility(VISIBLE);
    }

    private void configProgress(){
        if(mType == null){
            hideProgressBar();
            return;
        }
        showProgressBar();
        switch (mType){
            case SCALE:
                mSeekBar.setProgress((int)(mScale));
                break;
            case ANGLE:
                mSeekBar.setProgress((int)(mAngle));
                break;
            case MOVE:
                mSeekBar.setProgress((int)(mMove));
                break;
            case RADIUS:
                mSeekBar.setProgress((int)(mCorner));
                break;
            case BORDER:
                mSeekBar.setProgress((int)(mBoarder));
                break;
            case ALPHA:
                mSeekBar.setProgress((int)(mAlpha));
                break;
            case VOLUME:
                mSeekBar.setProgress((int)(mVolume));
                break;
            case DENOISE:
                mSeekBar.setProgress((int)(mDenoise));
                break;
            case VIGNETTE:
                mSeekBar.setProgress((int)(mVignette));
                break;
            case SATURATION:
                mSeekBar.setProgress((int)(mSaturation));
                break;
            case BRIGHTNESS:
                mSeekBar.setProgress((int)(mBrightness));
                break;
            case SHARPNESS:
                mSeekBar.setProgress((int)(mSharpness));
                break;
            case CONTRAST:
                mSeekBar.setProgress((int)(mContrast));
                break;
            case ADD:
                hideProgressBar();
                Dispatcher.getInstance().postMsg(new PipAddMsg());
                break;
            case EFFECT:
                hideProgressBar();
                Dispatcher.getInstance().postMsg(new PipEffectMsg());
                break;
            case FRAME_ANIMATION:
                hideProgressBar();
                Dispatcher.getInstance().postMsg(new PipFrameAnimationMsg());
                break;
            case DELETE:
                hideProgressBar();
                Dispatcher.getInstance().postMsg(new PipDeleteMsg());
                break;
            default:
                break;
        }
    }
}
