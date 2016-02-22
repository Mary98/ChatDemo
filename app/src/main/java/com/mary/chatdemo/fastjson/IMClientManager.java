package com.mary.chatdemo.fastjson;

import android.content.Context;

import com.mary.chat_fastjson_lib.android.ClientCoreSDK;
import com.mary.chat_fastjson_lib.android.conf.ConfigEntity;
import com.mary.chatdemo.fastjson.event.ChatBaseEventImpl;
import com.mary.chatdemo.fastjson.event.ChatTransDataEventImpl;
import com.mary.chatdemo.fastjson.event.MessageQoSEventImpl;

/**
 * File Name:   IMClientManager
 * Author:      Mary
 * Write Dates: 2016/2/22
 * Description:
 * Change Log:
 * 2016/2/22-10-28---[公司]---[姓名]
 * ......Added|Changed|Delete......
 * --------------------------------
 */
public class IMClientManager {
    /** 标识符 */
    private static String TAG = IMClientManager.class.getSimpleName();
    /** 实列对象 【单列模式】*/
    private static IMClientManager instance = null;

    /** MobileIMSDK是否已被初始化. true表示已初化完成，否则未初始化 */
    private boolean init = false;

    /** 基本事件回调 */
    private ChatBaseEventImpl baseEventListener = null;
    /** 实时消息事件回调 */
    private ChatTransDataEventImpl transDataListener = null;
    /** QoS相关事件回调 */
    private MessageQoSEventImpl messageQoSListener = null;
    /** 当前上下文对象 */
    private Context context = null;

    public static IMClientManager getInstance(Context context) {
        if(instance == null)
            instance = new IMClientManager(context);
        return instance;
    }
    /** 私有化构造函数. 【单列模式】*/
    private IMClientManager(Context context) {
        this.context = context;
        initMobileIMSDK();
    }

    /**
     * 初始化核心
     */
    public void initMobileIMSDK() {
        if(!init) {
            // 设置AppKey
            ConfigEntity.appKey = "5418023dfd98c579b6001741";

            // 设置服务器ip和服务器端口
//			ConfigEntity.serverIP = "192.168.82.138";
//			ConfigEntity.serverIP = "rbcore.openmob.net";
//			ConfigEntity.serverUDPPort = 7901;

            // MobileIMSDK核心IM框架的敏感度模式设置
//			ConfigEntity.setSenseMode(SenseMode.MODE_10S);

            // 开启/关闭DEBUG信息输出
//	    	ClientCoreSDK.DEBUG = false;

            // 【特别注意】请确保首先进行核心库的初始化（这是不同于iOS和Java端的地方)
            ClientCoreSDK.getInstance().init(this.context);

            // 设置事件回调
            baseEventListener = new ChatBaseEventImpl();
            transDataListener = new ChatTransDataEventImpl();
            messageQoSListener = new MessageQoSEventImpl();
            // 设置事件回调通知监听
            ClientCoreSDK.getInstance().setChatBaseEvent(baseEventListener);
            ClientCoreSDK.getInstance().setChatTransDataEvent(transDataListener);
            ClientCoreSDK.getInstance().setMessageQoSEvent(messageQoSListener);

            // 标记初始化完成
            init = true;
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        ClientCoreSDK.getInstance().release();
    }

    /**
     * 获取基本事件回调
     * @return	基本事件回调对象
     */
    public ChatBaseEventImpl getBaseEventListener() {
        return baseEventListener;
    }

    /**
     * 获取基本事件回调实时消息事件回调
     * @return	实时消息事件回调对象
     */
    public ChatTransDataEventImpl getTransDataListener() {
        return transDataListener;
    }

    /**
     * 获取QoS相关事件回调
     * @return	QoS相关事件回调对象
     */
    public MessageQoSEventImpl getMessageQoSListener() {
        return messageQoSListener;
    }
}
