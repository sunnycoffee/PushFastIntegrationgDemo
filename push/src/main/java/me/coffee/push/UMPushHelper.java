package me.coffee.push;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;

import org.android.agoo.huawei.HuaWeiRegister;
import org.android.agoo.mezu.MeizuRegister;
import org.android.agoo.oppo.OppoRegister;
import org.android.agoo.vivo.VivoRegister;
import org.android.agoo.xiaomi.MiPushRegistar;

import java.util.HashMap;
import java.util.Map;

/**
 * 友盟推送
 *
 * @author kongfei
 */
public class UMPushHelper {

    private static final String TAG = "UM-Push";
    private static final String ALIAS_TYPE = "Official";
    private PushAgent mPushAgent;

    private LaunchHandler mLaunchHandler;
    private NotificationClickHandler mNotificationClickHandler;
    private CustomMessageHandler mCustomMessageHandler;

    private UMPushHelper() {
    }

    private static final class Holder {
        final static UMPushHelper instance = new UMPushHelper();
    }

    public static UMPushHelper getInstance() {
        return Holder.instance;
    }

    public UMPushHelper init(Context context, String resPkg) {
        Map<String, String> data = getMetaData(context);
        UMConfigure.init(context,
                data.get("UMENG_APPKEY"),
                "Umeng",
                UMConfigure.DEVICE_TYPE_PHONE,
                data.get("UMENG_APPSECRET"));

        mPushAgent = PushAgent.getInstance(context);
        if (resPkg != null) mPushAgent.setResourcePackageName(resPkg);
        mPushAgent.register(new IUmengRegisterCallback() {

            @Override
            public void onSuccess(String deviceToken) {
                Log.d(TAG, "注册成功：deviceToken：-------->  " + deviceToken);
            }

            @Override
            public void onFailure(String s, String s1) {
                Log.e(TAG, "注册失败：-------->  " + "s:" + s + ",s1:" + s1);
            }
        });

        //厂商通道
        MiPushRegistar.register(context, data.get("XIAOMI_APPID"), data.get("XIAOMI_APPKEY"));
        MeizuRegister.register(context, data.get("MEIZU_APPID"), data.get("MEIZU_APPKEY"));
        OppoRegister.register(context, data.get("OPPO_APPKEY"), data.get("OPPO_APPSECRET"));
        HuaWeiRegister.register((Application) context.getApplicationContext());
        VivoRegister.register(context);

        return this;
    }

    public UMPushHelper setAlias(final String alias) {
        mPushAgent.setAlias(alias, ALIAS_TYPE, (isSuccess, message) -> {
            Log.d(TAG, "alias设置：" + isSuccess + ";-------->  " + "alias:" + alias + ",message:" + message);
        });
        return this;
    }

    public UMPushHelper addTag(final String... tags) {
        mPushAgent.getTagManager().addTags((isSuccess, result) -> {
            Log.d(TAG, "tags设置：" + isSuccess + ";-------->  " + "alias:" + tags + ",message:" + result.msg);
        }, tags);
        return this;
    }

    public UMPushHelper setLaunchHandler(LaunchHandler handler) {
        this.mLaunchHandler = handler;
        return this;
    }

    public UMPushHelper setNotificationClickHandler(final NotificationClickHandler handler) {
        this.mNotificationClickHandler = handler;
        UmengNotificationClickHandler notificationClickHandler = new UmengNotificationClickHandler() {

            @Override
            public void openActivity(Context context, UMessage uMessage) {
                handleNotification(uMessage);
            }

            @Override
            public void launchApp(Context context, UMessage uMessage) {
                handleNotification(uMessage);
            }

            @Override
            public void openUrl(Context context, UMessage uMessage) {
                handleNotification(uMessage);
            }

            @Override
            public void dealWithCustomAction(Context context, UMessage uMessage) {
                handleNotification(uMessage);
            }
        };
        mPushAgent.setNotificationClickHandler(notificationClickHandler);
        return this;
    }

    public UMPushHelper setCustomMessageHandler(CustomMessageHandler handler) {
        this.mCustomMessageHandler = handler;
        UmengMessageHandler messageHandler = new UmengMessageHandler() {

            @Override
            public void dealWithCustomMessage(Context context, UMessage uMessage) {
                handleCustomMessage(uMessage);
            }
        };
        mPushAgent.setMessageHandler(messageHandler);
        return this;
    }

    private void handleNotification(UMessage message) {
        Log.d(TAG, "Notification：-------->handleNotification");
        if (mNotificationClickHandler == null) return;
        final String json = message.custom;
        final Map<String, String> data = message.extra;
        MainLooper.runOnUiThread(() -> {
            mNotificationClickHandler.dealWithNotification();
        });
    }

    private void handleCustomMessage(UMessage message) {
        Log.d(TAG, "Notification：-------->handleCustomMessage");
        if (mCustomMessageHandler == null) return;
        final String json = message.custom;
        final Map<String, String> data = message.extra;
        MainLooper.runOnUiThread(() -> {
            mCustomMessageHandler.dealWithCustomMessage();
        });
    }

    void openNotification(String message) {
        Log.d(TAG, "Notification：-------->openNotification");
        if (mNotificationClickHandler == null) return;
        MainLooper.runOnUiThread(() -> {
            if (mLaunchHandler != null) mLaunchHandler.onLaunch();
            mNotificationClickHandler.dealWithNotification();
        });
    }

    private Map<String, String> getMetaData(Context context) {
        Map<String, String> map = new HashMap<>();
        PackageManager pm = context.getPackageManager();
        String packageName = context.getPackageName();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            for (String key : bundle.keySet()) {
                map.put(key, String.valueOf(bundle.get(key)));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return map;
    }

    public interface NotificationClickHandler {

        void dealWithNotification();
    }

    public interface CustomMessageHandler {

        void dealWithCustomMessage();
    }

    public interface LaunchHandler {

        void onLaunch();
    }
}
