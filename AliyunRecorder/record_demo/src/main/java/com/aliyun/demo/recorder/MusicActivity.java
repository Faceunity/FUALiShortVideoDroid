package com.aliyun.demo.recorder;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.demo.R;
import com.aliyun.demo.recorder.util.Common;
import com.aliyun.demo.recorder.util.MusicQuery;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;


public class MusicActivity extends Activity implements View.OnClickListener{

    private RecyclerView mMusicList;
    private MusicQuery mMusicQuery;
    private MusicAdapter mMusicAdapter;
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private Handler mPlayHandler = new Handler(Looper.getMainLooper());
    private int mLoopTime = 10*1000;
    private int mRecordTime;
    private int mStartTime;
    private ImageView mBackBtn;
    private TextView mCompeletBtn,mOnlineMusicBtn,mLocalMusicBtn;
    private int mCurrentSelectIndex = 0;
    private int mLastSelectIndex = 0;

    private String mMusicPath;
    private ArrayList<MusicQuery.MediaEntity> mLocalMusicList = new ArrayList<>();
    private ArrayList<MusicQuery.MediaEntity> mOnlineMusicList = new ArrayList<>();
    private boolean isLocalMusic = false;
    private boolean isPlaying = false;
    private int playedTime;
    private MediaMetadataRetriever mmr = new MediaMetadataRetriever();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.aliyun_svideo_activity_music);
        getData();
        initView();
        mMusicQuery.setOnResProgressListener(new MusicQuery.OnResProgressListener() {
            @Override
            public void onResProgress(ArrayList<MusicQuery.MediaEntity> musics) {
                mLocalMusicList.clear();
                mLocalMusicList.addAll(musics);
                if(isLocalMusic){
                    mMusicAdapter.setData(musics,0);
                }
            }
        });
        mMusicQuery.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getData(){
        mRecordTime = getIntent().getIntExtra(CameraDemo.MUSIC_MAX_RECORD_TIME,10 *1000);
        File[] files = new File(Common.SD_DIR + Common.QU_NAME + "/mp3").listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if(name != null && (name.endsWith(".mp3") || name.endsWith(".m4a")||name.endsWith(".m4r")|| name.endsWith(".aac"))) {
                    return true;
                }
                return false;
            }

        });
        for(final File file : files) {
            String path = file.getAbsolutePath();
            MusicQuery.MediaEntity entity = new MusicQuery.MediaEntity();
            entity.path = path;
            mmr.setDataSource(path);
            entity.artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            entity.title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            if(entity.title == null || entity.title.isEmpty()){
                entity.title = file.getName();
            }
            try {
                entity.duration = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            }catch (Exception e){
                e.printStackTrace();
            }
            mOnlineMusicList.add(entity);
        }

    }

    private void initView(){
        mMusicList = (RecyclerView) findViewById(R.id.aliyun_music_list);
        mMusicList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mBackBtn = (ImageView) findViewById(R.id.aliyun_back_btn);
        mBackBtn.setOnClickListener(this);
        mCompeletBtn = (TextView) findViewById(R.id.aliyun_compelet_btn);
        mCompeletBtn.setOnClickListener(this);
        mOnlineMusicBtn = (TextView) findViewById(R.id.aliyun_online_music);
        mOnlineMusicBtn.setOnClickListener(this);
        mLocalMusicBtn = (TextView) findViewById(R.id.aliyun_local_music);
        mLocalMusicBtn.setOnClickListener(this);
        mMusicQuery = new MusicQuery(this);
        mMusicAdapter = new MusicAdapter();
        mMusicAdapter.setRecordDuration(mRecordTime);
        mMusicAdapter.setOnMusicSeekListener(new MusicAdapter.OnMusicSeek() {
            @Override
            public void onSeekStop(long start) {
                mPlayHandler.removeCallbacks(mMusciRunnable);
                mStartTime = (int) start;
                mPlayHandler.postDelayed(mMusciRunnable,0);
            }

            @Override
            public void onSelectMusic(String path) {
                mStartTime = 0;
                try {
                    mPlayHandler.removeCallbacks(mMusciRunnable);
                    mMediaPlayer.reset();
                    if(path == null || path.isEmpty()){
                        mMusicPath = null;
                        return;
                    }
                    mmr.setDataSource(path);
                    long duration = Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                    if(duration < mRecordTime){
                        mLoopTime = (int) duration;
                    }else{
                        mLoopTime = mRecordTime;
                    }
                    mMediaPlayer.setDataSource(path);
                    mMediaPlayer.prepare();
                    mMediaPlayer.setLooping(true);
                    mPlayHandler.postDelayed(mMusciRunnable,0);
                    mMusicPath = path;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        mMusicList.setAdapter(mMusicAdapter);
        mOnlineMusicBtn.performClick();
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
    protected void onPause() {
        super.onPause();
        if(mMediaPlayer.isPlaying()){
            isPlaying = true;
            mPlayHandler.removeCallbacks(mMusciRunnable);
            playedTime = mMediaPlayer.getCurrentPosition() - mStartTime;
            mMediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isPlaying){
            mMediaPlayer.start();
            mPlayHandler.postDelayed(mMusciRunnable, mLoopTime - playedTime);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mMusicQuery != null){
            mMusicQuery.cancel(true);
        }
        mPlayHandler.removeCallbacks(mMusciRunnable);
        if(mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
        }
        mMediaPlayer.release();
        mmr.release();
    }

    @Override
    public void onClick(View v) {
        if(v == mBackBtn){
            setResult(Activity.RESULT_CANCELED);
            finish();
        }else if(v == mCompeletBtn){
            Intent intent = new Intent();
            intent.putExtra(CameraDemo.MUSIC_PATH,mMusicPath);
            intent.putExtra(CameraDemo.MUSIC_START_TIME,mStartTime);
            setResult(Activity.RESULT_OK,intent);
            finish();
        }else if(v == mOnlineMusicBtn){
            mOnlineMusicBtn.setSelected(true);
            mLocalMusicBtn.setSelected(false);
            isLocalMusic = false;
            mLastSelectIndex = mCurrentSelectIndex;
            mCurrentSelectIndex = mMusicAdapter.getSelectIndex();
            mMusicAdapter.setData(mOnlineMusicList,mLastSelectIndex);
        }else if(v == mLocalMusicBtn){
            mOnlineMusicBtn.setSelected(false);
            mLocalMusicBtn.setSelected(true);
            isLocalMusic = true;
            mLastSelectIndex = mCurrentSelectIndex;
            mCurrentSelectIndex = mMusicAdapter.getSelectIndex();
            mMusicAdapter.setData(mLocalMusicList,mLastSelectIndex);
        }
    }
}
