package com.game.sdk.domain;

/**
 * Created by liu hong liang on 2016/11/10.
 */

public class StartUpBean extends BaseRequestBean {
    public static final String OPEN_CNT="open_cnt";
    public String open_cnt;//	是	INT	打开次数 默认为1

    public StartUpBean() {
        setUser_token(null);
    }

    public String getOpen_cnt() {
        return open_cnt;
    }

    public void setOpen_cnt(String open_cnt) {
        this.open_cnt = open_cnt;
    }
}
