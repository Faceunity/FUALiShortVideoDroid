package com.aliyun.svideo.recorder.mixrecorder;

import android.content.Context;
import android.view.Gravity;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.aliyun.mix.AliyunIMixRecorder;
import com.aliyun.mix.AliyunMixMediaInfoParam;
import com.aliyun.mix.AliyunMixRecorderCreator;
import com.aliyun.recorder.supply.AliyunIClipManager;
import com.aliyun.recorder.supply.RecordCallback;
import com.aliyun.svideo.common.utils.ScreenUtils;
import com.aliyun.svideo.sdk.external.struct.common.VideoQuality;
import com.aliyun.svideo.sdk.external.struct.effect.EffectBase;
import com.aliyun.svideo.sdk.external.struct.effect.EffectBean;
import com.aliyun.svideo.sdk.external.struct.effect.EffectFilter;
import com.aliyun.svideo.sdk.external.struct.effect.EffectImage;
import com.aliyun.svideo.sdk.external.struct.effect.EffectPaster;
import com.aliyun.svideo.sdk.external.struct.recorder.CameraType;
import com.aliyun.svideo.sdk.external.struct.recorder.FlashType;
import com.aliyun.svideo.sdk.external.struct.recorder.MediaInfo;
import com.duanqu.qupai.adaptive.NativeAdaptiveUtil;
import com.qu.preview.callback.OnFrameCallBack;
import com.qu.preview.callback.OnTextureIdCallBack;

/**
 * 包含合拍功能
 */
public class AlivcMixRecorder implements AlivcIMixRecorderInterface {

    private Context mContext;
    private AliyunIMixRecorder mRecorder;


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

    /**
     * 设置录制界面比例和位置
     *
     * @param surfaceView
     */
    @Override
    public void setMixRecorderRatio(SurfaceView surfaceView) {
        if (surfaceView != null) {
            FrameLayout.LayoutParams params = this.getLayoutParams();
            params.gravity = Gravity.CENTER | Gravity.START;

            surfaceView.setLayoutParams(params);
        }
    }

    /**
     * 设置播放界面比例和位置
     */
    @Override
    public void setMixPlayerRatio(SurfaceView surfaceView) {
        if (surfaceView != null) {
            FrameLayout.LayoutParams params = this.getLayoutParams();
            params.gravity = Gravity.CENTER | Gravity.END;
            surfaceView.setLayoutParams(params);
        }
    }

    @Override
    public void takePhoto(boolean needBitmap) {
        //nothing to do
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
    public void setMediaInfo(AliyunMixMediaInfoParam inputInfo, MediaInfo outputInfo) {
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
    public void setVideoQuality(VideoQuality var1) {
        mRecorder.setVideoQuality(var1);
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
    public void startPreview() {
        mRecorder.startPreview();
    }

    @Override
    public void stopPreview() {
        mRecorder.stopPreview();

    }

    @Override
    public void addPaster(EffectPaster var1) {
        mRecorder.addPaster(var1);
    }

    @Override
    public void addPaster(EffectPaster var1, float var2, float var3, float var4, float var5, float var6, boolean var7) {
        mRecorder.addPaster(var1, var2, var3, var4, var5, var6, var7);
    }

    @Override
    public void setEffectView(float xRatio, float yRatio, float widthRatio, float heightRatio, EffectBase effectBase) {
        mRecorder.setEffectView(xRatio, yRatio, widthRatio, heightRatio, effectBase);
    }

    @Override
    public void addImage(EffectImage effctImage) {
        mRecorder.addImage(effctImage);
    }

    @Override
    public void removeImage(EffectImage effctImage) {
        mRecorder.removeImage(effctImage);
    }

    @Override
    public void removePaster(EffectPaster var1) {
        mRecorder.removePaster(var1);
    }

    @Override
    public void applyFilter(EffectFilter var1) {
        mRecorder.applyFilter(var1);
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
    public void setBeautyStatus(boolean var1) {
        mRecorder.setBeautyStatus(var1);
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
    public void setRecordCallback(RecordCallback var1) {
        mRecorder.setRecordCallback(var1);
    }

    @Override
    public void setOnFrameCallback(OnFrameCallBack var1) {
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
    public void setOnTextureIdCallback(OnTextureIdCallBack var1) {
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
    public void restartMv() {

    }

    @Override
    public void applyMv(EffectBean var1) {

    }

    @Override
    public void setMusic(String var1, long var2, long var4) {

    }
}
