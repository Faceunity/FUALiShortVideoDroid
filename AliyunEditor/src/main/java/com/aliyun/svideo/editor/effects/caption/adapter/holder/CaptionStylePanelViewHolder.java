/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.caption.adapter.holder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.caption.adapter.CaptionEditorViewPagerAdapter;
import com.aliyun.svideo.editor.effects.caption.listener.OnCaptionChooserStateChangeListener;
import com.google.android.material.tabs.TabLayout;

/**
 * 字幕花字样式
 */
public class CaptionStylePanelViewHolder extends BaseCaptionViewHolder {
    private ViewPager mViewPage;
    private TabLayout mTabLayout;

    private static final int[] ID_TITLE_ARRAY = {R.string.alivc_editor_dialog_caption_color,
                                                 R.string.alivc_editor_dialog_caption_font,
                                                 R.string.alivc_editor_effect_text_stroke,
                                                 R.string.alivc_editor_effect_text_shadow,
                                                 R.string.alivc_editor_effect_text_fontstyle,
                                                 R.string.alivc_editor_effect_text_alignment,
                                                };
    private CaptionColorViewHolder mColorViewHolder;
    private CaptionFontTypefacePanelViewHolder mCaptionFontTypefacePanelViewHolder;
    private CaptionBackgroundColorViewHolder mBackgroundColorViewHolder;
    private CaptionStrokeColorViewHolder mStoreColorViewHolder;
    private CaptionShadowViewHolder mCaptionShadowViewHolder;
    private CaptionFontStyleViewHolder mCaptionFontStyleViewHolder;
    private CaptionEditorViewPagerAdapter mCaptionEditorViewPagerAdapter;
    private CaptionAlignmentViewHolder mCaptionAlignmentViewHolder;

    public CaptionStylePanelViewHolder(Context context, String title, OnCaptionChooserStateChangeListener onCaptionChooserStateChangeListener) {
        super(context, title, onCaptionChooserStateChangeListener);
    }


    @Override
    public View onCreateView(Context context) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.alivc_editor_caption_style_container, null, false);
        initViewPage(rootView);
        initTableView(rootView);
        return rootView;
    }

    @Override
    public void onBindViewHolder() {
    }

    @Override
    public void notifyDataSetChanged() {
        if (mCaptionEditorViewPagerAdapter != null && mTabLayout != null) {
            mCaptionEditorViewPagerAdapter.notifyDataSetChanged(mTabLayout.getSelectedTabPosition());
        }
    }

    @Override
    public void resourceChanged() {
        if (mCaptionFontTypefacePanelViewHolder != null) {
            mCaptionFontTypefacePanelViewHolder.resourceChanged();
        }
    }

    @Override
    public void onTabClick() {
        notifyDataSetChanged();
    }

    private void initViewPage(View rootView) {
        mViewPage = rootView.findViewById(R.id.viewpager);
        mViewPage.setOffscreenPageLimit(5);
        mCaptionEditorViewPagerAdapter = new CaptionEditorViewPagerAdapter();

        mColorViewHolder = new CaptionColorViewHolder(getContext(),
                rootView.getContext().getString(R.string.alivc_editor_dialog_caption_color), getCaptionChooserStateChangeListener());
        mCaptionFontTypefacePanelViewHolder = new CaptionFontTypefacePanelViewHolder(getContext(),
                getContext().getString(R.string.alivc_editor_dialog_caption_font), getCaptionChooserStateChangeListener());
        mBackgroundColorViewHolder = new CaptionBackgroundColorViewHolder(getContext(),
                rootView.getContext().getString(R.string.alivc_editor_dialog_caption_background_color), getCaptionChooserStateChangeListener());

        mStoreColorViewHolder = new CaptionStrokeColorViewHolder(getContext(),
                rootView.getContext().getString(R.string.alivc_editor_effect_text_stroke), getCaptionChooserStateChangeListener());
        mCaptionShadowViewHolder = new CaptionShadowViewHolder(getContext(),
                getContext().getString(R.string.alivc_editor_effect_text_shadow), getCaptionChooserStateChangeListener());
        mCaptionFontStyleViewHolder = new CaptionFontStyleViewHolder(getContext(),
                getContext().getString(R.string.alivc_editor_effect_text_fontstyle), getCaptionChooserStateChangeListener());

        mCaptionAlignmentViewHolder = new CaptionAlignmentViewHolder(getContext(), getContext().getString(R.string.alivc_editor_effect_text_alignment), getCaptionChooserStateChangeListener());

        mCaptionEditorViewPagerAdapter.addViewHolder(mColorViewHolder);
        mCaptionEditorViewPagerAdapter.addViewHolder(mCaptionFontTypefacePanelViewHolder);
        mCaptionEditorViewPagerAdapter.addViewHolder(mBackgroundColorViewHolder);
        mCaptionEditorViewPagerAdapter.addViewHolder(mStoreColorViewHolder);
        mCaptionEditorViewPagerAdapter.addViewHolder(mCaptionShadowViewHolder);
        mCaptionEditorViewPagerAdapter.addViewHolder(mCaptionFontStyleViewHolder);
        mCaptionEditorViewPagerAdapter.addViewHolder(mCaptionAlignmentViewHolder);
        mViewPage.setAdapter(mCaptionEditorViewPagerAdapter);

    }


    /**
     * 初始化新View
     */
    private void initTableView(View rootview) {
        mTabLayout = rootview.findViewById(R.id.tl_tab);
        mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        for (int i = 0; i < ID_TITLE_ARRAY.length; i++) {
            View item = LayoutInflater.from(rootview.getContext()).inflate(R.layout.alivc_editor_caption_style_item_tab, (ViewGroup) rootview, false);
            ((TextView) item.findViewById(R.id.tv_title)).setText(ID_TITLE_ARRAY[i]);
            mTabLayout.addTab(mTabLayout.newTab().setCustomView(item));
        }
        mTabLayout.setSelectedTabIndicatorHeight(0);
        mTabLayout.setupWithViewPager(mViewPage);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                case 0:
                    mColorViewHolder.onTabClick();
                    break;
                case 1:
                    mCaptionFontTypefacePanelViewHolder.onTabClick();
                    break;
                case 2:
                    mBackgroundColorViewHolder.onTabClick();
                    break;
                case 3:
                    mStoreColorViewHolder.onTabClick();
                    break;
                case 4:
                    mCaptionShadowViewHolder.onTabClick();
                    break;
                case 5:
                    mCaptionFontStyleViewHolder.onTabClick();
                    break;
                case 6:
                    mCaptionAlignmentViewHolder.onTabClick();
                    break;
                default:
                    break;

                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

}
