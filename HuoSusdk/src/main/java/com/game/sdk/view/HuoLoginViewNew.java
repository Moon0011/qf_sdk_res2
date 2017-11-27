package com.game.sdk.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.game.sdk.HuosdkInnerManager;
import com.game.sdk.db.LoginControl;
import com.game.sdk.db.impl.UserLoginInfodao;
import com.game.sdk.domain.LoginRequestBean;
import com.game.sdk.domain.LoginResultBean;
import com.game.sdk.domain.LogincallBack;
import com.game.sdk.domain.UserInfo;
import com.game.sdk.domain.WebRequestBean;
import com.game.sdk.http.HttpCallbackDecode;
import com.game.sdk.http.HttpParamsBuild;
import com.game.sdk.http.SdkApi;
import com.game.sdk.listener.OnLoginListener;
import com.game.sdk.log.L;
import com.game.sdk.log.T;
import com.game.sdk.plugin.IHuoLogin;
import com.game.sdk.ui.FloatWebActivity;
import com.game.sdk.ui.HuoLoginActivity;
import com.game.sdk.util.DialogUtil;
import com.game.sdk.util.GsonUtil;
import com.game.sdk.util.MResource;
import com.game.sdk.util.RegExpUtil;
import com.kymjs.rxvolley.RxVolley;

import java.util.List;


/**
 * Created by Liuhongliangsdk on 2016/11/11.
 */
public class HuoLoginViewNew extends FrameLayout implements View.OnClickListener {
    private static final String TAG = HuoLoginViewNew.class.getSimpleName();
    //登陆
    LinearLayout huoLlLoginRegister, huoLlOneKeyLogin;
    RelativeLayout huoRlLogin;
    private HuoLoginActivity loginActivity;
    private EditText huo_et_loginAccount;
    private EditText huo_et_loginPwd;
    private Button huo_btn_loginSubmit;
    private ImageView huo_img_show_pwd;
    private ImageView huo_iv_logo;
    private boolean showPwd = false;
    private Button huo_btn_loginSubmitForgetPwd;
    private ViewStackManager viewStackManager;
    private PopupWindow pw_select_user;
    private RecordUserAdapter pw_adapter;
    private List<UserInfo> userInfoList;
    private ImageView huo_iv_loginUserSelect;
    private RelativeLayout huo_rl_loginAccount;
    IHuoLogin iHuoLogin;
    private Context mContext;

    public HuoLoginViewNew(Context context) {
        super(context);
        mContext = context;
        setupUI();
    }

    public HuoLoginViewNew(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setupUI();
    }

    public HuoLoginViewNew(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setupUI();
    }

    private void setupUI() {
        loginActivity = (HuoLoginActivity) getContext();
        viewStackManager = ViewStackManager.getInstance(loginActivity);
        LayoutInflater.from(getContext()).inflate(MResource.getIdByName(getContext(), MResource.LAYOUT, "huo_sdk_include_login_new"), this);
        huoRlLogin = (RelativeLayout) findViewById(MResource.getIdByName(getContext(), "R.id.huo_sdk_rl_login"));
        huo_et_loginAccount = (EditText) findViewById(MResource.getIdByName(getContext(), "R.id.huo_sdk_et_loginAccount"));
        huo_et_loginPwd = (EditText) findViewById(MResource.getIdByName(getContext(), "R.id.huo_sdk_et_loginPwd"));
        huo_img_show_pwd = (ImageView) findViewById(MResource.getIdByName(getContext(), "R.id.huo_sdk_img_show_pwd"));
        huo_btn_loginSubmitForgetPwd = (Button) findViewById(MResource.getIdByName(getContext(), "R.id.huo_sdk_btn_loginSubmitForgetPwd"));
        huo_btn_loginSubmit = (Button) findViewById(MResource.getIdByName(getContext(), "R.id.huo_sdk_btn_loginSubmit"));
        huoLlOneKeyLogin = (LinearLayout) findViewById(MResource.getIdByName(getContext(), "R.id.huo_sdk_ll_onekeylogin"));
        huoLlLoginRegister = (LinearLayout) findViewById(MResource.getIdByName(getContext(), "R.id.huo_sdk_ll_loginRegister"));
        huo_iv_loginUserSelect = (ImageView) findViewById(MResource.getIdByName(getContext(), "R.id.huo_sdk_iv_loginUserSelect"));
        huo_rl_loginAccount = (RelativeLayout) findViewById(MResource.getIdByName(getContext(), "R.id.huo_sdk_rl_loginAccount"));
        huo_iv_logo = (ImageView) findViewById(MResource.getIdByName(getContext(), "R.id.huo_sdk_iv_logo"));
        huoLlOneKeyLogin.setOnClickListener(this);
        huoLlLoginRegister.setOnClickListener(this);
        huo_btn_loginSubmit.setOnClickListener(this);
        huo_img_show_pwd.setOnClickListener(this);
        huo_iv_loginUserSelect.setOnClickListener(this);
        huo_btn_loginSubmitForgetPwd.setOnClickListener(this);
        UserInfo userInfoLast = UserLoginInfodao.getInstance(loginActivity).getUserInfoLast();
        if (userInfoLast != null) {
            huo_et_loginAccount.setText(userInfoLast.username);
            huo_et_loginPwd.setText(userInfoLast.password);
        }
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
    public void onClick(View view) {
        if (view.getId() == huoLlLoginRegister.getId()) {
            viewStackManager.addView(loginActivity.getHuoRegisterView());
        } else if (view.getId() == huo_btn_loginSubmit.getId()) {
            submitLogin();
        } else if (view.getId() == huo_img_show_pwd.getId()) {
            if (showPwd) {
                huo_et_loginPwd.setInputType(InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                showPwd = false;
            } else {
                huo_et_loginPwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                showPwd = true;
            }
        } else if (view.getId() == huo_btn_loginSubmitForgetPwd.getId()) {
            HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(new WebRequestBean()));
            FloatWebActivity.start(loginActivity, SdkApi.getWebForgetpwd(), "忘记密码",
                    httpParamsBuild.getHttpParams().getUrlParams().toString(), httpParamsBuild.getAuthkey());
        } else if (view.getId() == huo_iv_loginUserSelect.getId()) {
            userselect(huo_et_loginAccount, huo_rl_loginAccount.getWidth());
        } else if (view.getId() == huoLlOneKeyLogin.getId()) {//试玩
            Log.e("qfgame","试玩");
            HuoUserNameRegisterViewNew huoUserNameRegisterView = (HuoUserNameRegisterViewNew) viewStackManager.getViewByClass(HuoUserNameRegisterViewNew.class);
            if (huoUserNameRegisterView != null) {
                huoUserNameRegisterView.switchUI(true);
                viewStackManager.addView(huoUserNameRegisterView);
            }
        }
    }

    private void submitLogin() {
        final String account = huo_et_loginAccount.getText().toString().trim();
        final String password = huo_et_loginPwd.getText().toString().trim();
        if (!RegExpUtil.isMatchAccount(account)) {
            T.s(loginActivity, "账号只能由6至16位英文或数字组成");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            T.s(loginActivity, "密码不能为空");
            return;
        }
        final LoginRequestBean loginRequestBean = new LoginRequestBean();
        loginRequestBean.setUsername(account);
        loginRequestBean.setPassword(password);
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(loginRequestBean));
        HttpCallbackDecode httpCallbackDecode = new HttpCallbackDecode<LoginResultBean>(loginActivity, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(LoginResultBean data) {
                if (data != null) {
                    //接口回调通知
                    if (data.getUserlist() != null && data.getUserlist().size() > 1) {
                        SelectAccountView selectAccountView = (SelectAccountView) viewStackManager.getViewByClass(SelectAccountView.class);
                        selectAccountView.setUserNameList(data.getUserlist(), password);
                        //填对话框选择账号进行登陆
                        viewStackManager.showView(viewStackManager.getViewByClass(SelectAccountView.class));
                        return;
                    }
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
                }
            }
        };
        httpCallbackDecode.setShowTs(true);
        httpCallbackDecode.setLoadingCancel(false);
        httpCallbackDecode.setShowLoading(true);
        httpCallbackDecode.setLoadMsg("正在登录...");
        RxVolley.post(SdkApi.getLogin(), httpParamsBuild.getHttpParams(), httpCallbackDecode);
    }

    private void userselect(View v, int width) {
        L.e(TAG, "width=" + width);
        if (pw_select_user != null && pw_select_user.isShowing()) {
            pw_select_user.dismiss();
        } else {
            userInfoList = UserLoginInfodao.getInstance(loginActivity)
                    .getUserLoginInfo();
            if (null == userInfoList || userInfoList.isEmpty()) {
                return;
            }
            if (null == pw_adapter) {
                pw_adapter = new RecordUserAdapter();
            }
            if (pw_select_user == null) {
                // View
                // view=getLayoutInflater().inflate(R.layout.tiantianwan_pw_list,null);
                View view = LayoutInflater.from(loginActivity).inflate(MResource.getIdByName(loginActivity, "R.layout.huo_sdk_pop_record_account"), null);
                // ListView lv_pw=(ListView) view.findViewById(R.id.lv_pw);
                ListView lv_pw = (ListView) view.findViewById(MResource
                        .getIdByName(loginActivity, "R.id.huo_sdk_lv_pw"));
                // LinearLayout.LayoutParams lp=new
                // LinearLayout.LayoutParams(200,-2 );
                // lv_pw.setLayoutParams(lp);
                lv_pw.setCacheColorHint(0x00000000);
                lv_pw.setAdapter(pw_adapter);
                lv_pw.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> adapterview,
                                            View view, int position, long row) {
                        pw_select_user.dismiss();
                        UserInfo userInfo = userInfoList.get(position);
                        huo_et_loginAccount.setText(userInfo.username);
                        huo_et_loginPwd.setText(userInfo.password);
                    }
                });
                pw_select_user = new PopupWindow(view, width,
                        LinearLayout.LayoutParams.WRAP_CONTENT, true);
                pw_select_user.setBackgroundDrawable(new ColorDrawable(
                        0x00000000));
                pw_select_user.setContentView(view);
            } else {
                pw_adapter.notifyDataSetChanged();
            }
            pw_select_user.showAsDropDown(v, 0, 0);
        }
    }

    /**
     * popupwindow显示已经登录用户的设配器
     *
     * @author Administrator
     */
    private class RecordUserAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return userInfoList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return userInfoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            if (null == convertView) {
                View view = LayoutInflater.from(loginActivity).inflate(MResource.getIdByName(loginActivity,
                        "R.layout.huo_sdk_pop_record_account_list_item"), null);

                convertView = view;
            }
            TextView tv_username = (TextView) convertView.findViewById(MResource.getIdByName(loginActivity, "R.id.huo_sdk_tv_username"));
            ImageView iv_delete = (ImageView) convertView.findViewById(MResource.getIdByName(loginActivity, "R.id.huo_sdk_iv_delete"));
            iv_delete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 如果删除的用户名与输入框中的用户名一致将删除输入框中的用户名与密码
                    if (huo_et_loginAccount.getText().toString().trim()
                            .equals(userInfoList.get(position).username)) {
                        huo_et_loginAccount.setText("");
                        huo_et_loginPwd.setText("");
                    }
                    UserLoginInfodao.getInstance(loginActivity)
                            .deleteUserLoginByName(
                                    userInfoList.get(position).username);
                    userInfoList.remove(position);
                    if (null != pw_adapter) {
                        if (userInfoList.isEmpty()) {
                            pw_select_user.dismiss();
                        }
                        notifyDataSetChanged();
                    }
                }
            });
            tv_username.setText(userInfoList.get(position).username);
            return convertView;
        }
    }
}
