/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.apsaravideo.music.music;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;

public class MusicQuery extends AsyncTask<Void, ArrayList<MusicFileBean>, Void> {
    private static final String TAG = "MusicQuery";

    private Context context;
    private OnResProgressListener l;

    public MusicQuery(Context context) {
        this.context = context;
    }

    public void setOnResProgressListener(OnResProgressListener l) {
        this.l = l;
    }

    public static boolean checkIsMusic(int time, long size) {
        if (time <= 0 || size <= 0) {
            return false;
        }

        time /= 1000;
        int minute = time / 60;
//  int hour = minute / 60;
        int second = time % 60;
        minute %= 60;
        if (minute <= 0 && second <= 30) {
            return false;
        }
        if (size <= 1024 * 1024) {
            return false;
        }
        return true;
    }

    @Override
    protected void onProgressUpdate(ArrayList<MusicFileBean>... values) {
        if(l != null){
            l.onResProgress(values[0]);
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        Cursor cursor = null;
        ArrayList<MusicFileBean> mediaList = null;
        try {
            cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{
                            MediaStore.Audio.Media._ID,
                            MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.DISPLAY_NAME,
                            MediaStore.Audio.Media.DURATION,
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media.DATA,
                            MediaStore.Audio.Media.SIZE,
                            MediaStore.Audio.Media.MIME_TYPE},
                    String.format("%1$s IN (?)",
                            MediaStore.Audio.Media.MIME_TYPE), new String[]{"audio/mpeg"}, MediaStore.Audio.Media.DATE_ADDED + " DESC");
            if (cursor == null) {
                Log.d(TAG, "The getMediaList cursor is null.");

            }
            int count = cursor.getCount();
            if (count <= 0) {
                Log.d(TAG, "The getMediaList cursor count is 0.");

            }
            mediaList = new ArrayList<>();
            MusicFileBean mediaEntity = null;
//          String[] columns = cursor.getColumnNames();
            while (cursor.moveToNext()) {
                mediaEntity = new MusicFileBean();
                mediaEntity.id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                mediaEntity.title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                mediaEntity.displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                mediaEntity.duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                mediaEntity.size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                mediaEntity.artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                mediaEntity.path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                if(mediaEntity.displayName.endsWith("mp3")||mediaEntity.displayName.endsWith("aac")) {
                    mediaList.add(mediaEntity);
                }
                if (mediaList.size() % 20 == 0) {
                    publishRes(mediaList);
                }
            }
            publishRes(mediaList);
        } catch (Exception e) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private void publishRes(ArrayList<MusicFileBean> list){
        ArrayList<MusicFileBean> copy = new ArrayList<>();
        copy.addAll(list);
        publishProgress(copy);
    }



    public interface OnResProgressListener {
        void onResProgress(ArrayList<MusicFileBean> musics);
    }
}
