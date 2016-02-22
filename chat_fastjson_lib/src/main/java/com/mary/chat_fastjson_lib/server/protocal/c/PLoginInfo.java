package com.mary.chat_fastjson_lib.server.protocal.c;

/**
 * File Name:	PLoginInfo
 * Author:      Mary
 * Write Dates: 2016/2/18
 * Description:	登陆信息类
 * Change Log:
 * 2016/2/18-09-13---[公司]---[姓名]
 * ......Added|Changed|Delete......
 * --------------------------------
 */
public class PLoginInfo {
	/**登陆时提交的用户名*/
	private String loginName = null;
	/**登陆时提交的密码*/
	private String loginPsw = null;
	/**额外信息字符串*/
	private String extra = null;

	public PLoginInfo() {}

	public PLoginInfo(String loginName, String loginPsw) {
		this(loginName, loginPsw, null);
	}
	
	public PLoginInfo(String loginName, String loginPsw, String extra) {
		this.loginName = loginName;
		this.loginPsw = loginPsw;
		this.extra = extra;
	}

	public String getLoginName() {
		return this.loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getLoginPsw() {
		return this.loginPsw;
	}

	public void setLoginPsw(String loginPsw) {
		this.loginPsw = loginPsw;
	}
	
	public String getExtra() {
		return extra;
	}
	
	public void setExtra(String extra) {
		this.extra = extra;
	}
}