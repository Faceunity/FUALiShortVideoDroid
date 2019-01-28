/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.crop.media;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;


import com.aliyun.common.global.AliyunTag;
import com.aliyun.common.utils.ToastUtil;
import com.aliyun.demo.crop.R;
import com.aliyun.jasonparse.JSONSupport;
import com.aliyun.common.utils.FileUtils;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2016/5/18.
 */
public class MediaStorage {

    public static final int TYPE_VIDEO = 0;
    public static final int TYPE_PHOTO = 1;

    public static final int NOTIFY_SIZE_OFFSET = 20;

    public static final int FIRST_NOTIFY_SIZE = 5;

    private static final String CACHE_NAME = "media_dir";

    public static final int SORT_MODE_VIDEO = 0;
    public static final int SORT_MODE_PHOTO = 1;
    public static final int SORT_MODE_MERGE = 2;

    private int sortMode;

    private HashMap<MediaDir, List<MediaInfo>> mediaByDir = new HashMap<>();
    private List<MediaInfo> medias = new ArrayList<>();
    private List<MediaDir> dirs = new ArrayList<>();

    private MediaInfo currentMedia;
    private MediaDir currentDir;
    private boolean isActive = true;
    private boolean isCompleted;
    SortMergedTask task;
    private String cacheDirName;
    private String cacheSavePath;

    private JSONSupport jsonSupport;
    private OnMediaDirChange onMediaDirChangeListener;
    private OnCompletion onCompletionListener;
    private OnMediaDataUpdate onMediaDataUpdateListener;
    private OnMediaDirUpdate onMediaDirUpdateListener;
    private OnCurrentMediaInfoChange onCurrentMediaInfoChangeListener;

    private int minVideoDuration = 2000;
    private int maxVideoDuration = 10 * 60 * 1000;

    public MediaStorage(Context context, JSONSupport jsonSupport) {
        this.jsonSupport = jsonSupport;
        task = new SortMergedTask(context);
        File mediaFile = FileUtils.getApplicationSdcardPath(context);
        if(mediaFile != null){
            cacheSavePath = mediaFile.getPath();
        }else{
            cacheSavePath = null;
            ToastUtil.showToast(context, R.string.aliyun_sdcard_not_ready);
        }
    }

    public void setSortMode(int sortMode) {
        this.sortMode = sortMode;
    }

    public void setVideoDurationRange(int min,int max){
        this.minVideoDuration = min;
        this.maxVideoDuration = max;
    }

    public void startFetchmedias() {
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void saveCurrentDirToCache() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if(cacheSavePath == null){
                    return null;
                }
                MediaDir dir = getCurrentDir();
                File cacheDir = new File(cacheSavePath);
                File cacheFile = new File(cacheDir, CACHE_NAME + ".dir");
                FileUtils.deleteFD(cacheFile);
                if (dir == null || dir.id == -1) {
                    return null;
                }
                List<MediaInfo> list = mediaByDir.get(dir);
                MediaCache cache = new MediaCache();
                cache.dir = dir;
                cache.list = list;
                try {
                    jsonSupport.writeValue(new FileOutputStream(cacheFile), cache);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void readMediaFromCache() {
        if(cacheSavePath == null){
            return;
        }
        File cacheDir = new File(cacheSavePath);
        MediaCache cache = null;
        File cacheFile = new File(cacheDir, CACHE_NAME + ".dir");
        if (!cacheFile.exists()) {
            return;
        }
        try {
            cache = jsonSupport.readValue(new FileInputStream(cacheFile), MediaCache.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cache != null) {
            MediaDir dir = cache.dir;
            if(dir == null){
                return ;
            }
            cacheDirName = dir.dirName;
            mediaByDir.put(dir, cache.list);
            setCurrentDir(dir);
        }

    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void cancelTask() {
        if (task != null) {
            task.cancel(false);
        }
    }

    public boolean isMediaEmpty() {
        return isCompleted && medias.isEmpty();
    }

    public List<MediaInfo> findMediaByDir(MediaDir dir) {
        if (dir == null) {
            return null;
        }
        return mediaByDir.get(dir);
    }

    public List<MediaInfo> findMediaByDir(int id) {
        MediaDir d = findDirById(id);
        return findMediaByDir(d);
    }

    public MediaDir findDirById(int id) {
        for (MediaDir d : dirs) {
            if (d.id == id) {
                return d;
            }
        }
        return null;
    }

    public MediaDir findDirByName(String name) {
        for (MediaDir d : dirs) {
            if (TextUtils.equals(name, d.dirName)) {
                return d;
            }
        }
        return null;
    }

    public List<MediaInfo> getMedias() {
        return medias;
    }

    public List<MediaDir> getDirs() {
        return dirs;
    }

    public void setOnCompletionListener(OnCompletion onCompletionListener) {
        this.onCompletionListener = onCompletionListener;
    }

    public void setOnMediaDataUpdateListener(OnMediaDataUpdate onMediaDataUpdateListener) {
        this.onMediaDataUpdateListener = onMediaDataUpdateListener;
    }

    public void setOnMediaDirUpdateListener(OnMediaDirUpdate onMediaDirUpdateListener) {
        this.onMediaDirUpdateListener = onMediaDirUpdateListener;
    }

    public void setOnCurrentMediaInfoChangeListener(OnCurrentMediaInfoChange onCurrentMediaInfoChangeListener) {
        this.onCurrentMediaInfoChangeListener = onCurrentMediaInfoChangeListener;
    }

    public void setOnMediaDirChangeListener(OnMediaDirChange onMediaDirChangeListener) {
        this.onMediaDirChangeListener = onMediaDirChangeListener;
    }

    public interface OnCompletion {

        void onCompletion();

    }

    public interface OnMediaDataUpdate {

        void onDataUpdate(List<MediaInfo> data);

    }

    public interface OnMediaDirUpdate {

        void onDirUpdate(MediaDir dir);

    }

    public interface OnMediaDirChange {

        void onMediaDirChanged();

    }

    public interface OnCurrentMediaInfoChange {

        void onCurrentMediaInfoChanged(MediaInfo info);

//        void onCurrentDraftInfoChanged(ProjectInfo info);
//
//        void onDraftItemClicked();
//
//        void onBackToMediaClicked();

    }

//    public void setOnBackToMediaClick() {
//        if (onCurrentMediaInfoChangeListener != null) {
//            onCurrentMediaInfoChangeListener.onBackToMediaClicked();
//        }
//    }
//
//    public void onDraftItemClicked() {
//        if (onCurrentMediaInfoChangeListener != null) {
//            onCurrentMediaInfoChangeListener.onDraftItemClicked();
//        }
//    }

    public MediaDir getCurrentDir() {
        return currentDir;
    }

    public void setCurrentDir(MediaDir dir) {
        if (dir == null && currentDir == null) {
            return;
        }
        this.currentDir = dir;
        if (onMediaDirChangeListener != null) {
            onMediaDirChangeListener.onMediaDirChanged();
        }
    }

    public MediaInfo getCurrentMedia() {
        return currentMedia;
    }

    public void setCurrentDisplayMediaData(MediaInfo info) {
        if (currentMedia == null) {
            currentMedia = info;
        }
        if (onCurrentMediaInfoChangeListener != null) {
            onCurrentMediaInfoChangeListener.onCurrentMediaInfoChanged(info);
        }

    }

//    public void setCurrentDisplayDraftData(ProjectInfo info) {
//        if (currentDraft == null) {
//            currentDraft = info;
//            if (onCurrentMediaInfoChangeListener != null) {
//                onCurrentMediaInfoChangeListener.onCurrentDraftInfoChanged(info);
//            }
//        } else {
//            if (!currentDraft.equals(info)) {
//                currentDraft = info;
//                if (onCurrentMediaInfoChangeListener != null) {
//                    onCurrentMediaInfoChangeListener.onCurrentDraftInfoChanged(info);
//                }
//            }
//        }
//    }

    private class SortMergedTask extends AsyncTask<Void, ArrayList<MediaInfo>, Void> {
        private final ContentResolver _Resolver;

        public SortMergedTask(Context context) {
            _Resolver = context.getContentResolver();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (cacheMediaList != null) {
                mediaByDir.remove(cacheDir);
                mediaByDir.put(cacheDir, cacheMediaList);
            }
            isCompleted = true;
            if (onCompletionListener != null) {
                onCompletionListener.onCompletion();
            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                readMediaFromCache();
            }
        }

        @Override
        protected void onProgressUpdate(ArrayList<MediaInfo>... bean) {
            if (bean[0] != null) {
                medias.addAll(bean[0]);
                if (onMediaDataUpdateListener != null) {
                    onMediaDataUpdateListener.onDataUpdate(bean[0]);
                }

            }
            super.onProgressUpdate(bean);
        }

        @Override
        protected Void doInBackground(Void... params) {
//            if (Environment.getExternalStorageState().equals(
//                    Environment.MEDIA_MOUNTED)) {
                Cursor videoCursor = null;
                if (sortMode == SORT_MODE_MERGE || sortMode == SORT_MODE_VIDEO) {
                    try{
                        videoCursor = _Resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new String[]{
                                MediaStore.Video.Media.DATA,
                                MediaStore.Video.Media._ID,
                                MediaStore.Video.Media.TITLE,
                                MediaStore.Video.Media.MIME_TYPE,
                                MediaStore.Video.Media.DURATION,
                                MediaStore.Video.Media.DATE_ADDED,
                        }, String.format("%1$s IN (?, ?, ? ,?) AND %2$s > %3$d AND %2$s < %4$d",
                                MediaStore.Video.Media.MIME_TYPE,
                                MediaStore.Video.Media.DURATION,minVideoDuration,maxVideoDuration), new String[]{
                                "video/mp4",
                                "video/ext-mp4", /* MEIZU 5.0 */
                                "video/3gpp",
                                "video/mov",
                        }, MediaStore.Video.Media.DATE_ADDED + " DESC");
                    }catch (Exception e){
                        return null;
                    }
                }
                Cursor imageCursor = null;
                if (sortMode == SORT_MODE_MERGE || sortMode == SORT_MODE_PHOTO) {
                    try{
                        imageCursor = _Resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{
                                MediaStore.Images.Media.DATA,
                                MediaStore.Images.Media._ID,
                                MediaStore.Images.Media.TITLE,
                                MediaStore.Images.Media.MIME_TYPE,
                                MediaStore.Images.Media.DATE_ADDED,
                        }, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");
                    }catch (Exception e){
                        return null;
                    }
                }
                int totalCount = (videoCursor == null ? 0 : videoCursor.getCount()) + (imageCursor == null ? 0 : imageCursor.getCount());
                int colDurationVideo = 0;
                int colMineTypeVideo = 0;
                int colDataVideo = 0;
                int colTitleVideo = 0;
                int colIdVideo = 0;
                int colDateAddedVideo = 0;
                int colMineTypeImage = 0;
                int colDataImage = 0;
                int colTitleImage = 0;
                int colIdImage = 0;
                int colDateAddedImage = 0;
                if (videoCursor != null) {
                    colDurationVideo = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
                    colMineTypeVideo = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE);
                    colDataVideo = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                    colTitleVideo = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
                    colIdVideo = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                    colDateAddedVideo = videoCursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED);
                }
                if (imageCursor != null) {
                    colMineTypeImage = imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE);
                    colDataImage = imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    colTitleImage = imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
                    colIdImage = imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                    colDateAddedImage = imageCursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED);
                }
                boolean videoMoveToNext = true;
                boolean imageMoveToNext = true;
                MediaInfo videoInfo = null;
                MediaInfo imageInfo = null;
                ArrayList<MediaInfo> cachedList = new ArrayList<>();
                int notifySize = FIRST_NOTIFY_SIZE;
                for (int i = 0; i < totalCount; i++) {
                    if (isCancelled()) {
                        return null;
                    }
                    if (videoCursor != null) {
                        while (videoInfo == null && videoMoveToNext && videoCursor.moveToNext()) {
                            videoInfo = generateVideoInfo(videoCursor, colDataVideo, colDurationVideo, colMineTypeVideo, colTitleVideo, colIdVideo, colDateAddedVideo, _Resolver);
                        }
                    }
                    if (imageCursor != null) {
                        while (imageInfo == null && imageMoveToNext && imageCursor.moveToNext()) {
                            imageInfo = generateImageInfo(imageCursor, colMineTypeImage, colDataImage, colTitleImage, colIdImage, colDateAddedImage, _Resolver);
                        }
                    }
                    if (videoInfo == null && imageInfo != null) {
                        addMediaInfo(imageInfo);
                        cachedList.add(imageInfo);
                        imageMoveToNext = true;
                        imageInfo = null;
                    } else if (imageInfo == null && videoInfo != null) {
                        addMediaInfo(videoInfo);
                        cachedList.add(videoInfo);
                        videoMoveToNext = true;
                        videoInfo = null;
                    } else if (videoInfo != null) {
                        if (videoInfo.addTime > imageInfo.addTime) {
                            addMediaInfo(videoInfo);
                            cachedList.add(videoInfo);
                            videoMoveToNext = true;
                            imageMoveToNext = false;
                            videoInfo = null;
                        } else {
                            addMediaInfo(imageInfo);
                            cachedList.add(imageInfo);
                            videoMoveToNext = false;
                            imageMoveToNext = true;
                            imageInfo = null;
                        }
                    }
                    if (cachedList.size() == notifySize) {
                        publishProgress(cachedList);
                        cachedList = new ArrayList<>();
                        notifySize += NOTIFY_SIZE_OFFSET;
                    }
//                    Log.d("sort_merge", "current index..." + i);
                }
                publishProgress(cachedList);
                if (videoCursor != null) {
                    videoCursor.close();
                }
                if (imageCursor != null) {
                    imageCursor.close();
                }

//            }
            return null;
        }
    }

    private MediaDir findMediaDirByName(String dir) {
        if (dirs != null) {
            for (int i = 0; i < dirs.size(); i++) {
                MediaDir md = dirs.get(i);
                if (dir.equals(md.dirName)) {
                    return md;
                }
            }
        }

        return null;
    }

    private MediaInfo generateVideoInfo(Cursor cursor, int colData, int colDuration, int colMineType, int colTitle, int colId, int colDateAdded, ContentResolver resolver) {

        String filePath = cursor.getString(colData);
        if (!new File(filePath).exists()) {
            return null;
        }
        MediaInfo videoInfo = new MediaInfo();
        videoInfo.type = TYPE_VIDEO;

        int duration = cursor.getInt(colDuration);
        String mimeType = cursor.getString(colMineType);
        String title = cursor.getString(colTitle);
        videoInfo.filePath = filePath;
        videoInfo.mimeType = mimeType;
        videoInfo.duration = duration;
        videoInfo.title = title;

        videoInfo.id = cursor.getInt(colId);

        videoInfo.addTime = cursor.getLong(colDateAdded);
        Cursor thumbCursor = resolver.query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Video.Thumbnails.DATA,
                        MediaStore.Video.Thumbnails.VIDEO_ID
                },
                MediaStore.Video.Thumbnails.VIDEO_ID + "=?",
                new String[]{String.valueOf(videoInfo.id)}, null);

        if (thumbCursor.moveToFirst()) {
            videoInfo.thumbnailPath = thumbCursor.getString(
                    thumbCursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA));
        }
        thumbCursor.close();

        return videoInfo;
    }

    private MediaInfo generateImageInfo(Cursor cursor, int colMineType, int colData, int colTitle, int colId, int colDateAdded, ContentResolver resolver) {

        String mimeType = cursor.getString(colMineType);
        String filePath = cursor.getString(colData);
        if (!new File(filePath).exists()) {
            return null;
        }
        MediaInfo mediaInfo = new MediaInfo();
        mediaInfo.type = TYPE_PHOTO;
        String title = cursor.getString(colTitle);
        mediaInfo.filePath = filePath;
        mediaInfo.mimeType = mimeType;
        mediaInfo.title = title;

        mediaInfo.id = cursor.getInt(colId);

        mediaInfo.addTime = cursor.getLong(colDateAdded);
        Cursor thumbCursor = resolver.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Images.Thumbnails.DATA,
                        MediaStore.Images.Thumbnails.IMAGE_ID
                },
                MediaStore.Images.Thumbnails.IMAGE_ID + "=?",
                new String[]{String.valueOf(mediaInfo.id)}, null);
        if (thumbCursor.getCount() == 0) {
            thumbCursor.close();
            thumbCursor = createThumbnailAndRequery(mediaInfo, resolver);
        }
        if (thumbCursor.moveToFirst()) {
            String thumbPath = thumbCursor.getString(
                    thumbCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA));
            mediaInfo.thumbnailPath = thumbPath;
            checkIfNeedToRotateThumbnail(mediaInfo.filePath, thumbPath);
        }
        thumbCursor.close();

        return mediaInfo;
    }

    private void checkIfNeedToRotateThumbnail(String filePath, String thumbnailPath) {
        try {
            Log.e(AliyunTag.TAG, "checkIfNeedToRotateThumbnail :" +filePath+", thumbnailPath:"+thumbnailPath);
            ExifInterface fileEi = new ExifInterface(filePath);
            ExifInterface thumbnailEi = new ExifInterface(thumbnailPath);
            int orientationFile = fileEi.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int orientationThumbnail = thumbnailEi.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Log.e(AliyunTag.TAG, "orientationFile "+orientationFile+", orientationThumbnailFile "+orientationThumbnail);
            if (orientationFile != orientationThumbnail) {
                thumbnailEi.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(orientationFile));
                thumbnailEi.saveAttributes();
            }
        } catch (IOException e) {
            Log.e(AliyunTag.TAG, "check if need rotate thumbnail failed", e);
        } catch (Exception e2) {
            Log.e(AliyunTag.TAG, "check if need rotate thumbnail failed", e2);
        }
    }

    private Cursor createThumbnailAndRequery(MediaInfo info, ContentResolver resolver) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        MediaStore.Images.Thumbnails.getThumbnail(resolver,
                info.id, MediaStore.Images.Thumbnails.MICRO_KIND, options);
        Cursor thumbCursor = resolver.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Images.Thumbnails.DATA,
                        MediaStore.Images.Thumbnails.IMAGE_ID
                },
                MediaStore.Images.Thumbnails.IMAGE_ID + "=?",
                new String[]{String.valueOf(info.id)}, null);
        return thumbCursor;
    }

    private List<MediaInfo> cacheMediaList;
    private MediaDir cacheDir;

    private void addMediaInfo(MediaInfo info) {
        String[] dir = info.filePath.split("/");
        String dirName = dir[dir.length - 2];
        MediaDir dirInfo = findMediaDirByName(dirName);
        if (dirInfo == null) {
            dirInfo = new MediaDir();
            dirInfo.id = info.id;
            dirInfo.type = info.type;
            dirInfo.dirName = dirName;
            dirInfo.thumbnailUrl = info.thumbnailPath;
            dirInfo.VideoDirPath = info.filePath.substring(0,
                    info.filePath.lastIndexOf("/"));
            if (dirs.size() == 0) {
                MediaDir all = new MediaDir();
                all.thumbnailUrl = info.thumbnailPath;
                all.id = -1;
                all.resId = info.id;
                dirs.add(all);
            }
            dirs.add(dirInfo);
            if (onMediaDirUpdateListener != null) {
                onMediaDirUpdateListener.onDirUpdate(dirInfo);
            }
        }

        List<MediaInfo> list;
        if (mediaByDir.containsKey(dirInfo)) {
            list = mediaByDir.get(dirInfo);
            if (list == null) {
                list = new ArrayList<>();
            }
        } else {
            list = new ArrayList<>();
            mediaByDir.put(dirInfo, list);
        }
        if (TextUtils.equals(cacheDirName, dirInfo.dirName)) {
            if (cacheMediaList == null) {
                cacheMediaList = new ArrayList<>();
                cacheDir = dirInfo;
            }
            cacheMediaList.add(info);
            dirInfo.fileCount = cacheMediaList.size();
        } else {
            list.add(info);
            dirInfo.fileCount = list.size();
        }

    }

}
