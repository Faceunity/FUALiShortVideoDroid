package com.aliyun.demo.recorder.view.effects.filter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.aliyun.demo.R;
import com.aliyun.demo.recorder.view.effects.filter.interfaces.OnFilterItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 滤镜view的封装
 * @author xlx
 */
public class FilterLoadingView extends FrameLayout {
    private FilterAdapter alivcFilterAdapter;
    private List<String> dataList;
    private ContentLoadingProgressBar contentProgress;
    private RecyclerView recyclerView;
    /**
     * 滤镜item点击listener
     */
    private OnFilterItemClickListener mOnFilterItemClickListener;

    public FilterLoadingView(@NonNull Context context) {
        this(context, null);
    }

    public FilterLoadingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FilterLoadingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.alivc_svideo_filter_view_layout, this, true);
        initProgressView(view);
        initRecyclerView(view);
    }

    private void initRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.alivc_filter_list);
        dataList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        alivcFilterAdapter = new FilterAdapter(getContext(), dataList);

        recyclerView.setAdapter(alivcFilterAdapter);
        // item点击事件
        alivcFilterAdapter.setOnItemClickListener(new OnFilterItemClickListener() {
            @Override
            public void onItemClick(EffectInfo effectInfo, int index) {
                if (mOnFilterItemClickListener != null) {
                    mOnFilterItemClickListener.onItemClick(effectInfo, index);
                }

            }
        });


    }

    private void progressLoading() {
        contentProgress.postDelayed(new Runnable() {
            @Override
            public void run() {
                contentProgress.hide();
            }
        }, 3000);
    }

    private void initProgressView(View view) {
        contentProgress = view.findViewById(R.id.content_progress);
        contentProgress.getIndeterminateDrawable().setColorFilter(
            ContextCompat.getColor(getContext(), R.color.effect_bg_color),
            PorterDuff.Mode.MULTIPLY);
        progressLoading();
    }

    public void notifyDataChange(List<String> alivcFilterInfos) {

        this.dataList.addAll(alivcFilterInfos);
        if (contentProgress != null) {
            contentProgress.hide();
        }
        updateRecyclerState();
        alivcFilterAdapter.notifyDataSetChanged();
    }

    public void setOnFilterListItemClickListener(OnFilterItemClickListener listener) {
        this.mOnFilterItemClickListener = listener;
    }

    private void updateRecyclerState() {
        if (dataList != null && dataList.size() != 0) {
            recyclerView.setVisibility(VISIBLE);
        } else {
            recyclerView.setVisibility(GONE);
        }
    }

    public void setFilterPosition(int filterPosition) {
        alivcFilterAdapter.setSelectedPos(filterPosition);
    }
}
