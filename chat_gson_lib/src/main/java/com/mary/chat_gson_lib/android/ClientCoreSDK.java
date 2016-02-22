package com.mary.chat_gson_lib.android;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.mary.chat_gson_lib.android.core.AutoReLoginDaemon;
import com.mary.chat_gson_lib.android.core.KeepAliveDaemon;
import com.mary.chat_gson_lib.android.core.LocalUDPDataReciever;
import com.mary.chat_gson_lib.android.core.LocalUDPSocketProvider;
import com.mary.chat_gson_lib.android.core.QoS4ReciveDaemon;
import com.mary.chat_gson_lib.android.core.QoS4SendDaemon;
import com.mary.chat_gson_lib.android.event.ChatBaseEvent;
import com.mary.chat_gson_lib.android.event.ChatTransDataEvent;
import com.mary.chat_gson_lib.android.event.MessageQoSEvent;

/**
 * File Name:   ClientCoreSDK
 * Author:      Mary
 * Write Dates: 2016/2/17
 * Description: 核心入口类
 *              本类主要提供一些全局参数的读取和设置。
 * Change Log:
 * 2016/2/17-14-26---[公司]---[姓名]
 * ......Added|Changed|Delete......
 * --------------------------------
 */
public class ClientCoreSDK {
    /**标识符*/
    private static final String TAG = ClientCoreSDK.class.getSimpleName();
    /**true表示开启 Debug信息在Logcat下的输出，否则关闭*/
    public static boolean DEBUG = true;
    /**
     * 是否在登陆成功后掉线时自动重新登陆线程中实质性发起登陆请求，
     * true表示将在线程 运行周期中正常发起，否则不发起（即关闭实质性的重新登陆请求）。
     */
    public static boolean autoReLogin = true;
    /**单例实例对象*/
    private static ClientCoreSDK instance = null;

    private boolean _init = false;

    private boolean localDeviceNetworkOk = true;

    private boolean connectedToServer = true;

    private boolean loginHasInit = false;

    private int currentUserId = -1;

    private String currentLoginName = null;

    private String currentLoginPsw = null;

    private String currentLoginExtra = null;

    private ChatBaseEvent chatBaseEvent = null;

    private ChatTransDataEvent chatTransDataEvent = null;

    private MessageQoSEvent messageQoSEvent = null;

    private Context context = null;

    private final BroadcastReceiver networkConnectionStatusBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
            NetworkInfo mobNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (!(mobNetInfo != null && mobNetInfo.isConnected())
                    && !(wifiNetInfo != null && wifiNetInfo.isConnected())) {
                Log.e(ClientCoreSDK.TAG, "【本地网络通知】检测本地网络连接断开了!");

                ClientCoreSDK.this.localDeviceNetworkOk = false;
                LocalUDPSocketProvider.getInstance().closeLocalUDPSocket();
            } else {
                if (ClientCoreSDK.DEBUG) {
                    Log.e(ClientCoreSDK.TAG, "【本地网络通知】检测本地网络已连接上了!");
                }

                ClientCoreSDK.this.localDeviceNetworkOk = true;
                LocalUDPSocketProvider.getInstance().closeLocalUDPSocket();
            }
        }
    };

    public static ClientCoreSDK getInstance() {
        if (instance == null)
            instance = new ClientCoreSDK();
        return instance;
    }

    public void init(Context _context) {
        if (!this._init) {
            if (_context == null) {
                throw new IllegalArgumentException("context can't be null!");
            }

            // 将全局Application作为context上下文句柄：
            //   由于Android程序的特殊性，整个APP的生命周中除了Application外，其它包括Activity在内
            //   都可能是短命且不可靠的（随时可能会因虚拟机资源不足而被回收），所以MobileIMSDK作为跟
            //   整个APP的生命周期保持一致的全局资源，它的上下文用Application是最为恰当的。
            if(_context instanceof Application)
                this.context = _context;
            else {
                this.context = _context.getApplicationContext();
            }

            // Register for broadcasts when network status changed
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            this.context.registerReceiver(networkConnectionStatusBroadcastReceiver, intentFilter);

            this._init = true;
        }
    }

    public void release() {
        // 尝试停掉掉线重连线程（如果线程正在运行的话）
        AutoReLoginDaemon.getInstance(context).stop(); // 2014-11-08 add by Jack Jiang
        // 尝试停掉QoS质量保证（发送）心跳线程
        QoS4SendDaemon.getInstance(context).stop();
        // 尝试停掉Keep Alive心跳线程
        KeepAliveDaemon.getInstance(context).stop();
        // 尝试停掉消息接收者
        LocalUDPDataReciever.getInstance(context).stop();
        // 尝试停掉QoS质量保证（接收防重复机制）心跳线程
        QoS4ReciveDaemon.getInstance(context).stop();
        // 尝试关闭本地Socket
        LocalUDPSocketProvider.getInstance().closeLocalUDPSocket();

        try {
            this.context.unregisterReceiver(this.networkConnectionStatusBroadcastReceiver);
        } catch (Exception e) {
            Log.w(TAG, e.getMessage(), e);
        }

        this._init = false;

        setLoginHasInit(false);
        setConnectedToServer(false);
    }

    public int getCurrentUserId() {
        return this.currentUserId;
    }

    public ClientCoreSDK setCurrentUserId(int currentUserId) {
        this.currentUserId = currentUserId;
        return this;
    }

    public String getCurrentLoginName() {
        return this.currentLoginName;
    }

    public ClientCoreSDK setCurrentLoginName(String currentLoginName) {
        this.currentLoginName = currentLoginName;
        return this;
    }

    public String getCurrentLoginPsw() {
        return this.currentLoginPsw;
    }

    public void setCurrentLoginPsw(String currentLoginPsw) {
        this.currentLoginPsw = currentLoginPsw;
    }

    public String getCurrentLoginExtra() {
        return currentLoginExtra;
    }

    public ClientCoreSDK setCurrentLoginExtra(String currentLoginExtra) {
        this.currentLoginExtra = currentLoginExtra;
        return this;
    }

    public boolean isLoginHasInit() {
        return this.loginHasInit;
    }

    public ClientCoreSDK setLoginHasInit(boolean loginHasInit) {
        this.loginHasInit = loginHasInit;

        return this;
    }

    public boolean isConnectedToServer() {
        return this.connectedToServer;
    }

    public void setConnectedToServer(boolean connectedToServer) {
        this.connectedToServer = connectedToServer;
    }

    public boolean isInitialed() {
        return this._init;
    }

    public boolean isLocalDeviceNetworkOk() {
        return this.localDeviceNetworkOk;
    }

    public void setChatBaseEvent(ChatBaseEvent chatBaseEvent) {
        this.chatBaseEvent = chatBaseEvent;
    }

    public ChatBaseEvent getChatBaseEvent() {
        return this.chatBaseEvent;
    }

    public void setChatTransDataEvent(ChatTransDataEvent chatTransDataEvent) {
        this.chatTransDataEvent = chatTransDataEvent;
    }

    public ChatTransDataEvent getChatTransDataEvent() {
        return this.chatTransDataEvent;
    }

    public void setMessageQoSEvent(MessageQoSEvent messageQoSEvent) {
        this.messageQoSEvent = messageQoSEvent;
    }

    public MessageQoSEvent getMessageQoSEvent() {
        return this.messageQoSEvent;
    }
}
