package com.game.sdk.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * Created by liu hong liang on 2017/4/27.
 */

public class BaseActivity extends Activity {
    private static final String TAG = BaseActivity.class.getSimpleName();
    private View titleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置为横屏模式
//        setRequestedOrientation(HuosdkInnerManager.getInstance().getScreenOrientation());
    }

    /**
     * 改变标题栏显示状态
     * @param show
     */
    public void changeTitleStatus(boolean show){
        if(titleView==null){
            Log.e(TAG,"没有设置titleView");
            return;
        }
        if(show){
            titleView.setVisibility(View.VISIBLE);
        }else{
            titleView.setVisibility(View.GONE);
        }
    }

    /**
     * 设置标题栏view
     * @param titleView
     */
    public void setTitleView(View titleView){
        this.titleView=titleView;
    }



}
