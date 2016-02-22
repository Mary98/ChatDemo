package com.mary.chat_gson_lib.server.protocal;

import android.util.Log;

import com.google.gson.Gson;
import java.util.UUID;

/**
 * File Name:	Protocal
 * Author:      Mary
 * Write Dates: 2016/2/18
 * Description:	协议报文对象：相当于一个消息的Bean对象
 * Change Log:
 * 2016/2/18-09-13---[公司]---[姓名]
 * ......Added|Changed|Delete......
 * --------------------------------
 */
public class Protocal {
	/**信息类型*/
	private int type = 0;
	/**信息内容*/
	private String dataContent = null;
	/**来自那个用户*/
	private int from = -1;
	/**发送那个用户*/
	private int to = -1;
	/**指纹：作唯一标识*/
	private String fp = null;
	/**是否启用QoS*/
	private boolean QoS = false;
	/**重复数*/
	private transient int retryCount = 0;

	@Override
	public String toString() {
		return "Protocal{" +
				"type=" + type +
				", dataContent='" + dataContent + '\'' +
				", from=" + from +
				", to=" + to +
				", fp='" + fp + '\'' +
				", QoS=" + QoS +
				", retryCount=" + retryCount +
				'}';
	}

	public Protocal(int type, String dataContent, int from, int to) {
		this(type, dataContent, from, to, false, null);
	}

	public Protocal(int type, String dataContent, int from, int to, boolean QoS, String fingerPrint) {
		this.type = type;
		this.dataContent = dataContent;
		this.from = from;
		this.to = to;
		this.QoS = QoS;

		// 只有在需要QoS支持时才生成指纹，否则浪费数据传输流量
		// 目前一个包的指纹只在对象建立时创建哦
		if ((QoS) && (fingerPrint == null))
			this.fp = genFingerPrint();
		else
			this.fp = fingerPrint;
	}

	public int getType() {
		return this.type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getDataContent() {
		return this.dataContent;
	}

	public void setDataContent(String dataContent) {
		this.dataContent = dataContent;
	}

	public int getFrom() {
		return this.from;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public int getTo() {
		return this.to;
	}

	public void setTo(int to) {
		this.to = to;
	}

	public String getFp() {
		return this.fp;
	}

	public int getRetryCount() {
		return this.retryCount;
	}

	/**
	 * 累加信息数
	 */
	public void increaseRetryCount() {
		this.retryCount += 1;
	}

	/**
	 * 是否启用QoS识别
	 * @return 是否启用QoS识别
	 */
	public boolean isQoS() {
		return this.QoS;
	}

	/**
	 * 将该对象转化为Json字符串
	 * @return Json字符串
	 */
	public String toGsonString() {
		String string = new Gson().toJson(this);
		Log.e("Mary","测试的数据 == " +  string);
		return string;
	}

	/**
	 * 将Json字符串转化为字节数组
	 * @return Json字符串的字节数组
	 */
	public byte[] toBytes() {
		return CharsetHelper.getBytes(toGsonString());
	}

	/**
	 * 获取一个克隆的Protocal对象
	 * @return 一个Protocal对象
	 */
	public Object clone() {
		// 克隆一个Protocal对象（该对象已重置retryCount数值为0）
		Protocal cloneP = new Protocal(getType(), getDataContent(), getFrom(), getTo(), isQoS(), getFp());
		return cloneP;
	}

	/**
	 * 获取一个指纹
	 * @return 指纹：唯一标识
	 */
	public static String genFingerPrint() {
		// 随机生成一个指纹作为唯一标识
		return UUID.randomUUID().toString();
	}
}