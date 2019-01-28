#  FUAliyunShortVideoDemo（android）
## 概述

FUAliyunShortVideoDemo 是集成了 Faceunity 的2d/3d贴纸、animoji、哈哈镜、背景分割、动漫滤镜和手势识别和阿里云短视频（专业版） SDK 的 Demo 。 本文是 FaceUnity SDK 快速对接**[阿里云短视频](https://help.aliyun.com/document_detail/94329.html?spm=a2c4g.11186623.6.778.76b454bf0a4gyb)** SDK 的导读说明，关于 FaceUnity SDK 的更多详细说明，请参看 [FULiveDemo](https://github.com/Faceunity/FULiveDemoDroid/tree/dev).

## 快速集成
### 在点击3d贴纸等道具时，使用Faceunity创建对应的道具，示例代码如下：
```
public void createItem(Effect item) {
    if (item == null) return;
    mFuItemHandler.removeMessages(FUItemHandler.HANDLE_CREATE_ITEM);
    mFuItemHandler.sendMessage(Message.obtain(mFuItemHandler, FUItemHandler.HANDLE_CREATE_ITEM, item));
}
```
```
final Effect effect = (Effect) msg.obj;
final int newEffectItem = loadItem(effect);
queueEvent(new Runnable() {
    @Override
    public void run() {
        if (mItemsArray[ITEM_ARRAYS_EFFECT] > 0) {
            faceunity.fuDestroyItem(mItemsArray[ITEM_ARRAYS_EFFECT]);
        }
        mItemsArray[ITEM_ARRAYS_EFFECT] = newEffectItem;
    }
});
//queueEvent的Runnable在此处被调用
while (!mEventQueue.isEmpty()) {
    mEventQueue.remove(0).run();
}
```
### 动漫滤镜的使用与其他道具有些区别，其示例代码如下：
```
//动漫滤镜
public void onLoadAnimFilter(final boolean enable, Effect effect) {
    if (isOpenAnimoji == enable) {
        return;
    }
    isOpenAnimoji = enable;
    mFuItemHandler.removeMessages(ITEM_ARRAYS_ANIMOJI_FILTER);
    mFuItemHandler.sendMessage(Message.obtain(mFuItemHandler, ITEM_ARRAYS_ANIMOJI_FILTER, effect));
}
```
```
final Effect item = (Effect) msg.obj;
if (isOpenAnimoji) {
    if (mItemsArray[ITEM_ARRAYS_ANIMOJI_FILTER] <= 0) {
        mItemsArray[ITEM_ARRAYS_ANIMOJI_FILTER] = loadItem(item);
    }
    queueEvent(new Runnable() {
        @Override
        public void run() {
            faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_EFFECT], "{\"thing\":\"<global>\",\"param\":\"follow\"}", 1);
//          int supportGLVersion = GlUtil.getSupportGLVersion(mContext);
//          faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_ANIMOJI_FILTER], "glVer", supportGLVersion);
        }
    });
} else {
    if (mItemsArray[ITEM_ARRAYS_ANIMOJI_FILTER] > 0) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                faceunity.fuDestroyItem(mItemsArray[ITEM_ARRAYS_ANIMOJI_FILTER]);
                faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_EFFECT], "{\"thing\":\"<global>\",\"param\":\"follow\"}", 0);
                mItemsArray[ITEM_ARRAYS_ANIMOJI_FILTER] = 0;
            }
        });
    }
}
//queueEvent的Runnable在此处被调用
while (!mEventQueue.isEmpty()) {
    mEventQueue.remove(0).run();
}
```

### 具体代码参考AliyunRecorder中view下的otherfilter文件、AliyunSVideoRecordView和FaceUnityManager中的代码

阿里云短视频 SDK： https://help.aliyun.com/document_detail/51992.html?spm=a2c4g.11186623.6.746.4f105e5eCtOtbS

阿里短视频产品：https://promotion.aliyun.com/ntms/act/shortvideo.html?spm=5176.10695662.777961.1.7bf9260dQz2qnP