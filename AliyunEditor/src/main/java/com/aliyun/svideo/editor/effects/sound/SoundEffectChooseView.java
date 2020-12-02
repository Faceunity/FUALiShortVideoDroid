package com.aliyun.svideo.editor.effects.sound;

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

public class SoundEffectChooseView extends BaseChooser {

    private EffectSoundAdapter adapter;

    public SoundEffectChooseView(@NonNull Context context) {
        this(context, null);
    }

    public SoundEffectChooseView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SoundEffectChooseView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.alivc_editor_view_chooser_sound, this);
        RecyclerView recyclerView = findViewById(R.id.effect_sound_list_filter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),  LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new SpaceItemDecoration(getContext().getResources().getDimensionPixelSize(R.dimen.list_item_space)));

        if (adapter == null) {
            adapter = new EffectSoundAdapter(getContext());
            adapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public boolean onItemClick(EffectInfo effectInfo, int index) {
                    mOnEffectChangeListener.onEffectChange(effectInfo);
                    return false;
                }
            });
            adapter.setDataList(MockEffectSoundData.getEffectSound());
        }
        recyclerView.setAdapter(adapter);

        TextView soundTitle = findViewById(R.id.effect_sound_title_tv);
        soundTitle.setText(R.string.alivc_editor_dialog_sound_tittle);
        Drawable top = getContext().getResources().getDrawable(R.mipmap.alivc_svideo_icon_tab_filter);
        top.setBounds(0, 0, top.getMinimumWidth(), top.getMinimumHeight());
        soundTitle.setCompoundDrawables(top, null, null, null );
    }

    @Override
    public boolean isPlayerNeedZoom() {
        return false;
    }
}
