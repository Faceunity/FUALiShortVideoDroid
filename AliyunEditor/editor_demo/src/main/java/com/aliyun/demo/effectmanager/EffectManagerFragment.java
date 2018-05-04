/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.effectmanager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.demo.editor.R;
import com.aliyun.downloader.FileDownloaderModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class EffectManagerFragment extends Fragment implements StateController.StateAdapter.OnStateChangeListener{
    private static final String KEY_EFFECT_TYPE = "effect_type";
    private RecyclerView mRv;
    private RecycleViewAdapter mAdapter;
    private List<FileDownloaderModel> mList = new ArrayList<>();
    private StateController mStateController;
    private int mEffectType;
    private AsyncTask<Integer, Void, List<FileDownloaderModel>> mLoadTask = null;
    private EffectLoader mPasterLoader = new EffectLoader();
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEffectType = getArguments().getInt(KEY_EFFECT_TYPE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.aliyun_svideo_activity_effect_fragment, null);
        mRv = (RecyclerView) view.findViewById(R.id.rv_view);
        mRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new RecycleViewAdapter(mList);
        mAdapter.setStateChangeListener(this);
        mRv.setAdapter(mAdapter);
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                //首先回调的方法 返回int表示是否监听该方向
                int dragFlags = ItemTouchHelper.UP|ItemTouchHelper.DOWN;//拖拽
                int swipeFlags = ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT;//侧滑删除
                return makeMovementFlags(dragFlags,swipeFlags);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                //滑动事件
                Collections.swap(mList,viewHolder.getAdapterPosition(),target.getAdapterPosition());
                mAdapter.notifyItemMoved(viewHolder.getAdapterPosition(),target.getAdapterPosition());
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                //侧滑事件
                mList.remove(viewHolder.getAdapterPosition());
                mAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            }

            @Override
            public boolean isLongPressDragEnabled() {
                //是否可拖拽
                return true;
            }
        });
//        helper.attachToRecyclerView(mRv);
        mStateController = ((EffectManagerActivity) getActivity()).getStateController();
        mStateController.addAdatper(mAdapter);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mLoadTask = new AsyncTask<Integer, Void, List<FileDownloaderModel>>() {
            @Override
            protected List<FileDownloaderModel> doInBackground(Integer... params) {
                return mPasterLoader.loadLocalEffect(params[0]);
            }

            @Override
            protected void onPostExecute(List<FileDownloaderModel> fileDownloaderModels) {
                super.onPostExecute(fileDownloaderModels);
                if(fileDownloaderModels != null) {
                    mList.clear();
                    mList.addAll(fileDownloaderModels);
                    mAdapter.notifyDataSetChanged();
                }
            }
        };
        mLoadTask.execute(mEffectType);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mLoadTask != null) {
            mLoadTask.cancel(true);
        }
    }

    @Override
    public void onEditState() {

    }

    @Override
    public void onCompleteState() {

    }

    public static EffectManagerFragment newInstance(int effectType) {
        EffectManagerFragment fragment = new EffectManagerFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_EFFECT_TYPE, effectType);
        fragment.setArguments(args);
        return fragment;
    }
}
