package com.aliyun.race.sample.cameraView;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.text.TextUtils;
import android.util.Log;

import com.aliyun.race.sample.utils.PermissionUtils;

import java.io.IOException;
import java.util.List;

public class CameraInterface {
    private static final String TAG = "RACE";
    private Camera mCamera;
    private Camera.Parameters mParams;
    private boolean mIsPreviewing = false;
    private static CameraInterface mCameraInterface;
    private PreviewCallback mPreviewCallback;
    private int mCameraIndex = Camera.CameraInfo.CAMERA_FACING_FRONT;

    private CameraInterface(){
    }
    public static synchronized CameraInterface getInstance(){
        if(mCameraInterface == null){
            mCameraInterface = new CameraInterface();
        }
        return mCameraInterface;
    }

    public void setPreviewCallback(PreviewCallback previewCallback){
        this.mPreviewCallback = previewCallback;
    }

    public void release(){
        doStopCamera();
        mCameraInterface = null;
        mPreviewCallback = null;
    }

    public void setRotation(int rotation){
        if(mParams != null){
            mParams.setRotation(rotation);
        }
    }
    public void startPreview(){
        if(mCamera != null ){
            mCamera.startPreview();
        }
    }
    public void stopPreview(){
        if(mCamera != null ){
            mCamera.stopPreview();
        }
    }

    //打开相机
    public void doOpenCamera(){
        if(mCamera == null) {
            try {
                mCamera = Camera.open(mCameraIndex);
            } catch (RuntimeException e) {
                e.printStackTrace();
                doStopCamera();
            }
        } else{
            doStopCamera();
        }
    }
    /*使用TextureView预览Camera*/
    public void doStartPreview(SurfaceTexture surface){
        if(mIsPreviewing && mCamera != null){
            mCamera.stopPreview();
            return;
        }
        if(mCamera != null && surface != null){
            try {
                //将相机画面预览到纹理层上,纹理层有数据了，再通知view绘制,此时未开始预览
                mCamera.setPreviewTexture(surface);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //真正开启预览,Camera.startPreView()
            initCamera();
        }
    }

    /**
     * 停止预览，释放Camera
     */
    public void doStopCamera(){
        if(null != mCamera)
        {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mIsPreviewing = false;
            mCamera.release();
            mCamera = null;
        }
    }

    public int switchCamera() {
        doStopCamera();
        if(mCameraIndex == Camera.CameraInfo.CAMERA_FACING_BACK){
            mCameraIndex = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            mCameraIndex = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        doOpenCamera();
        return mCameraIndex;
    }

    public boolean isPreviewing(){
        return mIsPreviewing;
    }

    private void initCamera(){
        if(mCamera != null){
            mParams = mCamera.getParameters();
            mParams.setPreviewSize(1280, 720);
            //设置摄像头为持续自动聚焦模式
            setAutoFocus(mParams);
            mCamera.setParameters(mParams);
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    if(mPreviewCallback != null){
                        mPreviewCallback.onPreviewFrame(data, 1280, 720, mCameraIndex);
                    }
                }
            });
            mCamera.startPreview();//开启预览
            mCamera.cancelAutoFocus();
            //设置预览标志位
            mIsPreviewing = true;
        }
    }
   private void setAutoFocus(Camera.Parameters params) {
        List<String> focusModes = params.getSupportedFocusModes();

        String continuePicture = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE,
                continueVideo = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO,
                supportedMode = focusModes
                        .contains(continuePicture) ? continuePicture : focusModes
                        .contains(continueVideo) ? continueVideo : "";

        if (!TextUtils.isEmpty(supportedMode)) {
            params.setFocusMode(supportedMode);
        }
    }


}