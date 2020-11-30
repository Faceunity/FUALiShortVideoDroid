package com.aliyun.svideo.editor.msg;

public class MessageInfo {
    public static final int MSG_BACKGROUND = 1; //后台线程
    public static final int MSG_SYNC = 2;       //同步响应
    public static final int MSG_HANDLER = 3;    //主线程响应
    private int mId;
    private int mMsgType;
    private int mMsgId;
    private Class<?> mMessageBody;

    protected int getMsgType() {
        return mMsgType;
    }

    protected void setMsgType(int msgType) {
        mMsgType = msgType;
    }

    public int getMsgId() {
        return mMsgId;
    }

    public void setMsgId(int msgId) {
        mMsgId = msgId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof MessageInfo) {
            MessageInfo other = (MessageInfo) obj;
            return other.mId == mId &&
                   other.mMsgId == mMsgId && other.mMsgType == mMsgType
                   && other.getMessageBody().getName() ==
                   mMessageBody.getName();
        } else {
            return false;
        }
    }

    protected int getId() {
        return mId;
    }

    protected void setId(int id) {
        mId = id;
    }

    protected Class<?> getMessageBody() {
        return mMessageBody;
    }

    protected void setMessageBody(Class<?> messageBody) {
        mMessageBody = messageBody;
    }
}


