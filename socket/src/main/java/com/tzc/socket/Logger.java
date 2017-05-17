package com.tzc.socket;

import android.util.Log;

/**
 * Created by tongzhichao on 17-5-11.
 */

public class Logger {

    private static final boolean DEBUG = true;
    private static final String TAG = "SocketIM";

    public static void d(String tag, String msg) {
        if (DEBUG){
            Log.d(TAG, tag + " --- " + msg);
        }
    }

    public static void e(String tag, String msg){
        Log.e(TAG, tag + " --- " + msg);
    }
}
