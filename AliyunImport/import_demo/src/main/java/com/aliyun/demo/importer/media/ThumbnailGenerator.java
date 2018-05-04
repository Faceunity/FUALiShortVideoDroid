/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.importer.media;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.provider.MediaStore;


import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.aliyun.demo.importer.media.MediaStorage.TYPE_PHOTO;


public class ThumbnailGenerator {

    public interface OnThumbnailGenerateListener {

        void onThumbnailGenerate(int key, Bitmap thumbnail);

    }

    private Map<Integer, OnThumbnailGenerateListener> listeners = new HashMap<>();

    private Executor executor;
    private ContentResolver resolver;
    private Handler handler = new Handler();
//    private MemoryCache cache;

    public ThumbnailGenerator(Context context){
        executor = Executors.newFixedThreadPool(3);
        resolver = context.getContentResolver();
//        cache = new MemoryCache();
    }

    public void generateThumbnail(int type, int id, int resId ,OnThumbnailGenerateListener listener){
        int key = generateKey(type,id);
//        String memoryKey = generateKey(String.valueOf(key), 120, 120);
//        Bitmap bmp = cache.get(memoryKey);
//        if(bmp != null){
//            listener.onThumbnailGenerate(key, bmp);
//            return ;
//        }
        ThumbnailTask task = new ThumbnailTask(type, id,resId);
        listeners.put(key, listener);
        executor.execute(task);
    }

    public void cancelAllTask(){
        ((ExecutorService)executor).shutdown();
//        cache.clear();
    }

    private class ThumbnailTask implements Runnable {

        private int type;
        private int id;
        private int resId;

        public ThumbnailTask(int type, int id,int resId){
            this.type = type;
            this.id = id;
            this.resId = resId;
        }

        @Override
        public void run() {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            final Bitmap bitmap ;
            if (type == TYPE_PHOTO) {
                bitmap = MediaStore.Images.Thumbnails.getThumbnail(resolver,
                        id == -1?resId:id, MediaStore.Images.Thumbnails.MICRO_KIND, options);
            } else {
                bitmap = MediaStore.Video.Thumbnails.getThumbnail(resolver,
                        id == -1?resId:id, MediaStore.Video.Thumbnails.MICRO_KIND, options);
            }
            final int key = generateKey(type, id);
            if(bitmap == null){
                if(listeners.containsKey(key)){
                    listeners.remove(key);
                }
                return ;
            }
//            String memoryKey = MemoryCacheUtils.generateKey(String.valueOf(key), new ImageSize(120, 120));
//            cache.put(memoryKey, bitmap);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(listeners.containsKey(key)){
                        OnThumbnailGenerateListener l = listeners.remove(key);
                        if(l != null){
                            l.onThumbnailGenerate(key, bitmap);
                        }
                    }
                }
            });
        }

    }

    public static String generateKey(String imageUri, int width, int height) {
        return imageUri + "_" + width + "x" + height;
    }

    public static int generateKey(int type, int id){
        return type << 16 | id;
    }

    class MemoryCache {
        private final Map<String, Reference<Bitmap>> softMap = new HashMap<>();

        public Bitmap get(String key) {
            Bitmap result = null;
            Reference reference = (Reference)this.softMap.get(key);
            if(reference != null) {
                result = (Bitmap)reference.get();
            }

            return result;
        }

        public boolean put(String key, Bitmap value) {
            this.softMap.put(key, this.createReference(value));
            return true;
        }

        public Bitmap remove(String key) {
            Reference bmpRef = (Reference)this.softMap.remove(key);
            return bmpRef == null?null:(Bitmap)bmpRef.get();
        }

        public Collection<String> keys() {
            Map var1 = this.softMap;
            synchronized(this.softMap) {
                return new HashSet(this.softMap.keySet());
            }
        }

        public void clear() {
            for(Reference<Bitmap> ref : softMap.values()){
                Bitmap bmp = ref.get();
                if(bmp != null){
                    bmp.recycle();
                }
            }
            this.softMap.clear();
        }

        protected Reference<Bitmap> createReference(Bitmap var1){
            return new SoftReference<>(var1);
        }

    }

}
