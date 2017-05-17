package com.tzc.socket;

import android.text.TextUtils;

import com.tzc.socket.bean.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * Created by tongzhichao on 17-5-11.
 */

public class ServerManager {

    private static final String TAG = "ServerManager";

    private static final int PORT = 1990;

    private static final ServerManager mInstance = new ServerManager();

    private ArrayList<Socket> mClients = new ArrayList<>();
    private HashMap<Socket, BlockingQueue<String>> mMessages = new HashMap<>();
    private HashMap<Socket, BlockingQueue<String>> mFeedBacks = new HashMap<>();

    private ServerListener mSocketListener;

    private ServerManager() {
    }

    private ServerSocket mServerSocket;
    private boolean isAccepting = false;

    public static ServerManager getInstance() {
        return mInstance;
    }

    public void setSocketListener(ServerListener socketListener) {
        mSocketListener = socketListener;
    }

    public int createServer(int port) {
        while (mServerSocket == null) {
            try {
                mServerSocket = new ServerSocket(port);
            } catch (IOException e) {
                Logger.d(TAG, "create Server by port " + port + " exception : " + e.getMessage() + " , port++ " + ++port);
            }
        }
        return mServerSocket.getLocalPort();
    }

    public void acceptServer(int port) throws RuntimeException {
        if (isAccepting) {
            throw new RuntimeException("ServerSocket is accepting...");
        }
        if (mServerSocket == null) {
            port= createServer(port);
        }
        isAccepting = true;
        new AcceptThread().start();
        if (mSocketListener != null) {
            mSocketListener.onServerCreated(port);
        }

    }

    public void acceptServer() {
        acceptServer(PORT);
    }

    public void stopAccept() {
        isAccepting = false;
    }

    private class AcceptThread extends Thread {
        @Override
        public void run() {
            while (isAccepting) {
                try {
                    Socket socket = mServerSocket.accept();
                    mClients.add(socket);
                    mMessages.put(socket, new ArrayBlockingQueue<String>(Common.MAX_MESSAGE_COUNT));
                    mFeedBacks.put(socket, new ArrayBlockingQueue<String>(Common.MAX_MESSAGE_COUNT));
                    new ReadThread(socket).start();
                    new SendThread(socket).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class ReadThread extends Thread {
        private Socket socket;

        public ReadThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            super.run();
            if (socket == null) {
                return;
            }
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            String line;
            while (!socket.isClosed()) {
                try {
                    StringBuilder message = new StringBuilder();
                    if (!TextUtils.isEmpty(line = bufferedReader.readLine())) {
                        message.append(line);
                    }
                    if (!TextUtils.isEmpty(message.toString())) {
                        Message msg = Message.getMessage(message.toString());
                        if (msg.getType() == Message.MESSAGE) {
                            if (mSocketListener != null) {
                                mSocketListener.onMessageReceive(msg.getMsg(), msg.getUser());
                            }
                            feedBack(mFeedBacks.get(socket), msg.getTime());
                        } else if (msg.getType() == Message.FEEDBACK) {
                            if (mSocketListener != null) {
                                mSocketListener.onMessageSendSuccess(msg.getTime());
                            }
                        }
                    }
                    sleep(Common.SLEEP_TIME);
                } catch (Exception e) {
                }
            }

        }
    }


    private void feedBack(BlockingQueue<String> blockingQueue, int time) {
        try {
            blockingQueue.put(Message.feedback(time));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendAll(String message) {
        message = Message.pack(message);
        try {
            for (int i = 0; i < mClients.size(); i++) {
                mMessages.get(mClients.get(i)).put(message);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class SendThread extends Thread {
        private Socket socket;

        public SendThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            super.run();
            if (socket == null) {
                return;
            }
            PrintWriter printWriter = null;
            try {
                printWriter = new PrintWriter(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            new FeedbackThread(socket).start();
            while (!socket.isClosed()) {
                String message;
                try {
                    message = mMessages.get(socket).take();
                    printWriter.println(message);
                    printWriter.flush();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class FeedbackThread extends Thread {
        private Socket socket;

        public FeedbackThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            super.run();
            if (socket == null) {
                return;
            }
            PrintWriter printWriter = null;
            try {
                printWriter = new PrintWriter(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!socket.isClosed()) {
                String message;
                try {
                    message = mFeedBacks.get(socket).take();
                    printWriter.println(message);
                    printWriter.flush();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
