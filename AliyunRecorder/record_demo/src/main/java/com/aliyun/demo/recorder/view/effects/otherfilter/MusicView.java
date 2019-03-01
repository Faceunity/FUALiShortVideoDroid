package com.aliyun.demo.recorder.view.effects.otherfilter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.demo.R;
import com.aliyun.demo.recorder.view.dialog.IPageTab;
import com.aliyun.demo.recorder.view.dialog.OnClearEffectListener;

/**
 * 音乐滤镜
 * Created by hyj on 2019/2/25.
 */

public class MusicView extends Fragment implements IPageTab, OnClearEffectListener, DistortingMirrorAdapter.OnItemListener {
    private static final String TAG = MusicView.class.getSimpleName();
    //RecyclerView列数
    private static final int SPAN_COUNT = 5;
    private RecyclerView rvDMChooser;
    private DistortingMirrorAdapter mAdapter;
    private DistortingMirrorAdapter.OnItemListener onItemListener;
    private int mSelected = 0;
    private DistortingMirrorAdapter.OnMusicListener onMusicListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.alivc_mv_choose_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvDMChooser = view.findViewById(R.id.rv_mv_chooser);
        mAdapter = new DistortingMirrorAdapter(getActivity(), Effect.EFFECT_TYPE_MUSIC_FILTER);
        mAdapter.setOnItemListener(this);
        mAdapter.setSelected(mSelected);
        mAdapter.setmOnMusicListener(onMusicListener);
        rvDMChooser.setAdapter(mAdapter);
        rvDMChooser.setLayoutManager(new GridLayoutManager(getActivity(), SPAN_COUNT));
    }

    public void setOnMusicListener(DistortingMirrorAdapter.OnMusicListener onMusicListener) {
        this.onMusicListener = onMusicListener;
    }

    @Override
    public void onClearEffectClick() {

    }

    public void clearBundle() {
        if (mAdapter != null)
            mAdapter.clearBundle();
    }

    @Override
    public String getTabTitle() {
        return "音乐滤镜";
    }

    @Override
    public int getTabIcon() {
        return 0;
    }

    public void setOnItemListener(DistortingMirrorAdapter.OnItemListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    @Override
    public void onPosition(int position, Effect effect) {
        mSelected = position;
        if (onItemListener != null)
            onItemListener.onPosition(position, effect);
    }
}
