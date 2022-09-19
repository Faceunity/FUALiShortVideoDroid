package com.aliyun.svideo.editor.draft;

import com.google.gson.annotations.SerializedName;

public class CloudDraftItem {
    /**
     * 草稿id
     */
    /****
     * the draft id.
     */
    public String mDraftId;
    /**
     * 工程ID(草稿备份到云端后才有)
     */
    /****
     * the project id.
     */
    @SerializedName("id")
    public String mProjectId;
    /**
     * 工程配置地址
     */
    /****
     * the project url.
     */
    @SerializedName("project_url")
    public String mProjectUrl;
    /**
     * 草稿名称
     */
    /****
     * the draft name.
     */
    @SerializedName("name")
    public String mName;
    /**
     * 草稿封面地址
     */
    /****
     * the draft cover path.
     */
    @SerializedName("cover_url")
    public String mCoverPath;
    /**
     * 草稿资源文件大小
     */
    /****
     * the draft all file size.
     */
    @SerializedName("file_size")
    public long mFileSize;
    /**
     * 草稿最后更新时间
     */
    /****
     * the draft last update time.
     */
    @SerializedName("modified_time")
    public String mModifiedTime;
    /**
     * 草稿备份时间
     */
    /****
     * the draft backup time.
     */
    @SerializedName("backup_time")
    public long mBackupTime;
    /**
     * 草稿时长
     */
    /****
     * the draft duration.
     */
    @SerializedName("duration")
    public long mDuration;
    /**
     * 是否已下载
     */
    /****
     * whether downloaded.
     */
    public boolean isDownload;
}
