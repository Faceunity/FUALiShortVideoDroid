/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.effects.filter;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.aliyun.demo.editor.R;
import com.aliyun.demo.effects.control.EffectInfo;
import com.aliyun.demo.effects.control.OnItemClickListener;
import com.aliyun.demo.effects.control.OnItemLongClickListener;
import com.aliyun.demo.effects.control.OnItemTouchListener;
import com.aliyun.demo.effects.control.UIEditorPage;
import com.aliyun.quview.CircularImageView;
import com.aliyun.struct.effect.EffectFilter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;

import java.util.ArrayList;
import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener {

    private Context mContext;
    private OnItemClickListener mItemClick;
    private OnItemLongClickListener mItemLongClick;
    private OnItemTouchListener mItemTouchListener;
    private int mSelectedPos = 0;
    private FilterViewHolder mSelectedHolder;
    private List<String> mFilterList = new ArrayList<>();

    public FilterAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.aliyun_svideo_resources_item_view, parent, false);
        FilterViewHolder filterViewHolder = new FilterViewHolder(view);
        filterViewHolder.frameLayout = (FrameLayout) view.findViewById(R.id.resource_image);
        return filterViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final FilterViewHolder filterViewHolder = (FilterViewHolder) holder;
        String name = mContext.getString(R.string.none_effect);
        String path = mFilterList.get(position);
        if (path == null || "".equals(path)) {
            Glide.with(mContext).load(R.mipmap.aliyun_svideo_none).into(new ViewTarget<CircularImageView, GlideDrawable>(filterViewHolder.mImage) {
                @Override
                public void onResourceReady(GlideDrawable glideDrawable, GlideAnimation<? super GlideDrawable> glideAnimation) {
                    filterViewHolder.mImage.setImageBitmap(((GlideBitmapDrawable) glideDrawable).getBitmap());
                }
            });
        } else {
            EffectFilter effectFilter = new EffectFilter(path);
            if (effectFilter != null) {
                name = effectFilter.getName();
                if (filterViewHolder != null) {
                    Glide.with(mContext).load(effectFilter.getPath() + "/icon.png").into(new ViewTarget<CircularImageView, GlideDrawable>(filterViewHolder.mImage) {
                        @Override
                        public void onResourceReady(GlideDrawable glideDrawable, GlideAnimation<? super GlideDrawable> glideAnimation) {
                            filterViewHolder.mImage.setImageBitmap(((GlideBitmapDrawable) glideDrawable).getBitmap());
                        }
                    });
                }
            }
        }

        if (mSelectedPos > mFilterList.size()) {
            mSelectedPos = 0;
        }

        if (mSelectedPos == position) {
            filterViewHolder.mImage.setSelected(true);
            mSelectedHolder = filterViewHolder;
        } else {
            filterViewHolder.mImage.setSelected(false);
        }
        filterViewHolder.mName.setText(name);
        filterViewHolder.itemView.setTag(holder);
        filterViewHolder.itemView.setOnClickListener(this);
        filterViewHolder.itemView.setOnLongClickListener(this);
        filterViewHolder.itemView.setOnTouchListener(this);
    }

    @Override
    public int getItemCount() {
        return mFilterList.size();
    }

    @Override
    public boolean onLongClick(View v) {
        if (mItemLongClick != null) {
            FilterViewHolder viewHolder = (FilterViewHolder) v.getTag();
            int position = viewHolder.getAdapterPosition();
            mSelectedHolder.mImage.setSelected(false);
            viewHolder.mImage.setSelected(true);
            mSelectedPos = position;
            mSelectedHolder = viewHolder;

            EffectInfo effectInfo = new EffectInfo();
            effectInfo.type = UIEditorPage.FILTER_EFFECT;
            effectInfo.setPath(mFilterList.get(position));
            effectInfo.id = position;
            mItemLongClick.onItemLongClick(effectInfo, position);
        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int actionMasked = MotionEventCompat.getActionMasked(event);
        switch (actionMasked) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mItemTouchListener != null) {
                    FilterViewHolder viewHolder = (FilterViewHolder) v.getTag();
                    int position = viewHolder.getAdapterPosition();
                    EffectInfo effectInfo = new EffectInfo();
                    effectInfo.type = UIEditorPage.FILTER_EFFECT;
                    effectInfo.setPath(mFilterList.get(position));
                    effectInfo.id = position;
                    mItemTouchListener.onTouchEvent(OnItemTouchListener.EVENT_UP,
                            position, effectInfo);
                }
        }
        return false;
    }

    private static class FilterViewHolder extends RecyclerView.ViewHolder {

        FrameLayout frameLayout;

        CircularImageView mImage;
        TextView mName;

        public FilterViewHolder(View itemView) {
            super(itemView);
            mImage = (CircularImageView) itemView.findViewById(R.id.resource_image_view);
            mName = (TextView) itemView.findViewById(R.id.resource_name);
        }

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClick = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener li) {
        mItemLongClick = li;
    }

    public void setOnItemTouchListener(OnItemTouchListener li) {
        mItemTouchListener = li;
    }

    @Override
    public void onClick(View view) {
        if (mItemClick != null) {
            FilterViewHolder viewHolder = (FilterViewHolder) view.getTag();
            int position = viewHolder.getAdapterPosition();
            if (mSelectedPos != position && mSelectedHolder != null) {
                mSelectedHolder.mImage.setSelected(false);
                viewHolder.mImage.setSelected(true);
                mSelectedPos = position;
                mSelectedHolder = viewHolder;

                EffectInfo effectInfo = new EffectInfo();
                effectInfo.type = UIEditorPage.FILTER_EFFECT;
                effectInfo.setPath(mFilterList.get(position));
                effectInfo.id = position;
                mItemClick.onItemClick(effectInfo, position);
            }
        }
    }

    public void setDataList(List<String> list) {
        mFilterList.clear();
        mFilterList.add(null);
        mFilterList.addAll(list);
    }

    public void setSelectedPos(int position) {
        mSelectedPos = position;
    }
}
