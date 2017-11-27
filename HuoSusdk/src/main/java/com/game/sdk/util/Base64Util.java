package com.game.sdk.util;

import android.util.Base64;

import com.game.sdk.domain.NotProguard;

/**
 * Created by liu hong liang on 2016/11/14.
 */
@NotProguard
public class Base64Util {
    public final static String CHARACTER="utf-8";
    public static String encode(String data){
        return Base64.encodeToString(data.getBytes(),Base64.NO_WRAP);
    }
    public static String encode(byte[] data){
        return Base64.encodeToString(data,Base64.NO_WRAP);
    }
    public static byte[] decode(String data){
        return Base64.decode(data.getBytes(),Base64.NO_WRAP);
    }
    public static String createBigSmallLetterStrOrNumberRadom(int num) {
        String str = "";
        for(int i=0;i < num;i++){
            int intVal=(int)(Math.random()*58+65);
            if(intVal >= 91 && intVal <= 96){
                i--;
            }
            if(intVal < 91 || intVal > 96){
                if(intVal%2==0){
                    str += (char)intVal;
                }else{
                    str += (int)(Math.random()*10);
                }
            }
        }
        return str;
    }
}
