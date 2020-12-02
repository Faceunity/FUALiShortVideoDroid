package com.aliyun.svideo.editor.msg;

public class Message<T> {
    private MessageInfo mInfo;
    private T mMessageBody;
    private MessageHandler mHandler;

    public MessageInfo getInfo() {
        return mInfo;
    }

    public void setInfo(MessageInfo info) {
        mInfo = info;
    }

    public T getMessageBody() {
        return mMessageBody;
    }

    public void setMessageBody(T messageBody) {
        mMessageBody = messageBody;
    }

    public Message(MessageInfo info,
                   T messageBody,
                   MessageHandler handler) {
        mInfo = info;
        mMessageBody = messageBody;
        mHandler = handler;
    }

    public MessageHandler getHandler() {
        return mHandler;
    }
}
