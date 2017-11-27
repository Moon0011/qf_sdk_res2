package com.game.sdk.plugin.alipay;

import android.app.Activity;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.alipay.sdk.app.PayTask;
import com.game.sdk.domain.NotProguard;
import com.game.sdk.domain.PayResultBean;
import com.game.sdk.pay.AliPayResult;
import com.game.sdk.plugin.IHuoPay;
import com.game.sdk.pay.IPayListener;


/**
 * Created by liu hong liang on 2016/10/14.
 */

public class AlipayImpl extends IHuoPay {
    private Activity mActivity;
    private String orderId;
    private float money;
    private IPayListener iPayListener;
    AsyncTask<String, Integer, String> asyncTask;
    private boolean queryOrder=false;
    @Override
    @NotProguard
    public void startPay(Activity activity, IPayListener listener, float money, PayResultBean payResultBean) {
        this.iPayListener = listener;
        this.money = money;
        this.mActivity = activity;
        this.orderId = payResultBean.getOrder_id();
        //异步任务调用
        asyncTask = new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... params) {
                // 构造PayTask 对象
                PayTask alipay = new PayTask(mActivity);
                // 调用支付接口
                return alipay.pay(params[0], true);
            }

            @Override
            protected void onPostExecute(String s) {
                parseResult(s);
            }
        };
        asyncTask.execute(payResultBean.getToken());
    }

    private void parseResult(String result) {
        AliPayResult payResult = new AliPayResult(result);
        /**
         * 同步返回的结果必须放置到服务端进行验证（验证的规则请看https://doc.open.alipay.com/doc2/
         * detail.htm?spm=0.0.0.0.xdvAU6&treeId=59&articleId=103665&
         * docType=1) 建议商户依赖异步通知
         */
        String resultInfo = payResult.getResult();// 同步返回需要验证的信息

        String resultStatus = payResult.getResultStatus();
        // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
        if (TextUtils.equals(resultStatus, "9000")) {
            if (iPayListener != null) {
                iPayListener.paySuccess(orderId, money);
            }
        } else {
            // 判断resultStatus 为非"9000"则代表可能支付失败
            // "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
            if (TextUtils.equals(resultStatus, "8000")) {
                if (iPayListener != null) {
                    iPayListener.payFail(orderId, money,queryOrder, "支付失败");
                }
            } else {
                if (iPayListener != null) {
                    iPayListener.payFail(orderId, money,queryOrder, "用户取消支付");
                }
            }
        }
    }
    @Override
    public void onDestory() {
        //销毁异步任务
        iPayListener = null;
        if (asyncTask != null) {
            try {
                asyncTask.cancel(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
