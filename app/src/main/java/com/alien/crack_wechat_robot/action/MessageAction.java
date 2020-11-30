package com.alien.crack_wechat_robot.action;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alien.crack_wechat_robot.WechatHook;
import com.alien.crack_wechat_robot.model.ApiResponse;
import com.alien.crack_wechat_robot.model.WechatMessage;
import com.alien.crack_wechat_robot.service.WxContactService;
import com.alien.crack_wechat_robot.util.UnicodeUtil;
import com.camel.api.SharedObject;
import com.camel.api.rposed.RC_MethodHook;
import com.camel.api.rposed.RposedHelpers;
import com.virjar.sekiro.api.CommonRes;
import com.virjar.sekiro.api.SekiroRequest;
import com.virjar.sekiro.api.SekiroRequestHandler;
import com.virjar.sekiro.api.SekiroResponse;
import com.virjar.sekiro.api.databind.AutoBind;

import camel.external.org.apache.commons.lang3.StringUtils;

public class MessageAction implements SekiroRequestHandler {


    private static Object chattingInfo;
    private static Object chatFooterEventListener;
    private static Object chatFooter;
    private static final SendMessageHandler sendMessageHandler = SendMessageHandler.getHandler();

    @AutoBind
    private String content;
    @AutoBind
    private String receiverWxId;
    @AutoBind
    private String receiverNickname;

    static {
        RposedHelpers.findAndHookConstructor(RposedHelpers.findClass("com.tencent.mm.ui.chatting.p", SharedObject.loadPackageParam.classLoader), "com.tencent.mm.ui.chatting.d.a", "com.tencent.mm.pluginsdk.ui.chat.ChatFooter", "java.lang.String", new RC_MethodHook() {
            @Override
            protected void afterHookedMethod(RC_MethodHook.MethodHookParam param) throws Throwable {
                Log.i(WechatHook.TAG, "chatFooterEventListener hooked");
                chatFooterEventListener = param.thisObject;
                chatFooter = param.args[1];
                chattingInfo = param.args[0];
            }
        });
    }

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
        if (chatFooter == null) {
            sekiroResponse.send(CommonRes.failed("message sender chatFooter(com.tencent.mm.pluginsdk.ui.chat.ChatFooter) is null"));
            return;
        }
        if (chattingInfo == null) {
            sekiroResponse.send(CommonRes.failed("chattingInfo(com.tencent.mm.ui.chatting.d.a) is null"));
            return;
        }
        if (chatFooterEventListener == null) {
            sekiroResponse.send(CommonRes.failed("chatFooterEventListener(com.tencent.mm.ui.chatting.p) is null"));
            return;
        }
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
        postMessage(formatMessage(content), receiverWxId);
        sekiroResponse.send(CommonRes.success("消息发送完毕"));
    }

    private static String formatMessage(String message) {
        // unicode换行符
        return message.replaceAll("/r/n", UnicodeUtil.unicode2String("\\ua"));
    }

    public static void postMessage(final String content, final String receiverWxId) {
        WechatMessage wechatMessage = new WechatMessage(content, receiverWxId);
        Message message = Message.obtain();
        message.obj = wechatMessage;
        Log.i(WechatHook.TAG, "post message:" + wechatMessage);
        sendMessageHandler.sendMessage(message);
    }

    public static void sendMessage(final String message, final String receiverWxId) {
        /**
         * 一定要在UI线程中去发送，因为在构造newChatFooterEventListener的时候会需要获取MainLooper
         */
        final ApiResponse apiResponse = new ApiResponse();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    Object talkerInfo = queryTalkerInfo(receiverWxId);
                    if (talkerInfo == null) {
                        apiResponse.fail("未查询到该好友信息:" + receiverWxId);
                        return;
                    }
                    Log.i(WechatHook.TAG, receiverWxId + " talker info:" + JSON.toJSON(talkerInfo));
                    // 替换发送目标，重新构造发送事件
                    RposedHelpers.callMethod(chattingInfo, "ae", talkerInfo);
                    Object newChatFooterEventListener = RposedHelpers.newInstance(RposedHelpers.findClass("com.tencent.mm.ui.chatting.p", SharedObject.loadPackageParam.classLoader), chattingInfo, chatFooter, receiverWxId);
                    Boolean res = (Boolean) RposedHelpers.callMethod(newChatFooterEventListener, "TT", message);
                    apiResponse.suc("消息发送完毕:" + res);
                    Log.i(WechatHook.TAG, receiverWxId + " 发送消息结果：" + apiResponse);
                } catch (Throwable e) {
                    Log.i(WechatHook.TAG, "send message to " + receiverWxId + "error:" + message, e);
                    apiResponse.fail("send message exception:" + e.getMessage());
                }
            }
        });
    }
}
