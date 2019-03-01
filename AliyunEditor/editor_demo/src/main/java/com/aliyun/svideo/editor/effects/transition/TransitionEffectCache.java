package com.aliyun.svideo.editor.effects.transition;

import android.util.SparseIntArray;

import com.aliyun.svideo.sdk.external.struct.AliyunIClipConstructor;

import java.util.HashSet;

/**
 * @author cross_ly
 * @date 2018/09/04 <p>描述:转场效果缓存类
 */
public class TransitionEffectCache {

    private SparseIntArray mOldTransitionCache;
    private SparseIntArray mTransitionCache;
    private HashSet<Integer> mCheckTool = new HashSet<>();
    private AliyunIClipConstructor mAliyunIClipConstructor;

    private TransitionEffectCache(AliyunIClipConstructor clipConstructor) {
        mAliyunIClipConstructor = clipConstructor;
    }

    public static TransitionEffectCache newInstance(AliyunIClipConstructor clipConstructor) {

        TransitionEffectCache effectCache = new TransitionEffectCache(clipConstructor);
        effectCache.mTransitionCache = new SparseIntArray(clipConstructor.getMediaPartCount());
        effectCache.mOldTransitionCache = new SparseIntArray(clipConstructor.getMediaPartCount());
        return effectCache;

    }

    public void put(int aliyunClipIndex, int effectPosition) {
        mTransitionCache.put(aliyunClipIndex,effectPosition);
        mCheckTool.add(aliyunClipIndex);

    }

    public int get(int aliyunClipIndex){
        return mTransitionCache.get(aliyunClipIndex);
    }

    public AliyunIClipConstructor getAliyunIClipConstructor() {
        return mAliyunIClipConstructor;
    }

    public int getCount() {
        return mAliyunIClipConstructor.getMediaPartCount();
    }

    public void editor() {
        mTransitionCache = mOldTransitionCache.clone();
    }

    /**
     * 提交
     */
    public void commitCache() {
        mOldTransitionCache = mTransitionCache.clone();
        mTransitionCache.clear();
        mCheckTool.clear();
    }

    /**
     * 撤销操作，获取有改变的操作
     * @return SparseIntArray key = clipIndex, value = transitionType
     */
    public SparseIntArray recover() {
        SparseIntArray sparseIntArray = new SparseIntArray();
        for (Integer index :mCheckTool){
            int type = mOldTransitionCache.get(index);
            if (mTransitionCache.get(index) != type) {
                sparseIntArray.put(index,type);
            }
        }
        return sparseIntArray;
    }
}
