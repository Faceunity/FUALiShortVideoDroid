/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.imv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effectmanager.MoreMVActivity;
import com.aliyun.svideo.editor.effects.control.BaseChooser;
import com.aliyun.svideo.editor.effects.control.EffectInfo;
import com.aliyun.svideo.editor.effects.control.OnItemClickListener;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.aliyun.svideo.base.widget.CircularImageView;
import com.aliyun.svideo.sdk.external.struct.form.IMVForm;
import com.aliyun.video.common.utils.image.ImageLoaderImpl;
import com.aliyun.video.common.utils.image.AbstractImageLoaderTarget;

import java.util.ArrayList;
import java.util.List;

/**
 * mv效果选择页面adapter
 */
public class ImvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener{

    private Context mContext;
    private OnItemClickListener mItemClick;

    List<IMVForm> mDataList = new ArrayList<>();

    private static final int EFFECT_NONE = 0;
    private static final int EFFECT_RESOURCE = 1;
    private static final int EFFECT_MORE = 2;

    private int mSelectedPos = 0;
    private IMVViewHolder mSelectedHolder;
    private int selectId;

    public ImvAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.aliyun_svideo_resources_item_view, parent, false);
        IMVViewHolder iMVViewHolder = new IMVViewHolder(view);
        iMVViewHolder.frameLayout = (FrameLayout) view.findViewById(R.id.resource_image);
        return iMVViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final IMVViewHolder iMVViewHolder = (IMVViewHolder) holder;
        String name = mContext.getString(R.string.none_effect);
        int viewType = getItemViewType(position);
        iMVViewHolder.itemView.setOnClickListener(this);
        if(viewType == EFFECT_NONE) {

            new ImageLoaderImpl().loadImage(mContext,R.mipmap.aliyun_svideo_none)
                .into(iMVViewHolder.mImage, new AbstractImageLoaderTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource) {
                        iMVViewHolder.mImage.setImageDrawable(resource);
                    }
                });
        } else if (viewType == EFFECT_MORE) {
            name = mContext.getString(R.string.more);

            new ImageLoaderImpl().loadImage(mContext,R.mipmap.aliyun_svideo_more)
                .into(iMVViewHolder.mImage, new AbstractImageLoaderTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource) {
                        iMVViewHolder.mImage.setImageDrawable(resource);
                    }
                });
            iMVViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                //启动more界面
                Intent moreIntent = new Intent(mContext, MoreMVActivity.class);
                moreIntent.putExtra(MoreMVActivity.SELECTD_ID,selectId);
                ((Activity)mContext).startActivityForResult(moreIntent, BaseChooser.IMV_REQUEST_CODE);
                }
            });
        } else {
            IMVForm imvForm = mDataList.get(position);
            name = imvForm.getName();

            new ImageLoaderImpl().loadImage(mContext,imvForm.getIcon())
                .into(iMVViewHolder.mImage, new AbstractImageLoaderTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource) {
                        iMVViewHolder.mImage.setImageDrawable(resource);
                    }
                });
        }
        if(mSelectedPos == position) {

            iMVViewHolder.mImage.setVisibility(View.GONE);
            iMVViewHolder.mIvSelectState.setVisibility(View.VISIBLE);
            mSelectedHolder = iMVViewHolder;
        } else {
            iMVViewHolder.mImage.setVisibility(View.VISIBLE);
            iMVViewHolder.mIvSelectState.setVisibility(View.GONE);
        }
        iMVViewHolder.mName.setText(name);
        iMVViewHolder.itemView.setTag(holder);

    }

    @Override
    public int getItemCount() {
        return mDataList.size() + 1;
    }

    public static class IMVViewHolder extends RecyclerView.ViewHolder{
        FrameLayout frameLayout;
        CircularImageView mImage;
        ImageView mIvSelectState;
        TextView mName;
        public IMVViewHolder(View itemView) {
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
        if(mItemClick != null) {
            IMVViewHolder viewHolder = (IMVViewHolder) view.getTag();
            int position = viewHolder.getAdapterPosition();
            if(mSelectedPos != position) {
                setEffectInfo(viewHolder, position);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        int type = EFFECT_RESOURCE;
        if(position == 0) {
            type = EFFECT_NONE;
        } else if(position == getItemCount() - 1) {
            type = EFFECT_MORE;
        }
        return type;
    }

    public void setData(List<IMVForm> data) {
        if(data == null){
            return;
        }
        mDataList = data;
        notifyDataSetChanged();
    }

    public void setEffectInfo(IMVViewHolder holder, int index) {
        if(mSelectedPos != index && holder != null) {
            if (mSelectedHolder!=null){
                mSelectedHolder.mImage.setVisibility(View.VISIBLE);
                mSelectedHolder.mIvSelectState.setVisibility(View.GONE);
            }
            holder.mImage.setVisibility(View.GONE);
            holder.mIvSelectState.setVisibility(View.VISIBLE);
            mSelectedPos = index;
            mSelectedHolder = holder;
            setEffecteffective(index);
        }
    }

    /**
     * 根据下标获取数据, 回调itemclick
     * @param index 索引
     */
    public void setEffecteffective(int index) {
        if (index < 0 || index >= mDataList.size()) {
            return;
        }
        EffectInfo effectInfo = new EffectInfo();
        effectInfo.type = UIEditorPage.MV;
        effectInfo.list = mDataList.get(index).getAspectList();
        effectInfo.id = mDataList.get(index).getId();
        selectId = effectInfo.id;
        if (mItemClick != null) {
            mItemClick.onItemClick(effectInfo, effectInfo.id);
        }
    }

    /**
     * 设置数据更新
     * @param index
     */
    public void setEffecteffectiveAndNotify(int index){
        notifyItemChanged(mSelectedPos);
        notifyItemChanged(index);
        mSelectedPos = index;
        setEffecteffective(index);

    }

    public void setSelectedPos(int position) {
        mSelectedPos = position;
    }
}
