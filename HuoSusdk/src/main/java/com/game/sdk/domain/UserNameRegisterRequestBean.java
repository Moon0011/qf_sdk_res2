package com.game.sdk.domain;

/**
 * Created by liu hong liang on 2016/11/12.
 */

public class UserNameRegisterRequestBean extends BaseRequestBean{
    private String username;//	是	STRING	用户名，注册用户名
    private String password;//	是	STRING	密码，注册密码
    private String introducer;//	否	STRING	介绍人 2017-03-11 吉米项目添加
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIntroducer() {
        return introducer;
    }

    public void setIntroducer(String introducer) {
        this.introducer = introducer;
    }
}
