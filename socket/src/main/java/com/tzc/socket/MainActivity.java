package com.tzc.socket;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.server).setOnClickListener(this);
        findViewById(R.id.client).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.server:
                startServer();
                break;
            case R.id.client:
                startClient();
                break;
        }
    }

    private void startServer(){
        startActivity(new Intent(this,ServerActivity.class));
    }

    private void startClient(){
        startActivity(new Intent(this,ClientActivity.class));
    }
}
