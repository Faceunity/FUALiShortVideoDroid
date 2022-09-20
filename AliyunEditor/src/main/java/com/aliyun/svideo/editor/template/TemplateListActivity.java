package com.aliyun.svideo.editor.template;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyun.common.utils.StringUtils;
import com.aliyun.common.utils.ToastUtil;
import com.aliyun.svideo.common.utils.PermissionUtils;
import com.aliyun.svideo.common.widget.AlivcCircleLoadingDialog;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.util.AlivcResUtil;
import com.aliyun.svideo.editor.util.EditorCommon;
import com.aliyun.svideo.editor.util.ThreadUtil;
import com.aliyun.svideo.editor.view.AlivcEditView;
import com.aliyun.svideosdk.common.struct.project.Source;
import com.aliyun.svideosdk.common.struct.template.AliyunTemplate;
import com.aliyun.svideosdk.editor.draft.AliyunDraft;
import com.aliyun.svideosdk.editor.draft.AliyunDraftManager;
import com.aliyun.svideosdk.editor.draft.AliyunDraftResTask;
import com.aliyun.svideosdk.editor.draft.AliyunDraftResourceLoader;
import com.aliyun.svideosdk.editor.draft.AliyunTemplateDraftManager;
import com.aliyun.svideosdk.editor.resource.AliyunResModuleType;
import com.aliyun.svideosdk.editor.resource.AliyunResTask;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * 模板列表
 */
public class TemplateListActivity extends Activity implements View.OnClickListener {
    public static final int REQUEST_CODE = 5566;
    public static final int MEDIA_REQUEST_CODE = 6677;
    private RecyclerView mRecyclerView;
    private TemplateAdapter mTemplateAdapter;
    private ExecutorService executorService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executorService = ThreadUtil.newDynamicSingleThreadedExecutor(new AlivcEditView.AlivcEditThread());
        setContentView(R.layout.aliyun_svideo_activity_template_list);
        TextView tvCenter = (TextView) findViewById(R.id.tv_center);
        tvCenter.setText(R.string.alivc_editor_template_list);
        tvCenter.setVisibility(View.VISIBLE);
        ImageView ivLeft = (ImageView) findViewById(R.id.iv_left);
        ivLeft.setOnClickListener(this);
        ivLeft.setVisibility(View.VISIBLE);
        ivLeft.setImageResource(R.mipmap.aliyun_svideo_icon_back);
        TextView tvRight = (TextView) findViewById(R.id.tv_right);
        tvRight.setOnClickListener(this);
        tvRight.setVisibility(View.VISIBLE);
        tvRight.setText(R.string.alivc_editor_template_list_import);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        mTemplateAdapter = new TemplateAdapter();
        mRecyclerView.setAdapter(mTemplateAdapter);
        if (PermissionUtils.checkPermissionsGroup(this, PermissionUtils.PERMISSION_STORAGE)) {
            EditorCommon.copySelf(TemplateListActivity.this, "template", new EditorCommon.CopyCallback() {
                @Override
                public void onFileCopy(String filePath) {
                    ToastUtil.showToast(TemplateListActivity.this, R.string.alivc_editor_toast_template_loading);
                    onImportTemplate(filePath);
                }
            });
            copyAssets();
        }
        initData();
    }

    private void copyAssets() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                EditorCommon.copyAll(TemplateListActivity.this, new FrameLayout(TemplateListActivity.this));
            }
        });
    }

    private void initData() {
        TemplateManager.getInstance(getApplicationContext()).getTemplateListByAsync(new TemplateManager.TemplateListCallback() {
            @Override
            public void onFailure(String msg) {
                if (TemplateListActivity.this.isDestroyed() || TemplateListActivity.this.isFinishing()) {
                    return;
                }
                Toast.makeText(TemplateListActivity.this, R.string.alivc_editor_template_list_loading_exception, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(List<AliyunTemplate> list) {
                if (TemplateListActivity.this.isDestroyed() || TemplateListActivity.this.isFinishing()) {
                    return;
                }
                mTemplateAdapter.setData(list);
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.iv_left) {
            finish();
        } else if (view.getId() == R.id.tv_right) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/zip");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                final String path = getRealPathFromURI(data.getData());
                onImportTemplate(path);
            } else if (requestCode == MEDIA_REQUEST_CODE) {
                String path = data.getStringExtra(TemplateMediaActivity.RESULT_PATH);
                Intent intent = new Intent(TemplateListActivity.this, TemplateEditorActivity.class);
                intent.putExtra(TemplateEditorActivity.KEY_PARAM_CONFIG, path);
                startActivity(intent);
            }
        }
    }

    private String getRealPathFromURI(final Uri uri) {
        if (null == uri) {
            return null;
        }
        String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
            if (data == null) {
                cursor = getContentResolver().query(uri, new String[]{MediaStore.MediaColumns.DOCUMENT_ID}, null, null, null);
                if (null != cursor) {
                    if (cursor.moveToFirst()) {
                        int index = cursor.getColumnIndex(MediaStore.MediaColumns.DOCUMENT_ID);
                        if (index > -1) {
                            data = cursor.getString(index);
                        }
                    }
                    cursor.close();
                }
                if (data != null)
                {
                    Log.i("TemplateListActivity", "data = " + data);
                    data = data.replace("raw:/","");
                    data = data.replace("primary:","storage/emulated/0/");
                    if (data.contains("storage/emulated")) {
                        data = data.substring(data.indexOf("storage/emulated"));
                    }
                }

            }
        }
        if (data == null && uri.toString().contains("storage/emulated")) {
            data = uri.toString().substring(uri.toString().indexOf("storage/emulated"));
            try {
                data = URLDecoder.decode(data, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        Log.i("TemplateListActivity", "return data = " + data);
        return data;
    }

    public void showMenu(final View.OnClickListener onClickListener, final Object tag) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View.OnClickListener clickListener = new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                v.setTag(tag);
                onClickListener.onClick(v);
                bottomSheetDialog.dismiss();
            }
        };
        View bottomSheet = View.inflate(this, R.layout.alivc_editor_template_bottom_sheet, null);
        bottomSheet.findViewById(R.id.alivc_export_btn).setOnClickListener(clickListener);
        bottomSheet.findViewById(R.id.alivc_delete_btn).setOnClickListener(clickListener);
        bottomSheetDialog.setContentView(bottomSheet);
        bottomSheetDialog.show();
    }

    private void onImportTemplate(final String path) {
        final AlivcCircleLoadingDialog dialog = new AlivcCircleLoadingDialog(this, 0);
        dialog.show();
        TemplateManager.getInstance(getApplicationContext()).importTemplateZip(new File(path), new TemplateManager.ImportCallback() {
            @Override
            public void onFailure(String msg) {
                dialog.dismiss();
                Toast.makeText(TemplateListActivity.this, R.string.alivc_editor_template_list_import_failed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(final AliyunTemplate template) {
                TemplateManager.getInstance(getApplicationContext()).loadTemplateSource(template, new TemplateSourceHandleCallback() {
                    @Override
                    public void onHandleResourceTasks(String templateDir, List<TemplateResTask> tasks) {
                        for (final TemplateResTask resTask : tasks) {
                            final String url = resTask.getSource().getURL();
                            if (url.startsWith(AlivcResUtil.SCHEME)) {
                                AlivcResUtil.loadRes(getApplicationContext(), url, templateDir, new AlivcResUtil.LoadCallback() {
                                    @Override
                                    public void onSuccess(String path) {
                                        resTask.getSource().setPath(path);
                                        resTask.onHandleCallback(resTask.getSource());
                                    }

                                    @Override
                                    public void onFailure(String type, String msg) {
                                        resTask.onHandleCallback(resTask.getSource());
                                    }
                                });
                            } else {
                                String fileName = url.substring(url.lastIndexOf("/") + 1);
                                AlivcResUtil.downloadRes(url, new File(templateDir, fileName).getAbsolutePath(), new AlivcResUtil.LoadCallback() {
                                    @Override
                                    public void onSuccess(String path) {
                                        resTask.getSource().setPath(path);
                                        resTask.onHandleCallback(resTask.getSource());
                                    }

                                    @Override
                                    public void onFailure(String type, String msg) {
                                        resTask.onHandleCallback(resTask.getSource());
                                    }
                                });
                            }

                        }
                    }

                    @Override
                    public void onSuccess() {
                        dialog.dismiss();
                        loadTemplateDraftResource(new File(template.getPath()).getParentFile().getPath(), template.getProject().getPath());
                    }

                    @Override
                    public void onFailure(String msg) {
                        dialog.dismiss();
                        Toast.makeText(TemplateListActivity.this, R.string.alivc_editor_template_list_import_failed, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }

    /**
     * 模板草稿资源预处理
     *
     * @param dir
     * @param projectConfigPath
     */
    private void loadTemplateDraftResource(final String dir, String projectConfigPath) {
        final AlivcCircleLoadingDialog dialog = new AlivcCircleLoadingDialog(this, 0);
        dialog.show();
        AliyunDraftManager aliyunDraftManager = AliyunTemplateDraftManager.getInstance(getApplicationContext());
        AliyunDraft aliyunDraft = aliyunDraftManager.getDraftByPath(projectConfigPath);
        aliyunDraftManager.preLoadDraft(aliyunDraft, new AliyunDraftResourceLoader() {

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
                        } else if (task.getResModuleType() == AliyunResModuleType.TRANSITION) {
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
                                        for (AliyunDraftResTask task : list) {
                                            task.onIgnore();
                                        }
                                    }
                                };
                                AlivcResUtil.loadRes(getApplicationContext(), url, dir, callback);
                            } else {
                                String fileName = url.substring(url.lastIndexOf("/") + 1);
                                if (list != null && list.size() > 0 && list.get(0).getResModuleType() == AliyunResModuleType.COVER) {
                                    fileName = "cover.jpeg";
                                }
                                AlivcResUtil.downloadRes(url, new File(dir, fileName).getAbsolutePath(), new AlivcResUtil.LoadCallback() {
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
                Toast.makeText(TemplateListActivity.this, R.string.alivc_editor_template_list_import_failed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess() {
                dialog.dismiss();
                initData();
                Toast.makeText(TemplateListActivity.this, R.string.alivc_editor_template_list_import_success, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
