package com.game.sdk.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.game.sdk.domain.NotProguard;
import com.game.sdk.domain.Notice;

/**
 * author janecer 2014-7-23上午9:41:45
 */
@NotProguard
public class DialogUtil {

    private static Dialog dialog;// 显示对话框
    private static ImageView iv_pd;// 待旋转动画
    private static TextView tv_msg;// 消息
    private static View view;

    /**
     * 显示对话框
     */
    private static void init(Context context) {
        dialog = new Dialog(context, MResource.getIdByName(context, "style",
                "huo_sdk_customDialog"));
        view = LayoutInflater.from(context).inflate(
                MResource.getIdByName(context, "layout", "huo_sdk_dialog_loading"), null);
        iv_pd = (ImageView) view.findViewById(MResource.getIdByName(context,
                "id", "huo_sdk_iv_circle"));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        tv_msg = (TextView) view.findViewById(MResource.getIdByName(context,
                "id", "huo_sdk_tv_msg"));
        dialog.setContentView(view);
    }

    /**
     * 显示对话框
     *
     * @param context
     * @param msg
     */
    public static void showDialog(Context context, String msg) {
        try {
            init(context);
            tv_msg.setText(msg);// 显示进度信息
            if (null != dialog && !dialog.isShowing()) {
                iv_pd.startAnimation(rotaAnimation());
                dialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showDialog(Context ctx, boolean cansable, String msg) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        init(ctx);
        tv_msg.setText(msg);// 显示进度信息
        if (null != dialog && !dialog.isShowing()) {
            dialog.setCancelable(cansable);
            iv_pd.startAnimation(rotaAnimation());
            dialog.show();
        }
    }

    /**
     * 隐藏对话框
     */
    public static void dismissDialog() {
        try {
            if (null != dialog && dialog.isShowing()) {
                dialog.dismiss();
                iv_pd.clearAnimation();
                dialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 旋转动画
     *
     * @return
     */
    public static Animation rotaAnimation() {
        RotateAnimation ra = new RotateAnimation(0, 355,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        ra.setInterpolator(new LinearInterpolator());
        ra.setDuration(888);
        ra.setRepeatCount(-1);
        ra.setStartOffset(0);
        ra.setRepeatMode(Animation.RESTART);
        return ra;
    }

    /**
     * 判断对话框是否是显示状态
     *
     * @return
     */
    public static boolean isShowing() {
        if (null != dialog) {
            return dialog.isShowing();
        }
        return false;
    }

    /**
     * 弹出公告框
     *
     * @param notice
     */
    public static void showNoticeDialog(final Context context, Notice notice) {

        notice = new Notice("游戏公告","游戏公告游戏公告游戏公告游戏公告游戏公告游戏公告游戏公告","2017年11月27日16:02:00");
        if (notice == null || TextUtils.isEmpty(notice.getContent())) return;

        Log.e("notice", " notice.title:" + notice.getTitle() + " | notice.setContent:" + notice.getContent());
        if (context instanceof Activity) {

            final Notice finalNotice = notice;
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    final Dialog dialog = new Dialog(context, MResource.getIdByName(context,
                            "style", "huo_sdk_customDialog"));
                    View notcieView = LayoutInflater.from(context)
                            .inflate(MResource.getIdByName(context, "layout", "huo_sdk_dialog_notice"), null);
                    TextView title = (TextView) notcieView.findViewById(MResource.getIdByName(context,
                            "id", "huo_sdk_title_text"));
                    TextView time = (TextView) notcieView.findViewById(MResource.getIdByName(context,
                            "id", "huo_sdk_time_text"));
                    TextView content = (TextView) notcieView.findViewById(MResource.getIdByName(context,
                            "id", "huo_sdk_content_text"));
                    TextView confirm = (TextView) notcieView.findViewById(MResource.getIdByName(context,
                            "id", "huo_sdk_confirm_tv"));
                    title.setText(finalNotice.getTitle());
                    time.setText(finalNotice.getTime());
                    content.setText(finalNotice.getContent());
                    confirm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                        }
                    });

                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setContentView(notcieView);

                    if (!dialog.isShowing()) {
                        dialog.show();
                    }
                }
            });
        }
    }


}
