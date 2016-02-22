package com.mary.chatdemo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
    }

    public void btn_Gson(View view) {
        Intent intent = new Intent(mContext, com.mary.chatdemo.gson.LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void btn_Fastjson(View view) {
        Intent intent = new Intent(mContext, com.mary.chatdemo.fastjson.LoginActivity.class);
        startActivity(intent);
        finish();
    }


}
