/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.effects.overlay;

import android.content.Intent;
import android.os.AsyncTask;
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
import com.aliyun.demo.effectmanager.EffectLoader;
import com.aliyun.demo.effectmanager.MorePasterActivity;
import com.aliyun.demo.effects.caption.CategoryAdapter;
import com.aliyun.demo.effects.control.BaseChooser;
import com.aliyun.demo.effects.control.EffectInfo;
import com.aliyun.demo.effects.control.OnDialogButtonClickListener;
import com.aliyun.demo.effects.control.OnItemClickListener;
import com.aliyun.demo.effects.control.SpaceItemDecoration;
import com.aliyun.demo.effects.control.UIEditorPage;
import com.aliyun.demo.http.EffectService;
import com.aliyun.downloader.DownloaderManager;
import com.aliyun.downloader.FileDownloaderModel;
import com.aliyun.quview.pagerecyclerview.PageIndicatorView;
import com.aliyun.quview.pagerecyclerview.PageRecyclerView;
import com.aliyun.struct.form.PasterForm;
import com.aliyun.struct.form.ResourceForm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class OverlayChooserMediator extends BaseChooser implements OnItemClickListener,
        CategoryAdapter.OnMoreClickListener, View.OnClickListener{
    private PageRecyclerView mRecyclerView;
    private RecyclerView mCategoryList;
    private CategoryAdapter mCategoryAdapter;
    private ImageView mIvCancel;
    private RelativeLayout mDissRelative;

    private PageRecyclerView.PageAdapter mAdapter;
    private PageListCallback mPageListCallback;
    private PageIndicatorView mIndicator;
    private AsyncTask<Void, Void, List<FileDownloaderModel>> mLoadTask;
    private EffectLoader mPasterLoader = new EffectLoader();
    private ArrayList<ResourceForm> mPasterList = new ArrayList<>();
    private List<PasterForm> mPageDataList = new ArrayList<>();
    private OnDialogButtonClickListener mDialogButtonClickListener;
    private int mCurrID = 0;



//    List<FileDownloaderModel> mList = new ArrayList<>();

    public static OverlayChooserMediator newInstance() {
        OverlayChooserMediator dialog = new OverlayChooserMediator();
//        Bundle args = new Bundle();
//        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPasterLoader.init(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = LayoutInflater.from(getActivity()).inflate(R.layout.aliyun_svideo_overlay_view, container);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCategoryList = (RecyclerView) view.findViewById(R.id.category_list);
        mDissRelative = (RelativeLayout) view.findViewById(R.id.overlay_dismiss);
        mCategoryList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mCategoryList.addItemDecoration(new SpaceItemDecoration(getContext().getResources().getDimensionPixelSize(R.dimen.list_item_space)));
        mCategoryAdapter = new CategoryAdapter(getActivity());
        mCategoryAdapter.setOnItemClickListener(this);
        mCategoryList.setAdapter(mCategoryAdapter);
        mCategoryAdapter.setData(mPasterList);
        mCategoryAdapter.setMoreClickListener(this);
        mRecyclerView = (PageRecyclerView) view.findViewById(R.id.effect_overlay_view);
        mRecyclerView.setPageSize(2, 5);
        mRecyclerView.setPageMargin(30);
        mIndicator = (PageIndicatorView) view.findViewById(R.id.view_indicator);
        if(mEditorService != null && mEditorService.isFullScreen()) {
            mRecyclerView.setBackgroundColor(getResources().getColor(R.color.action_bar_bg_50pct));
            mDissRelative.setBackgroundColor(getResources().getColor(R.color.tab_bg_color_50pct));
        }
        mPageListCallback = new PageListCallback(getContext()) {
            @Override
            public void notifySelected(int selectedPos, int prePos) {
                mAdapter.realNotifyItemChanged(selectedPos);
                if (prePos >= 0) {
                    mAdapter.realNotifyItemChanged(prePos);
                }
                PasterForm pasterForm = mPageListCallback.getSelectedItem();
                EffectInfo effectInfo = new EffectInfo();
                effectInfo.type = UIEditorPage.OVERLAY;
                effectInfo.setPath(pasterForm.getPath());
                if (mOnEffectChangeListener != null) {
                    mOnEffectChangeListener.onEffectChange(effectInfo);
                }
            }
        };

//        mPageListCallback.setData(mList);
        mAdapter = mRecyclerView.new PageAdapter(mPageDataList, mPageListCallback);
        mRecyclerView.setIndicator(mIndicator);
        mAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mAdapter);
        mDismiss = (ImageView) view.findViewById(R.id.dismiss);
        mDismiss.setOnClickListener(onClickListener);
        mIvCancel = (ImageView) view.findViewById(R.id.cancel);
        mIvCancel.setOnClickListener(this);
    }

    public void loadLocalPaster() {
        mLoadTask = new AsyncTask<Void, Void, List<FileDownloaderModel>>() {
            @Override
            protected List<FileDownloaderModel> doInBackground(Void... params) {
                List<FileDownloaderModel> models = DownloaderManager.getInstance().getDbController().getResourceByType(EffectService.EFFECT_PASTER);
                return models;
            }

            @Override
            protected void onPostExecute(List<FileDownloaderModel> fileDownloaderModels) {
                super.onPostExecute(fileDownloaderModels);
                initResourceLocalWithSelectId(mCurrID);
            }
        };
        mLoadTask.execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        loadLocalPaster();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mLoadTask != null) {
            mLoadTask.cancel(true);
        }
    }

    @Override
    public boolean onItemClick(EffectInfo effectInfo, int index) {
        if (effectInfo.isCategory) {
            ResourceForm resourceForm = mPasterList.get(index);
            mPageListCallback.setData(resourceForm);
            mPageDataList.clear();
            mPageDataList.addAll(resourceForm.getPasterList());
            mAdapter.realNotifyDataSetChanged();
            mPageListCallback.resetSelected();
        }
        return true;
    }

    private PasterForm addPasterForm(FileDownloaderModel model) {
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
        pasterForm.setPath(model.getPath());
        return pasterForm;
    }

    @Override
    public void onMoreClick() {
        Intent moreIntent = new Intent(getActivity(), MorePasterActivity.class);
        getActivity().startActivityForResult(moreIntent, PASTER_REQUEST_CODE);
    }

    public void setCurrResourceID(int id) {
        this.mCurrID = id;
    }


    public void initResourceLocalWithSelectId(int id) {
        mPasterList.clear();
        mPageListCallback.resetSelected();
        List<FileDownloaderModel> modelsTemp = DownloaderManager.getInstance().getDbController().getResourceByType(EffectService.EFFECT_PASTER);
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
        mPasterList.addAll(resourceForms);
        ResourceForm form = new ResourceForm();
        form.setMore(true);
        mPasterList.add(form);
        mCategoryAdapter.setData(mPasterList);
        if(id == 0 && ids.size() >0){
            id = ids.get(0);
        }
        int categoryIndex = 0;
        for(ResourceForm resourceForm : mPasterList){
            if(resourceForm.getId() == id){
                mPageDataList.clear();
                if(resourceForm.getPasterList() != null) {
                    mPageDataList.addAll(resourceForm.getPasterList());
                }
                mPageListCallback.setData(resourceForm);
                mAdapter.realNotifyDataSetChanged();
                break;
            }
            categoryIndex ++;
        }
        mCategoryList.smoothScrollToPosition(categoryIndex);
        mCategoryAdapter.selectPosition(categoryIndex);
    }

    public void setDialogButtonClickListener(OnDialogButtonClickListener dialogButtonClickListener) {
        mDialogButtonClickListener = dialogButtonClickListener;
    }

    @Override
    public void onClick(View v) {
        if(v == mIvCancel && mDialogButtonClickListener != null) {
            mDialogButtonClickListener.onNegativeClickListener(UIEditorPage.OVERLAY.index());
        }
    }
}
