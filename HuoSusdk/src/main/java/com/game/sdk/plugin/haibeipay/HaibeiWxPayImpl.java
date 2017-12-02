package com.game.sdk.plugin.haibeipay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.RadioGroup;

import com.game.sdk.R;
import com.game.sdk.domain.PayResultBean;
import com.game.sdk.pay.IPayListener;
import com.game.sdk.plugin.IHuoPay;
import com.game.sdk.plugin.haibeipay.http.CallServer;
import com.game.sdk.plugin.haibeipay.http.Constant;
import com.game.sdk.plugin.haibeipay.http.HttpListener;
import com.game.sdk.plugin.haibeipay.http.LogUtil;
import com.game.sdk.plugin.haibeipay.http.Md5Util;
import com.game.sdk.plugin.haibeipay.http.PayListener;
import com.yolanda.nohttp.Logger;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2017/11/29.
 */

public class HaibeiWxPayImpl extends IHuoPay {
    private String appid;   //海贝付应用id
    private String timestamp; //请求时间戳
    private String once; //随机字符串
    private String method; // 请求方法名
    private String sign; //签名字符串
    private String versions; //接口版本号
    private String data;//数据包体
    private String format;//返回数据格式
    private String pay_type = "wechat_h5";//默认支付类型
    private PayListener payListener;

    //获取海贝付平台支付连接
    @Override
    public void startPay(final Activity activity, final IPayListener listener, final float money, final PayResultBean payResultBean) {
        initParams(money, payResultBean);
        Request<String> request = NoHttp.createStringRequest(Constant.H5_PAYINIT_URL, RequestMethod.POST);
        try {
            request.add("appid", appid);
            request.add("timestamp", timestamp);
            request.add("once", once);
            request.add("method", method);
            request.add("version", versions);
            request.add("data", data);
            request.add("attach", "001");
            request.add("format", format);
            request.add("sign", sign);
            request.add("sign_type", "MD5");
            //LogUtil.i("--->reuslt", request.toString() + "---");
            LogUtil.i("---->>result", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //开启进度条
        CallServer.getRequestInstance().add(activity, 2, request, httpListener, false, false);
        payListener = new PayListener() {
            @Override
            public void onSucceed(String result) {
                listener.paySuccess(payResultBean.getOrder_id(),money);
                //处理支付数据
                try {
                    JSONObject js = new JSONObject(result);
                    String data = js.getString("result");
                    JSONObject jsa = new JSONObject(data);
                    //获取支付连接
                    String payurl = jsa.getString("payUrl");
                    //获取海贝付平台订单号
                    String orderSn = jsa.getString("orderSn");
                    Intent intent = new Intent(activity, PayInterfaceActivity.class);
                    intent.putExtra("payUrl", payurl);
                    intent.putExtra("orderSn", orderSn);
                    activity.startActivityForResult(intent, Constant.REQUESTCODE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(String error) {
                Log.e("hb", "error =" + error);
                listener.payFail(payResultBean.getOrder_id(), money, false, "支付失败");
            }
        };
    }

    private void initParams(float money, PayResultBean payResultBean) {
        JSONObject ob = new JSONObject();
        try {
            ob.put("waresName", "测试");
            ob.put("cpOrderId", payResultBean.getOrder_id());
            ob.put("price", money);
            ob.put("returnUrl", "http://www.hao123.com");
            ob.put("notifyUrl", "http://www.hao123.com");
            ob.put("type", pay_type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        data = ob.toString();
        appid = Constant.APP_ID;
        timestamp = getTime();
        once = getStringDate();
        method = "sdk.web";
        versions = "1.0.0";
        format = "JSON";
        initmd5s();
    }

    //获取时间戳
    public String getTime() {
        long timestamp = System.currentTimeMillis();
        String str = String.valueOf(timestamp);
        return str;
    }

    /**
     * 获取现在时间
     */
    @SuppressLint("SimpleDateFormat")
    public static String getStringDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    //MD5加密     sign_type  MD5   RSA
    private void initmd5s() {
        StringBuilder sbSign = new StringBuilder();
        sbSign.append("appid=" + appid);
        sbSign.append("&attach=" + "001");
        sbSign.append("&data=" + data);
        sbSign.append("&format=" + format);
        sbSign.append("&key=" + Constant.MD5_KEY);
        sbSign.append("&method=" + method);
        sbSign.append("&once=" + once);
        sbSign.append("&sign_type=" + "MD5");
        sbSign.append("&timestamp=" + timestamp);
        sbSign.append("&version=" + versions);
        System.out.println(sbSign.toString() + "加密前---------------------");
        String initSign = Md5Util.md5(sbSign.toString()).toLowerCase();//md5加密
        System.out.println(initSign + "-----------" + initSign.toUpperCase() + "---------------------");
        sign = initSign.toUpperCase();
    }

    private HttpListener<String> httpListener = new HttpListener<String>() {
        @Override
        public void onSucceed(int what, Response<String> response) {
            Logger.i("返回参数==" + response.get().toString());
            payListener.onSucceed(response.get().toString());
        }

        @Override
        public void onFailed(int what, Response<String> response) {
        }
    };
}
