/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.effects.imv;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.aliyun.demo.editor.R;
import com.aliyun.demo.effects.control.BaseChooser;
import com.aliyun.demo.effects.control.EditorService;
import com.aliyun.demo.effects.control.EffectInfo;
import com.aliyun.demo.effects.control.OnItemClickListener;
import com.aliyun.demo.effects.control.SpaceItemDecoration;
import com.aliyun.demo.effects.control.UIEditorPage;
import com.aliyun.demo.http.EffectService;
import com.aliyun.downloader.DownloaderManager;
import com.aliyun.downloader.FileDownloaderModel;
import com.aliyun.struct.form.AspectForm;
import com.aliyun.struct.form.IMVForm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ImvChooserMediator extends BaseChooser implements OnItemClickListener{
    private RecyclerView mListView;
    private ImvAdapter mImvAdapter;
    private RelativeLayout mDismissRelative;

    List<IMVForm> mImvList = new ArrayList<>();

    public static ImvChooserMediator newInstance(){
        ImvChooserMediator dialog = new ImvChooserMediator();
        Bundle args = new Bundle();
//        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IMVForm imvForm = new IMVForm();
        mImvList.add(imvForm);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(mEditorService == null) {
            mEditorService = new EditorService();
        }
        initResourceLocalWithSelectId(-1);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = LayoutInflater.from(getActivity()).inflate(R.layout.aliyun_svideo_filter_view, container);
        mListView = (RecyclerView) mView.findViewById(R.id.effect_list_filter);
        mDismiss = (ImageView) mView.findViewById(R.id.dismiss);
        mDismiss.setOnClickListener(onClickListener);
        mDismissRelative = (RelativeLayout) mView.findViewById(R.id.effect_list_dismiss);
        if(mEditorService != null && mEditorService.isFullScreen()) {
            mListView.setBackgroundColor(getResources().getColor(R.color.action_bar_bg_50pct));
            mDismissRelative.setBackgroundColor(getResources().getColor(R.color.tab_bg_color_50pct));
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mListView.setLayoutManager(layoutManager);
        mImvAdapter = new ImvAdapter(getContext());
        mImvAdapter.setOnItemClickListener(this);
        mImvAdapter.setData(mImvList);
        mListView.setAdapter(mImvAdapter);
        mListView.addItemDecoration(new SpaceItemDecoration(getContext().getResources().getDimensionPixelSize(R.dimen.list_item_space)));
        return mView;
    }

    @Override
    public boolean onItemClick(EffectInfo effectInfo, int id) {
        if(mOnEffectChangeListener != null) {
            mEditorService.addTabEffect(UIEditorPage.MV, id);
            mEditorService.addTabEffect(UIEditorPage.AUDIO_MIX, 0);
            mOnEffectChangeListener.onEffectChange(effectInfo);
        }
        return true;
    }

    public void initResourceLocalWithSelectId(int id) {
        mImvList.clear();
        IMVForm imvForm = new IMVForm();
        mImvList.add(imvForm);
        List<FileDownloaderModel> modelsTemp = DownloaderManager.getInstance().getDbController().getResourceByType(EffectService.EFFECT_MV);
        ArrayList<IMVForm> resourceForms = new ArrayList<>();
        ArrayList<Integer> ids = new ArrayList<>();
        List<FileDownloaderModel> models = new ArrayList<>();
        if(modelsTemp != null && modelsTemp.size() > 0){
            for (FileDownloaderModel model : modelsTemp) {
                if (new File(model.getPath()).exists()) {
                    models.add(model);
                }
            }
            IMVForm form = null;
            ArrayList<AspectForm> pasterForms = null;
            for(FileDownloaderModel model :models){
                if(!ids.contains(model.getId())){
                    if(form != null){
                        form.setAspectList(pasterForms);
                        resourceForms.add(form);
                    }
                    ids.add(model.getId());
                    form = new IMVForm();
                    pasterForms = new ArrayList<>();
                    form.setId(model.getId());
                    form.setName(model.getName());
                    form.setKey(model.getKey());
                    form.setLevel(model.getLevel());
                    form.setTag(model.getTag());
                    form.setCat(model.getCat());
                    form.setIcon(model.getIcon());
                    form.setPreviewPic(model.getPreviewpic());
                    form.setPreviewMp4(model.getPreviewmp4());
                    form.setDuration(model.getDuration());
                    form.setType(model.getSubtype());
                }
                AspectForm pasterForm = addAspectForm(model);
                pasterForms.add(pasterForm);
            }
            if(form != null){
                form.setAspectList(pasterForms);
                resourceForms.add(form);
            }
        }
        mImvList.addAll(resourceForms);
        mImvAdapter.setData(mImvList);
        mImvAdapter.setSelectedPos(getIndexById(mEditorService.getEffectIndex(UIEditorPage.MV)));
        mListView.scrollToPosition(getIndexById(mEditorService.getEffectIndex(UIEditorPage.MV)));

        int index = -1;
        for(IMVForm resourceForm : mImvList){
            index ++;
            if(resourceForm.getId() == id){
                mImvAdapter.setEffecteffective(index);
                break;
            }
        }
    }

    private AspectForm addAspectForm(FileDownloaderModel model) {
        AspectForm aspectForm = new AspectForm();
        aspectForm.setAspect(model.getAspect());
        aspectForm.setDownload(model.getDownload());
        aspectForm.setMd5(model.getMd5());
        aspectForm.setPath(model.getPath());
        return aspectForm;
    }

    private int getIndexById(int id) {
        int index = 0;
        for(int i = 0; i< mImvList.size(); i++) {
            if(mImvList.get(i).getId() == id) {
                index = i;
            }
        }
        return index;
    }
}
