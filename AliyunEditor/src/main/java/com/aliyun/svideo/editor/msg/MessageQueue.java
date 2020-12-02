package com.aliyun.svideo.editor.msg;

import android.os.Handler;
import android.os.Looper;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

public class MessageQueue extends Thread {
    private ConcurrentLinkedQueue<Message> mQueue = new ConcurrentLinkedQueue<>();
    private CountDownLatch mLatch = null;
    private volatile boolean mQuit = false;
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    public MessageQueue() {
        start();
    }

    public synchronized void pushMessage(Message message) {
        mQueue.add(message);
        if (mLatch != null) {
            mLatch.countDown();
            mLatch = null;
        }
    }

    public synchronized void clearMessage(MessageInfo info) {
        if (info == null) {
            return;
        }
        Iterator<Message> it = mQueue.iterator();
        while (it.hasNext()) {
            Message i = it.next();
            if (i.getInfo().getMsgId() == info.getId()) {
                it.remove();
            }
        }

    }

    public synchronized void quitSafely() {
        mQueue.clear();
        mQuit = true;
        if (mLatch != null) {
            mLatch.countDown();
            mLatch = null;
        }
    }

    @Override
    public void run() {
        super.run();
        while (!mQuit) {
            if (mQueue.isEmpty()) {
                mLatch = new CountDownLatch(1);
                try {
                    mLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            final Message m = mQueue.remove();
            MessageInfo info = m.getInfo();
            switch (info.getMsgType()) {
            case MessageInfo.MSG_BACKGROUND:
                break;
            case MessageInfo.MSG_HANDLER:
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        m.getHandler().onHandleMessage(m.getMessageBody());
                    }
                });
                break;
            case MessageInfo.MSG_SYNC:
                m.getHandler().onHandleMessage(m.getMessageBody());
                break;
            }
        }
    }
}
