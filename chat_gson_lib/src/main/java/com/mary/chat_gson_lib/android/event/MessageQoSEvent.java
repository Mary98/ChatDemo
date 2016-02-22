package com.mary.chat_gson_lib.android.event;

import java.util.ArrayList;
import com.mary.chat_gson_lib.server.protocal.Protocal;

/**
 * File Name:	MessageQoSEvent
 * Author:      Mary
 * Write Dates: 2016/2/18
 * Description: 质量保证机制的回调事件接口
 * Change Log:
 * 2016/2/18-09-13---[公司]---[姓名]
 * ......Added|Changed|Delete......
 * --------------------------------
 */
public interface MessageQoSEvent {
  /**
   * 消息未送达的回调事件通知
   * @param lostMessages  由QoS算法判定出来的未送达消息列表(此列表中的Protocal对象是原对象的clone
   *                      (即原对象的深拷贝),请放心使用),应用层可通过指纹特征码找到原消息并可以
   *                      UI上将其标记为"发送失败"以便即时告之用户
   */
  void messagesLost(ArrayList<Protocal> lostMessages);

  /**
   * 消息已被对方收到的回调事件通知
   * 【注意】目前，判定消息被对方收到是有两种可能：
   *            1) 对方确实是在线并且实时收到了；
   *            2) 对方不在线或者服务端转发过程中出错了，由服务端进行离线存储成功后的反馈
   *            (此种情况严格来讲不能算是"已被收到"，但对于应用层来说，
   *            离线存储了的消息原则上就是已送达了的消息：因为用户下次登陆时肯定能通过HTTP协议取到)
   * @param fingerPrint 已被收到的消息的指纹特征码(唯一ID)，
   *                    应用层可据此ID来找到原先已发生的消息并可在UI是将其标记为
   *                    "已送达"或"已读"以便提升用户体验
   */
  void messagesBeReceived(String fingerPrint);
}