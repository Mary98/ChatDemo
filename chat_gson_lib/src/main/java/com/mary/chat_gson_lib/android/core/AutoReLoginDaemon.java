package com.mary.chat_gson_lib.android.core;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import com.mary.chat_gson_lib.android.ClientCoreSDK;

/**
 * File Name:	AutoReLoginDaemon
 * Author:      Mary
 * Write Dates: 2016/2/17
 * Description: 与服务端通信中断后的自动登陆（重连）独立线程。
 *              鉴于无线网络的不可靠性和特殊性，移动端的即时通讯经常存在网络通信断断续续的状况，
 *              可能的原因有（但不限于）：无线网络信号不稳定、WiFi与2G/3G/4G等同开情 况下的网络切换、
 *              						手机系统的省电策略等。
 *              这就使得即时通信框架拥有对上层透明且健壮的健康度探测和自动治愈机制非常有必要。
 *				本类的存在使得通讯拥有通讯自动治愈的能力。
 *				【注意】自动登陆（重连）只可能发生在登陆成功后与服务端的网络通信断开时。
 * Change Log:
 * 2016/2/17-14-26---[公司]---[姓名]
 * ......Added|Changed|Delete......
 * --------------------------------
 */
public class AutoReLoginDaemon {
	/**标识符*/
	private static final String TAG = AutoReLoginDaemon.class.getSimpleName();
	/**自动重新登陆时间间隔（单位：毫秒），默认2000毫秒。*/
	public static int AUTO_RE$LOGIN_INTERVAL = 1000 * 2;

	private Handler handler   = null;
	private Runnable runnable = null;
	/**是否正在重新登陆*/
	private boolean autoReLoginRunning = false;
	/**执行中*/
	private boolean _excuting = false;
	/**单例实例对象*/
	private static AutoReLoginDaemon instance = null;
	/**上下文对象*/
	private Context context = null;
	/**获取单例实例对象*/
	public static AutoReLoginDaemon getInstance(Context context) {
		if (instance == null)
			instance = new AutoReLoginDaemon(context);
		return instance;
	}

	/**私有化构造方法*/
	private AutoReLoginDaemon(Context context) {
		this.context = context;
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		this.handler = new Handler();
		this.runnable = new Runnable() {
			public void run() {
				if (!AutoReLoginDaemon.this._excuting) {
					new AsyncTask() {
						protected Integer doInBackground(Object[] params) {
							AutoReLoginDaemon.this._excuting = true;
							if (ClientCoreSDK.DEBUG)
								Log.e(AutoReLoginDaemon.TAG, "【IMCORE】自动重新登陆线程执行中, autoReLogin?" + ClientCoreSDK.autoReLogin + "...");
							int code = -1;

							if (ClientCoreSDK.autoReLogin) {
								code = LocalUDPDataSender.getInstance(
										AutoReLoginDaemon.this.context).sendLogin(
												ClientCoreSDK.getInstance().getCurrentLoginName()
												, ClientCoreSDK.getInstance().getCurrentLoginPsw()
												, ClientCoreSDK.getInstance().getCurrentLoginExtra());
							}
							return Integer.valueOf(code);
						}

						@Override
						protected void onPostExecute(Object object) {
							// super.onPostExecute(o);
							Integer result = Integer.valueOf(object.toString());
							if (result.intValue() == 0) {
								// *********************** 同样的代码也存在于LocalUDPDataSender.SendLoginDataAsync中的代码
								// 登陆消息成功发出后就启动本地消息侦听线程：
								// 第1）种情况：首次使用程序时，登陆信息发出时才启动本地监听线程是合理的；
								// 第2）种情况：因网络原因（比如服务器关闭或重启）而导致本地监听线程中断的问题：
								//      当首次登陆后，因服务端或其它网络原因导致本地监听出错，将导致中断本地监听线程，
								//	          所以在此处在自动登陆重连或用户自已手机尝试再次登陆时重启监听线程就可以恢复本地
								//	          监听线程的运行。
								LocalUDPDataReciever.getInstance(AutoReLoginDaemon.this.context).startup();
							}

							//
							_excuting = false;
							// 开始下一个心跳循环
							handler.postDelayed(runnable, AUTO_RE$LOGIN_INTERVAL);
						}

					}
					.execute(new Object[0]);
				}
			}
		};
	}

	/**
	 * 无条件中断本线程的运行
	 */
	public void stop() {
		this.handler.removeCallbacks(this.runnable);
		this.autoReLoginRunning = false;
	}

	/**
	 * 启动线程
	 * 无论本方法调用前线程是否已经在运行中，都会尝试首先调用 stop()方法，
	 * 以便确保线程被启动前是真正处于停止状态，这也意味着可无害调用本方法。
	 * @param immediately 立即启动线程还是延时启动线程
	 */
	public void start(boolean immediately) {
		stop();
		this.handler.postDelayed(this.runnable, immediately ? 0 : AUTO_RE$LOGIN_INTERVAL);
		this.autoReLoginRunning = true;
	}

	/**
	 * 重新连接服务器线程是否正在运行中
	 * @return 是否正在重新连接服务器
	 */
	public boolean isautoReLoginRunning() {
		return this.autoReLoginRunning;
	}
}