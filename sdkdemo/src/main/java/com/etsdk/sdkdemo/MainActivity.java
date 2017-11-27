package com.etsdk.sdkdemo;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.game.sdk.HuosdkManager;
import com.game.sdk.domain.CustomPayParam;
import com.game.sdk.domain.LoginErrorMsg;
import com.game.sdk.domain.LogincallBack;
import com.game.sdk.domain.PaymentCallbackInfo;
import com.game.sdk.domain.PaymentErrorMsg;
import com.game.sdk.domain.RoleInfo;
import com.game.sdk.domain.SubmitRoleInfoCallBack;
import com.game.sdk.listener.OnInitSdkListener;
import com.game.sdk.listener.OnLoginListener;
import com.game.sdk.listener.OnLogoutListener;
import com.game.sdk.listener.OnPaymentListener;
import com.game.sdk.log.T;


public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    Button btnTestLogin;
    EditText etTestMoney;
    Button btnTestCharger;
    Button btnTestSendRoleinfo;
    HuosdkManager sdkManager;
    private Button btn_test_logout;
    private Button btn_test_switchAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI();
    }

    private void setupUI() {
        btnTestLogin = (Button) findViewById(R.id.btn_test_login);
        etTestMoney = (EditText) findViewById(R.id.et_test_money);
        btnTestCharger = (Button) findViewById(R.id.btn_test_charger);
        btnTestSendRoleinfo = (Button) findViewById(R.id.btn_test_sendRoleinfo);
        btn_test_logout = (Button) findViewById(R.id.btn_test_logout);
        btn_test_switchAccount = (Button) findViewById(R.id.btn_test_switchAccount);
        btnTestCharger.setOnClickListener(this);
        btnTestLogin.setOnClickListener(this);
        btnTestSendRoleinfo.setOnClickListener(this);
        btn_test_logout.setOnClickListener(this);
        btn_test_switchAccount.setOnClickListener(this);
        //获得sdk单例
        sdkManager = HuosdkManager.getInstance();
        //设置是否使用直接登陆,true为使用：第一次调用登陆时自动生成一个账号登陆
        sdkManager.setDirectLogin(false);
        sdkManager.setFloatInitXY(500, 200);
        //sdk初始化
        sdkManager.initSdk(this, new OnInitSdkListener() {
            @Override
            public void initSuccess(String code, String msg) {
                Log.e(TAG, "initSdk=" + msg);
            }

            @Override
            public void initError(String code, String msg) {
                T.s(MainActivity.this, msg);
            }
        });
        //添加sdk登陆监听,包含正常登陆，切换账号登陆，登陆过期后重新登陆
        sdkManager.addLoginListener(new OnLoginListener() {
            @Override
            public void loginSuccess(LogincallBack logincBack) {
                Log.e(TAG, "登陆成功 memId=" +
                        logincBack.mem_id + "  token=" + logincBack.user_token);
                T.s(MainActivity.this, "登陆成功");
                //一般登陆成功后需要显示浮点
                sdkManager.showFloatView();
            }

            @Override
            public void loginError(LoginErrorMsg loginErrorMsg) {
                Log.e(TAG, " code=" + loginErrorMsg.code + "  msg=" + loginErrorMsg.msg);
            }
        });
        sdkManager.addLogoutListener(new OnLogoutListener() {
            @Override
            public void logoutSuccess(int type, String code, String msg) {
                Log.e(TAG, "登出成功，类型type=" + type + " code=" + code + " msg=" + msg);
                if (type == OnLogoutListener.TYPE_NORMAL_LOGOUT) {//正常退出成功
                    Toast.makeText(MainActivity.this, "退出成功", Toast.LENGTH_SHORT).show();
                }
                if (type == OnLogoutListener.TYPE_SWITCH_ACCOUNT) {//切换账号退出成功
                    //游戏此时可跳转到登陆页面，让用户进行切换账号
//                    Toast.makeText(MainActivity.this,"退出登陆",Toast.LENGTH_SHORT).show();

                }
                if (type == OnLogoutListener.TYPE_TOKEN_INVALID) {//登陆过期退出成功
                    //游戏此时可跳转到登陆页面，让用户进行重新登陆
                    sdkManager.showLogin(true);
                }
            }

            @Override
            public void logoutError(int type, String code, String msg) {
                Log.e(TAG, "登出失败，类型type=" + type + " code=" + code + " msg=" + msg);
                if (type == OnLogoutListener.TYPE_NORMAL_LOGOUT) {//正常退出失败

                }
                if (type == OnLogoutListener.TYPE_SWITCH_ACCOUNT) {//切换账号退出失败

                }
                if (type == OnLogoutListener.TYPE_TOKEN_INVALID) {//登陆过期退出失败

                }
            }
        });
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_test_login:
                sdkManager.showLogin(true);
                break;
            case R.id.btn_test_charger:
                String money_str = etTestMoney.getText().toString().trim();
                String money = "0.01";
                if (!TextUtils.isEmpty(money_str) && !"".equals(money_str)) {
                    money = money_str;
                }
                CustomPayParam customPayParam = new CustomPayParam();
                initTestParam(customPayParam, money);
                customPayParam.setRoleinfo(initTestRoleInfo());
                sdkManager.showPay(customPayParam, new OnPaymentListener() {
                    @Override
                    public void paymentSuccess(PaymentCallbackInfo callbackInfo) {
                        double money = callbackInfo.money;
                        String msg = callbackInfo.msg;

                        // 弹出支付成功信息，一般不用
                        Toast.makeText(getApplication(), "充值金额数：" +
                                        callbackInfo.money + " 消息提示：" + callbackInfo.msg,
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void paymentError(PaymentErrorMsg errorMsg) {
                        // TODO Auto-generated method stub
                        int code = errorMsg.code;
                        double money = errorMsg.money;
                        String msg = errorMsg.msg;
                        // 弹出支付失败信息，一般不用
                        Toast.makeText(getApplication(), "充值失败：code:" +
                                        errorMsg.code + "  ErrorMsg:" + errorMsg.msg +
                                        "  预充值的金额：" + errorMsg.money,
                                Toast.LENGTH_LONG).show();
                    }
                });
                break;
            case R.id.btn_test_sendRoleinfo:
                RoleInfo roleInfo = initTestRoleInfo();
                roleInfo.setRole_type(1);
                sdkManager.setRoleInfo(roleInfo, new SubmitRoleInfoCallBack() {
                    @Override
                    public void submitSuccess() {
                        Toast.makeText(MainActivity.this, "提交成功", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void submitFail(String msg) {
                        T.s(MainActivity.this, msg);
                    }
                });
                break;
            case R.id.btn_test_logout:
                //调用此方法前请先设置登出监听
                sdkManager.logout();
                break;
            case R.id.btn_test_switchAccount:
                //切换账号会退出登陆，请在登出监听中接收切换退出结果
                sdkManager.switchAccount();
                break;
        }
    }

    private RoleInfo initTestRoleInfo() {
        RoleInfo roleInfo = new RoleInfo();
        roleInfo.setRolelevel_ctime("" + System.currentTimeMillis() / 1000);
        roleInfo.setRolelevel_mtime("" + System.currentTimeMillis() / 1000);
        roleInfo.setParty_name("");
        roleInfo.setRole_balence(1.00f);
        roleInfo.setRole_id("Role_id");
        roleInfo.setRole_level(0);
        roleInfo.setRole_name("roleName");
        roleInfo.setRole_vip(0);
        roleInfo.setServer_id("Server_id");
        roleInfo.setServer_name("serverName");
        return roleInfo;
    }

    private void initTestParam(CustomPayParam payParam, String money) {
        payParam.setCp_order_id("20161028111");
        payParam.setProduct_price(Float.parseFloat(money));
        payParam.setProduct_count(1);
        payParam.setProduct_id("1");
        payParam.setProduct_name("元宝");
        payParam.setProduct_desc("很好");
        payParam.setExchange_rate(1);
        payParam.setCurrency_name("金币");
        payParam.setExt("穿透");
    }

    /**
     * 在游戏销毁时需要调用sdk的销毁
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        sdkManager.recycle();
    }
}
