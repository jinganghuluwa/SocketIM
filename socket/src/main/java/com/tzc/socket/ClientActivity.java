package com.tzc.socket;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.tzc.socket.bean.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "ClientActivity";
    private EditText ip, port, msg;
    private TextView msglist;
    private boolean isCientRunning;
    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.stop).setOnClickListener(this);
        findViewById(R.id.send).setOnClickListener(this);
        port = (EditText) findViewById(R.id.port);
        msg = (EditText) findViewById(R.id.message);
        ip = (EditText) findViewById(R.id.ip);
        msglist = (TextView) findViewById(R.id.msg_list);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.start:
                start();
                break;
            case R.id.stop:
                stop();
                break;
            case R.id.send:
                send();
                break;
        }
    }

    Socket socket = null;

    private void start() {
        if (isCientRunning) {
            return;
        }
        int portInt;
        try {
            portInt = Integer.parseInt(port.getText().toString());
        } catch (NumberFormatException e) {
            portInt = 1990;
        }
        new SocketThread(portInt).start();

    }

    private class SocketThread extends Thread {
        private int port;

        public SocketThread(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            super.run();
            try {
                socket = new Socket(ip.getText().toString(), port);
                isCientRunning = true;
                println("成功连接到服务器");
                new ReadThread().start();
                new SendThread().start();
            } catch (IOException e) {
                isCientRunning = false;
                e.printStackTrace();
                println("连接服务器失败：" + e.getMessage());
            }

        }
    }

    BufferedReader in;

    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();

            if (socket == null) {
                println("socket为空");
                return;
            }
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            String line;
            while (isCientRunning) {
                try {
                    StringBuilder message = new StringBuilder();
                    if (!TextUtils.isEmpty(line = in.readLine())) {
                        message.append(line);
                        Log.d(TAG,"message append: "+line);
                    }
                    if (!TextUtils.isEmpty(message.toString())) {
                        Log.d(TAG,"message:"+message);
                        Message msg = Message.getMessage(message.toString());
                        if(msg.getType()==Message.MESSAGE){
                            println(msg.getMsg());
                            feekback(msg.getTime());
                        }else if(msg.getType()==Message.FEEDBACK){
                            println("send ok : "+msg.getTime());
                        }

                    }
                    sleep(Common.SLEEP_TIME);
//                    in.close(); // 关闭Socket输入流
                } catch (Exception e) {
                }
            }

        }
    }

    PrintWriter write;

    private class SendThread extends Thread {
        @Override
        public void run() {
            super.run();
            if (socket == null) {
                println("socket为空");
                return;
            }
            try {
                write = new PrintWriter(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            new FeedbackThread().start();
            while (isCientRunning) {
                if (!TextUtils.isEmpty(message)) {
                    try {
                        write.println(message);
                        message = "";
                        write.flush();
                    } catch (Exception e) {
                        Log.d(TAG, "Exception:" + e.getMessage());
                    }
                }
                try {
                    sleep(Common.SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String feedback;

    private class FeedbackThread extends Thread {
        @Override
        public void run() {
            super.run();
            if (socket == null) {
                println("socket为空");
                return;
            }
            while (isCientRunning) {
                if (!TextUtils.isEmpty(feedback)) {
                    try {
                        write.println(feedback);
                        feedback = "";
                        write.flush();
                    } catch (Exception e) {
                        Log.d(TAG, "Exception:" + e.getMessage());
                    }
                }
                try {
                    sleep(Common.SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void feekback(int time){
        feedback = Message.feedback(time);
    }

    private void stop() {
        isCientRunning = false;
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void send() {
        message =Message.pack(msg.getText().toString()) ;
    }

    private void println(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                msglist.append(message + "\n");
            }
        });
    }
}
