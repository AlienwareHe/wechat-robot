package com.alien.crack_wechat_robot.db;

/**
 * 需要同步的群成员列表
 * Created by xuedongchao on 2018/4/23.
 */

public class SyncGroupMemberModel {
    private String username;//微信号
    private String nickname;//微信昵称

    public SyncGroupMemberModel() {
    }

    public SyncGroupMemberModel(String userName, String nickName) {
        this.username = userName;
        this.nickname = nickName;
    }

    public String getUserName() {
        return username;
    }

    public void setUserName(String userName) {
        this.username = userName;
    }

    public String getNickName() {
        return nickname;
    }

    public void setNickName(String nickName) {
        this.nickname = nickName;
    }

    @Override
    public String toString() {
        return "SyncGroupMemberModel{" +
                "userName='" + username + '\'' +
                ", nickName='" + nickname + '\'' +
                '}';
    }
}
