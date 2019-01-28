package com.aliyun.demo.effects.overlay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.demo.editor.AbstractPasterUISimpleImpl;
import com.aliyun.demo.editor.R;
import com.aliyun.demo.effectmanager.MorePasterActivity;
import com.aliyun.demo.effects.caption.CategoryAdapter;
import com.aliyun.demo.effects.control.BaseChooser;
import com.aliyun.demo.effects.control.EffectInfo;
import com.aliyun.demo.effects.control.OnItemClickListener;
import com.aliyun.demo.effects.control.UIEditorPage;
import com.aliyun.demo.http.EffectService;
import com.aliyun.downloader.DownloaderManager;
import com.aliyun.downloader.FileDownloaderModel;
import com.aliyun.svideo.base.widget.pagerecyclerview.PageIndicatorView;
import com.aliyun.svideo.base.widget.pagerecyclerview.PageRecyclerView;
import com.aliyun.svideo.sdk.external.struct.form.PasterForm;
import com.aliyun.svideo.sdk.external.struct.form.ResourceForm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cross_ly on 2018/8/27. <p>描述:动图
 */
public class OverlayChooserView extends BaseChooser {

    private RecyclerView mCategoryList;
    private CategoryAdapter mCategoryAdapter;

    private PageRecyclerView.PageAdapter mAdapter;
    private AbstractPageListCallback mPageListCallback;
    private AsyncTask<Void, Void, List<FileDownloaderModel>> mLoadTask;
    private ArrayList<ResourceForm> mPasterList;
    private List<PasterForm> mPageDataList;

    public OverlayChooserView(@NonNull Context context) {
        this(context, null);
    }

    public OverlayChooserView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OverlayChooserView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mCurrID = -1;
    }

    /**
     * 初始化
     */
    @Override
    protected void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.aliyun_svideo_overlay_view, this);
        initTitleView(view);
        mPasterList = new ArrayList<>();
        mPageDataList = new ArrayList<>();
        mCategoryList = view.findViewById(R.id.category_list);
        mCategoryList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mCategoryAdapter = new CategoryAdapter(getContext());
        mCategoryList.setAdapter(mCategoryAdapter);
        mCategoryAdapter.setData(mPasterList);

        mCategoryAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public boolean onItemClick(EffectInfo effectInfo, int index) {
                if (effectInfo.isCategory) {
                    ResourceForm resourceForm = mPasterList.get(index);
                    mCurrID = resourceForm.getId();
                    mPageListCallback.setData(resourceForm);
                    mPageDataList.clear();
                    mPageDataList.addAll(resourceForm.getPasterList());
                    mAdapter.realNotifyDataSetChanged();
                    mPageListCallback.resetSelected();
                }
                return true;
            }
        });
        mCategoryAdapter.setMoreClickListener(new CategoryAdapter.OnMoreClickListener() {
            /**
             * 下载更多动图
             * 接收方法
             * {@link com.aliyun.demo.editor.EditorActivity#onActivityResult(int, int, Intent)}
             */
            @Override
            public void onMoreClick() {
                Intent moreIntent = new Intent(getContext(), MorePasterActivity.class);
                ((Activity) getContext()).startActivityForResult(moreIntent, PASTER_REQUEST_CODE);
            }
        });
        PageRecyclerView recyclerView = view.findViewById(R.id.effect_overlay_view);
        recyclerView.setPageSize(1, 5);
        recyclerView.setPageMargin(30);
        recyclerView.setAutoScrollPage(false);
        PageIndicatorView indicator = view.findViewById(R.id.view_indicator);
        mPageListCallback = new AbstractPageListCallback(getContext()) {
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

        mAdapter = recyclerView.new PageAdapter(mPageDataList, mPageListCallback);
        recyclerView.setIndicator(indicator);
        mAdapter.setHasStableIds(true);
        recyclerView.setAdapter(mAdapter);
        loadLocalPaster();
    }

    private void initTitleView(View view) {

        ImageView ivEffect = view.findViewById(R.id.iv_effect_icon);
        TextView tvTitle = view.findViewById(R.id.tv_effect_title);
        ivEffect.setImageResource(R.mipmap.aliyun_svideo_icon_overlay);
        tvTitle.setText(R.string.motion_sticker_effect_manager);

        view.findViewById(R.id.iv_cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dealCancel();
            }
        });
        view.findViewById(R.id.iv_confirm).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnEffectActionLister != null) {
                    mOnEffectActionLister.onComplete();
                }
            }
        });
    }

    /**
     * 取消处理
     */
    private void dealCancel() {
        if (mOnEffectActionLister != null) {
            mOnEffectActionLister.onCancel();
        }
    }

    /**
     * 缩略图滑动条
     *
     * @return FrameLayout
     */
    @Override
    protected FrameLayout getThumbContainer() {
        return findViewById(R.id.fl_thumblinebar);
    }

    /**
     * 加载本地paster素材（module层）
     */
    public void loadLocalPaster() {

        mLoadTask = new MyLoadAsyncTask();
        mLoadTask.execute();
    }

    public void initResourceLocalWithSelectId(int id, List<FileDownloaderModel> modelsTemp) {
        mPasterList.clear();
        mPageListCallback.resetSelected();
        ArrayList<ResourceForm> resourceForms = new ArrayList<>();
        ArrayList<Integer> ids = new ArrayList<>();
        List<FileDownloaderModel> models = new ArrayList<>();
        if (modelsTemp != null && modelsTemp.size() > 0) {
            for (FileDownloaderModel model : modelsTemp) {
                if (new File(model.getPath()).exists()) {
                    models.add(model);
                }
            }
            ResourceForm form = null;
            ArrayList<PasterForm> pasterForms = null;
            for (FileDownloaderModel model : models) {
                if (!ids.contains(model.getId())) {
                    if (form != null) {
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
            if (form != null) {
                form.setPasterList(pasterForms);
                resourceForms.add(form);
            }
        }
        mPasterList.addAll(resourceForms);
        ResourceForm form = new ResourceForm();
        form.setMore(true);
        mPasterList.add(form);
        mCategoryAdapter.setData(mPasterList);
        if (ids.size() > 0 && (id == -1 ||id == 0 || !ids.contains(id)) ) {
            id = ids.get(0);
        }
        int categoryIndex = 0;
        for (ResourceForm resourceForm : mPasterList) {
            if (resourceForm.getId() == id) {
                mPageDataList.clear();
                if (resourceForm.getPasterList() != null) {
                    mPageDataList.addAll(resourceForm.getPasterList());
                }
                mPageListCallback.setData(resourceForm);
                mAdapter.realNotifyDataSetChanged();
                break;
            }
            categoryIndex++;
        }
        mCategoryList.smoothScrollToPosition(categoryIndex);
        mCategoryAdapter.selectPosition(categoryIndex);
        if (mCurrID == -1){
            loadLocalPaster();
            mCurrID = 0;
        }
        Log.d("TAG", "categoryIndex :" +categoryIndex);
    }

    public void setCurrResourceID(int id) {
        if (id != -1) {
            this.mCurrID = id;
        }
        loadLocalPaster();
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
    public boolean isHostPaster(AbstractPasterUISimpleImpl uic) {
        return uic != null && uic.getEditorPage() == UIEditorPage.OVERLAY;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dealCancel();
    }

    @Override
    protected void onRemove() {
        super.onRemove();
        if (mLoadTask != null) {
            mLoadTask.cancel(true);
        }
    }

    @Override
    protected UIEditorPage getUIEditorPage() {
        return UIEditorPage.OVERLAY;
    }

    @Override
    public boolean isPlayerNeedZoom() {
        return true;
    }

    /**
     * 加载本地资源的AsyncTask 优化到基类使用 {@link java.lang.ref.WeakReference}处理
     */
    private class MyLoadAsyncTask extends AsyncTask<Void, Void, List<FileDownloaderModel>> {

        @Override
        protected List<FileDownloaderModel> doInBackground(Void... voids) {
            return DownloaderManager.getInstance().getDbController().getResourceByType(EffectService.EFFECT_PASTER);
        }

        @Override
        protected void onPostExecute(List<FileDownloaderModel> fileDownloaderModels) {
            super.onPostExecute(fileDownloaderModels);
            initResourceLocalWithSelectId(mCurrID, fileDownloaderModels);
        }
    }
}
