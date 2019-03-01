package com.aliyun.svideo.editor.effects.transition;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.editor.EditorActivity;
import com.aliyun.svideo.editor.effects.caption.TextDialog;
import com.aliyun.svideo.editor.effects.control.EffectInfo;
import com.aliyun.svideo.editor.effects.control.OnItemClickListener;
import com.aliyun.video.common.utils.FastClickUtil;

public class TransitionEffectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private OnItemClickListener mOnItemClickListener;
    private int mSelectPosition = 0;

    public TransitionEffectAdapter(Context context) {
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View item = ((EditorActivity)mContext).getLayoutInflater().inflate(R.layout.aliyun_svideo_transition_effect_item_view, null);

        return new TransitionViewEffectHolder(item);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        TransitionViewEffectHolder effectHolder = (TransitionViewEffectHolder)holder;
        switch (position) {
        case TransitionChooserView.EFFECT_NONE:
            effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_none_effect_selector);
            effectHolder.mEffectName.setText(R.string.aliyun_svideo_transition_effect_none);
            break;
        case TransitionChooserView.EFFECT_UP:
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
        case TransitionChooserView.EFFECT_RIGHT:
            effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_translate_right_effect_selector);
            effectHolder.mEffectName.setText(R.string.aliyun_svideo_transition_effect_translate_right);
            break;
        case TransitionChooserView.EFFECT_SHUTTER:
            effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_shutter_effect_selector);
            effectHolder.mEffectName.setText(R.string.aliyun_svideo_transition_effect_shutter);
            break;
        case TransitionChooserView.EFFECT_FADE:
            effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_fade_effect_selector);
            effectHolder.mEffectName.setText(R.string.aliyun_svideo_transition_effect_fade);
            break;
        case TransitionChooserView.EFFECT_FIVE_STAR:
            effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_fivepointstar_effect_selector);
            effectHolder.mEffectName.setText(R.string.aliyun_svideo_transition_effect_five_star);
            break;
        case TransitionChooserView.EFFECT_CIRCLE:
            effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_circle_effect_selector);
            effectHolder.mEffectName.setText(R.string.aliyun_svideo_transition_effect_circle);
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
        if (mSelectPosition != selectPosition) {
            mSelectPosition = selectPosition;
            notifyDataSetChanged();
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    public int getItemCount() {
        return TransitionChooserView.EFFECT_LIST.length;
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
                    if (FastClickUtil.isFastClick()) {
                        return;
                    }
                    if (mOnItemClickListener != null) {
                        EffectInfo info = new EffectInfo();
                        info.transitionType = getAdapterPosition();
                        if (mOnItemClickListener.onItemClick(info, getAdapterPosition())) {
                            mSelectPosition = getAdapterPosition();
                            notifyDataSetChanged();
                        }
                    }
                }
            });
            mEffectName = itemView.findViewById(R.id.tv_effect_name);
        }
    }

}
