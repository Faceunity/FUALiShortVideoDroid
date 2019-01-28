package com.aliyun.apsaravideo.music.music;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
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
    private RecyclerView mAliyunMusicList;
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

    private boolean isViewAttached;
    /**
     * 用于判断当前界面是否可见, 如果不可见, 下载完成后不能自动播放
     */
    private boolean isVisible;

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
        if (isLocalMusic) {
            mAliyunLocalMusic.performClick();
        } else {
            mAliyunOnlineMusic.performClick();
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
        mAliyunMusicList = findViewById(R.id.aliyun_music_list);
        mAlivcMusicCopyrightTV = findViewById(R.id.alivc_music_copyright);
        mAliyunMusicList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

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

                    if (effectBody.isLocal()) {
                        onMusicSelected(musicFileBean, position);
                    } else {
                        onMusicSelected(new MusicFileBean(), position);
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
                                        (MusicAdapter.MusicViewHolder)mAliyunMusicList
                                            .findViewHolderForAdapterPosition(position), progress, position);
                                }
                            }

                            @Override
                            public void onFinish(int downloadId, String path) {
                                super.onFinish(downloadId, path);
                                effectBody.getData().setPath(path);
                                if (isVisible) {
                                    if (position == mMusicAdapter.getSelectIndex() && !isLocalMusic) {
                                        onMusicSelected(effectBody.getData(), position);

                                    }
                                    mMusicAdapter.notifyDownloadingComplete(effectBody, position);
                                }

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
        mAliyunMusicList.setAdapter(mMusicAdapter);

    }

    private void onMusicSelected(MusicFileBean musicFileBean, int position) {
        mSelectMusic = musicFileBean;
        String path = mSelectMusic.getPath();
        mStartTime = 0;
        try {
            if (isVisible) {
                mPlayHandler.removeCallbacks(mMusciRunnable);
                mMediaPlayer.reset();
                if (path == null || path.isEmpty()) {
                    mMusicPath = null;
                    return;
                }
                mMediaPlayer.setDataSource(path);
                mMediaPlayer.prepare();

                //mmr.setDataSource(path);
                //long duration = Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                int duration = mMediaPlayer.getDuration();
                mSelectMusic.duration = duration;
                if (duration < mRecordTime) {
                    mLoopTime = (int)duration;
                } else {
                    mLoopTime = mRecordTime;
                }
                mMusicPath = path;
                musicFileBean.setDuration(duration);
                mMusicAdapter.notifyItemChanged(position);
                mMediaPlayer.setLooping(true);
                mPlayHandler.postDelayed(mMusciRunnable, 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
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
            }
        } else if (v == mAliyunOnlineMusic) {
            if (isLocalMusic) {
                isLocalMusic = false;
                mLastSelectIndex = mCurrentSelectIndex;
                mCurrentSelectIndex = mMusicAdapter.getSelectIndex();
                mMusicAdapter.setData(mOnlineMusicList, mLastSelectIndex);
            } else {
                mMusicAdapter.setData(mOnlineMusicList, mMusicAdapter.getSelectIndex());
            }
            mAlivcMusicCopyrightTV.setVisibility(View.VISIBLE);
            mAliyunOnlineMusic.setSelected(true);
            mAliyunLocalMusic.setSelected(false);

        } else if (v == mAliyunLocalMusic) {

            if (!isLocalMusic) {
                isLocalMusic = true;
                mLastSelectIndex = mCurrentSelectIndex;
                mCurrentSelectIndex = mMusicAdapter.getSelectIndex();
                mMusicAdapter.setData(mLocalMusicList, mLastSelectIndex);
            } else {
                mMusicAdapter.setData(mLocalMusicList, mMusicAdapter.getSelectIndex());
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
                if (isPlaying) {
                    mMediaPlayer.start();
                    mPlayHandler.postDelayed(mMusciRunnable, mLoopTime - playedTime);
                }
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
