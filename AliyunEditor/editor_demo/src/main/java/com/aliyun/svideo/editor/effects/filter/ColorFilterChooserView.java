package com.aliyun.svideo.editor.effects.filter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.control.BaseChooser;
import com.aliyun.svideo.editor.effects.control.EffectInfo;
import com.aliyun.svideo.editor.effects.control.OnItemClickListener;
import com.aliyun.svideo.editor.effects.control.SpaceItemDecoration;
import com.aliyun.svideo.editor.msg.Dispatcher;
import com.aliyun.svideo.editor.msg.body.SelectColorFilter;
import com.aliyun.svideo.editor.util.Common;

public class ColorFilterChooserView extends BaseChooser implements OnItemClickListener {
    private RecyclerView mListView;
    private FilterAdapter mFilterAdapter;
    private TextView mTvEffectTitle;
    public ColorFilterChooserView(@NonNull Context context) {
        this(context,null);
    }

    public ColorFilterChooserView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context,attrs,0);
    }

    public ColorFilterChooserView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onItemClick(EffectInfo effectInfo, int index) {
        Dispatcher.getInstance().postMsg(new SelectColorFilter.Builder()
                .effectInfo(effectInfo)
                .index(index).build());
        return true;
    }
    @Override
    protected void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.aliyun_svideo_filter_view, this);
        mListView = (RecyclerView) findViewById(R.id.effect_list_filter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mListView.setLayoutManager(layoutManager);
        if (mFilterAdapter==null){
            mFilterAdapter = new FilterAdapter(getContext());
            mFilterAdapter.setOnItemClickListener(this);
            mFilterAdapter.setDataList(Common.getColorFilterList(getContext()));
        }

        //        mFilterAdapter.setSelectedPos(mEditorService.getEffectIndex(UIEditorPage.FILTER_EFFECT));
        mListView.setAdapter(mFilterAdapter);
        mListView.addItemDecoration(new SpaceItemDecoration(getContext().getResources().getDimensionPixelSize(R.dimen.list_item_space)));
        mTvEffectTitle = findViewById(R.id.effect_title_tv);
        mTvEffectTitle.setText(R.string.aliyun_svideo_filter);
        Drawable top = getContext().getResources().getDrawable(R.mipmap.alivc_svideo_icon_tab_filter);
        top.setBounds(0, 0, top.getMinimumWidth(), top.getMinimumHeight());
        mTvEffectTitle.setCompoundDrawables(top, null, null,null );
        //        mListView.scrollToPosition(mEditorService.getEffectIndex(UIEditorPage.FILTER_EFFECT));
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
