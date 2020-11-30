package com.aliyun.svideo.editor.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadUtil {

    private static int corePoolSize = 1;
    private static int maximumPoolSize = 1;
    private static int keepAliveTime = 60;

    public static ExecutorService newDynamicSingleThreadedExecutor(ThreadFactory threadFactory) {
        ThreadPoolExecutor mExecutorService = new ThreadPoolExecutor(
            corePoolSize,
            maximumPoolSize,
            keepAliveTime,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
            threadFactory,
            new ThreadPoolExecutor.AbortPolicy());
        mExecutorService.allowCoreThreadTimeOut(true);

        return mExecutorService;
    }

}
