package com.aliyun.svideo.recorder.mixrecorder;

import android.graphics.Bitmap;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.aliyun.svideo.recorder.bean.AlivcMixBorderParam;
import com.aliyun.svideo.recorder.bean.VideoDisplayParam;
import com.aliyun.svideosdk.common.callback.recorder.OnFrameCallback;
import com.aliyun.svideosdk.common.callback.recorder.OnPictureCallback;
import com.aliyun.svideosdk.common.callback.recorder.OnRecordCallback;
import com.aliyun.svideosdk.common.callback.recorder.OnTextureIdCallback;
import com.aliyun.svideosdk.common.struct.common.VideoQuality;
import com.aliyun.svideosdk.common.struct.effect.EffectBase;
import com.aliyun.svideosdk.common.struct.effect.EffectBean;
import com.aliyun.svideosdk.common.struct.effect.EffectFilter;
import com.aliyun.svideosdk.common.struct.effect.EffectImage;
import com.aliyun.svideosdk.common.struct.effect.EffectPaster;
import com.aliyun.svideosdk.common.struct.effect.EffectStream;
import com.aliyun.svideosdk.common.struct.recorder.CameraType;
import com.aliyun.svideosdk.common.struct.recorder.FlashType;
import com.aliyun.svideosdk.recorder.AliyunIClipManager;
import com.aliyun.svideosdk.recorder.AliyunIRecordPasterManager;
import com.aliyun.svideosdk.recorder.RecordCallback;

/**
*整合录制接口，包含录制，合拍，如果需要修改@AliyunSvideoRecordView的的recorder请先修改本接口
*/
public interface AlivcIMixRecorderInterface {
    AliyunIClipManager getClipManager();

    void setOutputPath(String var1);

    void setGop(int var1);

    void setCamera(CameraType var1);

    int getCameraCount();

    void setDisplayView(SurfaceView cameraPreviewView, SurfaceView playerView);

    void startPreview();

    void stopPreview();

    AliyunIRecordPasterManager getPasterManager();

    void applyFilter(EffectFilter var1);

    void removeFilter();

    int applyBackgroundMusic(EffectStream effectStream);

    int removeBackgroundMusic();

    int switchCamera();

    void setLight(FlashType var1);

    void setZoom(float var1);

    void setFocusMode(int var1);

    void setRate(float var1);

    void setFocus(float var1, float var2);

    void setBeautyLevel(int var1);

    void startRecording();

    void stopRecording();

    int finishRecording();

    void setOnRecordCallback(OnRecordCallback var1);

    void setOnFrameCallback(OnFrameCallback var1);

    void setRotation(int var1);

    void setOnTextureIdCallback(OnTextureIdCallback var1);

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

    void takePicture(boolean needBitmap, OnPictureCallback pictureCallback);

    void takeSnapshot(boolean needBitmap, OnPictureCallback pictureCallback);

    void applyAnimationFilter(EffectFilter effectFilter);

    void updateAnimationFilter(EffectFilter effectFilter);

    void removeAnimationFilter(EffectFilter effectFilter);

    void useFlip(boolean isUseFlip);

    void release();
    int getBackgroundColor();
    String getBackgroundImage();
    int getBackgroundImageDisplayMode();
    VideoDisplayParam getPlayDisplayParams();
    VideoDisplayParam getRecordDisplayParam();
    AlivcMixBorderParam getMixBorderParam();
    void setMixBorderParam(AlivcMixBorderParam param);
    void setIsAutoClearClipVideos(boolean isAutoClear);
}
