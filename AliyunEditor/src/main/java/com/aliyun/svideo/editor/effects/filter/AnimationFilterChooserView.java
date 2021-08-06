package com.aliyun.svideo.editor.effects.filter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.aliyun.svideo.base.EffectParamsAdjustView;
import com.aliyun.svideo.base.Form.ResourceForm;
import com.aliyun.svideo.base.http.EffectService;
import com.aliyun.svideo.base.utils.FastClickUtil;
import com.aliyun.svideo.downloader.DownloaderManager;
import com.aliyun.svideo.downloader.FileDownloaderModel;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effectmanager.MoreAnimationEffectActivity;
import com.aliyun.svideo.editor.effects.CategoryAdapter;
import com.aliyun.svideo.editor.effects.control.BaseChooser;
import com.aliyun.svideo.editor.effects.control.EffectInfo;
import com.aliyun.svideo.editor.effects.control.OnItemClickListener;
import com.aliyun.svideo.editor.effects.control.OnItemTouchListener;
import com.aliyun.svideo.editor.effects.control.SpaceItemDecoration;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.aliyun.svideo.editor.msg.Dispatcher;
import com.aliyun.svideo.editor.msg.body.ClearAnimationFilter;
import com.aliyun.svideo.editor.msg.body.ConfirmAnimationFilter;
import com.aliyun.svideo.editor.msg.body.DeleteLastAnimationFilter;
import com.aliyun.svideo.editor.msg.body.FilterTabClick;
import com.aliyun.svideo.editor.msg.body.LongClickAnimationFilter;
import com.aliyun.svideo.editor.msg.body.LongClickUpAnimationFilter;
import com.aliyun.svideo.editor.util.EditorCommon;
import com.aliyun.svideo.common.utils.DensityUtils;
import com.aliyun.svideosdk.common.struct.effect.EffectConfig;
import com.aliyun.svideosdk.common.struct.effect.EffectFilter;
import com.aliyun.svideosdk.common.struct.effect.ValueTypeEnum;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AnimationFilterChooserView extends BaseChooser
    implements OnItemClickListener, OnItemTouchListener, View.OnClickListener {
    private RecyclerView mListView;
    private FrameLayout mFlThumblinebar;
    private EffectAdapter mFilterAdapter;
    private ImageView mCancel;
    private TextView mTvEffectTitle;
    private ImageView mIvEffectIcon;
    private ImageView mComplete;
    private boolean isFirstShow;

    private ArrayList<ResourceForm> mFilterList4Category;
    private RecyclerView mCategoryList;
    private CategoryAdapter mCategoryAdapter;
    private AsyncTask<Void, Void, List<FileDownloaderModel>> mLoadTask;
    private EffectParamsAdjustView mParamsAdjustView;
    private EffectFilter mCurrentEffect;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Dispatcher.getInstance().postMsg(new FilterTabClick(FilterTabClick.POSITION_ANIMATION_FILTER));

        if (isFirstShow) {
            View contentView = LayoutInflater.from(getContext()).inflate(R.layout.alivc_editor_view_tip_first_show, null, false);
            PopupWindow window = new PopupWindow( contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            window.setContentView(contentView);
            window.setOutsideTouchable(true);
            // 设置PopupWindow的背景
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int yoff = 0 - getContext().getResources().getDimensionPixelSize(R.dimen.alivc_editor_size_effect_list_view) - DensityUtils
                       .dip2px(getContext(), 25 );
            int xoff = DensityUtils.dip2px(getContext(), 5);
            window.showAsDropDown(mListView, xoff, yoff);
            isFirstShow = false;
        }
    }


    public AnimationFilterChooserView(@NonNull Context context) {
        this(context, null);
    }

    public AnimationFilterChooserView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimationFilterChooserView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onItemClick(EffectInfo effectInfo, int index) {
        if (index == 0) {
            mParamsAdjustView.setVisibility(GONE);
            //删除最后一次添加效果
            Dispatcher.getInstance().postMsg(new DeleteLastAnimationFilter());
        }
        return false;
    }
    @Override
    public void onTouchEvent(int motionEvent, int index, EffectInfo info) {
        switch (motionEvent) {
        case OnItemTouchListener.EVENT_UP:
            //先判断缩略图是否在被拖动状态，如果在被拖动状态则不能添加特效
            if (mThumbLineBar != null && !mThumbLineBar.isTouching()) {
                //添加特效结束时，恢复缩略图滑动事件
                //setThumbScrollEnable(true);
                setClickable(true);
                Dispatcher.getInstance().postMsg(new LongClickUpAnimationFilter.Builder()
                                                 .effectInfo(info)
                                                 .index(index)
                                                 .effectConfig(copyEffectConfig()).build());
            }
            break;
        case OnItemTouchListener.EVENT_DOWN:
            if (index > 0) {
                //先判断缩略图是否在被拖动状态，如果在被拖动状态则不能添加特效
                if (mThumbLineBar != null && !mThumbLineBar.isTouching()) {
                    //添加特效开始时，关闭特效界面点击
                    //setThumbScrollEnable(false);
                    setClickable(false);
                    info.streamStartTime = mPlayerListener.getCurrDuration();
                    if (mCurrentEffect == null || !info.getPath().equals(mCurrentEffect.getPath())) {
                        mCurrentEffect = new EffectFilter(info.getPath());
                    }
                    showEffectParamsUI(mCurrentEffect);
                    Dispatcher.getInstance().postMsg(new LongClickAnimationFilter.Builder()
                                                     .effectInfo(info)
                                                     .index(index)
                                                     .effectConfig(copyEffectConfig()).build());
                }




            }
            break;
        default:
            break;
        }


    }


    public EffectConfig copyEffectConfig() {

        EffectFilter effectFilter = new EffectFilter(mCurrentEffect.getPath());
        mCurrentEffect.copy(effectFilter);
        return effectFilter.getEffectConfig();
    }

    @Override
    public void onClick(View v) {
        if (FastClickUtil.isFastClick()) {
            return;
        }

        if (v == mComplete) {
            Dispatcher.getInstance().postMsg(new ConfirmAnimationFilter());
            if (mOnEffectActionLister != null) {
                mOnEffectActionLister.onComplete();
            }
        } else if (v == mCancel) {
            onBackPressed();
        }

    }
    @Override
    protected void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.alivc_editor_view_chooser_animation_filter, this);
        mListView = (RecyclerView) findViewById(R.id.effect_list_filter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mListView.setLayoutManager(layoutManager);
        mFlThumblinebar = findViewById(R.id.fl_thumblinebar);
        mFilterAdapter = new EffectAdapter(getContext());
        mFilterAdapter.setOnItemClickListener(this);
        mFilterAdapter.setOnItemTouchListener(this);
        mListView.setAdapter(mFilterAdapter);
        mListView.addItemDecoration(new SpaceItemDecoration(getContext().getResources().getDimensionPixelSize(R.dimen.list_item_space)));
        mCancel = (ImageView) findViewById(R.id.cancel);
        mTvEffectTitle = (TextView) findViewById(R.id.tv_effect_title);
        mIvEffectIcon = (ImageView) findViewById(R.id.iv_effect_icon);
        mComplete = (ImageView) findViewById(R.id.complete);
        mIvEffectIcon.setImageResource(R.mipmap.alivc_svideo_effect);
        mTvEffectTitle.setText(R.string.alivc_editor_dialog_animate_tittle);
        mComplete.setOnClickListener(this);
        mCancel.setOnClickListener(this);

        mFilterList4Category = new ArrayList<>();
        mCategoryList = view.findViewById(R.id.effect_category_view);
        mCategoryList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mCategoryAdapter = new CategoryAdapter(getContext());
        mCategoryList.setAdapter(mCategoryAdapter);
        mCategoryAdapter.setData(mFilterList4Category);

        mCategoryAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public boolean onItemClick(EffectInfo effectInfo, int index) {
                if (effectInfo.isCategory && mFilterList4Category.size() > index) {
                    ResourceForm resourceForm = mFilterList4Category.get(index);
                    mCurrID = resourceForm.getId();
                    changeCategoryDir(resourceForm);
                    mParamsAdjustView.setVisibility(GONE);
                }
                return true;
            }
        });
        mCategoryAdapter.setMoreClickListener(new CategoryAdapter.OnMoreClickListener() {

            @Override
            public void onMoreClick() {
                Intent moreIntent = new Intent(getContext(), MoreAnimationEffectActivity.class);
                ((Activity) getContext()).startActivityForResult(moreIntent, ANIMATION_FILTER_REQUEST_CODE);
            }
        });

        mParamsAdjustView = view.findViewById(R.id.params_effect_view);
        loadLocalAnimationFilter();
    }

    /**
     * 显示参数调节ui，目前只提供{@link ValueTypeEnum#INT,ValueTypeEnum#FLOAT}两种类型
     * @param ef EffectFilter
     */
    private boolean showEffectParamsUI(final EffectFilter ef) {
        List<EffectConfig.NodeBean> nodeTree = ef.getNodeTree();
        List<EffectConfig.NodeBean.Params> paramsList = new ArrayList<>();
        if (nodeTree == null || nodeTree.size() == 0) {
            mParamsAdjustView.setVisibility(GONE);
            return false;
        }
        for (EffectConfig.NodeBean nodeBean : nodeTree) {
            List<EffectConfig.NodeBean.Params> params = nodeBean.getParams();
            if (params == null || params.size() == 0) {
                continue;
            }
            for (EffectConfig.NodeBean.Params param : params) {
                ValueTypeEnum type = param.getType();
                if (type == ValueTypeEnum.INT || type == ValueTypeEnum.FLOAT) {
                    //当前只调节INT和FLOAT类型参数
                    paramsList.add(param);
                }
            }
        }
        if (paramsList.size() == 0) {
            mParamsAdjustView.setVisibility(GONE);
        } else {
            mParamsAdjustView.setVisibility(VISIBLE);
            mParamsAdjustView.setData(paramsList);
        }
        return paramsList.size() != 0;
    }

    @Override
    protected UIEditorPage getUIEditorPage() {
        return UIEditorPage.FILTER_EFFECT;
    }

    @Override
    public void onBackPressed() {
        Dispatcher.getInstance().postMsg(new ClearAnimationFilter());
        if (mOnEffectActionLister != null) {
            mOnEffectActionLister.onCancel();
        }
    }

    public void setFirstShow(boolean firstShow) {
        isFirstShow = firstShow;
    }

    @Override
    public boolean isPlayerNeedZoom() {
        return true;
    }
    @Override
    protected FrameLayout getThumbContainer() {
        return mFlThumblinebar;
    }

    public void loadLocalAnimationFilter() {

        mLoadTask = new AnimationFilterChooserView.MyLoadAsyncTask();
        mLoadTask.execute();
    }

    public void initResourceLocalWithSelectId(int id, List<FileDownloaderModel> downloadmodels) {

        mFilterList4Category = new ArrayList<>();
        ArrayList<Integer> ids = new ArrayList<>();

        //添加一个默认资源
        FileDownloaderModel extFile = new FileDownloaderModel();
        extFile.setPath(EditorCommon.QU_DIR + EditorCommon.QU_ANIMATION_FILTER);
        extFile.setNameEn("default");
        extFile.setName("默认");
        extFile.setId(mFilterList4Category.size());
        downloadmodels.add(0, extFile);

        if (downloadmodels.size() > 0) {
            for (FileDownloaderModel model : downloadmodels) {
                if (new File(model.getPath()).exists()) {
                    if (!ids.contains(model.getId())) {
                        ids.add(model.getId());
                        ResourceForm form = new ResourceForm();
                        form.setPreviewUrl(model.getPreview());
                        form.setIcon(model.getIcon());
                        form.setLevel(model.getLevel());
                        form.setName(model.getName());
                        form.setNameEn(model.getNameEn());
                        form.setId(model.getId());
                        form.setDescription(model.getDescription());
                        form.setSort(model.getSort());
                        form.setIsNew(model.getIsnew());
                        form.setPath(model.getPath());
                        mFilterList4Category.add(form);
                    }
                }
            }
        }

        ResourceForm form = new ResourceForm();
        form.setMore(true);
        mFilterList4Category.add(form);
        mCategoryAdapter.setData(mFilterList4Category);
        if (ids.size() > 0 && (id == -1 || id == 0 || !ids.contains(id)) ) {
            id = ids.get(0);
        }
        int categoryIndex = 0;
        for (ResourceForm resourceForm : mFilterList4Category) {
            if (resourceForm.getId() == id) {
                changeCategoryDir(resourceForm);
                break;
            }
            categoryIndex++;
        }
        mCategoryList.smoothScrollToPosition(categoryIndex);
        mCategoryAdapter.selectPosition(categoryIndex);
        if (mCurrID == -1) {
            loadLocalAnimationFilter();
            mCurrID = 0;
        }
        Log.d("TAG", "categoryIndex :" + categoryIndex);
    }

    public void setCurrResourceID(int id) {
        if (id != -1) {
            this.mCurrID = id;
        }
        loadLocalAnimationFilter();
    }


    private void changeCategoryDir(ResourceForm resourceForm) {
        if (resourceForm != null && resourceForm.getPath() != null) {
            List<String> list = EditorCommon.getAnimationFilterListByDir(resourceForm.getPath());
            //添加撤销
            list.add(0, null);
            mFilterAdapter.setDataList(list);
            mFilterAdapter.notifyDataSetChanged();
        }
    }

    private class MyLoadAsyncTask extends AsyncTask<Void, Void, List<FileDownloaderModel>> {

        @Override
        protected List<FileDownloaderModel> doInBackground(Void... voids) {
            return DownloaderManager.getInstance().getDbController().getResourceByType(EffectService.ANIMATION_FILTER);
        }

        @Override
        protected void onPostExecute(List<FileDownloaderModel> fileDownloaderModels) {
            super.onPostExecute(fileDownloaderModels);
            initResourceLocalWithSelectId(mCurrID, fileDownloaderModels);
        }
    }
}
