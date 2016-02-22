package com.mary.chat_fastjson_lib.server.protocal;

/**
 * File Name:	ProtocalType
 * Author:      Mary
 * Write Dates: 2016/2/18
 * Description:	协议类型
 * Change Log:
 * 2016/2/18-09-13---[公司]---[姓名]
 * ......Added|Changed|Delete......
 * --------------------------------
 */
public interface ProtocalType {
	/**客户端信息类型接口*/
	interface C {
		/**由客户端发出 - 协议类型：客户端登陆*/
		int FROM_CLIENT_TYPE_OF_LOGIN 		= 0;
		/**由客户端发出 - 协议类型：心跳包*/
		int FROM_CLIENT_TYPE_OF_KEEP$ALIVE  = 1;
		/**由客户端发出 - 协议类型：发送通用数据*/
		int FROM_CLIENT_TYPE_OF_COMMON$DATA = 2;
		/**由客户端发出 - 协议类型：客户端退出登陆*/
		int FROM_CLIENT_TYPE_OF_LOGOUT 		= 3;
		/**由客户端发出 - 协议类型：QoS保证机制中的消息应答包(目前只支持客户端间的QoS机制哦)*/
		int FROM_CLIENT_TYPE_OF_RECIVED 	= 4;
		/**由客户端发出 - 协议类型：C2S时的回显指令(此指令目前仅用于测试时)*/
		int FROM_CLIENT_TYPE_OF_ECHO 		= 5;
	}

	/**服务端信息类型接口*/
	interface S {
		/**由服务端发出 - 协议类型：响应客户端的登陆*/
		int FROM_SERVER_TYPE_OF_RESPONSE$LOGIN 		= 50;
		/**由服务端发出 - 协议类型：响应客户端的心跳包*/
		int FROM_SERVER_TYPE_OF_RESPONSE$KEEP$ALIVE = 51;
		/**由服务端发出 - 协议类型：反馈给客户端的错误信息*/
		int FROM_SERVER_TYPE_OF_RESPONSE$FOR$ERROR  = 52;
		/**由服务端发出 - 协议类型：反馈回显指令给客户端*/
		int FROM_SERVER_TYPE_OF_RESPONSE$ECHO 		= 53;
	}
}