package com.mary.chat_fastjson_lib.android.core;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Observable;
import java.util.Observer;

import com.mary.chat_fastjson_lib.android.ClientCoreSDK;
import com.mary.chat_fastjson_lib.android.conf.ConfigEntity;
import com.mary.chat_fastjson_lib.server.protocal.Protocal;
import com.mary.chat_fastjson_lib.server.protocal.ProtocalFactory;
import com.mary.chat_fastjson_lib.server.protocal.ProtocalType;
import com.mary.chat_fastjson_lib.server.protocal.s.PErrorResponse;
import com.mary.chat_fastjson_lib.server.protocal.s.PLoginInfoResponse;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * File Name:	LocalUDPDataReciever
 * Author:      Mary
 * Write Dates: 2016/2/18
 * Description:	数据接收处理独立线程
 * 				主要工作是将收到的数据进行解析并按MobileIMSDK框架的协议进行调度和处理。
 * 				本类是MobileIMSDK框架数据接收处理的唯一实现类，也是整个框架算法最为关 键的部分。
 * 				本线程的启停，目前属于MobileIMSDK算法的一部分，暂时无需也不建议由应用层自行调用。
 * Change Log:
 * 2016/2/18-09-13---[公司]---[姓名]
 * ......Added|Changed|Delete......
 * --------------------------------
 */
public class LocalUDPDataReciever {
	/** 标识符 */
	private static final String TAG = LocalUDPDataReciever.class.getSimpleName();
	/**独立线程*/
	private Thread thread = null;
	/** 单例实例 */
	private static LocalUDPDataReciever instance = null;
	/**消息处理者*/
	private static MessageHandler messageHandler = null;
	/**上下文对象*/
	private Context context = null;

	/**
	 * 获取单例实例
	 * @param context 上下文对象
	 * @return 单例实例
	 */
	public static LocalUDPDataReciever getInstance(Context context) {
		if (instance == null) {
			instance = new LocalUDPDataReciever(context);
			messageHandler = new MessageHandler(context);
		}
		return instance;
	}

	/**
	 * 私有化构造方法
	 * @param context 上下文对象
	 */
	private LocalUDPDataReciever(Context context) {
		this.context = context;
	}

	/**
	 * 无条件中断本线程的运行
	 * 本线程的启停，目前属于MobileIMSDK算法的一部分，暂时无需也不建议由应用层自行调用。
	 */
	public void stop() {
		if (this.thread != null) {
			this.thread.interrupt();
			this.thread = null;
		}
	}

	/**
	 * 启动线程
	 * 无论本方法调用前线程是否已经在运行中，都会尝试首先调用 stop()方法，
	 * 以便确保线程被启动前是真正处于停止状态，这也意味着可无害调用本方法。
	 */
	public void startup() {
		stop();
		try {
			this.thread = new Thread(new Runnable() {
				public void run() {
					if (ClientCoreSDK.DEBUG)
						Log.d(TAG, "接收UDP数据监听线程即将开始");
					try {
						if (ClientCoreSDK.DEBUG) {
							Log.e(LocalUDPDataReciever.TAG, "本地UDP端口侦听中，端口=" + ConfigEntity.localUDPPort + "...");
						}

						//开始侦听
						LocalUDPDataReciever.this.p2pListeningImpl();
					} catch (Exception e) {
						Log.d(LocalUDPDataReciever.TAG, "本地UDP监听停止了(socket被关闭了?)," + e.getMessage(), e);
					}
				}
			});
			if (ClientCoreSDK.DEBUG)
				Log.d(TAG, "接收UDP数据监听线程即将结束");
			this.thread.start();
		} catch (Exception e) {
			Log.d(TAG, "本地UDPSocket监听开启时发生异常," + e.getMessage(), e);
		}
	}

	private void p2pListeningImpl() throws Exception {
		while (true) {
			// 缓冲区
			byte[] data = new byte[1024];
			// 接收数据报的包
			DatagramPacket packet = new DatagramPacket(data, data.length);
			DatagramSocket localUDPSocket = LocalUDPSocketProvider.getInstance().getLocalUDPSocket();
			if ((localUDPSocket == null) || (localUDPSocket.isClosed())) {
				continue;
			}
			localUDPSocket.receive(packet);

			Message m = Message.obtain();
			m.obj = packet;
			messageHandler.sendMessage(m);
		}
	}

	private static class MessageHandler extends Handler {
		/**上下文对象*/
		private Context context = null;
		/**构造方法*/
		public MessageHandler(Context context) {
			this.context = context;
		}

		/**
		 * 处理信息
		 * @param msg 信息
		 */
		@Override
		public void handleMessage(Message msg) {
			DatagramPacket packet = (DatagramPacket) msg.obj;
			if (packet == null) return;

			try {
				Protocal pFromServer = ProtocalFactory.parse(packet.getData(), packet.getLength());
				if (pFromServer.isQoS()) {
					if (QoS4ReciveDaemon.getInstance(this.context).hasRecieved(pFromServer.getFp())) {
						if (ClientCoreSDK.DEBUG) {
							Log.d(TAG, "【QoS】" + pFromServer.getFp() + "已经存在于发送列表中，这是重复包，通知应用层收到该包！");
						}
						QoS4ReciveDaemon.getInstance(this.context).addRecieved(pFromServer);
						sendRecievedBack(pFromServer);
						return;
					}

					QoS4ReciveDaemon.getInstance(this.context).addRecieved(pFromServer);

					sendRecievedBack(pFromServer);
				}

				switch (pFromServer.getType()) {
					case ProtocalType.C.FROM_CLIENT_TYPE_OF_COMMON$DATA: {
						if (ClientCoreSDK.getInstance().getChatTransDataEvent() == null)
							break;
						ClientCoreSDK.getInstance().getChatTransDataEvent().onTransBuffer(
								pFromServer.getFp(), pFromServer.getFrom(), pFromServer.getDataContent());
	
						break;
					}
					case ProtocalType.S.FROM_SERVER_TYPE_OF_RESPONSE$KEEP$ALIVE: {
						if (ClientCoreSDK.DEBUG) {
							Log.d(TAG, "收到服务端回过来的Keep Alive心跳响应包.");
						}
						KeepAliveDaemon.getInstance(this.context).updateGetKeepAliveResponseFromServerTimstamp();
						break;
					}
					case ProtocalType.C.FROM_CLIENT_TYPE_OF_RECIVED: {
						String theFingerPrint = pFromServer.getDataContent();
						if (ClientCoreSDK.DEBUG) {
							Log.d(TAG, "【QoS】收到" + pFromServer.getFrom() + "发过来的指纹为" + theFingerPrint + "的应答包.");
						}
	
						if (ClientCoreSDK.getInstance().getMessageQoSEvent() != null) {
							ClientCoreSDK.getInstance().getMessageQoSEvent().messagesBeReceived(theFingerPrint);
						}
	
						QoS4SendDaemon.getInstance(this.context).remove(theFingerPrint);
						break;
					}
					case ProtocalType.S.FROM_SERVER_TYPE_OF_RESPONSE$LOGIN: {
						PLoginInfoResponse loginInfoRes = ProtocalFactory.parsePLoginInfoResponse(pFromServer.getDataContent());
	
						if (loginInfoRes.getCode() == 0) {
							ClientCoreSDK.getInstance()
								.setLoginHasInit(true)
								.setCurrentUserId(loginInfoRes.getUser_id());
							AutoReLoginDaemon.getInstance(this.context).stop();
							KeepAliveDaemon.getInstance(this.context).setNetworkConnectionLostObserver(new Observer() {
								public void update(Observable observable, Object data) {
									QoS4SendDaemon.getInstance(MessageHandler.this.context).stop();
									QoS4ReciveDaemon.getInstance(MessageHandler.this.context).stop();
									ClientCoreSDK.getInstance().setConnectedToServer(false);
									ClientCoreSDK.getInstance().setCurrentUserId(-1);
									ClientCoreSDK.getInstance().getChatBaseEvent().onLinkCloseMessage(-1);
									AutoReLoginDaemon.getInstance(MessageHandler.this.context).start(true);
								}
							});
							KeepAliveDaemon.getInstance(this.context).start(false);
							QoS4SendDaemon.getInstance(this.context).startup(true);
							QoS4ReciveDaemon.getInstance(this.context).startup(true);
							ClientCoreSDK.getInstance().setConnectedToServer(true);
						} else {
							ClientCoreSDK.getInstance().setConnectedToServer(false);
							ClientCoreSDK.getInstance().setCurrentUserId(-1);
						}
	
						if (ClientCoreSDK.getInstance().getChatBaseEvent() == null)
							break;
						ClientCoreSDK.getInstance().getChatBaseEvent().onLoginMessage(
								loginInfoRes.getUser_id(), loginInfoRes.getCode());
						break;
					}
					case ProtocalType.S.FROM_SERVER_TYPE_OF_RESPONSE$FOR$ERROR: {
						PErrorResponse errorRes = ProtocalFactory.parsePErrorResponse(pFromServer.getDataContent());
	
						if (errorRes.getErrorCode() == 301) {
							ClientCoreSDK.getInstance().setLoginHasInit(false);
							if (ClientCoreSDK.DEBUG)
								Log.d(TAG, "收到服务端的“尚未登陆”的错误消息，心跳线程将停止，请应用层重新登陆.");
							KeepAliveDaemon.getInstance(this.context).stop();
							AutoReLoginDaemon.getInstance(this.context).start(false);
						}
	
						if (ClientCoreSDK.getInstance().getChatTransDataEvent() == null)
							break;
						ClientCoreSDK.getInstance().getChatTransDataEvent().onErrorResponse(
								errorRes.getErrorCode(), errorRes.getErrorMsg());
	
						break;
					}
					default:
						if (ClientCoreSDK.DEBUG)
							Log.d(TAG, "收到的服务端消息类型：" + pFromServer.getType() + "，但目前该类型客户端不支持解析和处理！");
				}
			} catch (Exception e) {
				if (ClientCoreSDK.DEBUG)
					Log.d(TAG, "处理消息的过程中发生了错误.", e);
			}
		}

		private void sendRecievedBack(final Protocal pFromServer) {
			if(pFromServer.getFp() != null) {
				new LocalUDPDataSender.SendCommonDataAsync(
						context
						, ProtocalFactory.createRecivedBack(
								pFromServer.getTo()
								, pFromServer.getFrom()
								, pFromServer.getFp())){
					@Override
					protected void onPostExecute(Integer code) {
						if(ClientCoreSDK.DEBUG)
							Log.e(TAG, "【QoS】向"+pFromServer.getFrom()+"发送"+pFromServer.getFp()+"包的应答包成功,from="+pFromServer.getTo()+"！");
					}
				}.execute();
			} else {
				Log.e(TAG, "【QoS】收到"+pFromServer.getFrom()+"发过来需要QoS的包，但它的指纹码却为null！无法发应答包！");
			}
		}
	}
}