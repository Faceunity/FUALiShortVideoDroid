package com.aliyun.race.sample.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Mulberry on 2018/4/11.
 */
public class HomeViewPagerAdapter extends PagerAdapter {

    private List<View> mViewLists;

    public HomeViewPagerAdapter(List<View> viewLists) {
        this.mViewLists = viewLists;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //remove item
        container.removeView(mViewLists.get(position));
    }

    /**
     * 将当前View添加到ViewGroup容器中
     * 这个方法，return一个对象，这个对象表明了PagerAdapter适配器选择哪个对象放在当前的ViewPager中
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        //add item return item view
        container.addView(mViewLists.get(position));
        return mViewLists.get(position);
    }

    @Override
    public int getCount() {
        return mViewLists != null ? mViewLists.size() : 0;
    }

    /**
     *用于标识一个视图是否和给定的Key对象相关
     */
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
