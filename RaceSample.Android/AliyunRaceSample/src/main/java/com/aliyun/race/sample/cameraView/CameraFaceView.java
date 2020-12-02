package com.aliyun.race.sample.cameraView;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.aliyun.race.AliyunFace;
import com.aliyun.race.sample.race.FaceManager;
import com.aliyun.race.sample.render.DrawCamera;
import com.aliyun.race.sample.render.DrawPoints;
import com.aliyun.race.sample.render.DrawPass;
import com.aliyun.race.sample.render.GLFramebuffer;
import com.aliyun.race.sample.render.GLUtils;
import com.aliyun.race.sample.utils.OrientationDetector;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES10.*;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.glBindFramebuffer;


public class CameraFaceView extends GLSurfaceView implements GLSurfaceView.Renderer,
        SurfaceTexture.OnFrameAvailableListener, PreviewCallback {
    private static final String TAG = "RACE";
    //以OpenGL ES纹理的形式从图像流中捕获帧,我把叫做纹理层
    private SurfaceTexture mSurface;
    //使用的纹理id
    private int mTexture = -1;
    private GLFramebuffer mFramebuffer;
    private DrawPoints mDrawPoints;
    private DrawPass mDrawPass;
    private DrawCamera mDrawCamera;
    private byte[] mCameraData;
    private int mWidth;
    private int mHeight;
    private FaceManager mFaceManager;
    private int mCurrentDrawTimes;
    private int mLastDrawTimes;
    private TextView mFpsTv;

    private int mDisplayWidth;
    private int mDisplayHeight;
    private OrientationDetector mOrientationDetector;
//    相机旋转角度
    private int mRotation;
    private int mCameraIndex = Camera.CameraInfo.CAMERA_FACING_FRONT;
//  切换摄像头时防止画面倒转
    private int mSwitchCameraCloseNum = 0;
//    人脸点位开关
    private boolean mIsFacePointOpen = true;
//    记录是否调用了release方法
    private boolean mIsRelease = false;

    public CameraFaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initOrientationDetector();
        if (mOrientationDetector != null && mOrientationDetector.canDetectOrientation()) {
            mOrientationDetector.enable();
        }
        setEGLContextClientVersion(2);
        setRenderer(this);
        mFaceManager = new FaceManager();
        mFaceManager.setUp(context);
        CameraInterface.getInstance().setPreviewCallback(this);
        //根据纹理层的监听，有数据就绘制
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        Message message = mHandler.obtainMessage(1);
        mHandler.sendMessage(message);
    }

    //更新fps显示
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    int drawTimes = mCurrentDrawTimes - mLastDrawTimes;
                    mLastDrawTimes = mCurrentDrawTimes;
                    if(mFpsTv != null){
                        mFpsTv.setText("fps: "+drawTimes);
                    }
                    this.sendMessageDelayed(this.obtainMessage(1),1000);
                    break;
                default:
                    break;

            }
        }
    };

    public void setFpsTv(TextView fpsTv){
        this.mFpsTv = fpsTv;
    }


    private void initOrientationDetector() {
        mOrientationDetector = new OrientationDetector(getContext().getApplicationContext());
        mOrientationDetector.setOrientationChangedListener(new OrientationDetector.OrientationChangedListener() {
            @Override
            public void onOrientationChanged() {
                mRotation = getCameraRotation();
                CameraInterface.getInstance().setRotation(mRotation);
            }
        });
    }

    private int getCameraRotation() {
        int orientation = mOrientationDetector.getOrientation();
        int rotation = 90;
        if ((orientation >= 45) && (orientation < 135)) {
            rotation = 180;
        }
        if ((orientation >= 135) && (orientation < 225)) {
            rotation = 270;
        }
        if ((orientation >= 225) && (orientation < 315)) {
            rotation = 0;
        }
        if (mCameraIndex == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            if (rotation != 0) {
                rotation = 360 - rotation;
            }
        }
        return rotation;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mIsRelease = false;
        //得到view表面的纹理id
        mTexture = GLUtils.getTextureOES();
        //使用这个纹理id得到纹理层SurfaceTexture
        mSurface = new SurfaceTexture(mTexture);
        //监听纹理层
        mSurface.setOnFrameAvailableListener(this);
        mDrawPoints = new DrawPoints();
        mDrawPass = new DrawPass();
        mDrawPass.setFaceIndex(mCameraIndex);
        mDrawCamera = new DrawCamera();
        //打开相机，并未预览
        CameraInterface.getInstance().doOpenCamera();
    }
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mDisplayWidth = width;
        mDisplayHeight = height;
        glViewport(0, 0, width, height);
        //如果还未预览，就开始预览
        if(!CameraInterface.getInstance().isPreviewing()){
            CameraInterface.getInstance().doStartPreview(mSurface);
        }
    }
    @Override
    public void onDrawFrame(GL10 gl) {
        if(mIsRelease ){
            if(mFaceManager != null){
                mFaceManager.release();
            }
            return;
        }
        if (mCameraData == null) {
            return;
        }
        mCurrentDrawTimes++;
        if(mDrawPass != null){
            mDrawPass.setFaceIndex(mCameraIndex);
        }
        AliyunFace[] face106s = new AliyunFace[6];
        glClearColor(1.F, 0, 0, 1.F);
        glClear(GL_COLOR_BUFFER_BIT);
        if (mFramebuffer == null) {
            mFramebuffer = new GLFramebuffer(mWidth, mHeight);
        }
        int framebuffer = mFramebuffer.getFramebuffer();
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
        glViewport(0, 0, mWidth, mHeight);
        mDrawCamera.draw(mCameraData, mWidth, mHeight);
        if(mFaceManager != null && mIsFacePointOpen){
            mFaceManager.drawPoint(mCameraData, mWidth, mHeight, mRotation, face106s);
        }
        if (face106s[0] != null) {
            mDrawPoints.addPoints(face106s[0].keyPoints, mWidth, mHeight);
            mDrawPoints.draw();
        }
        if(mSwitchCameraCloseNum > 0){
            mCameraData = null;
            mSwitchCameraCloseNum --;
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        GLUtils.viewport(mDisplayWidth, mDisplayHeight, mWidth, mHeight);
        if(mDrawPass != null){
            mDrawPass.draw(mFramebuffer.getTexture());
        }
        mSurface.updateTexImage();
    }

    public void drawVideoFrame(byte[] data, int width, int height, int index) {
        if (mCameraData == null) {
            mCameraData = new byte[data.length];
        }
        System.arraycopy(data, 0, mCameraData, 0, data.length);
        mWidth = width;
        mHeight = height;
        this.mCameraIndex = index;
        if(mSwitchCameraCloseNum > 0){
            mCameraData = null;
            mSwitchCameraCloseNum --;
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        Log.i(TAG, "onFrameAvailable...");
        this.requestRender();
    }

    public void release(){
        CameraInterface.getInstance().release();
        mOrientationDetector.disable();
        mIsRelease = true;
        if(mHandler != null){
            mHandler.removeMessages(1);
        }
    }

    public void onCameraSwitch() {
        mSwitchCameraCloseNum = 5;
        CameraInterface.getInstance().switchCamera();
        CameraInterface.getInstance().doStartPreview(mSurface);
    }

    public void onOpenFacePoint(boolean open) {
        this.mIsFacePointOpen = open;
    }

    @Override
    public void onPreviewFrame(byte[] data, int width, int height, int cameraFacing) {
        drawVideoFrame(data, width, height, cameraFacing);
        this.requestRender();
    }
}