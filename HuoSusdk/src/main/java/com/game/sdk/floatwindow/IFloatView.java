package com.game.sdk.floatwindow;

/**
 * Created by liu hong liang on 2017/5/10.
 */

public interface IFloatView {

    // 移除悬浮窗口
    void removeFloat();

    // 显示悬浮窗口
    void showFloat();

    // 移除悬浮窗口
    void hidFloat();
    void openucenter();
    //设置初始位置
    void setInitXY(int x,int y);
}
