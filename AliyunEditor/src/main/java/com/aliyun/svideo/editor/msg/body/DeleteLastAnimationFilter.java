package com.aliyun.svideo.editor.msg.body;

public class DeleteLastAnimationFilter {
    private String resourcePath;

    public DeleteLastAnimationFilter() {
    }

    public DeleteLastAnimationFilter(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getResourcePath() {
        return resourcePath;
    }
}
