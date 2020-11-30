package com.aliyun.svideo.music.music;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.aliyun.svideo.music.R;
import com.aliyun.common.utils.CommonUtil;
import com.aliyun.common.utils.StorageUtils;
import com.aliyun.common.utils.ToastUtil;
import com.aliyun.svideo.downloader.DownloaderManager;
import com.aliyun.svideo.downloader.FileDownloaderCallback;
import com.aliyun.svideo.downloader.FileDownloaderModel;
import com.aliyun.svideo.base.http.EffectService;
import com.aliyun.svideo.base.http.HttpCallback;
import com.aliyun.svideo.base.http.MusicFileBean;
import com.liulishuo.filedownloader.BaseDownloadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MusicLoader {
    public static final int EFFECT_MUSIC = 5;       //音乐

    private final MusicQuery mMusicQuery;
    private LoadCallback callback;
    private final Context mContext;
    public EffectService mService = new EffectService();
    public void loadAllMusic() {
        loadLocalMusic();
        loadMoreOnlineMusic();

    }

    public MusicLoader(Context context) {
        mContext = context;
        mMusicQuery = new MusicQuery(context);
    }

    /**
     * 加载本地音乐
     */
    public void loadLocalMusic() {
        mMusicQuery.setOnResProgressListener(new MusicQuery.OnResProgressListener() {
            @Override
            public void onResProgress(ArrayList<MusicFileBean> musics) {
                List<EffectBody<MusicFileBean>> effectBodyList = new ArrayList<>();
                for (MusicFileBean musicFileBean : musics) {
                    effectBodyList.add(new EffectBody<MusicFileBean>(musicFileBean, true));
                }
                if (callback != null) {
                    callback.onLoadLocalMusicCompleted(effectBodyList);
                }
            }
        });
        mMusicQuery.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }
    private int pageSize = 25;
    private int pageNo = 1;
    /**
     * 是否正在加载网络音乐
     */
    private boolean isLoadingMusic;
    private boolean isMusicEnd;
    /**
     * 搜索音乐
     */
    public void searchOnlineMusic(String keyWord) {
        loadOnlineMusic(1, 25, keyWord);

    }
    public void loadMoreOnlineMusic() {
        if (isLoadingMusic || isMusicEnd) {
            return;
        }
        loadOnlineMusic(pageNo, pageSize, "");
    }
    /**
     *加载网络音乐
     */
    private void loadOnlineMusic(int pageNo, final int pageSize, final String keyWord) {
        if (!CommonUtil.hasNetwork(mContext)) {
            ToastUtil.showToast(mContext, R.string.alivc_music_network_not_connect);
            return;
        }
        isLoadingMusic = true;
        mService.loadingMusicData(mContext.getPackageName(), pageNo, pageSize, keyWord, new HttpCallback<List<MusicFileBean>>() {
            @Override
            public void onSuccess(List<MusicFileBean> result) {
                isLoadingMusic = false;
                if (TextUtils.isEmpty(keyWord) && result.size() < pageSize) {
                    isMusicEnd = true;
                }
                List<EffectBody<MusicFileBean>> effectBodyList = new ArrayList<>();
                List<FileDownloaderModel> modelsTemp = DownloaderManager.getInstance().getDbController().getResourceByType(EFFECT_MUSIC);
                for (MusicFileBean musicFileBean : result) {
                    EffectBody<MusicFileBean> effectBody = new EffectBody<>(musicFileBean, false);
                    for (FileDownloaderModel fileDownloaderModel : modelsTemp) {
                        if (musicFileBean.getMusicId().equals(fileDownloaderModel.getDownload()) && new File(fileDownloaderModel.getPath()).exists()) {
                            musicFileBean.setPath(fileDownloaderModel.getPath());
                            effectBody.setLocal(true);
                        }
                    }
                    effectBodyList.add(effectBody);
                }
                if (callback != null) {
                    if (TextUtils.isEmpty(keyWord)) {
                        callback.onLoadNetMusicCompleted(effectBodyList);
                    } else {
                        callback.onSearchNetMusicCompleted(effectBodyList);
                    }

                }
                MusicLoader.this.pageNo++;
                MusicLoader.this.pageSize++;
            }

            @Override
            public void onFailure(Throwable e) {
                isLoadingMusic = false;
            }
        });

    }
    /**
     * 下载音乐
     * @param musicFileBean
     * @param callback
     */
    public void downloadMusic(final MusicFileBean musicFileBean, final FileDownloaderCallback callback) {

        if (!CommonUtil.hasNetwork(mContext)) {
            ToastUtil.showToast(mContext, R.string.alivc_music_network_not_connect);
            return;
        }
        if (CommonUtil.SDFreeSize() < 10 * 1000 * 1000) {
            Toast.makeText(mContext, R.string.alivc_music_no_free_memory, Toast.LENGTH_SHORT).show();
            return;
        }
        mService.getMusicDownloadUrlById(mContext.getPackageName(), musicFileBean.musicId, new HttpCallback<String>() {
            @Override
            public void onSuccess(String result) {
                String url = result;
                if (TextUtils.isEmpty(url)) {
                    ToastUtil.showToast(mContext,  mContext.getResources().getString(R.string.alivc_music_play_url_null));
                    return;
                }
                final FileDownloaderModel downloaderModel = new FileDownloaderModel();
                downloaderModel.setUrl(url);
                downloaderModel.setDownload(musicFileBean.getMusicId());
                downloaderModel.setName(musicFileBean.title);
                downloaderModel.setIsunzip(0);
                downloaderModel.setDuration(musicFileBean.duration);
                downloaderModel.setPath(StorageUtils.getFilesDirectory(mContext) + "/music/" + musicFileBean.title);
                downloaderModel.setDescription(musicFileBean.artist);
                downloaderModel.setEffectType(EFFECT_MUSIC);
                if (downloaderModel == null) {
                    Log.e("MusicLoader", "downloaderModel is null" );
                }
                final FileDownloaderModel model = DownloaderManager.getInstance().addTask(downloaderModel, url);
                if (model == null) {
                    Log.e("MusicLoader", "model is null" );
                }
                if (DownloaderManager.getInstance().isDownloading(model.getTaskId(), model.getPath())) {
                    return;
                }
                DownloaderManager.getInstance().startTask(model.getTaskId(), new FileDownloaderCallback() {
                    @Override
                    public void onFinish(int downloadId, String path) {
                        callback.onFinish(downloadId, path);
                    }

                    @Override
                    public void onStart(int downloadId, long soFarBytes, long totalBytes, int preProgress) {
                        callback.onStart(downloadId, soFarBytes, totalBytes, preProgress);
                    }

                    @Override
                    public void onProgress(int downloadId, long soFarBytes, long totalBytes, long speed,
                                           int progress) {
                        callback.onProgress(downloadId, soFarBytes, totalBytes, speed, progress);
                    }

                    @Override
                    public void onError(BaseDownloadTask task, Throwable e) {
                        ToastUtil.showToast(mContext, e.getMessage());
                        DownloaderManager.getInstance().deleteTaskByTaskId(model.getTaskId());
                        DownloaderManager.getInstance().getDbController().deleteTask(model.getTaskId());
                        callback.onError(task, e );
                    }
                });
            }

            @Override
            public void onFailure(Throwable e) {

            }
        });

    }

    public interface LoadCallback {
        void onLoadLocalMusicCompleted(List<EffectBody<MusicFileBean>> loacalMusic);
        void onLoadNetMusicCompleted(List<EffectBody<MusicFileBean>> netMusic);
        void onSearchNetMusicCompleted(List<EffectBody<MusicFileBean>> result);

    }

    public void setCallback(LoadCallback callback) {
        this.callback = callback;
    }
}
