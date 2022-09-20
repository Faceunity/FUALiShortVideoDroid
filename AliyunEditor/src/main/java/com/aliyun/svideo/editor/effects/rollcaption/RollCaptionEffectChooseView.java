package com.aliyun.svideo.editor.effects.rollcaption;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.bean.AlivcRollCaptionSubtitleBean;
import com.aliyun.svideo.editor.effectmanager.RollCaptionSubtitleActivity;
import com.aliyun.svideo.editor.effects.control.BaseChooser;
import com.aliyun.svideo.editor.effects.control.EffectInfo;
import com.aliyun.svideo.editor.effects.control.OnItemClickListener;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.aliyun.svideosdk.common.struct.project.Source;
import com.aliyun.svideosdk.editor.AliyunRollCaptionComposer;

import java.util.ArrayList;
import java.util.List;

public class RollCaptionEffectChooseView extends BaseChooser {

    private LinearLayout mRollCaptionChooser;
    private TextView mRollCaptionSubtitleTextView;
    private TextView mRollCaptionColorTextView;
    private TextView mRollCaptionFontTextView;
    private TextView mRollCaptionClearTextView;
    private ImageView mRollCaptionApplyImageView;
    /**
     * 颜色选择View
     */
    private RollCaptionColorView mRollCaptionColorView;
    /**
     * 默认字体颜色
     */
    private int mCurrentColor = Color.WHITE;
    /**
     * 字体选择
     */
    private RecyclerView mRollCaptionFontRecyclerView;
    /**
     * 字体选择 Adapter
     */
    private RollCaptionAdapter mCaptionAdapter;
    /**
     * 字幕列表
     */
    private ArrayList<String> mRollCaptionList;

    private ArrayList<AlivcRollCaptionSubtitleBean> mSubtitleBeans;
    /**
     * 翻转字幕接口
     */
    private AliyunRollCaptionComposer mAliyunRollCaptionComposer;
    /**
     * 字体资源
     */
    private Source mFontSource;

    /**
     * 是否修改全部字体颜色
     */
    private boolean mUseFamilyColor = true;

    public RollCaptionEffectChooseView(@NonNull Context context) {
        this(context, null);
    }

    public RollCaptionEffectChooseView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RollCaptionEffectChooseView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(mRollCaptionChooser != null){
            mRollCaptionChooser.setVisibility(View.VISIBLE);
        }
        if(mRollCaptionColorView != null){
            mRollCaptionColorView.setVisibility(View.GONE);
        }
        if(mRollCaptionFontRecyclerView != null){
            mRollCaptionFontRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.alivc_editor_view_chooser_roll_caption,this);
        mRollCaptionChooser = findViewById(R.id.roll_caption_chooser);
        mRollCaptionColorView = findViewById(R.id.roll_caption_color_view);
        mRollCaptionFontRecyclerView = findViewById(R.id.roll_caption_font_recyclerview);

        mRollCaptionSubtitleTextView = findViewById(R.id.tv_roll_caption_subtitle);
        mRollCaptionColorTextView = findViewById(R.id.tv_roll_caption_color);
        mRollCaptionFontTextView = findViewById(R.id.tv_roll_caption_font);
        mRollCaptionClearTextView = findViewById(R.id.roll_caption_clear_tv);
        mRollCaptionApplyImageView = findViewById(R.id.roll_caption_apply_iv);

        initRecyclerView();
        initListener();
        parseRollCaptionLrc();

    }

    /**
     * 解析字幕
     */
    private void parseRollCaptionLrc() {
        mRollCaptionList = new ArrayList<>();
        mSubtitleBeans = new ArrayList<>();
        mRollCaptionList.add("[00:01.32]我来到，你的城市");
        mRollCaptionList.add("[00:03.06]走过你来时的路");
        mRollCaptionList.add("[00:04.67]想像着，没我的日子");
        mRollCaptionList.add("[00:06.42]你是怎样的孤独");
        mRollCaptionList.add("[00:08.80]昨天已经");

        for (String subtitle : mRollCaptionList) {
            AlivcRollCaptionSubtitleBean bean = new AlivcRollCaptionSubtitleBean();
            String[] split = subtitle.split("]", 2);
            bean.setShowTime(split[0]+"]");
            bean.setContent(split[1]);
            mSubtitleBeans.add(bean);
        }
    }

    private void initRecyclerView(){
        mRollCaptionFontRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        mCaptionAdapter = new RollCaptionAdapter(getContext());
        mCaptionAdapter.showFontData();
        mRollCaptionFontRecyclerView.setAdapter(mCaptionAdapter);
    }

    private void initListener(){
        //字幕
        mRollCaptionSubtitleTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(),RollCaptionSubtitleActivity.class);
                intent.putExtra(RollCaptionSubtitleActivity.ROLL_CAPTION_USE_FAMILY_COLOR,mUseFamilyColor);
                intent.putExtra(RollCaptionSubtitleActivity.ROLL_CAPTION_FONT_COLOR,mCurrentColor);
                if(mRollCaptionList != null){
                    intent.putExtra(RollCaptionSubtitleActivity.INTENT_ROLL_CAPTION_SUBTITLE_LIST,mSubtitleBeans);
                }
                ((Activity)getContext()).startActivityForResult(intent,BaseChooser.ROLL_CAPTION_REQUEST_CODE);
            }
        });

        //颜色
        mRollCaptionColorTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mRollCaptionChooser.setVisibility(View.GONE);
                mRollCaptionFontRecyclerView.setVisibility(View.GONE);
                mRollCaptionColorView.setVisibility(View.VISIBLE);
            }
        });

        //字体
        mRollCaptionFontTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mRollCaptionChooser.setVisibility(View.GONE);
                mRollCaptionFontRecyclerView.setVisibility(View.VISIBLE);
                mRollCaptionColorView.setVisibility(View.GONE);
            }
        });

        //清空
        mRollCaptionClearTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentColor = Color.WHITE;
                mFontSource = null;
                mUseFamilyColor = true;
                if(mAliyunRollCaptionComposer != null){
                    mAliyunRollCaptionComposer.reset();
                    mAliyunRollCaptionComposer.hide();
                }
                if (mOnEffectActionLister != null) {
                    mOnEffectActionLister.onCancel();
                }
            }
        });

        //apply
        mRollCaptionApplyImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mAliyunRollCaptionComposer.reset();
                if(mAliyunRollCaptionComposer != null ){
                    List<String> list = new ArrayList<>();
                    if(mSubtitleBeans != null && !mUseFamilyColor){
                        for (AlivcRollCaptionSubtitleBean mSubtitleBean : mSubtitleBeans) {
                            list.add(mSubtitleBean.getShowTime() + mSubtitleBean.getContent());
                        }
                        //设置字幕 List
                        mAliyunRollCaptionComposer.updateCaptionList(list);
                        //设置每段字幕的颜色
                        AliyunRollCaptionComposer.StyleEditor styleEditor = null;
                        for(int i = 0;i < mSubtitleBeans.size();i++){
                            styleEditor = mAliyunRollCaptionComposer.editCaptionStyle(i);
                            styleEditor.setTextFont(mFontSource);
                            styleEditor.setTextColor(mSubtitleBeans.get(i).getColor());
                        }
                        if(styleEditor != null){
                            styleEditor.done();
                        }
                    }else{
                        //整体修改字幕颜色
                        List<String> lastList = mRollCaptionList;
                        if (mSubtitleBeans != null) {
                              lastList = new ArrayList<>();
                            for (AlivcRollCaptionSubtitleBean mSubtitleBean : mSubtitleBeans) {
                                lastList.add(mSubtitleBean.getShowTime() + mSubtitleBean.getContent());
                            }
                        }
                        mAliyunRollCaptionComposer.updateCaptionList(lastList);
                        mAliyunRollCaptionComposer.editCaptionFamilyStyle()
                                                  .setTextColor(mCurrentColor)
                                                  .setTextFont(mFontSource)
                                                  .done();
                    }
                    mAliyunRollCaptionComposer.show();

                    if(mOnEffectChangeListener != null){
                        EffectInfo effectInfo = new EffectInfo();
                        effectInfo.type = UIEditorPage.ROLL_CAPTION;
                        mOnEffectChangeListener.onEffectChange(effectInfo);
                    }
                    if (mOnEffectActionLister != null) {
                        mOnEffectActionLister.onCancel();
                    }
                }
            }
        });

        //字体选择
        mCaptionAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public boolean onItemClick(EffectInfo effectInfo, int index) {
                if (effectInfo.fontSource != null) {
                    mFontSource = effectInfo.fontSource;
                } else {
                    mFontSource = new Source(effectInfo.fontPath);
                }
                mRollCaptionChooser.setVisibility(View.VISIBLE);
                mRollCaptionFontRecyclerView.setVisibility(View.GONE);
                mRollCaptionColorView.setVisibility(View.GONE);
                return false;
            }
        });

        //选择颜色
        mRollCaptionColorView.setOnColorSelectedListener(new RollCaptionColorView.OnColorSelectedListener() {

            @Override
            public void onColorSelected(int color) {
                mUseFamilyColor = true;
                mRollCaptionChooser.setVisibility(View.VISIBLE);
                mRollCaptionColorView.setVisibility(View.GONE);
                mCurrentColor = color;
            }
        });
    }

    public void setUseFamilyColor(boolean use){
        this.mUseFamilyColor = use;
    }

    @Override
    public boolean isPlayerNeedZoom() {
        return false;
    }

    public void setAliyunRollCaptionComposer(AliyunRollCaptionComposer aliyunRollCaptionComposer){
        this.mAliyunRollCaptionComposer = aliyunRollCaptionComposer;
    }

    public void setSubtitleList(ArrayList<AlivcRollCaptionSubtitleBean> subtitleBeans) {
        this.mSubtitleBeans = subtitleBeans;
    }
}
