# 介绍
这是一个基于Sekiro的安卓微信消息发送功能暴露的类Xposed插件，功能是将WX发消息等功能通过Http接口对外暴露，只需调用:

> http://xxx.clickdream.xxx:9602/invoke?group=WECHAT_ROBOT&action=sendMessage&content=我是机器人&receiverNickname=alien

即可完成向alien发送「我是机器人」的微信消息，这只是微信群控的一个例子，一切扩展功能如朋友圈、群发等均可自由想象。

# 原理
- sekiro 通过sekiro建议服务端与手机上WX的长连接作为RPC调用通信通道
- 通过Hook框架如EdXposed、SandHook等等注入sekiro，并反射调用WX相关API，本项目使用的是QContainer(自己写的一个免Root手机上运行的应用分身和设备指纹框架)，如果想要迁移，只需替换相关Xposed API即可

# 快速试用QContainer版
如果你不想改API或者想试试效果，可以使用本项目提供的weixin.qc.apk（微信版本：7.0.21_1800）快速在安卓7～安卓10上使用本项目，使用方法：
1. 安装QContainer Manager（链接:https://pan.baidu.com/s/16Siy1SnTnBwM7LyEOgjzvw  密码:xw9r）和本插件
2. 下载安装感染后的微信（链接:https://pan.baidu.com/s/1CkUu03DQDWZVTrtBYiYo3A  密码:97eo）
3. 在QContainer Manager中菜单栏中选择模块，勾选插件使其生效
4. 打开微信

# How to use

## action:sendMessage

> https://xxx:9602/invoke?group=tobortahcew001&action=sendMessage&content=我叫你一声你敢答应吗&receiverNickname=having%20a%20house&atWechatIds=wxid_myutdawtuhzm22&isGroup=true

- content:消息内容
- receiverNickname:消息接收者
- atWechatIds:艾特的微信id，通过逗号分割多个微信Id
- isGroup:只有在群聊中才可以使用@功能
- msgType:消息类型，默认为文本消息，图片为1
- imageUrl:图片下载链接，发送图片必须字段
- md5:可选字段，用于避免重复下载文件

> 发送图片例子：https://xxx:9602/invoke?group=tobortahcew001&action=sendMessage&receiverNickname=having%20a%20house&isGroup=true&imageUrl=http%3a%2f%2fwww.xinhuanet.com%2fphoto%2f2021-01%2f21%2f1127010036_16112253748571n.jpg

## action:findContactInfo

根据微信昵称查找用户微信Id

> https://xxx:9602/invoke?group=WECHAT_ROBOT&action=findContactInfo&findBy=nickname&param=xxx

# 运维
## 查看设备长连接是否在线
> http://xxx:9602/natChannelStatus?group=WECHAT_ROBOT

## 重启微信
在长连接断线的情况下（一般情况下只要应用存活就不会出现这种情况），可以尝试重启微信重新注册长连接：
> adb shell am start -n com.tencent.mm/com.tencent.mm.ui.LauncherUI
> adb shell am force-stop com.tencent.mm