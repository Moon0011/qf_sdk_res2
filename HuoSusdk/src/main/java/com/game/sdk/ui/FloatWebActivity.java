package com.game.sdk.ui;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.DownloadListener;
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
import com.game.sdk.HuosdkService;
import com.game.sdk.domain.WebLoadAssert;
import com.game.sdk.http.SdkApi;
import com.game.sdk.log.L;
import com.game.sdk.log.T;
import com.game.sdk.pay.CommonJsForWeb;
import com.game.sdk.pay.IPayListener;
import com.game.sdk.util.BaseAppUtil;
import com.game.sdk.util.DialogUtil;
import com.game.sdk.util.MResource;
import com.game.sdk.util.WebLoadByAssertUtil;

import java.io.IOException;
import java.util.List;


public class FloatWebActivity extends BaseActivity implements OnClickListener, IPayListener {
	private static final String TAG = "FloatWebActivity";
	private final static String INDEX_URL_FLAG="User/index";
	private WebView wv;
	private TextView tv_back, tv_charge_title;
	private ImageView iv_cancel;
	private String url, title;
	private StringBuffer postDate = new StringBuffer("");


	private ImageView iv_return;
	private CommonJsForWeb commonJsForWeb;
	List<WebLoadAssert> webLoadAssertList = WebLoadByAssertUtil.getWebLoadAssertList();
	private String authKey;//对称解密用的authKey
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		HuosdkInnerManager.getInstance().removeFloatView();
		setContentView(MResource.getIdByName(getApplication(), "layout", "huo_sdk_activity_float_web"));
		Intent intent = getIntent();
		url = intent.getStringExtra("url");
		title = intent.getStringExtra("title");
		String urlParams = getIntent().getStringExtra("urlParams");
		if(urlParams.startsWith("?")){
			urlParams=urlParams.substring(1);
		}
		L.e("WebPayActivity","url="+ SdkApi.getWebSdkPay());
		authKey = getIntent().getStringExtra("authKey");
		wv = (WebView) findViewById(MResource.getIdByName(getApplication(),
				"R.id.huo_sdk_wv_content"));
		tv_back = (TextView) findViewById(MResource.getIdByName(
				getApplication(),"R.id.huo_sdk_tv_back"));
		iv_return = (ImageView) findViewById(MResource.getIdByName(
				getApplication(),  "R.id.huo_sdk_iv_return"));
		iv_cancel = (ImageView) findViewById(MResource.getIdByName(
				getApplication(),  "R.id.huo_sdk_iv_cancel"));
		setTitleView(findViewById(MResource.getIdByName(getApplication(),  "R.id.huo_sdk_rl_top")));
		tv_charge_title = (TextView) findViewById(MResource.getIdByName(
				getApplication(),  "R.id.huo_sdk_tv_charge_title"));
		tv_charge_title.setText(title);
		tv_back.setOnClickListener(this);
		iv_cancel.setOnClickListener(this);
		iv_return.setOnClickListener(this);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.getSettings().setLoadsImagesAutomatically(true);
		wv.getSettings().setAppCacheEnabled(false);
		wv.getSettings().setDomStorageEnabled(true);
		wv.getSettings().setDefaultTextEncodingName("UTF-8");
		//浮点里面的是没有回调的
		commonJsForWeb = new CommonJsForWeb(this,authKey,this);
		wv.addJavascriptInterface(commonJsForWeb,"huosdk");
		wv.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
		wv.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		wv.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				if (!DialogUtil.isShowing()) {
					DialogUtil.showDialog(FloatWebActivity.this, "正在加载...");
				}
				L.e("testWebview onPageStarted","url="+url);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				L.e(TAG,"url="+url);
				//返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
				if(url.startsWith("http")||url.startsWith("https")||url.startsWith("ftp")){
					return false;
				}else{
					try{
						Intent intent = new Intent();
						intent.setAction(Intent.ACTION_VIEW);
						intent.setData(Uri.parse(url));
						view.getContext().startActivity(intent);
					}catch (ActivityNotFoundException e){
						Toast.makeText(view.getContext(), "手机还没有安装支持打开此网页的应用！",Toast.LENGTH_SHORT).show();
					}
					return true;
				}
			}
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				try {
					tv_charge_title.setText(view.getTitle());
					webviewCompat(wv);
					DialogUtil.dismissDialog();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			@Override
			public void onFormResubmission(WebView view, Message dontResend, Message resend) {
				super.onFormResubmission(view, dontResend, resend);
				resend.sendToTarget();
			}
			@Override
			public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
				WebResourceResponse response;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){

					for(WebLoadAssert webLoadAssert:webLoadAssertList){
						if (url.contains(webLoadAssert.getName())){
							try {
								response = new WebResourceResponse(webLoadAssert.getMimeType(),"UTF-8",getAssets().open(webLoadAssert.getName()));
								L.e("hongliang","加载了："+webLoadAssert.getName());
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

				for(WebLoadAssert webLoadAssert:webLoadAssertList){
					if (request.getUrl().getPath().contains(webLoadAssert.getName())){
						try {
							response = new WebResourceResponse(webLoadAssert.getMimeType(),"UTF-8",getAssets().open(webLoadAssert.getName()));
							L.e("hongliang","加载了："+webLoadAssert.getName());
							return response;
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				return super.shouldInterceptRequest(view, request);
			}
		});
		wv.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
				if(!TextUtils.isEmpty(title)){
					tv_charge_title.setText(title);
				}else{
					if(TextUtils.isEmpty(FloatWebActivity.this.title)){
						tv_charge_title.setText("火速sdk");
					}else{
						tv_charge_title.setText(FloatWebActivity.this.title);
					}
				}
			}
		});
		wv.setDownloadListener(new DownloadListener() {
			@Override
			public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
				try{
					T.s(FloatWebActivity.this,"已开始下载，可在通知栏查看进度");
					Intent intent = new Intent(FloatWebActivity.this, HuosdkService.class);
					intent.putExtra(HuosdkService.DOWNLOAD_APK_URL,url);
					FloatWebActivity.this.startService(intent);
				}catch (Exception e){
					Toast.makeText(FloatWebActivity.this, "手机还没有安装支持打开此网页的应用！",Toast.LENGTH_SHORT).show();
				}
			}
		});
		webviewCompat(wv);
		wv.postUrl(url, urlParams.getBytes());
		L.e(TAG,"index_url="+url);
	}
//	public String getUrl(String url) {
//		url = url + "?user_token=" + HuosdkService.userinfo.user_token
//				+ "&param_token="
//				+ Util.getTokenForFloat(HuosdkService.userinfo.user_token);
//		return url;
//	}

	@Override
	public void onClick(View v) {
		if (v.getId() == tv_back.getId()||v.getId()==iv_return.getId()) {
			String url = wv.getUrl();
			L.e(TAG,"当前页面的url="+url);
			if(url!=null&&(url.toLowerCase().contains(INDEX_URL_FLAG.toLowerCase()))){//是用户中心页面，直接finish
				finish();
			}else{//其它则直接返回用户中心
				if(wv.canGoBack()){
					wv.goBack();
				}else{
					finish();
				}
			}
		}
		if (v.getId() == iv_cancel.getId()) {
			this.finish();
		}
	}
	/**
	 * 一些版本特性操作，需要适配、
	 *
	 * @date 6/3
	 * @param mWebView webview
	 * @reason 在微蓝项目的时候遇到了 返回键 之后 wv显示错误信息
	 * */
	private void webviewCompat(WebView mWebView) {
		try {
			if (BaseAppUtil.isNetWorkConneted(mWebView.getContext())) {
                mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
            } else {
                mWebView.getSettings().setCacheMode(
                        WebSettings.LOAD_CACHE_ELSE_NETWORK);
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && wv.canGoBack()) {
			wv.goBack();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(commonJsForWeb !=null){
			commonJsForWeb.onDestory();
		}
		if (wv!=null){
			wv.removeAllViews();
			wv = null;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		overridePendingTransition(0,0);
	}
	@Override
	protected void onResume() {
		super.onResume();
		if (commonJsForWeb != null) {
			commonJsForWeb.onResume();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(commonJsForWeb !=null){
			commonJsForWeb.onActivityResult(requestCode,resultCode,data);
		}
	}

	@Override
	public void paySuccess(String orderId, float money) {
		Toast.makeText(this,"支付成功",Toast.LENGTH_SHORT);
		if(wv!=null){
			wv.reload();
		}
	}

	@Override
	public void payFail(String orderId, float money, boolean queryOrder, String msg) {
		if(TextUtils.isEmpty(msg)){
			Toast.makeText(this,"支付失败",Toast.LENGTH_SHORT);
		}else{
			Toast.makeText(this,msg,Toast.LENGTH_SHORT);
		}
		if(wv!=null){
			wv.reload();
		}
	}
	public static void start(Context context,String url,String title,String urlParams, String authKey) {
		Intent starter = new Intent(context, FloatWebActivity.class);
		starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_NO_ANIMATION);
		starter.putExtra("url",url);
		starter.putExtra("title",title);
		starter.putExtra("urlParams",urlParams);
		starter.putExtra("authKey",authKey);
		context.startActivity(starter);
	}
}
