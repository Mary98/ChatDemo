package com.mary.chatdemo.gson;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mary.chat_gson_lib.android.ClientCoreSDK;
import com.mary.chat_gson_lib.android.core.LocalUDPDataSender;
import com.mary.chatdemo.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * File Name:   OneActivity
 * Author:      Mary
 * Write Dates: 2016/2/22
 * Description:
 * Change Log:
 * 2016/2/22-10-04---[公司]---[姓名]
 * ......Added|Changed|Delete......
 * --------------------------------
 */
public class OneActivity extends Activity {
    /** 标识符 */
    private final static String TAG = OneActivity.class.getSimpleName();
    /**上下文对象*/
    private Context context = null;
    /** 退出APP按钮 */
    private Button btnLogout = null;
    /** 对方用户ID*/
    private EditText editId = null;
    /** 消息内容*/
    private EditText editContent = null;
    /** 我的状态：连接时显示ID号， 断开时显示已断开 */
    private TextView viewMyid = null;
    /** 发送按钮 */
    private Button btnSend = null;
    /** 显示聊天记录 */
    private ListView chatInfoListView;
    /** 聊天记录适配器 */
    private MyAdapter chatInfoListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.demo_main_activity_layout);
        context = this;
        initViews();
        initListeners();
        initOthers();
    }

    /**
     * 捕获back键，实现调用 {@link # doExit(Context)}方法.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // ** 注意：Android程序要么就别处理，要处理就一定
        //			要退干净，否则会有意想不到的问题哦！
        // 退出登陆
        doLogout();
        // 退出程序
        doExit();
    }

    protected void onDestroy() {
        // 释放IM占用资源
        IMClientManager.getInstance(this).release();
        //
        super.onDestroy();
    }

    private void initViews() {
        btnLogout = (Button)this.findViewById(R.id.logout_btn);

        btnSend = (Button)this.findViewById(R.id.send_btn);
        editId = (EditText)this.findViewById(R.id.id_editText);
        editContent = (EditText)this.findViewById(R.id.content_editText);
        viewMyid = (TextView)this.findViewById(R.id.myid_view);

        chatInfoListView = (ListView)this.findViewById(R.id.demo_main_activity_layout_listView);
        chatInfoListAdapter = new MyAdapter(this);
        chatInfoListView.setAdapter(chatInfoListAdapter);

        this.setTitle("MobileIMSDK Demo");
    }

    private void initListeners() {
        btnLogout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // 退出登陆
                doLogout();
                // 退出程序
                doExit();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                doSendMessage();
            }
        });
    }

    private void initOthers() {
        // Refresh userId to show
        refreshMyid();

        // Set MainGUI instance refrence to listeners
        IMClientManager.getInstance(this).getTransDataListener().setGUI(this);
        IMClientManager.getInstance(this).getBaseEventListener().setGUI(this);
        IMClientManager.getInstance(this).getMessageQoSListener().setGUI(this);
    }

    public void refreshMyid() {
        int myid = ClientCoreSDK.getInstance().getCurrentUserId();
        this.viewMyid.setText(myid == -1 ? "连接断开"  : "" + myid);
    }

    /**
     * 发送消息
     */
    private void doSendMessage() {
        String msg = editContent.getText().toString().trim();
        if(msg.length() > 0) {
            int friendId = Integer.parseInt(editId.getText().toString().trim());
            showIMInfo_black("我对" + friendId + "说了一句：" + msg);

            // 发送消息（Android系统要求必须要在独立的线程中发送哦）
            new LocalUDPDataSender.SendCommonDataAsync(context, msg, friendId, true) {
                @Override
                protected void onPostExecute(Integer code) {
                    if(code == 0)
                        Log.e(TAG, "2数据已成功发出！");
                    else
                        Toast.makeText(getApplicationContext(), "数据发送失败。错误码是：" + code + "！", Toast.LENGTH_SHORT).show();
                }
            }.execute();
        }
        else
            Log.e(TAG, "txt2.len="+(msg.length()));
    }

    private void doLogout() {
        // 发出退出登陆请求包（Android系统要求必须要在独立的线程中发送哦）
        new AsyncTask<Object, Integer, Integer>(){
            @Override
            protected Integer doInBackground(Object... params) {
                int code = -1;
                try{
                    code = LocalUDPDataSender.getInstance(context).sendLoginout();
                } catch (Exception e){
                    Log.w(TAG, e);
                }

                return code;
            }

            @Override
            protected void onPostExecute(Integer code) {
                refreshMyid();
                if(code == 0)
                    Log.d(TAG, "注销登陆请求已完成！");
                else
                    Toast.makeText(getApplicationContext(), "注销登陆请求发送失败。错误码是："+code+"！", Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    private void doExit() {
        finish();
        System.exit(0);
    }

    //--------------------------------------------------------------- 各种信息输出方法 START
    public void showIMInfo_black(String txt) {
        chatInfoListAdapter.addItem(txt, ChatInfoColorType.black);
    }
    public void showIMInfo_blue(String txt) {
        chatInfoListAdapter.addItem(txt, ChatInfoColorType.blue);
    }
    public void showIMInfo_brightred(String txt) {
        chatInfoListAdapter.addItem(txt, ChatInfoColorType.brightred);
    }
    public void showIMInfo_red(String txt) {
        chatInfoListAdapter.addItem(txt, ChatInfoColorType.red);
    }
    public void showIMInfo_green(String txt) {
        chatInfoListAdapter.addItem(txt, ChatInfoColorType.green);
    }
    //--------------------------------------------------------------- 各种信息输出方法 END

    //--------------------------------------------------------------- inner classes START
    /**
     * 各种显示列表Adapter实现类。
     */
    public class MyAdapter extends BaseAdapter {
        private List<Map<String, Object>> mData;
        private LayoutInflater mInflater;
        private SimpleDateFormat hhmmDataFormat = new SimpleDateFormat("HH:mm:ss");

        public MyAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
            mData = new ArrayList<Map<String, Object>>();
        }

        public void addItem(String content, ChatInfoColorType color) {
            Map<String, Object> it = new HashMap<String, Object>();
            it.put("__content__", "["+hhmmDataFormat.format(new Date())+"]"+content);
            it.put("__color__", color);
            mData.add(it);
            this.notifyDataSetChanged();
            chatInfoListView.setSelection(this.getCount());
        }

        @Override
        public int getCount()
        {
            return mData.size();
        }

        @Override
        public Object getItem(int arg0)
        {
            return null;
        }

        @Override
        public long getItemId(int arg0)
        {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder=new ViewHolder();
                convertView = mInflater.inflate(R.layout.demo_main_activity_list_item_layout, null);
                holder.content = (TextView)convertView.findViewById(R.id.demo_main_activity_list_item_layout_tvcontent);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            holder.content.setText((String)mData.get(position).get("__content__"));
            ChatInfoColorType colorType = (ChatInfoColorType)mData.get(position).get("__color__");
            switch(colorType) {
                case blue:
                    holder.content.setTextColor(Color.rgb(0, 0, 255));
                    break;
                case brightred:
                    holder.content.setTextColor(Color.rgb(255,0,255));
                    break;
                case red:
                    holder.content.setTextColor(Color.rgb(255,0,0));
                    break;
                case green:
                    holder.content.setTextColor(Color.rgb(0,128,0));
                    break;
                case black:
                default:
                    holder.content.setTextColor(Color.rgb(0, 0, 0));
                    break;
            }

            return convertView;
        }

        public final class ViewHolder {
            public TextView content;
        }
    }

    /**
     * 信息颜色常量定义。
     */
    public enum ChatInfoColorType {
        black,
        blue,
        brightred,
        red,
        green,
    }
    //--------------------------------------------------------------- inner classes END

}
