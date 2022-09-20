package com.aliyun.svideo.editor.draft;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class DraftPagerAdapter extends FragmentPagerAdapter {
    private String[] tabs = new String[]{"草稿箱", "模板草稿", "云端草稿"};

    public DraftPagerAdapter(final FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(final int position) {
        Fragment fragment;
        if (position == 2) {
            fragment = new CloudDraftFragment();
        } else {
            fragment = new LocalDraftFragment();
            Bundle args = new Bundle();
            args.putInt("TAB_INDEX", position);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return tabs.length;
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        return tabs[position];
    }
}
