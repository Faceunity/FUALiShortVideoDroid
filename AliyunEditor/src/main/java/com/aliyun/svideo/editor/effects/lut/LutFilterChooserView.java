package com.aliyun.svideo.editor.effects.lut;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.SeekBar;
import android.widget.TextView;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.contant.EditorConstants;
import com.aliyun.svideo.editor.effects.control.BaseChooser;
import com.aliyun.svideo.editor.effects.control.EffectInfo;
import com.aliyun.svideo.editor.effects.control.OnItemClickListener;
import com.aliyun.svideo.editor.effects.control.SpaceItemDecoration;
import com.aliyun.svideo.editor.msg.Dispatcher;
import com.aliyun.svideo.editor.util.EditorCommon;
import com.aliyun.svideosdk.common.struct.effect.LUTEffectBean;
import com.aliyun.svideosdk.common.struct.project.Source;

public class LutFilterChooserView extends BaseChooser implements OnItemClickListener {
    private RecyclerView mListView;
    private LutAdapter mLutAdapter;
    private TextView mTvEffectTitle;
    private SeekBar mSeekBar;
    private String lutFilePath;

    public LutFilterChooserView(@NonNull Context context) {
        this(context, null);
    }

    public LutFilterChooserView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LutFilterChooserView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onItemClick(EffectInfo effectInfo, int index) {
        if (effectInfo != null && effectInfo.getSource() != null) {
            lutFilePath = effectInfo.getSource().getPath();
            LUTEffectBean bean = new LUTEffectBean();
            bean.setSource(effectInfo.getSource());
            bean.setIntensity(1.f);
            Dispatcher.getInstance().postMsg(bean);

        }
        return true;
    }

    @Override
    protected void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.alivc_editor_view_chooser_mv, this);
        findViewById(R.id.seek_bar).setVisibility(VISIBLE);
        mListView = (RecyclerView) findViewById(R.id.effect_list_filter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mListView.setLayoutManager(layoutManager);
        if (mLutAdapter == null) {
            mLutAdapter = new LutAdapter(getContext());
            mLutAdapter.setOnItemClickListener(this);
            mLutAdapter.setDataList(EditorCommon.getLutFilterList(getContext()));
        }

        mListView.setAdapter(mLutAdapter);
        mListView.addItemDecoration(new SpaceItemDecoration(getContext().getResources().getDimensionPixelSize(R.dimen.list_item_space)));
        mTvEffectTitle = findViewById(R.id.effect_title_tv);
        mTvEffectTitle.setText(R.string.alivc_svideo_filter_lut);
        Drawable top = getContext().getResources().getDrawable(R.mipmap.alivc_svideo_icon_tab_filter);
        top.setBounds(0, 0, top.getMinimumWidth(), top.getMinimumHeight());
        mTvEffectTitle.setCompoundDrawables(top, null, null, null);
        mSeekBar = findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (lutFilePath != null && !EditorConstants.EFFECT_FILTER_LOCAL_LUT_CLEAR.equals(lutFilePath)
                 && !EditorConstants.EFFECT_FILTER_LOCAL_LUT_ADD.equals(lutFilePath)){
                    LUTEffectBean bean = new LUTEffectBean();
                    Source source = new Source(lutFilePath);
                    bean.setSource(source);
                    float intensity = progress * 1.0f / 100;
                    bean.setIntensity(intensity);
                    Dispatcher.getInstance().postMsg(bean);

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void setSelectedPos(final int selectedPos) {
        if (mLutAdapter != null) {
            mLutAdapter.setSelectedPos(selectedPos);
        }
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
