package com.aliyun.svideo.editor.effects.transition;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.control.BaseChooser;
import com.aliyun.svideo.editor.effects.control.EffectInfo;
import com.aliyun.svideo.editor.effects.control.OnItemClickListener;
import com.aliyun.svideo.editor.effects.control.SpaceItemDecoration;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.aliyun.qupai.editor.AliyunIEditor;
import com.aliyun.svideo.sdk.external.struct.AliyunIClipConstructor;

import java.util.ArrayList;

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
        EFFECT_FIVE_STAR = 7, EFFECT_CIRCLE = 8;
    public static final int[] EFFECT_LIST = {EFFECT_NONE,
        EFFECT_UP,
        EFFECT_DOWN,
        EFFECT_LEFT,
        EFFECT_RIGHT,
        EFFECT_SHUTTER,
        EFFECT_FADE,
        EFFECT_FIVE_STAR,
        EFFECT_CIRCLE,};
    private TransitionEffectAdapter mTransitionEffectAdapter;
    private TransitionEffectCache mTransitionEffectCache;
    private OnPreviewListener mOnPreviewListener;

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
        View view = LayoutInflater.from(getContext()).inflate(R.layout.aliyun_svideo_transition_view, this);
        initTitleView(view);

        mTransitionView = view.findViewById(R.id.transition_view);

        RecyclerView transitionEffectView = view.findViewById(R.id.transition_effect_view);
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
                    if (mOnEffectChangeListener != null) {
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
    }

    private void initTitleView(View view) {
        ImageView iv = view.findViewById(R.id.iv_effect_icon);
        TextView tv = view.findViewById(R.id.tv_effect_title);
        iv.setImageResource(R.mipmap.aliyun_svideo_icon_transition);
        tv.setText(R.string.transition_effect_manager);

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
        SparseIntArray recover = mTransitionEffectCache.recover();
        if (recover.size() != 0) {
            for (int i = 0; i < recover.size(); i++) {
                EffectInfo info = new EffectInfo();
                info.transitionType = recover.valueAt(i);
                info.clipIndex = recover.keyAt(i);
                effectInfo.mutiEffect.add(info);
            }
            //因为转场撤销的耗时属性，在存在撤销的时候,会自行处理cancel事件
            mOnEffectChangeListener.onEffectChange(effectInfo);
        }else {
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

        mTransitionEffectCache.put(effectInfo.clipIndex, effectInfo.transitionType);
        mTransitionAdapter.notifyItemChanged(effectInfo.clipIndex,TRANSITION_PAYLOAD);
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
            public void onSelect(ImageView iv, int effectPosition, int clipIndex, boolean isClickTransition) {
                checkEffect(iv, effectPosition, clipIndex,isClickTransition);

            }

        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mTransitionView.setLayoutManager(linearLayoutManager);
        mTransitionView.setAdapter(mTransitionAdapter);
    }

    /**
     * 这里显示的是和转场片段缩略图一起的图标
     * 在初始化获取之前保存的转场特效时通过此方法恢复
     * 在需要预览转场效果的时候seek转场对应的时间
     *  @param iv             选择转场的特效imageView
     * @param effectPosition 效果position
     * @param clipIndex 片段角标
     * @param isSelectEffect 是否选中效果图标
     */
    private void checkEffect(ImageView iv, int effectPosition, int clipIndex, boolean isSelectEffect) {

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
            default:
                break;
        }

            //切换选中缩略图转场图标时
        if (isSelectEffect) {
            //1.效果选中恢复
            mTransitionEffectAdapter.setSelectPosition(effectPosition);
            //2.预览效果
            if (mOnPreviewListener != null && effectPosition != EFFECT_NONE && mTransitionAdapter.getSelectPosition() != -1){
                mOnPreviewListener.onPreview(clipIndex,0,false);
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

    public void setOnPreviewListener(OnPreviewListener onPreviewListener){
        mOnPreviewListener = onPreviewListener;
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

    public interface OnPreviewListener {

        /**
         * 转场预览的借口
         * @param clipIndex 片段的角标
         * @param leadTime 提前的时间(到转场的时间)
         * @param isStop 播放放转场是否要暂停
         */
        void onPreview(int clipIndex,long leadTime,boolean isStop);
    }
}
