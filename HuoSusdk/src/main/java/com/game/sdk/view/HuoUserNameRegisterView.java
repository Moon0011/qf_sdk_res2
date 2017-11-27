package com.game.sdk.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
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
import android.widget.TextView;
import android.widget.Toast;

import com.game.sdk.HuosdkInnerManager;
import com.game.sdk.SdkConstant;
import com.game.sdk.db.LoginControl;
import com.game.sdk.db.impl.UserLoginInfodao;
import com.game.sdk.domain.BaseRequestBean;
import com.game.sdk.domain.LogincallBack;
import com.game.sdk.domain.RegisterOneResultBean;
import com.game.sdk.domain.RegisterResultBean;
import com.game.sdk.domain.UserNameRegisterRequestBean;
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
 * Created by liu hong liang on 2016/11/12.
 */

public class HuoUserNameRegisterView extends FrameLayout implements View.OnClickListener {
    private HuoLoginActivity loginActivity;
    private ViewStackManager viewStackManager;
    private LinearLayout huo_sdk_ll_uRegisterAccount;
    private RelativeLayout huo_sdk_rl_uInvitationCode;
    private EditText huo_sdk_et_uRegisterAccount;
    private EditText huo_sdk_et_uInvitationCode;
    private RelativeLayout huo_sdk_rl_uRegisterBackLogin;
    private Button huo_sdk_btn_uRegisterSubmit;
    private EditText huo_sdk_et_uRegisterPwd;
    private TextView huo_sdk_tv_uRegisterTitle;
    private boolean isShiWan=false;
    private ImageView huo_sdk_img_show_pwd;
    private ImageView huo_sdk_iv_logo;

    private boolean showPwd=false;
    public HuoUserNameRegisterView(Context context) {
        super(context);
        setupUI();
    }

    public HuoUserNameRegisterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupUI();
    }

    public HuoUserNameRegisterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupUI();
    }
    private void setupUI() {
        loginActivity= (HuoLoginActivity) getContext();
        viewStackManager=ViewStackManager.getInstance(loginActivity);
        LayoutInflater.from(getContext()).inflate(MResource.getIdByName(getContext(), MResource.LAYOUT, "huo_sdk_include_user_register"), this);
        huo_sdk_tv_uRegisterTitle= (TextView) findViewById(MResource.getIdByName(loginActivity,"R.id.huo_sdk_tv_uRegisterTitle"));
        huo_sdk_ll_uRegisterAccount= (LinearLayout) findViewById(MResource.getIdByName(loginActivity,"R.id.huo_sdk_ll_uRegisterAccount"));
        huo_sdk_et_uRegisterAccount= (EditText) findViewById(MResource.getIdByName(loginActivity,"R.id.huo_sdk_et_uRegisterAccount"));
        huo_sdk_et_uRegisterPwd= (EditText) findViewById(MResource.getIdByName(loginActivity,"R.id.huo_sdk_et_uRegisterPwd"));
        huo_sdk_et_uInvitationCode= (EditText) findViewById(MResource.getIdByName(loginActivity,"R.id.huo_sdk_et_uInvitationCode"));
        huo_sdk_rl_uInvitationCode= (RelativeLayout) findViewById(MResource.getIdByName(loginActivity,"R.id.huo_sdk_rl_uInvitationCode"));
        huo_sdk_rl_uRegisterBackLogin= (RelativeLayout) findViewById(MResource.getIdByName(loginActivity,"R.id.huo_sdk_rl_uRegisterBackLogin"));
        huo_sdk_btn_uRegisterSubmit= (Button) findViewById(MResource.getIdByName(loginActivity,"R.id.huo_sdk_btn_uRegisterSubmit"));
        huo_sdk_img_show_pwd= (ImageView) findViewById(MResource.getIdByName(getContext(),"R.id.huo_sdk_img_show_pwd"));
        huo_sdk_iv_logo= (ImageView) findViewById(MResource.getIdByName(getContext(),"R.id.huo_sdk_iv_uRegisterLogo"));

        huo_sdk_rl_uRegisterBackLogin.setOnClickListener(this);
        huo_sdk_btn_uRegisterSubmit.setOnClickListener(this);
        huo_sdk_img_show_pwd.setOnClickListener(this);
        if("1".equals(SdkConstant.SHOW_INVITATION)){
            huo_sdk_rl_uInvitationCode.setVisibility(VISIBLE);
        }else{
            huo_sdk_rl_uInvitationCode.setVisibility(GONE);
        }
        //NEW 2017年2月28日10:12:23 - 加载switch资源
        if (HuosdkInnerManager.isSwitchLogin){
            MResource.loadImgFromSDCard(huo_sdk_iv_logo,MResource.PATH_FILE_ICON_LOGO);
        }
        huo_sdk_et_uRegisterPwd.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //账号注册，但是输入的是手机号
                if(hasFocus && RegExpUtil.isMobileNumber(huo_sdk_et_uRegisterAccount.getText().toString().trim())){
                    T.s(loginActivity,"账号由字母加数字组合");
                }
            }
        });
    }

    public void switchUI(boolean isShiWan) {
        this.isShiWan = isShiWan;
        if (isShiWan) {
            huo_sdk_et_uRegisterAccount.setEnabled(false);
            huo_sdk_ll_uRegisterAccount.setBackgroundColor(Color.parseColor("#e8ecf3"));
            huo_sdk_tv_uRegisterTitle.setText("一键注册");
            //一键注册显示密码
            huo_sdk_et_uRegisterPwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            showPwd=true;
            getAccountByNet();
        } else {
            huo_sdk_et_uRegisterAccount.setEnabled(true);
            huo_sdk_ll_uRegisterAccount.setBackgroundColor(loginActivity.getResources().getColor(android.R.color.transparent));
            huo_sdk_et_uRegisterAccount.setBackgroundColor(loginActivity.getResources().getColor(android.R.color.transparent));
            huo_sdk_tv_uRegisterTitle.setText("用户注册");
            huo_sdk_et_uRegisterAccount.setText("");
            huo_sdk_et_uRegisterPwd.setText("");
            huo_sdk_et_uRegisterPwd.setInputType(InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            showPwd = false;
        }
    }
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //自动设置相应的布局尺寸
        if(getChildCount()>0){
            View childAt = getChildAt(0);
            HuoFastLoginView.LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            layoutParams.leftMargin=(int)(getResources().getDimension(MResource.getIdByName(loginActivity, "R.dimen.huo_sdk_activity_horizontal_margin")));
            layoutParams.rightMargin=layoutParams.leftMargin;
        }
    }
    @Override
    public void onClick(View v) {
        if(v.getId()==huo_sdk_rl_uRegisterBackLogin.getId()){
            viewStackManager.showView(viewStackManager.getViewByClass(HuoLoginView.class));
        }else if(v.getId()==huo_sdk_btn_uRegisterSubmit.getId()){//提交注册
            submitRegister();
        }else  if(v.getId()== huo_sdk_img_show_pwd.getId()){
            if (showPwd) {
                huo_sdk_et_uRegisterPwd.setInputType(InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                showPwd = false;
            } else {
                huo_sdk_et_uRegisterPwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                showPwd = true;
            }
        }
    }
    public static boolean isSimplePassword(String password){
        if(TextUtils.isDigitsOnly(password)){
            char tempCh='0';
            for(int i=0;i<password.length();i++){
                if(i==0){
                    tempCh=password.charAt(i);
                }else{
                    if( ((int)tempCh+1) != ((int)(password.charAt(i))) ){
                        return false;
                    }
                    tempCh=password.charAt(i);
                }
            }
            return true;
        }
        return false;
    }
    private void submitRegister() {
        final String account = huo_sdk_et_uRegisterAccount.getText().toString().trim();
        final String password = huo_sdk_et_uRegisterPwd.getText().toString().trim();
        String inviationCode = huo_sdk_et_uInvitationCode.getText().toString().trim();
        //账号注册，但是输入的是手机号
        if( RegExpUtil.isMobileNumber(huo_sdk_et_uRegisterAccount.getText().toString().trim())){
            T.s(loginActivity,"账号只能由字母加数字组合");
            return;
        }
        if (password.length()<6) {
            T.s(loginActivity,"密码由6位以上英文或数字组成");
            return;
        }
        if(isSimplePassword(password)){
            T.s(loginActivity,"亲，密码太简单，请重新输入");
            return;
        }
        if ( !RegExpUtil.isMatchPassword(password)) {
            T.s(loginActivity, "密码只能由6至16位英文或数字组成");
            return;
        }
        UserNameRegisterRequestBean userNameRegisterRequestBean=new UserNameRegisterRequestBean();
        userNameRegisterRequestBean.setUsername(account);
        userNameRegisterRequestBean.setPassword(password);
        userNameRegisterRequestBean.setIntroducer(inviationCode);
        HttpParamsBuild httpParamsBuild=new HttpParamsBuild(GsonUtil.getGson().toJson(userNameRegisterRequestBean));
        HttpCallbackDecode httpCallbackDecode = new HttpCallbackDecode<RegisterResultBean>(loginActivity, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(RegisterResultBean data) {
                if(data!=null){
//                    T.s(loginActivity,"登陆成功："+data.getCp_user_token());
                    //接口回调通知
                    LoginControl.saveUserToken(data.getCp_user_token());
                    HuosdkInnerManager.notice = data.getNotice(); //发送通知内容
                    OnLoginListener onLoginListener = HuosdkInnerManager.getInstance().getOnLoginListener();
                    if(onLoginListener!=null){
                        onLoginListener.loginSuccess(new LogincallBack(data.getMem_id(),data.getCp_user_token()));
                        //登录成功后统一弹出弹框
                        DialogUtil.showNoticeDialog(HuosdkInnerManager.getInstance().getContext(), HuosdkInnerManager.notice);
                        if(isShiWan) {
                            Toast.makeText(getContext(), "试玩/一键注册无法进行实名信息认证，账号会存在安全隐患。", Toast.LENGTH_LONG).show();
                        }
                    }
                    loginActivity.callBackFinish();
                    //保存账号到数据库
                    if (!UserLoginInfodao.getInstance(loginActivity).findUserLoginInfoByName(account)) {
                        UserLoginInfodao.getInstance(loginActivity).saveUserLoginInfo(account, password);
                    } else {
                        UserLoginInfodao.getInstance(loginActivity).deleteUserLoginByName(account);
                        UserLoginInfodao.getInstance(loginActivity).saveUserLoginInfo(account, password);
                    }
                }
            }
        };
        httpCallbackDecode.setShowTs(true);
        httpCallbackDecode.setLoadingCancel(false);
        httpCallbackDecode.setShowLoading(true);
        httpCallbackDecode.setLoadMsg("注册中...");
        RxVolley.post(SdkApi.getRegister(), httpParamsBuild.getHttpParams(),httpCallbackDecode);
    }

    public void getAccountByNet() {
        BaseRequestBean baseRequestBean=new BaseRequestBean();
        HttpParamsBuild httpParamsBuild=new HttpParamsBuild(GsonUtil.getGson().toJson(baseRequestBean));
        HttpCallbackDecode httpCallbackDecode = new HttpCallbackDecode<RegisterOneResultBean>(loginActivity, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(RegisterOneResultBean data) {
                if(data!=null){
                    huo_sdk_et_uRegisterAccount.setText(data.getUsername());
                    huo_sdk_et_uRegisterPwd.setText(data.getPassword());
                }
            }
        };
        httpCallbackDecode.setShowTs(true);
        httpCallbackDecode.setLoadingCancel(false);
        httpCallbackDecode.setShowLoading(false);
        RxVolley.post(SdkApi.getRegisterOne(), httpParamsBuild.getHttpParams(),httpCallbackDecode);
    }
}
