package com.aliyun.demo.effects.paint;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.aliyun.demo.editor.R;
import com.aliyun.demo.effects.control.EditorService;
import com.aliyun.demo.effects.control.SpaceItemDecoration;
import com.aliyun.qupai.editor.AliyunICanvasController;

/**
 * Created by Administrator on 2017/6/6.
 */

public class PaintMenuView {
    private RecyclerView mListView;
    private ColorAdapter mColorAdapter;
    private ImageView mCacnel, mUndo, mComplete;
    private FrameLayout mPaintOne, mPaintTwo, mPaintThree;
    private AliyunICanvasController canvasController;
    private OnPaintOpera mOnPaintOpera;
    private View mView;
    private float mCurrentSize = 5f;
    private int mCurrentColor = Color.WHITE;
    private Map<Float, View> mViews = new HashMap<>();
    private Paint mCurrentPaint = new Paint();
    private EditorService mEditorService;
    private Context mContext;

    public PaintMenuView(AliyunICanvasController canvasController) {
        this.canvasController = canvasController;
    }
    public View getPaintMenu(Context context) {
        mContext = context;
        mView = View.inflate(context, R.layout.aliyun_svideo_paint_view, null);
        mListView = (RecyclerView)mView.findViewById(R.id.color_list);
        mCacnel = (ImageView) mView.findViewById(R.id.cancel);
        mUndo = (ImageView) mView.findViewById(R.id.undo);
        mComplete = (ImageView) mView.findViewById(R.id.complete);
        mPaintOne = (FrameLayout) mView.findViewById(R.id.paint_one);
        mPaintTwo = (FrameLayout) mView.findViewById(R.id.paint_two);
        mPaintThree = (FrameLayout) mView.findViewById(R.id.paint_three);
        fillViews();
        mCurrentSize = dip2px(mContext, 5);
        mCurrentPaint.setColor(mCurrentColor);
        mCurrentPaint.setStrokeWidth(mCurrentSize);
        if(mEditorService != null && mEditorService.getPaint() != null) {
            mCurrentPaint = mEditorService.getPaint();
            if(mCurrentPaint != null) {
                mCurrentColor = mCurrentPaint.getColor();
                mCurrentSize = mCurrentPaint.getStrokeWidth();
            }
        }
        mCacnel.setOnClickListener(onClickListener);
        mUndo.setOnClickListener(onClickListener);
        mComplete.setOnClickListener(onClickListener);
        mPaintOne.setOnClickListener(onClickListener);
        mPaintTwo.setOnClickListener(onClickListener);
        mPaintThree.setOnClickListener(onClickListener);
        if(mViews.get(mCurrentSize) != null) {
            mViews.get(mCurrentSize).setSelected(true);
        }
        canvasController.setCurrentSize(mCurrentSize);
        canvasController.setCurrentColor(mCurrentColor);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        mListView.setLayoutManager(layoutManager);
        mColorAdapter = new ColorAdapter(context);
        mColorAdapter.setPaintSelect(mPaintSelect);
        mColorAdapter.setSelectedPos(mCurrentColor);
        mListView.setAdapter(mColorAdapter);
        mListView.addItemDecoration(new SpaceItemDecoration(context.getResources().getDimensionPixelSize(R.dimen.list_item_space)));
        mListView.scrollToPosition(mColorAdapter.getSelectedPos());
        return mView;
    }

    private View.OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if(id == R.id.cancel) {
                if(mOnPaintOpera != null) {
                    canvasController.clear();
                    mOnPaintOpera.removeView(mView);
                }
            } else if (id == R.id.undo) {
                canvasController.undo();
            } else if (id == R.id.complete) {
                if(mOnPaintOpera != null) {
                    mOnPaintOpera.completeView();
                    mOnPaintOpera.removeView(mView);
                }
            } else {
                clearPaint();
                if (id == R.id.paint_one) {
                    mCurrentSize = dip2px(mContext, 5);
                } else if (id == R.id.paint_two) {
                    mCurrentSize = dip2px(mContext, 10);
                } else if (id == R.id.paint_three) {
                    mCurrentSize = dip2px(mContext, 15);
                }
                mViews.get(mCurrentSize).setSelected(true);
                canvasController.setCurrentSize(mCurrentSize);
                mCurrentPaint.setStrokeWidth(mCurrentSize);
                mEditorService.setPaint(mCurrentPaint);
            }
        }
    };

    public void setEditorService(EditorService editorService) {
        this.mEditorService = editorService;
    }

    private void clearPaint() {
        mPaintOne.setSelected(false);
        mPaintTwo.setSelected(false);
        mPaintThree.setSelected(false);
    }

    private PaintSelect mPaintSelect = new PaintSelect() {
        @Override
        public void onColorSelect(int color) {
            if(canvasController != null) {
                canvasController.setCurrentColor(color);
                mCurrentPaint.setColor(color);
                mEditorService.setPaint(mCurrentPaint);
            }
        }
    };

    public interface PaintSelect {
        void onColorSelect(int color);
    }

    public interface OnPaintOpera {
        void removeView(View view);
        void completeView();
    }

    public void setOnPaintOpera(OnPaintOpera mOnPaintOpera) {
        this.mOnPaintOpera = mOnPaintOpera;
    }

    private void fillViews() {
        mViews.put(dip2px(mContext, 5), mPaintOne);
        mViews.put(dip2px(mContext, 10), mPaintTwo);
        mViews.put(dip2px(mContext, 15), mPaintThree);
    }

    public float dip2px(Context paramContext, float paramFloat){
        return 0.5F + paramFloat * paramContext.getResources().getDisplayMetrics().density;
    }
}
