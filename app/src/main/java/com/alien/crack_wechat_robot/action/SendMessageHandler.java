package com.alien.crack_wechat_robot.action;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alien.crack_wechat_robot.WechatHook;
import com.alien.crack_wechat_robot.model.WechatMessage;
import com.blankj.utilcode.util.StringUtils;

public class SendMessageHandler extends Handler {

    private static final int SEND_INTERVAL_MILLION_SECONDS = 1000;

    private static SendMessageHandler INSTANCE;

    private String lastReceiver;

    public SendMessageHandler() {
        super();
    }

    public SendMessageHandler(@NonNull Looper looper) {
        super(looper);
    }

    public static SendMessageHandler getHandler() {
        if (INSTANCE == null) {
            HandlerThread handlerThread = new HandlerThread("sendMessageHandlerThread");
            handlerThread.start();
            INSTANCE = new SendMessageHandler(handlerThread.getLooper());
        }
        return INSTANCE;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        WechatMessage wechatMessage = (WechatMessage) msg.obj;
        if (wechatMessage == null) {
            return;
        }
        Log.i(WechatHook.TAG, "SendMessageHandler receive send message:" + wechatMessage);
        MessageAction.sendMessage(wechatMessage.content, wechatMessage.receiverWxId);
        try {
            if (!StringUtils.equals(wechatMessage.receiverWxId, lastReceiver)) {
                Thread.sleep(SEND_INTERVAL_MILLION_SECONDS);
                lastReceiver = wechatMessage.receiverWxId;
            }
        } catch (InterruptedException e) {
            Log.e(WechatHook.TAG, "SendMessage sleep interrupted:", e);
        }
    }
}
