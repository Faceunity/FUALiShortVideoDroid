package com.aliyun.svideo.editor.editor.thumblinebar;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.aliyun.svideo.editor.view.AlivcEditView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by cross on 2018/8/22.
 * <p>描述:适配了Overlay功能的thumbLineBar
 */
public class OverlayThumbLineBar extends ThumbLineBar {

    private static final String TAG = OverlayThumbLineBar.class.getName();

    /**
     * 缩略图覆盖view控制器的list，主要用于remove
     */
    private List<ThumbLineOverlay> mOverlayList = new ArrayList<>();

    public OverlayThumbLineBar(@NonNull Context context) {
        this(context, null);
    }

    public OverlayThumbLineBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OverlayThumbLineBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setup(ThumbLineConfig thumbLineConfig, OnBarSeekListener barSeekListener, AlivcEditView.PlayerListener linePlayer) {
        super.setup(thumbLineConfig, barSeekListener, linePlayer);
    }

    /**
     * 添加overlay
     *
     * @param overlayView overlayView
     * @param tailView    tailView
     * @param overlay     overlay
     */
    void addOverlayView(final View overlayView, final ThumbLineOverlayHandleView tailView, final ThumbLineOverlay overlay,final boolean isIvert) {

        addView(overlayView);
        final View view = tailView.getView();

        overlayView.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                if (isIvert) {
                    layoutParams.rightMargin = calculateTailViewInvertPosition(tailView);
                }else {
                    layoutParams.leftMargin = calculateTailViewPosition(tailView);
                }
                view.requestLayout();
                overlay.setVisibility(true);

            }
        });
    }

    public ThumbLineOverlay addOverlay(long startTime, long duration, ThumbLineOverlay.ThumbLineOverlayView view, long minDuration, boolean isInvert, UIEditorPage uiEditorPage) {

        return addOverlay(startTime, duration, view, minDuration, isInvert,uiEditorPage,null);
    }

    public ThumbLineOverlay addOverlay(long startTime, long duration, ThumbLineOverlay.ThumbLineOverlayView view, long minDuration, boolean isInvert, UIEditorPage uiEditorPage, ThumbLineOverlay.OnSelectedDurationChangeListener listener) {
        if (startTime < 0) {
            startTime = 0;
        }
        //设置类型tag到overlayView
        view.getContainer().setTag(uiEditorPage);

        if (mLinePlayer!=null) {
            //更新最新的duration
            mDuration = mLinePlayer.getDuration();
        }
        ThumbLineOverlay overlay = new ThumbLineOverlay(this, startTime, duration, view, mDuration, minDuration, isInvert,listener);
        //设置类型到ThumbLineOverlay
        overlay.setUIEditorPage(uiEditorPage);
        mOverlayList.add(overlay);
        return overlay;
    }

    /**
     * 实现和recyclerView的同步滑动
     * @param dx x的位移量
     * @param dy y的位移量
     */
    @Override
    protected void onRecyclerViewScroll(int dx, int dy) {
        super.onRecyclerViewScroll(dx, dy);
        int length = mOverlayList.size();
        for (int i = 0; i < length; i++) {
            mOverlayList.get(i).requestLayout();
        }

    }

    /**
     * 实现和recyclerView的同步滑动
     */
    @Override
    protected void onRecyclerViewScrollStateChanged(int newState) {
        super.onRecyclerViewScrollStateChanged(newState);

        switch (newState) {
            case RecyclerView.SCROLL_STATE_IDLE:
                for (ThumbLineOverlay overlay : mOverlayList) {
                    overlay.requestLayout();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 计算overlay尾部view左面的margin值
     * @param tailView view
     * @return int 单位sp
     */
    int calculateTailViewPosition(ThumbLineOverlayHandleView tailView) {
        if (tailView.getView() != null) {
            return (int) (mThumbLineConfig.getScreenWidth() / 2 - tailView.getView().getMeasuredWidth() + duration2Distance(tailView.getDuration()) - mCurrScroll);
        } else {
            return 0;
        }
    }

    /**
     * 计算在倒放时overlay尾部view右边的margin值
     * @param tailView view
     * @return 单位sp
     */
    int calculateTailViewInvertPosition(ThumbLineOverlayHandleView tailView) {
        if (tailView.getView() != null) {
            return (int) (mThumbLineConfig.getScreenWidth() / 2 - tailView.getView().getMeasuredWidth() - duration2Distance(tailView.getDuration()) + mCurrScroll);
        } else {
            return 0;
        }
    }

    /**
     * 时间转为尺寸
     *
     * @param duration 时长
     * @return 尺寸 pixel
     */
    int duration2Distance(long duration) {
        float lenth = getTimelineBarViewWidth()*duration * 1.0f / mDuration;
        return Math.round(lenth);
    }

    /**
     * 尺寸转为时间
     *
     * @param distance 尺寸 pixel
     * @return long duration
     */
    long distance2Duration(float distance) {
        float lenth = mDuration * distance / getTimelineBarViewWidth();
        return Math.round(lenth);
    }

    /**
     * 清除指定
     */
    public void removeOverlay(ThumbLineOverlay overlay) {
        if (overlay != null) {
            Log.d(TAG, "remove TimelineBar Overlay : " + overlay.getUIEditorPage());
            removeView(overlay.getOverlayView());
            mOverlayList.remove(overlay);
        }
    }

    /**
     * 清除指定by{@link UIEditorPage}
     */
    public void removeOverlayByPages(UIEditorPage ... uiEditorPages) {

        if (uiEditorPages == null || uiEditorPages.length == 0){
            return;
        }
        List<UIEditorPage> uiEditorPageList = Arrays.asList(uiEditorPages);
        //这里做合成（时间和转场特效会清空paster特效）恢复 针对缩略图的覆盖效果
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt.getTag() instanceof ThumbLineOverlay) {
                ThumbLineOverlay thumbLineOverlay = (ThumbLineOverlay)childAt.getTag();
                UIEditorPage uiEditorPage = thumbLineOverlay.getUIEditorPage();
                if (uiEditorPageList.contains(uiEditorPage)) {
                    removeOverlay(thumbLineOverlay);
                    i--;//这里用i--，有时间换成迭代器来删除
                }
            }
        }
    }

    /**
     * 显示指定的overlay
     * @param uiEditorPage UIEditorPage
     */
    public void showOverlay(UIEditorPage uiEditorPage){
        if (uiEditorPage == null){
            return;
        }
        boolean isCaption;
        isCaption = uiEditorPage == UIEditorPage.FONT || uiEditorPage == UIEditorPage.CAPTION;

        for (ThumbLineOverlay overlay : mOverlayList) {

            if (uiEditorPage == overlay.getUIEditorPage()){
                overlay.getOverlayView().setVisibility(VISIBLE);
            }else if ((isCaption && (overlay.getUIEditorPage() == UIEditorPage.CAPTION || overlay.getUIEditorPage() == UIEditorPage.FONT))){
                //如果是字幕，将字体和字幕全部显示
                overlay.getOverlayView().setVisibility(VISIBLE);
            }else {
                overlay.getOverlayView().setVisibility(INVISIBLE);
            }
        }
    }


    /**
     * 清除所有
     */
    public void clearOverlay() {
        for (ThumbLineOverlay overlay : mOverlayList) {
            removeView(overlay.getOverlayView());
        }
        mOverlayList.clear();
    }

}
