package com.game.sdk.plugin.nowpay;

import android.app.Activity;
import android.util.Log;

import com.game.sdk.domain.NotProguard;
import com.game.sdk.domain.PayResultBean;
import com.game.sdk.pay.IPayListener;
import com.game.sdk.plugin.IHuoPay;
import com.ipaynow.plugin.api.IpaynowPlugin;
import com.ipaynow.plugin.manager.route.dto.ResponseParams;
import com.ipaynow.plugin.manager.route.impl.ReceivePayResult;

/**
 * Created by liu hong liang on 2017/2/28.
 * 现在支付的实现类
 */

public class NowpayImpl extends IHuoPay implements ReceivePayResult{
    private static final String TAG = NowpayImpl.class.getSimpleName();
    private IpaynowPlugin mIpaynowplugin;
    private Activity mActivity;
    private String orderId;
    private float money;
    private IPayListener iPayListener;
    @Override
    @NotProguard
    public void startPay(Activity activity, IPayListener listener, float money, PayResultBean payResultBean) {
        this.iPayListener = listener;
        this.money = money;
        this.mActivity = activity;
        this.orderId = payResultBean.getOrder_id();
        mIpaynowplugin = IpaynowPlugin.getInstance().init(activity);// 1.插件初始化
        mIpaynowplugin.unCkeckEnvironment();// 无论微信、qq安装与否，网关页面都显示渠道按钮。
        mIpaynowplugin.setCallResultReceiver(this).pay(payResultBean.getToken());
    }

    @Override
    public void onIpaynowTransResult(ResponseParams responseParams) {
        String respCode = responseParams.respCode;
        String errorCode = responseParams.errorCode;
        String errorMsg = responseParams.respMsg;
        Log.d(TAG,"errorCode="+errorCode+" errorMsg="+errorMsg);
        if (iPayListener != null) {
            if (respCode.equals("00")) {
//            temp.append("交易状态:成功");
                if (iPayListener != null) {
                    iPayListener.paySuccess(orderId, money);
                }
            } else if (respCode.equals("02")) {
//            temp.append("交易状态:取消");
                iPayListener.payFail(orderId, money,false,"支付取消");
            } else if (respCode.equals("01")) {
                iPayListener.payFail(orderId, money,false,"支付失败");
//            temp.append("交易状态:失败").append("\n").append("错误码:").append(errorCode).append("原因:" + errorMsg);
            } else if (respCode.equals("03")) {
//                temp.append("交易状态:未知").append("\n").append("原因:" + errorMsg);
                iPayListener.payFail(orderId, money,true,"支付失败");
            } else {
//                temp.append("respCode=").append(respCode).append("\n").append("respMsg=").append(errorMsg);
                iPayListener.payFail(orderId, money,true,"支付失败");
            }
        }
    }
}
