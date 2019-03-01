package com.aliyun.svideo.editor.effects.caption;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.editor.EditorActivity;

public class FontAnimationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private TextDialog.OnItemClickListener mOnItemClickListerner;
    private int mSelectPosition = -1;

    public FontAnimationAdapter(Context context) {
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        /**
         * 复用了转场的layout.xml文件
         */
        View item = ((EditorActivity)mContext).getLayoutInflater().inflate(R.layout.aliyun_svideo_transition_effect_item_view, null);

        return new TransitionViewEffectHolder(item);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        TransitionViewEffectHolder effectHolder = (TransitionViewEffectHolder)holder;
        switch (position) {
            case TextDialog.EFFECT_NONE:
                effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_none_effect_selector);
                effectHolder.mEffectName.setText(R.string.aliyun_svideo_transition_effect_none);
                break;
            case TextDialog.EFFECT_UP:
                effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_translate_up_effect_selector);
                effectHolder.mEffectName.setText(R.string.aliyun_svideo_transition_effect_translate_up);
                break;
            case TextDialog.EFFECT_DOWN:
                effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_translate_down_effect_selector);
                effectHolder.mEffectName.setText(R.string.aliyun_svideo_transition_effect_translate_down);
                break;
            case TextDialog.EFFECT_LEFT:
                effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_translate_left_effect_selector);
                effectHolder.mEffectName.setText(R.string.aliyun_svideo_transition_effect_translate_left);
                break;
            case TextDialog.EFFECT_RIGHT:
                effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_translate_right_effect_selector);
                effectHolder.mEffectName.setText(R.string.aliyun_svideo_transition_effect_translate_right);
                break;
            case TextDialog.EFFECT_FADE:
                effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_fade_effect_selector);
                effectHolder.mEffectName.setText(R.string.aliyun_svideo_transition_effect_fade);
                break;
            case TextDialog.EFFECT_LINEARWIPE:
                effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_linearwipe_effect_selector);
                effectHolder.mEffectName.setText(R.string.aliyun_svideo_transition_effect_linearwipe);
                break;
            case TextDialog.EFFECT_SCALE:
                effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_fade_effect_selector);
                effectHolder.mEffectName.setText(R.string.aliyun_svideo_transition_effect_scale);
                break;
            default:
                break;

        }
        if (position == mSelectPosition) {
            effectHolder.mEffectIcon.setSelected(true);
        } else {
            effectHolder.mEffectIcon.setSelected(false);
        }
    }

    public void setSelectPosition(int selectPosition) {
        mSelectPosition = selectPosition;
    }

    public void setOnItemClickListener(TextDialog.OnItemClickListener listener) {
        mOnItemClickListerner = listener;
    }

    @Override
    public int getItemCount() {
        return TextDialog.POSITION_FONT_ANIM_ARRAY.length;
    }

    class TransitionViewEffectHolder extends RecyclerView.ViewHolder {
        ImageView mEffectIcon;
        TextView mEffectName;

        TransitionViewEffectHolder(View itemView) {
            super(itemView);
            mEffectIcon = itemView.findViewById(R.id.iv_effect_icon);
            mEffectIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListerner != null) {
                        mOnItemClickListerner.onItemClick( getAdapterPosition());
                            mSelectPosition = getAdapterPosition();
                            notifyDataSetChanged();

                    }
                }
            });
            mEffectName = itemView.findViewById(R.id.tv_effect_name);
        }
    }
}
