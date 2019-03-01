package com.aliyun.svideo.editor.effects.paint;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
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
import com.aliyun.svideo.editor.effects.control.BaseChooser;
import com.aliyun.svideo.editor.effects.control.SpaceItemDecoration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zsy_18 data:2018/8/30
 */
public class PaintChooserView extends BaseChooser implements View.OnClickListener{

    private RecyclerView mListView;
    private ColorAdapter mColorAdapter;
    private ImageView mCancel;
    private TextView mTvEffectTitle;
    private ImageView mIvEffectIcon;
    private ImageView mComplete;

    private FrameLayout mPaintOne, mPaintTwo, mPaintThree;
    private float mCurrentSize;
    private int mCurrentColor = Color.WHITE;
    private Map<Float, View> mViews;
    private Paint mCurrentPaint ;
    public PaintChooserView(@NonNull Context context) {
        this(context,null);
    }

    public PaintChooserView(@NonNull Context context,
                            @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PaintChooserView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        mViews = new HashMap<>();
        mCurrentPaint = new Paint();
        LayoutInflater.from(getContext()).inflate(R.layout.aliyun_svideo_paint_view, this, true);
        //初始化titile界面
        mTvEffectTitle = (TextView) findViewById(R.id.tv_effect_title);
        mIvEffectIcon = (ImageView) findViewById(R.id.iv_effect_icon);
        mIvEffectIcon.setImageResource(R.mipmap.alivc_svideo_icon_paint);
        mTvEffectTitle.setText(R.string.alivc_svideo_paint);
        mCancel = (ImageView) findViewById(R.id.cancel);
        mComplete = (ImageView) findViewById(R.id.complete);
        //mCancel.setImageResource(R.mipmap.alivc_svideo_icon_effect_revoke);

        mListView = (RecyclerView)findViewById(R.id.color_list);
        mPaintOne = (FrameLayout) findViewById(R.id.paint_one);
        mPaintTwo = (FrameLayout) findViewById(R.id.paint_two);
        mPaintThree = (FrameLayout) findViewById(R.id.paint_three);
        fillViews();
        mCurrentSize = dip2px(getContext(), 5);
        mCurrentPaint.setColor(mCurrentColor);
        mCurrentPaint.setStrokeWidth(mCurrentSize);

        mCancel.setOnClickListener(this);
        mComplete.setOnClickListener(this);
        mPaintOne.setOnClickListener(this);
        mPaintTwo.setOnClickListener(this);
        mPaintThree.setOnClickListener(this);

    }
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(mEditorService != null && mEditorService.getPaint() != null) {
            mCurrentPaint = mEditorService.getPaint();
            if(mCurrentPaint != null) {
                mCurrentColor = mCurrentPaint.getColor();
                mCurrentSize = mCurrentPaint.getStrokeWidth();
            }
        }
        if(mViews.get(mCurrentSize) != null) {
            mViews.get(mCurrentSize).setBackgroundDrawable(getCurrentCircle());
        }
        if (mPaintSelect!=null){
            mPaintSelect.onColorSelect(mCurrentColor);
            mPaintSelect.onSizeSelect(mCurrentSize);
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mListView.setLayoutManager(layoutManager);
        mColorAdapter = new ColorAdapter(getContext());
        mColorAdapter.setPaintSelect(new PaintSelect() {
            @Override
            public void onColorSelect(int color) {
                mCurrentColor = color;
                mCurrentPaint.setColor(color);
                mEditorService.setPaint(mCurrentPaint);
                if(mViews.get(mCurrentSize) != null) {
                    mViews.get(mCurrentSize).setBackgroundDrawable(getCurrentCircle());
                }
                if (mPaintSelect!=null){
                    mPaintSelect.onColorSelect(color);
                }
            }

            @Override
            public void onSizeSelect(float size) {
                if (mPaintSelect!=null){
                    mPaintSelect.onSizeSelect(size);
                }
            }

            @Override
            public void onUndo() {
                if (mPaintSelect!=null){
                    mPaintSelect.onUndo();
                }
            }
        });
        mColorAdapter.setSelectedPos(mCurrentColor);
        mListView.setAdapter(mColorAdapter);
        mListView.addItemDecoration(new SpaceItemDecoration(getContext().getResources().getDimensionPixelSize(R.dimen.list_item_space)));
        //mListView.scrollToPosition(mColorAdapter.getSelectedPos());
    }

    @Override
    public boolean isPlayerNeedZoom() {
        return true;
    }


    private void fillViews() {
        mViews.put(dip2px(getContext(), 5), mPaintOne);
        mViews.put(dip2px(getContext(), 10), mPaintTwo);
        mViews.put(dip2px(getContext(), 15), mPaintThree);
    }
    public float dip2px(Context paramContext, float paramFloat){
        return 0.5F + paramFloat * paramContext.getResources().getDisplayMetrics().density;
    }

    private void clearPaint() {
        mPaintOne.setBackgroundResource(R.color.alivc_transparent);
        mPaintTwo.setBackgroundResource(R.color.alivc_transparent);
        mPaintThree.setBackgroundResource(R.color.alivc_transparent);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.cancel) {
            if(mOnEffectActionLister != null) {
                mOnEffectActionLister.onCancel();
            }
        }else if (id == R.id.complete) {
            if(mOnEffectActionLister != null) {
                mOnEffectActionLister.onComplete();
            }
        } else {
            clearPaint();
            if (id == R.id.paint_one) {
                mCurrentSize = dip2px(getContext(), 5);
            } else if (id == R.id.paint_two) {
                mCurrentSize = dip2px(getContext(), 10);
            } else if (id == R.id.paint_three) {
                mCurrentSize = dip2px(getContext(), 15);
            }
            if(mViews.get(mCurrentSize) != null) {
                mViews.get(mCurrentSize).setBackgroundDrawable(getCurrentCircle());
            }
            if (mPaintSelect!=null){
                mPaintSelect.onSizeSelect(mCurrentSize);
            }
            mCurrentPaint.setStrokeWidth(mCurrentSize);
            //应用涂鸦尺寸的时候，也选择当前颜色，否则会出现第二次进入的时候没有颜色选中的问题
            mCurrentPaint.setColor(mCurrentColor);
            mEditorService.setPaint(mCurrentPaint);
        }
    }

    @Override
    public void onBackPressed() {
        if(mOnEffectActionLister != null) {
            mOnEffectActionLister.onCancel();
        }
    }

    public interface PaintSelect {
        void onColorSelect(int color);
        void onSizeSelect(float size);
        void onUndo();
    }
    private PaintSelect mPaintSelect;
    public void setPaintSelectListener(PaintSelect mPaintSelect) {
        this.mPaintSelect = mPaintSelect;
    }
    private Drawable getCurrentCircle(){
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(mCurrentColor);
        return drawable;
    }
}
