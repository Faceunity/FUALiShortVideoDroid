package com.aliyun.svideo.editor.msg;

import android.os.Bundle;

public interface MessageHandler {
    <T> int onHandleMessage(T message);
}
