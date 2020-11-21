# 介绍
这是一个基于Sekiro的安卓微信消息发送功能暴露的类Xposed插件，功能是将WX发消息等功能通过Http接口对外暴露，只需调用:

> http://xxx.clickdream.xxx:9602/invoke?group=WECHAT_ROBOT&action=sendMessage&content=我是机器人&receiverNickname=alien

即可完成向alien发送「我是机器人」的微信消息，这只是微信群控的一个例子，一切扩展功能如朋友圈、群发等均可自由想象。

# 原理
- sekiro 通过sekiro建议服务端与手机上WX的长连接作为RPC调用通信通道
- 通过Hook框架如EdXposed、SandHook等等注入sekiro，并反射调用WX相关API，本项目使用的是QContainer(自己写的一个免Root手机上运行的应用分身和设备指纹框架)，如果想要迁移，只需替换相关Xposed API即可
