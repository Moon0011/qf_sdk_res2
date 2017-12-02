package com.game.sdk.plugin.haibeipay.http;

import android.text.TextUtils;


/**
 * Log工具
 *      通过DEBUG的true或false来控制日志是否输出.
 */
public class LogUtil {

    private LogUtil() {
        // cannot be instantiated
    }

    private static Boolean DEBUG= true;

    public static void v(String tag, String msg) {
        if (DEBUG)
            android.util.Log.v(tag, msg);
    }

    public static void v(String tag, String msg, Throwable t) {
        if (DEBUG)
            android.util.Log.v(tag, msg, t);
    }

    public static void d(String tag, String msg) {
        if (DEBUG)
            android.util.Log.d(tag, msg);
    }

    public static void d(String tag, String msg, Throwable t) {
        if (DEBUG)
            android.util.Log.d(tag, msg, t);
    }

    public static void i(String tag, String msg) {
        if (DEBUG)
            android.util.Log.i(tag, msg);
    }

    public static void i(String tag, String msg, Throwable t) {
        if (DEBUG)
            android.util.Log.i(tag, msg, t);
    }

    public static void w(String tag, String msg) {
        if (DEBUG)
            android.util.Log.w(tag, msg);
    }

    public static void w(String tag, String msg, Throwable t) {
        if (DEBUG)
            android.util.Log.w(tag, msg, t);
    }

    public static void e(String tag, String msg) {
        if (DEBUG)
            android.util.Log.e(tag, msg);
    }

    public static void e(String tag, String msg, Throwable t) {
        if (DEBUG)
            android.util.Log.e(tag, msg, t);
    }

    public static void print(String str) {
        if (DEBUG) {
            if (TextUtils.isEmpty(str)) {
                System.out.println("null");
            } else {
                System.out.println(str);
            }
        }
    }
}
