package com.aliyun.demo.recorder;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.aliyun.struct.form.IMVForm;

import java.util.ArrayList;
import java.util.List;
import com.aliyun.demo.R;


/**
 * Created by aa on 2018/1/15.
 */

public class MVChooser extends DialogFragment implements MvAdapter.OnItemClickListener {

    public interface OnEffectChangeListener {

        void onEffectChanged(MvForm effect);

    }

    public interface OnChooseDismissListener {

        void onChooseDismiss();

    }

    private RecyclerView mListView;
    private MvAdapter mAdapter;
    private OnEffectChangeListener mListener;
    private OnChooseDismissListener mDismissListener;
    private List<IMVForm> mImvList = new ArrayList<>();
    private int mSelectedIndex;

    public void setChooseData(List<IMVForm> data){
        mImvList.addAll(data);
    }

    public void setChooseListener(OnEffectChangeListener l){
        mListener = l;
    }

    public void setOnDismissListener(OnChooseDismissListener l){
        mDismissListener = l;
    }

    public int getSelectedEffectIndex(){
        return mAdapter.getSelectedEffectIndex();
    }

    public void setSelectedEffectIndex(int index){
        mSelectedIndex = index;
    }

    public static MVChooser newInstance(){
        return new MVChooser();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.QUDemoDialogStyle);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.aliyun_video_bottom_dialog_animation;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = LayoutInflater.from(getActivity()).inflate(R.layout.aliyun_video_layout_effect_list, container);
        mListView = (RecyclerView) mView.findViewById(R.id.effect_list);
        LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        lm.setOrientation(LinearLayoutManager.HORIZONTAL);
        mListView.setLayoutManager(lm);
        mAdapter = new MvAdapter(getActivity());
        mAdapter.setSelectedEffectIndex(mSelectedIndex);
        mListView.setAdapter(mAdapter);
        mAdapter.setData(mImvList);
        mAdapter.setOnItemClickListener(this);
        mListView.addItemDecoration(new RecyclerView.ItemDecoration() {

            int space = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                if(parent.getChildPosition(view) != 0)
                    outRect.left = space;
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                super.onDraw(c, parent, state);
            }
        });
        return mView;
    }

    @Override
    public void onResume() {
        getDialog().getWindow().setGravity(Gravity.BOTTOM);
        super.onResume();
        DisplayMetrics dpMetrics = new DisplayMetrics();
        getActivity().getWindow().getWindowManager().getDefaultDisplay()
                .getMetrics(dpMetrics);
        WindowManager.LayoutParams p = getDialog().getWindow().getAttributes();
        p.width = dpMetrics.widthPixels;

        getDialog().getWindow().setAttributes(p);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if(mDismissListener != null){
            mDismissListener.onChooseDismiss();
        }
    }

    @Override
    public boolean onItemClick(MvForm effectInfo, int index) {
        if(mListener != null){
            mListener.onEffectChanged(effectInfo);
        }
        return false;
    }
}
