package com.game.sdk.view;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.game.sdk.HuosdkInnerManager;
import com.game.sdk.domain.LoginRequestBean;
import com.game.sdk.domain.SmsSendRequestBean;
import com.game.sdk.domain.SmsSendResultBean;
import com.game.sdk.http.HttpCallbackDecode;
import com.game.sdk.http.HttpParamsBuild;
import com.game.sdk.http.SdkApi;
import com.game.sdk.log.T;
import com.game.sdk.ui.HuoLoginActivity;
import com.game.sdk.util.GsonUtil;
import com.game.sdk.util.MResource;
import com.game.sdk.util.RegExpUtil;
import com.kymjs.rxvolley.RxVolley;

/**
 * Created by liu hong liang on 2016/11/14.
 */

public class HuoSmsLoginView extends FrameLayout implements View.OnClickListener {
    private HuoLoginActivity loginActivity;
    private ViewStackManager viewStackManager;
    private EditText huo_et_mLoginAccount;
    private EditText huo_et_mLoginSmsCode;
    private Button huo_btn_mSendCode;
    private Button huo_btn_mLoginSubmit;
    private LinearLayout huo_ll_mLoginGotoLogin;
    private ImageView huo_iv_logo;
    public HuoSmsLoginView(Context context) {
        super(context);
        setupUI();
    }

    public HuoSmsLoginView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupUI();
    }

    public HuoSmsLoginView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupUI();
    }
    private void setupUI() {
        loginActivity = (HuoLoginActivity) getContext();
        viewStackManager = ViewStackManager.getInstance(loginActivity);
        LayoutInflater.from(getContext()).inflate(MResource.getIdByName(getContext(), MResource.LAYOUT, "huo_sdk_include_mobile_login"), this);
        huo_et_mLoginAccount = (EditText) findViewById(MResource.getIdByName(loginActivity, "R.id.huo_et_mLoginAccount"));
        huo_et_mLoginSmsCode = (EditText) findViewById(MResource.getIdByName(loginActivity, "R.id.huo_et_mLoginSmsCode"));
        huo_btn_mSendCode = (Button) findViewById(MResource.getIdByName(loginActivity, "R.id.huo_btn_mSendCode"));
        huo_btn_mLoginSubmit = (Button) findViewById(MResource.getIdByName(loginActivity, "R.id.huo_btn_mLoginSubmit"));
        huo_ll_mLoginGotoLogin = (LinearLayout) findViewById(MResource.getIdByName(loginActivity, "R.id.huo_ll_mLoginGotoLogin"));
        huo_iv_logo= (ImageView) findViewById(MResource.getIdByName(getContext(),"R.id.huo_iv_mRegisterLogo"));

        huo_btn_mSendCode.setOnClickListener(this);
        huo_ll_mLoginGotoLogin.setOnClickListener(this);
        huo_btn_mLoginSubmit.setOnClickListener(this);

        //NEW 2017年2月28日10:12:23 - 加载switch资源
        if (HuosdkInnerManager.isSwitchLogin){
            MResource.loadImgFromSDCard(huo_iv_logo,MResource.PATH_FILE_ICON_LOGO);
        }
    }
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //自动设置相应的布局尺寸
        if(getChildCount()>0){
            View childAt = getChildAt(0);
            HuoFastLoginView.LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            layoutParams.leftMargin=(int)(getResources().getDimension(MResource.getIdByName(loginActivity, "R.dimen.activity_horizontal_margin")));
            layoutParams.rightMargin=layoutParams.leftMargin;
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==huo_btn_mLoginSubmit.getId()){

        }else if(v.getId()==huo_btn_mSendCode.getId()){
            sendSms();
        }else if(v.getId()==huo_ll_mLoginGotoLogin.getId()){
            viewStackManager.removeTopView();
        }
    }
    private void submitLogin() {
        final String account = huo_et_mLoginAccount.getText().toString().trim();
        final String authCode = huo_et_mLoginSmsCode.getText().toString().trim();
        if (!RegExpUtil.isMatchAccount(account)) {
            T.s(loginActivity, "账号只能由6至16位英文或数字组成");
            return;
        }
        final LoginRequestBean loginRequestBean=new LoginRequestBean();
        loginRequestBean.setUsername(account);
//        loginRequestBean.setPassword(password);
        HttpParamsBuild httpParamsBuild=new HttpParamsBuild(GsonUtil.getGson().toJson(loginRequestBean));
//        HttpCallbackDecode httpCallbackDecode = new HttpCallbackDecode<LoginResultBean>(loginActivity, httpParamsBuild.getAuthkey()) {
//            @Override
//            public void onDataSuccess(LoginResultBean data) {
//                if(data!=null){
//                    LoginControl.saveUserToken(data.getCp_user_token());
////                    T.s(loginActivity,"登陆成功："+data.getCp_user_token());
//                    //接口回调通知
//                    LoginControl.saveUserToken(data.getCp_user_token());
//                    HuosdkInnerManager.getInstance(loginActivity)
//                            .getLoginlistener()
//                            .loginSuccess(new LogincallBack(data.getMem_id(),data.getCp_user_token()));
//                    loginActivity.finish();
//                    //保存账号到数据库
//                    if (!UserLoginInfodao.getInstance(loginActivity).findUserLoginInfoByName(account)) {
//                        UserLoginInfodao.getInstance(loginActivity).saveUserLoginInfo(account, password);
//                    } else {
//                        UserLoginInfodao.getInstance(loginActivity).deleteUserLoginByName(account);
//                        UserLoginInfodao.getInstance(loginActivity).saveUserLoginInfo(account, password);
//                    }
//                }
//            }
//        };
//        httpCallbackDecode.setShowTs(true);
//        httpCallbackDecode.setLoadingCancel(false);
//        httpCallbackDecode.setShowLoading(true);
//        httpCallbackDecode.setLoadMsg("正在登录...");
//        RxVolley.post(SdkApi.getLogin(), httpParamsBuild.getHttpParams(),httpCallbackDecode);
    }


    private void sendSms() {
        final String account = huo_et_mLoginAccount.getText().toString().trim();
        if(!RegExpUtil.isMobileNumber(account)){
            T.s(loginActivity,"请输入正确的手机号");
        }
        SmsSendRequestBean smsSendRequestBean=new SmsSendRequestBean();
        smsSendRequestBean.setMobile(account);
        smsSendRequestBean.setSmstype(SmsSendRequestBean.TYPE_REGISTER);
        HttpParamsBuild httpParamsBuild=new HttpParamsBuild(GsonUtil.getGson().toJson(smsSendRequestBean));
        HttpCallbackDecode httpCallbackDecode = new HttpCallbackDecode<SmsSendResultBean>(loginActivity, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(SmsSendResultBean data) {
                if(data!=null){
                    //开始计时控件
                    startCodeTime(60);
                }
            }
        };
        httpCallbackDecode.setShowTs(true);
        httpCallbackDecode.setLoadingCancel(false);
        httpCallbackDecode.setShowLoading(true);
        httpCallbackDecode.setLoadMsg("发送中...");
        RxVolley.post(SdkApi.getSmsSend(), httpParamsBuild.getHttpParams(),httpCallbackDecode);
    }
    Handler handler=new Handler();
    private void startCodeTime(int time) {
        huo_btn_mSendCode.setTag(time);
        if(time<=0){
            huo_btn_mSendCode.setText("获取验证码");
            huo_btn_mSendCode.setClickable(true);
            return;
        }else{
            huo_btn_mSendCode.setClickable(false);
            huo_btn_mSendCode.setText(time+"秒");
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int delayTime = (int) huo_btn_mSendCode.getTag();
                startCodeTime(--delayTime);

            }
        },1000);
    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacksAndMessages(null);
    }
}
