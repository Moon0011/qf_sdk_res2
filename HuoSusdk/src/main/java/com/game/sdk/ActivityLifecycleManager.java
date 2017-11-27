package com.game.sdk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.game.sdk.domain.NotProguard;
import com.game.sdk.floatwindow.FloatViewManager;
import com.game.sdk.floatwindow.OrientationSensorManager;
import com.game.sdk.log.L;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by liu hong liang on 2017/6/16.
 * activity生命周期管理
 */
@NotProguard
public class ActivityLifecycleManager implements Application.ActivityLifecycleCallbacks{
    private static final String TAG = ActivityLifecycleManager.class.getSimpleName();
    private static final String IGNORE_ORIENTATION="noChangeScreenOrientation";
    List<Activity> activityList;
    HuosdkInnerManager huosdkInnerManager;
    private OrientationSensorManager sensorManager;

    public void startActivityLifecycleManager( Context context){
        activityList=new LinkedList<>();
        sensorManager = OrientationSensorManager.getInstance(context);
        sensorManager.setPhoneReversalListener(new OrientationSensorManager.PhoneReversalListener() {
            @Override
            public void onReversalChanged() {
                L.e(TAG,"onReversalChanged："+SdkConstant.isShowFloat);

                if(!SdkConstant.isShowFloat){
                    SdkConstant.isShowFloat=true;
                    FloatViewManager.getInstance(huosdkInnerManager.getContext()).showFloat();
                }else{
                    SdkConstant.isShowFloat=false;
                    FloatViewManager.getInstance(huosdkInnerManager.getContext()).hidFloat();
                }
            }
        });
        ((Application)context.getApplicationContext()).registerActivityLifecycleCallbacks(this);
        huosdkInnerManager = HuosdkInnerManager.getInstance();
        L.e(TAG,"ActivityLifecycleManager start");
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if(!IGNORE_ORIENTATION.equals(activity.getTitle())){//不支持改变屏幕方向
            activity.setRequestedOrientation(HuosdkInnerManager.getInstance().getScreenOrientation());
            L.e(TAG,"设置了屏幕方向："+activity+" ->"+ HuosdkInnerManager.getInstance().getScreenOrientation());
        }
        L.e(TAG,"onActivityCreated="+activity+" title="+activity.getTitle());
    }

    @Override
    public void onActivityStarted(Activity activity) {
        L.e(TAG,"onActivityStarted="+activity);
        activityList.add(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        L.e(TAG,"onActivityResumed="+activity);
        if(activity== huosdkInnerManager.getContext()&&sensorManager!=null){
            sensorManager.onResume();
            HuosdkManager.getInstance().showFloatView();
            L.e(TAG,"onActivityResumed sensorManager onResume "+activity);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        L.e(TAG,"onActivityPaused="+activity);
        if(activity== huosdkInnerManager.getContext()&&sensorManager!=null){
            HuosdkManager.getInstance().removeFloatView();
            sensorManager.onPause();
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        L.e(TAG,"onActivityStopped="+activity);
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        L.e(TAG,"onActivitySaveInstanceState="+activity);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        L.e(TAG,"onActivityDestroyed="+activity);
        activityList.remove(activity);
    }
    private boolean isOnlyActivity(Activity activity){
        if(activityList!=null&&activityList.size()==1&&activity==activityList.get(0)){
            return true;
        }
        return false;
    }
}
