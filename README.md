SDK 版本为 **7.4.1.0**。关于 SDK 的详细说明，请参看 **[FULiveDemoDroid](https://github.com/Faceunity/FULiveDemoDroid/tree/master/doc)**。

--------

## 快速集成方法

### 一、添加 SDK

### 1. build.gradle配置

#### 1.1 allprojects配置
```java
allprojects {
    repositories {
        ...
        maven { url 'http://maven.faceunity.com/repository/maven-public/' }
        ...
  }
}
```

#### 1.2 dependencies导入依赖
```java
dependencies {
...
implementation 'com.faceunity:core:7.4.1.0' // 实现代码
implementation 'com.faceunity:model:7.4.1.0' // 道具以及AI bundle
...
}
```

##### 备注

集成参考文档：FULiveDemoDroid 工程 doc目录

### 2. 其他接入方式-底层库依赖

```java
dependencies {
...
implementation 'com.faceunity:nama:7.4.1.0' //底层库-标准版
implementation 'com.faceunity:nama-lite:7.4.1.0' //底层库-lite版
...
}
```

如需指定应用的 so 架构，请修改 app 模块 build.gradle：

```groovy
android {
    // ...
    defaultConfig {
        // ...
        ndk {
            abiFilters 'armeabi-v7a', 'arm64-v8a'
        }
    }
}
```

如需剔除不必要的 assets 文件，请修改 app 模块 build.gradle：

```groovy
android {
    // ...
    applicationVariants.all { variant ->
        variant.mergeAssetsProvider.configure {
            doLast {
                delete(fileTree(dir: outputDir, includes: ['model/ai_face_processor_lite.bundle',
                                                           'model/ai_hand_processor.bundle',
                                                           'graphics/controller.bundle',
                                                           'graphics/fuzzytoonfilter.bundle',
                                                           'graphics/fxaa.bundle',
                                                           'graphics/tongue.bundle']))
            }
        }
    }
}
```


### 二、使用 SDK

#### 1. 初始化

调用 `FURenderer` 类的  `setup` 方法初始化 SDK，可以在工作线程调用，应用启动后仅需调用一次。

在 AliyunSVideoRecordView 中进行初始化，根据是否需要开启美颜来决定是否调用方法。

#### 2.创建

调用 `FURenderer` 类的  `prepareRenderer` 方法在 SDK 使用前加载必要的资源。

在 AliyunSVideoRecordView 类中，实现OnTextureIdCallBack接口，在onTextureIdBack回调方法中执行FURenderer.prepareRenderer();

```
recorder.setOnTextureIdCallback(new OnTextureIdCallBack() {
    private boolean mIsFirstFrame = true;

    @Override
    public int onTextureIdBack(int textureId, int textureWidth, int textureHeight, float[] matrix) {
        if (!mIsFuBeautyOpen) {
            return textureId;
        }
        if (mIsFirstFrame) {
            mIsFirstFrame = false;
            Log.d(TAG, "onTextureDestroyed thread:" + Thread.currentThread().getName()
                    + ", texId:" + textureId + ", width:" + textureWidth + ", height:" + textureHeight);
            mFURenderer.prepareRenderer(mFURendererListener);
        }
        int texId = 0;
        if (currentBeautyFaceMode == BeautyMode.Advanced) {
            texId = mFURenderer.onDrawFrameDualInput(frameBytes, textureId, textureWidth, textureHeight);
            if (mSkippedFrames > 0) {
                mSkippedFrames--;
                texId = textureId;
            }
        }
        if (texId <= 0) {
            texId = textureId;
        }
        return texId;
    }

    @Override
    public int onScaledIdBack(int scaledId, int textureWidth, int textureHeight, float[] matrix) {
        return scaledId;
    }

    @Override
    public void onTextureDestroyed() {
        // sdk3.7.8改动, 自定义渲染（第三方渲染）销毁gl资源，以前GLSurfaceView时可以通过GLSurfaceView.queueEvent来做，
        // 现在增加了一个gl资源销毁的回调，需要统一在这里面做。
        if (mFURenderer != null) {
        	mFURenderer.release();
        }
    }
});
```

#### 3. 图像处理

调用 `FURenderer` 类的  `onDrawFrameXXX` 方法进行图像处理，有许多重载方法适用于不同数据类型的需求。

在 AliyunSVideoRecordView 类中，实现OnTextureIdCallBack接口，在onTextureIdBack回调方法中执行美颜操作（代码见上一小节）。

onDrawFrameSingleInput 是单输入，输入图像buffer数组或者纹理Id，输出纹理Id
onDrawFrameDualInput 双输入，输入图像buffer数组与纹理Id，输出纹理Id。性能上，双输入优于单输入

在onDrawFrameSingleInput 与onDrawFrameDualInput 方法内，在执行底层方法之前，都会执行prepareDrawFrame()方法(执行各个特效模块的任务，将美颜参数传给底层)。

阿里云短视频支持单输入： onDrawFrameSingleInput 

#### 4. 销毁

调用 `FURenderer` 类的  `release` 方法在 SDK 结束前释放占用的资源。

在 AliyunSVideoRecordView 类中，实现OnTextureIdCallBack接口，在onTextureDestroyed回调方法中执行FURenderer.release();

#### 5. 切换相机

在 AliyunSVideoRecordView 类，initControlView()方法下的ControlViewListener接口中onCameraSwitch方法实现,
切换相机之后需要重新设置 sdk 参数

#### 6. 旋转手机

调用 `FURenderer` 类 的  `setDeviceOrientation` 方法，用于重新为 SDK 设置参数。

使用方法：AliyunSVideoRecordView 中可见

```java
1.implements SensorEventListener
2. onCreate()    
mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

3.
protected void destroyRecorder() {
    super.onDestroy();
    // 清理相关资源
    if (mSensorManager != null) {
        mSensorManager.unregisterListener(this);
    }
}
4. 
//实现接口
@Override
public void onSensorChanged(SensorEvent event) {
    //具体代码见 AliyunSVideoRecordView 类
}

```

**注意：** 上面一系列方法的使用，可以前往对应类查看，参考该代码示例接入即可。

### 三、接口介绍

- IFURenderer 是核心接口，提供了创建、销毁、渲染等接口。
- FaceUnityDataFactory 控制四个功能模块，用于功能模块的切换，初始化
- FaceBeautyDataFactory 是美颜业务工厂，用于调整美颜参数。
- PropDataFactory 是道具业务工厂，用于加载贴纸效果。
- MakeupDataFactory 是美妆业务工厂，用于加载美妆效果。
- BodyBeautyDataFactory 是美体业务工厂，用于调整美体参数。

关于 SDK 的更多详细说明，请参看 **[FULiveDemoDroid](https://github.com/Faceunity/FULiveDemoDroid/)**。如有对接问题，请联系技术支持。