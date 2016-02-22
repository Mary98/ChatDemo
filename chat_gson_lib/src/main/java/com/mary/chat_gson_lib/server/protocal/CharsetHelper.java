package com.mary.chat_gson_lib.server.protocal;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * File Name:	CharsetHelper
 * Author:      Mary
 * Write Dates: 2016/2/18
 * Description:	数据交互的编解码实现类
 * Change Log:
 * 2016/2/18-09-13---[公司]---[姓名]
 * ......Added|Changed|Delete......
 * --------------------------------
 */
public class CharsetHelper {
	public static final CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
	/**字符编码格式*/
	public static final String ENCODE_CHARSET  = "UTF-8";
	/**字符解码格式*/
	public static final String DECODE_CHARSET  = "UTF-8";

	/**
	 * 将byte数据使用服务端设定的解码方式组织成字符串
	 * @param b	byte数据
	 * @param len	从0开始的总长度
	 * @return	解码后的字符串结果
	 */
	public static String getString(byte[] b, int len) {
		try {
			return new String(b, 0 , len, DECODE_CHARSET);
		}
		// 如果是不支持的字符类型则按默认字符集进行解码
		catch (UnsupportedEncodingException e) {
			return new String(b, 0 , len);
		}
	}

	/**
	 * 将byte数据使用服务端设定的解码方式组织成字符串
	 * @param b	byte数据
	 * @param start	起始索引
	 * @param len	从0开始的总长度
	 * @return	解码后的字符串结果
	 */
	public static String getString(byte[] b, int start, int len) {
		try {
			return new String(b, start , len, DECODE_CHARSET);
		}
		// 如果是不支持的字符类型则按默认字符集进行解码
		catch (UnsupportedEncodingException e) {
			return new String(b, start , len);
		}
	}

	/**
	 * 将字符串按设置的方式编码成byte数组
	 * @param str	字符串
	 * @return	编码后的byte数组结果
	 */
	public static byte[] getBytes(String str) {
		if(str != null) {
			try {
				return str.getBytes(ENCODE_CHARSET);
			}
			// 如果是不支持的字符类型则按默认字符集进行编码
			catch (UnsupportedEncodingException e) {
				return str.getBytes();
			}
		}
		return new byte[0];
	}
}