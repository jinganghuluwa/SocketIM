package com.tzc.socket;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.tzc.socket.bean.User;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class ServerActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = "ServerActivity";
    private EditText port, msg;
    private TextView ip, msglist;

    private ServerManager mSocketManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.send).setOnClickListener(this);
        findViewById(R.id.stop).setOnClickListener(this);
        port = (EditText) findViewById(R.id.port);
        msg = (EditText) findViewById(R.id.message);
        ip = (TextView) findViewById(R.id.ip);
        msglist = (TextView) findViewById(R.id.msg_list);
        initData();
        mSocketManager = ServerManager.getInstance();
        mSocketManager.setSocketListener(mSocketListener);
    }

    private ServerListener mSocketListener = new ServerListener() {
        @Override
        public void onMessageReceive(String msg, User user) {
            println(msg);
        }

        @Override
        public void onMessageSendSuccess(int time) {
            println(time + "");
        }

        @Override
        public void onServerCreated(int port) {
            println("服务器启动成功 port:"+port);
        }

        @Override
        public void onUserJoin(User user) {

        }
    };

    private void initData() {
        ip.setText(getHostIP());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                start();
                break;
            case R.id.send:
                send();
                break;
            case R.id.stop:
                stop();
                break;
        }
    }

    public static String getHostIP() {
        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return hostIp;

    }


    private void start() {
        int portInt;
        try {
            portInt = Integer.parseInt(port.getText().toString());
        } catch (NumberFormatException e) {
            portInt = 1990;
        }
        try {
            mSocketManager.acceptServer(portInt);
        }catch (RuntimeException e){
            println(e.getMessage());
        }


    }


    private void println(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                msglist.append(message + "\n");
            }
        });
    }

    private void stop() {
    }

    private void send() {
        mSocketManager.sendAll(msg.getText().toString());
    }
}
