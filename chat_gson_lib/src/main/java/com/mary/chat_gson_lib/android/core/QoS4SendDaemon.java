package com.mary.chat_gson_lib.android.core;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.mary.chat_gson_lib.android.ClientCoreSDK;
import com.mary.chat_gson_lib.server.protocal.Protocal;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

/**
 * File Name:	QoS4SendDaemon
 * Author:      Mary
 * Write Dates: 2016/2/18
 * Description: QoS数据包质量包证机制之发送队列保证实现类
 * 				本类是QoS机制的核心，极端情况下将弥补因UDP协议天生的不可靠性而带来的丢包情况。
 *				当前QoS机制支持全部的C2C、C2S、S2C共3种消息交互场景下的消息送达质量保证.
 * Change Log:
 * 2016/2/18-09-13---[公司]---[姓名]
 * ......Added|Changed|Delete......
 * --------------------------------
 */
public class QoS4SendDaemon {
	/**标识符*/
	private static final String TAG = QoS4SendDaemon.class.getSimpleName();
	// 并发Hash，因为本类中可能存在不同的线程同时remove或遍历之
	private ConcurrentHashMap<String, Protocal> sentMessages = new ConcurrentHashMap<String, Protocal>();
	// 关发Hash，因为本类中可能存在不同的线程同时remove或遍历之
	private ConcurrentHashMap<String, Long> sendMessagesTimestamp = new ConcurrentHashMap<String, Long>();
	/**QoS质量保证线程心跳间隔(单位：毫秒),默认5000ms*/
	public static final int CHECH_INTERVAL = 1000 * 5;
	/**"刚刚"发出的消息阀值定义(单位：毫秒),默认3000毫秒*/
	public static final int MESSAGES_JUST$NOW_TIME = 1000 * 3;
	/**一个包允许的最大重发次数，默认3次*/
	public static final int QOS_TRY_COUNT = 3;

	private Handler handler   = null;
	private Runnable runnable = null;
	private boolean running   = false;
	private boolean _excuting = false;
	private Context context   = null;

	private static QoS4SendDaemon instance = null;

	public static QoS4SendDaemon getInstance(Context context) {
		if (instance == null) {
			instance = new QoS4SendDaemon(context);
		}
		return instance;
	}

	private QoS4SendDaemon(Context context) {
		this.context = context;
		init();
	}

	private void init() {
		this.handler = new Handler();
		this.runnable = new Runnable() {
			public void run() {
				// 极端情况下本次循环内可能执行时间超过了时间间隔，此处是防止在前一
				// 次还没有运行完的情况下又重复执行，从而出现无法预知的错误
				if (!QoS4SendDaemon.this._excuting) {
					new AsyncTask() {
						private ArrayList<Protocal> lostMessages = new ArrayList();

						protected ArrayList<Protocal> doInBackground(Object[] params) {
							QoS4SendDaemon.this._excuting = true;
							try {
								if (ClientCoreSDK.DEBUG) {
									Log.d(TAG, "【QoS】=========== 消息发送质量保证线程运行中, 当前需要处理的列表长度为"
													+ QoS4SendDaemon.this.sentMessages.size() + "...");
								}

								for (String key : QoS4SendDaemon.this.sentMessages.keySet()) {
									Protocal p = (Protocal)QoS4SendDaemon.this.sentMessages.get(key);
									if ((p != null) && (p.isQoS())) {
										if (p.getRetryCount() >= QOS_TRY_COUNT) {
											if (ClientCoreSDK.DEBUG) {
												Log.d(QoS4SendDaemon.TAG
														, "【QoS】指纹为" + p.getFp() +
														"的消息包重传次数已达" + p.getRetryCount() + "(最多" + QOS_TRY_COUNT + "次)上限，将判定为丢包！");
											}

											this.lostMessages.add((Protocal)p.clone());
											QoS4SendDaemon.this.remove(p.getFp());
										} else {
											long delta = System.currentTimeMillis() - ((Long)QoS4SendDaemon.this.sendMessagesTimestamp.get(key)).longValue();

											if (delta <= MESSAGES_JUST$NOW_TIME) {
												if (ClientCoreSDK.DEBUG) {
													Log.w(TAG, "【QoS】指纹为"
															+ key + "的包距\"刚刚\"发出才" + delta 
															+ "ms(<=" + MESSAGES_JUST$NOW_TIME 
															+ "ms将被认定是\"刚刚\"), 本次不需要重传哦.");
												}
											} else {
												new LocalUDPDataSender.SendCommonDataAsync(QoS4SendDaemon.this.context, p) {
													protected void onPostExecute(Integer code) {
														if (code.intValue() == 0) {
															this.p.increaseRetryCount();

															if (ClientCoreSDK.DEBUG)
																Log.d(TAG, "【QoS】指纹为" + this.p.getFp() +
																		"的消息包已成功进行重传，此次之后重传次数已达" + 
																		this.p.getRetryCount() + "(最多" + QOS_TRY_COUNT + "次).");
														} else {
															Log.w(TAG, "【QoS】指纹为" + this.p.getFp() +
																	"的消息包重传失败，它的重传次数之前已累计为" + 
																	this.p.getRetryCount() + "(最多" + QOS_TRY_COUNT + "次).");
														}
													}
												}.execute(new Object[0]);
											}
										}
									} else {
										QoS4SendDaemon.this.remove(key);
									}
								}
							} catch (Exception e) {
								Log.w(QoS4SendDaemon.TAG, "【QoS】消息发送质量保证线程运行时发生异常," + e.getMessage(), e);
							}

							return this.lostMessages;
						}

						@Override
						protected void onPostExecute(Object o) {
							// super.onPostExecute(o);
							ArrayList<Protocal> al = (ArrayList<Protocal>) o;
							if ((al != null) && (al.size() > 0)) {
								QoS4SendDaemon.this.notifyMessageLost(al);
							}

							QoS4SendDaemon.this._excuting = false;
							QoS4SendDaemon.this.handler.postDelayed(QoS4SendDaemon.this.runnable, 5000L);
						}
					}
					.execute(new Object[0]);
				}
			}
		};
	}

	/**
	 * 将未送达信息反馈给消息监听者
	 * @param lostMessages	已被判定为“消息未送达”的消息列表
	 */
	protected void notifyMessageLost(ArrayList<Protocal> lostMessages) {
		if (ClientCoreSDK.getInstance().getMessageQoSEvent() != null)
			ClientCoreSDK.getInstance().getMessageQoSEvent().messagesLost(lostMessages);
	}

	/**
	 * 启动线程
	 * 无论本方法调用前线程是否已经在运行中，都会尝试首先调用 stop()方法，
	 * 以便确保线程被启动前是真正处于停止状态，这也意味着可无害调用本方法
	 * @param immediately	true表示立即执行线程作业，
	 *                      否则直到 CHECH_INTERVAL 执行间隔的到来才进行首次作业的执行
	 */
	public void startup(boolean immediately) {
		stop();
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
	 * @return	true正在运行，否则停止运行
	 */
	public boolean isRunning() {
		return this.running;
	}

	/**
	 * 该包是否已存在于队列中
	 * @param fingerPrint	消息包的特纹特征码(理论上是唯一的)
	 * @return	true表示已存在，否则不存在
	 */
	boolean exist(String fingerPrint) {
		return this.sentMessages.get(fingerPrint) != null;
	}

	/**
	 * 推入一个消息包的指纹特征码
	 * 【注意】本方法只会将指纹码推入，而不是将整个Protocal对象放入列表中
	 * @param p	消息包对象
	 */
	public void put(Protocal p) {
		if (p == null) {
			Log.w(TAG, "Invalid arg p==null.");
			return;
		}
		if (p.getFp() == null) {
			Log.w(TAG, "Invalid arg p.getFp() == null.");
			return;
		}

		if (!p.isQoS()) {
			Log.w(TAG, "This protocal is not QoS pkg, ignore it!");
			return;
		}

		if (this.sentMessages.get(p.getFp()) != null) {
			Log.w(TAG, "【QoS】指纹为" + p.getFp() + "的消息已经放入了发送质量保证队列，该消息为何会重复？（生成的指纹码重复？还是重复put？）");
		}

		// save it
		sentMessages.put(p.getFp(), p);
		// 同时保存时间戳
		sendMessagesTimestamp.put(p.getFp(), System.currentTimeMillis());
	}

	/**
	 * 移除一个消息包
	 * @param fingerPrint	消息包的特纹特征码(理论上是唯一的)
	 */
	public void remove(final String fingerPrint) {
		new AsyncTask(){
			@Override
			protected Object doInBackground(Object... params) {
				sendMessagesTimestamp.remove(fingerPrint);
				return sentMessages.remove(fingerPrint);
			}

			protected void onPostExecute(Object result) {
				Log.w(TAG, "【QoS】指纹为"+fingerPrint+"的消息已成功从发送质量保证队列中移除(可能是收到接收方的应答也可能是达到了重传的次数上限)，重试次数="
						+(result != null?((Protocal)result).getRetryCount():"none呵呵."));
			}
		}.execute();
	}

	/**
	 * 队列大小
	 * @return	队列大小
	 */
	public int size() {
		return this.sentMessages.size();
	}
}