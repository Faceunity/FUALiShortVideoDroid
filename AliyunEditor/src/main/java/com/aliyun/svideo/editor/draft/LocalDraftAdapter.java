package com.aliyun.svideo.editor.draft;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

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

import com.aliyun.common.utils.StringUtils;
import com.aliyun.common.qupaiokhttp.HttpRequest;
import com.aliyun.common.qupaiokhttp.RequestParams;
import com.aliyun.common.qupaiokhttp.StringHttpRequestCallback;
import com.aliyun.svideo.common.utils.DateTimeUtils;
import com.aliyun.svideo.common.utils.image.ImageLoaderImpl;
import com.aliyun.svideo.common.utils.image.ImageLoaderOptions;
import com.aliyun.svideo.common.widget.AlivcCircleLoadingDialog;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.editor.EditorActivity;
import com.aliyun.svideo.editor.template.TemplateEditorActivity;
import com.aliyun.svideo.editor.util.AlivcResUtil;
import com.aliyun.svideo.editor.util.EditorCommon;
import com.aliyun.svideosdk.common.struct.project.Source;
import com.aliyun.svideosdk.editor.draft.AliyunDraft;
import com.aliyun.svideosdk.editor.draft.AliyunDraftManager;
import com.aliyun.svideosdk.editor.draft.AliyunDraftResourceLoader;
import com.aliyun.svideosdk.editor.draft.AliyunDraftResourceUploader;
import com.aliyun.svideosdk.editor.draft.AliyunDraftResTask;
import com.aliyun.svideosdk.editor.draft.AliyunTemplateDraftManager;
import com.aliyun.svideosdk.editor.resource.AliyunResModuleType;
import com.aliyun.svideosdk.editor.resource.AliyunResTask;
import com.google.gson.Gson;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 本地草稿列表适配器
 */
public class LocalDraftAdapter extends RecyclerView.Adapter<LocalDraftAdapter.DraftViewHolder> implements View.OnClickListener {
    private boolean isTemplateDraft;
    private List<AliyunDraft> mData = new ArrayList<>();

    public LocalDraftAdapter(boolean isTemplateDraft) {
        this.isTemplateDraft = isTemplateDraft;
    }

    @Override
    public DraftViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DraftViewHolder(LayoutInflater.from(parent.getContext())
                                                 .inflate(R.layout.alivc_editor_draft_item, parent, false));
    }

    public void setData(List<AliyunDraft> data) {
        this.mData.clear();
        this.mData.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final DraftViewHolder holder, final int position) {
        AliyunDraft item = mData.get(position);
        holder.tvTitle.setText(item.getName());
        holder.itemView.setTag(holder);
        holder.itemView.setOnClickListener(this);
        holder.ivMore.setTag(holder);
        holder.ivMore.setOnClickListener(this);
        holder.tvDuration.setText(DateTimeUtils.formatMs(item.getDuration()));
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        holder.tvUpdate.setText(holder.tvUpdate.getContext().getText(R.string.alivc_svideo_draft_update_tips) + " " + format.format(new Date(item.getUpdateTime())));
        holder.tvSize.setText(formatFileSize(item.getFileSize()));
        ImageLoaderOptions options = new ImageLoaderOptions.Builder()
                                       .skipDiskCacheCache()
                                       .skipMemoryCache()
                                       .build();
        new ImageLoaderImpl().loadImage(holder.ivCover.getContext(),item.getCoverPath(),options).into(holder.ivCover);
        holder.ivBackup.setVisibility(StringUtils.isEmpty(item.getProjectId()) ? View.GONE : View.VISIBLE);
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

    private AliyunDraftManager getAliyunDraftManager(Context context) {
        AliyunDraftManager aliyunDraftManager = null;
        if (isTemplateDraft) {
            aliyunDraftManager = AliyunTemplateDraftManager.getInstance(context);
        } else {
            aliyunDraftManager = AliyunDraftManager.getInstance(context);
        }
        return aliyunDraftManager;
    }

    @Override
    public void onClick(final View v) {
        
        DraftViewHolder holder = (DraftViewHolder) v.getTag();
        final int position = holder.getAdapterPosition();
        final AliyunDraft item = mData.get(position);
        int id = v.getId();
        if (id == R.id.iv_more) {
            ((DraftListActivity) v.getContext()).showMenu(isTemplateDraft, this, v.getTag());
        } else if (id == R.id.alivc_rename_btn) {
            ((DraftListActivity) v.getContext()).showRenameDialog(item.getName(),new DraftRenameDialogFragment.OnRenameListener() {
                @Override
                public void onRename(final String name) {
                    item.setName(name);
                    getAliyunDraftManager(v.getContext()).rename(item.getId(), name);
                    notifyDataSetChanged();
                }
            });
        } else if (id == R.id.alivc_update_cover_btn) {
            ((DraftListActivity) v.getContext()).updateCover(this, item);
        } else if (id == R.id.alivc_copy_btn) {
            AliyunDraft draft = getAliyunDraftManager(v.getContext()).copy(item.getId());
            this.mData.add(position,draft);
            notifyDataSetChanged();
        } else if (id == R.id.alivc_delete_btn) {
            getAliyunDraftManager(v.getContext()).deleteDraft(item.getId());
            this.mData.remove(item);
            notifyDataSetChanged();
            if (!StringUtils.isEmpty(item.getProjectId())) {
                notifyCloudDraftFragment(v.getContext());
            }
        } else if (id == R.id.alivc_backup_btn) {
            //模拟备份到云端
            uploadDraft(v.getContext(), item, position);
        } else {
            final Context context = v.getContext();
            final AlivcCircleLoadingDialog dialog = new AlivcCircleLoadingDialog(context, 0);
            dialog.show();
            getAliyunDraftManager(v.getContext()).preLoadDraft(item, new AliyunDraftResourceLoader() {

                @Override
                public void onHandleResourceTasks(final List<AliyunDraftResTask> tasks) {
                    HashMap<String, List<AliyunDraftResTask>> map = new HashMap<>();
                    for (AliyunDraftResTask task : tasks) {
                        if (task.getSource() != null && !StringUtils.isEmpty(task.getSource().getURL())) {
                            if (map.containsKey(task.getSource().getURL())) {
                                map.get(task.getSource().getURL()).add(task);
                            } else {
                                List<AliyunDraftResTask> list = new ArrayList<>();
                                list.add(task);
                                map.put(task.getSource().getURL(), list);
                            }
                        } else {
                            //必须对任务进行处理，可选项：修复、忽略、删除
                            if (task.getResModuleType() == AliyunResModuleType.MAIN_VIDEO) {
                                task.getSource().setPath(EditorCommon.SD_DIR + "svideo_res/image/aliyun_svideo_failed.jpg");
                                task.onHandleCallback(task.getSource());
                            } else if(task.getResModuleType() == AliyunResModuleType.TRANSITION) {
                                //删除
                                task.onRemove();
                            } else {
                                //忽略
                                task.onIgnore();
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
                                    AlivcResUtil.downloadRes(url, new File(item.getEditorProjectUri(), fileName).getAbsolutePath(), new AlivcResUtil.LoadCallback() {
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
                }

                @Override
                public void onFailure(final String msg) {
                    dialog.dismiss();
                    Toast.makeText(v.getContext(), R.string.alivc_svideo_draft_preload_failed, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess() {
                    dialog.dismiss();
                    if (isTemplateDraft) {
                        Intent intent = new Intent(v.getContext(), TemplateEditorActivity.class);
                        intent.putExtra(TemplateEditorActivity.KEY_PARAM_CONFIG, item.getEditorProjectUri());
                        v.getContext().startActivity(intent);
                    } else {
                        EditorActivity.startEdit(v.getContext(), item);
                    }
                }
            });
        }

    }

    public void uploadDraft(final Context context, final AliyunDraft item, final int position) {
        final AlivcCircleLoadingDialog dialog = new AlivcCircleLoadingDialog(context, 0);
        dialog.show();
        AliyunDraftManager.getInstance(context)
                          .uploadDraft(item, new AliyunDraftResourceUploader() {
                              @Override
                              public void onHandleResourceTasks(final List<AliyunDraftResTask> tasks) {
                                  HashMap<String, List<AliyunDraftResTask>> map = new HashMap<>();
                                  //过虑重复资源
                                  for (AliyunDraftResTask task : tasks) {
                                      if (task.getSource() == null) {
                                          task.onIgnore();
                                          continue;
                                      }
                                      //URL为空或者不以alivc_resource开头需要做上传处理
                                      String url = task.getSource().getURL();
                                      if (StringUtils.isEmpty(url) || !url.startsWith("alivc_resource")) {
                                          if (map.containsKey(task.getSource().getPath())) {
                                              map.get(task.getSource().getPath()).add(task);
                                          } else {
                                              List<AliyunDraftResTask> list = new ArrayList<>();
                                              list.add(task);
                                              map.put(task.getSource().getPath(), list);
                                          }
                                      } else {
                                          //忽略出错
                                          task.onIgnore();
                                      }
                                  }
                                  for (Map.Entry<String, List<AliyunDraftResTask>> entry : map.entrySet()) {
                                      try {
                                          uploadFile(context, entry.getKey(), entry.getValue());
                                      } catch (Exception e) {
                                          //忽略出错
                                          List<AliyunDraftResTask> list = entry.getValue();
                                          for (AliyunDraftResTask item:list){
                                              item.onIgnore();
                                          }
                                      }
                                  }
                              }

                              @Override
                              public void onSuccess(final String projectPath, String coverUrl) {
                                  @SuppressLint("SimpleDateFormat")
                                  SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                  String modifiedTime = format.format(new Date(item.getUpdateTime()));
                                  uploadDraftConfig(dialog, modifiedTime, context, item, projectPath, coverUrl, position);
                              }

                              @Override
                              public void onFailure(final String msg) {
                                  dialog.dismiss();
                                  Toast.makeText(context,R.string.alivc_svideo_draft_backup_failed,Toast.LENGTH_SHORT).show();
                              }
                          });
    }

    private void uploadFile(final Context context, final String path, final List<AliyunDraftResTask> tasks) {
        RequestParams requestParams = new RequestParams();
        requestParams.addFormDataPart("file", new File(path));
        HttpRequest.post(DraftListActivity.SERVER_UPLOAD_URL, requestParams, new StringHttpRequestCallback() {
            @Override
            public void onFailure(final int errorCode, final String msg) {
                for (AliyunDraftResTask task : tasks) {
                    task.onIgnore();
                }
                Toast.makeText(context, context.getText(R.string.alivc_svideo_draft_backup_exception) + "：" + msg, Toast.LENGTH_SHORT).show();
                Log.d("CloudDraft", "uploadFile>onFailure>" + msg+">"+path);
            }
            @Override
            protected void onSuccess(final String result) {
                Log.d("CloudDraft","uploadFile>onSuccess>"+result);
                try {
                    CloudUploadResResult cloudUploadResResult = new Gson().fromJson(result, CloudUploadResResult.class);
                    if (cloudUploadResResult.code == 0) {
                        for (AliyunDraftResTask task : tasks) {
                            Source source = task.getSource();
                            source.setURL(cloudUploadResResult.data);
                            task.onHandleCallback(source);
                        }
                    }
                } catch (Exception ignored) {
                    for (AliyunDraftResTask task : tasks) {
                        task.onIgnore();
                    }
                }

            }
        });
    }

    private void uploadDraftConfig(final AlivcCircleLoadingDialog dialog, String modifiedTime, final Context context, final AliyunDraft item, String projectPath, String coverUrl, final int position) {
        RequestParams requestParams = new RequestParams();
        requestParams.addFormDataPart("cover", coverUrl);
        requestParams.addFormDataPart("file", new File(projectPath));
        requestParams.addFormDataPart("name", item.getName());
        requestParams.addFormDataPart("file_size", item.getFileSize());
        requestParams.addFormDataPart("duration", item.getDuration());
        requestParams.addFormDataPart("modified_time", modifiedTime);
        HttpRequest.post(DraftListActivity.SERVER_ADD_PROJECT_URL, requestParams, new StringHttpRequestCallback() {

            @Override
            public void onFailure(final int errorCode, final String msg) {
                dialog.dismiss();
                Toast.makeText(context, context.getText(R.string.alivc_svideo_draft_backup_failed) + "：" + msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onSuccess(final String result) {
                dialog.dismiss();
                Log.d("CloudDraft", "uploadDraftConfig>onSuccess>" + result);
                try {
                    CloudUploadDraftResult cloudUploadDraftResult = new Gson().fromJson(result, CloudUploadDraftResult.class);
                    if (cloudUploadDraftResult.code == 0) {
                        AliyunDraftManager.getInstance(context).setProjectId(item.getId(), cloudUploadDraftResult.data.mProjectId);
                        item.setProjectId(cloudUploadDraftResult.data.mProjectId);
                        notifyItemChanged(position);
                        notifyCloudDraftFragment(context);
                        Toast.makeText(context, R.string.alivc_svideo_draft_backup_success, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ignored) {
                }
            }
        });
    }

    private void notifyCloudDraftFragment(Context context){
        if (context instanceof DraftListActivity) {
            FragmentManager fragmentManager = ((DraftListActivity) context).getSupportFragmentManager();
            List<Fragment> fragments = fragmentManager.getFragments();
            for (Fragment item : fragments) {
                if (item instanceof CloudDraftFragment) {
                    ((CloudDraftFragment) item).loadData();
                }
            }
        }
    }

    static class DraftViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivCover;
        public ImageView ivBackup;
        public TextView tvTitle;
        public TextView tvUpdate;
        public TextView tvSize;
        public TextView tvDuration;
        public ImageView ivMore;

        public DraftViewHolder(View itemView) {
            super(itemView);
            ivCover = (ImageView) itemView.findViewById(R.id.iv_cover);
            ivBackup = (ImageView) itemView.findViewById(R.id.iv_backup);
            tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
            tvUpdate = (TextView) itemView.findViewById(R.id.tv_update);
            tvSize = (TextView) itemView.findViewById(R.id.tv_size);
            tvDuration = (TextView) itemView.findViewById(R.id.tv_duration);
            ivMore = (ImageView) itemView.findViewById(R.id.iv_more);
        }

    }
}
