package com.game.sdk.floatwindow;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.game.sdk.HuosdkInnerManager;
import com.game.sdk.SdkConstant;
import com.game.sdk.domain.WebRequestBean;
import com.game.sdk.http.HttpParamsBuild;
import com.game.sdk.http.SdkApi;
import com.game.sdk.ui.FloatWebActivity;
import com.game.sdk.util.DimensionUtil;
import com.game.sdk.util.GsonUtil;
import com.game.sdk.util.MResource;

/**
 * 实名认证的浮点实现
 */
public class IdentifyFloatViewImpl  implements IFloatView{
    //	public static int LEFT_TOP=110;    //左上
//	public static int LEFT_CENTER_VERTICAL=111;  //左中
//	public static int LEFT_BOTTOM=101;  //左下
//	public static int RIGHT_TOP=010; //右上
//	public static int RIGHT_CENTER_VERTICAL=011; //5 右中
//	public static int RIGHT_BOTTOM=001;   //6 右下
    protected static final String TAG = IdentifyFloatViewImpl.class.getSimpleName();
    /**
     * 悬浮view
     **/
    private static IdentifyFloatViewImpl instance = null;
    // 定义浮动窗口布局
    private RelativeLayout mFloatLayout;
    private LinearLayout item_lay, float_item_user_lay, float_item_gift_lay,
            float_item_server_lay, float_item_bbs_lay;
    private WindowManager.LayoutParams wmParams;
    // 创建浮动窗口设置布局参数的对象
    private WindowManager mWindowManager;
    private ImageView mFloatView, float_item_id;
    private Context mContext;
    private final int MOBILE_QUERY = 1;
    private boolean isleft = true;
    //取配置里面的初始化坐标
    private int ViewRawX=SdkConstant.floatInitX;
    private int ViewRawY=SdkConstant.floatInitY;
 	private float beforeX;
    private float beforeY;
    private final int scaledTouchSlop;
    private ViewGroup bottomLayout;
	    //隐藏区域
    private View huo_sdk_hide_area;
    private ImageView huo_sdk_iv_hide;
    private LinearLayout float_item_identify_lay;
	
    private IdentifyFloatViewImpl(Context context) {
        this.mContext = context.getApplicationContext();
        scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    /**
     * @param context
     * @return
     */
    public synchronized static IdentifyFloatViewImpl getInstance(Context context) {
        if (instance == null) {
            instance = new IdentifyFloatViewImpl(context);
        }
        return instance;
    }

    private Handler hendler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MOBILE_QUERY:
                    if (!HuosdkInnerManager.isSwitchLogin){

                        mFloatView.setImageResource(MResource.getIdByName(mContext, "drawable", isleft ? "huo_sdk_pull_left" : "huo_sdk_pull_right"));
                    }else{
                        MResource.loadImgFromSDCard(mFloatView,isleft ? MResource.PATH_FILE_ICON_FLOAT_LEFT
                                : MResource.PATH_FILE_ICON_FLOAT_RIGHT);
                    }


                    item_lay.setVisibility(View.GONE);
                    if (mWindowManager != null && mFloatLayout != null)
                        mWindowManager.updateViewLayout(mFloatLayout, wmParams); // 当10秒到达后，作相应的操作。
                    break;
            }
        }

        ;
    };

    private void createFloatView() {
        if (mFloatLayout == null) {

            // 获取的是WindowManagerImpl.CompatModeWrapper
            mWindowManager = (WindowManager) mContext
                    .getSystemService(mContext.WINDOW_SERVICE);
            initFloatHideBottom();
            initFloatIcon();

        }
    }
    private void initFloatIcon(){
        wmParams = new WindowManager.LayoutParams();
        Log.i(TAG, "mWindowManager--->" + mWindowManager);
        // 设置window type
        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        // 设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888;
        // 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // 调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        // 设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // 获取浮动窗口视图所在布局
        mFloatLayout = (RelativeLayout) inflater.inflate(MResource
                .getIdByName(mContext, MResource.LAYOUT, "huo_sdk_float_layout"), null);
        // 添加mFloatLayout
        mWindowManager.addView(mFloatLayout, wmParams);
        initUI();
        mWindowManager.updateViewLayout(mFloatLayout, wmParams);
    }

    private void initUI() {
        // 浮动窗口按钮
        mFloatView = (ImageView) mFloatLayout.findViewById(MResource
                .getIdByName(mContext, "R.id.huo_sdk_iv_float"));

        item_lay = (LinearLayout) mFloatLayout.findViewById(MResource
                .getIdByName(mContext, "R.id.huo_sdk_item_lay"));
        float_item_id = (ImageView) mFloatLayout.findViewById(MResource
                .getIdByName(mContext,  "R.id.huo_sdk_float_item_id"));
        float_item_user_lay = (LinearLayout) mFloatLayout
                .findViewById(MResource.getIdByName(mContext, "R.id.huo_sdk_float_item_user_lay"));
        float_item_gift_lay = (LinearLayout) mFloatLayout
                .findViewById(MResource.getIdByName(mContext, "R.id.huo_sdk_float_item_gift_lay"));
        float_item_server_lay = (LinearLayout) mFloatLayout
                .findViewById(MResource.getIdByName(mContext, "R.id.huo_sdk_float_item_server_lay"));
        float_item_bbs_lay = (LinearLayout) mFloatLayout.findViewById(MResource
                .getIdByName(mContext, "R.id.huo_sdk_float_item_bbs_lay"));
        float_item_identify_lay = (LinearLayout) mFloatLayout.findViewById(MResource
                .getIdByName(mContext, "R.id.huo_sdk_float_item_identify_lay"));
        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        item_lay.setVisibility(View.VISIBLE);

        if (!HuosdkInnerManager.isSwitchLogin){
            mFloatView.setImageResource(MResource.getIdByName(mContext,
                    "drawable", "huo_sdk_fload"));
        }else{
            MResource.loadImgFromSDCard(mFloatView, MResource.PATH_FILE_ICON_FLOAT);
        }
        // 设置监听浮动窗口的触摸移动
        mFloatView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标


                if (!HuosdkInnerManager.isSwitchLogin){
                    mFloatView.setImageResource(MResource.getIdByName(mContext,
                            "drawable", "huo_sdk_fload"));
                }else{
                    MResource.loadImgFromSDCard(mFloatView, MResource.PATH_FILE_ICON_FLOAT);
                }

                wmParams.alpha = 10;
                wmParams.x = (int) event.getRawX()
                        - mFloatView.getMeasuredWidth() / 2;

                // 减25为状态栏的高度
                wmParams.y = (int) event.getRawY()
                        - mFloatView.getMeasuredHeight() / 2 - 25;
                // 刷新
                mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        beforeX=event.getRawX();
                        beforeY=event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        ViewRawX = (int) event.getRawX()
                                - mFloatView.getMeasuredWidth() / 2;
                        ViewRawY = (int) event.getRawY()
                                - mFloatView.getMeasuredHeight() / 2;
                        //只要发生移动，不响应点击事件
                        if(Math.abs(beforeX-event.getRawX())>=scaledTouchSlop||Math.abs(beforeY-event.getRawY())>=scaledTouchSlop){
                            bottomLayout.setVisibility(View.VISIBLE);
                        }
                        if(isScrollHideArea(ViewRawX,ViewRawY)){
                            huo_sdk_iv_hide.setImageResource(MResource.getIdByName(mContext,"R.drawable.huo_sdk_float_hide_acitve"));
                        }else{
                            huo_sdk_iv_hide.setImageResource(MResource.getIdByName(mContext,"R.drawable.huo_sdk_float_hide_normal"));
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        ViewRawX = (int) event.getRawX()
                                - mFloatView.getMeasuredWidth() / 2;
                        ViewRawY = (int) event.getRawY()
                                - mFloatView.getMeasuredHeight() / 2 - 25;

                        item_lay.setVisibility(View.GONE);
                        pullover(3000);
                        bottomLayout.setVisibility(View.INVISIBLE);
                        if(isScrollHideArea(ViewRawX,ViewRawY)){
                            huo_sdk_iv_hide.setImageResource(MResource.getIdByName(mContext,"R.drawable.huo_sdk_float_hide_normal"));
                            showHideHintDialog();
                        }
                        //只要发生移动，不响应点击事件
                        if(Math.abs(beforeX-event.getRawX())>=scaledTouchSlop||Math.abs(beforeY-event.getRawY())>=scaledTouchSlop){
                            return true;
                        }
                        break;
                }
                return false;
            }
        });

        mFloatView.setOnClickListener(onclick);

        // 礼包
        float_item_gift_lay.setOnClickListener(onclick);
        // 官网
        // game_web.setOnClickListener(onclick);
        // 加群
        // add_Group.setOnClickListener(onclick);
        // 攻略
        float_item_server_lay.setOnClickListener(onclick);
        // // 论坛
        float_item_bbs_lay.setOnClickListener(onclick);
        // 用户
        float_item_user_lay.setOnClickListener(onclick);
        float_item_identify_lay.setOnClickListener(onclick);
        pullover(3000);
        if("187".equals(SdkConstant.PROJECT_CODE)){
            float_item_server_lay.setVisibility(View.VISIBLE);
            float_item_gift_lay.setVisibility(View.VISIBLE);
        }
    }
    private boolean isScrollHideArea(int scrollX,int scrollY){
        int width = DimensionUtil.getWidth(mContext);
        int height =DimensionUtil.getHeight(mContext);
        int xStart=width/2- DimensionUtil.dip2px(mContext,90)-20;
        int xEnd=width/2+ DimensionUtil.dip2px(mContext,90)+20;
        int yStart=height-DimensionUtil.dip2px(mContext,90)-100;
        int yEnd=height;
//        Log.e("hongliang","90dp="+DimensionUtil.dip2px(mContext,90));
//        Log.e("hongliang","width="+width+" height="+height);
//        Log.e("hongliang","xstart="+xStart+" xend="+xEnd);
//        Log.e("hongliang","yStart="+yStart+" yEnd="+yEnd);
//        Log.e("hongliang","scrollX="+scrollX+" scrollY="+scrollY);
        if(scrollX>=xStart&&scrollX<=xEnd&&scrollY>=yStart&&scrollY<=yEnd){//滑动到隐藏区域
            return true;
        }
        return false;
    }

    public void initFloatHideBottom(){
        bottomLayout = (ViewGroup) LayoutInflater.from(mContext).inflate(MResource.getIdByName(mContext, "R.layout.huo_sdk_float_bottom"), null);
        huo_sdk_hide_area = (View) bottomLayout.findViewById(MResource
                .getIdByName(mContext, "R.id.huo_sdk_hide_area"));
        huo_sdk_iv_hide = (ImageView) bottomLayout.findViewById(MResource
                .getIdByName(mContext, "R.id.huo_sdk_iv_hide"));
        WindowManager.LayoutParams bottomLp=new WindowManager.LayoutParams();
        // 设置window type
        bottomLp.type = WindowManager.LayoutParams.TYPE_PHONE;
        // 设置图片格式，效果为背景透明
        bottomLp.format = PixelFormat.RGBA_8888;
        // 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        bottomLp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // 调整悬浮窗显示的停靠位置为左侧置顶
        bottomLp.gravity = Gravity.LEFT | Gravity.TOP;
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        bottomLp.x = 0;
        bottomLp.y = DimensionUtil.getHeight(mContext);
        // 设置悬浮窗口长宽数据
        bottomLp.width = WindowManager.LayoutParams.MATCH_PARENT;
        bottomLp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        bottomLayout.setVisibility(View.GONE);
        mWindowManager.addView(bottomLayout,bottomLp);
    }

    private void pullover(int delayTime) {
        // 传送msg
        int width = mWindowManager.getDefaultDisplay().getWidth();
        wmParams.x = 0;
        wmParams.y = ViewRawY;
        item_lay.setVisibility(View.GONE);
        isleft = true;
        if (ViewRawX > width / 2) {
            wmParams.x = width+500;//获取到的宽度不包括虚拟键的宽度，因限制在了屏幕内，所以，可以加大宽度解决虚拟键隐藏时浮点不靠边问题
            isleft = false;
        }
        mWindowManager.updateViewLayout(mFloatLayout, wmParams);
        hendler.removeMessages(MOBILE_QUERY);
        Message msg = hendler.obtainMessage(MOBILE_QUERY);
        hendler.sendMessageDelayed(msg, delayTime);
    }

    /**
     * 打开用户中心
     */
    public void openucenter() {
        hidFloat();
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(new WebRequestBean()));
        FloatWebActivity.start(mContext, SdkApi.getWebUser(), "用户中心", httpParamsBuild.getHttpParams().getUrlParams().toString(), httpParamsBuild.getAuthkey());
    }

    @Override
    public void setInitXY(int x, int y) {
        ViewRawX=x;
        ViewRawY=y;
    }

    /**
     * 打开实名认证
     */
    public void openIdentify() {
        hidFloat();
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(new WebRequestBean()));
        FloatWebActivity.start(mContext, SdkApi.getWebIdentify(), "实名认证", httpParamsBuild.getHttpParams().getUrlParams().toString(), httpParamsBuild.getAuthkey());
    }
    private void  web(String title,String url){
        hidFloat();
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(new WebRequestBean()));
        FloatWebActivity.start(mContext, url, title, httpParamsBuild.getHttpParams().getUrlParams().toString(), httpParamsBuild.getAuthkey());
    }
    private View.OnClickListener onclick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == mFloatView.getId()) {
                item_lay.setVisibility(View.VISIBLE);
                return;
            }
            if (v.getId() == float_item_user_lay.getId()) {
                hidFloat();
                HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(new WebRequestBean()));
                FloatWebActivity.start(mContext, SdkApi.getWebUser(), "用户中心", httpParamsBuild.getHttpParams().getUrlParams().toString(), httpParamsBuild.getAuthkey());
//				web("用户中心", url(WebConstant.FLOAT_USER_URL), getUserParams());
                return;
            }
            if (v.getId() == float_item_gift_lay.getId()) {
                web("礼包中心", SdkApi.getWebGift());
                return;
            }
            if (v.getId() == float_item_server_lay.getId()) {
                web("客服中心",SdkApi.getWebHelp());
                return;
            }
            if (v.getId() == float_item_bbs_lay.getId()) {
                hidFloat();
//				web("论坛", url(WebConstant.URL_FLOAT_BBS));
                return;
            }
            if (v.getId() == float_item_identify_lay.getId()) {
                openIdentify();
                return;
            }
        }
    };

    // 移除悬浮窗口
    public void removeFloat() {
        hidFloat();
        instance = null;
    }

    // 显示悬浮窗口
    public void showFloat() {
        if (instance != null) {
            createFloatView();
            pullover(0);
        }
    }
    // 移除悬浮窗口
    public void hidFloat() {
        hendler.removeCallbacksAndMessages(null);
        if (bottomLayout != null) {
            bottomLayout.setVisibility(View.GONE);
            bottomLayout.removeAllViews();

            if (mWindowManager == null) {
                mWindowManager = (WindowManager) mContext
                        .getSystemService(mContext.WINDOW_SERVICE);
            }
            mWindowManager.removeView(bottomLayout);
            bottomLayout = null;
        }
        if (mFloatLayout != null) {
            mFloatLayout.setVisibility(View.GONE);
            mFloatLayout.removeAllViews();

            if (mWindowManager == null) {
                mWindowManager = (WindowManager) mContext
                        .getSystemService(mContext.WINDOW_SERVICE);
            }
            mWindowManager.removeView(mFloatLayout);
            mFloatLayout = null;
        }
    }
    public void showHideHintDialog(){
        //先移除底部隐藏view和浮点
        hidFloat();
        //创建隐藏提示对话框并显示
        final ViewGroup hideHintLayout = (ViewGroup) LayoutInflater.from(mContext).inflate(MResource.getIdByName(mContext, "R.layout.huo_sdk_float_hide_dialog"), null);
        View huo_sdk_btn_cancel =  hideHintLayout.findViewById(MResource
                .getIdByName(mContext, "R.id.huo_sdk_btn_cancel"));
        View huo_sdk_btn_confirm = hideHintLayout.findViewById(MResource
                .getIdByName(mContext, "R.id.huo_sdk_btn_confirm"));
        ImageView huo_sdk_iv_hide_anim = (ImageView) hideHintLayout.findViewById(MResource
                .getIdByName(mContext, "R.id.huo_sdk_iv_hide_anim"));
        WindowManager.LayoutParams bottomLp=new WindowManager.LayoutParams();
        // 设置window type
        bottomLp.type = LayoutParams.TYPE_PHONE;
        // 设置图片格式，效果为背景透明
        bottomLp.format = PixelFormat.RGBA_8888;
        // 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
//        bottomLp.flags = LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        // 调整悬浮窗显示的停靠位置为左侧置顶
        bottomLp.gravity = Gravity.CENTER;
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        bottomLp.x = 0;
        bottomLp.y = 0;
        // 设置悬浮窗口长宽数据
        bottomLp.width = LayoutParams.WRAP_CONTENT;
        bottomLp.height = LayoutParams.WRAP_CONTENT;
        mWindowManager.addView(hideHintLayout,bottomLp);
        AnimationDrawable animationDrawable = (AnimationDrawable) huo_sdk_iv_hide_anim.getDrawable();
        animationDrawable.start();
        huo_sdk_btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hideHintLayout != null) {
                    hideHintLayout.setVisibility(View.GONE);
                    hideHintLayout.removeAllViews();

                    if (mWindowManager == null) {
                        mWindowManager = (WindowManager) mContext
                                .getSystemService(mContext.WINDOW_SERVICE);
                    }
                    mWindowManager.removeView(hideHintLayout);
                }
                showFloat();
            }
        });
        huo_sdk_btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SdkConstant.isShowFloat=false;
                if (hideHintLayout != null) {
                    hideHintLayout.setVisibility(View.GONE);
                    hideHintLayout.removeAllViews();

                    if (mWindowManager == null) {
                        mWindowManager = (WindowManager) mContext
                                .getSystemService(mContext.WINDOW_SERVICE);
                    }
                    mWindowManager.removeView(hideHintLayout);
                }
            }
        });
    }
}
