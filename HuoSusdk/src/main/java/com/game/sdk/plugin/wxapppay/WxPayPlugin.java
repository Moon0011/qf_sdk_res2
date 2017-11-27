package com.game.sdk.plugin.wxapppay;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.game.sdk.log.L;
import com.game.sdk.util.MResource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by liu hong liang on 2017/6/7.
 * 微信apk 插件支付管理类
 * 调起微信支付：
 * 1.判断是否安装支付插件apk
 * 安装了，直接调起
 * 未安装提示安装微信支付插件（是否需要对话框）
 */

public class WxPayPlugin {
    private static final String TAG = WxPayPlugin.class.getSimpleName();
    public static final int REQUEST_WX_PAY_CODE=1001;

    /**
     * 启动微信支付，结果将在传入activity的onActivityResult中接收
     *
     * @param activity
     * @param apkPackageName 插件apk的包名，即对应的app包名
     * @param token          支付的参数
     */
    public static void startWxPay(Activity activity, String apkPackageName, String token) {
        try {
            Intent intent = new Intent();
            intent.setClassName(apkPackageName, apkPackageName+".wxapi.WxPayPluginActivity");
            intent.putExtra("token", token);
            intent.putExtra("packageName", apkPackageName);
            activity.startActivityForResult(intent, REQUEST_WX_PAY_CODE);
        } catch (Exception e) {//启动失败，没有安装插件apk，需要安装
            e.printStackTrace();
            //提示安装插件
            showInstallHint(activity,apkPackageName);
        }
    }
    private static void showInstallHint(final Activity activity, final String apkPackageName){
        final Dialog dialog= new Dialog(activity, MResource.getIdByName(activity,"R.style.huo_sdk_dialog_bg_style"));
        View view = LayoutInflater.from(activity).inflate(MResource.getIdByName(activity,"R.layout.huo_sdk_hint_install_wxpay"), null);
        view.findViewById(MResource.getIdByName(activity,"R.id.huo_sdk_btn_install")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                installAssetsApk(activity,apkPackageName);
                dialog.dismiss();
            }
        });
        //设置view
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
    private static void installAssetsApk(Activity activity, String apkPackageName) {
        try {
            String fileName = apkPackageName.replace(".", "") + ".bin";
            L.e(TAG,"apk file="+fileName);
            AssetManager assets = activity.getAssets();
            InputStream stream = assets.open(fileName);
            if (stream == null) {
                Log.w(TAG, "no file");
                return;
            }
            String folder = activity.getExternalCacheDir().getAbsolutePath();
            File f = new File(folder);
            if (!f.exists()) {
                f.mkdir();
            }
            String apkPath = getDiskCacheDir(activity) + apkPackageName.replace(".", "") + ".apk";
            File file = new File(apkPath);
            //创建apk文件
            file.createNewFile();
            //将资源中的文件重写到sdcard中
            //<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
            writeStreamToFile(stream, file);
            //安装apk
            installApk(activity,apkPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeStreamToFile(InputStream stream, File file) {
        try {
            OutputStream output = null;
            try {
                output = new FileOutputStream(file);
            } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            try {
                try {
                    final byte[] buffer = new byte[1024];
                    int read;
                    while ((read = stream.read(buffer)) != -1)
                        output.write(buffer, 0, read);
                    output.flush();
                } finally {
                    output.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static String getDiskCacheDir(Context context) {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }

    private static void installApk(Activity activity, String apkPath) {
        try {
            Log.v(TAG, apkPath);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(new File(apkPath)),
                    "application/vnd.android.package-archive");
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}