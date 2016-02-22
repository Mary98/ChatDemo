package com.mary.chatdemo.fastjson.event;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.mary.chat_fastjson_lib.android.event.ChatTransDataEvent;
import com.mary.chatdemo.fastjson.TwoActivity;

/**
 * File Name:	ChatTransDataEventImpl
 * Author:      Mary
 * Write Dates: 2016/2/18
 * Description:	实时消息事件回调实现类
 * Change Log:	实现了实时消息的基本回调
 * 2016/2/18-09-13---[公司]---[姓名]
 * ......Added|Changed|Delete......
 * --------------------------------
 */
public class ChatTransDataEventImpl implements ChatTransDataEvent {
	/**标识符*/
	private final static String TAG = ChatTransDataEventImpl.class.getSimpleName();
	/**主线程*/
	private Activity activity = null;
	
	@Override
	public void onTransBuffer(String fingerPrintOfProtocal, int dwUserid, String dataContent) {
		Log.d(TAG, "【DEBUG_UI】收到来自用户" + dwUserid + "的消息:" + dataContent);
		
		//！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
		if(activity != null) {
			Toast.makeText(activity, "用户" + dwUserid+"说："+dataContent, Toast.LENGTH_SHORT).show();
			((TwoActivity)this.activity).showIMInfo_black("用户" + dwUserid + "说：" + dataContent);
		}
	}

	/**
	 * 设置主线程对象
	 * @param activity 主线程对象
	 * @return ChatTransDataEventImpl对象
	 */
	public ChatTransDataEventImpl setGUI(Activity activity) {
		this.activity = activity;
		return this;
	}

	@Override
	public void onErrorResponse(int errorCode, String errorMsg) {
		Log.d(TAG, "【DEBUG_UI】收到服务端错误消息，errorCode=" + errorCode + ", errorMsg=" + errorMsg);
		((TwoActivity)this.activity).showIMInfo_red("Server反馈错误码：" + errorCode + ",errorMsg=" + errorMsg);
	}
}
