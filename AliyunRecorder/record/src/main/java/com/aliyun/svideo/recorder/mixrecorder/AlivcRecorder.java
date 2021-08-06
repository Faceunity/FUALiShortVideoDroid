package com.aliyun.svideo.recorder.mixrecorder;

import android.content.Context;
import android.view.Gravity;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.aliyun.svideo.common.utils.ScreenUtils;
import com.aliyun.svideo.record.R;
import com.aliyun.svideo.recorder.bean.AlivcMixBorderParam;
import com.aliyun.svideo.recorder.bean.VideoDisplayParam;
import com.aliyun.svideosdk.common.callback.recorder.OnFrameCallBack;
import com.aliyun.svideosdk.common.callback.recorder.OnTextureIdCallBack;
import com.aliyun.svideosdk.common.struct.common.AliyunSnapVideoParam;
import com.aliyun.svideosdk.common.struct.common.VideoQuality;
import com.aliyun.svideosdk.common.struct.effect.EffectBase;
import com.aliyun.svideosdk.common.struct.effect.EffectBean;
import com.aliyun.svideosdk.common.struct.effect.EffectFilter;
import com.aliyun.svideosdk.common.struct.effect.EffectImage;
import com.aliyun.svideosdk.common.struct.effect.EffectPaster;
import com.aliyun.svideosdk.common.struct.recorder.CameraType;
import com.aliyun.svideosdk.common.struct.recorder.FlashType;
import com.aliyun.svideosdk.common.struct.recorder.MediaInfo;
import com.aliyun.svideosdk.recorder.AliyunIClipManager;
import com.aliyun.svideosdk.recorder.AliyunIRecorder;
import com.aliyun.svideosdk.recorder.RecordCallback;
import com.aliyun.svideosdk.recorder.impl.AliyunRecorderCreator;

/**
 *包含录制功能
 */
public class AlivcRecorder implements AlivcIMixRecorderInterface {


    private Context mContext;
    private AliyunIRecorder mRecorder;
    //视频分辨率
    private int mResolutionMode = AliyunSnapVideoParam.RESOLUTION_540P;
    //视频比例
    private int mRatioMode = AliyunSnapVideoParam.RATIO_MODE_3_4;

    public AlivcRecorder(Context mContext) {
        this.mContext = mContext;
        initRecorder();
    }


    /**
     * 初始化recorder
     */
    private void initRecorder() {
        mRecorder = AliyunRecorderCreator.getRecorderInstance(mContext);

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
        mRecorder.setDisplayView(cameraPreviewView);
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
    public void setMusic(String var1, long var2, long var4) {
        mRecorder.setMusic(var1, var2, var4);
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
        return mRecorder.finishRecording();
    }

    @Override
    public void setRecordCallback(RecordCallback var1) {
        mRecorder.setRecordCallback(var1);
    }

    @Override
    public void setOnFrameCallback(OnFrameCallBack var1) {
        mRecorder.setOnFrameCallback(var1);
    }

    @Override
    public void setRotation(int var1) {
        mRecorder.setRotation(var1);
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
        mRecorder.setMute(var1);
    }

    @Override
    public void deleteLastPart() {
        mRecorder.getClipManager().deletePart();
    }

    @Override
    public void restartMv() {
        mRecorder.restartMv();
    }

    @Override
    public void applyMv(EffectBean var1) {
        mRecorder.applyMv(var1);
    }

    @Override
    public void setResolutionMode(int resolutionMode) {
        mResolutionMode = resolutionMode;
    }

    @Override
    public void setRatioMode(int ratioMode) {
        mRatioMode = ratioMode;

    }

    @Override
    public boolean isMixRecorder() {
        return false;
    }
    @Override
    public void release() {
        mRecorder.destroy();
    }

    /**
     * 设置界面比例
     */
    @Override
    public FrameLayout.LayoutParams getLayoutParams() {

        int screenWidth = ScreenUtils.getRealWidth(mContext);
        int height = 0;
        int top = 0;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(screenWidth, height);

        switch (mRatioMode) {
            case AliyunSnapVideoParam.RATIO_MODE_1_1:
                //视频比例为1：1的时候，录制界面向下移动，移动位置为顶部菜单栏的高度
                top = mContext.getResources().getDimensionPixelSize(R.dimen.alivc_record_title_height);
                params.setMargins(0, top, 0, 0);
                height = screenWidth;
                break;
            case AliyunSnapVideoParam.RATIO_MODE_3_4:
                //视频比例为3：4的时候，录制界面向下移动，移动位置为顶部菜单栏的高度
                top = mContext.getResources().getDimensionPixelSize(R.dimen.alivc_record_title_height);
                params.setMargins(0, top, 0, 0);
                height = screenWidth * 4 / 3;
                break;
            case AliyunSnapVideoParam.RATIO_MODE_9_16:

                params.width = screenWidth;
                height = screenWidth * 16 / 9;
                /** 全屏幕录制，不带上下黑边，由于surface宽度超过屏幕宽度，导致切画幅切回9：16 的时候，会闪一下
                 //int screenHeight = ScreenUtils.getRealHeight(mContext);
                 //float screenRatio = screenWidth / (float) screenHeight;
                 //if (screenRatio >= 9 / 16f) {
                 //   //胖手机宽高比小于9/16
                 //   params.width = screenWidth;
                 //   height = screenWidth * 16 / 9;
                 //} else {
                 //   height = screenHeight;
                 //   params.width = screenHeight * 9 / 16;
                 //}
                 //Log.e("RealHeight", "height:" + screenHeight + "width:" + screenWidth);
                 */
                params.gravity = Gravity.CENTER;
                break;
            default:
                height = screenWidth * 16 / 9;
                break;
        }
        params.height = height;
        mRecorder.resizePreviewSize(params.width, params.height);
        return params;
    }
    @Override
    public void takePhoto(boolean needBitmap) {
        mRecorder.takePhoto(needBitmap);
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
        mRecorder.setVideoFlipH(isUseFlip);
    }

    /**
     * 获取拍摄视频宽度
     *
     * @return int
     */
    @Override
    public int getVideoWidth() {
        int width = 0;
        switch (mResolutionMode) {
            case AliyunSnapVideoParam.RESOLUTION_360P:
                width = 360;
                break;
            case AliyunSnapVideoParam.RESOLUTION_480P:
                width = 480;
                break;
            case AliyunSnapVideoParam.RESOLUTION_540P:
                width = 540;
                break;
            case AliyunSnapVideoParam.RESOLUTION_720P:
                width = 720;
                break;
            default:
                width = 540;
                break;
        }

        return width;
    }

    @Override
    public int getVideoHeight() {
        int width = getVideoWidth();
        int height = 0;
        switch (mRatioMode) {
            case AliyunSnapVideoParam.RATIO_MODE_1_1:
                height = width;
                break;
            case AliyunSnapVideoParam.RATIO_MODE_3_4:
                height = width * 4 / 3;
                break;
            case AliyunSnapVideoParam.RATIO_MODE_9_16:
                height = width * 16 / 9;
                break;
            default:
                height = width;
                break;
        }
        return height;
    }

    @Override
    public VideoDisplayParam getPlayDisplayParams() {
        return null;
    }

    @Override
    public VideoDisplayParam getRecordDisplayParam() {
        return new VideoDisplayParam.Builder().build();
    }

    @Override
    public AlivcMixBorderParam getMixBorderParam() {
        return null;
    }

    @Override
    public void setMixBorderParam(AlivcMixBorderParam param) {

    }

    public void setMediaInfo( MediaInfo outputInfo) {
        mRecorder.setMediaInfo(outputInfo);
    }

    @Override
    public int getBackgroundColor() {
        return 0;
    }
    @Override
    public String getBackgroundImage() {
        return null;
    }

    @Override
    public int getBackgroundImageDisplayMode() {
        return 0;
    }
}
