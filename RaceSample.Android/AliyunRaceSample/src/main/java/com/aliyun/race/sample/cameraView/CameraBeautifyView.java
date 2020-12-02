package com.aliyun.race.sample.cameraView;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
import com.aliyun.race.sample.bean.BeautyLevel;
import com.aliyun.race.sample.bean.BeautyParams;
import com.aliyun.race.sample.bean.BeautyShapeConstants;
import com.aliyun.race.sample.bean.BeautyShapeParams;
import com.aliyun.race.sample.bean.RaceMode;
import com.aliyun.race.sample.bean.RememberBeautyBean;
import com.aliyun.race.sample.bean.RememberBeautyShapeBean;
import com.aliyun.race.sample.race.RaceInitCallback;
import com.aliyun.race.sample.race.RaceManager;
import com.aliyun.race.sample.render.DirectDrawer;
import com.aliyun.race.sample.render.DrawPass;
import com.aliyun.race.sample.render.GLUtils;
import com.aliyun.race.sample.utils.OrientationDetector;
import com.aliyun.race.sample.utils.SharedPreferenceUtils;
import com.aliyun.race.sample.utils.constants.BeautyRaceConstants;
import com.aliyun.race.sample.view.BeautyEffectChooser;
import com.aliyun.race.sample.view.DialogVisibleListener;
import com.aliyun.race.sample.view.face.BeautyFaceDetailChooser;
import com.aliyun.race.sample.view.listener.OnBeautyDetailClickListener;
import com.aliyun.race.sample.view.listener.OnBeautyFaceItemSeletedListener;
import com.aliyun.race.sample.view.listener.OnBeautyParamsChangeListener;
import com.aliyun.race.sample.view.listener.OnBeautyShapeItemSeletedListener;
import com.aliyun.race.sample.view.listener.OnBeautyShapeParamsChangeListener;
import com.aliyun.race.sample.view.listener.OnViewClickListener;
import com.aliyun.race.sample.view.shape.BeautyShapeDetailChooser;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;
import static com.aliyun.race.sample.render.GLUtils.glCheck;


public class CameraBeautifyView extends GLSurfaceView implements GLSurfaceView.Renderer,
        SurfaceTexture.OnFrameAvailableListener,
        ControlViewListener,
        PreviewCallback, RaceInitCallback {
    private static final String TAG = "CameraBeautifyView";
    private SurfaceTexture mSurface;
    private int mTextureID = -1;
    private DrawPass mDrawPass;
    private DirectDrawer mDirectDrawer;
    private byte[] mCameraData;
    private int mWidth;
    private int mHeight;
    private RaceManager mRaceManagerModeTexture;
    private RaceManager mRaceManagerModeBuffer;
    private RaceManager mCurrentRaceManager;
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
//    记录是否开启美颜
    private boolean mIsRaceOpen = true;
//    记录race初始化是否成功
    private boolean mIsRaceInitSuccess = true;

    /**
     * 记录美颜选中的索引, 默认为3
     */
    private int mCurrentBeautyFacePosition = 3;

    /**
     * 当前美型选择的item下标, 默认为0
     */
    private int mCurrentBeautyShapePosition = 0;

//    记录是否调用了release方法
    private boolean mIsRelease = false;

    private List<BeautyParams> mRememberRaceParamList;
    private RememberBeautyBean mRememberRaceBeautyBean;
    private List<BeautyShapeParams> mRememberShapeParamList;
    private RememberBeautyShapeBean mRememberBeautyShapeBean;

    private BeautyParams mBeautyParams;
    private BeautyShapeParams mBeautyShapeParams;
    private BeautyEffectChooser mBeautyEffectChooser;
    private BeautyFaceDetailChooser mBeautyFaceDetailChooser;
    private BeautyShapeDetailChooser mBeautyShapeDetailChooser;

    public CameraBeautifyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initOrientationDetector();
        if (mOrientationDetector != null && mOrientationDetector.canDetectOrientation()) {
            mOrientationDetector.enable();
        }
        setEGLContextClientVersion(2);
        setRenderer(this);
        mRaceManagerModeTexture = new RaceManager();
        mRaceManagerModeTexture.setUp(context);
        mRaceManagerModeBuffer = new RaceManager();
        mRaceManagerModeBuffer.setUp(context);
        mCurrentRaceManager = mRaceManagerModeTexture;
        mRaceManagerModeTexture.setRaceInitCallback(this);
        mRaceManagerModeBuffer.setRaceInitCallback(this);
        initBeautyParam();
        CameraInterface.getInstance().setPreviewCallback(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        Message message = mHandler.obtainMessage(1);
        mHandler.sendMessage(message);
    }
    private void initBeautyParam() {
        beautyParamCopy();
        //进来前应该获取上一次记住的美颜模式
        mCurrentBeautyFacePosition = SharedPreferenceUtils.getBeautyFaceLevel(getContext());
        mCurrentBeautyShapePosition = SharedPreferenceUtils.getBeautyShapeLevel(getContext());
        // 初始化美颜的值，带入上一次设置值
        BeautyParams beautyParams = mRememberRaceParamList.get(mCurrentBeautyFacePosition);
        if (mCurrentRaceManager != null) {
            mCurrentRaceManager
                    .setFaceBeautyWhite(beautyParams.mBeautyWhite / 100)
                    .setFaceBeautySharpLevel(beautyParams.mBeautyRuddy / 100)
                    .setFaceBeautyBuffing(beautyParams.mBeautyBuffing / 100);

            mBeautyShapeParams = getBeautyShapeParams(mCurrentBeautyShapePosition);
            if (mBeautyShapeParams == null) {
                mBeautyShapeParams = mRememberShapeParamList.get(mCurrentBeautyShapePosition);
            }
            mCurrentRaceManager.setShapeParam(mBeautyShapeParams);
        }
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

    private void beautyParamCopy() {
//        race美颜
        mRememberRaceBeautyBean = new RememberBeautyBean();
        mRememberRaceParamList = new ArrayList<>();
        int raceSize = BeautyRaceConstants.BEAUTY_MAP.size();
        // 需求要记录之前修改的美颜参数, 所以每次先读取json中的数据,如果json无数据, 就从常量中拿
        String jsonRaceBeautyParam = SharedPreferenceUtils.getRaceBeautyParams(getContext());
        if (TextUtils.isEmpty(jsonRaceBeautyParam)) {
            for (int i = 0; i < raceSize; i++) {
                BeautyParams beautyParams = BeautyRaceConstants.BEAUTY_MAP.get(i);
                BeautyParams rememberParam = beautyParams.clone();
                mRememberRaceParamList.add(rememberParam);
            }
        } else {
            for (int i = 0; i < raceSize; i++) {
                BeautyParams beautyParams = getBeautyParams(i);
                mRememberRaceParamList.add(beautyParams);
            }
        }
        mRememberRaceBeautyBean.setBeautyList(mRememberRaceParamList);
//      美型
        mRememberBeautyShapeBean = new RememberBeautyShapeBean();

        mRememberShapeParamList = new ArrayList<>();

        int shapeSize = BeautyShapeConstants.BEAUTY_MAP.size();
        // 需求要记录之前修改的美颜参数, 所以每次先读取json中的数据,如果json无数据, 就从常量中拿
        String jsonBeautyShapeParam = SharedPreferenceUtils.getBeautyShapeParams(getContext());
        if (TextUtils.isEmpty(jsonBeautyShapeParam)) {
            for (int i = 0; i < shapeSize; i++) {
                BeautyShapeParams beautyParams = BeautyShapeConstants.BEAUTY_MAP.get(i);
                BeautyShapeParams rememberParam = beautyParams.clone();
                mRememberShapeParamList.add(rememberParam);
            }
        } else {
            for (int i = 0; i < shapeSize; i++) {
                BeautyShapeParams beautyParams = getBeautyShapeParams(i);
                if (beautyParams == null) {
                    beautyParams = BeautyShapeConstants.BEAUTY_MAP.get(i);
                }
                mRememberShapeParamList.add(beautyParams);
            }
        }
        mRememberBeautyShapeBean.setBeautyList(mRememberShapeParamList);
    }

    /**
     * 获取美型参数
     */
    private BeautyShapeParams getBeautyShapeParams(int position) {
        String jsonString = SharedPreferenceUtils.getBeautyShapeParams(getContext());
        if (TextUtils.isEmpty(jsonString)) {
            return null;
        }
        Gson gson = new Gson();
        RememberBeautyShapeBean rememberBeautyBean = gson.fromJson(jsonString, RememberBeautyShapeBean.class);
        List<BeautyShapeParams> beautyList = rememberBeautyBean.getBeautyList();
        if (beautyList == null) {
            return null;
        }
        if (position >= beautyList.size()) {
            return null;
        }
        return beautyList.get(position);
    }

    /**
     * 获取美颜美肌参数
     */
    private BeautyParams getBeautyParams(int position) {
        String jsonString = SharedPreferenceUtils.getRaceBeautyParams(getContext());
        if (TextUtils.isEmpty(jsonString)) {
            return null;
        }
        Gson gson = new Gson();
        RememberBeautyBean rememberBeautyBean = gson.fromJson(jsonString, RememberBeautyBean.class);
        List<BeautyParams> beautyList = rememberBeautyBean.getBeautyList();
        if (beautyList == null) {
            return null;
        }
        return beautyList.get(position);
    }


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


    private void processBufferToBuffer(){
        try {
        int widthInt = 608;
        int heightInt = 544;
        int rotationInt = 0;
        File file = new File(getContext().getExternalFilesDir("") + "/video.yuv");
        FileInputStream inputStream = null;
            inputStream = new FileInputStream(file);
            byte[] bytes = new byte[widthInt * heightInt * 3 / 2];
            int b;
            while((b = inputStream.read(bytes,0,bytes.length))!=-1){
                mCurrentRaceManager.processBufferToBuffer(bytes,widthInt,heightInt,rotationInt);
            }
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mIsRelease = false;
        mTextureID = GLUtils.getTextureOES();
        mSurface = new SurfaceTexture(mTextureID);
        mSurface.setOnFrameAvailableListener(this);
        mDrawPass = new DrawPass();
        mDrawPass.setBeautyIndex(mCameraIndex);
        mDirectDrawer = new DirectDrawer();
        mDirectDrawer.setBeautyIndex(mCameraIndex);
        glCheck();
        processBufferToBuffer();
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
        glCheck();
    }


    @Override
    public void onPause() {
        if(CameraInterface.getInstance().isPreviewing()){
            CameraInterface.getInstance().doStopCamera();
        }

    }

    @Override
    public void onResume() {
        if(!CameraInterface.getInstance().isPreviewing()){
            CameraInterface.getInstance().doOpenCamera();
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if(mIsRelease){
            if(mCurrentRaceManager != null){
                mCurrentRaceManager.release();
            }
            if(mRaceManagerModeTexture != null){
                mRaceManagerModeTexture.release();
            }
            if(mRaceManagerModeBuffer != null){
                mRaceManagerModeBuffer.release();
            }
            return;
        }
        mCurrentDrawTimes++;
        glCheck();
        glClearColor(1.F, 0, 0, 1.F);
        glClear(GL_COLOR_BUFFER_BIT);
        glCheck();
        //从图像流中将纹理图像更新为最近的帧
        int texture = mTextureID;
        if(mIsRaceOpen && mCameraData != null && mCurrentRaceManager != null){
            glCheck();
            texture = mCurrentRaceManager.draw(mCameraData,mTextureID, mWidth, mHeight, mRotation);
            glCheck();
        }
        if(mDrawPass != null){
            mDrawPass.setBeautyIndex(mCameraIndex);
        }
        if(mDirectDrawer != null){
            mDirectDrawer.setBeautyIndex(mCameraIndex);
        }
        if (mWidth > 0 && mHeight > 0) {
            int width = mHeight;
            int height = mWidth;
            float ar = mHeight / (float) mWidth;
            if (mDisplayWidth / (float) mDisplayHeight > ar) {
                int scaleHeight = mDisplayWidth * height / width;
                int offset = (mDisplayHeight - scaleHeight) / 2;
                glViewport(0, offset, mDisplayWidth, scaleHeight);
            } else {
                int scaleWidth = mDisplayHeight * width / height;
                int offset = (mDisplayWidth - scaleWidth) / 2;
                glViewport(offset, 0, scaleWidth, mDisplayHeight);
            }
        } else {
            glViewport(0, 0, mDisplayWidth, mDisplayHeight);
        }
        glCheck();
        if(mSwitchCameraCloseNum > 0){
            mCameraData = null;
            mSwitchCameraCloseNum --;
        }
        mSurface.updateTexImage();
        glCheck();

        if(mIsRaceOpen && mIsRaceInitSuccess && mDrawPass != null){
//        开启美颜且初始化成功，走race美颜的渲染
            mDrawPass.draw(texture);
        } else if((!mIsRaceOpen || !mIsRaceInitSuccess) && mDirectDrawer != null){
//        未开启美颜或者初始化失败，走普通渲染
            mDirectDrawer.draw(texture);
        }
    }


    public void drawVideoFrame(byte[] data, int width, int height, int index) {
        if (mCameraData == null) {
            mCameraData = new byte[data.length];
        }
        System.arraycopy(data, 0, mCameraData, 0, data.length);
        mWidth = width;
        mHeight = height;
        this.mCameraIndex = index;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        Log.i(TAG, "onFrameAvailable...");
        this.requestRender();
    }

    public void release(){
        mIsRelease = true;
        CameraInterface.getInstance().release();
        mOrientationDetector.disable();

        if(mHandler != null){
            mHandler.removeMessages(1);
        }
    }


    @Override
    public void onBeautyFaceClick(final FragmentManager manager) {
        if (mBeautyEffectChooser == null) {
            mBeautyEffectChooser = new BeautyEffectChooser();
        }
        // 美颜item选中listener
        mBeautyEffectChooser.setOnBeautyFaceItemSeletedListener(new OnBeautyFaceItemSeletedListener() {
            @Override
            public void onNormalSelected(int postion, BeautyLevel beautyLevel) {
            }
            @Override
            public void onAdvancedSelected(int position, BeautyLevel beautyLevel) {
                mCurrentBeautyFacePosition = position;
                // 高级美颜
                BeautyParams beautyParams = mRememberRaceParamList.get(position);
                //// 美白和红润faceUnity的值范围 0~1.0f
                if (mCurrentRaceManager != null) {
                    mCurrentRaceManager
                            .setFaceBeautyWhite(beautyParams.mBeautyWhite / 100)
                            .setFaceBeautySharpLevel(beautyParams.mBeautyRuddy / 100)
                            .setFaceBeautyBuffing(beautyParams.mBeautyBuffing / 100);
                    saveSelectParam(getContext(), mCurrentBeautyFacePosition, mCurrentBeautyShapePosition);
                }
            }
        });

        // 美型item选中
        mBeautyEffectChooser.setOnBeautyShapeItemSeletedListener(new OnBeautyShapeItemSeletedListener() {
            @Override
            public void onItemSelected(int postion) {
                mCurrentBeautyShapePosition = postion;
                saveSelectParam(getContext(), mCurrentBeautyFacePosition, mCurrentBeautyShapePosition);
                if (mCurrentRaceManager != null) {
                    mBeautyShapeParams = getBeautyShapeParams(mCurrentBeautyShapePosition);
                    if (mBeautyShapeParams == null) {
                        mBeautyShapeParams = mRememberShapeParamList.get(mCurrentBeautyShapePosition);
                    }
                    mCurrentRaceManager.setShapeParam(mBeautyShapeParams);

                }
            }
        });

        // 美颜微调dialog
        mBeautyEffectChooser.setOnBeautyFaceDetailClickListener(new OnBeautyDetailClickListener() {
            @Override
            public void onDetailClick() {
                mBeautyEffectChooser.dismiss();
                showBeautyFaceDetailDialog(manager);

            }
        });

        // 美型微调dialog
        mBeautyEffectChooser.setOnBeautyShapeDetailClickListener(new OnBeautyDetailClickListener() {
            @Override
            public void onDetailClick() {
                mBeautyEffectChooser.dismiss();
                showBeautyShapeDetailDialog(manager);
            }
        });


        mBeautyEffectChooser.setDismissListener(new DialogVisibleListener() {
            @Override
            public void onDialogDismiss() {
                // 如果微调的页面不在显示状态,
                saveSelectParam(getContext(), mCurrentBeautyFacePosition, mCurrentBeautyShapePosition);
            }

            @Override
            public void onDialogShow() {
                mBeautyEffectChooser.setBeautyParams(CameraBeautifyView.this.mBeautyParams);
            }
        });
        mBeautyEffectChooser.show(manager, "beauty");
    }
    /**
     * 显示美颜微调dialog
     */
    private void showBeautyFaceDetailDialog(final FragmentManager manager) {
        mBeautyParams = getBeautyParams(mCurrentBeautyFacePosition);
        if (mBeautyParams == null) {
            mBeautyParams = mRememberRaceParamList.get(mCurrentBeautyFacePosition);
        }
        mBeautyFaceDetailChooser = new BeautyFaceDetailChooser();
        mBeautyFaceDetailChooser.setBeautyLevel(mCurrentBeautyFacePosition);
        mBeautyFaceDetailChooser.setOnBeautyParamsChangeListener(
                new OnBeautyParamsChangeListener() {
                    @Override
                    public void onBeautyChange(BeautyParams param) {
                        if (mBeautyParams != null && param != null) {
                            mBeautyParams.mBeautyWhite = param.mBeautyWhite;
                            mBeautyParams.mBeautyBuffing = param.mBeautyBuffing;
                            mBeautyParams.mBeautyRuddy = param.mBeautyRuddy;
                            if (mCurrentRaceManager != null) {
                                // 美白
                                mCurrentRaceManager.setFaceBeautyWhite(param.mBeautyWhite / 100);
                                // 红润
                                mCurrentRaceManager.setFaceBeautySharpLevel(param.mBeautyRuddy / 100);
                                // 磨皮
                                mCurrentRaceManager.setFaceBeautyBuffing(param.mBeautyBuffing / 100);
                            }
                        }
                    }
                });
        // 点击back按钮
        mBeautyFaceDetailChooser.setOnBackClickListener(new OnViewClickListener() {
            @Override
            public void onClick() {
                mBeautyFaceDetailChooser.dismiss();
                mBeautyEffectChooser.show(manager, "");
            }
        });
        mBeautyFaceDetailChooser.setDismissListener(new DialogVisibleListener() {
            @Override
            public void onDialogDismiss() {
                // 如果是点击微调界面中的back按钮, controlview的底部view仍要保持隐藏状态
                saveBeautyParams(mCurrentBeautyFacePosition, mBeautyParams);
            }

            @Override
            public void onDialogShow() {
            }
        });
        mBeautyFaceDetailChooser.setBeautyParams(mBeautyParams);
        mBeautyEffectChooser.dismiss();
        mBeautyFaceDetailChooser.show(manager, "beautyFace");
    }


    /**
     * 显示美型微调dialog
     */
    private void showBeautyShapeDetailDialog(final FragmentManager manager) {
        mBeautyShapeParams = getBeautyShapeParams(mCurrentBeautyShapePosition);
        if (mBeautyShapeParams == null) {
            mBeautyShapeParams = mRememberShapeParamList.get(mCurrentBeautyShapePosition);
        }

        mBeautyShapeDetailChooser = new BeautyShapeDetailChooser();
        mBeautyShapeDetailChooser.setBeautyLevel(mCurrentBeautyShapePosition);
        mBeautyShapeDetailChooser.setOnBeautyParamsChangeListener(new OnBeautyShapeParamsChangeListener() {
            @Override
            public void onBeautyChange(BeautyShapeParams param) {
                if (mBeautyShapeParams != null && param != null) {

                    mBeautyShapeParams.mBeautyCutFace = param.mBeautyCutFace;
                    mBeautyShapeParams.mBeautyThinFace = param.mBeautyThinFace;
                    mBeautyShapeParams.mBeautyLongFace = param.mBeautyLongFace;
                    mBeautyShapeParams.mBeautyLowerJaw = param.mBeautyLowerJaw;
                    mBeautyShapeParams.mBeautyBigEye = param.mBeautyBigEye;
                    mBeautyShapeParams.mBeautyThinNose = param.mBeautyThinNose;
                    mBeautyShapeParams.mBeautyMouthWidth = param.mBeautyMouthWidth;
                    mBeautyShapeParams.mBeautyThinMandible = param.mBeautyThinMandible;
                    mBeautyShapeParams.mBeautyCutCheek = param.mBeautyCutCheek;
                    if (mCurrentRaceManager != null) {
                        mCurrentRaceManager.setShapeParam(mBeautyShapeParams);
                    }
                    saveBeautyShapeParams(mCurrentBeautyShapePosition, mBeautyShapeParams);

                }
            }
        });

        // 点击back按钮
        mBeautyShapeDetailChooser.setOnBackClickListener(new OnViewClickListener() {
            @Override
            public void onClick() {
                mBeautyShapeDetailChooser.dismiss();
                mBeautyEffectChooser.show(manager, "beautyShape");
            }
        });


        mBeautyShapeDetailChooser.setDismissListener(new DialogVisibleListener() {
            @Override
            public void onDialogDismiss() {
                // 如果是点击微调界面中的back按钮, controlview的底部view仍要保持隐藏状态
                saveBeautyShapeParams(mCurrentBeautyShapePosition, CameraBeautifyView.this.mBeautyShapeParams);
            }

            @Override
            public void onDialogShow() {

            }
        });
        mBeautyShapeDetailChooser.setBeautyShapeParams(mBeautyShapeParams);
        mBeautyEffectChooser.dismiss();
        mBeautyShapeDetailChooser.show(manager, "");
    }

    private void saveBeautyShapeParams(int position, BeautyShapeParams beautyParams) {
        if (beautyParams != null) {
            Gson gson = new Gson();

            mRememberShapeParamList.set(position, beautyParams);
            mRememberBeautyShapeBean.setBeautyList(mRememberShapeParamList);
            String jsonString = gson.toJson(mRememberBeautyShapeBean);

            if (!TextUtils.isEmpty(jsonString)) {
                SharedPreferenceUtils.setBeautyShapeParams(getContext(), jsonString);
            }
        }
    }

    private void saveBeautyParams(int position, BeautyParams beautyParams) {
        if (beautyParams != null) {
            Gson gson = new Gson();
            mRememberRaceParamList.set(position, beautyParams);
            mRememberRaceBeautyBean.setBeautyList(mRememberRaceParamList);
            String jsonString = gson.toJson(mRememberRaceBeautyBean);
            if (!TextUtils.isEmpty(jsonString)) {
                SharedPreferenceUtils.setRaceBeautyParams(getContext(), jsonString);
            }

        }
    }

    private void saveSelectParam(Context context,  int beautyFacePosition, int beautySharpPosition) {
        SharedPreferenceUtils.setBeautyFaceLevel(context, beautyFacePosition);
        SharedPreferenceUtils.setBeautyShapeLevel(context,beautySharpPosition);
    }



    @Override
    public void onCameraSwitch() {
        mSwitchCameraCloseNum = 5;
        CameraInterface.getInstance().switchCamera();
        CameraInterface.getInstance().doStartPreview(mSurface);
    }

    @Override
    public void onOpenRace(boolean open) {
        mIsRaceOpen = open;

    }

    @Override
    public void onOpenFacePoint(boolean open) {
    }

    @Override
    public void switchRaceMode(RaceMode mode) {
        if(mode == RaceMode.BUFFER){
            mCurrentRaceManager = mRaceManagerModeBuffer;
        } else {
            mCurrentRaceManager = mRaceManagerModeTexture;
        }
        initBeautyParam();
        mCurrentRaceManager.setRaceMode(mode);
    }

    @Override
    public void onPreviewFrame(byte[] data, int width, int height, int cameraFacing) {
        drawVideoFrame(data, width, height, cameraFacing);
    }

    /**
     * 判断race初始化是否成功，初始化失败则不走race的渲染
     * */
    @Override
    public void onRaceInitResult(boolean initSuccess) {
        mIsRaceInitSuccess = initSuccess;
    }
}