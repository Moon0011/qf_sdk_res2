package com.game.sdk.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.webkit.WebView;

import com.game.sdk.domain.DeviceBean;
import com.game.sdk.domain.NotProguard;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.UUID;

/**
 * Created by liu hong liang on 2016/11/11.
 */
@NotProguard
public class DeviceUtil {
    public static DeviceBean getDeviceBean(Context context){
        String deviceId=getDeviceId(context);
        String mac=getLocalMac(context);
        if(TextUtils.isEmpty(mac)){
            mac="null";
        }
        if(TextUtils.isEmpty(deviceId)){
            deviceId="null";
        }
        DeviceBean deviceBean=new DeviceBean();
        deviceBean.setUserua(getUserUa(context));
        deviceBean.setLocal_ip(getHostIP());
        deviceBean.setMac(mac);
        deviceBean.setDevice_id(deviceId);
        //设备信息: 电话号码，用户系统版本，MAC地址，机器码，机型，运营商
        StringBuffer deviceInfoSb=new StringBuffer();
        deviceInfoSb.append(getPhoneNum(context)).append("||android").
                append(Build.VERSION.RELEASE).append("||").
                append(mac).append("||").
                append(deviceId).append("||").
                append(getPhoneModel()).append("||").
                append(getOperators(context));
        deviceBean.setDeviceinfo(deviceInfoSb.toString());
        return deviceBean;
    }
    public static String getPhoneNum(Context context){
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNum=telephonyManager.getLine1Number();
        if(TextUtils.isEmpty(phoneNum)){
            return "null";
        }else{
            return phoneNum;
        }
    }
    /**
     * 获取SIM卡运营商
     *
     * @param context
     * @return
     */
    public static String getOperators(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String operator = null;
        String IMSI = tm.getSubscriberId();
        if (IMSI == null || IMSI.equals("")) {
            return operator;
        }
        if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {
            operator = "中国移动";
        } else if (IMSI.startsWith("46001")) {
            operator = "中国联通";
        } else if (IMSI.startsWith("46003")) {
            operator = "中国电信";
        }
        if(TextUtils.isEmpty(operator)){
            return "null";
        }else{
            return operator;
        }
    }

    /**
     * 手机型号
     *
     * @return
     */
    public static String getPhoneModel() {
        if(TextUtils.isEmpty(Build.MODEL)){
            return "null";
        }else{
            return Build.MODEL;
        }
    }
    /**
     * 获取ua信息
     *
     * @throws UnsupportedEncodingException
     */
    public static String getUserUa(Context context) {
        WebView webview = new WebView(context);
        webview.layout(0, 0, 0, 0);
        String str = webview.getSettings().getUserAgentString();
        return str;
    }
    /**
     * 获取ip地址
     * @return
     */
    public static String getHostIP() {
        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return hostIp;
    }
    // IMEI码
    private static String getIMIEStatus(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        return deviceId;
    }

    // Mac地址
    private static String getLocalMac(Context context) {
        WifiManager wifi = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }

    // Android Id
    private static String getAndroidId(Context context) {
        String androidId = Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return androidId;
    }
    public static boolean isPhone(Context context) {
        TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        int type = telephony.getPhoneType();
        if(type == 0) {
            return false;
        } else {
            return true;
        }
    }
    /**
     * 获取设备ID
     * @param context
     * @return
     */
    public static String getDeviceId(Context context) {
        String deviceId;
        if(isPhone(context)) {//是通信设备使用设备id
            TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            deviceId = telephony.getDeviceId();
        } else {//使用android_id
            deviceId = Settings.Secure.getString(context.getContentResolver(), "android_id");
        }
        if (!TextUtils.isEmpty(deviceId)) {
            return deviceId;
        }
        //使用mac地址
        try {
            deviceId = getLocalMac(context).replace(":", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(deviceId)) {
            return deviceId;
        }
        //使用UUID
        UUID uuid = UUID.randomUUID();
        deviceId = uuid.toString().replace("-", "");
        return deviceId;
    }


    /**
     * 打开权限设置界面
     */
    public static void openSettingPermission(Context context) {
        try {
            if(MiuiDeviceUtil.isMiui()){
                MiuiDeviceUtil.openMiuiPermissionActivity(context);
            }else{
                Intent intent1 = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                intent1.setData(uri);
                context.startActivity(intent1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 获取当前应用程序的版本号
     *
     * @return
     * @author wangjie
     */
    public static int getAppVersionCode(Context context) {
        int version = 1;
        try {
            version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }
    /**
     *   判断是魅族操作系统
     * <h3>Version</h3> 1.0
     * <h3>CreateTime</h3> 2016/6/18,9:43
     * <h3>UpdateTime</h3> 2016/6/18,9:43
     * <h3>CreateAuthor</h3> vera
     * <h3>UpdateAuthor</h3>
     * <h3>UpdateInfo</h3> (此处输入修改内容,若无修改可不写.)
     * @return true 为魅族系统 否则不是
     */
    public static boolean isMeizuFlymeOS() {
/* 获取魅族系统操作版本标识*/
        String meizuFlymeOSFlag  = getSystemProperty("ro.build.display.id","");
        if (TextUtils.isEmpty(meizuFlymeOSFlag)){
            return false;
        }else if (meizuFlymeOSFlag.contains("flyme") || meizuFlymeOSFlag.toLowerCase().contains("flyme")){
            return  true;
        }else {
            return false;
        }
    }
    /**
     *   获取系统属性
     * <h3>Version</h3> 1.0
     * <h3>CreateTime</h3> 2016/6/18,9:35
     * <h3>UpdateTime</h3> 2016/6/18,9:35
     * <h3>CreateAuthor</h3> vera
     * <h3>UpdateAuthor</h3>
     * <h3>UpdateInfo</h3> (此处输入修改内容,若无修改可不写.)
     * @param key  ro.build.display.id
     * @param defaultValue 默认值
     * @return 系统操作版本标识
     */
    private static String getSystemProperty(String key, String defaultValue) {
        try {
            Class<?> clz = Class.forName("android.os.SystemProperties");
            Method get = clz.getMethod("get", String.class, String.class);
            return (String)get.invoke(clz, key, defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
