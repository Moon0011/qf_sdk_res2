package com.game.sdk.plugin.haibeipay;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.game.sdk.plugin.haibeipay.http.CallServer;
import com.game.sdk.plugin.haibeipay.http.Constant;
import com.game.sdk.plugin.haibeipay.http.HttpListener;
import com.game.sdk.plugin.haibeipay.http.Md5Util;
import com.yolanda.nohttp.Logger;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class PayInterfaceActivity extends Activity {
    private WebView mWebView;
    private String payUrl;//海贝付支付连接
    private String orderSn;//海贝付平台订单号
    protected int mFlag = 0;
    private Timer mTimer;
    public ProgressDialog mProgressDialog;
    private H5Handler mH5Handler;

    private class H5Handler extends Handler {
        private WeakReference<PayInterfaceActivity> mActivitys;

        public H5Handler(PayInterfaceActivity activity) {
            mActivitys = new WeakReference<PayInterfaceActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            PayInterfaceActivity activity = mActivitys.get();
            if (activity != null) {
                activity.hideProgressDialog();
                int str =  msg.arg1;
                Logger.i("回传支付状态码："+str);
                setPayResult(str);
            }
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("payUrl")) {
                payUrl = intent.getStringExtra("payUrl");
            }
            if (intent.hasExtra("orderSn")){
                orderSn = intent.getStringExtra("orderSn");
            }
        }
        //此处商户可根据需要自行处理跳转中间页
        setView();
        if (!TextUtils.isEmpty(payUrl)) {
            mProgressDialog = showProgressDialog(this, "", "正在启动...");
            mWebView.loadUrl(payUrl);
        } else {
            finish();
        }
    }


    private void setView() {
        mH5Handler = new H5Handler(this);
        mWebView = new WebView(this);
        mWebView.setVisibility(View.GONE);
        setContentView(mWebView);
        Logger.d("---->中间遮盖");
        setWebViewClient();
        setWebViewProperty();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.mFlag += 1;
        if (mFlag % 2 != 0) {
            if (mTimer == null) {
                mTimer = new Timer();
            }
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideProgressDialog();
                            mWebView.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }, 10000);
            return;
        }
       // 再次进入支付页面时，进行订单查询，供仅参考，查询需要商户到商户服务器去查询
        testingCurrentVersion();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }
    /**
     * 获取现在时间
     *
     * @return返回字符串格式 yyyyMMddHHmmss
     */
    @SuppressLint("SimpleDateFormat")
    public static String getStringDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }
    //获取时间戳
    public String getTime(){
        long timestamp = System.currentTimeMillis();
        String str= String.valueOf(timestamp);
        return str;
    }


    //请求haibeifu
    private String appid;   //应用id
    private String timestamp; //请求时间错
    private String once; //随机字符串
    private String method; // 请求方法名
    private String sign; //加密文件
    private String versions; //接口版本号
    private String data;//数据包体
    private String format;

    private  void inits (){
        appid = Constant.APP_ID;
        timestamp =getTime();
        once = getStringDate();
        method = "order.query";
        versions ="1.0.0";
        data = jsons().toString();
        format = "JSON";
        initmd5s();

    }


    //生成请求包体：
    private JSONObject jsons (){
        JSONObject ob = new JSONObject();
        try {
            ob.put("orderSn",orderSn);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ob;
    }
    //MD5加密
    private void initmd5s() {
        StringBuilder sbSign = new StringBuilder();
        sbSign.append("appid=" + appid);
        sbSign.append("&attach=" + "001");
        sbSign.append("&data=" + data);
        sbSign.append("&format=" + format);
        sbSign.append("&key="+Constant.MD5_KEY);
        sbSign.append("&method=" + method);
        sbSign.append("&once=" + once);
        sbSign.append("&sign_type="+"MD5");
        sbSign.append("&timestamp=" + timestamp);
        sbSign.append("&version=" + versions);

        System.out.println(sbSign.toString() + "---------------------");
        String initSign = Md5Util.md5(sbSign.toString()).toLowerCase();//md5加密
        System.out.println(initSign+"-----------"+initSign.toUpperCase() + "---------------------");
        sign = initSign.toUpperCase();
    }

    /**
     * 检测当前版本是否为new
     * 获取支付连接
     */
    private void testingCurrentVersion() {
        inits();
        Request<String> request = NoHttp.createStringRequest(Constant.H5_PAYINIT_URL, RequestMethod.POST);
        try {
            request.add("appid",appid);
            request.add("timestamp", timestamp);
            request.add("once",once);
            request.add("method", method);
            request.add("version", versions);
            request.add("data", data);
            request.add("attach","001");
            request.add("format", format);
            request.add("sign", sign);
            request.add("sign_type","MD5");
            //LogUtil.i("--->reuslt", request.toString() + "---");
        } catch (Exception e) {
            e.printStackTrace();
        }

        CallServer.getRequestInstance().add(PayInterfaceActivity.this, 2, request, httpListener, false, false);
    }

    private HttpListener<String> httpListener = new HttpListener<String>() {
        @Override
        public void onSucceed(int what, Response<String> response) {
            Logger.i("返回参数==" + response.get().toString());
            try {
                JSONObject js  = new JSONObject(response.get().toString());
                String result = js.getString("result");
                Logger.i(result);
                JSONObject jsa = new JSONObject(result);
                //获取订单支付状态
                String pay_state = jsa.getString("pay_state");
                Message msg = Message.obtain();
                msg.arg1 = Integer.parseInt(pay_state);
                mH5Handler.sendMessage(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailed(int what, Response<String> response) {
        }
    };


    //将支付结果返回给上一个页面，仅供参考
    private void setPayResult(int code) {
        Intent intent = new Intent();
        intent.putExtra("code", code);
        setResult(Constant.RESULTCODE, intent);
        finish();
    }

    //配置webview
    protected void setWebViewClient() {
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("weixin:") || url.startsWith("alipayqr:") || url.startsWith("alipays:")) {
                    try {
                       Logger.d("启动微信客户端");
                        hideProgressDialog();
                        startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)));
                    } catch (ActivityNotFoundException localActivityNotFoundException) {
                        Toast.makeText(PayInterfaceActivity.this, "请检查是否安装客户端", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    return true;
                } else {
                   Logger.d("http");
                    return super.shouldOverrideUrlLoading(view, url);
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
              //  LogUtil.print("-------------errorCode1--------------"
                //        + errorCode);
            }

            @TargetApi(23)
            @Override
            public void onReceivedHttpError(WebView view,
                                            WebResourceRequest request,
                                            WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
              //  LogUtil.print("-------------errorCode2--------------"
                //        + errorResponse.getStatusCode());
            }
        });
    }

    //配置webview
    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    protected void setWebViewProperty() {
        WebSettings settings = mWebView.getSettings();
        // 支持JavaScript
        settings.setJavaScriptEnabled(true);
        // 支持通过js打开新的窗口
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setDomStorageEnabled(true);
    }

    protected ProgressDialog showProgressDialog(Context context, String title, String message) {
        mProgressDialog = ProgressDialog.show(context, title, message, false, true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setOnCancelListener(mCanListener);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        return mProgressDialog;
    }

    /**
     * Dialog关闭监听
     */
    protected DialogInterface.OnCancelListener mCanListener = new DialogInterface.OnCancelListener() {
        public void onCancel(DialogInterface dlg) {
            dlg.dismiss();
        }
    };

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
