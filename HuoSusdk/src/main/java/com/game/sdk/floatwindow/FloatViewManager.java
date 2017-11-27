package com.game.sdk.floatwindow;

import android.content.Context;
import com.game.sdk.SdkConstant;
import com.game.sdk.db.LoginControl;
import com.game.sdk.domain.WebRequestBean;
import com.game.sdk.http.HttpParamsBuild;
import com.game.sdk.http.SdkApi;
import com.game.sdk.ui.FloatWebActivity;
import com.game.sdk.util.GsonUtil;

/**
 * Created by liu hong liang on 2017/5/10.
 * 浮点view管理器
 */

public class FloatViewManager {
    private static FloatViewManager instance = null;
    private  Context mContext;
    private IFloatView iFloatView;
    private FloatViewManager(Context context) {
        this.mContext = context.getApplicationContext();
        if("0".equals(SdkConstant.SHOW_INDENTIFY)){//不需要实名认证
            iFloatView=FloatViewImpl.getInstance(mContext);
        }else{
            iFloatView=IdentifyFloatViewImpl.getInstance(mContext);
        }
    }

    /**
     * @param context
     * @return
     */
    public synchronized static FloatViewManager getInstance(Context context) {
        if (instance == null) {
            instance = new FloatViewManager(context);
        }
        return instance;
    }
    // 移除悬浮窗口
    public void removeFloat() {
        try {
            iFloatView.removeFloat();
            instance = null;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // 显示悬浮窗口
    public void showFloat() {
        try {
            if (!LoginControl.isLogin()) {
                return;
            }
            iFloatView.showFloat();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    // 移除悬浮窗口
    public void hidFloat() {
        try {
            iFloatView.hidFloat();
            instance = null;
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 打开网页
     */
    public void openUrl(String url,String title){
        hidFloat();
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(new WebRequestBean()));
        FloatWebActivity.start(mContext, url, title, httpParamsBuild.getHttpParams().getUrlParams().toString(), httpParamsBuild.getAuthkey());
    }
    /**
     * 打开用户中心
     */
    public void openucenter() {
        hidFloat();
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(new WebRequestBean()));
        FloatWebActivity.start(mContext, SdkApi.getWebUser(), "用户中心", httpParamsBuild.getHttpParams().getUrlParams().toString(), httpParamsBuild.getAuthkey());
    }
    public void setInitXY(int x,int y){
        if(iFloatView!=null){
            iFloatView.setInitXY(x,y);
        }
    }
}
