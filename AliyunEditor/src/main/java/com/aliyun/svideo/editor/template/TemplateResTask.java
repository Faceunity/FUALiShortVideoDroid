package com.aliyun.svideo.editor.template;


import com.aliyun.svideosdk.common.struct.project.Source;

public class TemplateResTask {
    public enum TemplateResType {
        Cover,
        Video,
        Project,
    }

    public interface HandleCallback {
        /**
         * 处理回调
         *
         * @param task 已处理任务
         */
        void onCallback(TemplateResTask task);
    }

    /**
     * 资源类型
     */
    private TemplateResType mResType;
    /**
     * 资源
     */
    private Source mSource;
    /**
     * 处理回调
     */
    private HandleCallback mHandleCallback;

    public TemplateResTask(TemplateResType resType, Source source, HandleCallback handleCallback) {
        this.mResType = resType;
        this.mSource = source;
        this.mHandleCallback = handleCallback;
    }

    public Source getSource() {
        return mSource;
    }

    /**
     * 处理回调
     */
    public void onHandleCallback(Source source) {
        if (mHandleCallback != null) {
            if (source != null) {
                mSource.setId(source.getId());
                mSource.setPath(source.getPath());
                mSource.setURL(source.getURL());
            }
            mHandleCallback.onCallback(this);
            mHandleCallback = null;
        }
    }
}
