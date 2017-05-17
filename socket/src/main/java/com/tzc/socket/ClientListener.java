package com.tzc.socket;

import com.tzc.socket.bean.User;

/**
 * Created by tongzhichao on 17-5-11.
 */

public interface ClientListener {
    void onMessageReceive(String msg, User user);

    void onMessageSendSuccess(int time);

    void onJoin();
}
