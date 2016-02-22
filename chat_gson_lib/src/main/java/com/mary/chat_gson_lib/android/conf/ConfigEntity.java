package com.mary.chat_gson_lib.android.conf;

import com.mary.chat_gson_lib.android.core.KeepAliveDaemon;

/**
 * File Name:	ConfigEntity
 * Author:      Mary
 * Write Dates: 2016/2/18
 * Description:	全局参数控制类
 * Change Log:
 * 2016/2/18-09-13---[公司]---[姓名]
 * ......Added|Changed|Delete......
 * --------------------------------
 */
public class ConfigEntity {
	/** AppKey */
	public static String appKey = null;
	/** 服务端IP或域名 */
	public static String serverIP = "rbcore.openmob.net";
	/** 服务端UDP服务侦听端口号 */
	public static int serverUDPPort = 7901;
	/** 本地UDP数据发送和侦听端口 */
	public static int localUDPPort = 0;

	/**
	 * 设置敏感度模式：保持连接
	 * @param mode 敏感度模式
	 */
	public static void setSenseMode(SenseMode mode) {
		// Kepp alive心跳间隔
		int keepAliveInterval = 0;
		// 与服务端掉线的超时时长
		int networkConnectionTimeout = 0;
		switch (mode) {
			case MODE_3S: {
				// 心跳间隔3秒
				keepAliveInterval = 1000 * 3;// 3s
				// 10秒后未收到服务端心跳反馈即认为连接已断开（相当于连续3 个心跳间隔后仍未收到服务端反馈）
				networkConnectionTimeout = keepAliveInterval * 3 + 1000;// 10s
				break;
			}
			case MODE_10S:
				// 心跳间隔10秒
				keepAliveInterval = 1000 * 10;// 10s
				// 10秒后未收到服务端心跳反馈即认为连接已断开（相当于连续2 个心跳间隔后仍未收到服务端反馈）
				networkConnectionTimeout = keepAliveInterval * 2 + 1000;// 21s
	    		break;
			case MODE_30S:
				// 心跳间隔30秒
				keepAliveInterval = 1000 * 30;// 30s
				// 10秒后未收到服务端心跳反馈即认为连接已断开（相当于连续2 个心跳间隔后仍未收到服务端反馈）
				networkConnectionTimeout = keepAliveInterval * 2 + 1000;// 61s
	    		break;
			case MODE_60S:
				// 心跳间隔60秒
				keepAliveInterval = 1000 * 60;// 60s
				// 10秒后未收到服务端心跳反馈即认为连接已断开（相当于连续2 个心跳间隔后仍未收到服务端反馈）
				networkConnectionTimeout = keepAliveInterval * 2 + 1000;// 121s
	    		break;
			case MODE_120S:
				// 心跳间隔120秒
				keepAliveInterval = 1000 * 120;// 120s
				// 10秒后未收到服务端心跳反馈即认为连接已断开（相当于连续2 个心跳间隔后仍未收到服务端反馈）
				networkConnectionTimeout = keepAliveInterval * 2 + 1000;// 241s
	    		break;
		}

		if(keepAliveInterval > 0) {
    		// 设置Kepp alive心跳间隔
    		KeepAliveDaemon.KEEP_ALIVE_INTERVAL = keepAliveInterval;
    	}

    	if(networkConnectionTimeout > 0) {
    		// 设置与服务端掉线的超时时长
    		KeepAliveDaemon.NETWORK_CONNECTION_TIME_OUT = networkConnectionTimeout;
    	}
	}

	/**
	 * 敏感度模式
	 * 对于客户端而言，此模式决定了用户与服务端网络会话的健康模式，原则上超敏感客户端的体验越好。
	 * 【重要说明】：客户端本模式的设定必须要与服务端的模式设制保持一致，
	 * 			 	 否则 可能因参数的不一致而导致IM算法的不匹配，进而出现不可预知的问题。
	 */
	public static enum SenseMode {
		/**
		 * KeepAlive心跳间隔为3秒
		 * 在10秒后未收到服务端心跳反馈即认为连接已断开（相当于连续3 个心跳间隔后仍未收到服务端反馈）
		 */
		MODE_3S,
		/**
		 * KeepAlive心跳间隔为10秒
		 * 在21秒后未收到服务端心跳反馈即认为连接已断开（相当于连续2 个心跳间隔后仍未收到服务端反馈）
		 */
		MODE_10S,
		/**
		 * KeepAlive心跳间隔为30秒
		 * 在61秒后未收到服务端心跳反馈即认为连接已断开（相当于连续2 个心跳间隔后仍未收到服务端反馈）
		 */
		MODE_30S,
		/**
		 * KeepAlive心跳间隔为60秒
		 * 在121秒后未收到服务端心跳反馈即认为连接已断开（相当于连续2 个心跳间隔后仍未收到服务端反馈）
		 */
		MODE_60S,
		/**
		 * KeepAlive心跳间隔为120秒
		 * 在241秒后未收到服务端心跳反馈即认为连接已断开（相当于连续2 个心跳间隔后仍未收到服务端反馈）
		 */
		MODE_120S;
	}
}