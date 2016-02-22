package com.mary.chatdemo.gson.event;

import java.util.Observer;

import com.mary.chat_gson_lib.android.event.ChatBaseEvent;
import com.mary.chatdemo.gson.OneActivity;

import android.app.Activity;
import android.util.Log;

/**
 * File Name:
 * Author:      Administrator
 * Write Dates: 2016/2/22
 * Description:
 * Change Log:
 * 2016/2/22-10-22---[公司]---[姓名]
 * ......Added|Changed|Delete......
 * --------------------------------
 */
public class ChatBaseEventImpl implements ChatBaseEvent {

    /** 标识符 */
    private final static String TAG = ChatBaseEventImpl.class.getSimpleName();

    private Activity activity = null;

    // 本Observer目前仅用于登陆时（因为登陆与收到服务端的登陆验证结果
    // 是异步的，所以有此观察者来完成收到验证后的处理）
    private Observer loginOkForLaunchObserver = null;

    @Override
    public void onLoginMessage(int userId, int errorCode) {
        if (errorCode == 0) {
//			Log.i(TAG, "【DEBUG_UI】登录成功，当前分配的user_id=！"+dwUserId);

            // TODO 以下代码仅用于DEMO哦
            if(this.activity != null) {
                ((OneActivity)this.activity).refreshMyid();
                ((OneActivity)this.activity).showIMInfo_green("登录成功,id=" + userId);
            }
        } else {
            Log.e(TAG, "【DEBUG_UI】登录失败，错误代码：" + errorCode);

            // TODO 以下代码仅用于DEMO哦
            if(this.activity != null) {
                ((OneActivity)this.activity).refreshMyid();
                ((OneActivity)this.activity).showIMInfo_red("登录失败,code=" + errorCode);
            }
        }

        // 此观察者只有开启程序首次使用登陆界面时有用
        if(loginOkForLaunchObserver != null) {
            loginOkForLaunchObserver.update(null, errorCode);
            loginOkForLaunchObserver = null;
        }
    }

    @Override
    public void onLinkCloseMessage(int dwErrorCode) {
        Log.e(TAG, "【DEBUG_UI】网络连接出错关闭了，error：" + dwErrorCode);

        // TODO 以下代码仅用于DEMO哦
        if(this.activity != null) {
            ((OneActivity)this.activity).refreshMyid();
            ((OneActivity)this.activity).showIMInfo_red("服务器连接已断开,error=" + dwErrorCode);
        }
    }

    public void setLoginOkForLaunchObserver(Observer loginOkForLaunchObserver) {
        this.loginOkForLaunchObserver = loginOkForLaunchObserver;
    }

    public ChatBaseEventImpl setGUI(Activity activity) {
        this.activity = activity;
        return this;
    }

}
