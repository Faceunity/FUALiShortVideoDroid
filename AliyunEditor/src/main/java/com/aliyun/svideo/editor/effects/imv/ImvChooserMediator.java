/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.imv;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.aliyun.svideo.downloader.DownloaderManager;
import com.aliyun.svideo.downloader.FileDownloaderModel;
import com.aliyun.svideo.base.http.EffectService;
import com.aliyun.svideo.base.Form.AspectForm;
import com.aliyun.svideo.base.Form.IMVForm;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.control.BaseChooser;
import com.aliyun.svideo.editor.effects.control.EditorService;
import com.aliyun.svideo.editor.effects.control.EffectInfo;
import com.aliyun.svideo.editor.effects.control.OnItemClickListener;
import com.aliyun.svideo.editor.effects.control.SpaceItemDecoration;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * MV选择 dialog view
 */
public class ImvChooserMediator extends BaseChooser implements OnItemClickListener {
    private RecyclerView mListView;
    private ImvAdapter mImvAdapter;
    private TextView mTvEffectTitle;
    List<IMVForm> mImvList ;
    private int currentId;

    public ImvChooserMediator(@NonNull Context context) {
        this(context, null);
    }

    public ImvChooserMediator(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImvChooserMediator(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mEditorService == null) {
            mEditorService = new EditorService();
        }
        initResourceLocalWithSelectId(-1);
    }

    @Override
    protected void init() {
        mImvList = new ArrayList<>();
        IMVForm imvForm = new IMVForm();
        mImvList.add(imvForm);
        LayoutInflater.from(getContext()).inflate(R.layout.alivc_editor_view_chooser_mv, this);
        mListView = findViewById(R.id.effect_list_filter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mListView.setLayoutManager(layoutManager);
        mImvAdapter = new ImvAdapter(getContext());
        mImvAdapter.setOnItemClickListener(this);
        mImvAdapter.setData(mImvList);
        mListView.setAdapter(mImvAdapter);
        mListView.addItemDecoration(new SpaceItemDecoration(getContext().getResources().getDimensionPixelSize(R.dimen.list_item_space)));
        mTvEffectTitle = findViewById(R.id.effect_title_tv);
        mTvEffectTitle.setText(R.string.alivc_editor_effect_mv);
        Drawable top = getContext().getResources().getDrawable(R.mipmap.alivc_svideo_icon_tab_mv);
        top.setBounds(0, 0, top.getMinimumWidth(), top.getMinimumHeight());
        mTvEffectTitle.setCompoundDrawables(top, null, null, null );
    }


    @Override
    public boolean onItemClick(EffectInfo effectInfo, int id) {
        if (mOnEffectChangeListener != null) {
            mEditorService.addTabEffect(UIEditorPage.MV, id);
            mEditorService.addTabEffect(UIEditorPage.AUDIO_MIX, 0);
            mOnEffectChangeListener.onEffectChange(effectInfo);
            mImvAdapter.notifyDataSetChanged();
        }
        return true;
    }

    public void setCurrResourceID(int id) {
        if (id != -1) {
            this.mCurrID = id;
        }
        initResourceLocalWithSelectId(mCurrID);
    }
    public void initResourceLocalWithSelectId(int id) {

        mImvList.clear();
        IMVForm imvForm = new IMVForm();
        mImvList.add(imvForm);
        List<FileDownloaderModel> modelsTemp = DownloaderManager.getInstance().getDbController().getResourceByType(EffectService.EFFECT_MV);
        ArrayList<IMVForm> resourceForms = new ArrayList<>();
        ArrayList<Integer> ids = new ArrayList<>();
        List<FileDownloaderModel> models = new ArrayList<>();
        if (modelsTemp != null && modelsTemp.size() > 0) {
            for (FileDownloaderModel model : modelsTemp) {
                if (new File(model.getPath()).exists()) {
                    models.add(model);
                }
            }
            IMVForm form = null;
            ArrayList<AspectForm> pasterForms = null;
            for (FileDownloaderModel model : models) {
                if (!ids.contains(model.getId())) {
                    if (form != null) {
                        form.setAspectList(pasterForms);
                        resourceForms.add(form);
                    }
                    ids.add(model.getId());
                    form = new IMVForm();
                    pasterForms = new ArrayList<>();
                    form.setId(model.getId());
                    form.setName(model.getName());
                    form.setNameEn(model.getNameEn());
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
            if (form != null) {
                form.setAspectList(pasterForms);
                resourceForms.add(form);
            }
        }
        mImvList.addAll(resourceForms);
        mImvAdapter.setData(mImvList);
        mImvAdapter.setSelectedPos(getIndexById(mEditorService.getEffectIndex(UIEditorPage.MV)));
        mListView.scrollToPosition(getIndexById(mEditorService.getEffectIndex(UIEditorPage.MV)));

        int index = -1;
        for (IMVForm resourceForm : mImvList) {
            index ++;
            if (resourceForm.getId() == id) {
                mImvAdapter.setEffecteffectiveAndNotify(index);
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
        for (int i = 0; i < mImvList.size(); i++) {
            if (mImvList.get(i).getId() == id) {
                index = i;
            }
        }
        return index;
    }

    @Override
    public boolean isPlayerNeedZoom() {
        return false;
    }
    @Override
    public boolean isShowSelectedView() {
        return false;
    }
}
