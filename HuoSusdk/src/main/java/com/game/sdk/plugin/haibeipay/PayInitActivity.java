//package com.game.sdk.plugin.haibeipay;
//
//import android.annotation.SuppressLint;
//import android.content.Intent;
//import android.os.Bundle;
//
//import android.support.v7.app.AppCompatActivity;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.RadioGroup;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.game.sdk.R;
//import com.game.sdk.plugin.haibeipay.http.CallServer;
//import com.game.sdk.plugin.haibeipay.http.Constant;
//import com.game.sdk.plugin.haibeipay.http.HttpListener;
//import com.game.sdk.plugin.haibeipay.http.LogUtil;
//import com.game.sdk.plugin.haibeipay.http.Md5Util;
//import com.game.sdk.plugin.haibeipay.http.PayListener;
//import com.yolanda.nohttp.Logger;
//import com.yolanda.nohttp.NoHttp;
//import com.yolanda.nohttp.RequestMethod;
//import com.yolanda.nohttp.rest.Request;
//import com.yolanda.nohttp.rest.Response;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//
//
//public class PayInitActivity extends AppCompatActivity implements View.OnClickListener {
//
//    private String appid;   //海贝付应用id
//    private String timestamp; //请求时间戳
//    private String once; //随机字符串
//    private String method; // 请求方法名
//    private String sign; //签名字符串
//    private String versions; //接口版本号
//    private String data;//数据包体
//    private String format;//返回数据格式
//    private String pay_type ="wechat_h5";//默认支付类型
//
//    private PayListener payListener;
//    private TextView etAmt, etPayResult;
//    private Button btCommit;
//    private RadioGroup rgPayType;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_payinit);
//        findViewById();
//        initEvent();
//    }
//
//    private void findViewById() {
//        etAmt = (TextView) findViewById(R.id.etAmt);
//        etPayResult = (TextView) findViewById(R.id.paysuccess_txt);
//        btCommit = (Button) findViewById(R.id.btCommit);
//        btCommit.setOnClickListener(this);
//        rgPayType = (RadioGroup) findViewById(R.id.rg_pay_way);
//    }
//
//    private void initEvent() {
//        rgPayType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                switch (checkedId) {
//                    case R.id.rb_alipay:
//                        pay_type = "alipay_h5";
//                        break;
//                    case R.id.rb_wechat_pay:
//                        pay_type = "wechat_h5";
//                        break;
//                }
//            }
//        });
//    }
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.btCommit:
//                //微信H5页面地址需要商户下单到商户服务器获取，此处仅供参考
//                Logger.i("提交支付！！");
//                testingCurrentVersion();
//                payListener = new PayListener() {
//                    @Override
//                    public void onSucceed(String result) {
//                        //处理支付数据
//                        try {
//                            JSONObject js = new JSONObject(result);
//                            String data = js.getString("result");
//                            JSONObject jsa = new JSONObject(data);
//                            //获取支付连接
//                            String payurl = jsa.getString("payUrl");
//                            //获取海贝付平台订单号
//                            String orderSn = jsa.getString("orderSn");
//                            Intent intent = new Intent(PayInitActivity.this, PayInterfaceActivity.class);
//                            intent.putExtra("payUrl", payurl);
//                            intent.putExtra("orderSn",orderSn);
//                            startActivityForResult(intent, Constant.REQUESTCODE);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onFailed(String error) {
//                        Log.e("hb","error =" +error);
//                    }
//                };
//                break;
//        }
//    }
//
//    /**
//     * 获取现在时间
//     *
//     * @return返回字符串格式 yyyyMMddHHmmss
//     */
//    @SuppressLint("SimpleDateFormat")
//    public static String getStringDate() {
//        Date currentTime = new Date();
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
//        String dateString = formatter.format(currentTime);
//        return dateString;
//    }
//    //获取时间戳
//    public String getTime(){
//        long timestamp = System.currentTimeMillis();
//        String str= String.valueOf(timestamp);
//        return str;
//    }
//
//
//    private  void inits (){
//        appid = Constant.APP_ID;
//        timestamp =getTime();
//        once = getStringDate();
//        method = "sdk.web";
//        versions ="1.0.0";
//        data = jsons().toString();
//        format = "JSON";
//        initmd5s();
//    }
//
//    //生成请求包体：
//    private JSONObject jsons (){
//        JSONObject ob = new JSONObject();
//        try {
//            ob.put("waresName","测试");
//            ob.put("cpOrderId",getStringDate());
//            ob.put("price","0.01");
//            ob.put("returnUrl","http://www.hao123.com");
//            ob.put("notifyUrl","http://www.hao123.com");
//            ob.put("type",pay_type);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return ob;
//    }
//
//    //MD5加密     sign_type  MD5   RSA
//    private void initmd5s() {
//        StringBuilder sbSign = new StringBuilder();
//        sbSign.append("appid=" + appid);
//        sbSign.append("&attach=" + "001");
//        sbSign.append("&data=" + data);
//        sbSign.append("&format=" + format);
//        sbSign.append("&key="+Constant.MD5_KEY);
//        sbSign.append("&method=" + method);
//        sbSign.append("&once=" + once);
//        sbSign.append("&sign_type="+"MD5");
//        sbSign.append("&timestamp=" + timestamp);
//        sbSign.append("&version=" + versions);
//        System.out.println(sbSign.toString() + "加密前---------------------");
//        String initSign = Md5Util.md5(sbSign.toString()).toLowerCase();//md5加密
//        System.out.println(initSign+"-----------"+initSign.toUpperCase() + "---------------------");
//        sign = initSign.toUpperCase();
//    }
//
//    /**
//     * 获取海贝付平台支付连接
//     */
//    private void testingCurrentVersion() {
//        inits();
//        Request<String> request = NoHttp.createStringRequest(Constant.H5_PAYINIT_URL, RequestMethod.POST);
//        try {
//            request.add("appid",appid);
//            request.add("timestamp", timestamp);
//            request.add("once",once);
//            request.add("method", method);
//            request.add("version", versions);
//            request.add("data", data);
//            request.add("attach","001");
//            request.add("format", format);
//            request.add("sign", sign);
//            request.add("sign_type","MD5");
//            //LogUtil.i("--->reuslt", request.toString() + "---");
//            LogUtil.i("---->>result","");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        //开启进度条
//        CallServer.getRequestInstance().add(PayInitActivity.this, 2, request, httpListener, false, false);
//    }
//
//    private HttpListener<String> httpListener = new HttpListener<String>() {
//        @Override
//        public void onSucceed(int what, Response<String> response) {
//            Logger.i("返回参数==" + response.get().toString());
//            payListener.onSucceed(response.get().toString());
//        }
//        @Override
//        public void onFailed(int what, Response<String> response) {
//        }
//    };
//
//
//    /**
//     * 获取支付结果，此处为演示，商户可自行封装获取
//     *
//     * @param requestCode
//     * @param resultCode
//     * @param data
//     */
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == Constant.REQUESTCODE) {
//            if (data != null) {
//                int code = -2;
//                if (data.hasExtra("code")) {
//                    code = data.getIntExtra("code", -1);
//                }
//                if (code == 10000) {
//                    Toast.makeText(this, "支付成功", Toast.LENGTH_SHORT).show();
//                    etPayResult.setText("支付成功");
//                } else if (code == 999) {
//                    Toast.makeText(this, "支付失败", Toast.LENGTH_SHORT).show();
//                    etPayResult.setText("支付失败");
//                } else if (code == -1) {
//                    Toast.makeText(this, "支付未完成", Toast.LENGTH_SHORT).show();
//                    etPayResult.setText("支付未完成");
//                }
//            }
//        }
//    }
//}
