package com.aliyun.svideo.editor.effects.transition;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.svideo.base.EffectParamsAdjustView;
import com.aliyun.svideo.base.Form.ResourceForm;
import com.aliyun.svideo.base.http.EffectService;
import com.aliyun.svideo.downloader.DownloaderManager;
import com.aliyun.svideo.downloader.FileDownloaderModel;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effectmanager.MoreTransitionEffectActivity;
import com.aliyun.svideo.editor.effects.CategoryAdapter;
import com.aliyun.svideo.editor.effects.control.BaseChooser;
import com.aliyun.svideo.editor.effects.control.EffectInfo;
import com.aliyun.svideo.editor.effects.control.OnItemClickListener;
import com.aliyun.svideo.editor.effects.control.SpaceItemDecoration;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.aliyun.qupai.editor.AliyunIEditor;
import com.aliyun.svideo.editor.util.EditorCommon;
import com.aliyun.svideo.sdk.external.struct.AliyunIClipConstructor;
import com.aliyun.svideo.sdk.external.struct.effect.EffectConfig;
import com.aliyun.svideo.sdk.external.struct.effect.TransitionBase;
import com.aliyun.svideo.sdk.external.struct.effect.ValueTypeEnum;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cross_ly
 * @date 2018/08/28 <p>描述:
 */
public class TransitionChooserView extends BaseChooser {

    private RecyclerView mTransitionView;
    private TransitionAdapter mTransitionAdapter;
    private static final int MIN_COUNT = 2;
    private static final int MAX_COUNT = Integer.MAX_VALUE;
    public static final String TRANSITION_PAYLOAD = "transition_payload";

    public static final int EFFECT_NONE = 0, EFFECT_UP = 1, EFFECT_DOWN = 2,
                            EFFECT_LEFT = 3, EFFECT_RIGHT = 4,
                            EFFECT_SHUTTER = 5, EFFECT_FADE = 6,
                            EFFECT_FIVE_STAR = 7, EFFECT_CIRCLE = 8, EFFECT_CUSTOM = 9;
    public static final int[] EFFECT_LIST = {EFFECT_NONE,
                                             EFFECT_UP,
                                             EFFECT_DOWN,
                                             EFFECT_LEFT,
                                             EFFECT_RIGHT,
                                             EFFECT_SHUTTER,
                                             EFFECT_FADE,
                                             EFFECT_FIVE_STAR,
                                             EFFECT_CIRCLE,
                                            };
    private TransitionEffectAdapter mTransitionEffectAdapter;
    private TransitionEffectCache mTransitionEffectCache;
    private OnPreviewListener mOnPreviewListener;
    private RecyclerView mCategoryRecycleView;
    private CategoryAdapter mCategoryAdapter;
    private ArrayList<ResourceForm> mTransitionList4Category = new ArrayList<>();
    private MyLoadAsyncTask mLoadTask;
    private EffectParamsAdjustView mParamsAdjustView;
    private EffectInfo mCurrentEffectInfo;

    public TransitionChooserView(@NonNull Context context) {
        this(context, null);
    }

    public TransitionChooserView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TransitionChooserView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.alivc_editor_view_chooser_transition, this);
        initTitleView(view);

        mTransitionView = view.findViewById(R.id.transition_view);

        RecyclerView transitionEffectView = view.findViewById(R.id.transition_effect_view);
        mCategoryRecycleView = view.findViewById(R.id.transition_category_list);
        mCategoryAdapter = new CategoryAdapter(getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mCategoryRecycleView.setLayoutManager(layoutManager);
        mCategoryRecycleView.setAdapter(mCategoryAdapter);
        mCategoryAdapter.setData(mTransitionList4Category);
        mCategoryAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public boolean onItemClick(EffectInfo effectInfo, int index) {
                if (effectInfo.isCategory && mTransitionList4Category.size() > index) {
                    ResourceForm resourceForm = mTransitionList4Category.get(index);
                    mCurrID = resourceForm.getId();
                    changeCategoryDir(resourceForm);
                }
                return false;
            }
        });
        mCategoryAdapter.setMoreClickListener(new CategoryAdapter.OnMoreClickListener() {
            @Override
            public void onMoreClick() {
                Intent moreIntent = new Intent(getContext(), MoreTransitionEffectActivity.class);
                ((Activity) getContext()).startActivityForResult(moreIntent, TRANSITION_EFFECT_REQUEST_CODE);
            }
        });
        mTransitionEffectAdapter = new TransitionEffectAdapter(getContext());
        mTransitionEffectAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public boolean onItemClick(EffectInfo effectInfo, int index) {

                effectInfo.clipIndex = mTransitionAdapter.getSelectPosition();
                if (effectInfo.clipIndex == -1) {
                    //尚未选中操作片段时，不响应特效点击事件
                    return false;
                } else {
                    effectInfo.type = UIEditorPage.TRANSITION;
                    if (effectInfo.transitionType == EFFECT_CUSTOM) {
                        showEffectParamsUI(effectInfo.transitionBase);
                    } else {
                        mParamsAdjustView.setVisibility(GONE);
                    }
                    if (mOnEffectChangeListener != null) {
                        mCurrentEffectInfo = effectInfo;
                        mOnEffectChangeListener.onEffectChange(effectInfo);
                    }
                    addLocalCacheType(effectInfo);
                }
                return true;
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        transitionEffectView.setLayoutManager(linearLayoutManager);
        transitionEffectView.addItemDecoration(
            new SpaceItemDecoration(getContext().getResources().getDimensionPixelSize(R.dimen.list_item_space)));
        transitionEffectView.setAdapter(mTransitionEffectAdapter);
        loadLocalTransitionEffect();
        mParamsAdjustView = view.findViewById(R.id.params_effect_view);
        mParamsAdjustView.setOnAdjustListener(new EffectParamsAdjustView.OnAdjustListener() {
            @Override
            public void onAdjust() {
                if (mParamsAdjustView.getVisibility() == VISIBLE && mCurrentEffectInfo != null
                        && mCurrentEffectInfo.transitionType == EFFECT_CUSTOM) {
                    mCurrentEffectInfo.isUpdateTransition = true;
                    mOnEffectChangeListener.onEffectChange(mCurrentEffectInfo);
                }
            }
        });
    }

    private void initTitleView(View view) {
        ImageView iv = view.findViewById(R.id.iv_effect_icon);
        TextView tv = view.findViewById(R.id.tv_effect_title);
        iv.setImageResource(R.mipmap.aliyun_svideo_icon_transition);
        tv.setText(R.string.alivc_editor_dialog_transition_tittle);

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
                mTransitionEffectCache.commitCache();
            }
        });
    }

    /**
     * 转场的
     */
    private void dealCancel() {

        EffectInfo effectInfo = new EffectInfo();
        effectInfo.type = UIEditorPage.TRANSITION;
        effectInfo.mutiEffect = new ArrayList<>();
        LinkedHashMap<Integer, EffectInfo> recover = mTransitionEffectCache.recover();

        if (recover.size() != 0) {
            for (Map.Entry<Integer, EffectInfo> entry : recover.entrySet()) {
                effectInfo.mutiEffect.add(entry.getValue());
            }
            //因为转场撤销的耗时属性，在存在撤销的时候,会自行处理cancel事件
            mOnEffectChangeListener.onEffectChange(effectInfo);
        } else {
            if (mOnEffectActionLister != null) {
                mOnEffectActionLister.onCancel();
            }
        }

    }

    /**
     * 添加本地的转场选择缓存
     *
     * @param effectInfo EffectInfo
     */
    private void addLocalCacheType(EffectInfo effectInfo) {

        mTransitionEffectCache.put(effectInfo.clipIndex, effectInfo);
        mTransitionAdapter.notifyItemChanged(effectInfo.clipIndex, TRANSITION_PAYLOAD);
    }

    /**
     * 初始化短视频片段显示的View
     *
     * @param clipConstructor AliyunIClipConstructor
     */
    public void initTransitionAdapter(AliyunIClipConstructor clipConstructor) {
        mTransitionEffectCache = mEditorService.getTransitionEffectCache(clipConstructor);
        mTransitionAdapter = new TransitionAdapter(getContext(), mTransitionEffectCache);
        mTransitionAdapter.setOnSelectListener(new TransitionAdapter.OnSelectListener() {
            @Override
            public void onSelect(ImageView iv, int clipIndex, boolean isClickTransition) {
                if (mTransitionEffectCache != null) {
                    checkEffect(iv, clipIndex, isClickTransition);
                }
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mTransitionView.setLayoutManager(linearLayoutManager);
        mTransitionView.setAdapter(mTransitionAdapter);
    }

    /**
     * 显示参数调节ui，目前只提供{@link ValueTypeEnum#INT,ValueTypeEnum#FLOAT}两种类型
     * @param ef EffectFilter
     */
    private boolean showEffectParamsUI(final TransitionBase ef) {
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

    /**
     * 这里显示的是和转场片段缩略图一起的图标
     * 在初始化获取之前保存的转场特效时通过此方法恢复
     * 在需要预览转场效果的时候seek转场对应的时间
     *  @param iv             选择转场的特效imageView
     * @param clipIndex 片段角标
     * @param isSelectEffect 是否选中效果图标
     */
    private void checkEffect(ImageView iv, int clipIndex, boolean isSelectEffect) {
        int effectPosition = 0;
        EffectInfo effectInfo = mTransitionEffectCache.get(clipIndex);

        if (effectInfo != null) {
            effectPosition = effectInfo.transitionType;
        }
        switch (effectPosition) {
        case EFFECT_NONE:
            iv.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_none_selector);
            break;
        case EFFECT_UP:
            iv.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_translate_up_selector);
            break;
        case EFFECT_DOWN:
            iv.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_translate_down_selector);
            break;
        case EFFECT_LEFT:
            iv.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_translate_left_selector);
            break;
        case EFFECT_RIGHT:
            iv.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_translate_right_selector);
            break;
        case EFFECT_SHUTTER:
            iv.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_shutter_selector);
            break;
        case EFFECT_FADE:
            iv.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_fade_selector);
            break;
        case EFFECT_FIVE_STAR:
            iv.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_fivepointstar_selector);
            break;
        case EFFECT_CIRCLE:
            iv.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_circle_selector);
            break;
        case EFFECT_CUSTOM:
            if (effectInfo.getPath() == null) {
                iv.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_none_selector);
            } else {
                iv.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_custom_selector);
            }
            break;
        default:
            break;
        }
        //切换选中缩略图转场图标时,避免两个view循环选中
        if (isSelectEffect) {
            mCurrentEffectInfo = effectInfo;
            //1.效果选中恢复
            if (mCurrentEffectInfo == null || mCurrentEffectInfo.transitionType == EFFECT_NONE) {
                mTransitionEffectAdapter.setSelectPosition(0);
                mParamsAdjustView.setVisibility(GONE);
            } else if (mCurrentEffectInfo.transitionType == EFFECT_CUSTOM && mCurrentEffectInfo.transitionBase != null) {
                mTransitionEffectAdapter.setCustomSelectPosition(mCurrentEffectInfo.getPath());
                showEffectParamsUI(mCurrentEffectInfo.transitionBase);
            } else {
                mTransitionEffectAdapter.setSelectPosition(effectPosition);
                mParamsAdjustView.setVisibility(GONE);
            }

            //2.预览效果
            if (mOnPreviewListener != null && effectPosition != EFFECT_NONE && mTransitionAdapter.getSelectPosition() != -1) {
                mOnPreviewListener.onPreview(clipIndex, 0, false);
            }
        }
    }

    @Override
    public boolean isPlayerNeedZoom() {
        return true;
    }


    @Override
    protected void onRemove() {
        super.onRemove();
        if (mTransitionAdapter != null) {
            mTransitionAdapter.release();
        }
    }

    public void setOnPreviewListener(OnPreviewListener onPreviewListener) {
        mOnPreviewListener = onPreviewListener;
    }

    public void initResourceLocalWithSelectId(int id, List<FileDownloaderModel> downloadmodels) {
        mTransitionList4Category = new ArrayList<>();
        ArrayList<Integer> ids = new ArrayList<>();

        //添加一个默认资源
        FileDownloaderModel extFile = new FileDownloaderModel();
        extFile.setNameEn("default");
        extFile.setName("默认");
        extFile.setId(0);
        downloadmodels.add(0, extFile);

        if (downloadmodels.size() > 0) {
            for (FileDownloaderModel model : downloadmodels) {
                if ("default".equals(model.getNameEn()) || new File(model.getPath()).exists()) {
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
                        mTransitionList4Category.add(form);
                    }
                }
            }
        }

        ResourceForm form = new ResourceForm();
        form.setMore(true);
        mTransitionList4Category.add(form);
        mCategoryAdapter.setData(mTransitionList4Category);
        if (ids.size() > 0 && (id == -1 || id == 0 || !ids.contains(id)) ) {
            id = ids.get(0);
        }
        int categoryIndex = 0;
        mCurrentEffectInfo = mTransitionEffectCache.get(0);
        boolean isSelect = false;
        if (mCurrentEffectInfo != null && mCurrentEffectInfo.getPath() != null) {
            one: for (ResourceForm resourceForm : mTransitionList4Category) {
                List<String> list = EditorCommon.getAnimationFilterListByDir(resourceForm.getPath());
                if (list != null) {
                    for (String path : list) {
                        if (mCurrentEffectInfo.getPath().equals(path)) {
                            changeCategoryDir(resourceForm);
                            isSelect = true;
                            break one;
                        }
                    }
                }

                categoryIndex++;
            }
        }
        if (!isSelect) {
            //没有缓存的选中时以回传的分类id为准
            for (ResourceForm resourceForm : mTransitionList4Category) {
                if (resourceForm.getId() == id) {
                    changeCategoryDir(resourceForm);
                    break;
                }
                categoryIndex++;
            }
        }

        mCategoryRecycleView.smoothScrollToPosition(categoryIndex);
        mCategoryAdapter.selectPosition(categoryIndex);
        if (mCurrID == -1) {
            loadLocalTransitionEffect();
            mCurrID = 0;
        }
        Log.d("TAG", "categoryIndex :" + categoryIndex);
    }

    public void loadLocalTransitionEffect() {

        mLoadTask = new TransitionChooserView.MyLoadAsyncTask();
        mLoadTask.execute();
    }

    /**
     * 判断短视频的片段数是否符合限制
     *
     * @param editor AliyunIEditor
     * @return 符合返回 AliyunIClipConstructor 不符合返回null
     */
    public static AliyunIClipConstructor isClipLimit(AliyunIEditor editor) {
        AliyunIClipConstructor clipConstructor = editor.getSourcePartManager();
        int size = clipConstructor.getMediaPartCount();
        if (size < MIN_COUNT /*|| size > MAX_COUNT*/) {
            return null;
        }
        return clipConstructor;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dealCancel();
    }

    public void setCurrResourceID(int id) {
        if (id != -1) {
            this.mCurrID = id;
        }
        loadLocalTransitionEffect();
        if (mTransitionAdapter != null) {
            mTransitionAdapter.notifyDataSetChanged();
        }
    }

    public interface OnPreviewListener {

        /**
         * 转场预览的借口
         * @param clipIndex 片段的角标
         * @param leadTime 提前的时间(到转场的时间)
         * @param isStop 播放放转场是否要暂停
         */
        void onPreview(int clipIndex, long leadTime, boolean isStop);
    }

    private void changeCategoryDir(ResourceForm resourceForm) {
        List<String> list = null;

        if (resourceForm != null && resourceForm.getPath() != null) {
            list = EditorCommon.getAnimationFilterListByDir(resourceForm.getPath());
            //添加无转场的类型
            list.add(0, null);
        }
        mTransitionEffectAdapter.setData(list);
        //初始化和切换tab选择使用的转场
        if (mCurrentEffectInfo == null || mCurrentEffectInfo.transitionType == EFFECT_NONE) {
            mTransitionEffectAdapter.setSelectPosition(0);
        } else if (mCurrentEffectInfo.transitionType == EFFECT_CUSTOM) {
            mTransitionEffectAdapter.setCustomSelectPosition(mCurrentEffectInfo.getPath());
        } else {
            mTransitionEffectAdapter.setSelectPosition(mCurrentEffectInfo.transitionType);
        }

        mTransitionEffectAdapter.notifyDataSetChanged();

    }

    private class MyLoadAsyncTask extends AsyncTask<Void, Void, List<FileDownloaderModel>> {

        @Override
        protected List<FileDownloaderModel> doInBackground(Void... voids) {
            return DownloaderManager.getInstance().getDbController().getResourceByType(
                       EffectService.EFFECT_TRANSITION);
        }

        @Override
        protected void onPostExecute(List<FileDownloaderModel> fileDownloaderModels) {
            super.onPostExecute(fileDownloaderModels);
            initResourceLocalWithSelectId(mCurrID, fileDownloaderModels);
        }
    }
}
