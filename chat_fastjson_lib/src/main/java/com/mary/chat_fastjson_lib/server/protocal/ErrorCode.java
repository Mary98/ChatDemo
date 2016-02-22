package com.mary.chat_fastjson_lib.server.protocal;

/**
 * File Name:	ErrorCode
 * Author:      Mary
 * Write Dates: 2016/2/18
 * Description:	错误码常量表. 建议0~1024范围内的错误码作为保留，业务层请使用>1024的码表示！
 * Change Log:
 * 2016/2/18-09-13---[公司]---[姓名]
 * ......Added|Changed|Delete......
 * --------------------------------
 */
public interface ErrorCode {
	/**一切正常*/
	int COMMON_CODE_OK 			= 0;
	/**客户端尚未登陆*/
	int COMMON_NO_LOGIN 		= 1;
	/**未知错误*/
	int COMMON_UNKNOW_ERROR 	= 2;
	/**数据发送失败*/
	int COMMON_DATA_SEND_FAILD 	= 3;
	/**无效的 Protocal对象*/
	int COMMON_INVALID_PROTOCAL = 4;

	/**
	 * 由客户端产生的错误码
	 */
	interface ForC {
		/**与服务端的连接已断开*/
		int BREOKEN_CONNECT_TO_SERVER 	 = 201;
		/**与服务端的网络连接失败*/
		int BAD_CONNECT_TO_SERVER 		 = 202;
		/**客户端SDK尚未初始化*/
		int CLIENT_SDK_NO_INITIALED 	 = 203;
		/**本地网络不可用(未打开)*/
		int LOCAL_NETWORK_NOT_WORKING 	 = 204;
		/**要连接的服务端网络参数未设置*/
		int TO_SERVER_NET_INFO_NOT_SETUP = 205;
	}

	/**
	 * 由服务端产生的错误码
	 */
	interface ForS {
		int RESPONSE_FOR_UNLOGIN = 301;
	}
}