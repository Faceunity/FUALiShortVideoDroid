package com.aliyun.svideo.editor.template;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.common.utils.image.ImageLoaderImpl;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideosdk.common.struct.template.AliyunTemplateImageParam;
import com.aliyun.svideosdk.common.struct.template.AliyunTemplateParam;
import com.aliyun.svideosdk.common.struct.template.AliyunTemplateTextParam;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 模板输入适配器
 */
public class TemplateInputAdapter extends RecyclerView.Adapter<TemplateInputAdapter.TemplateInputViewHolder> implements View.OnClickListener {
    private List<AliyunTemplateParam> mData = new ArrayList<>();

    @Override
    public TemplateInputViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TemplateInputViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alivc_editor_template_input_item, parent, false));
    }

    public void setData(List<AliyunTemplateParam> data) {
        this.mData.clear();
        this.mData.addAll(data);
        notifyDataSetChanged();
    }

    public List<AliyunTemplateParam> getData() {
        return mData;
    }

    @Override
    public void onBindViewHolder(final TemplateInputViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final AliyunTemplateParam item = mData.get(position);
        holder.cbItem.setOnCheckedChangeListener(null);
        holder.cbItem.setChecked(!item.isLock());
        holder.cbItem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                item.setLock(!b);
                notifyItemChanged(position);
            }
        });
        DecimalFormat df = new DecimalFormat("#.##");
        holder.tvDuration.setText(df.format(item.getTimelineOut() - item.getTimelineIn()) + "S");
        holder.tvIndex.setText(String.valueOf(position + 1));
        if (item instanceof AliyunTemplateImageParam) {
            holder.ivCover.setVisibility(View.VISIBLE);
            new ImageLoaderImpl().loadImage(holder.ivCover.getContext(), ((AliyunTemplateImageParam) item).getSource().getPath()).into(holder.ivCover);
        } else if (item instanceof AliyunTemplateTextParam) {
            holder.ivCover.setImageResource(R.color.alivc_edit_template_item_bg);
            holder.tvDuration.setText(((AliyunTemplateTextParam) item).getText());
        }
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public void onClick(final View v) {
        TemplateInputViewHolder holder = (TemplateInputViewHolder) v.getTag();
        final int position = holder.getAdapterPosition();
        final AliyunTemplateParam item = mData.get(position);
        int id = v.getId();

    }


    static class TemplateInputViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivCover;
        public TextView tvDuration;
        public CheckBox cbItem;
        public TextView tvIndex;

        public TemplateInputViewHolder(View itemView) {
            super(itemView);
            ivCover = (ImageView) itemView.findViewById(R.id.iv_cover);
            tvDuration = (TextView) itemView.findViewById(R.id.tv_duration);
            cbItem = (CheckBox) itemView.findViewById(R.id.cb_item);
            tvIndex = (TextView) itemView.findViewById(R.id.iv_index);
        }

    }
}
