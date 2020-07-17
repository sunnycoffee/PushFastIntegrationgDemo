package com.umeng.message.demo;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import me.coffee.push.UMPushHelper;

public class MyApplication extends Application {
    private static final String TAG = MyApplication.class.getName();

    @Override
    public void onCreate() {
        super.onCreate();
        UMPushHelper.getInstance()
                .init(this, "com.umeng.message.demo")
                .setAlias("umeng-app-123123123")
                .addTag("1231231233840auswer112wer23g")
                .setLaunchHandler(() -> {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                })
                .setNotificationClickHandler(() -> {
                    Toast.makeText(this, "打开通知", Toast.LENGTH_SHORT).show();
                    Log.d("UM-Push", "open");
                })
                .setCustomMessageHandler(() -> {
                    Toast.makeText(this, "打开自定义消息", Toast.LENGTH_SHORT).show();
                    Log.d("UM-Push", "open-msg");
                });
    }
}
