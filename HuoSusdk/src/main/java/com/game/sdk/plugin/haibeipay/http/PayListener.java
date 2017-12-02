package com.game.sdk.plugin.haibeipay.http;

/**
 * Created by Admin on 2017/7/19.
 */

public interface PayListener  {

    void onSucceed(String result);

    void onFailed(String error);
}
