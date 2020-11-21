package com.alien.crack_wechat_robot.action;

import android.util.Log;

import com.alien.crack_wechat_robot.WechatHook;
import com.alien.crack_wechat_robot.service.WxContactService;
import com.virjar.sekiro.api.CommonRes;
import com.virjar.sekiro.api.SekiroRequest;
import com.virjar.sekiro.api.SekiroRequestHandler;
import com.virjar.sekiro.api.SekiroResponse;

import camel.external.org.apache.commons.lang3.StringUtils;

public class ContactInfoAction implements SekiroRequestHandler {

    @Override
    public void handleRequest(SekiroRequest sekiroRequest, SekiroResponse sekiroResponse) {
        String findBy = sekiroRequest.getString("findBy");
        String param = sekiroRequest.getString("param");
        if (StringUtils.isBlank(findBy) || StringUtils.isEmpty(param)) {
            sekiroResponse.send("查询条件参数不能为空");
            return;
        }
        Log.i(WechatHook.TAG, "query contact info where " + findBy + " = " + param);
        if (findBy.equals("nickname")) {
            String wxId = WxContactService.findWxIdByNickName(param);
            sekiroResponse.send(CommonRes.success(StringUtils.isBlank(wxId) ? "not found" : wxId));
        } else {
            sekiroResponse.send("暂不支持" + findBy + "维度查询");
        }
    }
}
