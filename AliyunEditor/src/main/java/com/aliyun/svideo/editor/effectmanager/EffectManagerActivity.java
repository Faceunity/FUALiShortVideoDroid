/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effectmanager;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.aliyun.svideo.base.http.EffectService;
import com.aliyun.svideo.editor.R;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class EffectManagerActivity extends AbstractActionBarActivity implements View.OnClickListener {
    public static final String KEY_TAB = "key_tab";
    public static final int PASTER = 0;
    public static final int CAPTION = 1;
    public static final int TEXT = 2;
    public static final int MV = 3;
    public static final int ANIM_EFFECT = 4;
    public static final int TRANSITION = 5;
    private EffectManagerFragment mPasterManagerFragment;
    private EffectManagerFragment mCaptionManagerFragment;
    private EffectManagerFragment mTextManagerFragment;
    private EffectManagerFragment mMvManagerFragment;
    private EffectManagerFragment mEffectManagerFragment;
    private EffectManagerFragment mTransitionManagerFragment;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private StateController mStateController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alivc_editor_activity_effect_manager);
        setActionBarTitle(getString(R.string.alivc_editor_mananger_tittle));
//        setActionBarRightView(R.mipmap.aliyun_svideo_icon_effect_manage);
        setActionBarLeftView(R.mipmap.aliyun_svideo_icon_back);
        setActionBarLeftViewVisibility(View.VISIBLE);
        setActionBarTitleVisibility(View.VISIBLE);
        setActionBarRightViewVisibility(View.GONE);
//        setActionBarRightClickListener(this);
        mStateController = new StateController();
        initViewPager();
    }

    private void initViewPager() {
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mPasterManagerFragment = EffectManagerFragment.newInstance(EffectService.EFFECT_PASTER);
        mCaptionManagerFragment = EffectManagerFragment.newInstance(EffectService.EFFECT_CAPTION);
        mTextManagerFragment = EffectManagerFragment.newInstance(EffectService.EFFECT_TEXT);
        mMvManagerFragment = EffectManagerFragment.newInstance(EffectService.EFFECT_MV);
        mEffectManagerFragment = EffectManagerFragment.newInstance(EffectService.ANIMATION_FILTER);
        mTransitionManagerFragment = EffectManagerFragment.newInstance(EffectService.EFFECT_TRANSITION);
        EffectPagerAdapter adapter = new EffectPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(getString(R.string.alivc_editor_effect_sticker), mPasterManagerFragment);
        adapter.addFragment(getString(R.string.alivc_editor_effect_caption), mCaptionManagerFragment);
        adapter.addFragment(getString(R.string.alivc_editor_manager_font), mTextManagerFragment);
        adapter.addFragment(getString(R.string.alivc_editor_effect_mv), mMvManagerFragment);
        adapter.addFragment(getString(R.string.alivc_editor_effect_effect), mEffectManagerFragment);
        adapter.addFragment(getString(R.string.alivc_editor_effect_transition), mTransitionManagerFragment);
        mViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mViewPager);
        int selectedTab = getIntent().getIntExtra(KEY_TAB, 0);
        mTabLayout.getTabAt(selectedTab).select();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == getActionBarRightViewID()) {
            mStateController.switchState();
        }
    }

    private class EffectPagerAdapter extends FragmentPagerAdapter {
        private LinkedHashMap<String, Fragment> mFragments = new LinkedHashMap<>();
        private Set<Map.Entry<String, Fragment>> mEntries;

        public EffectPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Set<Map.Entry<String, Fragment>> entrySet = mFragments.entrySet();
            int i = 0;
            for (Map.Entry<String, Fragment> entry : entrySet) {
                if (i == position) {
                    return entry.getValue();
                }
                i++;
            }
            return null;
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Set<Map.Entry<String, Fragment>> entrySet = mFragments.entrySet();
            int i = 0;
            for (Map.Entry<String, Fragment> entry : entrySet) {
                if (i == position) {
                    return entry.getKey();
                }
                i++;
            }
            return null;
        }

        public void addFragment(String title, Fragment fragment) {
            mFragments.put(title, fragment);
            mEntries = mFragments.entrySet();
        }
    }

    public StateController getStateController() {
        return mStateController;
    }
}
