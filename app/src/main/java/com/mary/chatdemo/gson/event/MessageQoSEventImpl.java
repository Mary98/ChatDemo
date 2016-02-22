package com.mary.chatdemo.gson.event;

import java.util.ArrayList;

import com.mary.chat_gson_lib.android.event.MessageQoSEvent;
import com.mary.chat_gson_lib.server.protocal.Protocal;
import com.mary.chatdemo.gson.OneActivity;

import android.app.Activity;
import android.util.Log;

/**
 * File Name:	MessageQoSEventImpl
 * Author:      Mary
 * Write Dates: 2016/2/18
 * Description:	QoS相关事件回调实现类
 * Change Log:	实现了QoS相关事件的基本回调
 * 2016/2/18-09-13---[公司]---[姓名]
 * ......Added|Changed|Delete......
 * --------------------------------
 */
public class MessageQoSEventImpl implements MessageQoSEvent {
	/**标识符*/
	private final static String TAG = MessageQoSEventImpl.class.getSimpleName();
	/**主线程*/
	private Activity activity = null;
	
	@Override
	public void messagesLost(ArrayList<Protocal> lostMessages) {
		Log.d(TAG, "【DEBUG_UI】收到系统的未实时送达事件通知，当前共有"+lostMessages.size()+"个包QoS保证机制结束，判定为【无法实时送达】！");
	
		if(this.activity != null) {
			((OneActivity)this.activity).showIMInfo_brightred("[消息未成功送达]共"+lostMessages.size()+"条!(网络状况不佳或对方id不存在)");
		}
	}

	@Override
	public void messagesBeReceived(String fingerPrint) {
		if(fingerPrint != null) {
			Log.d(TAG, "【DEBUG_UI】收到对方已收到消息事件的通知，fp=" + fingerPrint);
			if(this.activity != null) {
				((OneActivity)this.activity).showIMInfo_blue("[收到对方消息应答]fp=" + fingerPrint);
			}
		}
	}
	
	public MessageQoSEventImpl setGUI(Activity activity) {
		this.activity = activity;
		return this;
	}
}
