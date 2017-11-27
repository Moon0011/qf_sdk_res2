package com.game.sdk.plugin.wxapppay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.game.sdk.SdkConstant;
import com.game.sdk.domain.NotProguard;
import com.game.sdk.domain.PayResultBean;
import com.game.sdk.log.L;
import com.game.sdk.pay.IPayListener;
import com.game.sdk.plugin.IHuoPay;

import java.util.List;

/**
 * Created by liu hong liang on 2017/6/7.
 * 通过调用插件apk 或者对应app的微信支付实现
 */
@NotProguard
public class WxapppayImpl extends IHuoPay {
    private Activity mActivity;
    private String orderId;
    private float money;
    private IPayListener iPayListener;
    @Override
    public void startPay(Activity activity, IPayListener listener, float money, PayResultBean payResultBean) {
        this.iPayListener = listener;
        this.money = money;
        this.mActivity = activity;
        this.orderId = payResultBean.getOrder_id();
        if (!isWeixinAvilible(activity)) {
            Toast.makeText(activity, "未安装微信,请先安装微信！", Toast.LENGTH_SHORT).show();
            return;
        }
        WxPayPlugin.startWxPay(activity, SdkConstant.APP_PACKAGENAME,payResultBean.getToken());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("MainActivity","requestCode="+requestCode+" resultCode="+resultCode+" data="+data);
        if(requestCode==WxPayPlugin.REQUEST_WX_PAY_CODE){
            if(data!=null){
                if(data.getIntExtra("errCode",-1)==0){
                    if (iPayListener != null) {
                        iPayListener.paySuccess(orderId, money);
                    }
                }else if(data.getIntExtra("errCode",-1)==-2){
                    if (iPayListener != null) {
                        iPayListener.payFail(orderId, money,false,"取消支付");
                    }
                }else{
                    if (iPayListener != null) {
                        iPayListener.payFail(orderId, money,false,"支付失败");
                    }
                }
            }else{
                L.e("WxapppayImpl","未安装wx支付插件");
            }
        }
    }
    public static boolean isWeixinAvilible(Context context) {
        final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mm")) {
                    return true;
                }
            }
        }
        return false;
    }
}
