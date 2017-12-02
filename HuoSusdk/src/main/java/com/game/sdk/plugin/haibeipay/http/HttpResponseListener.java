/*
 * Copyright © YOLANDA. All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.game.sdk.plugin.haibeipay.http;

import android.content.Context;
import android.content.DialogInterface;

import com.game.sdk.R;
import com.yolanda.nohttp.Logger;
import com.yolanda.nohttp.error.NetworkError;
import com.yolanda.nohttp.error.NotFoundCacheError;
import com.yolanda.nohttp.error.ParseError;
import com.yolanda.nohttp.error.TimeoutError;
import com.yolanda.nohttp.error.URLError;
import com.yolanda.nohttp.error.UnKnownHostError;
import com.yolanda.nohttp.rest.OnResponseListener;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;

import java.net.ProtocolException;



/**
 * Created in Nov 4, 2016 12:02:55 PM.
 *
 * @author dr;
 */
public class HttpResponseListener<T> implements OnResponseListener<T> {

    private Context mContext;

    /**
     * Dialog.
     */
    private WaitDialog mWaitDialog;

    private Request<?> mRequest;

    /**
     * 结果回调.
     */
    private HttpListener<T> callback;

    /**
     * 是否显示dialog.
     */
    private boolean isLoading;

    /**
     * @param context      context用来实例化dialog.
     * @param request      请求对象.
     * @param httpCallback 回调对象.
     * @param canCancel    是否允许用户取消请求.
     * @param isLoading    是否显示dialog.
     */
    public HttpResponseListener(Context context, Request<?> request, HttpListener<T> httpCallback, boolean canCancel, boolean isLoading) {
        this.mRequest = request;
        this.mContext = context;
        if (context != null && isLoading) {
            mWaitDialog = new WaitDialog(context);
            mWaitDialog.setCancelable(canCancel);
            mWaitDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mRequest.cancel();
                }
            });
        }
        this.callback = httpCallback;
        this.isLoading = isLoading;
    }

    /**
     * 开始请求, 这里显示一个dialog.
     */
    @Override
    public void onStart(int what) {
        if (isLoading && mWaitDialog != null && !mWaitDialog.isShowing())
            mWaitDialog.show();
    }

    /**
     * 结束请求, 这里关闭dialog.
     */
    @Override
    public void onFinish(int what) {
        if (isLoading && mWaitDialog != null && mWaitDialog.isShowing())
            mWaitDialog.dismiss();
    }

    /**
     * 成功回调.
     */
    @Override
    public void onSucceed(int what, Response<T> response) {
        if (callback != null)
            callback.onSucceed(what, response);
    }

    @Override
    public void onFailed(int what, Response<T> response) {
       Exception exception = response.getException();
        if (exception instanceof NetworkError) {// 网络不好
            Snackbar.show(mContext, R.string.error_please_check_network);
        } else if (exception instanceof TimeoutError) {// 请求超时
            Snackbar.show(mContext, R.string.error_timeout);
        } else if (exception instanceof UnKnownHostError) {// 找不到服务器
            Snackbar.show(mContext, R.string.error_not_found_server);
        } else if (exception instanceof URLError) {// URL是错的
            Snackbar.show(mContext, R.string.error_url_error);
        } else if (exception instanceof NotFoundCacheError) {
            // 这个异常只会在仅仅查找缓存时没有找到缓存时返回
            Snackbar.show(mContext, R.string.error_not_found_cache);
        } else if (exception instanceof ProtocolException) {
            Snackbar.show(mContext, R.string.error_system_unsupport_method);
        } else if (exception instanceof ParseError) {
            Snackbar.show(mContext, R.string.error_parse_data_error);
        } else {
            Snackbar.show(mContext, R.string.error_unknow);
        }
        Logger.e("错误：" + exception.getMessage());
        if (callback != null)
            callback.onFailed(what, response);
    }
}
