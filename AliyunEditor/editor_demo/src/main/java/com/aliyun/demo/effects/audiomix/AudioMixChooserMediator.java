/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.effects.audiomix;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.aliyun.common.global.AppInfo;
import com.aliyun.common.logger.Logger;
import com.aliyun.demo.editor.EditorActivity;
import com.aliyun.demo.editor.R;
import com.aliyun.demo.effects.control.BaseChooser;
import com.aliyun.demo.effects.control.EffectInfo;
import com.aliyun.demo.effects.control.OnItemClickListener;
import com.aliyun.demo.effects.control.UIEditorPage;
import com.aliyun.demo.util.Common;
import com.aliyun.jasonparse.JSONSupportImpl;
import com.aliyun.qupai.editor.AliyunIPlayer;
import com.aliyun.qupaiokhttp.HttpRequest;
import com.aliyun.qupaiokhttp.StringHttpRequestCallback;
import com.aliyun.quview.PagerSlidingTabStrip;
import com.aliyun.struct.form.MusicForm;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;



public class AudioMixChooserMediator extends BaseChooser implements OnItemClickListener, OnClickListener {

    private static final String MUSIC_WEIGHT = "music_weight";
    private static final String MUSIC_WEIGHT_KEY = "music_weight_key";

    private ViewPager mViewPager;
    private RecyclerView mOnlineMusicRecyclerView;
    private RecyclerView mLocalMusicRecyclerView;
    private SeekBar mMusicWeightSeekBar;
    private ImageView mVoiceBtn;
    private PagerSlidingTabStrip mTabPageIndicator;
    private MusicQuery mMusicQuery;
    private EffectInfo mMusicWeightInfo = new EffectInfo();
    private LocalAudioMixAdapter mLocalMusicAdapter;
    private OnlineAudioMixAdapter mOnlineAudioMixAdapter;
    private ArrayList<MusicForm> mMusicList = new ArrayList<>();


    public static AudioMixChooserMediator newInstance() {
        AudioMixChooserMediator dialog = new AudioMixChooserMediator();
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
        initResourceOnLine();
    }
    private int getMusicWeight(){
        return getContext().getSharedPreferences(MUSIC_WEIGHT, Context.MODE_PRIVATE).getInt(MUSIC_WEIGHT_KEY,50);
    }
    private void saveMusicWeight(){
        Context context = getContext();
        if(context != null) {
            SharedPreferences.Editor editor = context.getSharedPreferences(MUSIC_WEIGHT, Context.MODE_PRIVATE).edit();
            int weight = mMusicWeightSeekBar.getProgress();
            editor.putInt(MUSIC_WEIGHT_KEY, weight);
            editor.commit();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        saveMusicWeight();
        if (mMusicQuery != null) {
            mMusicQuery.cancel(true);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = LayoutInflater.from(getActivity()).inflate(R.layout.aliyun_svideo_music_view, container);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewPager = (ViewPager) view.findViewById(R.id.music_content_container);
        mVoiceBtn = (ImageView) view.findViewById(R.id.voice_btn);
        mVoiceBtn.setOnClickListener(this);
        mMusicWeightSeekBar = (SeekBar) view.findViewById(R.id.music_weight);
        mMusicWeightSeekBar.setMax(100);
        int musicWeight = getMusicWeight();
        mMusicWeightSeekBar.setProgress(musicWeight);
        mMusicWeightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                if(mOnEffectChangeListener != null){
                    mMusicWeightInfo.isAudioMixBar = true;
                    mMusicWeightInfo.type = UIEditorPage.AUDIO_MIX;
                    mMusicWeightInfo.musicWeight = seekBar.getMax() - i;
                    mOnEffectChangeListener.onEffectChange(mMusicWeightInfo);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mOnlineMusicRecyclerView = new RecyclerView(view.getContext());
        mOnlineMusicRecyclerView.setBackgroundColor(getResources().getColor(R.color.music_back_color));
        mOnlineMusicRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false));
        mOnlineAudioMixAdapter = new OnlineAudioMixAdapter(getActivity(), mOnlineMusicRecyclerView);
        mOnlineAudioMixAdapter.setOnItemClickListener(this);
        mOnlineMusicRecyclerView.setAdapter(mOnlineAudioMixAdapter);
        mOnlineMusicRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mLocalMusicRecyclerView = new RecyclerView(view.getContext());
        mLocalMusicRecyclerView.setBackgroundColor(getResources().getColor(R.color.music_back_color));
        mLocalMusicRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false));
        mLocalMusicAdapter = new LocalAudioMixAdapter(getActivity());
        mLocalMusicAdapter.setOnItemClickListener(this);
        mLocalMusicRecyclerView.setAdapter(mLocalMusicAdapter);
        mLocalMusicRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mViewPager.setAdapter(new MusicPagerAdapter());
        mTabPageIndicator = (PagerSlidingTabStrip) view.findViewById(R.id.music_content_container_indicator);
        mTabPageIndicator.setTextColorResource(R.color.aliyun_svideo_tab_text_color_selector);
        mTabPageIndicator.setTabViewId(R.layout.aliyun_svideo_layout_tab_top);
        mTabPageIndicator.setViewPager(mViewPager);
        if(mEditorService != null && mEditorService.isFullScreen()) {
            mOnlineMusicRecyclerView.setBackgroundColor(getResources().getColor(R.color.action_bar_bg_50pct));
            mLocalMusicRecyclerView.setBackgroundColor(getResources().getColor(R.color.action_bar_bg_50pct));
            mTabPageIndicator.setBackgroundColor(getResources().getColor(R.color.tab_bg_color_50pct));
        }
        mMusicQuery = new MusicQuery(view.getContext());
        mMusicQuery.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        mMusicQuery.setOnResProgressListener(new MusicQuery.OnResProgressListener() {
            @Override
            public void onResProgress(ArrayList<MusicQuery.MediaEntity> musics) {
                mLocalMusicAdapter.setData(musics);
                if(mEditorService == null){
                    return;
                }
                int index = getLocalLaseSelectIndex(mEditorService.getEffectIndex(UIEditorPage.AUDIO_MIX),musics);
                mLocalMusicAdapter.setSelectedIndex(index);
                mLocalMusicRecyclerView.scrollToPosition(index);
            }
        });
    }

    private void initResourceOnLine() {
        StringBuilder requestUrl = new StringBuilder();
        requestUrl.append(Common.BASE_URL)
                .append("/api/res/type/5")
                .append("?packageName=").append(getActivity().getApplicationInfo().packageName)
                .append("&signature=").append(AppInfo.getInstance().obtainAppSignature(getActivity().getApplicationContext()));
        Logger.getDefaultLogger().d("pasterUrl url = " +requestUrl.toString());
        HttpRequest.get(requestUrl.toString(),
                new StringHttpRequestCallback() {
                    @Override
                    protected void onSuccess(String s) {
                        super.onSuccess(s);
                        JSONSupportImpl jsonSupport = new JSONSupportImpl();

                        try {
                            List<MusicForm> resourceList = jsonSupport.readListValue(s,
                                    new TypeToken<List<MusicForm>>(){}.getType());
                            if (resourceList != null && resourceList.size() > 0) {
                                mMusicList = (ArrayList<MusicForm>) resourceList;
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            mMusicList = null;
                        }
                        if(mMusicList != null){
                            MusicForm empty = new MusicForm();
                            mMusicList.add(0,empty);
                        }
                        mOnlineAudioMixAdapter.setData(mMusicList);
                        if(mEditorService != null){
                            int index = getLastSelectIndex(mEditorService.getEffectIndex(UIEditorPage.AUDIO_MIX),mMusicList);
                            mOnlineAudioMixAdapter.setSelectedIndex(index);
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String msg) {
                        super.onFailure(errorCode, msg);
                        if(mMusicList != null){
                            MusicForm empty = new MusicForm();
                            mMusicList.add(0,empty);
                            mOnlineAudioMixAdapter.setData(mMusicList);
                            int index = getLastSelectIndex(mEditorService.getEffectIndex(UIEditorPage.AUDIO_MIX),mMusicList);
                            mOnlineAudioMixAdapter.setSelectedIndex(index);
                        }
                    }
                });
    }
    private int getLastSelectIndex(int id,ArrayList<MusicForm> mMusicList){
        int index = 0;
        if(mMusicList == null){
            return index;
        }
        for(MusicForm musicForm : mMusicList){
            if(musicForm.getId() ==  id){
                break;
            }
            index++;
        }
        return index;
    }

    private int getLocalLaseSelectIndex(int id, ArrayList<MusicQuery.MediaEntity> musics) {
        int index = 0;
        if(musics == null) {
            return index;
        }
        for(MusicQuery.MediaEntity mediaEntity : musics) {
            String displayName = mediaEntity.display_name;
            if(displayName != null && !"".equals(displayName) && displayName.hashCode() == id) {
                break;
            }
            index++;
        }
        return index;
    }

    @Override
    public boolean onItemClick(EffectInfo effectInfo, int index) {
        if (mOnEffectChangeListener != null) {
            effectInfo.musicWeight = mMusicWeightSeekBar.getMax() - mMusicWeightSeekBar.getProgress();
            mOnEffectChangeListener.onEffectChange(effectInfo);
        }
        if(effectInfo.isLocalMusic){
            mOnlineAudioMixAdapter.clearSelect();
        }else{
            mLocalMusicAdapter.clearSelect();
        }
        mEditorService.addTabEffect(UIEditorPage.AUDIO_MIX,effectInfo.id);
        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.voice_btn) {
            AliyunIPlayer mPlayer = ((EditorActivity)getActivity()).getPlayer();
            if(mPlayer != null) {
                boolean isAudioSilence = mPlayer.isAudioSilence();
                mPlayer.setAudioSilence(!isAudioSilence);
                mVoiceBtn.setSelected(!isAudioSilence);
            }
        }
    }

    private class MusicPagerAdapter extends PagerAdapter {
        ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return getString(R.string.my_music);
            } else if (position == 1) {
                return getString(R.string.local_music);
            }
            return "";
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (position == 0) {
                container.addView(mOnlineMusicRecyclerView, params);
                return mOnlineMusicRecyclerView;
            } else if (position == 1) {
                container.addView(mLocalMusicRecyclerView, params);
                return mLocalMusicRecyclerView;
            }
            return null;
        }
    }
}
