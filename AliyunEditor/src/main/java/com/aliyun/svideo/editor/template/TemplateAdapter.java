package com.aliyun.svideo.editor.template;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.common.utils.FileUtils;
import com.aliyun.svideo.common.utils.image.ImageLoaderImpl;
import com.aliyun.svideo.common.utils.image.ImageLoaderOptions;
import com.aliyun.svideo.common.widget.AlivcCircleLoadingDialog;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideosdk.common.struct.template.AliyunTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 本地模板列表适配器
 */
public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.TemplateViewHolder> implements View.OnClickListener {
    private List<AliyunTemplate> mData = new ArrayList<>();

    @Override
    public TemplateViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TemplateViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alivc_editor_template_item, parent, false));
    }

    public void setData(List<AliyunTemplate> data) {
        this.mData.clear();
        this.mData.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final TemplateViewHolder holder, final int position) {
        AliyunTemplate item = mData.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.ivMore.setTag(holder);
        holder.ivMore.setOnClickListener(this);
        holder.itemView.setTag(holder);
        holder.itemView.setOnClickListener(this);
        ImageLoaderOptions options = new ImageLoaderOptions.Builder()
                .skipDiskCacheCache()
                .skipMemoryCache()
                .build();
        new ImageLoaderImpl().loadImage(holder.ivCover.getContext(), item.getCover().getPath(), options).into(holder.ivCover);
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public void onClick(final View v) {
        TemplateViewHolder holder = (TemplateViewHolder) v.getTag();
        final int position = holder.getAdapterPosition();
        final AliyunTemplate item = mData.get(position);
        if (v.getId() == R.id.iv_more) {
            ((TemplateListActivity) v.getContext()).showMenu(this, holder);
        } else if (v.getId() == R.id.alivc_export_btn) {
            final Context context = v.getContext();
            final AlivcCircleLoadingDialog dialog = new AlivcCircleLoadingDialog(context, 0);
            dialog.show();
            TemplateManager.getInstance(context).exportTemplateZip(item, new TemplateManager.ExportCallback() {
                @Override
                public void onFailure(String msg) {
                    dialog.dismiss();
                    Toast.makeText(context, R.string.alivc_editor_template_list_export_failed, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(String zipPath) {
                    dialog.dismiss();
                    Toast.makeText(context, context.getText(R.string.alivc_editor_template_list_export_success) + "：" + zipPath, Toast.LENGTH_SHORT).show();
                }
            });
        } else if (v.getId() == R.id.alivc_delete_btn) {
            File file = new File(item.getPath());
            if (file.exists()) {
                FileUtils.deleteDirectory(file.getParentFile());
            }
            mData.remove(item);
            notifyDataSetChanged();
        } else {
            Intent intent = new Intent(v.getContext(), TemplateMediaActivity.class);
            intent.putExtra(TemplateMediaActivity.TEMPLATE_PATH, item.getPath());
            ((TemplateListActivity) v.getContext()).startActivityForResult(intent, TemplateListActivity.MEDIA_REQUEST_CODE);
        }
    }

    static class TemplateViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivCover;
        public TextView tvTitle;
        public ImageView ivMore;

        public TemplateViewHolder(View itemView) {
            super(itemView);
            ivCover = (ImageView) itemView.findViewById(R.id.iv_cover);
            tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
            ivMore = (ImageView) itemView.findViewById(R.id.iv_more);
        }
    }
}
