package com.aliyun.svideo.editor.effects.transition;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.base.Form.I18nBean;
import com.aliyun.svideo.common.utils.LanguageUtils;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.editor.EditorActivity;
import com.aliyun.svideo.editor.effects.control.EffectInfo;
import com.aliyun.svideo.editor.effects.control.OnItemClickListener;
import com.aliyun.svideo.common.utils.FastClickUtil;
import com.aliyun.svideo.editor.util.AlivcResUtil;
import com.aliyun.svideo.editor.util.EditorCommon;
import com.aliyun.svideosdk.common.struct.project.Source;
import com.aliyun.svideosdk.common.struct.effect.TransitionBase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TransitionEffectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private OnItemClickListener mOnItemClickListener;
    private int mSelectPosition = 0;
    private List<String> mTransitionEffectList = new ArrayList<>();
    private int mGroupId;
    private boolean isDefault = true;

    public TransitionEffectAdapter(Context context) {
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View item = ((EditorActivity)mContext).getLayoutInflater().inflate(R.layout.alivc_editor_item_transition_effect, null);

        return new TransitionViewEffectHolder(item);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        TransitionViewEffectHolder effectHolder = (TransitionViewEffectHolder)holder;
        if (isDefault) {

            switch (position) {
            case TransitionChooserView.EFFECT_NONE:
                effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_none_effect_selector);
                effectHolder.mEffectName.setText(R.string.alivc_editor_dialog_none);
                break;
            case TransitionChooserView.EFFECT_UP:
                effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_translate_up_effect_selector);
                effectHolder.mEffectName.setText(R.string.alivc_editor_dialog_effect_translate_up);
                break;
            case TransitionChooserView.EFFECT_DOWN:
                effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_translate_down_effect_selector);
                effectHolder.mEffectName.setText(R.string.alivc_editor_dialog_effect_translate_down);
                break;
            case TransitionChooserView.EFFECT_LEFT:
                effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_translate_left_effect_selector);
                effectHolder.mEffectName.setText(R.string.alivc_editor_dialog_effect_translate_left);
                break;
            case TransitionChooserView.EFFECT_RIGHT:
                effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_translate_right_effect_selector);
                effectHolder.mEffectName.setText(R.string.alivc_editor_dialog_effect_translate_right);
                break;
            case TransitionChooserView.EFFECT_SHUTTER:
                effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_shutter_effect_selector);
                effectHolder.mEffectName.setText(R.string.alivc_editor_dialog_effect_shutter);
                break;
            case TransitionChooserView.EFFECT_FADE:
                effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_fade_effect_selector);
                effectHolder.mEffectName.setText(R.string.alivc_editor_dialog_effect_fade);
                break;
            case TransitionChooserView.EFFECT_FIVE_STAR:
                effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_fivepointstar_effect_selector);
                effectHolder.mEffectName.setText(R.string.alivc_editor_dialog_transition_star);
                break;
            case TransitionChooserView.EFFECT_CIRCLE:
                effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_circle_effect_selector);
                effectHolder.mEffectName.setText(R.string.alivc_editor_dialog_effect_circle);
                break;
            default:
                break;

            }

        } else {
            String path = mTransitionEffectList.get(position);
            if (path == null) {
                effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_none_effect_selector);
                effectHolder.mEffectName.setText(R.string.alivc_editor_dialog_none);
            } else {
                String name = getTransitionName(path);
                effectHolder.mEffectIcon.setImageResource(R.drawable.aliyun_svideo_video_edit_transition_custom_effect_selector);
                effectHolder.mEffectName.setText(name);
            }
        }
        if (position == mSelectPosition) {
            effectHolder.mEffectIcon.setSelected(true);
        } else {
            effectHolder.mEffectIcon.setSelected(false);
        }
    }

    public void setSelectPosition(int selectPosition) {
        if (isDefault) {
            mSelectPosition = selectPosition;
        } else {
            mSelectPosition = -1;
        }
        notifyDataSetChanged();
    }

    public void setCustomSelectPosition(String path) {
        if (isDefault) {
            mSelectPosition = -1;
        } else {
            mSelectPosition = mTransitionEffectList.indexOf(path);
        }
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    public int getItemCount() {
        return isDefault ? TransitionChooserView.EFFECT_LIST.length : mTransitionEffectList.size();
    }

    public void setData(int groupId, List<String> list) {
        mTransitionEffectList.clear();
        if (list == null) {
            isDefault = true;
            mGroupId = -1;
        } else {
            isDefault = false;
            mGroupId = groupId;
            mTransitionEffectList.addAll(list);
        }
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
                        int position = getAdapterPosition();
                        info.transitionType = position;
                        if (!isDefault ) {
                            String path = mTransitionEffectList.get(position);
                            TransitionBase transitionBase = new TransitionBase(path);
                            info.setPath(path);
                            Source source = new Source(path);
                            if (source.getPath() != null && source.getPath().contains(File.separator)) {
                                String name = source.getPath().substring(source.getPath().lastIndexOf(File.separator) + 1);
                                source.setURL(AlivcResUtil.getCloudResUri(AlivcResUtil.TYPE_TRANSITION, String.valueOf(mGroupId), name));
                            }
                            info.setSource(source);
                            transitionBase.setCustomSource(source);
                            if (position == 0){
                                info.transitionType = TransitionChooserView.EFFECT_NONE;
                            }else {
                                info.transitionType = TransitionChooserView.EFFECT_CUSTOM;
                            }
                            info.transitionBase = transitionBase;
                        }
                        if (mOnItemClickListener.onItemClick(info, position)) {
                            mSelectPosition = getAdapterPosition();
                            notifyDataSetChanged();
                        }
                    }
                }
            });
            mEffectName = itemView.findViewById(R.id.tv_effect_name);
        }
    }

    /**
     * 获取滤镜名称 适配系统语言/中文或其他
     * @param path 滤镜文件目录
     * @return name
     */
    private String getTransitionName(String path) {
        String name = "";
        I18nBean effectI18n = EditorCommon.getCurrentEffectI18n(path, "name");
        if (effectI18n == null) {
            if (LanguageUtils.isCHEN(mContext)) {
                path = path + "/config.json";
            } else {
                String pathEn = path + "/configEn.json";
                if (new File(pathEn).exists()) {
                    path = pathEn;
                } else {
                    path = path + "/config.json";
                }
            }
            StringBuilder var2 = new StringBuilder();
            File var3 = new File(path);

            try {
                FileReader var4 = new FileReader(var3);

                int var7;
                while ((var7 = var4.read()) != -1) {
                    var2.append((char)var7);
                }

                var4.close();
            } catch (IOException var6) {
                var6.printStackTrace();
            }

            try {
                JSONObject var4 = new JSONObject(var2.toString());
                name = var4.optString("name");
            } catch (JSONException var5) {
                var5.printStackTrace();
            }

            return name;
        }

        if (LanguageUtils.isCHEN(mContext)) {
            return effectI18n.getZh_cn();
        } else {
            return effectI18n.getEn();
        }


    }

}
