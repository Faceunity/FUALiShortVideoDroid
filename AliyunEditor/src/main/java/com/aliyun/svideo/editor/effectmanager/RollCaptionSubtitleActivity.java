package com.aliyun.svideo.editor.effectmanager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.bean.AlivcRollCaptionSubtitleBean;
import com.aliyun.svideo.editor.effects.rollcaption.RollCaptionColorView;
import java.util.ArrayList;

public class RollCaptionSubtitleActivity extends AppCompatActivity {
    public static final String ROLL_CAPTION_USE_FAMILY_COLOR = "roll_caption_use_family_color";
    public static final String ROLL_CAPTION_FONT_COLOR = "roll_caption_font_color";
    public static final String INTENT_ROLL_CAPTION_SUBTITLE_LIST = "intent_roll_caption_subtitle_list";

    private ImageView mBackImageView;
    private ImageView mCommitImageView;
    private ImageView mEditColorImageView;
    private RecyclerView mRollCaptionRecyclerView;
    private RollCaptionSubtitleAdapter mRollCaptionSubtitleAdapter;
    private RollCaptionColorView mRollCaptionColorView;
    /**
     * 字体颜色
     */
    private int mFontColor;
    private ArrayList<AlivcRollCaptionSubtitleBean> mSubtitleList;
    /**
     * 是否使用全局颜色控制
     */
    private boolean mUseFamilyColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roll_caption_subtitle);

        mFontColor = getIntent().getIntExtra(ROLL_CAPTION_FONT_COLOR, Color.WHITE);
        mSubtitleList = (ArrayList<AlivcRollCaptionSubtitleBean>) getIntent().getSerializableExtra(INTENT_ROLL_CAPTION_SUBTITLE_LIST);
        mUseFamilyColor = getIntent().getBooleanExtra(ROLL_CAPTION_USE_FAMILY_COLOR, true);

        initView();
        initRecyclerView();
        initListener();
    }

    private void initView(){
        mBackImageView = findViewById(R.id.aliyun_back_iv);
        mCommitImageView = findViewById(R.id.iv_commit);
        mEditColorImageView = findViewById(R.id.iv_edit_color);
        mRollCaptionRecyclerView = findViewById(R.id.roll_caption_recyclerview);
        mRollCaptionColorView = findViewById(R.id.roll_caption_color_view);
    }


    private void initRecyclerView(){
        mRollCaptionRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        mRollCaptionSubtitleAdapter = new RollCaptionSubtitleAdapter(mSubtitleList);
        if(mUseFamilyColor){
            mRollCaptionSubtitleAdapter.setAllColor(mFontColor);
        }
        mRollCaptionRecyclerView.setAdapter(mRollCaptionSubtitleAdapter);

        mRollCaptionSubtitleAdapter.setOnSelectItemChangedListener(new RollCaptionSubtitleAdapter.OnSelectItemChangedListener() {
            @Override
            public void onSelectItemChanged(int total, int inEditCount,int changedIndex, boolean isChecked) {
                mEditColorImageView.setVisibility(total > 0 ? View.VISIBLE : View.GONE);
                if(total <= 0 || inEditCount > 0){
                    mRollCaptionColorView.setVisibility(View.GONE);
                }
                mEditColorImageView.setClickable(inEditCount == 0);
            }
        });
    }

    private void initListener(){
        mBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Commit
        mCommitImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Color Commit
                if(mRollCaptionColorView.getVisibility() == View.VISIBLE){
                    mRollCaptionColorView.setVisibility(View.GONE);
                    mEditColorImageView.setVisibility(View.GONE);
                    if(mRollCaptionSubtitleAdapter != null){
                        mRollCaptionSubtitleAdapter.confirmColor();
                    }
                }else{
                    //apply
                    Intent intent = new Intent();
                    intent.putExtra(INTENT_ROLL_CAPTION_SUBTITLE_LIST,mSubtitleList);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                    mSubtitleList.clear();

                }
            }
        });

        mEditColorImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mRollCaptionColorView.getVisibility() != View.VISIBLE){
                    mRollCaptionColorView.setVisibility(View.VISIBLE);
                }
            }
        });

        mRollCaptionColorView.setOnColorSelectedListener(new RollCaptionColorView.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                if(mRollCaptionSubtitleAdapter != null){
                    mRollCaptionSubtitleAdapter.setColor(color);
                }
            }
        });

    }
}