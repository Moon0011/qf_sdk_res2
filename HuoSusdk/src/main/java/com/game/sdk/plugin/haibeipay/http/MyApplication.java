package com.game.sdk.plugin.haibeipay.http;


import android.app.Application;

import com.yolanda.nohttp.Logger;
import com.yolanda.nohttp.NoHttp;


/**
 * Created by dr on 2016-9-1.
 */
public class MyApplication extends Application {

    private static MyApplication _instance;

    @Override
    public void onCreate() {
        super.onCreate();

        _instance = this;
        NoHttp.initialize(this);
        Logger.setTag("clock");
        Logger.setDebug(true);// 开始NoHttp的调试模式, 这样就能看到请求过程和日志
    }

    public static MyApplication getInstance() {
        return _instance;
    }
}
