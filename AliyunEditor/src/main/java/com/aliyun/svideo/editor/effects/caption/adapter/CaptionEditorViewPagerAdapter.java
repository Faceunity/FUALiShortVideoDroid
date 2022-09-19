/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.caption.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;

import com.aliyun.svideo.editor.effects.caption.adapter.holder.BaseCaptionViewHolder;

import java.util.ArrayList;
import java.util.List;

public class CaptionEditorViewPagerAdapter extends PagerAdapter {
    private List<BaseCaptionViewHolder> mTabHolder = new ArrayList<>();

    @Override
    public int getCount() {
        return mTabHolder.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        BaseCaptionViewHolder holder = mTabHolder.get(position);
        container.addView(holder.getItemView());
        holder.onBindViewHolder();
        return holder.getItemView();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabHolder.get(position).getTitle();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (object instanceof View) {
            container.removeView((View) object);
        }
    }

    /**
     * 刷新一个位置
     * @param postion
     */
    public void notifyDataSetChanged(int postion) {
        if (mTabHolder.size() > postion) {
            mTabHolder.get(postion).notifyDataSetChanged();
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public void addViewHolder(BaseCaptionViewHolder holder) {
        mTabHolder.add(holder);
    }


}
