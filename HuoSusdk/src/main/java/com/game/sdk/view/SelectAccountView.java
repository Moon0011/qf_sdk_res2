package com.game.sdk.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.game.sdk.HuosdkInnerManager;
import com.game.sdk.db.LoginControl;
import com.game.sdk.db.impl.UserLoginInfodao;
import com.game.sdk.domain.BaseRequestBean;
import com.game.sdk.domain.LoginRequestBean;
import com.game.sdk.domain.LoginResultBean;
import com.game.sdk.domain.LogincallBack;
import com.game.sdk.domain.NotProguard;
import com.game.sdk.domain.Notice;
import com.game.sdk.http.HttpCallbackDecode;
import com.game.sdk.http.HttpParamsBuild;
import com.game.sdk.http.SdkApi;
import com.game.sdk.listener.OnLoginListener;
import com.game.sdk.log.L;
import com.game.sdk.log.T;
import com.game.sdk.ui.HuoLoginActivity;
import com.game.sdk.util.DialogUtil;
import com.game.sdk.util.GsonUtil;
import com.game.sdk.util.MResource;
import com.game.sdk.util.RegExpUtil;
import com.kymjs.rxvolley.RxVolley;

import java.util.List;


/**
 * Created by liu hong liang on 2017/6/20.
 */
@NotProguard
public class SelectAccountView extends FrameLayout {
    ListView lvAccountList;
    Button btnSubmit;
    private ViewStackManager viewStackManager;
    private List<LoginResultBean.UserName> userNameList;
    private String password;
    private SelectAccountAdapter selectAccountAdapter;
    private HuoLoginActivity loginActivity;
    private Context mContext;
    public SelectAccountView(Context context) {
        super(context);
        mContext = context;
        initUI();
    }

    public SelectAccountView( Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initUI();
    }

    public SelectAccountView( Context context,AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initUI();
    }

    private void initUI() {
        loginActivity= (HuoLoginActivity) getContext();
        viewStackManager = ViewStackManager.getInstance(loginActivity);
        LayoutInflater.from(getContext()).inflate(MResource.getIdByName(getContext(),"R.layout.huo_sdk_view_select_account"), this);
        lvAccountList= (ListView) findViewById(MResource.getIdByName(getContext(),"R.id.huo_sdk_lv_account_list"));
        btnSubmit= (Button) findViewById(MResource.getIdByName(getContext(),"R.id.huo_sdk_btn_submit"));
        btnSubmit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectAccountAdapter==null){
                    ((Activity) getContext()).finish();
                    return;
                }else{
                    int selectPosition = selectAccountAdapter.getSelectPosition();
                    LoginResultBean.UserName userName = userNameList.get(selectPosition);
                    submitLogin(userName.getUsername(),password);
                }
            }
        });

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
    public void setUserNameList(List<LoginResultBean.UserName> userNameList, String password) {
        this.userNameList = userNameList;
        this.password = password;
        selectAccountAdapter = new SelectAccountAdapter();
        lvAccountList.setAdapter(selectAccountAdapter);
    }

    private void submitLogin(final String account, final String password) {
        if (!RegExpUtil.isMatchAccount(account)) {
            T.s(loginActivity, "账号只能由6至16位英文或数字组成");
            return;
        }
        if ( !RegExpUtil.isMatchPassword(password)) {
            T.s(loginActivity, "密码只能由6至16位英文或数字组成");
            return;
        }
        final LoginRequestBean loginRequestBean=new LoginRequestBean();
        loginRequestBean.setUsername(account);
        loginRequestBean.setPassword(password);
        HttpParamsBuild httpParamsBuild=new HttpParamsBuild(GsonUtil.getGson().toJson(loginRequestBean));
        HttpCallbackDecode httpCallbackDecode = new HttpCallbackDecode<LoginResultBean>(loginActivity, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(LoginResultBean data) {
                if(data!=null){
//                    T.s(loginActivity,"登陆成功："+data.getCp_user_token());
                    //接口回调通知
                    LoginControl.saveUserToken(data.getCp_user_token());
                    HuosdkInnerManager.notice = data.getNotice(); //发送通知内容
                    OnLoginListener onLoginListener = HuosdkInnerManager.getInstance().getOnLoginListener();
                    if(onLoginListener!=null){
                        onLoginListener.loginSuccess(new LogincallBack(data.getMem_id(),data.getCp_user_token()));
                        //登录成功后统一弹出弹框
                        getNotice();
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
        RxVolley.post(SdkApi.getLogin(), httpParamsBuild.getHttpParams(),httpCallbackDecode);
    }

    private void getNotice() {
        BaseRequestBean baseRequestBean = new BaseRequestBean();
        baseRequestBean.setApp_id("1");
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(baseRequestBean));
        HttpCallbackDecode httpCallbackDecode = new HttpCallbackDecode<Notice>(mContext, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(Notice data) {
                //登录成功后统一弹出弹框
                DialogUtil.showNoticeDialog(HuosdkInnerManager.getInstance().getContext(), data);
            }

            @Override
            public void onFailure(String code, String msg) {
            }
        };
        httpCallbackDecode.setShowTs(false);
        httpCallbackDecode.setLoadingCancel(false);
        httpCallbackDecode.setShowLoading(false);//对话框继续使用install接口，在startup联网结束后，自动结束等待loading
        RxVolley.post(SdkApi.getNotice(), httpParamsBuild.getHttpParams(), httpCallbackDecode);
    }

    public class SelectAccountAdapter extends BaseAdapter {
        private int selectPosition=0;

        public int getSelectPosition() {
            return selectPosition;
        }

        public void setSelectPosition(int selectPosition) {
            this.selectPosition = selectPosition;
        }

        @Override
        public int getCount() {
            return userNameList.size();
        }

        @Override
        public Object getItem(int position) {
            return userNameList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView==null){
                convertView = LayoutInflater.from(parent.getContext()).inflate(MResource.getIdByName(parent.getContext(),"R.layout.huosdk_item_select_account"), parent, false);
                viewHolder=new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            }else{
                viewHolder= (ViewHolder) convertView.getTag();
            }
            viewHolder.tvAccountNumHint.setText("账号"+(position+1)+":");
            viewHolder.tvLoginAccount.setText(userNameList.get(position).getUsername());
            viewHolder.cbSelectAccount.setChecked(position==selectPosition);
            viewHolder.cbSelectAccount.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectPosition=position;
                    notifyDataSetChanged();
                }
            });
            convertView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectPosition=position;
                    notifyDataSetChanged();
                }
            });
            return convertView;
        }

        public  class ViewHolder {
            TextView tvAccountNumHint;
            TextView tvLoginAccount;
            CheckBox cbSelectAccount;
            ViewHolder(View view) {
                tvAccountNumHint= (TextView) view.findViewById(MResource.getIdByName(view.getContext(),"R.id.tv_account_num_hint"));
                tvLoginAccount= (TextView) view.findViewById(MResource.getIdByName(view.getContext(),"R.id.tv_login_account"));
                cbSelectAccount= (CheckBox) view.findViewById(MResource.getIdByName(view.getContext(),"R.id.cb_select_account"));
            }
        }
    }
}
