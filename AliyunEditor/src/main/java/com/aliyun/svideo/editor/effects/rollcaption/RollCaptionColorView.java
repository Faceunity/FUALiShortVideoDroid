package com.aliyun.svideo.editor.effects.rollcaption;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effectmanager.RollCaptionColorAdapter;

public class RollCaptionColorView extends FrameLayout {

    private RecyclerView mRollCaptionColorRecyclerView;
    private OnColorSelectedListener mListener;

    public RollCaptionColorView(Context context) {
        this(context,null);
    }

    public RollCaptionColorView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RollCaptionColorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.alivc_editor_roll_caption_view, this);
        mRollCaptionColorRecyclerView = findViewById(R.id.roll_caption_color_recyclerview);

        initRecyclerView();
    }

    private void initRecyclerView(){
        mRollCaptionColorRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        RollCaptionColorAdapter mRollCaptionColorAdapter = new RollCaptionColorAdapter(getContext());
        mRollCaptionColorRecyclerView.setAdapter(mRollCaptionColorAdapter);

        mRollCaptionColorAdapter.setOnRollCaptionColorItemClickListener(new RollCaptionColorAdapter.OnRollCaptionColorItemClickListener() {
            @Override
            public void onItemClick(int color) {
                if(mListener != null){
                    mListener.onColorSelected(color);
                }
            }
        });
    }

    public interface OnColorSelectedListener{
        void onColorSelected(int color);
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener){
        this.mListener = listener;
    }
}
