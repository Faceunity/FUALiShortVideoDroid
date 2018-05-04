/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.effects.imv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;
import com.aliyun.demo.editor.R;
import com.aliyun.demo.effectmanager.MoreMVActivity;
import com.aliyun.demo.effects.control.BaseChooser;
import com.aliyun.demo.effects.control.EffectInfo;
import com.aliyun.demo.effects.control.OnItemClickListener;
import com.aliyun.demo.effects.control.UIEditorPage;
import com.aliyun.quview.CircularImageView;
import com.aliyun.struct.form.IMVForm;

import java.util.ArrayList;
import java.util.List;

public class ImvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener{

    private Context mContext;
    private OnItemClickListener mItemClick;

    List<IMVForm> mDataList = new ArrayList<>();

    private static final int EFFECT_NONE = 0;
    private static final int EFFECT_RESOURCE = 1;
    private static final int EFFECT_MORE = 2;

    private int mSelectedPos = 0;
    private IMVViewHolder mSelectedHolder;

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
            Glide.with(mContext).load(R.mipmap.aliyun_svideo_none).into(new ViewTarget<CircularImageView, GlideDrawable>(iMVViewHolder.mImage) {
                @Override
                public void onResourceReady(GlideDrawable glideDrawable, GlideAnimation<? super GlideDrawable> glideAnimation) {
                    iMVViewHolder.mImage.setImageBitmap(((GlideBitmapDrawable) glideDrawable).getBitmap());
                }
            });
        } else if (viewType == EFFECT_MORE) {
            name = mContext.getString(R.string.more);
            Glide.with(mContext).load(R.mipmap.aliyun_svideo_more).into(new ViewTarget<CircularImageView, GlideDrawable>(iMVViewHolder.mImage) {
                @Override
                public void onResourceReady(GlideDrawable glideDrawable, GlideAnimation<? super GlideDrawable> glideAnimation) {
                    iMVViewHolder.mImage.setImageBitmap(((GlideBitmapDrawable) glideDrawable).getBitmap());
                }
            });
            iMVViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                //启动more界面
                Intent moreIntent = new Intent(mContext, MoreMVActivity.class);
                ((Activity)mContext).startActivityForResult(moreIntent, BaseChooser.IMV_REQUEST_CODE);
                }
            });
        } else {
            IMVForm imvForm = mDataList.get(position);
            name = imvForm.getName();
            Glide.with(mContext).load(imvForm.getIcon()).into(new ViewTarget<CircularImageView, GlideDrawable>(iMVViewHolder.mImage) {
                @Override
                public void onResourceReady(GlideDrawable glideDrawable, GlideAnimation<? super GlideDrawable> glideAnimation) {
                    iMVViewHolder.mImage.setImageBitmap(((GlideBitmapDrawable) glideDrawable).getBitmap());
                }
            });

        }
        if(mSelectedPos == position) {
            iMVViewHolder.mImage.setSelected(true);
            mSelectedHolder = iMVViewHolder;
        } else {
            iMVViewHolder.mImage.setSelected(false);
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
        TextView mName;
        public IMVViewHolder(View itemView) {
            super(itemView);
            mImage = (CircularImageView) itemView.findViewById(R.id.resource_image_view);
            mName = (TextView) itemView.findViewById(R.id.resource_name);
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
            mSelectedHolder.mImage.setSelected(false);
            holder.mImage.setSelected(true);
            mSelectedPos = index;
            mSelectedHolder = holder;
            setEffecteffective(index);
        }
    }

    public void setEffecteffective(int index) {
        EffectInfo effectInfo = new EffectInfo();
        effectInfo.type = UIEditorPage.MV;
        effectInfo.list = mDataList.get(index).getAspectList();
        effectInfo.id = mDataList.get(index).getId();
        mItemClick.onItemClick(effectInfo, effectInfo.id);
    }

    public void setSelectedPos(int position) {
        mSelectedPos = position;
    }
}
