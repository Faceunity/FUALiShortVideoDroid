/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.effects.caption;

import android.content.Intent;
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
import com.aliyun.demo.effectmanager.MoreCaptionActivity;
import com.aliyun.demo.effects.control.BaseChooser;
import com.aliyun.demo.effects.control.EffectInfo;
import com.aliyun.demo.effects.control.OnDialogButtonClickListener;
import com.aliyun.demo.effects.control.OnItemClickListener;
import com.aliyun.demo.effects.control.SpaceItemDecoration;
import com.aliyun.demo.effects.control.UIEditorPage;
import com.aliyun.downloader.DownloaderManager;
import com.aliyun.downloader.FileDownloaderModel;
import com.aliyun.struct.form.PasterForm;
import com.aliyun.struct.form.ResourceForm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class CaptionChooserMediator extends BaseChooser implements OnItemClickListener, CategoryAdapter.OnMoreClickListener {

    private static final int CAPTION_TYPE = 6;

    private RecyclerView mCaptionList;
    private RecyclerView mCategoryList;
    private CategoryAdapter mCategoryAdapter;
    private CaptionAdapter mCaptionAdapter;
    private ArrayList<ResourceForm> mCaptionData = new ArrayList<>();
    private OnDialogButtonClickListener mDialogButtonClickListener;
    private ImageView mIvCancel;
    private RelativeLayout mDismissRelative;


    public static CaptionChooserMediator newInstance(){
        CaptionChooserMediator dialog = new CaptionChooserMediator();
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = LayoutInflater.from(getActivity()).inflate(R.layout.aliyun_svideo_caption_view, container);
        mDismiss = (ImageView) mView.findViewById(R.id.dismiss);
        mDismiss.setOnClickListener(onClickListener);
        mDismissRelative = (RelativeLayout) mView.findViewById(R.id.caption_dismiss);
        mIvCancel = (ImageView) mView.findViewById(R.id.cancel);
        mIvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mDialogButtonClickListener != null){
                    mDialogButtonClickListener.onNegativeClickListener(UIEditorPage.CAPTION.index());
                }
            }
        });
        mCaptionList = (RecyclerView) mView.findViewById(R.id.effect_list);
        mCaptionList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mCaptionList.addItemDecoration(new SpaceItemDecoration(getContext().getResources().getDimensionPixelSize(R.dimen.list_item_space)));
        if(mEditorService != null && mEditorService.isFullScreen()) {
            mCaptionList.setBackgroundColor(getResources().getColor(R.color.action_bar_bg_50pct));
            mDismissRelative.setBackgroundColor(getResources().getColor(R.color.tab_bg_color_50pct));
        }
        mCaptionAdapter = new CaptionAdapter(getActivity());
        mCaptionAdapter.setOnItemClickListener(this);
        mCaptionList.setAdapter(mCaptionAdapter);
        mCategoryList = (RecyclerView) mView.findViewById(R.id.category_list);
        mCategoryList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mCategoryList.addItemDecoration(new SpaceItemDecoration(getContext().getResources().getDimensionPixelSize(R.dimen.list_item_space)));
        mCategoryAdapter = new CategoryAdapter(getActivity());
        mCategoryAdapter.setOnItemClickListener(this);
        mCategoryAdapter.setMoreClickListener(this);
        mCategoryList.setAdapter(mCategoryAdapter);
        initResourceLocal();
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private PasterForm addPasterForm(FileDownloaderModel model){
        PasterForm pasterForm = new PasterForm();
        pasterForm.setPreviewUrl(model.getSubpreview());
        pasterForm.setSort(model.getSubsort());
        pasterForm.setId(model.getSubid());
        pasterForm.setFontId(model.getFontid());
        pasterForm.setMD5(model.getMd5());
        pasterForm.setType(model.getSubtype());
        pasterForm.setIcon(model.getSubicon());
        pasterForm.setDownloadUrl(model.getUrl());
        pasterForm.setName(model.getSubname());
        return pasterForm;
    }

    private void initResourceLocal(){
        initResourceLocalWithSelectId(0);
    }

    public void initResourceLocalWithSelectId(int id) {
        mCaptionData.clear();
        List<FileDownloaderModel> modelsTemp = DownloaderManager.getInstance().getDbController().getResourceByType(CAPTION_TYPE);
        ArrayList<ResourceForm> resourceForms = new ArrayList<>();
        ArrayList<Integer> ids = new ArrayList<>();
        List<FileDownloaderModel> models = new ArrayList<>();
        if(modelsTemp != null && modelsTemp.size() > 0){
            for(FileDownloaderModel model : modelsTemp) {
                if(new File(model.getPath()).exists()) {
                    models.add(model);
                }
            }
            ResourceForm form = null;
            ArrayList<PasterForm> pasterForms = null;
            for(FileDownloaderModel model :models){
                if(!ids.contains(model.getId())){
                    if(form != null){
                        form.setPasterList(pasterForms);
                        resourceForms.add(form);
                    }
                    ids.add(model.getId());
                    form = new ResourceForm();
                    pasterForms = new ArrayList<>();
                    form.setPreviewUrl(model.getPreview());
                    form.setIcon(model.getIcon());
                    form.setLevel(model.getLevel());
                    form.setName(model.getName());
                    form.setId(model.getId());
                    form.setDescription(model.getDescription());
                    form.setSort(model.getSort());
                    form.setIsNew(model.getIsnew());
                }
                PasterForm pasterForm = addPasterForm(model);
                pasterForms.add(pasterForm);
            }
            if(form != null){
                form.setPasterList(pasterForms);
                resourceForms.add(form);
            }
        }
        mCaptionData.addAll(resourceForms);
        ResourceForm form = new ResourceForm();
        form.setMore(true);
        mCaptionData.add(form);
        mCategoryAdapter.setData(mCaptionData);
        if(mCaptionData.size() == 1){
            mCaptionAdapter.clearData();
        }else{
            if(id == 0 && ids.size() >0){
                id = ids.get(0);
            }
            int categoryIndex = 0;
            for(ResourceForm resourceForm : mCaptionData){
                if(resourceForm.getId() == id){
                    mCaptionAdapter.setData(resourceForm);
                    break;
                }
                categoryIndex ++;
            }
            mCategoryList.smoothScrollToPosition(categoryIndex);
            mCategoryAdapter.selectPosition(categoryIndex);
        }
    }

    @Override
    public boolean onItemClick(EffectInfo effectInfo, int index) {
        if(effectInfo.isCategory){
            ResourceForm resourceForm =  mCaptionData.get(index);
            mCaptionAdapter.setData(resourceForm);
        }else{
            if(mOnEffectChangeListener != null) {
                mOnEffectChangeListener.onEffectChange(effectInfo);
            }
        }
        return true;
    }

    public void setDialogButtonClickListener(OnDialogButtonClickListener dialogButtonClickListener) {
        mDialogButtonClickListener = dialogButtonClickListener;
    }

    @Override
    public void onMoreClick() {
        Intent moreIntent = new Intent(getActivity(), MoreCaptionActivity.class);
        getActivity().startActivityForResult(moreIntent,CaptionChooserMediator.CAPTION_REQUEST_CODE);
    }
}
