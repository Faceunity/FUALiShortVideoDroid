package com.aliyun.apsaravideo.music.music;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aliyun.apsaravideo.music.R;
import com.aliyun.common.utils.ToastUtil;
import com.aliyun.downloader.FileDownloaderCallback;
import com.liulishuo.filedownloader.BaseDownloadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicChooseView extends LinearLayout implements View.OnClickListener {
    private static final String TAG = "MusicChooseView";
    private ImageView mAliyunBackBtn;
    private TextView mAliyunCompeletBtn;
    private TextView mAliyunOnlineMusic;
    private TextView mAliyunLocalMusic;
    private RecyclerView mAliyunMusicRecyclerView;
    private MusicAdapter mMusicAdapter;
    private Handler mPlayHandler = new Handler(Looper.getMainLooper());
    private MediaPlayer mMediaPlayer;
    //视频录制时长
    private int mRecordTime = 10 * 1000;
    //截取音乐的开始时间
    private int mStartTime;
    //音乐播放循环时间
    private int mLoopTime = 10 * 1000;
    //选中音乐路径
    private String mMusicPath = "";
    //选中音乐是否是本地音乐
    private boolean isLocalMusic = false;
    private boolean isPlaying = false;
    private int playedTime;
    private int mCurrentSelectIndex = 0;
    private int mLastSelectIndex = 0;
    private ArrayList<EffectBody<MusicFileBean>> mLocalMusicList = new ArrayList<>();
    private ArrayList<EffectBody<MusicFileBean>> mOnlineMusicList = new ArrayList<>();
    private MediaMetadataRetriever mmr;
    private MusicLoader musicLoader;
    private MusicSelectListener musicSelectListener;
    private MusicFileBean mSelectMusic;
    private TextView mAlivcMusicCopyrightTV;
    /**
     * 选中的角标
     */
    private int mSelectPosition;

    private boolean isViewAttached;
    /**
     * 用于判断当前界面是否可见, 如果不可见, 下载完成后不能自动播放
     */
    private boolean isVisible;

    /**
     * 判断该界面是否显示过
     */
    boolean isShowed;
    /**
     * 缓存上次选择的音乐
     */
    private MusicFileBean mCacheMusic;
    /**
     * 缓存上次选择的时间
     */
    private int mCacheStartTime;
    /**
     * 缓存上次选择的角标
     */
    private int mCachePosition;
    /**
     * 缓存上次选择的tab 网络/本地
     */
    private boolean mCacheIsLocalMusic;

    public MusicChooseView(Context context) {
        super(context);
        init();
    }

    public MusicChooseView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MusicChooseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        initView();
        initData();
    }
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mMediaPlayer = new MediaPlayer();
        mmr = new MediaMetadataRetriever();
        isViewAttached = true;
        setVisibleStatus(true);
        if (mCacheIsLocalMusic) {
            mAliyunLocalMusic.performClick();
        } else {
            mAliyunOnlineMusic.performClick();
        }
        //恢复上次选择的音乐和开始时间 并且开始播放
        if (isShowed && mCacheMusic != null && mMusicAdapter != null) {


            mMusicAdapter.notifySelectPosition(mCacheStartTime, mCachePosition);
            mAliyunMusicRecyclerView.scrollToPosition(mCachePosition);
            Log.d(TAG, "onAttachedToWindow notifySelectPosition");
            try {
                prepareMusiceInfo(mCacheMusic, mCacheMusic.path);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaPlayer.setLooping(true);
            mPlayHandler.postDelayed(mMusciRunnable, 0);
        } else if (isShowed && mMusicAdapter != null) {
            mMusicAdapter.notifySelectPosition(0, 0);
            mAliyunMusicRecyclerView.scrollToPosition(0);
        }

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setVisibleStatus(false);
        isViewAttached = false;
        mPlayHandler.removeCallbacks(mMusciRunnable);
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mmr.release();
    }


    private void initData() {
        if (musicLoader == null) {
            musicLoader = new MusicLoader(getContext());
            musicLoader.setCallback(new MusicLoader.LoadCallback() {
                @Override
                public void onLoadLocalMusicCompleted(List<EffectBody<MusicFileBean>> loacalMusic) {
                    mLocalMusicList.clear();
                    mLocalMusicList.addAll(loacalMusic);
                    if (isLocalMusic) {
                        mMusicAdapter.setData(mLocalMusicList, 0);
                    }
                }

                @Override
                public void onLoadNetMusicCompleted(List<EffectBody<MusicFileBean>> netMusic) {
                    mOnlineMusicList.clear();
                    mOnlineMusicList.addAll(netMusic);
                    if (!isLocalMusic) {
                        mMusicAdapter.setData(mOnlineMusicList, 0);
                    }
                }
            });
            musicLoader.loadAllMusic();
        }

    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.alivc_svideo_music_chooser, this, true);
        mAliyunBackBtn = findViewById(R.id.aliyun_back_btn);
        mAliyunBackBtn.setOnClickListener(this);
        mAliyunCompeletBtn = findViewById(R.id.aliyun_compelet_btn);
        mAliyunCompeletBtn.setOnClickListener(this);
        mAliyunOnlineMusic = findViewById(R.id.aliyun_online_music);
        mAliyunOnlineMusic.setOnClickListener(this);
        mAliyunLocalMusic = findViewById(R.id.aliyun_local_music);
        mAliyunLocalMusic.setOnClickListener(this);
        mAliyunMusicRecyclerView = findViewById(R.id.aliyun_music_list);
        mAlivcMusicCopyrightTV = findViewById(R.id.alivc_music_copyright);
        mAliyunMusicRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        if (mMusicAdapter == null) {
            mMusicAdapter = new MusicAdapter();
            mMusicAdapter.setRecordDuration(mRecordTime);
            mMusicAdapter.setOnMusicSeekListener(new MusicAdapter.OnMusicSeek() {
                @Override
                public void onSeekStop(long start) {
                    mPlayHandler.removeCallbacks(mMusciRunnable);
                    mStartTime = (int)start;
                    mPlayHandler.postDelayed(mMusciRunnable, 0);
                }

                @Override
                public void onSelectMusic(final int position, final EffectBody<MusicFileBean> effectBody) {
                    final MusicFileBean musicFileBean = effectBody.getData();
                    mSelectMusic = musicFileBean;
                    mSelectPosition = position;

                    if (effectBody.isLocal()) {
                        onMusicSelected(musicFileBean, position);
                    } else {

                        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                            //如果正在播放音乐，停止播放
                            mMediaPlayer.stop();
                        }
                        musicLoader.downloadMusic(musicFileBean, new FileDownloaderCallback() {
                            @Override
                            public void onStart(int downloadId, long soFarBytes, long totalBytes, int preProgress) {
                                super.onStart(downloadId, soFarBytes, totalBytes, preProgress);
                                if (!isLocalMusic) {
                                    mMusicAdapter.notifyDownloadingStart(effectBody);
                                }

                            }

                            @Override
                            public void onProgress(int downloadId, long soFarBytes, long totalBytes, long speed,
                                                   int progress) {
                                super.onProgress(downloadId, soFarBytes, totalBytes, speed, progress);
                                if (!isLocalMusic) {
                                    mMusicAdapter.updateProcess(
                                        (MusicAdapter.MusicViewHolder) mAliyunMusicRecyclerView
                                        .findViewHolderForAdapterPosition(position), progress, position);
                                }
                            }

                            @Override
                            public void onFinish(int downloadId, String path) {
                                super.onFinish(downloadId, path);
                                effectBody.getData().setPath(path);
                                //if (isVisible) {
                                if (mMusicAdapter == null) {
                                    return;
                                }
                                // 无论是否可见, 都要去刷新界面信息
                                // 否则在下载过程中退后台, 当下载进度完成再返回前台时, 下载进度会卡在99%的状态
                                if (position == mMusicAdapter.getSelectIndex() && !isLocalMusic) {
                                    onMusicSelected(effectBody.getData(), position);

                                }
                                mMusicAdapter.notifyDownloadingComplete((MusicAdapter.MusicViewHolder)
                                                                        mAliyunMusicRecyclerView
                                                                        .findViewHolderForAdapterPosition(position), effectBody, position);
                                //}

                            }

                            @Override
                            public void onError(BaseDownloadTask task, Throwable e) {
                                super.onError(task, e);
                                ToastUtil.showToast(getContext(), R.string.aliyun_download_failed);
                            }
                        });

                    }

                }
            });
        }
        mAliyunMusicRecyclerView.setAdapter(mMusicAdapter);

    }

    private void onMusicSelected(MusicFileBean musicFileBean, int position) {

        if (mSelectPosition != position ) {
            //恢复时，不能重置
            mStartTime = 0;
        }
        try {
            if (isVisible) {
                //mPlayHandler.removeCallbacks(mMusciRunnable);
                //mMediaPlayer.reset();
                //if (path == null || path.isEmpty()) {
                //    mMusicPath = null;
                //    return;
                //}
                //mMediaPlayer.setDataSource(path);
                //mMediaPlayer.prepare();
                //
                ////mmr.setDataSource(path);
                ////long duration = Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                //int duration = mMediaPlayer.getDuration();
                //mSelectMusic.duration = duration;
                //if (duration < mRecordTime) {
                //    mLoopTime = (int)duration;
                //} else {
                //    mLoopTime = mRecordTime;
                //}
                //mMusicPath = path;
                //musicFileBean.setDuration(duration);
                //mMusicAdapter.notifyItemChanged(position);
                //mMediaPlayer.setLooping(true);
                //mPlayHandler.postDelayed(mMusciRunnable, 0);
                prepareMusiceInfo(musicFileBean, musicFileBean.path);
                mMusicAdapter.notifyItemChanged(position);
                mMediaPlayer.setLooping(true);
                mPlayHandler.postDelayed(mMusciRunnable, 0);
            } else if (isShowed) {
                // 如果界面不可见, 且曾经显示过, 再去更新item信息, 但不能播放
                prepareMusiceInfo(musicFileBean, musicFileBean.path);
                mMusicAdapter.notifyItemChanged(position);
                mMediaPlayer.setLooping(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 准备要播放的音乐资源
     */
    private void prepareMusiceInfo(MusicFileBean musicFileBean, String path) throws IOException, IllegalStateException {
        mPlayHandler.removeCallbacks(mMusciRunnable);
        mMediaPlayer.reset();
        if (TextUtils.isEmpty(path)) {
            mMusicPath = null;
            return;
        }
        mMediaPlayer.setDataSource(path);
        mMediaPlayer.prepare();

        int duration = mMediaPlayer.getDuration();
        mSelectMusic.duration = duration;
        if (duration < mRecordTime) {
            mLoopTime = (int)duration;
        } else {
            mLoopTime = mRecordTime;
        }
        mMusicPath = path;
        musicFileBean.setDuration(duration);

    }

    private Runnable mMusciRunnable = new Runnable() {
        @Override
        public void run() {
            mMediaPlayer.seekTo(mStartTime);
            mMediaPlayer.start();
            mPlayHandler.postDelayed(this, mLoopTime);
        }
    };

    @Override
    public void onClick(View v) {
        if (v == mAliyunBackBtn) {
            if (musicSelectListener != null) {
                musicSelectListener.onCancel();
            }
        } else if (v == mAliyunCompeletBtn) {

            if (musicSelectListener != null) {
                musicSelectListener.onMusicSelect(mSelectMusic, mStartTime);
                //缓存选择的值
                mCacheMusic = mSelectMusic;
                mCacheStartTime = mStartTime;
                mCachePosition = mSelectPosition;
                mCacheIsLocalMusic = isLocalMusic;
            }
        } else if (v == mAliyunOnlineMusic) {
            if (isLocalMusic) {
                isLocalMusic = false;

                mMusicAdapter.setData(mOnlineMusicList, 0);
            }
            mAlivcMusicCopyrightTV.setVisibility(View.VISIBLE);
            mAliyunOnlineMusic.setSelected(true);
            mAliyunLocalMusic.setSelected(false);

        } else if (v == mAliyunLocalMusic) {

            if (!isLocalMusic) {
                isLocalMusic = true;
                mMusicAdapter.setData(mLocalMusicList, 0);
            }

            mAlivcMusicCopyrightTV.setVisibility(View.INVISIBLE);
            mAliyunOnlineMusic.setSelected(false);
            mAliyunLocalMusic.setSelected(true);

        }
    }

    public void setMusicSelectListener(MusicSelectListener musicSelectListener) {
        this.musicSelectListener = musicSelectListener;
    }

    public void setRecordTime(int mRecordTime) {
        this.mRecordTime = mRecordTime;
        if (mMusicAdapter != null) {
            mMusicAdapter.setRecordDuration(mRecordTime);
        }
    }


    /**
     * 设置view的可见状态, 如果不可见, 则停止音乐播放, 如果可见,开始播放
     *
     * @param visibleStatus true: 可见, false: 不可见
     */
    public void setVisibleStatus(boolean visibleStatus) {
        isVisible = visibleStatus;
        if (isViewAttached) {
            if (visibleStatus) {
                //if (isPlaying) {
                mMediaPlayer.start();
                mPlayHandler.postDelayed(mMusciRunnable, mLoopTime - playedTime);
                //}
                isShowed = true;
            } else {
                isVisible = false;
                if (mMediaPlayer.isPlaying()) {
                    isPlaying = true;
                    mPlayHandler.removeCallbacks(mMusciRunnable);
                    playedTime = mMediaPlayer.getCurrentPosition() - mStartTime;
                    mMediaPlayer.pause();
                }
            }
        }
    }


}
