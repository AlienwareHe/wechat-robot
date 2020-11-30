# 介绍
这是一个基于Sekiro的安卓微信消息发送功能暴露的类Xposed插件，功能是将WX发消息等功能通过Http接口对外暴露，只需调用:

> http://xxx.clickdream.xxx:9602/invoke?group=WECHAT_ROBOT&action=sendMessage&content=我是机器人&receiverNickname=alien

即可完成向alien发送「我是机器人」的微信消息，这只是微信群控的一个例子，一切扩展功能如朋友圈、群发等均可自由想象。

# 原理
- sekiro 通过sekiro建议服务端与手机上WX的长连接作为RPC调用通信通道
- 通过Hook框架如EdXposed、SandHook等等注入sekiro，并反射调用WX相关API，本项目使用的是QContainer(自己写的一个免Root手机上运行的应用分身和设备指纹框架)，如果想要迁移，只需替换相关Xposed API即可

# 使用
对于QContain版来说：
1. 手机安装「感染」后的微信和QContainer
2. 修改插件中的com.alien.crack_wechat_robot.WechatHook.SEKIRO_HOST和SEKIRO_PORT，并安装插件至手机
3. 在QContainer中勾选插件生效
4. 打开微信，查看adb logcat -s WX_HOOK查看插件是否正常生效

# 运维
## 查看设备长连接是否在线
> http://sz.clickdream.top:9602/natChannelStatus?group=WECHAT_ROBOT

## 查看设备当前截图
> http://sz.clickdream.top:9602/invoke?group=WECHAT_ROBOT&action=screenShot

## 重启微信
在长连接断线的情况下（一般情况下只要应用存活就不会出现这种情况），可以尝试重启微信重新注册长连接：
> adb shell am start -n com.tencent.mm/com.tencent.mm.ui.LauncherUI
> adb shell am force-stop com.tencent.mm
