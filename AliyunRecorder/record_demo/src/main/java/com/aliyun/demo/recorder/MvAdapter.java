package com.aliyun.demo.recorder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.aliyun.demo.R;
import com.aliyun.quview.CircularImageView;
import com.aliyun.struct.effect.EffectBean;
import com.aliyun.struct.form.IMVForm;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aa on 2018/1/11.
 */

public class MvAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {


    interface OnItemClickListener {
        boolean onItemClick(MvForm effectInfo, int index);
    }

    private Context mContext;
    private OnItemClickListener mItemClick;
    private int mSelectedPos;
    private IMVViewHolder mSelectedHolder;

    List<IMVForm> mDataList = new ArrayList<>();

    private static final int EFFECT_NONE = 0;
    private static final int EFFECT_RESOURCE = 1;

    public MvAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_mv_item, parent, false);
        IMVViewHolder iMVViewHolder = new IMVViewHolder(view);
        iMVViewHolder.frameLayout = (FrameLayout) view.findViewById(R.id.resource_image);
        return iMVViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final IMVViewHolder iMVViewHolder = (IMVViewHolder) holder;
        int viewType = getItemViewType(position);
        iMVViewHolder.itemView.setOnClickListener(this);
        if(viewType == EFFECT_NONE) {
            Glide.with(mContext).load(R.mipmap.aliyun_video_icon_raw_effect).into(new ViewTarget<CircularImageView, GlideDrawable>(iMVViewHolder.mImage) {
                @Override
                public void onResourceReady(GlideDrawable glideDrawable, GlideAnimation<? super GlideDrawable> glideAnimation) {
                    iMVViewHolder.mImage.setImageBitmap(((GlideBitmapDrawable) glideDrawable).getBitmap());
                }
            });
        } else {
            IMVForm imvForm = mDataList.get(position);
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
        iMVViewHolder.itemView.setTag(holder);

    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class IMVViewHolder extends RecyclerView.ViewHolder{
        FrameLayout frameLayout;
        CircularImageView mImage;
//        TextView mName;
        public IMVViewHolder(View itemView) {
            super(itemView);
            mImage = (CircularImageView) itemView.findViewById(R.id.resource_image_view);
//            mName = (TextView) itemView.findViewById(R.id.resource_name);
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
        if(holder != null) {
            if(mSelectedHolder != null){
                mSelectedHolder.mImage.setSelected(false);
                holder.mImage.setSelected(true);
            }

            mSelectedPos = index;
            mSelectedHolder = holder;
            setEffecteffective(index);
        }
    }

    public void setEffecteffective(int index) {
        MvForm effectInfo = new MvForm();
        effectInfo.list = mDataList.get(index).getAspectList();
        effectInfo.id = mDataList.get(index).getId();
        mItemClick.onItemClick(effectInfo, effectInfo.id);
    }

    public int getSelectedEffectIndex(){
        return mSelectedPos;
    }

    public void setSelectedEffectIndex(int index){
        mSelectedPos = index;
    }

}
