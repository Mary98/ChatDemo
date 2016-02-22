package com.mary.chat_gson_lib.android.utils;

import android.util.Log;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * File Name:	UDPUtils
 * Author:      Mary
 * Write Dates: 2016/2/18
 * Description:	本地UDP消息发送工具类
 * 				【注意】本类只提供发送UDP消息，没有提供接受消息的方法
 * Change Log:
 * 2016/2/18-09-13---[公司]---[姓名]
 * ......Added|Changed|Delete......
 * --------------------------------
 */
public class UDPUtils {
	/**标识符*/
	private static final String TAG = UDPUtils.class.getSimpleName();

	/**
	 * 发送一条UDP消息
	 * @param skt	对象引用
	 * @param d	要发送的字节数组
	 * @param length	字节数组长长矛
	 * @return	true表示成功发出，false则表示发送失败
	 */
	public static boolean send(DatagramSocket skt, byte[] d, int length) {
		if(skt != null && d != null) {
			try {
				return send(skt, new DatagramPacket(d, length));
			} catch (Exception e) {
				Log.e(TAG, "【IMCORE】send方法中》》发送UDP数据报文时出错了：remoteIp="+skt.getInetAddress()
						+", remotePort="+skt.getPort()+".原因是："+e.getMessage(), e);
				return false;
			}
		} else {
			Log.e(TAG, "【IMCORE】send方法中》》无效的参数：skt="+skt);
			// 解决google统计报告的bug: NullPointerException (@UDPUtils:send:30) {AsyncTask #4}
			//+", d="+d+", remoteIp="+skt.getInetAddress()+", remotePort="+skt.getPort());
			return false;
		}
	}

	/**
	 * 发送一条UDP消息
	 * @param skt	对象引用
	 * @param p	要发送的UDP数据报
	 * @return	true表示成功发出，false则表示发送失败
	 */
	public static synchronized boolean send(DatagramSocket skt, DatagramPacket p) {
		boolean sendSucess = true;
		if ((skt != null) && (p != null)) {
			if (skt.isConnected()) {
				try {
					skt.send(p);
				} catch (Exception e) {
					sendSucess = false;
					Log.e(TAG, "【IMCORE】send方法中》》发送UDP数据报文时出错了，原因是：" + e.getMessage(), e);
				}
			}
		} else {
			Log.w(TAG, "【IMCORE】在send()UDP数据报时没有成功执行，原因是：skt==null || p == null!");
		}
		return sendSucess;
	}
}