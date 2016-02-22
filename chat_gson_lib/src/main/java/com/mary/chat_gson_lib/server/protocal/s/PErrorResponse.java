package com.mary.chat_gson_lib.server.protocal.s;

/**
 * File Name:	PErrorResponse
 * Author:      Mary
 * Write Dates: 2016/2/18
 * Description:	错误的响应信息类
 * Change Log:
 * 2016/2/18-09-13---[公司]---[姓名]
 * ......Added|Changed|Delete......
 * --------------------------------
 */
public class PErrorResponse {
	/**错误码*/
	private int errorCode   = -1;
	/**错误信息*/
	private String errorMsg = null;

	public PErrorResponse(int errorCode, String errorMsg) {
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
	}

	public int getErrorCode() {
		return this.errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMsg() {
		return this.errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
}