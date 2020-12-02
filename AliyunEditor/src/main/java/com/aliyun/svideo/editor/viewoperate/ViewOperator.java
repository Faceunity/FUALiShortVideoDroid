package com.aliyun.svideo.editor.viewoperate;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.audiomix.MusicChooser;
import com.aliyun.svideo.editor.effects.control.BaseChooser;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.aliyun.svideo.base.widget.beauty.animation.AnimUitls;
import com.aliyun.svideo.common.utils.DensityUtils;

/**
 * @author zsy_18 data:2018/8/24
 */
public class ViewOperator {
    private static final String TAG = "ViewOperator";
    /**
     * 父布局
     */
    private RelativeLayout rootView;
    /**
     * 编辑界面缩小的比率
     */
    public static float SCALE_SIZE = 0.6f;
    private ViewGroup titleView;
    private View playerView;
    private View bottomMenuView;
    private View pasterContainerView;
    private View playerBtn;
    private BaseChooser bottomView;
    private int bottomViewHeight;
    private int playerWidth;
    private int playerHeight;
    private int rootViewHeight;
    private int confirmViewHeight;
    private int playBtnMarginBottom;
    private int playViewMarginTop;
    private int btnTranslationY;
    private int btnTranslationYMax = -1000;
    private int moveLenth;

    //int playerBtn
    public ViewOperator(RelativeLayout rootView, ViewGroup titleView, View playerView, View bottomMenuView,
                        View pasterContainerView, View playerBtn) {
        this.rootView = rootView;
        this.titleView = titleView;
        this.playerView = playerView;
        this.pasterContainerView = pasterContainerView;
        this.bottomMenuView = bottomMenuView;
        this.playerBtn = playerBtn;
    }

    public void showBottomView(BaseChooser bottomView) {
        if (this.bottomView != null) {
            return;
        }
        ViewGroup.LayoutParams lp = playerBtn.getLayoutParams();
        bottomMenuView.setVisibility(View.GONE);
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            playBtnMarginBottom = ((ViewGroup.MarginLayoutParams)lp).bottomMargin;
        } else {
            playBtnMarginBottom = rootView.getContext().getResources().getDimensionPixelSize(
                                      R.dimen.alivc_svideo_btn_player_margin_b);
        }
        lp = playerView.getLayoutParams();
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            playViewMarginTop = ((ViewGroup.MarginLayoutParams)lp).topMargin;
        } else {
            playViewMarginTop = 0;
        }
        rootViewHeight = rootView.getHeight();
        playerWidth = playerView.getWidth();
        playerHeight = playerView.getHeight();
        bottomViewHeight = bottomView.getCalculateHeight();
        confirmViewHeight = titleView.getHeight();
        moveLenth = Math.abs(DensityUtils.dip2px(rootView.getContext(), 10) - playViewMarginTop);
        //计算播放按钮移动距离
        btnTranslationY = playBtnMarginBottom - DensityUtils.dip2px(rootView.getContext(), 10) - bottomViewHeight;
        if (btnTranslationYMax < btnTranslationY && btnTranslationY < 0) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(playerBtn, "translationY", 0, btnTranslationY);
            animator.setDuration(250);
            animator.start();
        }

        if (this.bottomView == bottomView) {
            return;
        }

        if (bottomView.isShowSelectedView()) {
            AnimUitls.startDisappearAnimOnTop(titleView);
            int count = titleView.getChildCount();
            for (int i = 0; i < count; i++) {
                titleView.getChildAt(i).setClickable(false);
            }
        }
        if (bottomView.isPlayerNeedZoom()) {
            //计算缩放比例
            int h = rootViewHeight - bottomViewHeight - DensityUtils.dip2px(rootView.getContext(), 20);
            SCALE_SIZE = (float)h / playerHeight;
            if (SCALE_SIZE >= 0.95f) {
                SCALE_SIZE = 0.95f;
            }
            //  2018/8/24 缩放播放UI
            ValueAnimator anim = ValueAnimator.ofFloat(1f, SCALE_SIZE);
            anim.setDuration(250);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float currentValue = (float)animation.getAnimatedValue();
                    ViewGroup.LayoutParams params = playerView.getLayoutParams();
                    params.height = (int)(playerHeight * currentValue);
                    params.width = (int)(playerWidth * currentValue);
                    ViewGroup.MarginLayoutParams marginParams = null;
                    //获取view的margin设置参数
                    if (params instanceof ViewGroup.MarginLayoutParams) {
                        marginParams = (ViewGroup.MarginLayoutParams)params;
                    } else {
                        //不存在时创建一个新的参数
                        //基于View本身原有的布局参数对象
                        marginParams = new ViewGroup.MarginLayoutParams(params);

                    }
                    long currentTime = animation.getCurrentPlayTime();

                    int marginTop = (int)Math.abs(
                                        playViewMarginTop - moveLenth * (1 - currentValue) / (1 - SCALE_SIZE));

                    marginParams.setMargins(0, marginTop, 0, 0);
                    playerView.setLayoutParams(marginParams);
                    pasterContainerView.setLayoutParams(marginParams);
                    if (currentValue == SCALE_SIZE && animatorListener != null) {
                        animatorListener.onShowAnimationEnd();
                    }
                }
            });
            anim.start();
        }
        RelativeLayout.LayoutParams layoutParams;
        if (bottomView instanceof MusicChooser) {

            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rootView.addView(bottomView, layoutParams);
        AnimUitls.startAppearAnimY(bottomView);
        this.bottomView = bottomView;

    }

    public void hideBottomView() {
        if (bottomView == null) {
            return;
        }

        AnimUitls.startAppearAnimY(bottomMenuView);
        bottomMenuView.setVisibility(View.VISIBLE);
        if (bottomView.isShowSelectedView()) {
            AnimUitls.startAppearAnimOnTop(titleView);
            titleView.setVisibility(View.VISIBLE);
            int count = titleView.getChildCount();
            for (int i = 0; i < count; i++) {
                titleView.getChildAt(i).setClickable(true);
            }
        }
        if (btnTranslationYMax < btnTranslationY && btnTranslationY < 0) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(playerBtn, "translationY", btnTranslationY, 0);
            animator.setDuration(250);
            animator.start();
        }
        if (bottomView.isPlayerNeedZoom()) {
            //  2018/8/24 缩放播放UI
            ValueAnimator anim = ValueAnimator.ofFloat(SCALE_SIZE, 1f);
            anim.setDuration(250);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    long currentTime = animation.getCurrentPlayTime();
                    float currentValue = (float)animation.getAnimatedValue();
                    ViewGroup.LayoutParams params = playerView.getLayoutParams();
                    params.height = (int)(playerHeight * currentValue);
                    params.width = (int)(playerWidth * currentValue);
                    ViewGroup.MarginLayoutParams marginParams = null;
                    //获取view的margin设置参数
                    if (params instanceof ViewGroup.MarginLayoutParams) {
                        marginParams = (ViewGroup.MarginLayoutParams)params;
                    } else {
                        //不存在时创建一个新的参数
                        //基于View本身原有的布局参数对象
                        marginParams = new ViewGroup.MarginLayoutParams(params);

                    }

                    int marginTop = (int)Math.abs(playViewMarginTop - moveLenth * (1 - currentValue) / (1
                                                  - SCALE_SIZE)); //(int)Math.abs(playViewMarginTop-moveLenth*(250-currentTime)/250);
                    marginParams.setMargins(0, marginTop, 0, 0);
                    playerView.setLayoutParams(marginParams);
                    pasterContainerView.setLayoutParams(marginParams);
                    if (currentValue == 1 && animatorListener != null) {
                        animatorListener.onHideAnimationEnd();
                    }

                }
            });
            anim.start();
        }
        AnimUitls.startDisappearAnimY(bottomView);
        bottomView.removeOwn();
        bottomView = null;
    }

    /**
     * 点击空白区域或返回按钮是, 需要隐藏的弹窗
     *
     * @param page index
     */
    public void hideBottomEditorView(UIEditorPage page) {
        switch (page) {
        case FILTER:
        case SOUND:
        case MV:
            hideBottomView();
            break;
        default:
            break;
        }
    }

    AnimatorListener animatorListener;

    public interface AnimatorListener {
        /**
         * 动画结束监听
         */
        void onShowAnimationEnd();

        void onHideAnimationEnd();
    }

    public void setAnimatorListener(AnimatorListener animatorListener) {
        this.animatorListener = animatorListener;
    }

    public boolean isBottomViewShow() {
        return bottomView != null;
    }

    public BaseChooser getBottomView() {
        return bottomView;
    }
}
