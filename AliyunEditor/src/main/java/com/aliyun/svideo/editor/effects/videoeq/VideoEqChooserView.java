package com.aliyun.svideo.editor.effects.videoeq;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.control.BaseChooser;
import com.aliyun.svideo.editor.effects.control.SpaceItemDecoration;
import com.aliyun.svideo.editor.msg.Dispatcher;
import com.aliyun.svideo.editor.msg.body.BrightnessProgressMsg;
import com.aliyun.svideo.editor.msg.body.ContrastProgressMsg;
import com.aliyun.svideo.editor.msg.body.SaturationProgressMsg;
import com.aliyun.svideo.editor.msg.body.SharpProgressMsg;
import com.aliyun.svideo.editor.msg.body.VideoEqResetAllMsg;
import com.aliyun.svideo.editor.msg.body.VideoEqResetMsg;
import com.aliyun.svideo.editor.msg.body.VignetteMsg;
import com.aliyun.svideo.editor.util.EditorCommon;
import com.aliyun.svideosdk.common.internal.videoaugment.VideoAugmentationConstant;
import com.aliyun.svideosdk.common.internal.videoaugment.VideoAugmentationType;

public class VideoEqChooserView extends BaseChooser implements FilterAdapter.OnItemClickListener, SeekBar.OnSeekBarChangeListener {
    private RecyclerView mListView;
    private FilterAdapter mFilterAdapter;
    private SeekBar mSeekBar;
    private VideoAugmentationType mType;
    private View mProgressBar;
    private float mBrightness = VideoAugmentationConstant.DEFAULT_BRIGHTNESS,
            mContrast = VideoAugmentationConstant.DEFAULT_CONTRAST,
            mSaturation = VideoAugmentationConstant.DEFAULT_SATURATION,
            mSharpness = VideoAugmentationConstant.DEFAULT_SHARP,
            mVignette = VideoAugmentationConstant.DEFAULT_VIGNETTE;
    private static final int MAXPROGRESS = 100;
    private View mRest;
    public VideoEqChooserView(@NonNull Context context) {
        this(context, null);
    }

    public VideoEqChooserView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoEqChooserView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.alivc_editor_view_chooser_videoeq, this);
        mListView = (RecyclerView) findViewById(R.id.effect_list_filter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mListView.setLayoutManager(layoutManager);
        if (mFilterAdapter == null) {
            mFilterAdapter = new FilterAdapter(getContext());
            mFilterAdapter.setOnItemClickListener(this);
            mFilterAdapter.setDataList(EditorCommon.getVideoEqList(getContext()));
        }
        mListView.setAdapter(mFilterAdapter);
        mListView.addItemDecoration(new SpaceItemDecoration(getContext().getResources().getDimensionPixelSize(R.dimen.list_item_space)));
        Drawable top = getContext().getResources().getDrawable(R.mipmap.alivc_svideo_icon_tab_filter);
        top.setBounds(0, 0, top.getMinimumWidth(), top.getMinimumHeight());
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setMax(MAXPROGRESS);
        mProgressBar = findViewById(R.id.seek_bar_area);
        mRest = findViewById(R.id.iv_reset);
        mRest.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                resetEq(mType);
            }
        });
    }

    private void resetAllEq(){
        mBrightness = VideoAugmentationConstant.DEFAULT_BRIGHTNESS;
        mContrast = VideoAugmentationConstant.DEFAULT_CONTRAST;
        mSaturation = VideoAugmentationConstant.DEFAULT_SATURATION;
        mSharpness = VideoAugmentationConstant.DEFAULT_SHARP;
        mVignette = VideoAugmentationConstant.DEFAULT_VIGNETTE;
        Dispatcher.getInstance().postMsg(new VideoEqResetAllMsg());

        configProgress();
    }

    private void resetEq(VideoAugmentationType type){
        switch (type){
            case BRIGHTNESS:
                mBrightness = VideoAugmentationConstant.DEFAULT_BRIGHTNESS;
                break;
            case CONTRAST:
                mContrast = VideoAugmentationConstant.DEFAULT_CONTRAST;
                break;
            case SATURATION:
                mSaturation = VideoAugmentationConstant.DEFAULT_SATURATION;
                break;
            case SHARPNESS:
                mSharpness = VideoAugmentationConstant.DEFAULT_SHARP;
                break;
            case VIGNETTE:
                mVignette = VideoAugmentationConstant.DEFAULT_VIGNETTE;
                break;
            default:
                return;
        }
        VideoEqResetMsg msg = new VideoEqResetMsg();
        msg.setType(type);
        Dispatcher.getInstance().postMsg(msg);

        configProgress();
    }

    private void configProgress(){
        if(mType == null){
            hideProgressBar();
            return;
        }
        showProgressBar();
        switch (mType){
            case BRIGHTNESS:
                mSeekBar.setProgress((int)(MAXPROGRESS * mBrightness));
                break;
            case CONTRAST:
                mSeekBar.setProgress((int)(MAXPROGRESS * mContrast));
                break;
            case SATURATION:
                mSeekBar.setProgress((int)(MAXPROGRESS * mSaturation));
                break;
            case SHARPNESS:
                mSeekBar.setProgress((int)(MAXPROGRESS * mSharpness));
                break;
            case VIGNETTE:
                mSeekBar.setProgress((int)(MAXPROGRESS * mVignette));
                break;
            default:
                break;
        }
    }

    private void hideProgressBar(){
        mProgressBar.setVisibility(GONE);
    }

    private void showProgressBar(){
        mProgressBar.setVisibility(VISIBLE);
    }

    @Override
    public boolean isPlayerNeedZoom() {
        return false;
    }

    @Override
    public boolean isShowSelectedView() {
        return false;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (mType){
            case BRIGHTNESS:
                mBrightness = (float)progress/(float) MAXPROGRESS;
                Dispatcher.getInstance().postMsg(new BrightnessProgressMsg().progress(mBrightness));
                break;
            case CONTRAST:
                mContrast = (float)progress/(float) MAXPROGRESS;
                Dispatcher.getInstance().postMsg(new ContrastProgressMsg().progress(mContrast));
                break;
            case SATURATION:
                mSaturation = (float)progress/(float) MAXPROGRESS;
                Dispatcher.getInstance().postMsg(new SaturationProgressMsg().progress(mSaturation));
                break;
            case SHARPNESS:
                mSharpness = (float)progress/(float) MAXPROGRESS;
                Dispatcher.getInstance().postMsg(new SharpProgressMsg().progress(mSharpness));
                break;
            case VIGNETTE:
                mVignette = (float)progress/(float) MAXPROGRESS;
                Dispatcher.getInstance().postMsg(new VignetteMsg().progress(mVignette));
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
    public boolean onItemClick(VideoAugmentationType type, int index) {
        this.mType = type;
        configProgress();
        if(index == 0){
            resetAllEq();
        }
        return true;
    }
}
