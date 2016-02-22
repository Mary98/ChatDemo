package com.mary.chat_gson_lib.server.protocal;

import com.google.gson.Gson;
import com.mary.chat_gson_lib.server.protocal.c.PKeepAlive;
import com.mary.chat_gson_lib.server.protocal.c.PLoginInfo;
import com.mary.chat_gson_lib.server.protocal.s.PErrorResponse;
import com.mary.chat_gson_lib.server.protocal.s.PKeepAliveResponse;
import com.mary.chat_gson_lib.server.protocal.s.PLoginInfoResponse;

/**
 * File Name:	ProtocalFactory
 * Author:      Mary
 * Write Dates: 2016/2/18
 * Description:	协议工厂类
 * Change Log:
 * 2016/2/18-09-13---[公司]---[姓名]
 * ......Added|Changed|Delete......
 * --------------------------------
 */
public class ProtocalFactory {

	private static String create(Object c) {
		return new Gson().toJson(c);
	}

	/**
	 * 将JSON文本反射成Java对象
	 * @param fullProtocalJASOnBytes	对象的json(byte数组组织形式)
	 * @param len	bye数组有效数据长度
	 * @param clazz	要反射成的对象
	 * @param <T>	泛型
	 * @return	反射完成的Java对象
	 */
	public static <T> T parse(byte[] fullProtocalJASOnBytes, int len, Class<T> clazz) {
		return parse(CharsetHelper.getString(fullProtocalJASOnBytes, len), clazz);
	}

	/**
	 * 将JSON文本反射成Java对象
	 * @param dataContentOfProtocal	对象json文本
	 * @param clazz	要反射成的对象
	 * @param <T>	泛型
	 * @return	反射完成的Java对象
	 */
	public static <T> T parse(String dataContentOfProtocal, Class<T> clazz) {
		return new Gson().fromJson(dataContentOfProtocal, clazz);
	}

	/**
	 * 将JSON文本反射成Java对象
	 * @param fullProtocalJASOnBytes	对象的json(byte数组组织形式)
	 * @param len	bye数组有效数据长度
	 * @return	反射完成的Java对象
	 */
	public static Protocal parse(byte[] fullProtocalJASOnBytes, int len) {
		return (Protocal)parse(fullProtocalJASOnBytes, len, Protocal.class);
	}

	/**
	 * 创建响应客户端的心跳消息对象(该对象由服务端发出)
	 * @param to_user_id	接受用户ID
	 * @return	Protocal对象
	 */
	public static Protocal createPKeepAliveResponse(int to_user_id) {
		return new Protocal(ProtocalType.S.FROM_SERVER_TYPE_OF_RESPONSE$KEEP$ALIVE, 
				create(new PKeepAliveResponse()), 0, to_user_id);
	}

	/**
	 * 创建KeepAlive响应对象
	 * @param dataContentOfProtocal	字符串
	 * @return	KeepAlive响应对象
	 */
	public static PKeepAliveResponse parsePKeepAliveResponse(String dataContentOfProtocal) {
		return (PKeepAliveResponse)parse(dataContentOfProtocal, PKeepAliveResponse.class);
	}

	/**
	 * 创建用户心跳包对象(该对象由客户端发出)
	 * @param from_user_id	发送的用户ID
	 * @return	Protocal对象
	 */
	public static Protocal createPKeepAlive(int from_user_id) {
		return new Protocal(ProtocalType.C.FROM_CLIENT_TYPE_OF_KEEP$ALIVE, 
				create(new PKeepAlive()), from_user_id, 0);
	}

	/**
	 * 创建KeepAlive心跳对象
	 * @param dataContentOfProtocal	字符串
	 * @return	KeepAlive心跳对象
	 */
	public static PKeepAlive parsePKeepAlive(String dataContentOfProtocal) {
		return (PKeepAlive)parse(dataContentOfProtocal, PKeepAlive.class);
	}

	/**
	 * 创建错误响应消息对象(该对象由服务端发出)
	 * @param errorCode	错误码
	 * @param errorMsg	错误消息文本内容(本参数非必须的)
	 * @param user_id	用户ID
	 * @return	Protocal对象
	 */
	public static Protocal createPErrorResponse(int errorCode, String errorMsg, int user_id) {
		return new Protocal(ProtocalType.S.FROM_SERVER_TYPE_OF_RESPONSE$FOR$ERROR, 
				create(new PErrorResponse(errorCode, errorMsg)), 0, user_id);
	}

	/**
	 * 解析错误响应消息对象(该对象由客户端接收)
	 * @param dataContentOfProtocal	字符串
	 * @return	错误的响应信息类
	 */
	public static PErrorResponse parsePErrorResponse(String dataContentOfProtocal) {
		return (PErrorResponse)parse(dataContentOfProtocal, PErrorResponse.class);
	}

	/**
	 * 创建用户注消登陆消息对象(该对象由客户端发出)
	 * @param user_id	用户ID
	 * @param loginName	登陆名
	 * @return	Protocal对象
	 */
	public static Protocal createPLoginoutInfo(int user_id, String loginName) {
		return new Protocal(ProtocalType.C.FROM_CLIENT_TYPE_OF_LOGOUT
				//, create(new PLogoutInfo(user_id, loginName))
				, null
				, user_id, 0);
	}

	/**
	 * 创建用户登陆消息对象(该对象由客户端发出)
	 * @param loginName	登陆时提交的用户名：此用户名对框架来说可以随意，具体意义由上层逻辑决即可
	 * @param loginPsw	登陆时提交的密码：此密码对框架来说可以随意，具体意义由上层逻辑决即可
	 * @param extra	额外信息字符串。本字段目前为保留字段，供上层应用自行放置需要的内容
	 * @return	Protocal对象
	 */
	public static Protocal createPLoginInfo(String loginName, String loginPsw, String extra) {
		return new Protocal(ProtocalType.C.FROM_CLIENT_TYPE_OF_LOGIN
				, create(new PLoginInfo(loginName, loginPsw, extra)), -1, 0);
	}

	/**
	 * 解析用户登陆消息对象(该对象由服务端接收)
	 * @param dataContentOfProtocal 字符串
	 * @return	登陆信息类
	 */
	public static PLoginInfo parsePLoginInfo(String dataContentOfProtocal) {
		return (PLoginInfo)parse(dataContentOfProtocal, PLoginInfo.class);
	}

	/**
	 * 创建用户登陆响应消息对象(该对象由服务端发出)
	 * @param code	返回码
	 * @param user_id	用户ID
	 * @return	Protocal对象
	 */
	public static Protocal createPLoginInfoResponse(int code, int user_id) {
		return new Protocal(ProtocalType.S.FROM_SERVER_TYPE_OF_RESPONSE$LOGIN, 
				create(new PLoginInfoResponse(code, user_id)), 
				0, 
				user_id, 
				true, Protocal.genFingerPrint());
	}

	/**
	 * 接收用户登陆响应消息对象(该对象由客户端接收)
	 * @param dataContentOfProtocal	字符串
	 * @return	登陆结果响应信息类
	 */
	public static PLoginInfoResponse parsePLoginInfoResponse(String dataContentOfProtocal) {
		return (PLoginInfoResponse)parse(dataContentOfProtocal, PLoginInfoResponse.class);
	}

	/**
	 * 通用消息的Protocal对象新建方法(默认QoS=false)
	 * @param dataContent	要发送的消息内容
	 * @param from_user_id	发送人的user_id
	 * @param to_user_id	接收人的user_id
	 * @param QoS	是否需要QoS支持,true表示需要,否则不需要
	 * @param fingerPrint	消息指纹特征码,为null则表示由系统自动生成指纹码,否则使用本参数指明的指纹码
	 * @return	Protocal对象
	 */
	public static Protocal createCommonData(String dataContent, int from_user_id, int to_user_id, boolean QoS, String fingerPrint) {
		return new Protocal(ProtocalType.C.FROM_CLIENT_TYPE_OF_COMMON$DATA, 
				dataContent, from_user_id, to_user_id, QoS, fingerPrint);
	}

	/**
	 * 通用消息的Protocal对象新建方法(默认QoS=false)
	 * @param dataContent	要发送的消息内容
	 * @param from_user_id	发送人的user_id
	 * @param to_user_id	接收人的user_id
	 * @return	Protocal对象
	 */
	public static Protocal createCommonData(String dataContent, int from_user_id, int to_user_id) {
		return new Protocal(ProtocalType.C.FROM_CLIENT_TYPE_OF_COMMON$DATA, 
				dataContent, from_user_id, to_user_id);
	}

	/**
	 * 客户端from_user_id向to_user_id发送一个QoS机制中需要的"收到消息应答包"
	 * @param from_user_id	发送人的user_id
	 * @param to_user_id	接收人的user_id
	 * @param recievedMessageFingerPrint	已收到的消息包指纹码
	 * @return	Protocal对象
	 */
	public static Protocal createRecivedBack(int from_user_id, int to_user_id, String recievedMessageFingerPrint) {
		return new Protocal(ProtocalType.C.FROM_CLIENT_TYPE_OF_RECIVED
				, recievedMessageFingerPrint, from_user_id, to_user_id);// 该包当然不需要QoS支持！
	}
}