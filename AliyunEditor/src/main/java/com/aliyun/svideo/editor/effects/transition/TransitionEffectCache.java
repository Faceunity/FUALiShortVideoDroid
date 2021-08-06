package com.aliyun.svideo.editor.effects.transition;

import android.util.Log;

import com.aliyun.svideo.editor.effects.control.EffectInfo;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.aliyun.svideosdk.common.AliyunIClipConstructor;
import com.aliyun.svideosdk.common.struct.effect.TransitionBase;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cross_ly
 * @date 2018/09/04 <p>描述:转场效果缓存类
 */
public class TransitionEffectCache {

    private static final String TAG = "TransitionEffectCache";
    private LinkedHashMap<Integer, EffectInfo> mOldTransitionCache;
    private LinkedHashMap<Integer, EffectInfo> mTransitionCache;
    private HashSet<Integer> mCheckTool = new HashSet<>();
    private AliyunIClipConstructor mAliyunIClipConstructor;

    private TransitionEffectCache(AliyunIClipConstructor clipConstructor) {
        mAliyunIClipConstructor = clipConstructor;
    }

    public static TransitionEffectCache newInstance(AliyunIClipConstructor clipConstructor) {

        TransitionEffectCache effectCache = new TransitionEffectCache(clipConstructor);
        effectCache.mTransitionCache = new LinkedHashMap<>(clipConstructor.getMediaPartCount());
        effectCache.mOldTransitionCache = new LinkedHashMap<>(clipConstructor.getMediaPartCount());
        return effectCache;

    }

    public void put(int aliyunClipIndex, EffectInfo effectInfo) {
        mTransitionCache.put(aliyunClipIndex, effectInfo);
        mCheckTool.add(aliyunClipIndex);

    }

    public EffectInfo get(int aliyunClipIndex) {
        return mTransitionCache.get(aliyunClipIndex);
    }

    public AliyunIClipConstructor getAliyunIClipConstructor() {
        return mAliyunIClipConstructor;
    }

    public int getCount() {
        return mAliyunIClipConstructor.getMediaPartCount();
    }

    public void editor() {
        if (mTransitionCache != null && mTransitionCache.size() > 0) {
            Log.w(TAG, "TransitionCache editing is not empty");
            return;
        }
        mTransitionCache = new LinkedHashMap<>(mOldTransitionCache);

    }

    /**
     * 提交
     */
    public void commitCache() {
        mOldTransitionCache = new LinkedHashMap<>(mTransitionCache);
        mTransitionCache.clear();
        mCheckTool.clear();
    }

    /**
     * 检查缓存的转场特效资源是否有被删除，如果被删除则清楚对应的缓存
     * @return 被删除的资源
     */
    public List<EffectInfo> checkTransitionCacheIsDelete() {
        List<EffectInfo> list = new ArrayList<>();

        if (mTransitionCache.size() != 0) {
            Iterator<Map.Entry<Integer, EffectInfo>> iterator = mTransitionCache.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, EffectInfo> next = iterator.next();
                EffectInfo effectInfo = next.getValue();
                if (effectInfo != null && effectInfo.getPath() != null
                        && !new File(effectInfo.getPath()).exists()) {
                    iterator.remove();
                    Log.e(TAG, "removeTransition mTransitionCache path :" + effectInfo.getPath());
                    effectInfo.setPath(null);
                    effectInfo.transitionBase = new TransitionBase();
                    list.add(effectInfo);
                    mCheckTool.remove(next.getKey());
                }
            }
        }

        if (mOldTransitionCache.size() != 0) {
            Iterator<Map.Entry<Integer, EffectInfo>> iterator = mOldTransitionCache.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, EffectInfo> next = iterator.next();
                EffectInfo effectInfo = next.getValue();
                if (effectInfo != null && effectInfo.getPath() != null
                        && !new File(effectInfo.getPath()).exists()) {
                    iterator.remove();
                    Log.e(TAG, "removeTransition mOldTransitionCache path :" + effectInfo.getPath());
                    effectInfo.setPath(null);
                    effectInfo.transitionBase = new TransitionBase();
                    list.add(effectInfo);
                }
            }
        }
        return list;
    }

    /**
     * 撤销操作，获取有改变的操作
     * @return SparseIntArray key = clipIndex, value = transitionType
     */
    public LinkedHashMap<Integer, EffectInfo> recover() {
        LinkedHashMap<Integer, EffectInfo> hashMap = new LinkedHashMap<>();
        for (Integer index : mCheckTool) {
            EffectInfo effectInfo = mOldTransitionCache.get(index);
            EffectInfo effectCache = mTransitionCache.get(index);
            if (effectInfo == null) {
                effectInfo = new EffectInfo();
                effectInfo.clipIndex = effectCache.clipIndex;
                effectInfo.transitionType = TransitionChooserView.EFFECT_NONE;
                effectInfo.type = UIEditorPage.TRANSITION;
            }
            if (!effectInfo.equals(effectCache)) {
                hashMap.put(index, effectInfo);
            }
        }
        mTransitionCache.clear();
        mCheckTool.clear();
        return hashMap;
    }
}
