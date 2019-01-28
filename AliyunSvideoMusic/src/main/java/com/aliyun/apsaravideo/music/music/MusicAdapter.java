package com.aliyun.apsaravideo.music.music;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aliyun.apsaravideo.music.R;
import com.aliyun.svideo.base.widget.CircleProgressBar;
import com.aliyun.svideo.base.widget.MusicHorizontalScrollView;
import com.aliyun.svideo.base.widget.MusicWaveView;
import com.aliyun.svideo.sdk.external.struct.form.IMVForm;

import java.util.ArrayList;
import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter{
    private List<EffectBody<MusicFileBean>> dataList = new ArrayList<>();
    private int mRecordDuration = 10*1000;
    private OnMusicSeek onMusicSeek;
    private int mSelectIndex = 0;
    private int[] mScrollX;

    private ArrayList<MusicFileBean> mLoadingMusic = new ArrayList<>();//正在下载的的音乐

    private static final int VIEW_TYPE_NO = 0;
    private static final int VIEW_TYPE_LOCAL = 1;
    private static final int VIEW_TYPE_REMOTE = 2;
    private static final int VIEW_TYPE_DOWNLOADING = 3;
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MusicViewHolder holder = new MusicViewHolder(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.aliyun_svideo_layout_music_item, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        ((MusicViewHolder)holder).updateData(position,dataList.get(position));
        int viewType = getItemViewType(position);
        final MusicFileBean musicFileBean = dataList.get(position).getData();
        switch (viewType) {
            case VIEW_TYPE_NO:
                ((MusicViewHolder)holder).musicName.setText(R.string.aliyun_empty_music);
                ((MusicViewHolder)holder).mLocalIcon.setVisibility(View.GONE);
                ((MusicViewHolder)holder).musicSinger.setVisibility(View.GONE);
                ((MusicViewHolder)holder).musicInfoLayout.setVisibility(View.GONE);
                ((MusicViewHolder)holder).scrollBar.setScrollViewListener(null);
                if(mSelectIndex == 0){
                    ((MusicViewHolder)holder).musicName.setSelected(true);
                    ((MusicViewHolder)holder).musicSinger.setSelected(true);
                }else{
                    ((MusicViewHolder)holder).musicName.setSelected(false);
                    ((MusicViewHolder)holder).musicSinger.setSelected(false);
                }
                break;
            case VIEW_TYPE_LOCAL:

                ((MusicViewHolder)holder).downloadProgress.setVisibility(View.GONE);
                if (position == mSelectIndex){
                    ((MusicViewHolder)holder).mLocalIcon.setVisibility(View.VISIBLE);
                    ((MusicViewHolder)holder).musicName.setSelected(true);
                    ((MusicViewHolder)holder).musicSinger.setSelected(true);
                    ((MusicViewHolder)holder).musicName.setText(musicFileBean.getTitle());
                    if(musicFileBean.artist == null || musicFileBean.artist.isEmpty()){
                        ((MusicViewHolder)holder).musicSinger.setVisibility(View.GONE);
                    }else{
                        ((MusicViewHolder)holder).musicSinger.setVisibility(View.VISIBLE);
                        ((MusicViewHolder)holder).musicSinger.setText(musicFileBean.artist);
                    }
                    ((MusicViewHolder)holder).musicInfoLayout.setVisibility(View.VISIBLE);
                    ((MusicViewHolder)holder).musicWave.setDisplayTime(mRecordDuration);
                    ((MusicViewHolder)holder).musicWave.setTotalTime(musicFileBean.duration);
                    ((MusicViewHolder)holder).musicWave.layout();
                    ((MusicViewHolder)holder).scrollBar.setScrollViewListener(new MusicHorizontalScrollView.ScrollViewListener() {
                        @Override
                        public void onScrollChanged(HorizontalScrollView scrollView, int x, int y, int oldx, int oldy) {
                            if (position<mScrollX.length){//添加判断，解决选择音乐片段的时候，切换模式引起的数组越界问题
                                mScrollX[position] = x;
                                setDurationTxt(((MusicViewHolder)holder),x,musicFileBean.duration);
                            }

                        }

                        @Override
                        public void onScrollStop() {

                            if(onMusicSeek != null&&position<mScrollX.length){//添加判断，解决选择音乐片段的时候，切换模式引起的数组越界问题
                                onMusicSeek.onSeekStop((int) ((float)mScrollX[position] / ((MusicViewHolder)holder).musicWave.getMusicLayoutWidth() * musicFileBean.duration));
                            }
                        }
                    });
                    ((MusicViewHolder)holder).scrollBar.scrollTo(mScrollX[position],0);
                }else {
                    ((MusicViewHolder)holder).mLocalIcon.setVisibility(View.GONE);
                    ((MusicViewHolder)holder).musicInfoLayout.setVisibility(View.GONE);
                    ((MusicViewHolder)holder).scrollBar.setScrollViewListener(null);
                    ((MusicViewHolder)holder).musicName.setSelected(false);
                    ((MusicViewHolder)holder).musicSinger.setSelected(false);
                }
                break;
            case VIEW_TYPE_REMOTE:
                ((MusicViewHolder)holder).mLocalIcon.setVisibility(View.GONE);
                ((MusicViewHolder)holder).downloadProgress.setVisibility(View.GONE);
                ((MusicViewHolder)holder).musicInfoLayout.setVisibility(View.GONE);
                ((MusicViewHolder)holder).scrollBar.setScrollViewListener(null);
                ((MusicViewHolder)holder).musicName.setSelected(false);
                ((MusicViewHolder)holder).musicSinger.setSelected(false);
                if(mSelectIndex == position){
                    ((MusicViewHolder)holder).musicName.setSelected(true);
                    ((MusicViewHolder)holder).musicSinger.setSelected(true);
                }else{
                    ((MusicViewHolder)holder).musicName.setSelected(false);
                    ((MusicViewHolder)holder).musicSinger.setSelected(false);
                }
                break;
            case VIEW_TYPE_DOWNLOADING:
                ((MusicViewHolder)holder).mLocalIcon.setVisibility(View.GONE);
                ((MusicViewHolder)holder).downloadProgress.setVisibility(View.VISIBLE);
                ((MusicViewHolder)holder).musicInfoLayout.setVisibility(View.GONE);
                ((MusicViewHolder)holder).scrollBar.setScrollViewListener(null);
                ((MusicViewHolder)holder).musicName.setSelected(false);
                ((MusicViewHolder)holder).musicSinger.setSelected(false);
                if(mSelectIndex == position){
                    ((MusicViewHolder)holder).musicName.setSelected(true);
                    ((MusicViewHolder)holder).musicSinger.setSelected(true);
                }else{
                    ((MusicViewHolder)holder).musicName.setSelected(false);
                    ((MusicViewHolder)holder).musicSinger.setSelected(false);
                }
                break;
            default:
                break;
        }




    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
    public void setData(ArrayList<EffectBody<MusicFileBean>> dataList, int selectIndex){

        this.dataList.clear();
        this.dataList.addAll(dataList);
        MusicFileBean mediaEntity = new MusicFileBean();
        EffectBody<MusicFileBean> effectBody = new EffectBody<>(mediaEntity,true);
        this.dataList.add(0,effectBody);
        mScrollX = new int[this.dataList.size()];
        mSelectIndex = selectIndex;
        if(onMusicSeek != null){
            onMusicSeek.onSelectMusic(selectIndex,this.dataList.get(selectIndex));
        }
        notifyDataSetChanged();
    }
    @Override
    public int getItemViewType(int position) {
        int type = VIEW_TYPE_NO;

        if(position > 0 && position < dataList.size()) {
            EffectBody<MusicFileBean> data = dataList.get(position);
            if(data.isLocal()) {
                return VIEW_TYPE_LOCAL;
            }else if(data.isLoading()) {
                return VIEW_TYPE_DOWNLOADING;
            }else {
                return VIEW_TYPE_REMOTE;
            }
        }
        return type;
    }
    /**
     * 下载开始
     * @param musicBody
     */
    public void notifyDownloadingStart(EffectBody<MusicFileBean> musicBody) {
        if(!mLoadingMusic.contains(musicBody.getData())) {
            mLoadingMusic.add(musicBody.getData());
            musicBody.setLoading(true);
        }
    }

    /**
     * 下载结束
     * @param mvBody
     * @param position
     */
    public synchronized void notifyDownloadingComplete(EffectBody<MusicFileBean> mvBody, int position) {
        mvBody.setLocal(true);
        mvBody.setLoading(false);
        mLoadingMusic.remove(mvBody.getData());
        notifyItemChanged(position);
    }

    public void updateProcess(MusicViewHolder viewHolder, int process, int position) {
        if(viewHolder != null && viewHolder.mPosition == position) {
            viewHolder.mLocalIcon.setVisibility(View.GONE);
            viewHolder.downloadProgress.setVisibility(View.VISIBLE);
            viewHolder.downloadProgress.setProgress(process);
        }
    }
    /**
     * 监听接口，外部实现
     */
    interface OnItemClickListener {
        /**
         * 外部实现下载事件
         * @param position 点击位置
         * @param data 数据
         */
        void onRemoteItemClick(int position, EffectBody<IMVForm> data);

        /**
         * 外部实现应用此mv
         * @param position 点击位置
         * @param data 该位置数据
         */
        void onLocalItemClick(int position, EffectBody<IMVForm> data);
    }
    class MusicViewHolder extends RecyclerView.ViewHolder{
        public TextView musicName;
        public TextView musicSinger;
        public MusicWaveView musicWave;
        public LinearLayout musicInfoLayout;
        public LinearLayout musicNameLayout;
        public MusicHorizontalScrollView scrollBar;
        public TextView musicStartTxt;
        public TextView musicEndTxt;
        public ImageView mLocalIcon;
        private CircleProgressBar downloadProgress;
        private EffectBody<MusicFileBean> mData;
        private int mPosition;
        public void updateData(int position, EffectBody<MusicFileBean> data) {
            this.mData = data;
            this.mPosition = position;
            MusicFileBean music = data.getData();
            musicName.setText(music.title);
            if(music.artist == null || music.artist.isEmpty()){
                musicSinger.setVisibility(View.GONE);
            }else{
                musicSinger.setVisibility(View.VISIBLE);
                musicSinger.setText("- "+music.artist);
            }
        }
        public MusicViewHolder(View itemView) {
            super(itemView);
            musicName = (TextView) itemView.findViewById(R.id.aliyun_music_name);
            musicSinger = (TextView) itemView.findViewById(R.id.aliyun_music_artist);
            musicWave = (MusicWaveView) itemView.findViewById(R.id.aliyun_wave_view);
            musicInfoLayout = (LinearLayout) itemView.findViewById(R.id.aliyun_music_info_layout);
            musicNameLayout = (LinearLayout) itemView.findViewById(R.id.aliyun_music_name_layout);
            scrollBar = (MusicHorizontalScrollView) itemView.findViewById(R.id.aliyun_scroll_bar);
            musicStartTxt = (TextView) itemView.findViewById(R.id.aliyun_music_start_txt);
            musicEndTxt = (TextView) itemView.findViewById(R.id.aliyun_music_end_txt);
            mLocalIcon = itemView.findViewById(R.id.alivc_record_local_iv);
            downloadProgress = (CircleProgressBar) itemView.findViewById(R.id.download_progress);
            downloadProgress.isFilled(true);
            setDurationTxt(this,0,0);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelectIndex!=mPosition){
                        if(onMusicSeek!=null){
                            onMusicSeek.onSelectMusic(mPosition,mData);
                        }
                        mSelectIndex = mPosition;
                        if (mSelectIndex < mScrollX.length) {
                            //解决mSelectIndex引起的角标越界
                            mScrollX[mSelectIndex] = 0;
                            notifyDataSetChanged();

                        }
                    }


                }
            });
        }

    }
    private void setDurationTxt(MusicViewHolder holder,int x,int duration){
        int leftTime = (int) ((float)x / holder.musicWave.getMusicLayoutWidth() * duration);
        int rightTime = leftTime + mRecordDuration;
        int time = leftTime / 1000;
        int min = time / 60;
        int sec = time % 60;
        holder.musicStartTxt.setText(String.format("%1$02d:%2$02d", min, sec));
        time = rightTime / 1000;
        min = time / 60;
        sec = time % 60;
        holder.musicEndTxt.setText(String.format("%1$02d:%2$02d", min, sec));
    }
    public interface OnMusicSeek {
        void onSeekStop(long start);
        void onSelectMusic(int position, EffectBody<MusicFileBean> effectBody);
    }

    public void setRecordDuration(int mRecordDuration) {
        this.mRecordDuration = mRecordDuration;
    }

    public void setOnMusicSeekListener(OnMusicSeek onMusicSeek) {
        this.onMusicSeek = onMusicSeek;
    }

    public int getSelectIndex() {
        return mSelectIndex;
    }
}
