package com.alien.crack_wechat_robot.model;

public class WechatMessage {

    public String content;
    public String receiverWxId;

    public WechatMessage(){}

    public WechatMessage(String content, String receiverWxId) {
        this.content = content;
        this.receiverWxId = receiverWxId;
    }

    @Override
    public String toString() {
        return "WechatMessage{" +
                "content='" + content + '\'' +
                ", receiverWxId='" + receiverWxId + '\'' +
                '}';
    }
}
