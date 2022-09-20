/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.lut;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.svideo.base.widget.CircularImageView;
import com.aliyun.svideo.common.utils.image.AbstractImageLoaderTarget;
import com.aliyun.svideo.common.utils.image.ImageLoaderImpl;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.contant.EditorConstants;
import com.aliyun.svideo.editor.effects.control.EffectInfo;
import com.aliyun.svideo.editor.effects.control.OnItemClickListener;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.aliyun.svideo.editor.util.AlivcResUtil;
import com.aliyun.svideosdk.common.struct.project.Source;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements View.OnClickListener {

    private Context mContext;
    private OnItemClickListener mItemClick;
    private int mSelectedPos = 0;
    private FilterViewHolder mSelectedHolder;
    private List<String> mFilterList = new ArrayList<>();

    public LutAdapter(Context context) {
        this.mContext = context;
    }

    public void setSelectedPos(final int selectedPos) {
        mSelectedPos = selectedPos;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.alivc_editor_item_effect, parent, false);
        FilterViewHolder filterViewHolder = new FilterViewHolder(view);
        filterViewHolder.frameLayout = (FrameLayout) view.findViewById(R.id.resource_image);
        return filterViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final FilterViewHolder filterViewHolder = (FilterViewHolder) holder;
        String name = mContext.getString(R.string.alivc_editor_dialog_none_effect);
        String path = mFilterList.get(position);
        if (EditorConstants.EFFECT_FILTER_LOCAL_LUT_CLEAR.equals(path)) {
            filterViewHolder.mImage.setImageResource(R.drawable.alivc_svideo_effect_none);
            name = mContext.getString(R.string.alivc_svideo_filter_lut_remove);
        } else if (EditorConstants.EFFECT_FILTER_LOCAL_LUT_ADD.equals(path)) {
            filterViewHolder.mImage.setImageResource(R.drawable.alivc_svideo_effect_local_lut);
            name = mContext.getString(R.string.alivc_svideo_filter_lut_add);
        } else {
            //后续内置lut文件
            String iconPath = "";
            int index = path.lastIndexOf("/");
            if (index > 0) {
                String dirPath = path.substring(0, index);
                iconPath = dirPath + File.separator + "icon.png";
                int seqIndex = dirPath.lastIndexOf("/");
                if (seqIndex > 0) {
                    name = EditorConstants.LUT_FILE_SEQ_TO_NAME.get(dirPath.substring(seqIndex + 1));
                }
            } else {
                name = "";
            }
            if (filterViewHolder != null) {
                new ImageLoaderImpl().loadImage(mContext, iconPath).into(filterViewHolder.mImage, new AbstractImageLoaderTarget<Drawable>() {

                    @Override
                    public void onResourceReady(@NonNull Drawable resource) {
                        filterViewHolder.mImage.setImageDrawable(resource);
                    }
                });

            }
        }

        if (mSelectedPos > mFilterList.size()) {
            mSelectedPos = 0;
        }

        if (mSelectedPos == position) {
            filterViewHolder.mImage.setVisibility(View.GONE);
            filterViewHolder.mIvSelectState.setVisibility(View.VISIBLE);
            mSelectedHolder = filterViewHolder;
        } else {
            filterViewHolder.mImage.setVisibility(View.VISIBLE);
            filterViewHolder.mIvSelectState.setVisibility(View.GONE);
        }
        filterViewHolder.mName.setText(name);
        filterViewHolder.itemView.setTag(holder);
        filterViewHolder.itemView.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return mFilterList.size();
    }


    private static class FilterViewHolder extends RecyclerView.ViewHolder {

        FrameLayout frameLayout;
        ImageView mIvSelectState;
        CircularImageView mImage;
        TextView mName;

        public FilterViewHolder(View itemView) {
            super(itemView);
            mImage = (CircularImageView) itemView.findViewById(R.id.resource_image_view);
            mName = (TextView) itemView.findViewById(R.id.resource_name);
            mIvSelectState = itemView.findViewById(R.id.iv_select_state);
        }

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClick = listener;
    }


    @Override
    public void onClick(View view) {
        if (mItemClick != null) {
            FilterViewHolder viewHolder = (FilterViewHolder) view.getTag();
            int position = viewHolder.getAdapterPosition();
            if (mSelectedPos != position) {
                if (mSelectedHolder != null && mSelectedHolder.mImage != null) {
                    mSelectedHolder.mImage.setVisibility(View.VISIBLE);
                    mSelectedHolder.mIvSelectState.setVisibility(View.GONE);
                }
                viewHolder.mImage.setVisibility(View.GONE);
                viewHolder.mIvSelectState.setVisibility(View.VISIBLE);
                mSelectedPos = position;
                mSelectedHolder = viewHolder;

                EffectInfo effectInfo = new EffectInfo();
                effectInfo.type = UIEditorPage.FILTER_EFFECT;
                String path = mFilterList.get(position);
                Source source = new Source(path);
                int index = path.lastIndexOf("/");
                if (index > 0) {
                    String dirPath = path.substring(0, index);
                    int seqIndex = dirPath.lastIndexOf("/");
                    if (seqIndex > 0) {
                        String dirName = dirPath.substring(seqIndex + 1);
                        source.setURL(AlivcResUtil.getAppResUri(AlivcResUtil.TYPE_LUT, "0", dirName));

                    }
                }
                source.setId(String.valueOf(position));
                effectInfo.setSource(source);
                effectInfo.id = position;
                mItemClick.onItemClick(effectInfo, position);
            }
        }
    }

    public void setDataList(List<String> list) {
        mFilterList.clear();
        mFilterList.add(EditorConstants.EFFECT_FILTER_LOCAL_LUT_CLEAR);
        mFilterList.add(EditorConstants.EFFECT_FILTER_LOCAL_LUT_ADD);
        if (list != null && !list.isEmpty()) {
            mFilterList.addAll(list);
        }
    }


}
