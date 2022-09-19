package com.aliyun.svideo.editor.effects.caption.component;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.bean.AlivcEditMenuBean;
import com.aliyun.svideo.editor.bean.AlivcEditMenus;
import com.aliyun.svideo.editor.editor.AliyunBasePasterController;
import com.aliyun.svideo.editor.effects.caption.adapter.BottomMenuAdapter;
import com.aliyun.svideo.editor.effects.control.BaseChooser;
import com.aliyun.svideo.editor.effects.control.EffectInfo;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.aliyun.svideo.editor.effects.caption.manager.AlivcEditorViewFactory;

import java.util.ArrayList;
import java.util.List;


public class CaptionChooserPanelView extends BaseChooser {

    private BottomMenuAdapter.OnItemClickListener mOnItemClickListener;

    public CaptionChooserPanelView(@NonNull Context context) {
        this(context, null);
    }

    public CaptionChooserPanelView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CaptionChooserPanelView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.alivc_editor_view_chooser_panel_caption, this);
        initTitleView(view);
        initListener();
        RecyclerView bottomMenuRecyclerview = view.findViewById(R.id.bottom_menu_recycleview);
        BottomMenuAdapter bottomMenuAdapter = new BottomMenuAdapter();
        bottomMenuAdapter.setOnItemClickListener(mOnItemClickListener);
        LinearLayoutManager menuLinearLayoutManager = new LinearLayoutManager(getContext());
        menuLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        bottomMenuRecyclerview.setLayoutManager(menuLinearLayoutManager);
        bottomMenuRecyclerview.setAdapter(bottomMenuAdapter);
        bottomMenuAdapter.setData(loadMenus());
    }
    private void initTitleView(View view) {

        ImageView ivEffect = view.findViewById(R.id.iv_effect_icon);
        TextView tvTitle = view.findViewById(R.id.tv_effect_title);
        ivEffect.setImageResource(R.mipmap.aliyun_svideo_icon_caption);
        tvTitle.setText(R.string.alivc_editor_effect_caption);

        view.findViewById(R.id.iv_cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dealCancel();
            }
        });
        view.findViewById(R.id.iv_confirm).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnEffectActionLister != null) {
                    mOnEffectActionLister.onComplete();
                }
            }
        });
       setOnClickListener(new OnClickListener() {
           @Override
           public void onClick(View v) {

           }
       });
    }
    private List<AlivcEditMenuBean> loadMenus() {
        List<AlivcEditMenuBean> list = new ArrayList<>();
        list.add(new AlivcEditMenuBean("加字幕", R.mipmap.alivc_svideo_icon_roll_caption, AlivcEditMenus.AddText));
        return list;
    }

    private void initListener() {
        mOnItemClickListener = new BottomMenuAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(AlivcEditMenus alivcEditMenus) {
                if (alivcEditMenus == AlivcEditMenus.AddText) {
                    if (mOnEffectChangeListener != null) {
                        EffectInfo effectInfo = new EffectInfo();
                        effectInfo.type = UIEditorPage.COMPOUND_CAPTION;
                        mOnEffectChangeListener.onEffectChange(effectInfo);
                    }
                }

            }
        };

    }


    private void dealCancel() {
        if (mOnEffectActionLister != null) {
            mOnEffectActionLister.onCancel();
        }
    }

    @Override
    public boolean isPlayerNeedZoom() {
        return true;
    }

    @Override
    public boolean isShowSelectedView() {
        return false;
    }

    /**
     * 缩略图滑动条
     *
     * @return FrameLayout
     */
    @Override
    protected FrameLayout getThumbContainer() {
        return findViewById(R.id.fl_thumblinebar);
    }


    @Override
    protected UIEditorPage getUIEditorPage() {
        return UIEditorPage.COMPOUND_CAPTION;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dealCancel();
    }

    @Override
    public boolean isHostPaster(AliyunBasePasterController uic) {
        return false;
    }



    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        View rootView = getRootView();
        if (rootView instanceof ViewGroup){
            CaptionEditorPanelView captionEditorPanelView = AlivcEditorViewFactory.findCaptionEditorPanelView((ViewGroup) rootView);
            if (captionEditorPanelView != null){
                captionEditorPanelView.onActivityResult(requestCode,resultCode,data);
            }
        }
    }
}
