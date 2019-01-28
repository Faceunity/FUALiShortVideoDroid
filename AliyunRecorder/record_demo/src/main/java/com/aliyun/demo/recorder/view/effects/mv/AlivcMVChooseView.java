package com.aliyun.demo.recorder.view.effects.mv;

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
import com.aliyun.svideo.sdk.external.struct.form.IMVForm;
import com.liulishuo.filedownloader.BaseDownloadTask;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class AlivcMVChooseView extends Fragment implements MVAdapter.OnItemClickListener,IPageTab,  OnClearEffectListener {
    private static final String TAG = AlivcMVChooseView.class.getSimpleName();
    //RecyclerView列数
    private static final int SPAN_COUNT = 5;
    private RecyclerView rvMvChooser;
    private MVAdapter mAdapter;
    private EffectLoader mMVLoader ;
    private int mSelectedMVId = -1;

    private MvSelectListener mMvSelectListener;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.alivc_mv_choose_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvMvChooser = view.findViewById(R.id.rv_mv_chooser);
        mAdapter = new MVAdapter(getActivity());
        mAdapter.setmItemClickListener(this);
        mAdapter.setSelectedMVId(mSelectedMVId);
        rvMvChooser.setAdapter(mAdapter);
        rvMvChooser.setLayoutManager(new GridLayoutManager(getActivity(), SPAN_COUNT));
        mMVLoader= new EffectLoader(getContext());
        mMVLoader.loadAllMV(AppInfo.getInstance().obtainAppSignature(getActivity().getApplicationContext()),
            new EffectLoader.LoadCallback<IMVForm>() {
                @Override
                public void onLoadCompleted(List<IMVForm> localInfos,
                                            List<IMVForm> remoteInfos,
                                            Throwable e) {
                    List<EffectBody<IMVForm>> remoteData = new ArrayList<>();
                    List<EffectBody<IMVForm>> localData = new ArrayList<>();
                    if (localInfos != null) {
                        EffectBody<IMVForm> body;
                        for (IMVForm form : localInfos) {
                            body = new EffectBody<IMVForm>(form, true);
                            localData.add(body);
                        }
                    }
                    if (remoteInfos != null) {
                        EffectBody<IMVForm> body;
                        for (IMVForm mv : remoteInfos) {
                            body = new EffectBody<IMVForm>(mv, false);
                            remoteData.add(body);
                        }
                    }
                    remoteData.addAll(0,localData);

                    mAdapter.syncData(remoteData);
                }
            });
    }

    @Override
    public void onRemoteItemClick(final int position, final EffectBody<IMVForm> data) {
        //取消现在的应用的效果
        if (mMvSelectListener!=null){
            mMvSelectListener.onMvSelected(mAdapter.getDataList().get(0).getData());
        }
        mSelectedMVId = data.getData().getId();
        //下载
        final IMVForm mv = data.getData();
        mMVLoader.downloadMV(mv, new MVDownloadListener() {
            @Override
            public void onStart(int downloadId, long soFarBytes, long totalBytes, int preProgress) {
                super.onStart(downloadId, soFarBytes, totalBytes, preProgress);
                mAdapter.notifyDownloadingStart(data);
            }
            @Override
            public void onFinish(int downloadId, String path,boolean allFinish) {
                if (allFinish){
                    if (mMvSelectListener!=null&&mSelectedMVId==mv.getId()){
                        mMvSelectListener.onMvSelected(data.getData());
                    }
                    mAdapter.notifyDownloadingComplete(data, position,false);
                }

            }

            @Override
            public void onProgress(int downloadId, long soFarBytes, long totalBytes, long speed, int progress) {
                super.onProgress(downloadId, soFarBytes, totalBytes, speed, progress);
                mAdapter.updateProcess(
                    (MVAdapter.MVViewHolder)rvMvChooser.findViewHolderForAdapterPosition(position), progress, position);
            }

            @Override
            public void onError(BaseDownloadTask task1, Throwable e) {
                super.onError(task1, e);
                mAdapter.notifyDownloadingComplete(data, position,true);
            }
        });

    }
    @Override
    public void onLocalItemClick(int position, EffectBody<IMVForm> data) {
        mSelectedMVId = data.getData().getId();
        if (mMvSelectListener!=null){
            mMvSelectListener.onMvSelected(data.getData());
        }
    }

    @Override
    public String getTabTitle() {

        return "MV";
    }

    @Override
    public int getTabIcon() {
        return R.mipmap.alivc_svideo_icon_tab_mv;
    }

    public void setMvSelectListener(MvSelectListener mMvSelectListener) {
        this.mMvSelectListener = mMvSelectListener;
    }

    @Override
    public void onClearEffectClick() {
        mSelectedMVId = mAdapter.getDataList().get(0).getData().getId();
        mAdapter.setSelectedMVId(mSelectedMVId);
        mAdapter.notifyDataSetChanged();
        if (mMvSelectListener!=null){
            mMvSelectListener.onMvSelected(mAdapter.getDataList().get(0).getData());
        }
    }


}
