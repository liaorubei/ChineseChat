package com.hanwen.chinesechat.util;

public class Log {

    private static boolean logOn = false;

    public static void i(String tag, String msg) {
        if (logOn) {
            android.util.Log.i("Fylx:" + tag, msg);
        }
    }

    public static void i(Object msg) {
        if (logOn) {
            android.util.Log.i("Fylx", "" + msg);
        }
    }

    public static void e(String tag, String s) {
        if (logOn) {
            android.util.Log.e(tag, s);
        }
    }
}
