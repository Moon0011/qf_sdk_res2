package com.game.sdk.domain;

/**
 * Created by liu hong liang on 2016/11/12.
 */

public class SdkPayRequestBean extends BaseRequestBean {
    private CustomPayParam orderinfo;
    private RoleInfo roleinfo =new RoleInfo();

    public CustomPayParam getOrderinfo() {
        return orderinfo;
    }

    public void setOrderinfo(CustomPayParam orderinfo) {
        this.orderinfo = orderinfo;
    }

    public RoleInfo getRoleinfo() {
        return roleinfo;
    }

    public void setRoleinfo(RoleInfo roleinfo) {
        this.roleinfo = roleinfo;
    }
}
