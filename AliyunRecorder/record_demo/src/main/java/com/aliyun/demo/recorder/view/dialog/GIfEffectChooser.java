package com.aliyun.demo.recorder.view.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.aliyun.demo.R;
import com.aliyun.demo.recorder.view.effects.otherfilter.ARView;
import com.aliyun.demo.recorder.view.effects.otherfilter.AnimojiView;
import com.aliyun.demo.recorder.view.effects.otherfilter.BackgroundView;
import com.aliyun.demo.recorder.view.effects.otherfilter.DistortingMirrorAdapter;
import com.aliyun.demo.recorder.view.effects.otherfilter.DistortingMirrorView;
import com.aliyun.demo.recorder.view.effects.otherfilter.DongMLvjView;
import com.aliyun.demo.recorder.view.effects.otherfilter.Effect;
import com.aliyun.demo.recorder.view.effects.otherfilter.ExpressionView;
import com.aliyun.demo.recorder.view.effects.otherfilter.GestureView;
import com.aliyun.demo.recorder.view.effects.otherfilter.MusicView;
import com.aliyun.demo.recorder.view.effects.otherfilter.ThreeDStickerView;
import com.aliyun.demo.recorder.view.effects.paster.AlivcPasterChooseView;
import com.aliyun.demo.recorder.view.effects.paster.PasterSelectListener;
import com.aliyun.svideo.sdk.external.struct.form.PreviewPasterForm;
import com.faceunity.OnFUControlListener;

import java.util.ArrayList;
import java.util.List;

public class GIfEffectChooser extends BasePageChooser {
    private PasterSelectListener pasterSelectListener;

    private DistortingMirrorAdapter.OnItemListener onItemListener;
    private DistortingMirrorAdapter.OnItemListener animojiItemListener;
    private DistortingMirrorAdapter.OnItemListener threeDItemListener;
    private DistortingMirrorAdapter.OnItemListener dongMLvjItemListener;
    private DistortingMirrorAdapter.OnItemListener gestureItemListener;
    private DistortingMirrorAdapter.OnItemListener backgroundItemListener;
    private DistortingMirrorAdapter.OnItemListener musicItemListener;
    private DistortingMirrorAdapter.OnMusicListener musicTimeListener;
    private DistortingMirrorAdapter.OnItemListener expressionItemListener;
    private DistortingMirrorAdapter.OnItemListener arItemListener;
    private DistortingMirrorAdapter.OnDescriptionChangeListener expressionDesListener;

    private AlivcPasterChooseView pasterChooseView;
    private DistortingMirrorView distortingMirrorView;
    private AnimojiView animojiView;
    private ThreeDStickerView threeDSticker;
    private DongMLvjView lvjView;
    private GestureView gestureView;
    private BackgroundView backgroundView;
    private MusicView musicView;
    private ExpressionView expressionView;
    private ARView arView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //适配有底部导航栏的手机，在full的style下会盖住部分视图的bug
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.QUDemoFullFitStyle);
    }

    @Override
    public List<Fragment> createPagerFragmentList() {
        List<Fragment> fragments = new ArrayList<>();

        pasterChooseView = new AlivcPasterChooseView();
        pasterChooseView.setPasterSelectListener(new PasterSelectListener() {
            @Override
            public void onPasterSelected(PreviewPasterForm pasterForm) {
                if (pasterSelectListener != null) {
                    pasterSelectListener.onPasterSelected(pasterForm);
                }
            }

            @Override
            public void onSelectPasterDownloadFinish(String path) {
                if (pasterSelectListener != null) {
                    pasterSelectListener.onSelectPasterDownloadFinish(path);
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

        musicView = new MusicView();
        musicView.setOnItemListener(new DistortingMirrorAdapter.OnItemListener() {
            @Override
            public void onPosition(int position, Effect effect) {
                if (musicItemListener != null)
                    musicItemListener.onPosition(position, effect);
            }
        });
        musicView.setOnMusicListener(new DistortingMirrorAdapter.OnMusicListener() {
            @Override
            public void onMusic(long time) {
                if (musicTimeListener != null) {
                    musicTimeListener.onMusic(time);
                }
            }
        });

        expressionView = new ExpressionView();
        expressionView.setOnItemListener(new DistortingMirrorAdapter.OnItemListener() {
            @Override
            public void onPosition(int position, Effect effect) {
                if (expressionItemListener != null)
                    expressionItemListener.onPosition(position, effect);
            }
        });
        expressionView.setOnDescriptionChangeListener(new DistortingMirrorAdapter.OnDescriptionChangeListener() {
            @Override
            public void onDescriptionChangeListener(int description) {
                if (expressionDesListener != null)
                    expressionDesListener.onDescriptionChangeListener(description);
            }
        });

        arView = new ARView();
        arView.setOnItemListener(new DistortingMirrorAdapter.OnItemListener() {
            @Override
            public void onPosition(int position, Effect effect) {
                if (arItemListener != null)
                    arItemListener.onPosition(position, effect);
            }
        });

        fragments.add(pasterChooseView);
        fragments.add(threeDSticker);
        fragments.add(animojiView);
        fragments.add(distortingMirrorView);
        fragments.add(backgroundView);
        fragments.add(lvjView);
        fragments.add(gestureView);
        fragments.add(musicView);
        fragments.add(expressionView);
        fragments.add(arView);
        return fragments;
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

    public void setMusicItemListener(DistortingMirrorAdapter.OnItemListener musicItemListener) {
        this.musicItemListener = musicItemListener;
    }

    public void setMusicTimeListener(DistortingMirrorAdapter.OnMusicListener musicTimeListener) {
        this.musicTimeListener = musicTimeListener;
    }

    public void setExpressionItemListener(DistortingMirrorAdapter.OnItemListener expressionItemListener) {
        this.expressionItemListener = expressionItemListener;
    }

    public void setExpressionDesListener(DistortingMirrorAdapter.OnDescriptionChangeListener listener) {
        this.expressionDesListener = listener;
    }

    public void setArItemListener(DistortingMirrorAdapter.OnItemListener arItemListener) {
        this.arItemListener = arItemListener;
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

    public void clearMusic() {
        musicView.clearBundle();
    }

    public void clearExpression() {
        expressionView.clearBundle();
    }

    public void clearAr() {
        arView.clearBundle();
    }

}
