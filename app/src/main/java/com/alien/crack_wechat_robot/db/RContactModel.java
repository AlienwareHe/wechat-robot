package com.alien.crack_wechat_robot.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class RContactModel {

    //数据表：rcontact    (字段 - 不全）
    public String username = ""; //微信 id
    public String alias = ""; //微信号
    private String conRemark = ""; //微信备注
    private String domainList = "";
    public String nickname = "";//微信昵称
    private String pyInitial = "";
    private String quanPin = "";
    private int showHead = 0;
    private int type = 0;
    private String weiboFlag = "";
    private String weiboNickname = "";
    private String conRemarkPYFull = "";
    private String conRemarkPYShort = "";
    private String encryptUsername = "";//加密的wxid
    private int chatroomFlag = 0;
    private int verifyFlag = 0;
    private String contactLabelIds = "";
    private byte[] lvbuff;

    //附加属性
    private int sex;// 0未知 1男 2女
    public String avatar = "";
    private List<String> phone = new ArrayList<>();// 列表中第一条是从联系人列表中获取的电话号码,可能为空;剩余的是备注添加的电话号码
    private String ticketId = "";
    private int contactScene = -1;
    private ArrayList tagList = new ArrayList();
    private int addWay;

    public RContactModel() {
    }

    public RContactModel(List<String> phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getConRemark() {
        return conRemark;
    }

    public void setConRemark(String conRemark) {
        this.conRemark = conRemark;
    }

    public String getDomainList() {
        return domainList;
    }

    public void setDomainList(String domainList) {
        this.domainList = domainList;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPyInitial() {
        return pyInitial;
    }

    public void setPyInitial(String pyInitial) {
        this.pyInitial = pyInitial;
    }

    public String getQuanPin() {
        return quanPin;
    }

    public void setQuanPin(String quanPin) {
        this.quanPin = quanPin;
    }

    public int getShowHead() {
        return showHead;
    }

    public void setShowHead(int showHead) {
        this.showHead = showHead;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getWeiboFlag() {
        return weiboFlag;
    }

    public void setWeiboFlag(String weiboFlag) {
        this.weiboFlag = weiboFlag;
    }

    public String getWeiboNickname() {
        return weiboNickname;
    }

    public void setWeiboNickname(String weiboNickname) {
        this.weiboNickname = weiboNickname;
    }

    public String getConRemarkPYFull() {
        return conRemarkPYFull;
    }

    public void setConRemarkPYFull(String conRemarkPYFull) {
        this.conRemarkPYFull = conRemarkPYFull;
    }

    public String getConRemarkPYShort() {
        return conRemarkPYShort;
    }

    public void setConRemarkPYShort(String conRemarkPYShort) {
        this.conRemarkPYShort = conRemarkPYShort;
    }

    public String getEncryptUsername() {
        return encryptUsername;
    }

    public void setEncryptUsername(String encryptUsername) {
        this.encryptUsername = encryptUsername;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public List<String> getPhone() {
        return phone;
    }

    public void setPhone(List<String> phone) {
        this.phone = phone;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public int getContactScene() {
        return contactScene;
    }

    public void setContactScene(int contactScene) {
        this.contactScene = contactScene;
    }

    public int getChatroomFlag() {
        return chatroomFlag;
    }

    public void setChatroomFlag(int chatroomFlag) {
        this.chatroomFlag = chatroomFlag;
    }

    public int getVerifyFlag() {
        return verifyFlag;
    }

    public void setVerifyFlag(int verifyFlag) {
        this.verifyFlag = verifyFlag;
    }

    public String getContactLabelIds() {
        return contactLabelIds;
    }

    public void setContactLabelIds(String contactLabelIds) {
        this.contactLabelIds = contactLabelIds;
    }

    public byte[] getLvbuff() {
        return lvbuff;
    }

    public void setLvbuff(byte[] lvbuff) {
        this.lvbuff = lvbuff;
    }

    public ArrayList getTagList() {
        return tagList;
    }

    public void setTagList(ArrayList tagList) {
        this.tagList = tagList;
    }

    @Override
    public String toString() {
        return "RContactModel{" +
                "username='" + username + '\'' +
                ", alias='" + alias + '\'' +
                ", conRemark='" + conRemark + '\'' +
                ", domainList='" + domainList + '\'' +
                ", nickname='" + nickname + '\'' +
                ", pyInitial='" + pyInitial + '\'' +
                ", quanPin='" + quanPin + '\'' +
                ", showHead=" + showHead +
                ", type=" + type +
                ", weiboFlag='" + weiboFlag + '\'' +
                ", weiboNickname='" + weiboNickname + '\'' +
                ", conRemarkPYFull='" + conRemarkPYFull + '\'' +
                ", conRemarkPYShort='" + conRemarkPYShort + '\'' +
                ", encryptUsername='" + encryptUsername + '\'' +
                ", chatroomFlag=" + chatroomFlag +
                ", verifyFlag=" + verifyFlag +
                ", contactLabelIds='" + contactLabelIds + '\'' +
                ", lvbuff=" + Arrays.toString(lvbuff) +
                ", sex=" + sex +
                ", avatar='" + avatar + '\'' +
                ", phone='" + phone + '\'' +
                ", ticketId='" + ticketId + '\'' +
                ", contactScene=" + contactScene +
                ", tagList=" + tagList +
                ", addWay=" + addWay +
                '}';
    }

    public int getAddWay() {
        return addWay;
    }

    public void setAddWay(int addWay) {
        this.addWay = addWay;
    }
}
