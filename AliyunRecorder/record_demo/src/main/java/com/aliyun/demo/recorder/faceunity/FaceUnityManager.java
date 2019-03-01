package com.aliyun.demo.recorder.faceunity;

import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.aliyun.demo.recorder.view.effects.otherfilter.Effect;
import com.faceunity.FURenderer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * FaceUnify管理类，支持高级美颜(美白，磨皮，红润调整)，美型(脸型，大眼，瘦脸)
 *
 * @author Mulberry
 *         create on 2018/7/11.
 */

public class FaceUnityManager {
    private static final String TAG = "FaceUnityManager";
    static FaceUnityManager faceUnityManager = null;

    /**
     * 美颜道具
     */
    //private int mFaceBeautyItem = 0;

    /**
     * 美白
     */
    private float mFaceBeautyColorLevel = 0.2f;
    /**
     * 磨皮
     */
    private float mFaceBeautyBlurLevel = 6.0f;
    /**
     * 精准磨皮,传0和1的值是指是否开启精准磨皮
     */
    private float mFaceBeautyALLBlurLevel = 0.0f;
    /**
     * 红润
     */
    private float mFaceBeautyRedLevel = 0.5f;
    /**
     * 瘦脸
     */
    private float mFaceBeautyCheekThin = 1.0f;
    /**
     * 大眼
     */
    private float mFaceBeautyEnlargeEye = 0.5f;
    /**
     * 美型，脸型选择，有0，1，2，3值可选
     */
    private int mFaceShape = 3;
    /**
     * 美型程度 0-1的值
     */
    private float mFaceShapeLevel = 0.5f;

    public FaceUnityManager() {

    }

    public static FaceUnityManager getInstance(Context mContext) {
        if (faceUnityManager != null) {
            faceUnityManager.init(mContext);
            return faceUnityManager;
        }
        synchronized (FaceUnityManager.class) {
            faceUnityManager = new FaceUnityManager();
            faceUnityManager.init(mContext);
            return faceUnityManager;
        }
    }

    public void init(Context mContext) {
        this.mContext = mContext.getApplicationContext();
        mEventQueue = new ArrayList<>();
        mFuItemHandlerThread = new HandlerThread("FUItemHandlerThread");
        mFuItemHandlerThread.start();
        mFuItemHandler = new FUItemHandler(mFuItemHandlerThread.getLooper());
    }

    /**
     * faceUnity SDK初始化,
     *
     * @param context
     */
    public boolean setUp(Context context) {
        return FURenderer.initFURenderer(context);
    }

    /**
     * 创建美颜相关
     *
     * @param context
     * @return
     */
    public boolean createBeautyItem(Context context) {
        InputStream inputStream = null;
        /**
         * 美颜初始化
         */
        try {
            inputStream = context.getAssets().open("face_beautification.bundle");
            byte[] itemData = new byte[inputStream.available()];
            int len = inputStream.read(itemData);
            Log.e(TAG, "beautification len " + len);
            inputStream.close();
            mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX] = FURenderer.fuCreateItemFromPackage(itemData);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean createBeautyItem(Context context, final boolean isDongmLvj, Effect effect) {
        InputStream inputStream = null;
        if (effect == null || effect.effectType() == Effect.EFFECT_TYPE_NONE)
            return false;
        /**
         * 美颜初始化
         */
        try {
            inputStream = context.getAssets().open(effect.path());
            byte[] itemData = new byte[inputStream.available()];
            int len = inputStream.read(itemData);
            Log.e(TAG, "len " + len);
            inputStream.close();

            final int newEffectItem = loadItem(effect);
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    if (isDongmLvj) {
                        isOpenAnimoji = isDongmLvj;
                        if (mItemsArray[ITEM_ARRAYS_ANIMOJI_FILTER] > 0) {
                            FURenderer.fuDestroyItem(mItemsArray[ITEM_ARRAYS_ANIMOJI_FILTER]);
                        }
                        mItemsArray[ITEM_ARRAYS_ANIMOJI_FILTER] = newEffectItem;
                    } else {
                        if (mItemsArray[ITEM_ARRAYS_EFFECT] > 0) {
                            FURenderer.fuDestroyItem(mItemsArray[ITEM_ARRAYS_EFFECT]);
                        }
                        mItemsArray[ITEM_ARRAYS_EFFECT] = newEffectItem;
                    }
                }
            });
            //queueEvent的Runnable在此处被调用
            while (!mEventQueue.isEmpty()) {
                mEventQueue.remove(0).run();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param cameraNV21Byte    cameraNV21原始数据
     * @param fuImgNV21Bytes    为用来人脸识别的图像内存数据
     * @param cameraTextureId   为用来绘制的texture id
     * @param cameraWidth       摄像头采集数据的宽
     * @param cameraHeight      摄像头采集数据的高
     * @param frameId           为当前帧数序号，重新初始化后需要从0开始
     * @param currentCameraType
     */
    public int draw(byte[] cameraNV21Byte, byte[] fuImgNV21Bytes, int cameraTextureId, int cameraWidth, int cameraHeight, int frameId, int currentCameraType
            , int mInputImageOrientation, boolean isNormal, boolean isAnimoji, boolean isGesture, boolean isMusic, long time) {
        final int isTracking = FURenderer.fuIsTracking();
        Log.d("draw", "isTracking=" + isTracking);
        FURenderer.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "color_level", mFaceBeautyColorLevel);
        FURenderer.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "blur_level", mFaceBeautyBlurLevel);
        FURenderer.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "skin_detect", mFaceBeautyALLBlurLevel);
        FURenderer.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "cheek_thinning", mFaceBeautyCheekThin);
        FURenderer.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "eye_enlarging", mFaceBeautyEnlargeEye);
        FURenderer.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "face_shape", mFaceShape);
        FURenderer.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "face_shape_level", mFaceShapeLevel);
        FURenderer.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "red_level", mFaceBeautyRedLevel);

        boolean isOESTexture = true; //Tip: camera texture类型是默认的是OES的，和texture 2D不同
        int flags = isOESTexture ? FURenderer.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE : 0;
        boolean isNeedReadBack = false; //是否需要写回，如果是，则入参的byte[]会被修改为带有fu特效的；支持写回自定义大小的内存数组中，即readback custom img
        flags = isNeedReadBack ? flags | FURenderer.FU_ADM_FLAG_ENABLE_READBACK : flags;
        if (isNeedReadBack) {
            if (fuImgNV21Bytes == null) {
                fuImgNV21Bytes = new byte[cameraNV21Byte.length];
            }
            System.arraycopy(cameraNV21Byte, 0, fuImgNV21Bytes, 0, cameraNV21Byte.length);
        } else {
            fuImgNV21Bytes = cameraNV21Byte;
        }
        flags |= currentCameraType == Camera.CameraInfo.CAMERA_FACING_FRONT ? 0 : FURenderer.FU_ADM_FLAG_FLIP_X;

        updateEffectItemParams(isNormal, isAnimoji, isGesture
                , currentCameraType == Camera.CameraInfo.CAMERA_FACING_FRONT ? 0 : 1, mInputImageOrientation, isMusic, time);
            /*
             * 这里拿到fu处理过后的texture，可以对这个texture做后续操作，如硬编、预览。
             */
        return FURenderer.fuDualInputToTexture(fuImgNV21Bytes, cameraTextureId, flags,
                cameraWidth, cameraHeight, frameId, mItemsArray);
    }

    /**
     * 美白
     *
     * @param mFaceBeautyColorLevel
     */
    public void setFaceBeautyColorLevel(float mFaceBeautyColorLevel) {
        this.mFaceBeautyColorLevel = mFaceBeautyColorLevel;
    }

    /**
     * 磨皮
     *
     * @param mFaceBeautyBlurLevel
     */
    public void setFaceBeautyBlurLevel(float mFaceBeautyBlurLevel) {
        this.mFaceBeautyBlurLevel = mFaceBeautyBlurLevel;
    }

    /**
     * 精准磨皮
     *
     * @param mFaceBeautyALLBlurLevel
     */
    public void setFaceBeautyALLBlurLevel(float mFaceBeautyALLBlurLevel) {
        this.mFaceBeautyALLBlurLevel = mFaceBeautyALLBlurLevel;
    }


    /**
     * 瘦脸
     *
     * @return
     */
    public void setFaceBeautyCheekThin(float mFaceBeautyCheekThin) {
        this.mFaceBeautyCheekThin = mFaceBeautyCheekThin;
    }

    /**
     * 大眼
     *
     * @param mFaceBeautyEnlargeEye
     */
    public void setFaceBeautyEnlargeEye(float mFaceBeautyEnlargeEye) {
        this.mFaceBeautyEnlargeEye = mFaceBeautyEnlargeEye;
    }

    /**
     * 红润
     *
     * @return
     */
    public void setFaceBeautyRedLevel(float mFaceBeautyRedLevel) {
        this.mFaceBeautyRedLevel = mFaceBeautyRedLevel;
    }

    /**
     * 美型，脸型选择
     *
     * @param mFaceShape
     */
    public void setFaceShape(int mFaceShape) {
        this.mFaceShape = mFaceShape;
    }

    /**
     * 美型程度
     *
     * @param mFaceShapeLevel 0-1的值
     */
    public void setFaceShapeLevel(float mFaceShapeLevel) {
        this.mFaceShapeLevel = mFaceShapeLevel;
    }

    //--------------------------------------道具（异步加载道具）----------------------------------------
    //用于和异步加载道具的线程交互
    private HandlerThread mFuItemHandlerThread;
    private Handler mFuItemHandler;
    private ArrayList<Runnable> mEventQueue;
    private static final int ITEM_ARRAYS_FACE_BEAUTY_INDEX = 0;
    private static final int ITEM_ARRAYS_EFFECT = 1;
    private static final int ITEM_ARRAYS_ANIMOJI_FILTER = 2;
    private static final int ITEM_ARRAYS_COUNT = 3;
    //美颜和其他道具的handle数组
    private final int[] mItemsArray = new int[ITEM_ARRAYS_COUNT];
    private Context mContext;
    private boolean isOpenAnimoji = false;//是否开启动漫滤镜

    //哈哈镜
    public void createItem(Effect item) {
        if (item == null) return;
        mFuItemHandler.removeMessages(FUItemHandler.HANDLE_CREATE_ITEM);
        mFuItemHandler.sendMessage(Message.obtain(mFuItemHandler, FUItemHandler.HANDLE_CREATE_ITEM, item));
    }

    //动漫滤镜
    public void onLoadAnimFilter(final boolean enable, Effect effect) {
        isOpenAnimoji = enable;
        mFuItemHandler.removeMessages(ITEM_ARRAYS_ANIMOJI_FILTER);
        mFuItemHandler.sendMessage(Message.obtain(mFuItemHandler, ITEM_ARRAYS_ANIMOJI_FILTER, effect));
    }

    class FUItemHandler extends Handler {

        static final int HANDLE_CREATE_ITEM = 1;
        static final int HANDLE_CREATE_ANIMOJI3D_ITEM = 3;

        FUItemHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //加载道具
                case HANDLE_CREATE_ITEM:
                    final Effect effect = (Effect) msg.obj;
                    final int newEffectItem = loadItem(effect);
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            if (mItemsArray[ITEM_ARRAYS_EFFECT] > 0) {
                                FURenderer.fuDestroyItem(mItemsArray[ITEM_ARRAYS_EFFECT]);
                            }
                            mItemsArray[ITEM_ARRAYS_EFFECT] = newEffectItem;
                        }
                    });
                    //queueEvent的Runnable在此处被调用
                    while (!mEventQueue.isEmpty()) {
                        mEventQueue.remove(0).run();
                    }
                    break;
                //加载animoji道具3D抗锯齿bundle
                case HANDLE_CREATE_ANIMOJI3D_ITEM:
                    break;
                // 加载 animoji 风格滤镜
                case ITEM_ARRAYS_ANIMOJI_FILTER:
                    final Effect item = (Effect) msg.obj;
                    if (isOpenAnimoji) {
                        if (mItemsArray[ITEM_ARRAYS_ANIMOJI_FILTER] <= 0) {
                            mItemsArray[ITEM_ARRAYS_ANIMOJI_FILTER] = loadItem(item);
                        }
                        queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                FURenderer.fuItemSetParam(mItemsArray[ITEM_ARRAYS_EFFECT], "{\"thing\":\"<global>\",\"param\":\"follow\"}", 1);
//                                int supportGLVersion = GlUtil.getSupportGLVersion(mContext);
//                                faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_ANIMOJI_FILTER], "glVer", supportGLVersion);
                            }
                        });
                    } else {
                        if (mItemsArray[ITEM_ARRAYS_ANIMOJI_FILTER] > 0) {
                            queueEvent(new Runnable() {
                                @Override
                                public void run() {
                                    FURenderer.fuDestroyItem(mItemsArray[ITEM_ARRAYS_ANIMOJI_FILTER]);
                                    FURenderer.fuItemSetParam(mItemsArray[ITEM_ARRAYS_EFFECT], "{\"thing\":\"<global>\",\"param\":\"follow\"}", 0);
                                    mItemsArray[ITEM_ARRAYS_ANIMOJI_FILTER] = 0;
                                }
                            });
                        }
                    }
                    //queueEvent的Runnable在此处被调用
                    while (!mEventQueue.isEmpty()) {
                        mEventQueue.remove(0).run();
                    }
                    break;
            }
        }
    }

    /**
     * 设置对道具设置相应的参数
     */
    private void updateEffectItemParams(boolean isNORMAL, boolean isANIMOJI, boolean isGESTURE
            , int mCurrentCameraType, int mInputImageOrientation, boolean isMusic, final long time) {
        final int itemHandle = mItemsArray[ITEM_ARRAYS_EFFECT];
        if (itemHandle <= 0)
            return;
        FURenderer.fuItemSetParam(itemHandle, "isAndroid", 1.0);
        if (mItemsArray[ITEM_ARRAYS_ANIMOJI_FILTER] > 0) {
            FURenderer.fuItemSetParam(itemHandle, "{\"thing\":\"<global>\",\"param\":\"follow\"}", 1);
        }

        if (isNORMAL) {
            //rotationAngle 参数是用于旋转普通道具
            FURenderer.fuItemSetParam(itemHandle, "rotationAngle", 360 - mInputImageOrientation);
        }
        if (isANIMOJI) {
            //is3DFlipH 参数是用于对3D道具的镜像
            FURenderer.fuItemSetParam(itemHandle, "is3DFlipH", mCurrentCameraType);
            //isFlipExpr 参数是用于对人像驱动道具的镜像
            FURenderer.fuItemSetParam(itemHandle, "isFlipExpr", mCurrentCameraType);
            //这两句代码用于识别人脸默认方向的修改，主要针对animoji道具的切换摄像头倒置问题
            FURenderer.fuItemSetParam(itemHandle, "camera_change", 1.0);
            FURenderer.fuSetDefaultRotationMode((360 - mInputImageOrientation) / 90);

            // 设置人转向的方向
            FURenderer.fuItemSetParam(itemHandle, "isFlipTrack", mCurrentCameraType);
            // 设置 Animoji 跟随人脸
            FURenderer.fuItemSetParam(itemHandle, "{\"thing\":\"<global>\",\"param\":\"follow\"}", 1);
        }
        if (isGESTURE) {
            //loc_y_flip与loc_x_flip 参数是用于对手势识别道具的镜像
            FURenderer.fuItemSetParam(itemHandle, "is3DFlipH", mCurrentCameraType);
            FURenderer.fuItemSetParam(itemHandle, "loc_y_flip", mCurrentCameraType);
            FURenderer.fuItemSetParam(itemHandle, "loc_x_flip", mCurrentCameraType);
            setRotMode(itemHandle, mInputImageOrientation, mCurrentCameraType == 0);
        }
        if (isMusic) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    FURenderer.setMusicTime(itemHandle, time);
                }
            });
            while (!mEventQueue.isEmpty()) {
                mEventQueue.remove(0).run();
            }
        }
    }

    private int mDefaultOrientation = 90;

    public void setTrackOrientation(final int rotation) {
        if (mDefaultOrientation != rotation) {
            mDefaultOrientation = rotation;
        }
    }

    public int getFrontCameraOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        int cameraId = 1;
        int numCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numCameras; i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                break;
            }
        }
        return getCameraOrientation(cameraId);
    }

    public int getCameraOrientation(int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        return info.orientation;
    }

    private void setRotMode(int item, int mInputImageOrientation, boolean isFront) {
        int mode;
        if (mInputImageOrientation == 270) {
            if (isFront) {
                mode = mDefaultOrientation / 90;
            } else {
                mode = (mDefaultOrientation - 180) / 90;
            }
        } else {
            if (isFront) {
                mode = (mDefaultOrientation + 180) / 90;
            } else {
                mode = (mDefaultOrientation) / 90;
            }
        }
        Log.d(TAG, "setTrackOrientation: rot:" + mDefaultOrientation + ", mode:" + mode);
//        faceunity.fuSetDefaultOrientation(mDefaultOrientation / 90);//设置识别人脸默认方向，能够提高首次识别的速度
        FURenderer.fuItemSetParam(item, "rotMode", mode);
    }


    /**
     * 类似GLSurfaceView的queueEvent机制
     */
    public void queueEvent(Runnable r) {
        mEventQueue.add(r);
    }

    /**
     * fuCreateItemFromPackage 加载道具
     *
     * @param bundle（Effect本demo定义的道具实体类）
     * @return 大于0时加载成功
     */
    private int loadItem(final Effect bundle) {
        int item = 0;
        if (mContext == null)
            return item;
        try {
            if (bundle.effectType() == Effect.EFFECT_TYPE_NONE) {
                item = 0;
            } else {
                InputStream is = mContext.getAssets().open(bundle.path());
                byte[] itemData = new byte[is.available()];
                int len = is.read(itemData);
                Log.e(TAG, bundle.path() + " len " + len);
                is.close();
                item = FURenderer.fuCreateItemFromPackage(itemData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return item;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void release() {
        mFuItemHandler.removeMessages(FUItemHandler.HANDLE_CREATE_ITEM);
        mFuItemHandler.removeMessages(ITEM_ARRAYS_ANIMOJI_FILTER);
        if (mFuItemHandlerThread != null) {
            mFuItemHandlerThread.quitSafely();
            mFuItemHandlerThread = null;
            mFuItemHandler = null;
        }
        if (mEventQueue != null) {
            mEventQueue.clear();
            mEventQueue = null;
        }
        Arrays.fill(mItemsArray, 0);

        mContext = null;
        FURenderer.relase();
    }
}
