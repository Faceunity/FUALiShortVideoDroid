package com.aliyun.svideo.editor.template;

import java.util.List;

public interface TemplateSourceHandleCallback {
    /**
     * 待处理任务，子线程调用
     *
     * @param templateDir 模板目录
     * @param tasks 待处理任务
     */
    /****
     * resource processing task
     * @param templateDir template dir
     * @param tasks task
     */
    void onHandleResourceTasks(String templateDir, List<TemplateResTask> tasks);

    /**
     * 成功回调
     */
    /****
     * success callback.
     */
    void onSuccess();

    /**
     * 处理出错回调
     */
    /****
     * failure callback.
     */
    void onFailure(String msg);
}
