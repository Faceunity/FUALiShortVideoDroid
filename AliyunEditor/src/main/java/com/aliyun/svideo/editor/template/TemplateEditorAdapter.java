package com.aliyun.svideo.editor.template;

import android.annotation.SuppressLint;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.svideo.common.utils.image.ImageLoaderImpl;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideosdk.common.struct.template.AliyunTemplateImageParam;
import com.aliyun.svideosdk.common.struct.template.AliyunTemplateParam;
import com.aliyun.svideosdk.common.struct.template.AliyunTemplateTextParam;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 模板编辑配器
 */
public class TemplateEditorAdapter extends RecyclerView.Adapter<TemplateEditorAdapter.TemplateEditorViewHolder> implements View.OnClickListener {
    private List<AliyunTemplateParam> mData = new ArrayList<>();
    private String mSelectNode = "";
    private OnItemClickCallback mOnItemClickCallback;

    @Override
    public TemplateEditorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TemplateEditorViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alivc_editor_template_editor_item, parent, false));
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
    public void onBindViewHolder(final TemplateEditorViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final AliyunTemplateParam item = mData.get(position);
        if (mSelectNode.equals(item.getNodeKey())) {
            holder.tvDuration.setVisibility(View.GONE);
            holder.tvEdit.setVisibility(View.VISIBLE);
        } else {
            DecimalFormat df = new DecimalFormat("#.##");
            holder.tvDuration.setText(df.format(item.getTimelineOut() - item.getTimelineIn()) + "S");
            holder.tvDuration.setVisibility(View.VISIBLE);
            holder.tvEdit.setVisibility(View.GONE);
        }
        if (item.isLock()) {
            holder.ivLock.setVisibility(View.VISIBLE);
        } else {
            holder.ivLock.setVisibility(View.GONE);
        }
        holder.tvIndex.setText(String.valueOf(position + 1));
        if (item instanceof AliyunTemplateImageParam) {
            new ImageLoaderImpl().loadImage(holder.ivCover.getContext(), ((AliyunTemplateImageParam) item).getSource().getPath()).into(holder.ivCover);
        } else if (item instanceof AliyunTemplateTextParam) {
            holder.ivCover.setImageResource(R.color.alivc_edit_template_item_bg);
            holder.tvDuration.setText(((AliyunTemplateTextParam) item).getText());
        }
        holder.layoutItem.setTag(holder);
        holder.layoutItem.setOnClickListener(this);
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public void onClick(final View v) {
        if (v.getId() == R.id.layout_item) {
            TemplateEditorViewHolder holder = (TemplateEditorViewHolder) v.getTag();
            final int position = holder.getAdapterPosition();
            AliyunTemplateParam item = mData.get(position);
            if (mSelectNode.equals(item.getNodeKey())) {
                if (mOnItemClickCallback != null && !item.isLock()) {
                    mOnItemClickCallback.onEdit(item);
                }
            } else {
                mSelectNode = item.getNodeKey();
                if (mOnItemClickCallback != null) {
                    mOnItemClickCallback.onSelect(item);
                }
                notifyDataSetChanged();
            }

        }

    }

    public void setOnItemClickCallback(OnItemClickCallback onItemClickCallback) {
        this.mOnItemClickCallback = onItemClickCallback;
    }

    static class TemplateEditorViewHolder extends RecyclerView.ViewHolder {
        public View layoutItem;
        public ImageView ivCover;
        public TextView tvDuration;
        public TextView tvEdit;
        public ImageView ivLock;
        public TextView tvIndex;

        public TemplateEditorViewHolder(View itemView) {
            super(itemView);
            layoutItem = itemView.findViewById(R.id.layout_item);
            ivCover = (ImageView) itemView.findViewById(R.id.iv_cover);
            tvDuration = (TextView) itemView.findViewById(R.id.tv_duration);
            tvEdit = (TextView) itemView.findViewById(R.id.tv_edit);
            ivLock = (ImageView) itemView.findViewById(R.id.iv_lock);
            tvIndex = (TextView) itemView.findViewById(R.id.iv_index);
        }

    }

    public interface OnItemClickCallback {

        void onEdit(AliyunTemplateParam param);

        void onSelect(AliyunTemplateParam param);
    }
}
