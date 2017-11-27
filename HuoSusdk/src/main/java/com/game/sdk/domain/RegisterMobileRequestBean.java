package com.game.sdk.domain;

/**
 * Created by liu hong liang on 2016/11/12.
 */

public class RegisterMobileRequestBean extends BaseRequestBean {
    private String mobile;//	是	STRING	玩家注册手机号
    private String password;//	是	STRING	密码，注册密码
    private String smstype;//	是	STRING	短信类型 1 注册 2 登陆 3 修改密码 4 信息变更
    private String smscode;//	是	STRING	短信校验码
    private String introducer;//	否	STRING	介绍人 2017-03-11 吉米项目添加

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSmstype() {
        return smstype;
    }

    public void setSmstype(String smstype) {
        this.smstype = smstype;
    }

    public String getSmscode() {
        return smscode;
    }

    public void setSmscode(String smscode) {
        this.smscode = smscode;
    }

    public String getIntroducer() {
        return introducer;
    }

    public void setIntroducer(String introducer) {
        this.introducer = introducer;
    }
}
