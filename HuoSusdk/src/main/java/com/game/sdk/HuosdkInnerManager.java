package com.game.sdk;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.game.sdk.db.LoginControl;
import com.game.sdk.dialog.OpenFloatPermissionDialog;
import com.game.sdk.domain.BaseRequestBean;
import com.game.sdk.domain.CustomPayParam;
import com.game.sdk.domain.NotProguard;
import com.game.sdk.domain.Notice;
import com.game.sdk.domain.NoticeResultBean;
import com.game.sdk.domain.RoleInfo;
import com.game.sdk.domain.SdkPayRequestBean;
import com.game.sdk.domain.StartUpBean;
import com.game.sdk.domain.StartupResultBean;
import com.game.sdk.domain.SubmitRoleInfoCallBack;
import com.game.sdk.domain.UproleinfoRequestBean;
import com.game.sdk.floatwindow.FloatViewManager;
import com.game.sdk.floatwindow.OrientationSensorManager;
import com.game.sdk.http.HttpCallbackDecode;
import com.game.sdk.http.HttpParamsBuild;
import com.game.sdk.http.SdkApi;
import com.game.sdk.listener.OnInitSdkListener;
import com.game.sdk.listener.OnLoginListener;
import com.game.sdk.listener.OnLogoutListener;
import com.game.sdk.listener.OnPaymentListener;
import com.game.sdk.log.L;
import com.game.sdk.log.SP;
import com.game.sdk.log.T;
import com.game.sdk.so.NativeListener;
import com.game.sdk.so.SdkNative;
import com.game.sdk.ui.HuoLoginActivity;
import com.game.sdk.ui.WebPayActivity;
import com.game.sdk.util.BaseAppUtil;
import com.game.sdk.util.DeviceUtil;
import com.game.sdk.util.DialogUtil;
import com.game.sdk.util.GsonUtil;
import com.game.sdk.util.HLAppUtil;
import com.game.sdk.util.MiuiDeviceUtil;
import com.kymjs.rxvolley.RxVolley;
import com.kymjs.rxvolley.http.RequestQueue;
import com.kymjs.rxvolley.toolbox.HTTPSTrustManager;

/**
 * author janecer 2014年7月22日上午9:45:18
 */
public class HuosdkInnerManager {
    private final static int CODE_INIT_FAIL = -1;
    private final static int CODE_INIT_SUCCESS = 1;
    private static final String TAG = HuosdkInnerManager.class.getSimpleName();
    private static HuosdkInnerManager instance;
    private  Context mContext;
    private OnInitSdkListener onInitSdkListener;
    private OnPaymentListener paymentListener;
    private OnLoginListener onLoginListener;
    private OnLogoutListener onLogoutListener;
    private int initRequestCount=0;
    public static Notice notice ; //登录后公告
    public static boolean isSwitchLogin = false; //是否切换
    private boolean directLogin=false;//是否使用直接登陆
    private boolean initSuccess=false;
    private Handler huosdkHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CODE_INIT_FAIL:
                    if(msg.arg2<3){//最多重试3次
                        initSdk(msg.arg2+1);
                    }else{
                        //关闭等待loading
                        onInitSdkListener.initError(msg.arg1+"",msg.obj+"");
                        DialogUtil.dismissDialog();
                    }
                    break;
                case CODE_INIT_SUCCESS:
                    L.e("hongliangsdk1",SdkConstant.HS_AGENT);
                    initRequestCount++;
                    //去初始化
                    gotoStartup(1);
                    break;
            }
        }
    };


    // 单例模式
    @NotProguard
    public static synchronized HuosdkInnerManager getInstance() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            L.e(TAG, "实例化失败,未在主线程调用");
            return null;
        }
        if (null == instance) {
            instance = new HuosdkInnerManager();
        }
        return instance;
    }
    @NotProguard
    public void setContext(Context context){
        this.mContext=context;
    }
    public Context getContext(){
        return mContext;
    }
    @NotProguard
    private HuosdkInnerManager() {
    }
    /**
     * 初始化设置
     */
    private void initSetting(){
        boolean isPortrait=BaseAppUtil.isPortraitForActivity(mContext);
        if(isPortrait){
            SdkConstant.screen_orientation= ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }else{
            SdkConstant.screen_orientation= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }
        HTTPSTrustManager.allowAllSSL();//开启https支持
        try {
            RxVolley.setRequestQueue(RequestQueue.newRequestQueue(BaseAppUtil.getDefaultSaveRootPath(mContext,"huoHttp")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void setScreenOrientation(boolean isPortrait){
        if(isPortrait){
            SdkConstant.customer_screen_orientation= ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }else{
            SdkConstant.customer_screen_orientation= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }
    }
    public int getScreenOrientation(){
        if(SdkConstant.customer_screen_orientation==null){
            return SdkConstant.screen_orientation;
        }
        return SdkConstant.customer_screen_orientation;
    }
    /**
     * 初始化sdk
     * @param context 上下文对象
     * @param onInitSdkListener 回调监听
     */
    public void initSdk(Context context,OnInitSdkListener onInitSdkListener){
        this.onInitSdkListener=onInitSdkListener;
        this.mContext=context;
        if(!checkCallOk(false)){
            return;
        }
        initSetting();
        HuosdkService.startService(mContext);
//        CrashHandler crashHandler = CrashHandler.getInstance();
//        crashHandler.init(mContext, "");
        new ActivityLifecycleManager().startActivityLifecycleManager(mContext);
        //初始化设备信息
        SdkNative.soInit(context);
        //初始化sp
        SP.init(mContext);
        initRequestCount=0;
        initSdk(1);
    }


    public boolean isDirectLogin() {
        return directLogin;
    }
    public void setDirectLogin(boolean directLogin) {
        this.directLogin = directLogin;
    }

    public void setFloatInitXY(int x,int y){
        SdkConstant.floatInitX=x;
        SdkConstant.floatInitY=y;
    }

    /**
     * 初始化相关数据
     * count=1标示正常请求，2表示在初始化时发现rsakey错误后的重试流程
     */
    private void initSdk(final int count) {
        Log.e(TAG,"isSLogin:"+isSwitchLogin);
        isSwitchLogin = mContext.getSharedPreferences("huo_sdk_sp", Context.MODE_PRIVATE).getBoolean("switch_login",false);
        //TODO 如果判断有切换账号逻辑，则不执行nativeInit，将使用net获取的值,此时直接返回init_success
        if (isSwitchLogin){
            Message message = Message.obtain();
            message.what = CODE_INIT_SUCCESS;
            message.arg2=count;
            huosdkHandler.sendMessage(message);
            return;
        }
        //初始化native
        AsyncTask<String, Integer, String> nativeAsyncTask = new AsyncTask<String, Integer, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //弹出等待loading在，installer和startup都完成后或者出现异常时关闭
                DialogUtil.showDialog(mContext, false, "初始化中，请稍后……");
            }
            @Override
            protected String doInBackground(String... params) {
                //初始化本地c配置
                if(SdkNative.initLocalConfig(mContext,SdkNative.TYPE_SDK)){
                    SdkNative.initNetConfig(mContext, new NativeListener() {
                        @Override
                        public void onSuccess() {
                            Message message = Message.obtain();
                            message.what = CODE_INIT_SUCCESS;
                            message.arg2=count;
                            huosdkHandler.sendMessage(message);
                        }
                        @Override
                        public void onFail(int code, final String msg) {
                            L.e("hongliangsdk", "native 失败code=" + code);
                            L.e("hongliangsdk", "native 失败msg=" + msg);
                            Message message = Message.obtain();
                            message.what = CODE_INIT_FAIL;
                            message.arg1 = code;
                            message.obj = msg;
                            message.arg2=count;
                            huosdkHandler.sendMessage(message);
                        }
                    });
                }else{
                    Message message = Message.obtain();
                    message.what = CODE_INIT_SUCCESS;
                    message.arg2=count;
                    huosdkHandler.sendMessage(message);
                }
                return null;
            }
        };
        if (!BaseAppUtil.isNetWorkConneted(mContext)) {
            Toast.makeText(mContext, "网络连接错误，请检查网络", Toast.LENGTH_SHORT).show();
            return;
        }
        nativeAsyncTask.execute();
    }
    /**
     * count=1标示正常请求，2表示在初始化时发现rsakey错误后的重试流程
     * @param count 当前是第几次请求
     */
    private void gotoStartup(final int count) {
        StartUpBean startUpBean = new StartUpBean();
        int open_cnt = SdkNative.addInstallOpenCnt(mContext);//增量更新openCnt
        startUpBean.setOpen_cnt(open_cnt + "");
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(startUpBean));
        HttpCallbackDecode httpCallbackDecode = new HttpCallbackDecode<StartupResultBean>(mContext, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(StartupResultBean data) {
                if (data != null) {
                    SdkConstant.userToken = data.getUser_token();
                    SdkConstant.SERVER_TIME_INTERVAL = data.getTimestamp() - System.currentTimeMillis();
                    SdkConstant.thirdLoginInfoList=data.getOauth_info();
                    if ("1".equals(data.getUp_status())) {//版本更新
                        SdkNative.resetInstall(mContext);//有更新重置install数据
                        if (!TextUtils.isEmpty(data.getUp_url())) {
                            HuosdkService.startServiceByUpdate(mContext, data.getUp_url());
                        }
                    }
                    initSuccess=true;
                    onInitSdkListener.initSuccess("200","初始化成功");
                }
            }
            @Override
            public void onFailure(String code, String msg) {
                if(count<3){
                    //1001	请求KEY错误	rsakey	解密错误
                    if(HttpCallbackDecode.CODE_RSA_KEY_ERROR.equals(code)){//删除本地公钥，重新请求rsa公钥
                        SdkNative.resetInstall(mContext);
                        L.e(TAG,"rsakey错误，重新请求rsa公钥");
                        if(initRequestCount<2){//initSdk只重试一次rsa请求
                            initSdk(1000);
                            return;
                        }
                    }
                    super.onFailure(code,msg);
                    gotoStartup(count+1);//重试
                }else{
                    super.onFailure(code,msg);
                    onInitSdkListener.initError(code,msg);
                }
            }
        };
        httpCallbackDecode.setShowTs(false);
        httpCallbackDecode.setLoadingCancel(false);
        httpCallbackDecode.setShowLoading(false);//对话框继续使用install接口，在startup联网结束后，自动结束等待loading
        RxVolley.post(SdkApi.getStartup(), httpParamsBuild.getHttpParams(), httpCallbackDecode);
    }

    /**
     * 执行退出登陆
     * @param type
     */
    public void logoutExecute(final int type){
        if(!LoginControl.isLogin()){
            if(onLogoutListener!=null){
                onLogoutListener.logoutSuccess(type,SdkConstant.CODE_NOLOGIN,"尚未登陆");
            }
            return;
        }
        if(type==OnLogoutListener.TYPE_TOKEN_INVALID){//账号过期的，直接通知cp过期
            removeFloatView();
            removeFloatView();
            if(onLogoutListener!=null){
                onLogoutListener.logoutSuccess(type,SdkConstant.CODE_SUCCESS,"退出成功");
            }
            LoginControl.clearLogin();
            return;
        }
        BaseRequestBean baseRequestBean=new BaseRequestBean();
        HttpParamsBuild httpParamsBuild=new HttpParamsBuild(GsonUtil.getGson().toJson(baseRequestBean));
        HttpCallbackDecode httpCallbackDecode = new HttpCallbackDecode<NoticeResultBean>(mContext, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(NoticeResultBean data) {
                removeFloatView();
                if(onLogoutListener!=null){
                    onLogoutListener.logoutSuccess(type,SdkConstant.CODE_SUCCESS,"退出成功");
                }
                LoginControl.clearLogin();
            }

            @Override
            public void onFailure(String code, String msg) {
                super.onFailure(code, msg);
                if(onLogoutListener!=null){
                    onLogoutListener.logoutError(type,code,msg);
                }
            }
        };
        httpCallbackDecode.setShowTs(false);
        httpCallbackDecode.setLoadingCancel(false);
        httpCallbackDecode.setShowLoading(false);
        RxVolley.post(SdkApi.getLogout(), httpParamsBuild.getHttpParams(),httpCallbackDecode);
    }

    /**
     * 退出登陆
     */
    public void logout(){
        if(!checkCallOk(true)){
            return;
        }
        logoutExecute(OnLogoutListener.TYPE_NORMAL_LOGOUT);
    }
    /**
     * 退出登录
     */
    public void addLogoutListener(final OnLogoutListener onLogoutListener) {
        this.onLogoutListener=onLogoutListener;
    }
    /**
     * 打开用户中心
     */
    public void openUcenter() {
        if(!checkCallOk(true)){
            return;
        }
        if (!LoginControl.isLogin()) {
            Toast.makeText(mContext, "请先登录！", Toast.LENGTH_SHORT).show();
            return;
        }
        FloatViewManager.getInstance(mContext).openucenter();
    }

    /**
     * 显示登录
     */
    public void showLogin(boolean isShowQuikLogin) {
        if(!checkCallOk(true)){
            return;
        }
        LoginControl.clearLogin();
        //普通登陆类型
        removeFloatView();
        if(isShowQuikLogin){
            HuoLoginActivity.start(mContext, HuoLoginActivity.TYPE_FAST_LOGIN);
        }else{
            HuoLoginActivity.start(mContext, HuoLoginActivity.TYPE_LOGIN);
        }
    }
    /**
     * 切换账号
     */
    public void switchAccount(){
        if(!checkCallOk(true)){
            return;
        }
        logoutExecute(OnLogoutListener.TYPE_SWITCH_ACCOUNT);
    }
    /**
     * 注册一个登录监听，需要在不使用的时候解除监听，例如onDestory方法中解除
     * @param  onLoginListener 登陆监听
     */
    public void addLoginListener(OnLoginListener onLoginListener) {
       this.onLoginListener =onLoginListener;
    }
    /**
     * 解除登陆监听
     */
    public void removeLoginListener(OnLoginListener onLoginListener) {
        this.onLoginListener =null;
    }
    /**
     * 启动支付
     * @param payParam        支付参数
     * @param paymentListener 支付回调监听
     */
    public void showPay(CustomPayParam payParam, OnPaymentListener paymentListener) {
        if(!checkCallOk(true)){
            return;
        }
        if (!checkPayParams(payParam)) {
            return;
        }
        SdkPayRequestBean sdkPayRequestBean = new SdkPayRequestBean();
        sdkPayRequestBean.setOrderinfo(payParam);
        sdkPayRequestBean.setRoleinfo(payParam.getRoleinfo());
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(sdkPayRequestBean));
        StringBuilder urlParams = httpParamsBuild.getHttpParams().getUrlParams();
        this.paymentListener = paymentListener;
        WebPayActivity.start(mContext, urlParams.toString(),payParam.getProduct_price(),payParam.getProduct_name(),httpParamsBuild.getAuthkey());
    }

    private boolean checkPayParams(CustomPayParam payParam) {
        if (!BaseAppUtil.isNetWorkConneted(mContext)) {
            Toast.makeText(mContext, "网络连接错误，请检查网络", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!LoginControl.isLogin()) {
            Toast.makeText(mContext, "请先登录！", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (payParam.getCp_order_id()==null) {
            Toast.makeText(mContext, "订单号不能为空！", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (payParam.getProduct_price() == null) {
            Toast.makeText(mContext, "商品价格不能为空！", Toast.LENGTH_SHORT).show();
            return false;
        }
        float price = payParam.getProduct_price();
        float tempPrice = price * 100;
        if (tempPrice - (int) tempPrice > 0) {
            Log.d("checkPayParams", "价格不合理，多于两位小数,已经去掉");
            payParam.setProduct_price((Float.valueOf((int) tempPrice)) / 100);
        }
        if (payParam.getProduct_id()==null) {
            Toast.makeText(mContext, "商品id不能为空！", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (payParam.getProduct_name()==null) {
            Toast.makeText(mContext, "商品名称不能为空！", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (payParam.getExt()==null) {
            Toast.makeText(mContext, "cp扩展参数不能为空！", Toast.LENGTH_SHORT).show();
            return false;
        }
        return checkRoleInfoParam(payParam.getRoleinfo());
    }

    public OnPaymentListener getPaymentListener() {
        return paymentListener;
    }

    public void setRoleInfo(RoleInfo roleInfo, final SubmitRoleInfoCallBack submitRoleInfoCallBack) {
        if(!checkCallOk(true)){
            return;
        }
        if (!BaseAppUtil.isNetWorkConneted(mContext)) {
            Toast.makeText(mContext, "网络连接错误，请检查网络", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!LoginControl.isLogin()) {
            Toast.makeText(mContext, "请先登录！", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!checkRoleInfoParam(roleInfo)){
            return;
        }
        UproleinfoRequestBean uproleinfoRequestBean = new UproleinfoRequestBean();
        uproleinfoRequestBean.setRoleinfo(roleInfo);
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(uproleinfoRequestBean));
        HttpCallbackDecode httpCallbackDecode = new HttpCallbackDecode<String>(mContext, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(String data) {
                submitRoleInfoCallBack.submitSuccess();
                L.e("setRoleinfo", "成功");
            }

            @Override
            public void onFailure(String code, String msg) {
                super.onFailure(code, msg);
                if (!TextUtils.isEmpty(msg)) {
                    submitRoleInfoCallBack.submitFail(msg);
                } else {
                    submitRoleInfoCallBack.submitFail("发送失败");
                }
                L.e("setRoleinfo", "失败：" + code + "  " + msg);
            }
        };
        httpCallbackDecode.setShowTs(false);
        httpCallbackDecode.setLoadingCancel(false);
        httpCallbackDecode.setShowLoading(false);
        RxVolley.post(SdkApi.getUproleinfo(), httpParamsBuild.getHttpParams(), httpCallbackDecode);
    }
    private boolean checkRoleInfoParam(RoleInfo roleInfo){
        if(roleInfo==null){
            Toast.makeText(mContext, "角色信息不能为空！", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (roleInfo.getRole_type() == null) {
            Toast.makeText(mContext, "数据类型不能为空！", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (roleInfo.getServer_id()==null) {
            Toast.makeText(mContext, "服务器id不能为空！", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (roleInfo.getServer_name()==null) {
            Toast.makeText(mContext, "所在服务器名称不能为空！", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (roleInfo.getRole_id()==null) {
            Toast.makeText(mContext, "角色id不能为空！", Toast.LENGTH_SHORT).show();
            return false;

        }
        if (roleInfo.getRole_name()==null) {
            Toast.makeText(mContext, "角色名称不能为空！", Toast.LENGTH_SHORT).show();
            return false;

        }
        if (roleInfo.getParty_name() == null) {
            Toast.makeText(mContext, "工会、帮派名称不能为空！", Toast.LENGTH_SHORT).show();
            return false;

        }

        if (roleInfo.getRole_level()==null) {
            Toast.makeText(mContext, "角色等级不能为空！", Toast.LENGTH_SHORT).show();
            return false;

        }
        if (roleInfo.getRole_vip()==null) {
            Toast.makeText(mContext, "vip等级不能为空！", Toast.LENGTH_SHORT).show();
            return false;

        }
        if (roleInfo.getRole_balence() == null) {
            Toast.makeText(mContext, "用户游戏币余额不能为空！", Toast.LENGTH_SHORT).show();
            return false;

        }
        if (roleInfo.getRolelevel_ctime()==null) {
            Toast.makeText(mContext, "创建角色的时间不能为空！", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (roleInfo.getRolelevel_mtime()==null) {
            Toast.makeText(mContext, "角色等级变化时间不能为空！", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    /**
     * 显示浮标
     */
    public void showFloatView() {
        if(!checkCallOk(false)){
            return;
        }
        if (!LoginControl.isLogin()) {
            return;
        }
        L.e(TAG,"准备显示浮点："+SdkConstant.isShowFloat);
        if(SdkConstant.isShowFloat){//没有设置隐藏显示
            FloatViewManager.getInstance(mContext).showFloat();
            boolean floatWindowOpAllowed = HLAppUtil.isFloatWindowOpAllowed(mContext);
            if (!floatWindowOpAllowed&&(MiuiDeviceUtil.isMiui()|| DeviceUtil.isMeizuFlymeOS())) {
                new OpenFloatPermissionDialog().showDialog(mContext, true, null, new OpenFloatPermissionDialog.ConfirmDialogListener() {
                    @Override
                    public void ok() {
                        DeviceUtil.openSettingPermission(mContext);
                    }

                    @Override
                    public void cancel() {
                    }
                });
            }
        }
    }
    /**
     * 隐藏浮标
     */
    public void removeFloatView() {
        try {
            if(!checkCallOk(false)){
                return;
            }
            FloatViewManager.getInstance(mContext).hidFloat();
            L.e(TAG,"浮点隐藏了");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 资源回收
     */
    public void recycle() {
        try {
            if(!checkCallOk(false)){
                return;
            }
            onLogoutListener=null;//登出监听置null
            //消耗重力感应监听
            OrientationSensorManager.getInstance(mContext).onDestroy();
            logoutExecute(OnLogoutListener.TYPE_NORMAL_LOGOUT);
            // 移除浮标
            removeLoginListener(onLoginListener);
            LoginControl.clearLogin();
            FloatViewManager.getInstance(mContext).removeFloat();
            Intent intent = new Intent(mContext,
                    HuosdkService.class);
            mContext.stopService(intent);
            mContext = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取用户注册的登陆监听
     * @return
     */
    public OnLoginListener getOnLoginListener() {
        return onLoginListener;
    }

    /**
     * 检查调用是否正常，是否在主线程调用，是否进行初始化
     * @param requestInitSuccess
     * @return true 调用ok  false 调用不正常
     */
    private boolean checkCallOk(boolean requestInitSuccess){
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new RuntimeException("未在主线程调用此方法！！！！");
        }
        if(mContext==null){
            throw new RuntimeException("请在调用initSdk方法后调用此方法！！！！");
        }
        if(requestInitSuccess&&!initSuccess){
            T.s(mContext,"初始化失败，请重新打开应用");
            return false;
        }
        return true;
    }
}
