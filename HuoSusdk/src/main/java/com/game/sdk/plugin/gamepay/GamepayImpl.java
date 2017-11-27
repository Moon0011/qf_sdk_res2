package com.game.sdk.plugin.gamepay;

import android.app.Activity;

import com.game.sdk.domain.NotProguard;
import com.game.sdk.domain.PayResultBean;
import com.game.sdk.plugin.IHuoPay;
import com.game.sdk.pay.IPayListener;


/**
 * Created by liu hong liang on 2016/10/14.
 * 游戏币支付
 */
public class GamepayImpl extends IHuoPay {
    @Override
    @NotProguard
    public void startPay(Activity activity, IPayListener listener, float money, PayResultBean payResultBean) {
        if(listener!=null){
            if("1".equals(payResultBean.getStatus())){
                listener.payFail(payResultBean.getOrder_id(),money,false,"未支付");
            }else if("2".equals(payResultBean.getStatus())){
                listener.paySuccess(payResultBean.getOrder_id(),money);
            }else if("3".equals(payResultBean.getStatus())){
                listener.payFail(payResultBean.getOrder_id(),money,false,"支付失败");
            }
        }
    }
}
