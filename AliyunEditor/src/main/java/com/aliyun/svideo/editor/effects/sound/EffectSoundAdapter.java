package com.aliyun.svideo.editor.effects.sound;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.aliyun.svideo.base.widget.CircularImageView;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.control.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

class EffectSoundAdapter extends RecyclerView.Adapter<EffectSoundAdapter.SoundHolder> {
    private Context mContext;
    private OnItemClickListener mClickListener;
    private List<SoundEffectInfo> mSoundList = new ArrayList<>();
    private int mSelectedPos;

    public EffectSoundAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public SoundHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SoundHolder(LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.alivc_editor_item_effcet_sound, parent, false));
    }

    @Override
    public void onBindViewHolder(final SoundHolder holder, final int position) {
        holder.mSoundImage.setImageResource(R.drawable.alivc_svideo_effect_none);
        holder.mSoundName.setText(mSoundList.get(position).soundNameId);
        holder.mSoundImage.setImageDrawable(ContextCompat.getDrawable(mContext, mSoundList.get(position).imgIcon));

        if (mSelectedPos > mSoundList.size()) {
            mSelectedPos = 0;
        }

        if (mSelectedPos == position) {
            holder.mSoundImage.setVisibility(View.GONE);
            holder.mSoundSelectState.setVisibility(View.VISIBLE);
        } else {
            holder.mSoundImage.setVisibility(View.VISIBLE);
            holder.mSoundSelectState.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mSoundList != null ? mSoundList.size() : 0;
    }


    public void setDataList(List<SoundEffectInfo> soundList) {
        mSoundList.clear();
        mSoundList.addAll(soundList);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mClickListener = onItemClickListener;
    }

    class SoundHolder extends RecyclerView.ViewHolder {
        ImageView mSoundSelectState;
        CircularImageView mSoundImage;
        TextView mSoundName;
        SoundHolder(View itemView) {
            super(itemView);
            mSoundImage =  itemView.findViewById(R.id.effect_sound_image_view);
            mSoundName =  itemView.findViewById(R.id.effect_sound_name);
            mSoundSelectState = itemView.findViewById(R.id.iv_effect_select_state);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mClickListener != null) {
                        int position = getAdapterPosition();
                        if (mSelectedPos != position) {
                            mSelectedPos = position;
                            mClickListener.onItemClick(mSoundList.get(position), mSelectedPos);
                            notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    }
}
