/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.effectmanager;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.aliyun.demo.actionbar.ActionBarActivity;
import com.aliyun.demo.editor.R;
import com.aliyun.demo.http.EffectService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class EffectManagerActivity extends ActionBarActivity implements View.OnClickListener{
    public static final String KEY_TAB = "key_tab";
    public static final int PASTER = 0;
    public static final int CAPTION = 1;
    public static final int TEXT = 2;
    public static final int MV = 3;
    private EffectManagerFragment mPasterManagerFragment;
    private EffectManagerFragment mCaptionManagerFragment;
    private EffectManagerFragment mTextManagerFragment;
    private EffectManagerFragment mMvManagerFragment;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private StateController mStateController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aliyun_svideo_activity_effect_manager);
        setActionBarTitle(getString(R.string.effect_mananger_nav_edit));
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
        EffectPagerAdapter adapter = new EffectPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(getString(R.string.motion_sticker_effect_manager), mPasterManagerFragment);
        adapter.addFragment(getString(R.string.subtitle_effect_manager), mCaptionManagerFragment);
        adapter.addFragment(getString(R.string.font_effect_manager), mTextManagerFragment);
        adapter.addFragment(getString(R.string.mv_effect_manager),mMvManagerFragment);
        mViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mViewPager);
        int selectedTab = getIntent().getIntExtra(KEY_TAB, 0);
        mTabLayout.getTabAt(selectedTab).select();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == getActionBarRightViewID()) {
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
            int i=0;
            for(Map.Entry<String, Fragment> entry:entrySet) {
                if(i == position) {
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
            int i=0;
            for(Map.Entry<String, Fragment> entry:entrySet) {
                if(i == position) {
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
