package com.aliyun.demo.recorder.view.effects.otherfilter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.common.global.AppInfo;
import com.aliyun.demo.R;
import com.aliyun.demo.recorder.view.dialog.IPageTab;
import com.aliyun.demo.recorder.view.dialog.OnClearEffectListener;
import com.aliyun.demo.recorder.view.effects.EffectBody;
import com.aliyun.demo.recorder.view.effects.manager.EffectLoader;
import com.aliyun.demo.recorder.view.effects.mv.MVAdapter;
import com.aliyun.demo.recorder.view.effects.paster.AlivcPasterAdapter;
import com.aliyun.demo.recorder.view.effects.paster.AlivcPasterChooseView;
import com.aliyun.demo.recorder.view.effects.paster.PasterSelectListener;
import com.aliyun.svideo.sdk.external.struct.form.IMVForm;
import com.aliyun.svideo.sdk.external.struct.form.PreviewPasterForm;

import java.util.ArrayList;
import java.util.List;

/**
 * 哈哈镜
 * Created by hyj on 2018/11/5.
 */

public class DistortingMirrorView extends Fragment implements IPageTab, OnClearEffectListener, DistortingMirrorAdapter.OnItemListener {
    private static final String TAG = DistortingMirrorView.class.getSimpleName();
    //RecyclerView列数
    private static final int SPAN_COUNT = 5;
    private RecyclerView rvDMChooser;
    private DistortingMirrorAdapter mAdapter;
    private DistortingMirrorAdapter.OnItemListener onItemListener;
    private int mSelected = 0;

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
        mAdapter = new DistortingMirrorAdapter(getActivity(), Effect.EFFECT_TYPE_FACE_WARP);
        mAdapter.setOnItemListener(this);
        mAdapter.setSelected(mSelected);
        rvDMChooser.setAdapter(mAdapter);
        rvDMChooser.setLayoutManager(new GridLayoutManager(getActivity(), SPAN_COUNT));
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
        return "哈哈镜";
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
