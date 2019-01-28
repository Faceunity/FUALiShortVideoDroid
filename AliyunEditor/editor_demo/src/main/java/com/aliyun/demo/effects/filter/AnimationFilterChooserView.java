package com.aliyun.demo.effects.filter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.aliyun.demo.editor.R;
import com.aliyun.demo.effects.control.BaseChooser;
import com.aliyun.demo.effects.control.EffectInfo;
import com.aliyun.demo.effects.control.OnItemClickListener;
import com.aliyun.demo.effects.control.OnItemTouchListener;
import com.aliyun.demo.effects.control.SpaceItemDecoration;
import com.aliyun.demo.effects.control.UIEditorPage;
import com.aliyun.demo.msg.Dispatcher;
import com.aliyun.demo.msg.body.ClearAnimationFilter;
import com.aliyun.demo.msg.body.ConfirmAnimationFilter;
import com.aliyun.demo.msg.body.DeleteLastAnimationFilter;
import com.aliyun.demo.msg.body.FilterTabClick;
import com.aliyun.demo.msg.body.LongClickAnimationFilter;
import com.aliyun.demo.msg.body.LongClickUpAnimationFilter;
import com.aliyun.demo.util.Common;
import com.aliyun.svideo.base.utils.DensityUtil;

public class AnimationFilterChooserView extends BaseChooser
        implements OnItemClickListener, OnItemTouchListener,View.OnClickListener {
    private RecyclerView mListView;
    private FrameLayout mFlThumblinebar;
    private EffectAdapter mFilterAdapter;
    private ImageView mCancel;
    private TextView mTvEffectTitle;
    private ImageView mIvEffectIcon;
    private ImageView mComplete;
    private boolean isFirstShow;
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Dispatcher.getInstance().postMsg(new FilterTabClick(FilterTabClick.POSITION_ANIMATION_FILTER));

        if (isFirstShow){
            View contentView=LayoutInflater.from(getContext()).inflate(R.layout.alivc_svideo_tip, null, false);
            PopupWindow window=new PopupWindow( contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            window.setContentView(contentView);
            window.setOutsideTouchable(true);
            // 设置PopupWindow的背景
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int yoff =0-getContext().getResources().getDimensionPixelSize(R.dimen.effect_list_view_size)- DensityUtil
                .dip2px(getContext(),25 );
            int xoff = DensityUtil.dip2px(getContext(),5);
            window.showAsDropDown(mListView,xoff,yoff);
            isFirstShow = false;
        }
    }


    public AnimationFilterChooserView(@NonNull Context context) {
        this(context, null);
    }

    public AnimationFilterChooserView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimationFilterChooserView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onItemClick(EffectInfo effectInfo, int index) {
        if(index == 0) {
            //删除最后一次添加效果
            Dispatcher.getInstance().postMsg(new DeleteLastAnimationFilter());

        }
        return false;
    }
    @Override
    public void onTouchEvent(int motionEvent, int index, EffectInfo info) {
        switch (motionEvent){
            case OnItemTouchListener.EVENT_UP:
                //先判断缩略图是否在被拖动状态，如果在被拖动状态则不能添加特效
                if (mThumbLineBar!=null&&!mThumbLineBar.isTouching()){
                    //添加特效结束时，恢复缩略图滑动事件
                    //setThumbScrollEnable(true);
                    setClickable(true);
                    Dispatcher.getInstance().postMsg(new LongClickUpAnimationFilter.Builder()
                        .effectInfo(info)
                        .index(index)
                        .build());
                }
                break;
            case OnItemTouchListener.EVENT_DOWN:
                if(index > 0) {
                    //先判断缩略图是否在被拖动状态，如果在被拖动状态则不能添加特效
                    if (mThumbLineBar!=null&&!mThumbLineBar.isTouching()){
                        //添加特效开始时，关闭特效界面点击
                        //setThumbScrollEnable(false);
                        setClickable(false);
                        info.streamStartTime = mPlayerListener.getCurrDuration();
                        Dispatcher.getInstance().postMsg(new LongClickAnimationFilter.Builder()
                            .effectInfo(info)
                            .index(index)
                            .build());
                    }


                }
                break;
                default:
                    break;
        }


    }

    @Override
    public void onClick(View v) {
        if (v==mComplete){
            Dispatcher.getInstance().postMsg(new ConfirmAnimationFilter());
            if (mOnEffectActionLister!=null){
                mOnEffectActionLister.onComplete();
            }
        }else if (v==mCancel){
            onBackPressed();
        }

    }
    @Override
    protected void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.aliyun_svideo_layout_filter_container, this);
        mListView = (RecyclerView) findViewById(R.id.effect_list_filter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mListView.setLayoutManager(layoutManager);
        mFlThumblinebar = findViewById(R.id.fl_thumblinebar);
        mFilterAdapter = new EffectAdapter(getContext());
        mFilterAdapter.setOnItemClickListener(this);
        mFilterAdapter.setOnItemTouchListener(this);
        mFilterAdapter.setDataList(Common.getAnimationFilterList());
        //        mFilterAdapter.setSelectedPos(mEditorService.getEffectIndex(UIEditorPage.FILTER_EFFECT));
        mListView.setAdapter(mFilterAdapter);
        mListView.addItemDecoration(new SpaceItemDecoration(getContext().getResources().getDimensionPixelSize(R.dimen.list_item_space)));
        //        mListView.scrollToPosition(mEditorService.getEffectIndex(UIEditorPage.FILTER_EFFECT));
        mCancel = (ImageView) findViewById(R.id.cancel);
        mTvEffectTitle = (TextView) findViewById(R.id.tv_effect_title);
        mIvEffectIcon = (ImageView) findViewById(R.id.iv_effect_icon);
        mComplete = (ImageView) findViewById(R.id.complete);
        mIvEffectIcon.setImageResource(R.mipmap.alivc_svideo_effect);
        mTvEffectTitle.setText(R.string.alivc_svideo_filter_effect);
        mComplete.setOnClickListener(this);
        mCancel.setOnClickListener(this);
    }

    @Override
    protected UIEditorPage getUIEditorPage() {
        return UIEditorPage.FILTER_EFFECT;
    }

    @Override
    public void onBackPressed() {
        Dispatcher.getInstance().postMsg(new ClearAnimationFilter());
        if (mOnEffectActionLister!=null){
            mOnEffectActionLister.onCancel();
        }
    }

    public void setFirstShow(boolean firstShow) {
        isFirstShow = firstShow;
    }

    @Override
    public boolean isPlayerNeedZoom() {
        return true;
    }
    @Override
    protected FrameLayout getThumbContainer() {
        return mFlThumblinebar;
    }
}
