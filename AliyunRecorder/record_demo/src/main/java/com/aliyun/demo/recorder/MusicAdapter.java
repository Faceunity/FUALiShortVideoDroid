package com.aliyun.demo.recorder;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aliyun.demo.R;
import com.aliyun.demo.recorder.util.MusicQuery;
import com.aliyun.quview.MusicHorizontalScrollView;
import com.aliyun.quview.MusicWaveView;

import java.util.ArrayList;


public class MusicAdapter extends RecyclerView.Adapter implements View.OnClickListener{
    private ArrayList<MusicQuery.MediaEntity> data = new ArrayList<>();
    private OnMusicSeek onMusicSeek;
    private int mRecordDuration = 10*1000;
    private int mSelectIndex = 0;
    private String mSelectMusicPath;
    private int[] mScrollX;



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MusicViewHolder holder = new MusicViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.aliyun_svideo_layout_music_item, parent, false));
        return holder;
    }

    public void setData(ArrayList<MusicQuery.MediaEntity> data,int selectIndex){
        this.data.clear();
        this.data.addAll(data);
        MusicQuery.MediaEntity mediaEntity = new MusicQuery.MediaEntity();
        this.data.add(0,mediaEntity);
        mScrollX = new int[this.data.size()];
        mSelectIndex = selectIndex;
        mSelectMusicPath = this.data.get(selectIndex).path;
        if(onMusicSeek != null){
            onMusicSeek.onSelectMusic(mSelectMusicPath);
        }
        notifyDataSetChanged();
    }

    public void setRecordDuration(int duration){
        mRecordDuration = duration;
    }

    public int getSelectIndex(){
        return mSelectIndex;
    }

    public void setOnMusicSeekListener(OnMusicSeek l){
        onMusicSeek = l;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        Log.e("HorizontalScrollView","position ... " +position);
        final MusicQuery.MediaEntity mediaEntity = data.get(position);
        ((MusicViewHolder)holder).musicNameLayout.setTag(holder);
        ((MusicViewHolder)holder).musicNameLayout.setOnClickListener(this);

        if(mediaEntity.title == null && mediaEntity.path == null){
            ((MusicViewHolder)holder).musicName.setText(R.string.aliyun_empty_music);
            ((MusicViewHolder)holder).musicSinger.setVisibility(View.GONE);
            ((MusicViewHolder)holder).musicInfoLayout.setVisibility(View.GONE);

            if(mSelectIndex == 0){
                ((MusicViewHolder)holder).musicSelect.setVisibility(View.VISIBLE);
            }else{
                ((MusicViewHolder)holder).musicSelect.setVisibility(View.INVISIBLE);
            }
        }else if(position == mSelectIndex){
            ((MusicViewHolder)holder).musicName.setText(mediaEntity.title);
            if(mediaEntity.artist == null || mediaEntity.artist.isEmpty()){
                ((MusicViewHolder)holder).musicSinger.setVisibility(View.GONE);
            }else{
                ((MusicViewHolder)holder).musicSinger.setVisibility(View.VISIBLE);
                ((MusicViewHolder)holder).musicSinger.setText(mediaEntity.artist);
            }
            ((MusicViewHolder)holder).musicInfoLayout.setVisibility(View.VISIBLE);
            ((MusicViewHolder)holder).musicWave.setDisplayTime(mRecordDuration);
            ((MusicViewHolder)holder).musicWave.setTotalTime(mediaEntity.duration);
            ((MusicViewHolder)holder).musicWave.layout();
            ((MusicViewHolder)holder).scrollBar.setScrollViewListener(new MusicHorizontalScrollView.ScrollViewListener() {
                @Override
                public void onScrollChanged(HorizontalScrollView scrollView, int x, int y, int oldx, int oldy) {
                    mScrollX[position] = x;

                    setDurationTxt(((MusicViewHolder)holder),x,mediaEntity.duration);
                }

                @Override
                public void onScrollStop() {
                    if(onMusicSeek != null){
                        onMusicSeek.onSeekStop((int) ((float)mScrollX[position] / ((MusicViewHolder)holder).musicWave.getMusicLayoutWidth() * mediaEntity.duration));
                    }
                }
            });
            ((MusicViewHolder)holder).scrollBar.scrollTo(mScrollX[position],0);
            ((MusicViewHolder)holder).musicSelect.setVisibility(View.VISIBLE);
        }else {
            ((MusicViewHolder)holder).musicName.setText(mediaEntity.title);
            if(mediaEntity.artist == null || mediaEntity.artist.isEmpty()){
                ((MusicViewHolder)holder).musicSinger.setVisibility(View.GONE);
            }else{
                ((MusicViewHolder)holder).musicSinger.setVisibility(View.VISIBLE);
                ((MusicViewHolder)holder).musicSinger.setText(mediaEntity.artist);
            }
            ((MusicViewHolder)holder).musicInfoLayout.setVisibility(View.GONE);
            ((MusicViewHolder)holder).scrollBar.setScrollViewListener(null);
            ((MusicViewHolder)holder).musicSelect.setVisibility(View.INVISIBLE);
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

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onClick(View v) {
        MusicViewHolder holder = (MusicViewHolder) v.getTag();
        int position = holder.getAdapterPosition();
        if(position == mSelectIndex){
            return;
        }
        mSelectMusicPath = data.get(position).path;
        mSelectIndex = position;
        notifyDataSetChanged();
        if(onMusicSeek != null){
            onMusicSeek.onSelectMusic(mSelectMusicPath);
        }
//        if(onItemClickListener != null){
//            onItemClickListener.OnItemClick(path,0,0);
//        }
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
        public ImageView musicSelect;

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
            musicSelect = (ImageView) itemView.findViewById(R.id.aliyun_music_select);
            setDurationTxt(this,0,0);
        }

    }

    public interface OnMusicSeek {
        void onSeekStop(long start);
        void onSelectMusic(String path);
    }
}
