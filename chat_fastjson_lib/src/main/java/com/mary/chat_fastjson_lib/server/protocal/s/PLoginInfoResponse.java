package com.mary.chat_fastjson_lib.server.protocal.s;

/**
 * File Name:	PLoginInfoResponse
 * Author:      Mary
 * Write Dates: 2016/2/18
 * Description:	登陆结果响应信息类
 * Change Log:
 * 2016/2/18-09-13---[公司]---[姓名]
 * ......Added|Changed|Delete......
 * --------------------------------
 */
public class PLoginInfoResponse {
	/**返回码*/
	private int code = 0;
	/**服务端生成的用户ID*/
	private int user_id = -1;

	public PLoginInfoResponse() {}

	public PLoginInfoResponse(int code, int user_id) {
		this.code = code;
		this.user_id = user_id;
	}

	public int getCode() {
		return this.code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public int getUser_id() {
		return this.user_id;
	}

	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}
}