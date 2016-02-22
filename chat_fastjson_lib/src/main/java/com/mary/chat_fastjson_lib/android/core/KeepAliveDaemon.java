package com.mary.chat_fastjson_lib.android.core;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import java.util.Observer;
import com.mary.chat_fastjson_lib.android.ClientCoreSDK;

/**
 * File Name:	KeepAliveDaemon
 * Author:      Mary
 * Write Dates: 2016/2/18
 * Description:	用于保持与服务端通信活性的Keep alive独立线程。
 * 				Keep alive的目的有2个：
 *				1、防止NAT路由算法导致的端口老化：
 *				路由器的NAT路由算法存在所谓的“端口老化”概念
 *				(理论上NAT算法中UDP端口老化时间为300S，但这不是标准,而且中高端路由器可由网络管理员自行设定此值),
 *				Keep alive机制可确保在端口老化时间到来前 重置老化时间，
 *				进而实现端口“保活”的目的，否则端口老化导致的后果是服务器将向客户端发送的数据将被路由器抛弃。
 *				2、即时探测由于网络状态的变动而导致的通信中断（进而自动触发自动治愈机制）：
 *				此种情况可的原因有(但不限于):无线网络信号不稳定、WiFi与2G/3G/4G等同开情况下的网络切换、
 *				手机系统的省电策略等。
 * Change Log:
 * 2016/2/18-09-13---[公司]---[姓名]
 * ......Added|Changed|Delete......
 * --------------------------------
 */
public class KeepAliveDaemon {
	/**标识符*/
	private static final String TAG = KeepAliveDaemon.class.getSimpleName();
	/**互联网连接超时*/
	public static int NETWORK_CONNECTION_TIME_OUT = 10 * 1000;
	/**保持激活时间间隔*/
	public static int KEEP_ALIVE_INTERVAL = 3 * 1000;

	private Handler handler = null;
	private Runnable runnable = null;
	private boolean keepAliveRunning = false;
	private long lastGetKeepAliveResponseFromServerTimstamp = 0L;

	private Observer networkConnectionLostObserver = null;
	private boolean _excuting = false;
	private Context context = null;

	private static KeepAliveDaemon instance = null;
	
	public static KeepAliveDaemon getInstance(Context context) {
		if (instance == null)
			instance = new KeepAliveDaemon(context);
		return instance;
	}

	private KeepAliveDaemon(Context context) {
		this.context = context;
		init();
	}

	private void init() {
		this.handler = new Handler();
		this.runnable = new Runnable()
		{
			public void run()
			{
				// 极端情况下本次循环内可能执行时间超过了时间间隔，此处是防止在前一
				// 次还没有运行完的情况下又重复过劲行，从而出现无法预知的错误
				if (!KeepAliveDaemon.this._excuting) {
					new AsyncTask() {
						private boolean willStop = false;

						protected Integer doInBackground(Object[] params) {
							KeepAliveDaemon.this._excuting = true;
							if (ClientCoreSDK.DEBUG)
								Log.d(KeepAliveDaemon.TAG, "【IMCORE】【调试】心跳线程执行中...");
							int code = LocalUDPDataSender.getInstance(KeepAliveDaemon.this.context).sendKeepAlive();

							return Integer.valueOf(code);
						}

						@Override
						protected void onPostExecute(Object object) {
							Integer code = Integer.valueOf(object.toString());
							boolean isInitialedForKeepAlive = 
									KeepAliveDaemon.this.lastGetKeepAliveResponseFromServerTimstamp == 0L;
							if ((code.intValue() == 0) 
									&& (KeepAliveDaemon.this.lastGetKeepAliveResponseFromServerTimstamp == 0L)) {
								KeepAliveDaemon.this.lastGetKeepAliveResponseFromServerTimstamp = System.currentTimeMillis();
							}

							if (!isInitialedForKeepAlive) {
								long now = System.currentTimeMillis();

								// 当当前时间与最近一次服务端的心跳响应包时间间隔>= 10秒就判定当前与服务端的网络连接已断开
								if (now - KeepAliveDaemon.this.lastGetKeepAliveResponseFromServerTimstamp 
										>= KeepAliveDaemon.NETWORK_CONNECTION_TIME_OUT) {
									KeepAliveDaemon.this.stop();

									if (KeepAliveDaemon.this.networkConnectionLostObserver != null) {
										KeepAliveDaemon.this.networkConnectionLostObserver.update(null, null);
									}
									this.willStop = true;
								}
							}

							KeepAliveDaemon.this._excuting = false;
							if (!this.willStop) {
								// 开始下一个心跳循环
								KeepAliveDaemon.this.handler.postDelayed (
										KeepAliveDaemon.this.runnable
										, KeepAliveDaemon.KEEP_ALIVE_INTERVAL);
							}
						}
					}.execute(new Object[0]);
				}
			}
		};
	}

	public void stop() {
		this.handler.removeCallbacks(this.runnable);
		this.keepAliveRunning = false;
		this.lastGetKeepAliveResponseFromServerTimstamp = 0L;
	}

	public void start(boolean immediately) {
		stop();
		this.handler.postDelayed(this.runnable, immediately ? 0 : KEEP_ALIVE_INTERVAL);
		this.keepAliveRunning = true;
	}

	public boolean isKeepAliveRunning()
	{
		return this.keepAliveRunning;
	}

	public void updateGetKeepAliveResponseFromServerTimstamp() {
		this.lastGetKeepAliveResponseFromServerTimstamp = System.currentTimeMillis();
	}

	public void setNetworkConnectionLostObserver(Observer networkConnectionLostObserver) {
		this.networkConnectionLostObserver = networkConnectionLostObserver;
	}
}