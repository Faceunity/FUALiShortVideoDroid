package com.aliyun.svideo.editor.msg;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Dispatcher {
    private static Dispatcher sDefaultInstance;
    private ThreadLocal<MessageQueue> mThreadLocalQueue = new ThreadLocal<>();
    private ConcurrentHashMap<MessageInfo, MessageHandler> mServices = new ConcurrentHashMap<>();

    public synchronized void registerService(MessageInfo info, MessageHandler service) {
        Method[] methods = service.getClass().getDeclaredMethods();
        for (Method m : methods) {
            Class[] pmTypes = m.getParameterTypes();
            info.setMessageBody(pmTypes[0]);
        }
        info.setId(service.hashCode());
        mServices.put(info, service);
    }


    public synchronized void unRegisterService(MessageInfo messageAddr) {

    }

    public <T> void postMsg(int messageID, int msgType, T message) {
        for (Map.Entry<MessageInfo, MessageHandler> e : mServices.entrySet()) {
            if (messageID == e.getKey().getMsgId()
                    && message.getClass().equals(
                        e.getKey().getMessageBody())) {
                MessageQueue queue = mThreadLocalQueue.get();
                if (queue == null) {
                    queue = new MessageQueue();
                }
                queue.pushMessage(new Message(e.getKey(), message, e.getValue()));
            }
        }
        message.getClass();
    }



    public void register(Object subscriber) {
        EventBus.getDefault().register(subscriber);
    }

    public void unRegister(Object subscriber) {
        EventBus.getDefault().unregister(subscriber);
    }

    public void postMsg(Object message) {
        EventBus.getDefault().post(message);
    }

    public static Dispatcher getInstance() {
        if (sDefaultInstance == null) {
            synchronized (Dispatcher.class) {
                if (sDefaultInstance == null) {
                    sDefaultInstance = new Dispatcher();
                }
            }
        }
        return sDefaultInstance;
    }

}
