package com.aliyun.svideo.recorder.mixrecorder;

import android.content.Context;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.aliyun.svideo.common.utils.ScreenUtils;
import com.aliyun.svideo.recorder.bean.AlivcMixBorderParam;
import com.aliyun.svideo.recorder.bean.VideoDisplayParam;
import com.aliyun.svideosdk.common.NativeAdaptiveUtil;
import com.aliyun.svideosdk.common.callback.recorder.OnFrameCallback;
import com.aliyun.svideosdk.common.callback.recorder.OnPictureCallback;
import com.aliyun.svideosdk.common.callback.recorder.OnRecordCallback;
import com.aliyun.svideosdk.common.callback.recorder.OnTextureIdCallback;
import com.aliyun.svideosdk.common.struct.common.VideoQuality;
import com.aliyun.svideosdk.common.struct.effect.EffectBase;
import com.aliyun.svideosdk.common.struct.effect.EffectFilter;
import com.aliyun.svideosdk.common.struct.effect.EffectImage;
import com.aliyun.svideosdk.common.struct.effect.EffectPaster;
import com.aliyun.svideosdk.common.struct.effect.EffectStream;
import com.aliyun.svideosdk.common.struct.recorder.CameraType;
import com.aliyun.svideosdk.common.struct.recorder.FlashType;
import com.aliyun.svideosdk.common.struct.recorder.MediaInfo;
import com.aliyun.svideosdk.mixrecorder.AliyunIMixRecorder;
import com.aliyun.svideosdk.mixrecorder.AliyunMixBorderParam;
import com.aliyun.svideosdk.mixrecorder.AliyunMixMediaInfoParam;
import com.aliyun.svideosdk.mixrecorder.MixAudioAecType;
import com.aliyun.svideosdk.mixrecorder.MixAudioSourceType;
import com.aliyun.svideosdk.mixrecorder.impl.AliyunMixRecorderCreator;
import com.aliyun.svideosdk.recorder.AliyunIClipManager;
import com.aliyun.svideosdk.recorder.AliyunIRecordPasterManager;
import com.aliyun.svideosdk.recorder.RecordCallback;

/**
 * 包含合拍功能
 */
public class AlivcMixRecorder implements AlivcIMixRecorderInterface {

    private Context mContext;
    private AliyunIMixRecorder mRecorder;
    private VideoDisplayParam mPlayDisplayParam;
    private VideoDisplayParam mRecordDisplayParam;

    public AlivcMixRecorder(Context context) {
        this.mContext = context;
        initRecorder(mContext);
    }

    /**
     * 初始化recorder
     */
    private void initRecorder(Context context) {

        mRecorder = AliyunMixRecorderCreator.createAlivcMixRecorderInstance(context);

    }

    @Override
    public void applyAnimationFilter(EffectFilter effectFilter) {
        mRecorder.applyAnimationFilter(effectFilter);
    }

    @Override
    public void updateAnimationFilter(EffectFilter effectFilter) {
        mRecorder.updateAnimationFilter(effectFilter);
    }

    @Override
    public void removeAnimationFilter(EffectFilter effectFilter) {
        mRecorder.removeAnimationFilter(effectFilter);
    }

    @Override
    public void useFlip(boolean isUseFlip) {
        //nothing to do
    }

    @Override
    public FrameLayout.LayoutParams getLayoutParams() {

        int screenWidth = ScreenUtils.getRealWidth(mContext);
        int height = 0;
        int width = screenWidth / 2;
        height = width * 16 / 9;

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);

        params.height = height;
        params.width = width;
        return params;
    }

    @Override
    public void takePicture(boolean needBitmap, OnPictureCallback pictureCallback) {

    }

    @Override
    public void takeSnapshot(boolean needBitmap, OnPictureCallback pictureCallback) {

    }

    public void setMediaInfo(String videoPath, VideoDisplayParam playDisplayParam, VideoDisplayParam recordDisplayParam, MediaInfo outputInfo) {
        // mMixInputInfo只对合拍有效，普通录制情况下，该参数将被忽略
        mPlayDisplayParam = playDisplayParam;
        mRecordDisplayParam = recordDisplayParam;
        AliyunMixMediaInfoParam inputInfo = new AliyunMixMediaInfoParam
        .Builder()
        .streamStartTimeMills(0L)
        .streamEndTimeMills(0L)
        .mixVideoFilePath(videoPath)
        .mixDisplayParam(mPlayDisplayParam.getAliDisplayParam())
        .recordDisplayParam(mRecordDisplayParam.getAliDisplayParam())
        .build();
        mRecorder.setMixMediaInfo(inputInfo, outputInfo);
    }
    @Override
    public AliyunIClipManager getClipManager() {
        return mRecorder.getClipManager();
    }

    @Override
    public void setOutputPath(String var1) {
        mRecorder.setOutputPath(var1);
    }

    @Override
    public void setGop(int var1) {
        mRecorder.setGop(var1);
    }

    @Override
    public void setCamera(CameraType var1) {
        mRecorder.setCamera(var1);
    }

    @Override
    public int getCameraCount() {
        return mRecorder.getCameraCount();
    }

    @Override
    public void setDisplayView(SurfaceView cameraPreviewView, SurfaceView playerView) {
        mRecorder.setDisplayView(cameraPreviewView, playerView);
    }

    @Override
    public int applyBackgroundMusic(EffectStream effectStream) {
        //do nothing
        return 0;
    }

    @Override
    public int removeBackgroundMusic() {
        //do nothing
        return 0;
    }

    @Override
    public void startPreview() {
        mRecorder.startPreview();
    }

    @Override
    public void stopPreview() {
        mRecorder.stopPreview();

    }

    @Override
    public AliyunIRecordPasterManager getPasterManager() {
        return mRecorder.getPasterManager();
    }


    @Override
    public void applyFilter(EffectFilter var1) {
        mRecorder.applyFilter(var1);
    }

    @Override
    public void removeFilter() {
        mRecorder.removeFilter();
    }


    @Override
    public int switchCamera() {
        return mRecorder.switchCamera();
    }

    @Override
    public void setLight(FlashType var1) {
        mRecorder.setLight(var1);
    }

    @Override
    public void setZoom(float var1) {
        mRecorder.setZoom(var1);
    }

    @Override
    public void setFocusMode(int var1) {
        mRecorder.setFocusMode(var1);
    }

    @Override
    public void setRate(float var1) {
        mRecorder.setRate(var1);
    }

    @Override
    public void setFocus(float var1, float var2) {
        mRecorder.setFocus(var1, var2);
    }


    @Override
    public void setBeautyLevel(int var1) {
        mRecorder.setBeautyLevel(var1);
    }

    @Override
    public void startRecording() {
        mRecorder.startRecording();
    }

    @Override
    public void stopRecording() {
        mRecorder.stopRecording();
    }

    @Override
    public int finishRecording() {
        //由于Demo这里合拍视频都是经过降采样转码的，分辨率比较低，建议使用软解码，如果是高分辨率视频且不转码降采样，则不建议关闭硬解码
        NativeAdaptiveUtil.setHWDecoderEnable(false);
        int code = mRecorder.finishRecording();
        NativeAdaptiveUtil.setHWDecoderEnable(true);//重新开启硬解码
        return code;
    }

    @Override
    public void setOnRecordCallback(OnRecordCallback var1) {
        mRecorder.setOnRecordCallback(var1);
    }


    @Override
    public void setOnFrameCallback(OnFrameCallback var1) {
        mRecorder.setOnFrameCallback(var1);
    }

    /**
     * 横屏竖屏旋转多段录制视频,保证旋转的角度跟录制出来的保持一致
     */
    @Override
    public void setRotation(int var1) {
        mRecorder.setRecordRotation(0);
        mRecorder.setFaceDetectRotation(var1);
    }

    @Override
    public void setOnTextureIdCallback(OnTextureIdCallback var1) {
        mRecorder.setOnTextureIdCallback(var1);
    }

    @Override
    public void needFaceTrackInternal(boolean var1) {
        mRecorder.needFaceTrackInternal(var1);
    }

    @Override
    public void setFaceTrackInternalModelPath(String var1) {
        mRecorder.setFaceTrackInternalModelPath(var1);
    }

    @Override
    public void setFaceTrackInternalMaxFaceCount(int var1) {
        mRecorder.setFaceTrackInternalMaxFaceCount(var1);
    }

    @Override
    public void setMute(boolean var1) {
    }

    @Override
    public void deleteLastPart() {
        mRecorder.deleteLastPart();
    }

    /**
     * 目前固定分辨率720*640
     */
    @Override
    public int getVideoWidth() {
        return 720;
    }

    @Override
    public int getVideoHeight() {
        return getVideoWidth() * 8 / 9;
    }

    @Override
    public boolean isMixRecorder() {
        return true;
    }

    @Override
    public void setResolutionMode(int resolutionMode) {
    }

    @Override
    public void setRatioMode(int ratioMode) {

    }

    @Override
    public void release() {
        mRecorder.release();
    }

    @Override
    public VideoDisplayParam getPlayDisplayParams() {
        return mPlayDisplayParam;
    }

    @Override
    public VideoDisplayParam getRecordDisplayParam() {
        return mRecordDisplayParam;
    }

    @Override
    public AlivcMixBorderParam getMixBorderParam() {
        return mMixBorderParam;
    }

    @Override
    public void setMixBorderParam(AlivcMixBorderParam param) {
        mMixBorderParam = param;
        if (mMixBorderParam != null) {
            AliyunMixBorderParam mixBorderParam = new AliyunMixBorderParam.Builder()
            .borderColor(mMixBorderParam.getBorderColor())
            .cornerRadius(mMixBorderParam.getCornerRadius())
            .borderWidth(mMixBorderParam.getBorderWidth())
            .build();
            mRecorder.setRecordBorderParam(mixBorderParam);
        } else {
            mRecorder.setRecordBorderParam(null);
        }
    }

    public void setMixAudioSource(MixAudioSourceType mMixAudioSourceType) {
        mRecorder.setMixAudioSource(mMixAudioSourceType);
    }

    public void setMixAudioAecType(MixAudioAecType mMixAudioAecType) {
        mRecorder.setMixAudioAecType(mMixAudioAecType);
    }

    private int mBackgroundColor;
    /**
     * 设置合成窗口非填充模式下的背景颜色
     * v3.19.0 新增
     * @param color
     */
    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
        mRecorder.setBackgroundColor(color);
    }
    private String mBackGroundImage;
    private int mDisplayMode;
    private AlivcMixBorderParam mMixBorderParam;
    /**
     * 设置合成窗口非填充模式下的背景图片路径
     * v3.19.0 新增
     * @param path
     * @param displayMode 0：裁切 1：填充 2：拉伸
     */
    public void setBackgroundImage(String path, int displayMode) {
        mBackGroundImage = path;
        mDisplayMode = displayMode;
        mRecorder.setBackgroundImage(path, displayMode);
    }

    @Override
    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    @Override
    public String getBackgroundImage() {
        return mBackGroundImage;
    }

    @Override
    public int getBackgroundImageDisplayMode() {
        return mDisplayMode;
    }

    @Override
    public void setIsAutoClearClipVideos(boolean isAutoClear) {
        mRecorder.setIsAutoClearClipVideos(isAutoClear);
    }
}
