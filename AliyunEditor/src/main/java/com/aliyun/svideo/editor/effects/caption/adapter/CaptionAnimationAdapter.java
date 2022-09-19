package com.aliyun.svideo.editor.effects.caption.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.contant.CaptionConfig;
import com.aliyun.svideo.editor.editor.EditorActivity;

public class CaptionAnimationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private OnItemClickListener mOnItemClickListerner;
    private int mSelectPosition = 0;

    public CaptionAnimationAdapter(Context context) {
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        /**
         * 复用了转场的layout.xml文件
         */
        View item = ((EditorActivity) mContext).getLayoutInflater().inflate(R.layout.alivc_editor_item_transition_effect, null);

        return new TransitionViewEffectHolder(item);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        TransitionViewEffectHolder effectHolder = (TransitionViewEffectHolder) holder;
        switch (position) {
        case CaptionConfig.EFFECT_NONE:
            effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_none_effect_selector);
            effectHolder.mEffectName.setText(R.string.alivc_editor_dialog_none);
            break;
        case CaptionConfig.EFFECT_UP:
            effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_translate_up_effect_selector);
            effectHolder.mEffectName.setText(R.string.alivc_editor_dialog_effect_translate_up);
            break;
        case CaptionConfig.EFFECT_DOWN:
            effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_translate_down_effect_selector);
            effectHolder.mEffectName.setText(R.string.alivc_editor_dialog_effect_translate_down);
            break;
        case CaptionConfig.EFFECT_LEFT:
            effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_translate_left_effect_selector);
            effectHolder.mEffectName.setText(R.string.alivc_editor_dialog_effect_translate_left);
            break;
        case CaptionConfig.EFFECT_RIGHT:
            effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_translate_right_effect_selector);
            effectHolder.mEffectName.setText(R.string.alivc_editor_dialog_effect_translate_right);
            break;
        case CaptionConfig.EFFECT_FADE:
            effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_fade_effect_selector);
            effectHolder.mEffectName.setText(R.string.alivc_editor_dialog_effect_fade);
            break;
        case CaptionConfig.EFFECT_LINEARWIPE:
            effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_linearwipe_effect_selector);
            effectHolder.mEffectName.setText(R.string.alivc_editor_dialog_effect_translate_linearwipe);
            break;
        case CaptionConfig.EFFECT_SCALE:
            effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_fade_effect_selector);
            effectHolder.mEffectName.setText(R.string.alivc_editor_dialog_effect_translate_effect_scale);
            break;
        case CaptionConfig.EFFECT_PRINT:
            effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_fade_effect_selector);
            effectHolder.mEffectName.setText(R.string.alivc_editor_dialog_effect_printer);
            break;

        case CaptionConfig.EFFECT_ROTATE_BY:
            effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_fade_effect_selector);
            effectHolder.mEffectName.setText(R.string.alivc_editor_dialog_effect_rotate_by);
            break;
        case CaptionConfig.EFFECT_ROTATE_TO:
            effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_fade_effect_selector);
            effectHolder.mEffectName.setText(R.string.alivc_editor_dialog_effect_rotate_to);
            break;
        case CaptionConfig.EFFECT_SET1:
            effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_fade_effect_selector);
            effectHolder.mEffectName.setText(R.string.alivc_editor_dialog_effect_set1);
            break;
        case CaptionConfig.EFFECT_SET2:
            effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_fade_effect_selector);
            effectHolder.mEffectName.setText(R.string.alivc_editor_dialog_effect_set2);
            break;
        case CaptionConfig.EFFECT_WAVE:
            effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_fade_effect_selector);
            effectHolder.mEffectName.setText(R.string.alivc_editor_dialog_effect_wave);
            break;
        case CaptionConfig.EFFECT_ROTATE_IN:
            effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_fade_effect_selector);
            effectHolder.mEffectName.setText(R.string.alivc_editor_dialog_effect_rotate_in);
            break;
        case CaptionConfig.EFFECT_HEAT:
            effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_fade_effect_selector);
            effectHolder.mEffectName.setText(R.string.alivc_editor_dialog_effect_heat);
            break;
        case CaptionConfig.EFFECT_ROUNDSCAN:
            effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_fade_effect_selector);
            effectHolder.mEffectName.setText(R.string.alivc_editor_dialog_effect_roundscan);
            break;
        case CaptionConfig.EFFECT_WAVE_JUMP:
            effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_fade_effect_selector);
            effectHolder.mEffectName.setText(R.string.alivc_editor_dialog_effect_wave_jump);
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
        notifySelectedView(selectPosition);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListerner = listener;
    }

    @Override
    public int getItemCount() {
        return CaptionConfig.POSITION_FONT_ANIM_ARRAY.length;
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
                        int adapterPosition = getAdapterPosition();
                        if (notifySelectedView(adapterPosition)) {
                            mOnItemClickListerner.onItemClick(getAdapterPosition());
                        }
                    }

                }
            });
            mEffectName = itemView.findViewById(R.id.tv_effect_name);
        }
    }

    private boolean notifySelectedView(int adapterPosition) {
        if (adapterPosition != mSelectPosition) {
            int last = mSelectPosition;
            mSelectPosition = adapterPosition;
            notifyItemChanged(last);
            notifyItemChanged(mSelectPosition);
            return true;
        } else {
            return false;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int postion);
    }
}
