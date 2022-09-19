/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.caption.adapter;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.bean.AlivcEditMenuBean;
import com.aliyun.svideo.editor.bean.AlivcEditMenus;

import java.util.List;

public class BottomMenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<AlivcEditMenuBean> data;
    private OnItemClickListener mItemClick;


    public void setData(List<AlivcEditMenuBean> data) {
        if (data == null) {
            return;
        }
        this.data = data;
        notifyDataSetChanged();
    }

    public void clearData() {
        data.clear();
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClick = listener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MenuViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.alivc_editor_item_menu, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final MenuViewHolder menuViewHolder = (MenuViewHolder) holder;

        menuViewHolder.menuTv.setText(data.get(position).menuName);
        int resourceId = data.get(position).resourceId;
        Drawable drawable = ((MenuViewHolder) holder).menuTv.getContext().getResources().getDrawable(resourceId);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        menuViewHolder.menuTv.setCompoundDrawables(null, drawable, null, null);
        menuViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClick != null) {
                    mItemClick.onItemClick(AlivcEditMenus.AddText);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }


    private static class MenuViewHolder extends RecyclerView.ViewHolder {

        private final TextView menuTv;

        public MenuViewHolder(View itemView) {
            super(itemView);
            menuTv = itemView.findViewById(R.id.tv_roll_caption_subtitle);

        }
    }

    public interface OnItemClickListener {
        void onItemClick(AlivcEditMenus alivcEditMenus);
    }
}
