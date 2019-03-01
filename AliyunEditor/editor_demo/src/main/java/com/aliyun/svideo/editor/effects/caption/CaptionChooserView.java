package com.aliyun.svideo.editor.effects.caption;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.editor.PasterUICaptionImpl;
import com.aliyun.svideo.editor.editor.AbstractPasterUISimpleImpl;
import com.aliyun.svideo.editor.editor.PasterUITextImpl;
import com.aliyun.svideo.editor.effectmanager.MoreCaptionActivity;
import com.aliyun.svideo.editor.effects.control.BaseChooser;
import com.aliyun.svideo.editor.effects.control.EffectInfo;
import com.aliyun.svideo.editor.effects.control.OnItemClickListener;
import com.aliyun.svideo.editor.effects.control.SpaceItemDecoration;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.aliyun.downloader.DownloaderManager;
import com.aliyun.downloader.FileDownloaderModel;
import com.aliyun.svideo.sdk.external.struct.form.PasterForm;
import com.aliyun.svideo.sdk.external.struct.form.ResourceForm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cross_ly on 2018/8/27. <p>描述:
 */
public class CaptionChooserView extends BaseChooser {

    private static final int CAPTION_TYPE = 6;

    private RecyclerView mCategoryList;
    private CategoryAdapter mCategoryAdapter;
    private CaptionAdapter mCaptionAdapter;
    private ArrayList<ResourceForm> mCaptionData;
    private AsyncTask<Void, Void, List<FileDownloaderModel>> mLoadTask;

    public CaptionChooserView(@NonNull Context context) {
        this(context, null);
    }

    public CaptionChooserView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CaptionChooserView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.aliyun_svideo_caption_view, this);
        initTitleView(view);
        mCaptionData = new ArrayList<>();
        initListener();
        RecyclerView captionList = view.findViewById(R.id.effect_list);
        captionList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        captionList.addItemDecoration(
            new SpaceItemDecoration(getContext().getResources().getDimensionPixelSize(R.dimen.list_item_space)));
        mCaptionAdapter = new CaptionAdapter(getContext());
        mCaptionAdapter.setOnItemClickListener(mOnItemClickListener);
        captionList.setAdapter(mCaptionAdapter);
        mCategoryList = view.findViewById(R.id.category_list);
        mCategoryList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mCategoryAdapter = new CategoryAdapter(getContext());
        mCategoryAdapter.addShowFontCategory();
        mCategoryAdapter.setOnItemClickListener(mOnItemClickListener);
        mCategoryAdapter.setMoreClickListener(mOnMoreClickListener);
        mCategoryList.setAdapter(mCategoryAdapter);
        loadLocalPaster();
    }

    private void initTitleView(View view) {

        ImageView ivEffect = view.findViewById(R.id.iv_effect_icon);
        TextView tvTitle = view.findViewById(R.id.tv_effect_title);
        ivEffect.setImageResource(R.mipmap.aliyun_svideo_icon_caption);
        tvTitle.setText(R.string.subtitle_effect_manager);

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

    private void dealCancel() {
        if (mOnEffectActionLister != null) {
            mOnEffectActionLister.onCancel();
        }
    }

    @Override
    public boolean isPlayerNeedZoom() {
        return true;
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
        return pasterForm;
    }

    private void loadLocalPaster() {
        mLoadTask = new MyLoadAsyncTask();
        mLoadTask.execute();
    }

    /**
     * 获取本地资源
     *
     * @param id 选中资源的角标
     */
    public void initResourceLocalWithSelectId(int id, List<FileDownloaderModel> modelsTemp) {
        mCaptionData.clear();

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
        mCaptionData.addAll(resourceForms);
        ResourceForm form = new ResourceForm();
        form.setMore(true);
        mCaptionData.add(form);
        mCategoryAdapter.setData(mCaptionData);
        if (mCaptionData.size() == 1) {
            mCaptionAdapter.clearData();
        } else {
            int categoryIndex = 0;
            if (id == 0 || !ids.contains(id)) {
                //默认选中纯字体
                mCaptionAdapter.showFontData();
            } else if (ids.size() > 0) {
                //下载选中时
                for (ResourceForm resourceForm : mCaptionData) {
                    if (resourceForm.getId() == id) {
                        mCaptionAdapter.setData(resourceForm);
                        break;
                    }
                    categoryIndex++;
                }
            }
            mCategoryList.smoothScrollToPosition(categoryIndex);
            mCategoryAdapter.selectPosition(categoryIndex);
        }
    }

    public void setCurrResourceID(int id) {
        if (id != -1) {
            this.mCurrID = id;
        }
        loadLocalPaster();
    }

    /**
     * 点击更多
     */
    private CategoryAdapter.OnMoreClickListener mOnMoreClickListener;
    /**
     * 条目点击监听
     */
    private OnItemClickListener mOnItemClickListener;

    /**
     * 因为成员变量在构造方法之后被初始化，这里两个listener需要调用方法来初始化
     */
    private void initListener() {

        mOnMoreClickListener = new CategoryAdapter.OnMoreClickListener() {
            /**
             * startActivityForResult 结果接收在
             * @see com.aliyun.svideo.editor.editor.EditorActivity#onActivityResult(int, int, Intent)
             */
            @Override
            public void onMoreClick() {
                Intent moreIntent = new Intent(getContext(), MoreCaptionActivity.class);
                ((Activity) getContext()).startActivityForResult(moreIntent, CAPTION_REQUEST_CODE);
            }
        };

        mOnItemClickListener = new OnItemClickListener() {
            @Override
            public boolean onItemClick(EffectInfo effectInfo, int index) {
                if (effectInfo.isCategory) {
                    if (index == 0) {
                        //纯字体
                        mCaptionAdapter.showFontData();
                        mCurrID = 0;
                    } else {
                        ResourceForm resourceForm = mCaptionData.get(index);
                        mCurrID = resourceForm.getId();
                        mCaptionAdapter.setData(resourceForm);
                    }
                } else {
                    if (mOnEffectChangeListener != null) {
                        mOnEffectChangeListener.onEffectChange(effectInfo);
                    }
                }
                return true;
            }
        };

    }

    @Override
    protected UIEditorPage getUIEditorPage() {
        return UIEditorPage.CAPTION;
    }

    @Override
    protected void onRemove() {
        super.onRemove();
        if (mLoadTask != null) {
            mLoadTask.cancel(true);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dealCancel();
    }

    @Override
    public boolean isHostPaster(AbstractPasterUISimpleImpl uic) {

        return uic != null && (uic instanceof PasterUITextImpl || uic instanceof PasterUICaptionImpl);
    }

    /**
     * 加载本地资源的AsyncTask
     */
    private class MyLoadAsyncTask extends AsyncTask<Void, Void, List<FileDownloaderModel>> {

        @Override
        protected List<FileDownloaderModel> doInBackground(Void... voids) {
            return DownloaderManager.getInstance().getDbController().getResourceByType(CAPTION_TYPE);
        }

        @Override
        protected void onPostExecute(List<FileDownloaderModel> fileDownloaderModels) {
            super.onPostExecute(fileDownloaderModels);
            initResourceLocalWithSelectId(mCurrID, fileDownloaderModels);
        }
    }

}
