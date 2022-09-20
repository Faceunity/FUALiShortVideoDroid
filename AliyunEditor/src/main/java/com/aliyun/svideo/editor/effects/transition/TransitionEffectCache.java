package com.aliyun.svideo.editor.effects.transition;

import android.util.Log;

import com.aliyun.svideo.editor.effects.control.EffectInfo;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.aliyun.svideosdk.common.struct.common.AliyunClip;
import com.aliyun.svideosdk.common.struct.effect.TransitionBase;
import com.aliyun.svideosdk.common.struct.effect.TransitionTranslate;
import com.aliyun.svideosdk.editor.AliyunISourcePartManager;

import java.io.File;
import java.util.ArrayList;
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
    private AliyunISourcePartManager mAliyunSourcePartManager;

    private TransitionEffectCache(AliyunISourcePartManager clipConstructor) {
        mAliyunSourcePartManager = clipConstructor;
    }

    public static TransitionEffectCache newInstance(AliyunISourcePartManager clipConstructor) {
        TransitionEffectCache effectCache = new TransitionEffectCache(clipConstructor);
        effectCache.mTransitionCache = new LinkedHashMap<>(clipConstructor.getMediaPartCount());
        effectCache.mOldTransitionCache = new LinkedHashMap<>(clipConstructor.getMediaPartCount());
        //草稿转场状态恢复
        List<AliyunClip> list = clipConstructor.getAllClips();
        if (list.size() > 0) {
            for (int i = 1; i < list.size(); i++) {
                AliyunClip clip = list.get(i);
                TransitionBase transition = clip.getTransition();
                if (transition != null) {
                    EffectInfo effectInfo = new EffectInfo();
                    effectInfo.clipIndex = i - 1;
                    effectInfo.transitionType = transition.mType;
                    switch (transition.mType) {
                    case TransitionBase.TRANSITION_TYPE_SHUTTER:
                        effectInfo.transitionType = TransitionChooserView.EFFECT_SHUTTER;
                        break;
                    case TransitionBase.TRANSITION_TYPE_TRANSLATE:
                        TransitionTranslate TransitionTranslate = ((TransitionTranslate) transition);
                        if (TransitionTranslate.getDirection() == TransitionBase.DIRECTION_LEFT) {
                            effectInfo.transitionType = TransitionChooserView.EFFECT_LEFT;
                        } else if (TransitionTranslate.getDirection() == TransitionBase.DIRECTION_RIGHT) {
                            effectInfo.transitionType = TransitionChooserView.EFFECT_RIGHT;
                        } else if (TransitionTranslate.getDirection() == TransitionBase.DIRECTION_UP) {
                            effectInfo.transitionType = TransitionChooserView.EFFECT_UP;
                        } else if (TransitionTranslate.getDirection() == TransitionBase.DIRECTION_DOWN) {
                            effectInfo.transitionType = TransitionChooserView.EFFECT_DOWN;
                        }
                        break;
                    case TransitionBase.TRANSITION_TYPE_CIRCLE:
                        effectInfo.transitionType = TransitionChooserView.EFFECT_CIRCLE;
                        break;
                    case TransitionBase.TRANSITION_TYPE_FIVEPOINTSTAR:
                        effectInfo.transitionType = TransitionChooserView.EFFECT_FIVE_STAR;
                        break;
                    case TransitionBase.TRANSITION_TYPE_FADE:
                        effectInfo.transitionType = TransitionChooserView.EFFECT_FADE;
                        break;
                    case TransitionBase.TRANSITION_TYPE_CUSTOM:
                        effectInfo.transitionType = TransitionChooserView.EFFECT_CUSTOM;
                        effectInfo.setSource(transition.getCustomSource());
                        break;
                    default:
                        effectInfo.transitionType = TransitionChooserView.EFFECT_NONE;
                    }
                    effectInfo.type = UIEditorPage.TRANSITION;
                    effectInfo.transitionBase = transition;
                    //转场保存在后一位
                    effectCache.mOldTransitionCache.put(i - 1, effectInfo);
                    effectCache.mTransitionCache.put(i - 1, effectInfo);
                }
            }
        }
        return effectCache;

    }

    public void put(int aliyunClipIndex, EffectInfo effectInfo) {
        mTransitionCache.put(aliyunClipIndex, effectInfo);
        mCheckTool.add(aliyunClipIndex);

    }

    public EffectInfo get(int aliyunClipIndex) {
        return mTransitionCache.get(aliyunClipIndex);
    }

    public AliyunISourcePartManager getAliyunSourcePartManager() {
        return mAliyunSourcePartManager;
    }

    public int getCount() {
        return mAliyunSourcePartManager.getMediaPartCount();
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
                if (effectInfo != null && effectInfo.getSource() != null && effectInfo.getSource().getPath() != null
                        && !new File(effectInfo.getSource().getPath()).exists()) {
                    iterator.remove();
                    Log.e(TAG, "removeTransition mTransitionCache path :" + effectInfo.getSource().getPath());
                    effectInfo.setPath(null);
                    effectInfo.setSource(null);
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
                if (effectInfo != null && effectInfo.getSource() != null && effectInfo.getSource().getPath() != null
                        && !new File(effectInfo.getSource().getPath()).exists()) {
                    iterator.remove();
                    Log.e(TAG, "removeTransition mOldTransitionCache path :" + effectInfo.getSource().getPath());
                    effectInfo.setPath(null);
                    effectInfo.setSource(null);
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
