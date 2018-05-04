package com.aliyun.demo.msg;

import android.os.Bundle;

public interface MessageHandler {
    <T> int onHandleMessage(T message);
}
