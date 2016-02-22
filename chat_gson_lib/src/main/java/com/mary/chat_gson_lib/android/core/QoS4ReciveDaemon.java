package com.mary.chat_gson_lib.android.core;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import java.util.concurrent.ConcurrentHashMap;
import com.mary.chat_gson_lib.android.ClientCoreSDK;
import com.mary.chat_gson_lib.server.protocal.Protocal;

/**
 * File Name:	QoS4ReciveDaemon
 * Author:      Mary
 * Write Dates: 2016/2/18
 * Description:	用于服务端的S2C模式下的QoS机制中提供对已收到包进行有限生命周期存储并提供
 * 				重复性判断的守护线程
 * 			   【原理】当收到需QoS机制支持消息包时，会把它的唯一特征码（即指纹id）
 * 			   		  存放于本类的“已收到”消息队列中，寿命约为 MESSAGES_VALID_TIME指明的时间,
 * 			   		  每当CHECH_INTERVAL定时检查间隔到来时会对其存活期进行检查,超期将被移除,
 * 			   		  否则允许其继续存活.理论情况下,一个包的最大寿命不可能超过2倍的CHECH_INTERVAL时长。
 *			   【补充说明】"超期”即意味着对方要么已收到应答包(这是QoS机制正 常情况下的表现)
 *			   		  而无需再次重传、要么是已经达到QoS机制的重试极限而无可能再收到重复包
 *			   		  (那么在本类列表中该表也就没有必要再记录了).总之,"超期"是队列中这些消息包
 *			   		  的正常生命周期的终止，无需过多解读。
 *			   【本类存在的意义在于】极端情况下QoS机制中存在因网络丢包导致应答包的丢失而触发重传机制
 *			   		  从而导致消息重复,而本类将维护一个有限时间段内收到的所有需要QoS支持的
 *			   		  消息的指纹列表且提供"重复性"判断机制,从而保证应用层绝不会因为QoS的重传机制
 *			   		  而导致重复收到消息的情况。
 *				当前QoS机制支持全部的C2C、C2S、S2C共3种消息交互场景下的 消息送达质量保证.
 * Change Log:
 * 2016/2/18-09-13---[公司]---[姓名]
 * ......Added|Changed|Delete......
 * --------------------------------
 */
public class QoS4ReciveDaemon {
	/**标识符*/
	private static final String TAG = QoS4ReciveDaemon.class.getSimpleName();
	/**检查线程执行间隔(单位：毫秒),默认5分钟*/
	public static final int CHECH_INTERVAL = 1000 * 300;
	/**一个消息放到在列表中(用于判定重复时使用)的生存时长(单位：毫秒),默认10分钟*/
	public static final int MESSAGES_VALID_TIME = 1000 * 600;
	private ConcurrentHashMap<String, Long> recievedMessages = new ConcurrentHashMap();

	private Handler handler   = null;
	private Runnable runnable = null;
	private boolean running   = false;
	private boolean _excuting = false;
	private Context context   = null;

	private static QoS4ReciveDaemon instance = null;

	public static QoS4ReciveDaemon getInstance(Context context) {
		if (instance == null) {
			instance = new QoS4ReciveDaemon(context);
		}
		return instance;
	}

	public QoS4ReciveDaemon(Context context) {
		this.context = context;
		init();
	}

	private void init() {
		this.handler = new Handler();
		this.runnable = new Runnable() {
			public void run() {
				// 极端情况下本次循环内可能执行时间超过了时间间隔，此处是防止在前一
				// 次还没有运行完的情况下又重复过劲行，从而出现无法预知的错误
				if (!QoS4ReciveDaemon.this._excuting) {
					QoS4ReciveDaemon.this._excuting = true;

					if (ClientCoreSDK.DEBUG) {
						Log.d(QoS4ReciveDaemon.TAG, "【QoS接收方】++++++++++ START 暂存处理线程正在运行中，当前长度" + QoS4ReciveDaemon.this.recievedMessages.size() + ".");
					}

					for (String key : QoS4ReciveDaemon.this.recievedMessages.keySet()) {
						long delta = System.currentTimeMillis() - ((Long)QoS4ReciveDaemon.this.recievedMessages.get(key)).longValue();

						if (delta < MESSAGES_VALID_TIME)
							continue;
						if (ClientCoreSDK.DEBUG)
							Log.d(QoS4ReciveDaemon.TAG, "【QoS接收方】指纹为" + key + "的包已生存" + delta +
									"ms(最大允许" + MESSAGES_VALID_TIME + "ms), 马上将删除之.");
						QoS4ReciveDaemon.this.recievedMessages.remove(key);
					}

				}

				if (ClientCoreSDK.DEBUG) {
					Log.d(QoS4ReciveDaemon.TAG, "【QoS接收方】++++++++++ END 暂存处理线程正在运行中，当前长度" + QoS4ReciveDaemon.this.recievedMessages.size() + ".");
				}

				QoS4ReciveDaemon.this._excuting = false;

				QoS4ReciveDaemon.this.handler.postDelayed(QoS4ReciveDaemon.this.runnable, CHECH_INTERVAL);
			}
		};
	}

	/**
	 * 启动线程
	 * 无论本方法调用前线程是否已经在运行中,都会尝试首先调用 stop()方法，
	 * 以便确保线程被启动前是真正处于停止状态，这也意味着可无害调用本方法
	 * @param immediately true表示立即执行线程作业,
	 *                      否则直到 CHECH_INTERVAL 执行间隔的到来才进行首次作业的执行
	 */
	public void startup(boolean immediately) {
		stop();

		if ((this.recievedMessages != null) && (this.recievedMessages.size() > 0)) {
			for (String key : this.recievedMessages.keySet()) {
				putImpl(key);
			}

		}

		this.handler.postDelayed(this.runnable, immediately ? 0 : CHECH_INTERVAL);

		this.running = true;
	}

	/**
	 * 无条件中断本线程的运行
	 */
	public void stop() {
		this.handler.removeCallbacks(this.runnable);
		this.running = false;
	}

	/**
	 * 线程是否正在运行中
	 * @return	true表示是，否则线路处于停止状态
	 */
	public boolean isRunning() {
		return this.running;
	}

	/**
	 * 向列表中加入一个包的特征指纹
	 * 【注意】本方法只会将指纹码推入，而不是将整个Protocal对象放入列表中
	 * @param p	Protocal对象
	 */
	public void addRecieved(Protocal p) {
		if ((p != null) && (p.isQoS()))
			addRecieved(p.getFp());
	}

	/**
	 * 向列表中加入一个包的特征指纹
	 * @param fingerPrintOfProtocal	消息包的特纹特征码(理论上是唯一的)
	 */
	public void addRecieved(String fingerPrintOfProtocal) {
		if (fingerPrintOfProtocal == null) {
			Log.w(TAG, "无效的 fingerPrintOfProtocal==null!");
			return;
		}

		if (this.recievedMessages.containsKey(fingerPrintOfProtocal)) {
			Log.w(TAG, "【QoS接收方】指纹为" + fingerPrintOfProtocal +
					"的消息已经存在于接收列表中，该消息重复了（原理可能是对方因未收到应答包而错误重传导致），更新收到时间戳哦.");
		}

		putImpl(fingerPrintOfProtocal);
	}

	private void putImpl(String fingerPrintOfProtocal) {
		if (fingerPrintOfProtocal != null)
			this.recievedMessages.put(fingerPrintOfProtocal, Long.valueOf(System.currentTimeMillis()));
	}

	/**
	 * 指定指纹码的Protocal是否已经收到过
	 * 此方法用于QoS机制中在防止因网络丢包导致对方未收到应答时
	 * 而再次发送消息从而导致消息重复时的判断依赖
	 * @param fingerPrintOfProtocal	消息包的特纹特征码(理论上是唯一的)
	 * @return	true表示已经存在，否则不存在
	 */
	public boolean hasRecieved(String fingerPrintOfProtocal) {
		return this.recievedMessages.containsKey(fingerPrintOfProtocal);
	}

	/**
	 * 当前"已收到消息"队列列表的大小
	 * @return	列表大小
	 */
	public int size() {
		return this.recievedMessages.size();
	}
}