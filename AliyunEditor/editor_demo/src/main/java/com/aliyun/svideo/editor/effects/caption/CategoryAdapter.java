/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.caption;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.sdk.external.struct.form.ResourceForm;
import com.aliyun.svideo.editor.effects.control.EffectInfo;
import com.aliyun.svideo.editor.effects.control.OnItemClickListener;
import com.aliyun.svideo.base.widget.CircularImageView;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    private Context mContext;
    private OnItemClickListener mItemClick;
    private ArrayList<ResourceForm> data = new ArrayList<>();
    private OnMoreClickListener mMoreClickListener;
    private int mSelectedPosition = 0;
    //是否显示字体分类
    private boolean mIsShowFontCategory;
    private static final int VIEW_TYPE_SELECTED = 1;
    private static final int VIEW_TYPE_UNSELECTED = 2;

    public CategoryAdapter(Context context) {
        this.mContext = context;
    }

    public void setData(ArrayList<ResourceForm> data) {
        if (data == null) {
            return;
        }
        this.data = data;
        if (mIsShowFontCategory) {
            ResourceForm resourceForm = new ResourceForm();
            this.data.add(0,resourceForm);
        }
        notifyDataSetChanged();
    }

    public void addShowFontCategory(){

        mIsShowFontCategory = true;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.aliyun_svideo_category_item_view, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final CategoryViewHolder categoryViewHolder = (CategoryViewHolder)holder;
        ResourceForm form = data.get(position);
        int viewType = getItemViewType(position);
        if (position == 0 && mIsShowFontCategory){
            categoryViewHolder.mName.setVisibility(View.VISIBLE);
            categoryViewHolder.mImage.setVisibility(View.GONE);
            categoryViewHolder.mName.setText(R.string.caption_effect_font_category_name);
        }else if (form.isMore()) {
            categoryViewHolder.mName.setVisibility(View.GONE);
            categoryViewHolder.mImage.setVisibility(View.VISIBLE);
            categoryViewHolder.mImage.setImageResource(R.mipmap.aliyun_svideo_more);
        } else {
            categoryViewHolder.mImage.setVisibility(View.GONE);
            categoryViewHolder.mName.setVisibility(View.VISIBLE);
            categoryViewHolder.mName.setText(form.getName());
            /*Glide.with(mContext).load(form.getIcon()).into(
                new ViewTarget<CircularImageView, GlideDrawable>(categoryViewHolder.mImage) {
                    @Override
                    public void onResourceReady(GlideDrawable glideDrawable,
                                                GlideAnimation<? super GlideDrawable> glideAnimation) {
                        categoryViewHolder.mImage.setImageBitmap(((GlideBitmapDrawable)glideDrawable).getBitmap());
                    }
                });*/
        }
        categoryViewHolder.itemView.setTag(holder);
        categoryViewHolder.itemView.setOnClickListener(this);
        switch (viewType) {
            case VIEW_TYPE_SELECTED:
                categoryViewHolder.itemView.setSelected(true);
                break;
            case VIEW_TYPE_UNSELECTED:
                categoryViewHolder.itemView.setSelected(false);
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void selectPosition(int categoryIndex) {
        int lasPos = mSelectedPosition;
        mSelectedPosition = categoryIndex;
        notifyItemChanged(mSelectedPosition);
        notifyItemChanged(lasPos);
    }

    private static class CategoryViewHolder extends RecyclerView.ViewHolder {

        CircularImageView mImage;
        TextView mName;

        public CategoryViewHolder(View itemView) {
            super(itemView);
            mImage = itemView.findViewById(R.id.category_image_source);
            mName = itemView.findViewById(R.id.tv_category_name_source);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClick = listener;
    }

    @Override
    public void onClick(View view) {
        CategoryViewHolder viewHolder = (CategoryViewHolder)view.getTag();
        int position = viewHolder.getAdapterPosition();
        ResourceForm form = data.get(position);
        if (form.isMore()) {
            if (mMoreClickListener != null) {
                mMoreClickListener.onMoreClick();
            }
        } else {
            if (mItemClick != null) {
                EffectInfo effectInfo = new EffectInfo();
                effectInfo.isCategory = true;
                int lastPos = mSelectedPosition;
                mSelectedPosition = viewHolder.getAdapterPosition();
                mItemClick.onItemClick(effectInfo, viewHolder.getAdapterPosition());
                notifyItemChanged(lastPos);
                notifyItemChanged(mSelectedPosition);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mSelectedPosition) {
            return VIEW_TYPE_SELECTED;
        } else {
            return VIEW_TYPE_UNSELECTED;
        }
    }

    public void setMoreClickListener(OnMoreClickListener moreClickListener) {
        mMoreClickListener = moreClickListener;
    }

    public interface OnMoreClickListener {
        void onMoreClick();
    }
}
