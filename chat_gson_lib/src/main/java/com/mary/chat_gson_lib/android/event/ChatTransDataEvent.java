package com.mary.chat_gson_lib.android.event;

/**
 * File Name:	ChatTransDataEvent
 * Author:      Mary
 * Write Dates: 2016/2/18
 * Description:	通用数据通信消息的回调事件接口(如：收到聊天数据事件、通知、服务端返回的错误信息事件通知等)
 * Change Log:
 * 2016/2/18-09-13---[公司]---[姓名]
 * ......Added|Changed|Delete......
 * --------------------------------
 */
public interface ChatTransDataEvent {
    /**
     * 收到普通消息的回调事件通知
     * 应用层可以将此消息进一步按自已的IM协议进行定义， 从而实现完整的即时通信软件逻辑。
     * @param fingerPrintOfProtocal 当该消息需要QoS支持时本回调参数为该消息的特征指纹码，否则为null
     * @param userid    消息的发送者id(按照规定：发送者id=0即表示是由服务端主动发过的，
     *                  否则表示的是其它客户端id发过来的消息)
     * @param dataContent   消息内容字符串
     */
    void onTransBuffer(String fingerPrintOfProtocal, int userid, String dataContent);

    /**
     * 服务端反馈的出错信息回调事件通知
     * @param errorCode 错误码，定义在常量表ErrorCode.ForS中
     * @param errorMsg  描述错误内容的文本信息
     */
    void onErrorResponse(int errorCode, String errorMsg);

}