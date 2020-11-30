package com.alien.crack_wechat_robot.action;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alien.crack_wechat_robot.WechatHook;
import com.alien.crack_wechat_robot.model.ApiResponse;
import com.alien.crack_wechat_robot.model.WechatMessage;

public class SendMessageHandler extends Handler {

    private static final int SEND_INTERVAL_MILLION_SECONDS = 500;

    @Override
    public void handleMessage(@NonNull Message msg) {
        WechatMessage wechatMessage = (WechatMessage) msg.obj;
        if (wechatMessage == null) {
            return;
        }
        MessageAction.sendMessage(wechatMessage.content, wechatMessage.receiverWxId);
        try {
            Thread.sleep(SEND_INTERVAL_MILLION_SECONDS);
        } catch (InterruptedException e) {
            Log.e(WechatHook.TAG, "SendMessage sleep interrupted:", e);
        }
    }
}
