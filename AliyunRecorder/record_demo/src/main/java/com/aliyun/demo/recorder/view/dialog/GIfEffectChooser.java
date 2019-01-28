package com.aliyun.demo.recorder.view.dialog;

import android.support.v4.app.Fragment;
import android.util.Log;

import com.aliyun.demo.recorder.view.effects.mv.AlivcMVChooseView;
import com.aliyun.demo.recorder.view.effects.mv.MvSelectListener;
import com.aliyun.demo.recorder.view.effects.otherfilter.AnimojiView;
import com.aliyun.demo.recorder.view.effects.otherfilter.BackgroundView;
import com.aliyun.demo.recorder.view.effects.otherfilter.DistortingMirrorAdapter;
import com.aliyun.demo.recorder.view.effects.otherfilter.DistortingMirrorView;
import com.aliyun.demo.recorder.view.effects.otherfilter.DongMLvjView;
import com.aliyun.demo.recorder.view.effects.otherfilter.Effect;
import com.aliyun.demo.recorder.view.effects.otherfilter.GestureView;
import com.aliyun.demo.recorder.view.effects.otherfilter.ThreeDStickerView;
import com.aliyun.demo.recorder.view.effects.paster.AlivcPasterChooseView;
import com.aliyun.demo.recorder.view.effects.paster.PasterSelectListener;
import com.aliyun.svideo.sdk.external.struct.form.IMVForm;
import com.aliyun.svideo.sdk.external.struct.form.PreviewPasterForm;

import java.util.ArrayList;
import java.util.List;

public class GIfEffectChooser extends BasePageChooser {
    private MvSelectListener mvSelectListener;
    private PasterSelectListener pasterSelectListener;
    private DistortingMirrorAdapter.OnItemListener onItemListener;
    private DistortingMirrorAdapter.OnItemListener animojiItemListener;
    private DistortingMirrorAdapter.OnItemListener threeDItemListener;
    private DistortingMirrorAdapter.OnItemListener dongMLvjItemListener;
    private DistortingMirrorAdapter.OnItemListener gestureItemListener;
    private DistortingMirrorAdapter.OnItemListener backgroundItemListener;

    private AlivcPasterChooseView pasterChooseView;
    private DistortingMirrorView distortingMirrorView;
    private AnimojiView animojiView;
    private ThreeDStickerView threeDSticker;
    private DongMLvjView lvjView;
    private GestureView gestureView;
    private BackgroundView backgroundView;

    @Override
    public List<Fragment> createPagerFragmentList() {
        List<Fragment> fragments = new ArrayList<>();
        AlivcMVChooseView mvChooseView = new AlivcMVChooseView();
        mvChooseView.setMvSelectListener(new MvSelectListener() {
            @Override
            public void onMvSelected(IMVForm imvForm) {
                Log.e("GIfEffectChooser", "onMvSelected");
                if (mvSelectListener != null) {
                    mvSelectListener.onMvSelected(imvForm);
                }
            }
        });
        pasterChooseView = new AlivcPasterChooseView();
        pasterChooseView.setPasterSelectListener(new PasterSelectListener() {
            @Override
            public void onPasterSelected(PreviewPasterForm imvForm) {
                if (pasterSelectListener != null) {
                    pasterSelectListener.onPasterSelected(imvForm);
                }
            }
        });
        distortingMirrorView = new DistortingMirrorView();
        distortingMirrorView.setOnItemListener(new DistortingMirrorAdapter.OnItemListener() {
            @Override
            public void onPosition(int position, Effect effect) {
                if (onItemListener != null) {
                    onItemListener.onPosition(position, effect);
                }
            }
        });

        animojiView = new AnimojiView();
        animojiView.setOnItemListener(new DistortingMirrorAdapter.OnItemListener() {
            @Override
            public void onPosition(int position, Effect effect) {
                if (animojiItemListener != null)
                    animojiItemListener.onPosition(position, effect);
            }
        });

        threeDSticker = new ThreeDStickerView();
        threeDSticker.setOnItemListener(new DistortingMirrorAdapter.OnItemListener() {
            @Override
            public void onPosition(int position, Effect effect) {
                if (threeDItemListener != null)
                    threeDItemListener.onPosition(position, effect);
            }
        });

        lvjView = new DongMLvjView();
        lvjView.setOnItemListener(new DistortingMirrorAdapter.OnItemListener() {
            @Override
            public void onPosition(int position, Effect effect) {
                if (dongMLvjItemListener != null)
                    dongMLvjItemListener.onPosition(position, effect);
            }
        });

        gestureView = new GestureView();
        gestureView.setOnItemListener(new DistortingMirrorAdapter.OnItemListener() {
            @Override
            public void onPosition(int position, Effect effect) {
                if (gestureItemListener != null)
                    gestureItemListener.onPosition(position, effect);
            }
        });

        backgroundView = new BackgroundView();
        backgroundView.setOnItemListener(new DistortingMirrorAdapter.OnItemListener() {
            @Override
            public void onPosition(int position, Effect effect) {
                if (backgroundItemListener != null)
                    backgroundItemListener.onPosition(position, effect);
            }
        });
        fragments.add(pasterChooseView);
        fragments.add(mvChooseView);
        fragments.add(threeDSticker);
        fragments.add(animojiView);
        fragments.add(distortingMirrorView);
        fragments.add(backgroundView);
        fragments.add(lvjView);
        fragments.add(gestureView);
        return fragments;
    }

    public void setMvSelectListener(MvSelectListener mvSelectListener) {
        this.mvSelectListener = mvSelectListener;
    }

    public void setPasterSelectListener(PasterSelectListener pasterSelectListener) {
        this.pasterSelectListener = pasterSelectListener;
    }

    public void setOnItemListener(DistortingMirrorAdapter.OnItemListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    public void setAnimojiItemListener(DistortingMirrorAdapter.OnItemListener animojiItemListener) {
        this.animojiItemListener = animojiItemListener;
    }

    public void setThreeDItemListener(DistortingMirrorAdapter.OnItemListener threeDItemListener) {
        this.threeDItemListener = threeDItemListener;
    }

    public void setDongMLvjItemListener(DistortingMirrorAdapter.OnItemListener dongMLvjItemListener) {
        this.dongMLvjItemListener = dongMLvjItemListener;
    }

    public void setGestureItemListener(DistortingMirrorAdapter.OnItemListener gestureItemListener) {
        this.gestureItemListener = gestureItemListener;
    }

    public void setBackgroundItemListener(DistortingMirrorAdapter.OnItemListener backgroundItemListener) {
        this.backgroundItemListener = backgroundItemListener;
    }

    public void clearBundle() {
        distortingMirrorView.clearBundle();
    }

    public void clearPaster() {
        pasterChooseView.clearPaster();
    }

    public void clearAnimoji() {
        animojiView.clearBundle();
    }

    public void clearThreeD() {
        threeDSticker.clearBundle();
    }

    public void clearDongMLvj() {
        lvjView.clearBundle();
    }

    public void clearBackGround() {
        backgroundView.clearBundle();
    }

    public void clearGesture() {
        gestureView.clearBundle();
    }
}
