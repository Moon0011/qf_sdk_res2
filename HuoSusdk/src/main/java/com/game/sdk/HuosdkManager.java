package com.game.sdk;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.game.sdk.domain.CustomPayParam;
import com.game.sdk.domain.NotProguard;
import com.game.sdk.domain.RoleInfo;
import com.game.sdk.domain.SubmitRoleInfoCallBack;
import com.game.sdk.listener.OnInitSdkListener;
import com.game.sdk.listener.OnLoginListener;
import com.game.sdk.listener.OnLogoutListener;
import com.game.sdk.listener.OnPaymentListener;
import com.game.sdk.log.L;

/**
 * 对外提供的接口api单例类
 */
public final class HuosdkManager {
    private static final String TAG = HuosdkManager.class.getSimpleName();
    private static HuosdkManager instance;
    private static HuosdkInnerManager sdkInnerManager;

    // 单例模式
    @NotProguard
    public static synchronized HuosdkManager getInstance() {
        Log.d("huosdk","HuosdkManager getInstance");
        if (Looper.myLooper() != Looper.getMainLooper()) {
            L.e(TAG, "实例化失败,未在主线程调用");
            return null;
        }
        if (null == instance) {
            instance = new HuosdkManager();
            sdkInnerManager = HuosdkInnerManager.getInstance();
        }
        return instance;
    }
    @NotProguard
    private HuosdkManager() {
    }

    /**
     * 设置上下文对象，默认在初始化时设置，如果中途有替换，再此替换
     * @param context
     */
    @NotProguard
    public void setContext(Context context){
        sdkInnerManager.setContext(context);
    }

    /**
     * 设置屏幕方向属性，是否是竖屏
     * @param isPortrait
     */
    @NotProguard
    public void setScreenOrientation(boolean isPortrait){
        Log.d("huosdk","HuosdkManager setScreenOrientation:"+isPortrait);
        sdkInnerManager.setScreenOrientation(isPortrait);
    }
    /**
     * 初始化sdk
     * @param context 上下文对象
     * @param onInitSdkListener 回调监听
     */
    @NotProguard
    public void initSdk(Context context,OnInitSdkListener onInitSdkListener){
        Log.d("huosdk","HuosdkManager initSdk");
        sdkInnerManager.initSdk(context,onInitSdkListener);
    }

    /**
     * 设置浮点的位置信息
     * @param x  横向坐标，浮点根据x靠屏幕左边还是右边自动靠边
     * @param y  竖直方向坐标
     */
    @NotProguard
    public void setFloatInitXY(int x,int y){
        Log.d("huosdk","HuosdkManager setFloatInitXY x="+x+" y="+y);
        sdkInnerManager.setFloatInitXY(x,y);
    }

    /**
     * 设置是否使用直接登录
     * @param directLogin 是否直接登陆 true在第一次登陆的时候，系统没有找到登陆的账号记录，则自动获取账号并登陆
     */
    @NotProguard
    public void setDirectLogin(boolean directLogin) {
        Log.d("huosdk","HuosdkManager setDirectLogin:"+directLogin);
        sdkInnerManager.setDirectLogin(directLogin);
    }
    /**
     * 提交用户角色信息
     * @param roleInfo 角色信息类
     * @param submitRoleInfoCallBack 结果回调
     */
    @NotProguard
    public void setRoleInfo(RoleInfo roleInfo, SubmitRoleInfoCallBack submitRoleInfoCallBack) {
        Log.d("huosdk","HuosdkManager setRoleInfo");
        sdkInnerManager.setRoleInfo(roleInfo,submitRoleInfoCallBack);
    }

    /**
     * 退出登陆
     */
    @NotProguard
    public void logout(){
        Log.d("huosdk","HuosdkManager logout");
        sdkInnerManager.logout();
    }
    /**
     * 添加登陆
     */
    @NotProguard
    public void addLogoutListener(OnLogoutListener onLogoutListener) {
        Log.d("huosdk","HuosdkManager addLogoutListener");
        sdkInnerManager.addLogoutListener(onLogoutListener);
    }
    /**
     * 打开用户中心
     */
    @NotProguard
    public void openUcenter() {
        Log.d("huosdk","HuosdkManager openUcenter");
        sdkInnerManager.openUcenter();
    }

    /**
     * 显示登录
     */
    @NotProguard
    public void showLogin(boolean isShowQuikLogin) {
        Log.d("huosdk","HuosdkManager showLogin");
       sdkInnerManager.showLogin(isShowQuikLogin);
    }
    /**
     * 切换账号
     */
    @NotProguard
    public void switchAccount(){
        Log.d("huosdk","HuosdkManager switchAccount");
        sdkInnerManager.switchAccount();
    }
    /**
     * 注册一个登录监听，需要在不使用的时候解除监听，例如onDestory方法中解除
     * @param  onLoginListener 登陆监听
     */
    @NotProguard
    public void addLoginListener(OnLoginListener onLoginListener) {
        Log.d("huosdk","HuosdkManager addLoginListener");
        sdkInnerManager.addLoginListener(onLoginListener);
    }
    /**
     * 启动支付
     * @param payParam        支付参数
     * @param paymentListener 支付回调监听
     */
    @NotProguard
    public void showPay(CustomPayParam payParam, OnPaymentListener paymentListener) {
        Log.d("huosdk","HuosdkManager showPay");
        sdkInnerManager.showPay(payParam,paymentListener);
    }
    /**
     * 显示浮标
     */
    @NotProguard
    public void showFloatView() {
        Log.d("huosdk","HuosdkManager showFloatView");
        sdkInnerManager.showFloatView();
    }
    /**
     * 隐藏浮标
     */
    @NotProguard
    public void removeFloatView() {
        Log.d("huosdk","HuosdkManager removeFloatView");
        sdkInnerManager.removeFloatView();
    }
    /**
     * 资源回收
     */
    @NotProguard
    public void recycle() {
        Log.d("huosdk","HuosdkManager recycle");
        sdkInnerManager.recycle();
    }
}
