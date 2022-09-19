/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.editor;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ColorViewPagerAdapter extends PagerAdapter {
    private List<ViewHolder> mTabHolder = new ArrayList<>();

    @Override
    public int getCount() {
        return mTabHolder.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ViewHolder holder = mTabHolder.get(position);
        container.addView(holder.mItemView);
        holder.onBindViewHolder();
        return holder.mItemView;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabHolder.get(position).mTitle;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (object instanceof View) {
            container.removeView((View) object);
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public void addViewHolder(ViewHolder holder) {
        mTabHolder.add(holder);
    }

    public static abstract class ViewHolder {
        View mItemView;
        String mTitle;

        public ViewHolder(Context context, String title) {
            this.mItemView = onCreateView(context);
            this.mTitle = title;
        }

        protected abstract View onCreateView(Context context);
        protected abstract void onBindViewHolder();
    }
}
