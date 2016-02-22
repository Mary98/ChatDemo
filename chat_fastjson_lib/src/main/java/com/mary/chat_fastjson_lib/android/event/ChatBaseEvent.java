package com.mary.chat_fastjson_lib.android.event;

/**
 * File Name:	ChatBaseEvent
 * Author:      Mary
 * Write Dates: 2016/2/18
 * Description: 基础通信消息的回调事件接口(如：登陆成功事件 通知、掉线事件通知等)
 * Change Log:
 * 2016/2/18-09-13---[公司]---[姓名]
 * ......Added|Changed|Delete......
 * --------------------------------
 */
public interface ChatBaseEvent {

  /**
   * 用户的登陆结果回调事件通知
   * @param userId  当回调参数errorCode==0时，
   *                本回调参数值表示登陆成功后服务端分配的用户id，
   *                否则本回调参数值无意义
   * @param errorCode 服务端反馈的登录结果：0 表示登陆成功，
   *                  否则为服务端自定义的出错代码
   *                  (按照约定通常为>=1025的数)
   */
  void onLoginMessage(int userId, int errorCode);

  /**
   * 与服务端的通信断开的回调事件通知
   * 【注意】该消息只有在客户端连接服务器成功之后网络异常中断之时触发。
   *        导致与与服务端的通信断开的原因有(但不限于)：
   *            无线网络信号不稳定、WiFi与2G/3G/4G等同开情 况下的网络切换、手机系统的省电策略等。
   * @param errorCode  本回调参数表示连接断开的原因，
   *                   目前错误码没有太多意义，仅作保留字段，目前通常为-1
   */
  void onLinkCloseMessage(int errorCode);

}