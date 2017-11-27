package com.game.sdk.view;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.game.sdk.HuosdkInnerManager;
import com.game.sdk.db.LoginControl;
import com.game.sdk.db.impl.UserLoginInfodao;
import com.game.sdk.domain.LoginRequestBean;
import com.game.sdk.domain.LoginResultBean;
import com.game.sdk.domain.LogincallBack;
import com.game.sdk.domain.UserInfo;
import com.game.sdk.http.HttpCallbackDecode;
import com.game.sdk.http.HttpParamsBuild;
import com.game.sdk.http.SdkApi;
import com.game.sdk.listener.OnLoginListener;
import com.game.sdk.ui.HuoLoginActivity;
import com.game.sdk.util.DialogUtil;
import com.game.sdk.util.GsonUtil;
import com.game.sdk.util.MResource;
import com.kymjs.rxvolley.RxVolley;

/**
 * Created by liu hong liang on 2016/11/12.
 */

public class HuoFastLoginView extends FrameLayout implements View.OnClickListener {
    private static final String TAG = HuoFastLoginView.class.getSimpleName();
    //快速登陆
    ImageView huoIvFastLoading;

    TextView huoTvFastUserName;

    TextView huoTvFastChangeCount;

    LinearLayout huoLlFastLogin;
    private HuoLoginActivity loginActivity;
    private ViewStackManager viewStackManager;
    Handler handler=new Handler();
    public HuoFastLoginView(Context context) {
        super(context);
        setupUI();
    }
    public HuoFastLoginView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupUI();
    }
    public HuoFastLoginView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupUI();
    }

    private void setupUI() {
        loginActivity= (HuoLoginActivity) getContext();
        viewStackManager=ViewStackManager.getInstance(loginActivity);
        LayoutInflater.from(getContext()).inflate(MResource.getIdByName(getContext(), MResource.LAYOUT, "huo_sdk_include_fast_login"), this);
        huoIvFastLoading= (ImageView) findViewById(MResource.getIdByName(getContext(),"id","huo_sdk_iv_fastLoading"));
        huoTvFastUserName= (TextView) findViewById(MResource.getIdByName(getContext(),"id","huo_sdk_tv_fastUserName"));
        huoTvFastChangeCount= (TextView) findViewById(MResource.getIdByName(getContext(),"id","huo_sdk_tv_fastChangeCount"));
        huoLlFastLogin= (LinearLayout) findViewById(MResource.getIdByName(getContext(),"id","huo_sdk_ll_fast_login"));
        huoTvFastChangeCount.setOnClickListener(this);

        huoIvFastLoading.setAnimation(DialogUtil.rotaAnimation());
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

    /**
     * 在设置显示的时候判断是否能够进行快速登陆，不能则直接去登陆
     * @param visibility
     */
    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if(visibility==VISIBLE){
            //获取最后一次登陆账号进行快速登陆
            UserInfo userInfoLast = UserLoginInfodao.getInstance(loginActivity).getUserInfoLast();
            if(userInfoLast!=null){
                if(!TextUtils.isEmpty(userInfoLast.username)&&!TextUtils.isEmpty(userInfoLast.password)){
                    huoTvFastUserName.setText(userInfoLast.username);
                    submitLogin(userInfoLast.username,userInfoLast.password);
                    return;
                }
            }
            //上一次没有登录过，直接去登陆界面
            viewStackManager.addView(loginActivity.getHuoLoginView());
            viewStackManager.removeView(this);
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==huoTvFastChangeCount.getId()){
            viewStackManager.addView(loginActivity.getHuoLoginView());
            viewStackManager.removeView(this);
        }
    }
    private void submitLogin(final String userName, final String password) {
        final LoginRequestBean loginRequestBean=new LoginRequestBean();
        loginRequestBean.setUsername(userName);
        loginRequestBean.setPassword(password);
        HttpParamsBuild httpParamsBuild=new HttpParamsBuild(GsonUtil.getGson().toJson(loginRequestBean));
        HttpCallbackDecode httpCallbackDecode = new HttpCallbackDecode<LoginResultBean>(loginActivity, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(final LoginResultBean data) {
                //快速登陆需要延时3秒供用户选择是否切换账号
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(data!=null&&getVisibility()==VISIBLE){//当前界面还在显示状态才执行
//                    T.s(loginActivity,"登陆成功："+data.getCp_user_token());
                            //接口回调通知
                            LoginControl.saveUserToken(data.getCp_user_token());
                            HuosdkInnerManager.notice = data.getNotice(); //发送通知内容
                            OnLoginListener onLoginListener = HuosdkInnerManager.getInstance().getOnLoginListener();
                            if(onLoginListener!=null){
                                onLoginListener.loginSuccess(new LogincallBack(data.getMem_id(),data.getCp_user_token()));
                                //登录成功后统一弹出弹框
                                DialogUtil.showNoticeDialog(HuosdkInnerManager.getInstance().getContext(), HuosdkInnerManager.notice);
                            }
                            loginActivity.callBackFinish();
                            //保存账号到数据库
                            if (!UserLoginInfodao.getInstance(loginActivity).findUserLoginInfoByName(userName)) {
                                UserLoginInfodao.getInstance(loginActivity).saveUserLoginInfo(userName, password);
                            } else {
                                UserLoginInfodao.getInstance(loginActivity).deleteUserLoginByName(userName);
                                UserLoginInfodao.getInstance(loginActivity).saveUserLoginInfo(userName, password);
                            }
                        }
                    }
                },3000);
            }
            @Override
            public void onFailure(String code, String msg) {
                super.onFailure(code, msg);
                //快速登陆出错，直接去登陆页面
                viewStackManager.addView(loginActivity.getHuoLoginView());
                viewStackManager.removeView(HuoFastLoginView.this);
            }
        };
        httpCallbackDecode.setShowTs(true);
        httpCallbackDecode.setLoadingCancel(false);
        httpCallbackDecode.setShowLoading(false);
        httpCallbackDecode.setLoadMsg("正在登录...");
        RxVolley.post(SdkApi.getLogin(), httpParamsBuild.getHttpParams(),httpCallbackDecode);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(handler!=null){
            handler.removeCallbacksAndMessages(null);
        }
    }
}
