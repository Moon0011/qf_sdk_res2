package com.game.sdk.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * @作者: XQ
 * @创建时间：15-7-20 下午10:08
 * @类说明:登录控制类
 */
public class LoginControl {
    public static final String PRFS_USER_TOKEN="prfs_user_token";

    protected static SharedPreferences login_sp =null;
    protected static String mUserToken;

    public static synchronized void init(Context context){
        if(login_sp ==null){
            login_sp = context.getSharedPreferences(LoginControl.class.getName(), Context.MODE_PRIVATE);
        }
    }
    public static void saveUserToken(String userToken){
        if (!TextUtils.isEmpty(userToken)) {
            mUserToken = userToken;
//            login_sp.edit().putString(PRFS_USER_TOKEN, mUserToken).commit();
        }
    }
    public static String getUserToken() {
        return mUserToken;
    }
    public static boolean isLogin(){
        return !TextUtils.isEmpty(getUserToken());
    }
    public static void clearLogin() {
        mUserToken = null;
        if (login_sp!= null) {
            if (login_sp.edit() != null) {
                login_sp.edit().clear().commit();
            }
        }
    }
}
