package com.aliyun.svideo.editor.draft;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.common.utils.FileUtils;
import com.aliyun.common.utils.StringUtils;
import com.aliyun.common.qupaiokhttp.HttpRequest;
import com.aliyun.common.qupaiokhttp.RequestParams;
import com.aliyun.common.qupaiokhttp.StringHttpRequestCallback;
import com.aliyun.svideo.common.utils.DateTimeUtils;
import com.aliyun.svideo.common.utils.image.ImageLoaderImpl;
import com.aliyun.svideo.common.widget.AlivcCircleLoadingDialog;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.util.AlivcResUtil;
import com.aliyun.svideosdk.common.struct.project.AliyunEditorProject;
import com.aliyun.svideosdk.common.struct.project.Source;
import com.aliyun.svideosdk.editor.draft.AliyunDraft;
import com.aliyun.svideosdk.editor.draft.AliyunDraftManager;
import com.aliyun.svideosdk.editor.draft.AliyunDraftResTask;
import com.aliyun.svideosdk.editor.draft.AliyunDraftResourceDownloader;
import com.aliyun.svideosdk.editor.resource.AliyunResModuleType;
import com.aliyun.svideosdk.editor.resource.AliyunResTask;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 云端草稿列表适配器
 */
public class CloudDraftAdapter extends RecyclerView.Adapter<CloudDraftAdapter.DraftViewHolder> implements View.OnClickListener {
    private List<CloudDraftItem> mData = new ArrayList<>();

    @Override
    public DraftViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DraftViewHolder(LayoutInflater.from(parent.getContext())
                                                 .inflate(R.layout.alivc_editor_cloud_draft_item, parent, false));
    }

    public void setData(List<CloudDraftItem> data) {
        this.mData.clear();
        this.mData.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final DraftViewHolder holder, final int position) {
        CloudDraftItem item = mData.get(position);
        holder.tvTitle.setText(item.mName);
        holder.itemView.setTag(holder);
        holder.itemView.setOnClickListener(this);
        holder.ivMore.setTag(holder);
        holder.ivMore.setOnClickListener(this);
        holder.ivDownload.setOnClickListener(this);
        holder.ivDownload.setTag(holder);
        holder.tvDuration.setText(DateTimeUtils.formatMs(item.mDuration));
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        holder.tvUpdate.setText(holder.tvUpdate.getContext().getText(R.string.alivc_svideo_draft_backup_tips) + " " + format.format(new Date(item.mBackupTime * 1000)));
        holder.tvSize.setText(formatFileSize(item.mFileSize));
        new ImageLoaderImpl().loadImage(holder.ivCover.getContext(), item.mCoverPath).into(holder.ivCover);
        holder.ivDownload.setVisibility(item.isDownload ? View.GONE : View.VISIBLE);
    }

    private String formatFileSize(long size) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (size == 0) {
            return wrongSize;
        }
        if (size < 1024) {
            fileSizeString = df.format((double) size) + "B";
        } else if (size < 1048576) {
            fileSizeString = df.format((double) size / 1024) + "KB";
        } else if (size < 1073741824) {
            fileSizeString = df.format((double) size / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) size / 1073741824) + "GB";
        }
        return fileSizeString;
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public void onClick(final View v) {
        DraftViewHolder holder = (DraftViewHolder) v.getTag();
        final int position = holder.getAdapterPosition();
        final CloudDraftItem item = mData.get(position);
        int id = v.getId();
        if (id == R.id.iv_more) {
            ((DraftListActivity) v.getContext()).showCloudMenu(this, v.getTag());
        } if (id == R.id.alivc_delete_btn) {
            final AlivcCircleLoadingDialog dialog = new AlivcCircleLoadingDialog(v.getContext(), 0);
            dialog.show();
            RequestParams requestParams = new RequestParams();
            JSONObject json = new JSONObject();
            try {
                json.put("project_id", item.mProjectId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            requestParams.setCustomRequestBody(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString()));
            HttpRequest.post(DraftListActivity.SERVER_DELETE_URL, requestParams, new StringHttpRequestCallback() {

                @Override
                public void onFailure(final int errorCode, final String msg) {
                    dialog.dismiss();
                    Log.d("CloudDraft", "delete_project>onFailure>" + msg);
                }

                @Override
                protected void onSuccess(final String result) {
                    dialog.dismiss();
                    Log.d("CloudDraft", "delete_project>onSuccess>" + result);
                    try {
                        CloudDeleteDraftResult cloudDeleteDraftResult = new Gson().fromJson(result, CloudDeleteDraftResult.class);

                        if (cloudDeleteDraftResult.code == 0) {
                            CloudDraftAdapter.this.mData.remove(item);
                            notifyDataSetChanged();
                        }
                    } catch (Exception ignored) {
                    }
                }
            });

        } else if (id == R.id.iv_download) {
            final AlivcCircleLoadingDialog dialog = new AlivcCircleLoadingDialog(v.getContext(), 0);
            dialog.show();
            HttpRequest.get(item.mProjectUrl,new StringHttpRequestCallback(){
                @Override
                public void onFailure(final int errorCode, final String msg) {
                    dialog.dismiss();
                }

                @Override
                protected void onSuccess(final String result) {
                    try {
                        File file = new File(v.getContext()
                                              .getExternalFilesDir("svideo_res/cloud_draft"),
                                             item.mProjectId + File.separator +AliyunEditorProject.PROJECT_FILENAME);
                        if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }
                        if (file.exists()) {
                            file.delete();
                        }
                        FileUtils.writeStringToFile(file, result, "utf-8");
                        downloadDraft(dialog, v.getContext(), file, item, position);
                    } catch (Exception ignored) {
                        dialog.dismiss();
                    }
                }
            });
        }
    }

    private void downloadDraft(final AlivcCircleLoadingDialog dialog, final Context context, File file, final CloudDraftItem item, final int position) {
        AliyunDraftManager.getInstance(context).downloadDraft(file, new AliyunDraftResourceDownloader() {
            @Override
            public void onHandleResourceTasks(final String projectDir, final List<AliyunDraftResTask> tasks) {
                HashMap<String, List<AliyunDraftResTask>> map = new HashMap<>();
                //过虑重复资源
                for (AliyunDraftResTask task : tasks) {
                    if (task.getSource() == null || StringUtils.isEmpty(task.getSource().getURL())) {
                        task.onIgnore();
                    } else if (map.containsKey(task.getSource().getURL())) {
                        map.get(task.getSource().getURL()).add(task);
                    } else {
                        List<AliyunDraftResTask> list = new ArrayList<>();
                        list.add(task);
                        map.put(task.getSource().getURL(), list);
                    }
                }
                for (final Map.Entry<String, List<AliyunDraftResTask>> entry : map.entrySet()) {
                    final List<AliyunDraftResTask> list = entry.getValue();
                    try {
                        final String url = entry.getKey();
                        if (url.startsWith(AlivcResUtil.SCHEME)) {
                            AlivcResUtil.LoadCallback callback = new AlivcResUtil.LoadCallback() {
                                @Override
                                public void onSuccess(String path) {
                                    for (AliyunDraftResTask task : list) {
                                        Source source = task.getSource();
                                        source.setPath(path);
                                        //如果是MV则解压出ID赋值给Source供显示还原
                                        if (task.getResModuleType() == AliyunResModuleType.MV) {
                                            try {
                                                source.setId(Uri.parse(url).getQueryParameter("gid"));
                                            }catch (Exception ignored){
                                            }
                                        }
                                        task.onHandleCallback(source);
                                    }
                                }

                                @Override
                                public void onFailure(String type, String msg) {
                                    Log.d("CloudDraft", "loadRes>Failure>type>" + type + ">msg>" + msg);
                                    for (AliyunDraftResTask task : list) {
                                        task.onIgnore();
                                    }
                                }
                            };
                            AlivcResUtil.loadRes(context, url, callback);
                        } else {
                            String fileName = url.substring(url.lastIndexOf("/") + 1);
                            if (list != null && list.size() > 0 && list.get(0).getResModuleType() == AliyunResModuleType.COVER) {
                                fileName = "cover.jpeg";
                            }
                            AlivcResUtil.downloadRes(url, new File(projectDir, fileName).getAbsolutePath(), new AlivcResUtil.LoadCallback() {
                                @Override
                                public void onSuccess(String path) {
                                    for (AliyunResTask task : list) {
                                        Source source = task.getSource();
                                        source.setPath(path);
                                        task.onHandleCallback(source);
                                    }

                                }

                                @Override
                                public void onFailure(String type, String msg) {
                                    for (AliyunResTask task : list) {
                                        task.onIgnore();
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        //出错
                        for (AliyunDraftResTask item : list) {
                            item.onIgnore();
                        }
                    }
                }

            }

            @Override
            public void onSuccess(final AliyunDraft draft) {
                dialog.dismiss();
                item.isDownload = true;
                AliyunDraftManager.getInstance(context).setProjectId(draft.getId(), item.mProjectId);
                Toast.makeText(context, R.string.alivc_svideo_draft_restore_success, Toast.LENGTH_SHORT).show();
                notifyItemChanged(position);
                if(context instanceof DraftListActivity){
                    FragmentManager fragmentManager = ((DraftListActivity)context).getSupportFragmentManager();
                    List<Fragment> fragments = fragmentManager.getFragments();
                    for(Fragment item : fragments){
                        if(item instanceof LocalDraftFragment){
                            ((LocalDraftFragment)item).loadData();
                        }
                    }
                }
            }

            @Override
            public void onFailure(final String msg) {
                dialog.dismiss();
                Toast.makeText(context, R.string.alivc_svideo_draft_restore_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    static class DraftViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivCover;
        public ImageView ivDownload;
        public TextView tvTitle;
        public TextView tvUpdate;
        public TextView tvSize;
        public TextView tvDuration;
        public ImageView ivMore;

        public DraftViewHolder(View itemView) {
            super(itemView);
            ivCover = (ImageView) itemView.findViewById(R.id.iv_cover);
            ivDownload = (ImageView) itemView.findViewById(R.id.iv_download);
            tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
            tvUpdate = (TextView) itemView.findViewById(R.id.tv_update);
            tvSize = (TextView) itemView.findViewById(R.id.tv_size);
            tvDuration = (TextView) itemView.findViewById(R.id.tv_duration);
            ivMore = (ImageView) itemView.findViewById(R.id.iv_more);
        }

    }
}
