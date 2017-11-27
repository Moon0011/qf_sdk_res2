package com.game.sdk.ui;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.game.sdk.HuosdkInnerManager;
import com.game.sdk.domain.PaymentCallbackInfo;
import com.game.sdk.domain.PaymentErrorMsg;
import com.game.sdk.domain.QueryOrderRequestBean;
import com.game.sdk.domain.QueryOrderResultBean;
import com.game.sdk.domain.WebLoadAssert;
import com.game.sdk.http.HttpCallbackDecode;
import com.game.sdk.http.HttpParamsBuild;
import com.game.sdk.http.SdkApi;
import com.game.sdk.listener.OnPaymentListener;
import com.game.sdk.log.L;
import com.game.sdk.pay.CommonJsForWeb;
import com.game.sdk.pay.IPayListener;
import com.game.sdk.util.DialogUtil;
import com.game.sdk.util.GsonUtil;
import com.game.sdk.util.MResource;
import com.game.sdk.util.WebLoadByAssertUtil;
import com.kymjs.rxvolley.RxVolley;

import java.io.IOException;
import java.util.List;

public class WebPayActivity extends BaseActivity implements View.OnClickListener, IPayListener {
    private final static int CODE_PAY_FAIL = -1;//支付失败
    private final static int CODE_PAY_CANCEL = -2;//用户取消支付
    private WebView payWebview;
    private static float charge_money;
    private CommonJsForWeb checkPayJsForPay;
    private TextView tv_back;
    private ImageView iv_cancel;
    private TextView tv_charge_title;
    private ImageView iv_return;
    List<WebLoadAssert> webLoadAssertList = WebLoadByAssertUtil.getWebLoadAssertList();
    private String authKey;//对称解密用的authKey
    private boolean callBacked = false;//是否回调过了
    private String urlParams;
    int requestCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(MResource.getIdByName(this, "R.layout.huo_sdk_activity_web_pay"));
        setupUI();
    }



    @Override
    public void changeTitleStatus(boolean show) {
        super.changeTitleStatus(show);
    }

    private void setupUI() {
        HuosdkInnerManager huosdkInnerManager = HuosdkInnerManager.getInstance();
        huosdkInnerManager.removeFloatView();
        payWebview = (WebView) findViewById(MResource.getIdByName(this, "R.id.huo_sdk_pay_webview"));
        initWebView(payWebview);
        urlParams = getIntent().getStringExtra("urlParams");
        authKey = getIntent().getStringExtra("authKey");
        charge_money = getIntent().getFloatExtra("product_price", 0.00f);
        if (urlParams.startsWith("?")) {
            urlParams = urlParams.substring(1);
        }
        L.e("WebPayActivity", "url=" + SdkApi.getWebSdkPay());
        String setProduct_name = getIntent().getStringExtra("setProduct_name");
        payWebview.postUrl(SdkApi.getWebSdkPay(), urlParams.getBytes());
        checkPayJsForPay = new CommonJsForWeb(this, authKey, this);
        checkPayJsForPay.setChargeMoney(charge_money);//设置支付金额
        checkPayJsForPay.setProduct_name(setProduct_name);
        payWebview.addJavascriptInterface(checkPayJsForPay, "huosdk");
        tv_back = (TextView) findViewById(MResource.getIdByName(
                getApplication(), "R.id.huo_sdk_tv_back"));
        iv_return = (ImageView) findViewById(MResource.getIdByName(
                getApplication(), "R.id.huo_sdk_iv_return"));
        iv_cancel = (ImageView) findViewById(MResource.getIdByName(
                getApplication(), "R.id.huo_sdk_iv_cancel"));
        //设置标题栏view
        setTitleView(findViewById(MResource.getIdByName(getApplication(), "R.id.huo_sdk_rl_top")));
        tv_charge_title = (TextView) findViewById(MResource.getIdByName(
                getApplication(), "R.id.huo_sdk_tv_charge_title"));
        tv_charge_title.setText("充值中心");
        tv_back.setOnClickListener(this);
        iv_cancel.setOnClickListener(this);
        iv_return.setOnClickListener(this);
    }

    private void initWebView(WebView webView) {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (!DialogUtil.isShowing()) {
                    DialogUtil.showDialog(WebPayActivity.this, "正在加载...");
                }
                if(SdkApi.getWebSdkPay().equals(url)){
                    requestCount++;
                    if(requestCount>1){
                        finish();
                    }
                    L.e("testWebview onPageStarted", "url=" + url+"  count="+requestCount);
                }
                L.e("testWebview onPageStarted", "url=" + url);
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                L.e("WebPayActivity1", "url=" + url);
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                if (url.startsWith("http") || url.startsWith("https") || url.startsWith("ftp")) {
                    return false;
                } else {
                    try {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        view.getContext().startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(view.getContext(), "手机还没有安装支持打开此网页的应用！", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                try {
                    DialogUtil.dismissDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                WebResourceResponse response;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

                    for (WebLoadAssert webLoadAssert : webLoadAssertList) {
                        if (url.contains(webLoadAssert.getName())) {
                            try {
                                response = new WebResourceResponse(webLoadAssert.getMimeType(), "UTF-8", getAssets().open(webLoadAssert.getName()));
                                L.e("hongliangsdk", "加载了：" + webLoadAssert.getName());
                                return response;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                return super.shouldInterceptRequest(view, url);
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                WebResourceResponse response;
                for (WebLoadAssert webLoadAssert : webLoadAssertList) {
                    if (request.getUrl().getPath().contains(webLoadAssert.getName())) {
                        try {
                            response = new WebResourceResponse(webLoadAssert.getMimeType(), "UTF-8", getAssets().open(webLoadAssert.getName()));
                            L.e("hongliangsdk", "加载了：" + webLoadAssert.getName());
                            return response;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.setWebChromeClient(new WebChromeClient());
        //设置缓存模式，默认是缓存静态资源
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && payWebview.canGoBack()) {
            payWebview.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            payWebview.goBack();// 返回前一个页面
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * 微付通支付结果回调
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (checkPayJsForPay != null) {
            checkPayJsForPay.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (checkPayJsForPay != null) {
            checkPayJsForPay.onResume();
        }
    }
    /**
     * 关闭的时候，将支付信息回调回去
     */
    @Override
    protected void onDestroy() {
        if (payWebview != null) {
            payWebview.destroy();
        }
        if (checkPayJsForPay != null) {
            checkPayJsForPay.onDestory();
        }
        if (!callBacked) {//还没有回调过
            PaymentErrorMsg paymentErrorMsg = new PaymentErrorMsg();
            paymentErrorMsg.code = CODE_PAY_CANCEL;
            paymentErrorMsg.msg = "用户取消支付";
            paymentErrorMsg.money = charge_money;
            OnPaymentListener paymentListener = HuosdkInnerManager.getInstance().getPaymentListener();
            if (paymentListener != null) {
                paymentListener.paymentError(paymentErrorMsg);
            }
        }
        L.i("hongliangsdk", "执行销毁");
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == tv_back.getId() || v.getId() == iv_return.getId()) {
//            if (payWebview.canGoBack()) {
//                // 返回的时候web端 出现异常，可能需要改成退出界面 @author ling
//                payWebview.goBack();// 返回前一个页面
//            } else {
//                finish();
//            }
            finish();
        }
        if (v.getId() == iv_cancel.getId()) {
            this.finish();
        }
    }

    @Override
    public void paySuccess(String orderId, final float money) {
        queryOrder(orderId, money, "支付成功，等待处理");
    }

    @Override
    public void payFail(String orderId, float money, boolean queryOrder, String msg) {
        if (queryOrder) {
            queryOrder(orderId, money, msg);
        } else {
            OnPaymentListener paymentListener = HuosdkInnerManager.getInstance().getPaymentListener();
            if (paymentListener != null) {
                PaymentErrorMsg paymentErrorMsg = new PaymentErrorMsg();
                paymentErrorMsg.code = CODE_PAY_FAIL;
                paymentErrorMsg.msg = msg;
                paymentErrorMsg.money = money;
                paymentListener.paymentError(paymentErrorMsg);
            }
            callBacked = true;
            finish();
        }
    }

    /**
     * 向服务器查询支付结果
     */
    private void queryOrder(String orderId, final float money, final String msg) {
        //向服务器查询订单结果
        QueryOrderRequestBean queryOrderRequestBean = new QueryOrderRequestBean();
        queryOrderRequestBean.setOrder_id(orderId);
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(queryOrderRequestBean));
        HttpCallbackDecode httpCallbackDecode = new HttpCallbackDecode<QueryOrderResultBean>(this, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(QueryOrderResultBean data) {
                OnPaymentListener paymentListener = HuosdkInnerManager.getInstance().getPaymentListener();
                if (paymentListener != null) {
                    if (data != null) {
                        if ("2".equals(data.getStatus())) {
                            if ("2".equals(data.getCpstatus())) {
                                PaymentCallbackInfo paymentCallbackInfo = new PaymentCallbackInfo("支付成功", money);
                                paymentListener.paymentSuccess(paymentCallbackInfo);
                            } else {
                                PaymentCallbackInfo paymentCallbackInfo = new PaymentCallbackInfo("支付成功，等待处理", money);
                                paymentListener.paymentSuccess(paymentCallbackInfo);
                            }
                        } else {
                            PaymentErrorMsg paymentErrorMsg = new PaymentErrorMsg(CODE_PAY_FAIL, msg, money);
                            paymentListener.paymentError(paymentErrorMsg);
                        }
                    } else {
                        PaymentErrorMsg paymentErrorMsg = new PaymentErrorMsg(CODE_PAY_FAIL, msg, money);
                        paymentListener.paymentError(paymentErrorMsg);
                    }
                }
                callBacked = true;
                finish();
            }

            @Override
            public void onFailure(String code, String msg) {
                super.onFailure(code, msg);
                OnPaymentListener paymentListener = HuosdkInnerManager.getInstance().getPaymentListener();
                if (paymentListener != null) {
                    PaymentErrorMsg paymentErrorMsg = new PaymentErrorMsg(CODE_PAY_FAIL, msg, money);
                    paymentListener.paymentError(paymentErrorMsg);
                }
                callBacked = true;
                finish();
            }
        };
        httpCallbackDecode.setShowTs(true);
        httpCallbackDecode.setLoadingCancel(false);
        httpCallbackDecode.setShowLoading(true);
        httpCallbackDecode.setLoadMsg("查询支付结果中……");
        RxVolley.post(SdkApi.getQueryorder(), httpParamsBuild.getHttpParams(), httpCallbackDecode);
    }

    public static void start(Context context, String urlParams, Float product_price, String product_name, String authKey) {
        Intent starter = new Intent(context, WebPayActivity.class);
        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        starter.putExtra("urlParams", urlParams);
        starter.putExtra("authKey", authKey);
        starter.putExtra("product_name", product_name);
        starter.putExtra("product_price", product_price);
        context.startActivity(starter);
    }
}
