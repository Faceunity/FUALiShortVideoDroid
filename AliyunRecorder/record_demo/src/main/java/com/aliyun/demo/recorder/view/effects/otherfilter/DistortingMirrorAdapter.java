package com.aliyun.demo.recorder.view.effects.otherfilter;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.demo.R;

import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by hyj on 2018/11/5.
 */

public class DistortingMirrorAdapter extends RecyclerView.Adapter<DistortingMirrorAdapter.HomeRecyclerHolder> {
    private Context mContext;
    private int mEffectType;
    private List<Effect> mEffects;
    private int mPositionSelect = 0;
    private OnItemListener onItemListener;

    public DistortingMirrorAdapter(Context context, int effectType) {
        mContext = context;
        mEffectType = effectType;
        mEffects = EffectEnum.getEffectsByEffectType(mEffectType);
    }

    @Override
    public HomeRecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HomeRecyclerHolder(LayoutInflater.from(mContext).inflate(R.layout.distorting_mirror_item, parent, false));
    }

    @Override
    public void onBindViewHolder(HomeRecyclerHolder holder, final int position) {
        holder.effectImg.setImageResource(mEffects.get(position).resId());
        holder.effectImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPositionSelect == position) {
                    return;
                }
                Effect click = mEffects.get(mPositionSelect = position);
                //mOnFUControlListener.onEffectSelected(click);
                if (onItemListener != null)
                    onItemListener.onPosition(position, click);
//                playMusic(click);
                notifyDataSetChanged();
                if (onDescriptionChangeListener != null)
                    onDescriptionChangeListener.onDescriptionChangeListener(click.description());
            }
        });
        if (mPositionSelect == position) {
            holder.effectImg.setBackgroundResource(R.drawable.effect_select);
        } else {
            holder.effectImg.setBackgroundResource(0);
        }
    }

//    public void onResume() {
//        playMusic(mEffects.get(mPositionSelect));
//    }

//    public void onPause() {
//        stopMusic();
//    }


    @Override
    public int getItemCount() {
        return mEffects.size();
    }

    public void setOnItemListener(OnItemListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    class HomeRecyclerHolder extends RecyclerView.ViewHolder {

        CircleImageView effectImg;

        public HomeRecyclerHolder(View itemView) {
            super(itemView);
            effectImg = (CircleImageView) itemView.findViewById(R.id.effect_recycler_img);
        }
    }

    public void setSelected(int mSelected) {
        this.mPositionSelect = mSelected;
    }

    public void clearBundle() {
        if (mPositionSelect == 0) {
            return;
        }
        mPositionSelect = 0;
        Effect click = mEffects.get(mPositionSelect);
        //mOnFUControlListener.onEffectSelected(click);
        if (onItemListener != null)
            onItemListener.onPosition(0, click);
        notifyDataSetChanged();
    }

    public interface OnItemListener {
        void onPosition(int position, Effect effect);
    }

    private OnDescriptionChangeListener onDescriptionChangeListener;

    public void setOnDescriptionChangeListener(OnDescriptionChangeListener onDescriptionChangeListener) {
        this.onDescriptionChangeListener = onDescriptionChangeListener;
    }

    public interface OnDescriptionChangeListener {
        void onDescriptionChangeListener(int description);
    }

    public interface OnMusicListener {
        void onMusic(long time);
    }


    private OnMusicListener mOnMusicListener;

    public void setmOnMusicListener(OnMusicListener mOnMusicListener) {
        this.mOnMusicListener = mOnMusicListener;
    }
}
