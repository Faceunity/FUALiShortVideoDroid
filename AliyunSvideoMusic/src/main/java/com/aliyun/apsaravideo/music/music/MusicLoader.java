package com.aliyun.apsaravideo.music.music;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.aliyun.apsaravideo.music.R;
import com.aliyun.common.utils.CommonUtil;
import com.aliyun.common.utils.StorageUtils;
import com.aliyun.common.utils.ToastUtil;
import com.aliyun.downloader.DownloaderManager;
import com.aliyun.downloader.FileDownloaderCallback;
import com.aliyun.downloader.FileDownloaderModel;
import com.aliyun.qupaiokhttp.HttpRequest;
import com.aliyun.qupaiokhttp.StringHttpRequestCallback;
import com.liulishuo.filedownloader.BaseDownloadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MusicLoader {
    public static final int EFFECT_MUSIC = 5;       //音乐
    /**
     * 音螺音乐详情URL
     */
    private static final String URL_MUSIC_DETAIL = "https://demo-vod.cn-shanghai.aliyuncs"
        + ".com/voddemo/XiamiApiMltpMusicPlayinfo?BusinessType=vodsdk&TerminalType=pc&DeviceModel=iPhone9,"
        + "2&UUID=59ECA-4193-4695-94DD-7E1247288&AppVersion=1.0.0&play_info_get={\"music_id\":\"%s\"}";
    private final MusicQuery mMusicQuery;
    private LoadCallback callback;
    private final Context mContext;

    public void loadAllMusic(){
        loadLocalMusic();
        loadOnlinMusic();

    }

    public MusicLoader(Context context) {
        mContext = context;
        mMusicQuery = new MusicQuery(context);
    }

    /**
     * 加载本地音乐
     */
    public void loadLocalMusic(){
        mMusicQuery.setOnResProgressListener(new MusicQuery.OnResProgressListener() {
            @Override
            public void onResProgress(ArrayList<MusicFileBean> musics) {
                List<EffectBody<MusicFileBean>> effectBodyList = new ArrayList<>();
                for (MusicFileBean musicFileBean:musics){
                    effectBodyList.add(new EffectBody<MusicFileBean>(musicFileBean,true));
                }
                if (callback!=null){
                    callback.onLoadLocalMusicCompleted(effectBodyList);
                }
            }
        });
        mMusicQuery.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    /**
     *加载网络音乐
     */
    public void loadOnlinMusic(){
        List<EffectBody<MusicFileBean>> effectBodyList = new ArrayList<>();
        List<MusicFileBean> musicList = new MusicMockData().loadingMusicData();
        List<FileDownloaderModel> modelsTemp = DownloaderManager.getInstance().getDbController().getResourceByType(EFFECT_MUSIC);
        for (MusicFileBean musicFileBean:musicList){
            EffectBody<MusicFileBean> effectBody = new EffectBody<>(musicFileBean,false);
            for (FileDownloaderModel fileDownloaderModel:modelsTemp){
                if (musicFileBean.getMusicId().equals(fileDownloaderModel.getDownload())&&new File(fileDownloaderModel.getPath()).exists()) {
                    musicFileBean.setPath(fileDownloaderModel.getPath());
                    effectBody.setLocal(true);
                }
            }
            effectBodyList.add(effectBody);
        }
        if (callback!=null){
            callback.onLoadNetMusicCompleted(effectBodyList);
        }

    }

    /**
     * 加载网络缓存数据
     */
    public void loadCacheMusic(){


    }

    /**
     * 下载音乐
     * @param musicFileBean
     * @param callback
     */
    public void downloadMusic(final MusicFileBean musicFileBean, final FileDownloaderCallback callback){
        if (!CommonUtil.hasNetwork(mContext)) {
            ToastUtil.showToast(mContext, R.string.aliyun_network_not_connect);
            return;
        }
        if (CommonUtil.SDFreeSize() < 10 * 1000 * 1000) {
            Toast.makeText(mContext, R.string.aliyun_no_free_memory, Toast.LENGTH_SHORT).show();
            return;
        }
        String url = String.format(URL_MUSIC_DETAIL,musicFileBean.musicId);
        HttpRequest.get(url,new StringHttpRequestCallback(){
            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);
                try {
                    Log.e("TAG",s );
                    JSONObject jsonObject = new JSONObject(s);
                    String code = jsonObject.getString("code");
                    if ("ok".equals(code)){
                        JSONObject result = jsonObject.getJSONObject("result");
                        String status = result.getString("status");
                        if ("true".equals(status)){
                            String url = result.getJSONObject("result_obj").getString("listen_file_url");
                            final FileDownloaderModel downloaderModel = new FileDownloaderModel();
                            downloaderModel.setUrl(url);
                            downloaderModel.setDownload(musicFileBean.getMusicId());
                            downloaderModel.setName(musicFileBean.title);
                            downloaderModel.setIsunzip(0);
                            downloaderModel.setDuration(musicFileBean.duration);
                            downloaderModel.setPath(StorageUtils.getFilesDirectory(mContext)+"/music/"+musicFileBean.title);
                            downloaderModel.setDescription(musicFileBean.artist);
                            downloaderModel.setEffectType(EFFECT_MUSIC);
                            final FileDownloaderModel model = DownloaderManager.getInstance().addTask(downloaderModel, url);
                            if (DownloaderManager.getInstance().isDownloading(model.getTaskId(), model.getPath())) {
                                return;
                            }
                            DownloaderManager.getInstance().startTask(model.getTaskId(),new FileDownloaderCallback(){
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
                                    ToastUtil.showToast(mContext, R.string.aliyun_download_failed);
                                    DownloaderManager.getInstance().deleteTaskByTaskId(model.getTaskId());
                                    DownloaderManager.getInstance().getDbController().deleteTask(model.getTaskId());
                                    callback.onError(task,e );
                                }
                            });
                        }else {
                            ToastUtil.showToast(mContext, R.string.alivc_load_fail);
                        }
                    }else {
                        ToastUtil.showToast(mContext, R.string.alivc_load_fail);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int errorCode, String msg) {
                super.onFailure(errorCode, msg);

            }
        });

    }

    public interface LoadCallback{
        void onLoadLocalMusicCompleted(List<EffectBody<MusicFileBean>> loacalMusic);
        void onLoadNetMusicCompleted(List<EffectBody<MusicFileBean>> netMusic);

    }

    public void setCallback(LoadCallback callback) {
        this.callback = callback;
    }
}
