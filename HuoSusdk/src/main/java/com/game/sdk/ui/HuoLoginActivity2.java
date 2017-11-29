package com.game.sdk.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.game.sdk.HuosdkInnerManager;
import com.game.sdk.R;
import com.game.sdk.db.LoginControl;
import com.game.sdk.db.impl.UserLoginInfodao;
import com.game.sdk.domain.BaseRequestBean;
import com.game.sdk.domain.LoginErrorMsg;
import com.game.sdk.domain.LoginRequestBean;
import com.game.sdk.domain.LoginResultBean;
import com.game.sdk.domain.LogincallBack;
import com.game.sdk.domain.Notice;
import com.game.sdk.domain.RegisterOneResultBean;
import com.game.sdk.domain.RegisterResultBean;
import com.game.sdk.domain.UserInfo;
import com.game.sdk.domain.UserNameRegisterRequestBean;
import com.game.sdk.http.HttpCallbackDecode;
import com.game.sdk.http.HttpParamsBuild;
import com.game.sdk.http.SdkApi;
import com.game.sdk.listener.OnLoginListener;
import com.game.sdk.log.L;
import com.game.sdk.util.Base64Util;
import com.game.sdk.util.DialogUtil;
import com.game.sdk.util.GsonUtil;
import com.game.sdk.util.MResource;
import com.game.sdk.view.HuoFastLoginView;
import com.game.sdk.view.HuoLoginViewNew;
import com.game.sdk.view.HuoRegisterViewNew;
import com.game.sdk.view.HuoUserNameRegisterViewNew;
import com.game.sdk.view.SelectAccountView;
import com.game.sdk.view.ViewStackManager;
import com.kymjs.rxvolley.RxVolley;

public class HuoLoginActivity2 extends BaseActivity {
    private static final String TAG = HuoLoginActivity2.class.getSimpleName();
    public final static int TYPE_FAST_LOGIN = 0;
    public final static int TYPE_LOGIN = 1;
    //    public final static int TYPE_REGISTER_LOGIN=2;
    private final static int CODE_LOGIN_FAIL = -1;//登陆失败
    private final static int CODE_LOGIN_CANCEL = -2;//用户取消登陆
    HuoLoginViewNew huoLoginView;
    HuoRegisterViewNew huoRegisterView;
    //    private HuoFastLoginView huoFastLoginView;
    private HuoUserNameRegisterViewNew huoUserNameRegisterView;
    private ViewStackManager viewStackManager;
    private boolean callBacked;//是否已经回调过了
    private SelectAccountView huoSdkSelectAccountView;
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(MResource.getIdByName(this, "R.layout.huo_sdk_activity_huo_login_new2"));
        setupUI();
    }

    private void setupUI() {
        callBacked = false;
        viewStackManager = ViewStackManager.getInstance(this);
        int type = getIntent().getIntExtra("type", 1);
        huoLoginView = (HuoLoginViewNew) findViewById(MResource.getIdByName(this, "R.id.huo_sdk_loginView_new"));
//        huoFastLoginView = (HuoFastLoginView) findViewById(MResource.getIdByName(this, "R.id.huo_sdk_fastLoginView"));
        huoRegisterView = (HuoRegisterViewNew) findViewById(MResource.getIdByName(this, "R.id.huo_sdk_registerView"));
        huoUserNameRegisterView = (HuoUserNameRegisterViewNew) findViewById(MResource.getIdByName(this, "R.id.huo_sdk_userNameRegisterView"));
        huoSdkSelectAccountView = (SelectAccountView) findViewById(MResource.getIdByName(this, "R.id.huo_sdk_selectAccountView"));
        viewStackManager.addBackupView(huoLoginView);
//        viewStackManager.addBackupView(huoFastLoginView);
        viewStackManager.addBackupView(huoRegisterView);
        viewStackManager.addBackupView(huoUserNameRegisterView);
        viewStackManager.addBackupView(huoSdkSelectAccountView);
        switchUI(type);
    }

    public void switchUI(int type) {
        //如果是直接登陆，且之前没有账号
        if (HuosdkInnerManager.getInstance().isDirectLogin()) {
            UserInfo userInfoLast = UserLoginInfodao.getInstance(this).getUserInfoLast();
            if (userInfoLast == null || TextUtils.isEmpty(userInfoLast.username) || TextUtils.isEmpty(userInfoLast.password)) {//之前没有账号
                //从后台回去一个账号，并登陆
                L.e("hongliang", "准备自动注册登陆");
                getAccountByNet();
                return;
            }
        }
        if (type == TYPE_FAST_LOGIN) {
//            viewStackManager.addView(huoFastLoginView);
            //获取最后一次登陆账号进行快速登陆
            UserInfo userInfoLast = UserLoginInfodao.getInstance(this).getUserInfoLast();
            if (userInfoLast != null) {
                if (!TextUtils.isEmpty(userInfoLast.username) && !TextUtils.isEmpty(userInfoLast.password)) {
                    showLoging("正在登录...", true, null);
                    submitLogin(userInfoLast.username, userInfoLast.password);
                    return;
                }
            }
        } else if (type == TYPE_LOGIN) {
            viewStackManager.addView(huoLoginView);
        } else {
            viewStackManager.addView(huoRegisterView);
        }
    }

    @Override
    public void onBackPressed() {
        if (viewStackManager.isLastView()) {
//            if (huoFastLoginView.getVisibility() == View.VISIBLE) {//当前最后一个view是快速登陆view，不允许返回
//                return;
//            }
            super.onBackPressed();
        } else {
            viewStackManager.removeTopView();
        }
    }

    public HuoUserNameRegisterViewNew getHuoUserNameRegisterView() {
        return huoUserNameRegisterView;
    }

    public HuoRegisterViewNew getHuoRegisterView() {
        return huoRegisterView;
    }

    public HuoLoginViewNew getHuoLoginView() {
        return huoLoginView;
    }

    public void getAccountByNet() {
        BaseRequestBean baseRequestBean = new BaseRequestBean();
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(baseRequestBean));
        HttpCallbackDecode httpCallbackDecode = new HttpCallbackDecode<RegisterOneResultBean>(this, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(RegisterOneResultBean data) {
                if (data != null) {//注册账号
                    submitRegister(data);
                }
            }

            @Override
            public void onFailure(String code, String msg) {
                super.onFailure(code, msg);
                //失败了，显示登陆view
                viewStackManager.addView(huoLoginView);
            }
        };
        httpCallbackDecode.setShowTs(true);
        httpCallbackDecode.setLoadingCancel(false);
        httpCallbackDecode.setShowLoading(true);
        httpCallbackDecode.setLoadMsg("登陆中...");
        RxVolley.post(SdkApi.getRegisterOne(), httpParamsBuild.getHttpParams(), httpCallbackDecode);
    }

    private void submitRegister(RegisterOneResultBean data) {
        final String account = data.getUsername();
        final String password;
        if (TextUtils.isEmpty(data.getPassword())) {
            password = Base64Util.createBigSmallLetterStrOrNumberRadom(8);
        } else {
            password = data.getPassword();
        }
        UserNameRegisterRequestBean userNameRegisterRequestBean = new UserNameRegisterRequestBean();
        userNameRegisterRequestBean.setUsername(account);
        userNameRegisterRequestBean.setPassword(password);
        userNameRegisterRequestBean.setIntroducer("");
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(userNameRegisterRequestBean));
        HttpCallbackDecode httpCallbackDecode = new HttpCallbackDecode<RegisterResultBean>(this, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(RegisterResultBean data) {
                if (data != null) {
//                    T.s(loginActivity,"登陆成功："+data.getCp_user_token());
                    //接口回调通知
                    LoginControl.saveUserToken(data.getCp_user_token());
                    HuosdkInnerManager.notice = data.getNotice(); //发送通知内容
                    OnLoginListener onLoginListener = HuosdkInnerManager.getInstance().getOnLoginListener();
                    if (onLoginListener != null) {
                        onLoginListener.loginSuccess(new LogincallBack(data.getMem_id(), data.getCp_user_token()));
                        //登录成功后统一弹出弹框
                        DialogUtil.showNoticeDialog(HuosdkInnerManager.getInstance().getContext(), HuosdkInnerManager.notice);
                        if (true) {
                            Toast.makeText(HuoLoginActivity2.this, "试玩/一键注册无法进行实名信息认证，账号会存在安全隐患。", Toast.LENGTH_LONG).show();
                        }
                    }
                    HuoLoginActivity2.this.callBackFinish();
                    //保存账号到数据库
                    if (!UserLoginInfodao.getInstance(HuoLoginActivity2.this).findUserLoginInfoByName(account)) {
                        UserLoginInfodao.getInstance(HuoLoginActivity2.this).saveUserLoginInfo(account, password);
                    } else {
                        UserLoginInfodao.getInstance(HuoLoginActivity2.this).deleteUserLoginByName(account);
                        UserLoginInfodao.getInstance(HuoLoginActivity2.this).saveUserLoginInfo(account, password);
                    }
                }
            }

            @Override
            public void onFailure(String code, String msg) {
                super.onFailure(code, msg);
                //失败了，显示登陆view
                viewStackManager.addView(huoLoginView);
            }
        };
        httpCallbackDecode.setShowTs(true);
        httpCallbackDecode.setLoadingCancel(false);
        httpCallbackDecode.setShowLoading(true);
        httpCallbackDecode.setLoadMsg("登陆中...");
        RxVolley.post(SdkApi.getRegister(), httpParamsBuild.getHttpParams(), httpCallbackDecode);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewStackManager.clear();
        if (!callBacked) {//还没有回调过，是用户取消登陆
            LoginErrorMsg loginErrorMsg = new LoginErrorMsg(CODE_LOGIN_CANCEL, "用户取消登陆");
            OnLoginListener onLoginListener = HuosdkInnerManager.getInstance().getOnLoginListener();
            if (onLoginListener != null) {
                onLoginListener.loginError(loginErrorMsg);
            }
        }
    }

    /**
     * 通知回调成功并关闭activity
     */
    public void callBackFinish() {
        this.callBacked = true;
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    public static void start(Context context, int type) {
        Intent starter = new Intent(context, HuoLoginActivity2.class);
        starter.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(0, 0);
        }
        starter.putExtra("type", type);
        context.startActivity(starter);
    }

    private void submitLogin(final String userName, final String password) {
        final LoginRequestBean loginRequestBean = new LoginRequestBean();
        loginRequestBean.setUsername(userName);
        loginRequestBean.setPassword(password);
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(loginRequestBean));
        HttpCallbackDecode httpCallbackDecode = new HttpCallbackDecode<LoginResultBean>(HuoLoginActivity2.this, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(final LoginResultBean data) {
                //快速登陆需要延时3秒供用户选择是否切换账号
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (data != null) {//当前界面还在显示状态才执行
//                    T.s(loginActivity,"登陆成功："+data.getCp_user_token());
                            //接口回调通知
                            LoginControl.saveUserToken(data.getCp_user_token());
                            HuosdkInnerManager.notice = data.getNotice(); //发送通知内容
                            OnLoginListener onLoginListener = HuosdkInnerManager.getInstance().getOnLoginListener();
                            if (onLoginListener != null) {
                                onLoginListener.loginSuccess(new LogincallBack(data.getMem_id(), data.getCp_user_token()));
//                                //登录成功后统一弹出弹框
//                                getNotice();
                            }
                            HuoLoginActivity2.this.callBackFinish();
                            //保存账号到数据库
                            if (!UserLoginInfodao.getInstance(HuoLoginActivity2.this).findUserLoginInfoByName(userName)) {
                                UserLoginInfodao.getInstance(HuoLoginActivity2.this).saveUserLoginInfo(userName, password);
                            } else {
                                UserLoginInfodao.getInstance(HuoLoginActivity2.this).deleteUserLoginByName(userName);
                                UserLoginInfodao.getInstance(HuoLoginActivity2.this).saveUserLoginInfo(userName, password);
                            }

                            showLoging("欢迎回来", false, userName);
                        }
                    }
                }, 3000);
            }

            @Override
            public void onFailure(String code, String msg) {
                super.onFailure(code, msg);
                //快速登陆出错，直接去登陆页面
                viewStackManager.addView(HuoLoginActivity2.this.getHuoLoginView());
            }
        };
        httpCallbackDecode.setShowTs(true);
        httpCallbackDecode.setLoadingCancel(false);
        httpCallbackDecode.setShowLoading(false);
        httpCallbackDecode.setLoadMsg("正在登录...");
        RxVolley.post(SdkApi.getLogin(), httpParamsBuild.getHttpParams(), httpCallbackDecode);
    }

    private void getNotice() {
        BaseRequestBean baseRequestBean = new BaseRequestBean();
        baseRequestBean.setApp_id("1");
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(baseRequestBean));
        HttpCallbackDecode httpCallbackDecode = new HttpCallbackDecode<Notice>(this, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(Notice data) {
                L.e(TAG, "content =" + data.getContent() + ", title =" + data.getTitle());
                //登录成功后统一弹出弹框
                DialogUtil.showNoticeDialog(HuosdkInnerManager.getInstance().getContext(), data);
            }

            @Override
            public void onFailure(String code, String msg) {
                L.e(TAG, "code =" + code + ", msg =" + msg);
            }
        };
        httpCallbackDecode.setShowTs(false);
        httpCallbackDecode.setLoadingCancel(false);
        httpCallbackDecode.setShowLoading(false);//对话框继续使用install接口，在startup联网结束后，自动结束等待loading
        RxVolley.post(SdkApi.getNotice(), httpParamsBuild.getHttpParams(), httpCallbackDecode);
    }


    private void showLoging(String title, boolean isLoading, String accountname) {
        final Dialog dialog = new Dialog(this, R.style.Theme_Light_Dialog);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.fast_loign_dailog_layout, null);
        ImageView loading = (ImageView) dialogView.findViewById(R.id.huo_sdk_iv_fastLoading);
        TextView tvAccount = (TextView) dialogView.findViewById(R.id.tv_account);
        if (!isLoading) {
            tvAccount.setVisibility(View.VISIBLE);
            tvAccount.setText(accountname);
            loading.setVisibility(View.GONE);
        } else {
            tvAccount.setVisibility(View.GONE);
            loading.setVisibility(View.VISIBLE);
        }
        TextView tvTitle = (TextView) dialogView.findViewById(R.id.tv_title);
        tvTitle.setText(title);
        loading.setAnimation(DialogUtil.rotaAnimation());
        //获得dialog的window窗口
        Window window = dialog.getWindow();
        //设置dialog在屏幕底部
        window.setGravity(Gravity.BOTTOM);
        //设置dialog弹出时的动画效果，从屏幕底部向上弹出
        window.setWindowAnimations(R.style.dialogStyle);
        window.getDecorView().setPadding(0, 0, 0, 0);
        //获得window窗口的属性
        WindowManager.LayoutParams lp = window.getAttributes();
        //设置窗口宽度为充满全屏
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        //设置窗口高度为包裹内容
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //将设置好的属性set回去
        window.setAttributes(lp);
        //将自定义布局加载到dialog上
        dialog.setContentView(dialogView);
        dialog.show();
    }
}
