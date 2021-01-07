package com.alien.crack_wechat_robot.action;

import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.alien.crack_wechat_robot.WechatHook;
import com.alien.crack_wechat_robot.chat.ChatConstant;
import com.alien.crack_wechat_robot.db.RContactModel;
import com.alien.crack_wechat_robot.db.UserTable;
import com.alien.crack_wechat_robot.model.WechatMessage;
import com.alien.crack_wechat_robot.service.WxContactService;
import com.alien.crack_wechat_robot.util.UnicodeUtil;
import com.camel.api.SharedObject;
import com.camel.api.rposed.RposedHelpers;
import com.virjar.sekiro.api.CommonRes;
import com.virjar.sekiro.api.SekiroRequest;
import com.virjar.sekiro.api.SekiroRequestHandler;
import com.virjar.sekiro.api.SekiroResponse;
import com.virjar.sekiro.api.databind.AutoBind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import camel.external.org.apache.commons.lang3.StringUtils;

public class MessageAction implements SekiroRequestHandler {

    private static final SendMessageHandler sendMessageHandler = SendMessageHandler.getHandler();

    @AutoBind
    private String content;
    @AutoBind
    private String receiverWxId;
    @AutoBind
    private String receiverNickname;
    @AutoBind
    private Boolean isGroup;
    @AutoBind
    private String atWechatIds;

    /**
     * 根据微信Id查询用户信息
     *
     * @param userName 微信Id
     * @return
     */
    public static Object queryTalkerInfo(String userName) {
        try {
            Object storageQueryer = RposedHelpers.callStaticMethod(RposedHelpers.findClass("com.tencent.mm.model.c", SharedObject.loadPackageParam.classLoader), "aja");
            return RposedHelpers.callMethod(storageQueryer, "aub", userName);
        } catch (Throwable e) {
            Log.e(WechatHook.TAG, "queryTalkerInfo exception", e);
        }
        return null;
    }


    @Override
    public void handleRequest(SekiroRequest invokeRequest, SekiroResponse sekiroResponse) {
        if (StringUtils.isEmpty(content)) {
            Log.i(WechatHook.TAG, "message content cannot be empty");
            sekiroResponse.send(CommonRes.failed("message content cannot be empty"));
            return;
        }
        if (StringUtils.isBlank(receiverWxId)) {
            receiverWxId = WxContactService.findWxIdByNickName(receiverNickname);
            if (StringUtils.isBlank(receiverWxId)) {
                Log.i(WechatHook.TAG, "not found wxId,receiverNickname:" + receiverNickname);
                sekiroResponse.send(CommonRes.failed("not found wxId,receiverNickname:" + receiverNickname));
                return;
            }
        }
        WechatMessage wechatMessage = new WechatMessage(content, receiverWxId);
        wechatMessage.isGroup = isGroup;
        wechatMessage.atWechatIds = atWechatIds;
        postMessage(wechatMessage);
        sekiroResponse.send(CommonRes.success("消息发送完毕"));
    }

    private static String formatMessage(String message) {
        // unicode换行符
        return message.replaceAll("/r/n", UnicodeUtil.unicode2String("\\ua"));
    }

    public static void postMessage(WechatMessage wechatMessage) {
        Message message = Message.obtain();
        message.obj = wechatMessage;
        Log.i(WechatHook.TAG, "post message:" + wechatMessage);
        sendMessageHandler.sendMessage(message);
    }

    public static void postMessage(final String content, final String receiverWxId) {
        WechatMessage wechatMessage = new WechatMessage(content, receiverWxId);
        Message message = Message.obtain();
        message.obj = wechatMessage;
        Log.i(WechatHook.TAG, "post message:" + wechatMessage);
        sendMessageHandler.sendMessage(message);
    }

    /**
     * 发送消息
     * MsgRetransmitUI
     * 此方法是调用微信的事件管理中心，通过分发消息事件发送消息
     *
     * @param talker  消息对象
     * @param content 消息内容
     */
    private static void sendRawText(String talker, String content) {
        Object obj = RposedHelpers.newInstance(RposedHelpers.findClass("com.tencent.mm.g.a.tk", SharedObject.loadPackageParam.classLoader));
        Object model = RposedHelpers.getObjectField(obj, "dxK");
        RposedHelpers.setObjectField(model, "cIB", talker);
        RposedHelpers.setObjectField(model, "content", content);
        RposedHelpers.setObjectField(model, "type", 1);
        RposedHelpers.setObjectField(model, "flags", 0);
        Object aObj = RposedHelpers.getStaticObjectField(RposedHelpers.findClass("com.tencent.mm.sdk.b.a", SharedObject.loadPackageParam.classLoader), "KqH");
        RposedHelpers.callMethod(aObj, "l", obj);
    }

    /**
     * 发送带@的消息
     * 6.6.5版本的发送消息协议改变了
     * 此方法是直接使用微信的底层协议，实际上推荐发消息都用本方法，但是因为上面的方法已经稳定运行多个版本
     * 后面看情况再决定是否全部替换
     */
    private static void sendATText(String talker, List<String> atBeans, String content) {
        String atList = (String) RposedHelpers.callStaticMethod(RposedHelpers.findClass("com.tencent.mm.sdk.platformtools.by", SharedObject.loadPackageParam.classLoader),
                "o", atBeans, ",");
        HashMap<String, String> hashMap = new HashMap(1);
        hashMap.put("atuserlist", "<![CDATA[" + atList + "]]>");
        Object obj = RposedHelpers.newInstance(RposedHelpers.findClass("com.tencent.mm.modelmulti.i", SharedObject.loadPackageParam.classLoader),
                talker, content, 1, 291, hashMap);
        Object netObj = RposedHelpers.callStaticMethod(RposedHelpers.findClass("com.tencent.mm.model.be", SharedObject.loadPackageParam.classLoader), "akK");
        RposedHelpers.callMethod(netObj, "a", obj, 0);
    }

    //获取单个人@文本
    private static String getSingleAtHeader(String weixinId) {
        char a = 8197;
        //获取weixinId的用户名
        RContactModel contactModel = UserTable.findUserInfo(SharedObject.loadPackageParam.classLoader, weixinId);
        if (contactModel == null) return "";
        String username = contactModel.nickname;
        return "@" + username + a;
    }

    public static void sendMessage(WechatMessage wechatMessage) {
        switch (wechatMessage.msgType) {
            case ChatConstant.SEND_TYPE_TEXT:
                if (wechatMessage.isGroup) {
                    String atIds = wechatMessage.atWechatIds;
                    String content = wechatMessage.content;
                    if (TextUtils.isEmpty(atIds) || atIds.equals("null")) {
                        sendRawText(wechatMessage.receiverWxId, content);
                    } else {
                        //如果是群聊，并且是at
                        ArrayList<String> arr = new ArrayList<>();
                        String[] wxIds = atIds.split(",");
                        for (int i = 0; i < wxIds.length; ++i) {
                            arr.add(wxIds[i]);
                            content = getSingleAtHeader(wxIds[i]) + content;
                        }
                        sendATText(wechatMessage.receiverWxId, arr, content);
                    }
                } else {
                    sendRawText(wechatMessage.receiverWxId, wechatMessage.content);
                }
                break;
            case ChatConstant.SEND_TYPE_IMAGE:
                ChatHelper.downloadAndSendImageFile(wechatMessage.imageUrl,wechatMessage.receiverWxId,wechatMessage.md5);
            default:
                break;
        }
    }
}
