package com.aliyun.svideo.editor.effectmanager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.bean.AlivcRollCaptionSubtitleBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 翻转字幕，字幕列表 Adapter
 */
public class RollCaptionSubtitleAdapter extends RecyclerView.Adapter<RollCaptionSubtitleAdapter.RollCaptionSubtitleViewHolder> {

    private List<AlivcRollCaptionSubtitleBean> datas;
    /**
     * 选中的字幕下标集合
     */
    private ArrayList<Integer> mSelectedIndex;
    /**
     * 统计编辑状态的Count
     */
    private int mInEditCount = 0;
    private int mSelectItemCount = 0;
    private OnSelectItemChangedListener mListener;

    public RollCaptionSubtitleAdapter(List<AlivcRollCaptionSubtitleBean> datas){
        mSelectedIndex = new ArrayList<>();
        this.datas = datas;
    }

    @Override
    public RollCaptionSubtitleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.alivc_editor_roll_caption_item_subtitle_effect, parent, false);
        return new RollCaptionSubtitleViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(final RollCaptionSubtitleViewHolder holder, int position) {
        holder.mRollCaptionSubtitleEditText.setText(datas.get(position).getContent());
        holder.mRollCaptionSubtitleEditText.setTextColor(datas.get(position).getColor());
        holder.mRollCaptionSubtitleCheckBox.setChecked(datas.get(position).isChecked());

        holder.mRollCaptionItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlivcRollCaptionSubtitleBean alivcRollCaptionSubtitleBean = datas.get(holder.getAdapterPosition());
                boolean checked = !alivcRollCaptionSubtitleBean.isChecked();
                if(!alivcRollCaptionSubtitleBean.isInEdit()){
                    holder.mRollCaptionSubtitleCheckBox.setChecked(checked);
                }
            }
        });

        holder.mRollCaptionSubtitleCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                AlivcRollCaptionSubtitleBean alivcRollCaptionSubtitleBean = datas.get(holder.getAdapterPosition());
                changeViewState(alivcRollCaptionSubtitleBean,holder,checked);
                alivcRollCaptionSubtitleBean.setInEdit(false);
            }
        });

        //item编辑/完成
        holder.mRollCaptionEditTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlivcRollCaptionSubtitleBean alivcRollCaptionSubtitleBean = datas.get(holder.getAdapterPosition());
                if(alivcRollCaptionSubtitleBean.isInEdit()){
                    //编辑完成
                    alivcRollCaptionSubtitleBean.setContent(holder.mRollCaptionSubtitleEditText.getText().toString());
                    changeEditTextFocus(holder,false);
                    mInEditCount--;
                }else{
                    //进入编辑
                    changeEditTextFocus(holder,true);
                    mInEditCount++;
                }
                holder.mRollCaptionEditTextView.setText(alivcRollCaptionSubtitleBean.isInEdit() ? R.string.alivc_editor_dialog_roll_caption_edit : R.string.alivc_editor_dialog_roll_caption_finish);
                alivcRollCaptionSubtitleBean.setInEdit(!alivcRollCaptionSubtitleBean.isInEdit());
                if(mListener != null){
                    mListener.onSelectItemChanged(mSelectItemCount,mInEditCount,holder.getAdapterPosition(),alivcRollCaptionSubtitleBean.isChecked());
                }
            }
        });
    }

    /**
     * 选中/未选中 item 切换对应的 View 状态
     */
    private void changeViewState(AlivcRollCaptionSubtitleBean alivcRollCaptionSubtitleBean, RollCaptionSubtitleViewHolder holder, boolean checked){
        if(alivcRollCaptionSubtitleBean.isInEdit()){
            return;
        }
        alivcRollCaptionSubtitleBean.setChecked(checked);
        holder.mRollCaptionSubtitleCheckBox.setChecked(checked);
        holder.mRollCaptionEditTextView.setVisibility(checked ? View.VISIBLE : View.GONE);
        if(!checked){
            changeEditTextFocus(holder,false);
            mSelectItemCount--;
            mSelectedIndex.remove(Integer.valueOf(holder.getAdapterPosition()));
        }else{
            mSelectItemCount++;
            mSelectedIndex.add(holder.getAdapterPosition());
            holder.mRollCaptionEditTextView.setText(R.string.alivc_editor_dialog_roll_caption_edit);
        }
        if(mListener != null){
            mListener.onSelectItemChanged(mSelectItemCount,mInEditCount,holder.getAdapterPosition(),checked);
        }
    }

    /**
     * 修改 EditText
     */
    private void changeEditTextFocus(RollCaptionSubtitleViewHolder holder,boolean enable){
        holder.mRollCaptionSubtitleCheckBox.setClickable(!enable);
        holder.mRollCaptionSubtitleCheckBox.setEnabled(!enable);

        holder.mRollCaptionSubtitleEditText.setFocusable(enable);
        holder.mRollCaptionSubtitleEditText.setEnabled(enable);
        holder.mRollCaptionSubtitleEditText.setFocusableInTouchMode(enable);
        if(enable){
            holder.mRollCaptionSubtitleEditText.requestFocus();
            //光标定位到最后
            holder.mRollCaptionSubtitleEditText.setSelection(holder.mRollCaptionSubtitleEditText.getText().length());
            //弹出软键盘
            InputMethodManager inputManager =
                    (InputMethodManager) holder.mRollCaptionSubtitleEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(holder.mRollCaptionSubtitleEditText, 0);
        }
    }

    @Override
    public int getItemCount() {
        return datas == null ? 0 : datas.size();
    }

    public void setColor(int color) {
        for (Integer selectedIndex : mSelectedIndex) {
            AlivcRollCaptionSubtitleBean alivcRollCaptionSubtitleBean = datas.get(selectedIndex);
            if(alivcRollCaptionSubtitleBean.isChecked()){
                alivcRollCaptionSubtitleBean.setColor(color);
            }
            notifyItemChanged(selectedIndex);
        }
    }

    public void confirmColor() {
        mSelectItemCount = 0;
        for (Integer selectedIndex : mSelectedIndex) {
            AlivcRollCaptionSubtitleBean alivcRollCaptionSubtitleBean = datas.get(selectedIndex);
            if(alivcRollCaptionSubtitleBean.isChecked()){
                alivcRollCaptionSubtitleBean.setChecked(false);
            }
            notifyItemChanged(selectedIndex);
        }
    }

    public void setAllColor(int mFontColor) {
        for (AlivcRollCaptionSubtitleBean bean : datas) {
            bean.setColor(mFontColor);
        }
        notifyDataSetChanged();
    }

    public static class RollCaptionSubtitleViewHolder extends RecyclerView.ViewHolder{

        private final CheckBox mRollCaptionSubtitleCheckBox;
        private final EditText mRollCaptionSubtitleEditText;
        private final TextView mRollCaptionEditTextView;
        private final LinearLayout mRollCaptionItem;

        public RollCaptionSubtitleViewHolder(View itemView) {
            super(itemView);
            mRollCaptionItem = itemView.findViewById(R.id.roll_caption_item);
            mRollCaptionSubtitleCheckBox = itemView.findViewById(R.id.roll_caption_subtitle_checkbox);
            mRollCaptionSubtitleEditText = itemView.findViewById(R.id.roll_caption_subtitle_et);
            mRollCaptionEditTextView = itemView.findViewById(R.id.roll_caption_edit_tv);
        }
    }

    public interface OnSelectItemChangedListener{
        void onSelectItemChanged(int total, int inEditCount, int changedIndex, boolean isChecked);
    }

    public void setOnSelectItemChangedListener(OnSelectItemChangedListener listener){
        this.mListener = listener;
    }
}
