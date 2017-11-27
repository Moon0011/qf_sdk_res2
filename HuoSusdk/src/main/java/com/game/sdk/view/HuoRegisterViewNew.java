package com.game.sdk.view;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.game.sdk.HuosdkInnerManager;
import com.game.sdk.SdkConstant;
import com.game.sdk.db.LoginControl;
import com.game.sdk.db.impl.UserLoginInfodao;
import com.game.sdk.domain.LogincallBack;
import com.game.sdk.domain.RegisterMobileRequestBean;
import com.game.sdk.domain.RegisterResultBean;
import com.game.sdk.domain.SmsSendRequestBean;
import com.game.sdk.domain.SmsSendResultBean;
import com.game.sdk.http.HttpCallbackDecode;
import com.game.sdk.http.HttpParamsBuild;
import com.game.sdk.http.SdkApi;
import com.game.sdk.listener.OnLoginListener;
import com.game.sdk.log.T;
import com.game.sdk.ui.HuoLoginActivity;
import com.game.sdk.util.DialogUtil;
import com.game.sdk.util.GsonUtil;
import com.game.sdk.util.MResource;
import com.game.sdk.util.RegExpUtil;
import com.kymjs.rxvolley.RxVolley;

/**
 * Created by Liuhongliangsdk on 2016/11/11.
 */

public class HuoRegisterViewNew extends FrameLayout implements View.OnClickListener {
    private HuoLoginActivity loginActivity;
    EditText huo_sdk_et_mRegisterAccount;
    private EditText huo_sdk_et_mRegisterPwd;
    private EditText huo_sdk_et_mRegisterCode;
    private EditText huo_sdk_et_mInvitationCode;
    private Button huo_sdk_btn_mRegisterSendCode;
    private Button huo_sdk_btn_mRegisterSubmit;
    private LinearLayout huo_sdk_ll_mRegisterUserNameRegister;
    private RelativeLayout huo_sdk_rl_mInvitationCode;
    //    private LinearLayout huo_sdk_ll_mRegisterFastRegister;
    private LinearLayout huo_sdk_ll_mRegisterGotoLogin;
    private ViewStackManager viewStackManager;
    private ImageView huo_sdk_img_show_pwd;
    private ImageView huo_sdk_iv_logo;
    private boolean showPwd = false;
    private Context mContext;

    public HuoRegisterViewNew(Context context) {
        super(context);
        mContext = context;
        setupUI();
    }

    public HuoRegisterViewNew(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setupUI();
    }

    public HuoRegisterViewNew(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setupUI();
    }

    private void setupUI() {
        loginActivity = (HuoLoginActivity) getContext();
        viewStackManager = ViewStackManager.getInstance(loginActivity);
        LayoutInflater.from(getContext()).inflate(MResource.getIdByName(getContext(), MResource.LAYOUT, "huo_sdk_inlude_mobile_register_new"), this);
        huo_sdk_et_mRegisterAccount = (EditText) findViewById(MResource.getIdByName(loginActivity, "R.id.huo_sdk_et_mRegisterAccount"));
        huo_sdk_et_mRegisterPwd = (EditText) findViewById(MResource.getIdByName(loginActivity, "R.id.huo_sdk_et_mRegisterPwd"));
        huo_sdk_et_mRegisterCode = (EditText) findViewById(MResource.getIdByName(loginActivity, "R.id.huo_sdk_et_mRegisterCode"));
        huo_sdk_btn_mRegisterSendCode = (Button) findViewById(MResource.getIdByName(loginActivity, "R.id.huo_sdk_btn_mRegisterSendCode"));
        huo_sdk_et_mInvitationCode = (EditText) findViewById(MResource.getIdByName(loginActivity, "R.id.huo_sdk_et_mInvitationCode"));
        huo_sdk_rl_mInvitationCode = (RelativeLayout) findViewById(MResource.getIdByName(loginActivity, "R.id.huo_sdk_rl_mInvitationCode"));
        huo_sdk_btn_mRegisterSubmit = (Button) findViewById(MResource.getIdByName(loginActivity, "R.id.huo_sdk_btn_mRegisterSubmit"));
        huo_sdk_ll_mRegisterUserNameRegister = (LinearLayout) findViewById(MResource.getIdByName(loginActivity, "R.id.huo_sdk_ll_mRegisterUserNameRegister"));
//        huo_sdk_ll_mRegisterFastRegister= (LinearLayout) findViewById(MResource.getIdByName(loginActivity,"R.id.huo_sdk_ll_mRegisterFastRegister"));
        huo_sdk_ll_mRegisterGotoLogin = (LinearLayout) findViewById(MResource.getIdByName(loginActivity, "R.id.huo_sdk_ll_mRegisterGotoLogin"));
        huo_sdk_img_show_pwd = (ImageView) findViewById(MResource.getIdByName(getContext(), "R.id.huo_sdk_img_show_pwd"));
        huo_sdk_iv_logo = (ImageView) findViewById(MResource.getIdByName(getContext(), "R.id.huo_sdk_iv_mRegisterLogo"));
        huo_sdk_btn_mRegisterSendCode.setOnClickListener(this);
        huo_sdk_btn_mRegisterSubmit.setOnClickListener(this);
        huo_sdk_img_show_pwd.setOnClickListener(this);
        huo_sdk_ll_mRegisterUserNameRegister.setOnClickListener(this);
//        huo_sdk_ll_mRegisterFastRegister.setOnClickListener(this);
        huo_sdk_ll_mRegisterGotoLogin.setOnClickListener(this);
        if ("1".equals(SdkConstant.SHOW_INVITATION)) {
            huo_sdk_rl_mInvitationCode.setVisibility(VISIBLE);
        } else {
            huo_sdk_rl_mInvitationCode.setVisibility(GONE);
        }
        //NEW 2017年2月28日10:12:23 - 加载switch资源
        if (HuosdkInnerManager.isSwitchLogin) {
            MResource.loadImgFromSDCard(huo_sdk_iv_logo, MResource.PATH_FILE_ICON_LOGO);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == huo_sdk_ll_mRegisterGotoLogin.getId()) {//退回登陆
            viewStackManager.showView(viewStackManager.getViewByClass(HuoLoginViewNew.class));
        } else if (view.getId() == huo_sdk_btn_mRegisterSendCode.getId()) {//发送验证码
            sendSms();
        } else if (view.getId() == huo_sdk_btn_mRegisterSubmit.getId()) {//提交注册
            submitRegister();
        }
//        else if(view.getId()==huo_sdk_ll_mRegisterFastRegister.getId()){//试玩
//            HuoUserNameRegisterView huoUserNameRegisterView = (HuoUserNameRegisterView) viewStackManager.getViewByClass(HuoUserNameRegisterView.class);
//            if(huoUserNameRegisterView!=null){
//                huoUserNameRegisterView.switchUI(true);
//                viewStackManager.addView(huoUserNameRegisterView);
//            }
//        }
        else if (view.getId() == huo_sdk_ll_mRegisterUserNameRegister.getId()) {//用户名注册
            HuoUserNameRegisterViewNew huoUserNameRegisterView = (HuoUserNameRegisterViewNew) viewStackManager.getViewByClass(HuoUserNameRegisterViewNew.class);
            if (huoUserNameRegisterView != null) {
                huoUserNameRegisterView.switchUI(false);
                viewStackManager.addView(huoUserNameRegisterView);
            }
        } else if (view.getId() == huo_sdk_img_show_pwd.getId()) {
            if (showPwd) {
                huo_sdk_et_mRegisterPwd.setInputType(InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                showPwd = false;
            } else {
                huo_sdk_et_mRegisterPwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                showPwd = true;
            }
        }
    }

    public static boolean isSimplePassword(String password) {
        if (TextUtils.isDigitsOnly(password)) {
            char tempCh = '0';
            for (int i = 0; i < password.length(); i++) {
                if (i == 0) {
                    tempCh = password.charAt(i);
                } else {
                    if (((int) tempCh + 1) != ((int) (password.charAt(i)))) {
                        return false;
                    }
                    tempCh = password.charAt(i);
                }
            }
            return true;
        }
        return false;
    }

    private void submitRegister() {
        final String account = huo_sdk_et_mRegisterAccount.getText().toString().trim();
        final String password = huo_sdk_et_mRegisterPwd.getText().toString().trim();
        String authCode = huo_sdk_et_mRegisterCode.getText().toString().trim();
        String inviationCode = huo_sdk_et_mInvitationCode.getText().toString().trim();
        if (!RegExpUtil.isMobileNumber(account)) {
            T.s(loginActivity, "请输入正确的手机号");
            return;
        }
        if (password.length() < 6) {
            T.s(loginActivity, "密码由6位以上英文或数字组成");
            return;
        }
        if (isSimplePassword(password)) {
            T.s(loginActivity, "亲，密码太简单，请重新输入");
            return;
        }
        if (TextUtils.isEmpty(authCode)) {
            T.s(loginActivity, "请先输入验证码");
            return;
        }
        RegisterMobileRequestBean registerMobileRequestBean = new RegisterMobileRequestBean();
        registerMobileRequestBean.setMobile(account);
        registerMobileRequestBean.setPassword(password);
        registerMobileRequestBean.setSmscode(authCode);
        registerMobileRequestBean.setIntroducer(inviationCode);
        registerMobileRequestBean.setSmstype(SmsSendRequestBean.TYPE_REGISTER);
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(registerMobileRequestBean));
        HttpCallbackDecode httpCallbackDecode = new HttpCallbackDecode<RegisterResultBean>(loginActivity, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(RegisterResultBean data) {
                if (data != null) {
                    //接口回调通知
                    LoginControl.saveUserToken(data.getCp_user_token());
                    HuosdkInnerManager.notice = data.getNotice(); //发送通知内容
                    OnLoginListener onLoginListener = HuosdkInnerManager.getInstance().getOnLoginListener();
                    if (onLoginListener != null) {
                        onLoginListener.loginSuccess(new LogincallBack(data.getMem_id(), data.getCp_user_token()));
                        //登录成功后统一弹出弹框
                        DialogUtil.showNoticeDialog(HuosdkInnerManager.getInstance().getContext(), HuosdkInnerManager.notice);
                    }
                    loginActivity.callBackFinish();
                    //保存账号到数据库
                    if (!UserLoginInfodao.getInstance(loginActivity).findUserLoginInfoByName(account)) {
                        UserLoginInfodao.getInstance(loginActivity).saveUserLoginInfo(account, password);
                    } else {
                        UserLoginInfodao.getInstance(loginActivity).deleteUserLoginByName(account);
                        UserLoginInfodao.getInstance(loginActivity).saveUserLoginInfo(account, password);
                    }

                    //弹出notice框
                }
            }
        };
        httpCallbackDecode.setShowTs(true);
        httpCallbackDecode.setLoadingCancel(false);
        httpCallbackDecode.setShowLoading(true);
        httpCallbackDecode.setLoadMsg("注册中...");
        RxVolley.post(SdkApi.getRegisterMobile(), httpParamsBuild.getHttpParams(), httpCallbackDecode);

    }

    private void sendSms() {
        final String account = huo_sdk_et_mRegisterAccount.getText().toString().trim();
        if (!RegExpUtil.isMobileNumber(account)) {
            T.s(loginActivity, "请输入正确的手机号");
            return;
        }
        SmsSendRequestBean smsSendRequestBean = new SmsSendRequestBean();
        smsSendRequestBean.setMobile(account);
        smsSendRequestBean.setSmstype(SmsSendRequestBean.TYPE_REGISTER);
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(smsSendRequestBean));
        HttpCallbackDecode httpCallbackDecode = new HttpCallbackDecode<SmsSendResultBean>(loginActivity, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(SmsSendResultBean data) {
                if (data != null) {
                    //开始计时控件
                    startCodeTime(60);
                }
            }
        };
        httpCallbackDecode.setShowTs(true);
        httpCallbackDecode.setLoadingCancel(false);
        httpCallbackDecode.setShowLoading(true);
        httpCallbackDecode.setLoadMsg("发送中...");
        RxVolley.post(SdkApi.getSmsSend(), httpParamsBuild.getHttpParams(), httpCallbackDecode);
    }

    Handler handler = new Handler();

    private void startCodeTime(int time) {
        huo_sdk_btn_mRegisterSendCode.setTag(time);
        if (time <= 0) {
            huo_sdk_btn_mRegisterSendCode.setText("获取验证码");
            huo_sdk_btn_mRegisterSendCode.setClickable(true);
            return;
        } else {
            huo_sdk_btn_mRegisterSendCode.setClickable(false);
            huo_sdk_btn_mRegisterSendCode.setText(time + "秒");
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int delayTime = (int) huo_sdk_btn_mRegisterSendCode.getTag();
                startCodeTime(--delayTime);

            }
        }, 1000);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //自动设置相应的布局尺寸
        if (getChildCount() > 0) {
            View childAt = getChildAt(0);
            LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            layoutParams.leftMargin = (int) (getResources().getDimension(MResource.getIdByName(loginActivity, "R.dimen.huo_sdk_activity_horizontal_margin")));
            layoutParams.rightMargin = layoutParams.leftMargin;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacksAndMessages(null);
    }
}
