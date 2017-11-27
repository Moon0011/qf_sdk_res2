package com.game.sdk.domain;

import java.util.List;

/**
 * Created by liu hong liang on 2016/11/11.
 */

public class LoginResultBean {
    private String mem_id;	//STRING	用户在平台的用户ID
    private String cp_user_token;//	STRING	CP用user_token
    private String agentgame;//	STRING	渠道游戏编号
    public Notice notice; //登录后通知 -2017年1月17日15:14:15 新加
    private List<UserName> userlist;//用户名列表
    public Notice getNotice() {
        return notice;
    }

    public List<UserName> getUserlist() {
        return userlist;
    }

    public void setUserlist(List<UserName> userlist) {
        this.userlist = userlist;
    }

    public void setNotice(Notice notice) {
        this.notice = notice;
    }

    public String getAgentgame() {
        return agentgame;
    }

    public void setAgentgame(String agentgame) {
        this.agentgame = agentgame;
    }

    public String getCp_user_token() {
        return cp_user_token;
    }

    public void setCp_user_token(String cp_user_token) {
        this.cp_user_token = cp_user_token;
    }

    public String getMem_id() {
        return mem_id;
    }

    public void setMem_id(String mem_id) {
        this.mem_id = mem_id;
    }
    public static class UserName{
        private String username;//用户名
        public String getUsername() {
            return username;
        }
        public void setUsername(String username) {
            this.username = username;
        }

        public UserName(String username) {
            this.username = username;
        }

    }
}
