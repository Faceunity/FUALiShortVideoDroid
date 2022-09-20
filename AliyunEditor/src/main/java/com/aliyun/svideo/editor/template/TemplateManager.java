package com.aliyun.svideo.editor.template;

import android.content.Context;
import android.os.Environment;

import com.aliyun.common.utils.FileUtils;
import com.aliyun.common.utils.ThreadUtils;
import com.aliyun.svideo.editor.util.ZipUtils;
import com.aliyun.svideosdk.common.struct.project.AliyunEditorProject;
import com.aliyun.svideosdk.common.struct.project.json.ProjectJSONSupportImpl;
import com.aliyun.svideosdk.common.struct.template.AliyunTemplate;
import com.aliyun.svideosdk.editor.draft.AliyunDraftManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TemplateManager {
    public static final String TEMPLATE_LIST_DIR = "svideo_res/template";
    private static volatile TemplateManager sManagerManager;
    private File mTemplateListDir;

    public interface TemplateListCallback {

        void onFailure(String msg);

        void onSuccess(List<AliyunTemplate> list);
    }

    public interface ExportCallback {

        void onFailure(String msg);

        void onSuccess(String zipPath);
    }

    public interface ImportCallback {

        void onFailure(String msg);

        void onSuccess(AliyunTemplate template);
    }

    public static TemplateManager getInstance(Context context) {
        if (sManagerManager == null) {
            synchronized (AliyunDraftManager.class) {
                if (sManagerManager == null) {
                    File appFilesDir = context.getExternalFilesDir(null);
                    File templateDir = new File(appFilesDir.getAbsolutePath() + File.separator + TEMPLATE_LIST_DIR);
                    if (!templateDir.exists()) {
                        templateDir.mkdirs();
                    }
                    sManagerManager = new TemplateManager(templateDir);
                }
            }
        }
        return sManagerManager;
    }


    private TemplateManager(File templateListDir) {
        mTemplateListDir = templateListDir;
    }

    public File getTemplateListDir() {
        return mTemplateListDir;
    }

    public void getTemplateListByAsync(final TemplateListCallback callback) {
        if (callback == null) {
            return;
        }
        ThreadUtils.runOnSubThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<AliyunTemplate> dataList = new ArrayList<>();
                    File[] files = getTemplateListDir().listFiles();
                    if (files != null) {
                        for (File templateDir : files) {
                            if (templateDir.isDirectory()) {
                                File file = new File(templateDir, AliyunTemplate.FILENAME);
                                if (file.exists() && file.isFile()) {
                                    try {
                                        AliyunTemplate template = new ProjectJSONSupportImpl().readValue(file, AliyunTemplate.class);
                                        template.setPath(file.getPath());
                                        dataList.add(template);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(dataList);
                        }
                    });
                } catch (final Exception e) {
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure(e.getMessage());
                        }
                    });
                }

            }
        });
    }

    /**
     * 导出成压缩包
     *
     * @param template
     * @param callback
     */
    public void exportTemplateZip(final AliyunTemplate template, final ExportCallback callback) {
        ThreadUtils.runOnSubThread(new Runnable() {
            @Override
            public void run() {
                File file = new File(template.getPath());
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                final File zipFile = new File(downloadsDir, "svideo_template_" + System.currentTimeMillis() + ".zip");
                if (file.exists()) {
                    try {
                        ZipUtils.zip(file.getParentFile().getPath(), zipFile.getPath());
                        if (zipFile.exists()) {
                            ThreadUtils.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onSuccess(zipFile.getAbsolutePath());
                                }
                            });
                            return;
                        }
                    } catch (final Exception e) {
                        e.printStackTrace();
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFailure(e.getMessage());
                            }
                        });
                        return;
                    }

                }
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailure("The template file not exists");
                    }
                });
            }
        });

    }

    /**
     * 根据zip压缩包导入模板
     *
     * @param templateZip
     * @param callback
     */
    public void importTemplateZip(final File templateZip, final ImportCallback callback) {
        ThreadUtils.runOnSubThread(new Runnable() {
            @Override
            public void run() {
                if (templateZip == null || !templateZip.exists()) {
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure("The template zip file not exists");
                        }
                    });
                    return;
                }
                File templateDir = new File(getTemplateListDir(), String.valueOf(System.currentTimeMillis()));
                try {
                    ZipUtils.unZip(templateZip.getPath(), templateDir.getAbsolutePath());
                    File file = new File(templateDir, AliyunTemplate.FILENAME);
                    if (file.exists() && file.isFile()) {
                        ProjectJSONSupportImpl jsonSupport = new ProjectJSONSupportImpl();
                        final AliyunTemplate template = jsonSupport.readValue(file, AliyunTemplate.class);
                        template.setPath(file.getAbsolutePath());
                        File projectFile = new File(templateDir, AliyunEditorProject.PROJECT_FILENAME);
                        template.getProject().setPath(projectFile.getAbsolutePath());
                        jsonSupport.writeValue(file, template);
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess(template);
                            }
                        });
                        return;
                    } else {
                        FileUtils.deleteDirectory(templateDir);
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFailure("The template json not exists");
                            }
                        });
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    FileUtils.deleteDirectory(templateDir);
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure(e.getMessage());
                        }
                    });
                    return;
                }

            }
        });
    }

    public void loadTemplateSource(final AliyunTemplate template, final TemplateSourceHandleCallback callback) {
        ThreadUtils.runOnSubThread(new Runnable() {
            @Override
            public void run() {
                if (template == null) {
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure("The template not exists");
                        }
                    });
                    return;
                }
                final List<TemplateResTask> list = new ArrayList<>();
                final TemplateResTask.HandleCallback handleCallback = new TemplateResTask.HandleCallback() {

                    @Override
                    public void onCallback(final TemplateResTask task) {
                        if (task != null) {
                            list.remove(task);
                        }
                        if (list.isEmpty()) {
                            try {
                                new ProjectJSONSupportImpl().writeValue(new File(template.getPath()), template);
                            } catch (final Exception e) {
                                e.printStackTrace();
                                ThreadUtils.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onFailure(e.getMessage());
                                    }
                                });
                                return;
                            }
                            ThreadUtils.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onSuccess();
                                }
                            });
                        }
                    }
                };
                list.add(new TemplateResTask(TemplateResTask.TemplateResType.Cover, template.getCover(), handleCallback));
                list.add(new TemplateResTask(TemplateResTask.TemplateResType.Video, template.getVideo(), handleCallback));
                list.add(new TemplateResTask(TemplateResTask.TemplateResType.Project, template.getProject(), handleCallback));

                try {
                    callback.onHandleResourceTasks(new File(template.getPath()).getParentFile().getAbsolutePath(), new ArrayList<TemplateResTask>(list));
                } catch (final Exception e) {
                    e.printStackTrace();
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure(e.getMessage());
                        }
                    });
                }
            }
        });
    }
}
