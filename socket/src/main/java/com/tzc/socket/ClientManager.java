package com.tzc.socket;

import android.text.TextUtils;
import android.util.Log;

import com.tzc.socket.bean.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by tongzhichao on 17-5-11.
 */

public class ClientManager {

    private static final String TAG = "ClientManager";

    private static final ClientManager mInstance = new ClientManager();

    private ClientListener mClientListener;
    private Socket mSocket;

    private BlockingQueue<String> mMessages = new ArrayBlockingQueue<>(Common.MAX_MESSAGE_COUNT);
    private BlockingQueue<String> mFeedBacks = new ArrayBlockingQueue<>(Common.MAX_MESSAGE_COUNT);

    private ClientManager() {
    }

    public void setSocketListener(ClientListener clientListener) {
        mClientListener = clientListener;
    }

    public static ClientManager getInstance() {
        return mInstance;
    }

    public void join(String ip, int port) {
        new SocketThread(ip, port).start();
    }

    private class SocketThread extends Thread {
        private int port;
        private String ip;

        public SocketThread(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        @Override
        public void run() {
            super.run();
            try {
                mSocket = new Socket(ip, port);
                new ReadThread().start();
                new SendThread().start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    BufferedReader in;

    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();

            if (mSocket == null) {
                return;
            }
            try {
                in = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            String line;
            while (!mSocket.isClosed()) {
                try {
                    StringBuilder message = new StringBuilder();
                    if (!TextUtils.isEmpty(line = in.readLine())) {
                        message.append(line);
                        Log.d(TAG, "message append: " + line);
                    }
                    if (!TextUtils.isEmpty(message.toString())) {
                        Log.d(TAG, "message:" + message);
                        Message msg = Message.getMessage(message.toString());
                        if (msg.getType() == Message.MESSAGE) {
                            feedBack(msg.getTime());
                        } else if (msg.getType() == Message.FEEDBACK) {
                        }

                    }
                    sleep(Common.SLEEP_TIME);
                } catch (Exception e) {
                }
            }

        }
    }

    PrintWriter printWriter;

    private class SendThread extends Thread {
        @Override
        public void run() {
            super.run();
            if (mSocket == null) {
                return;
            }
            try {
                printWriter = new PrintWriter(mSocket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            new FeedbackThread().start();
            while (!mSocket.isClosed()) {
                String message;
                try {
                    message = mMessages.take();
                    printWriter.println(message);
                    printWriter.flush();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private class FeedbackThread extends Thread {
        @Override
        public void run() {
            super.run();
            if (mSocket == null) {
                return;
            }
            while (!mSocket.isClosed()) {

                while (!mSocket.isClosed()) {
                    String message;
                    try {
                        message = mFeedBacks.take();
                        printWriter.println(message);
                        printWriter.flush();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    private void feedBack(int time) {
        try {
            mFeedBacks.put(Message.feedback(time));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
