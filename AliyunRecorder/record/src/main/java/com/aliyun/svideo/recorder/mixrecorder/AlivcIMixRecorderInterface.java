package com.aliyun.svideo.recorder.mixrecorder;

import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.aliyun.mix.AliyunMixMediaInfoParam;
import com.aliyun.recorder.supply.AliyunIClipManager;
import com.aliyun.recorder.supply.RecordCallback;
import com.aliyun.svideo.sdk.external.struct.common.VideoQuality;
import com.aliyun.svideo.sdk.external.struct.effect.EffectBase;
import com.aliyun.svideo.sdk.external.struct.effect.EffectBean;
import com.aliyun.svideo.sdk.external.struct.effect.EffectFilter;
import com.aliyun.svideo.sdk.external.struct.effect.EffectImage;
import com.aliyun.svideo.sdk.external.struct.effect.EffectPaster;
import com.aliyun.svideo.sdk.external.struct.recorder.CameraType;
import com.aliyun.svideo.sdk.external.struct.recorder.FlashType;
import com.aliyun.svideo.sdk.external.struct.recorder.MediaInfo;
import com.qu.preview.callback.OnFrameCallBack;
import com.qu.preview.callback.OnTextureIdCallBack;

/**
*整合录制接口，包含录制，合拍，如果需要修改@AliyunSvideoRecordView的的recorder请先修改本接口
*/
public interface AlivcIMixRecorderInterface {

    void setMediaInfo(AliyunMixMediaInfoParam inputInfo, MediaInfo outputInfo);

    AliyunIClipManager getClipManager();

    void setOutputPath(String var1);

    void setVideoQuality(VideoQuality var1);

    void setGop(int var1);

    void setCamera(CameraType var1);

    int getCameraCount();

    void setDisplayView(SurfaceView cameraPreviewView, SurfaceView playerView);

    void startPreview();

    void stopPreview();

    void addPaster(EffectPaster var1);

    void addPaster(EffectPaster var1, float var2, float var3, float var4, float var5, float var6, boolean var7);

    void setEffectView(float xRatio, float yRatio, float widthRatio, float heightRatio, EffectBase effectBase);

    void addImage(EffectImage effctImage);

    void removeImage(EffectImage effctImage);

    void removePaster(EffectPaster var1);

    void applyFilter(EffectFilter var1);

    void setMusic(String var1, long var2, long var4);

    int switchCamera();

    void setLight(FlashType var1);

    void setZoom(float var1);

    void setFocusMode(int var1);

    void setRate(float var1);

    void setFocus(float var1, float var2);

    void restartMv();

    void applyMv(EffectBean var1);

    void setBeautyLevel(int var1);

    void setBeautyStatus(boolean var1);

    void startRecording();

    void stopRecording();

    int finishRecording();

    void setRecordCallback(RecordCallback var1);

    void setOnFrameCallback(OnFrameCallBack var1);

    void setRotation(int var1);

    void setOnTextureIdCallback(OnTextureIdCallBack var1);

    void needFaceTrackInternal(boolean var1);

    void setFaceTrackInternalModelPath(String var1);

    void setFaceTrackInternalMaxFaceCount(int var1);

    void setMute(boolean var1);

    void deleteLastPart();

    int getVideoWidth();

    int getVideoHeight();

    void setResolutionMode(int resolutionMode);

    /**
     * 获取当前录制类型（合拍true还是普通false）
     */
    boolean isMixRecorder();

    /**
     * 视频分辨率
     */
    void setRatioMode(int ratioMode);

    /**
     * 录制界面比例layout参数
     */
    FrameLayout.LayoutParams getLayoutParams();

    void setMixRecorderRatio(SurfaceView surfaceView);

    void setMixPlayerRatio(SurfaceView surfaceView);

    void takePhoto(boolean needBitmap);

    void applyAnimationFilter(EffectFilter effectFilter);

    void updateAnimationFilter(EffectFilter effectFilter);

    void removeAnimationFilter(EffectFilter effectFilter);

    void useFlip(boolean isUseFlip);

    void release();
}
