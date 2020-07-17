package me.coffee.push;

import android.os.Handler;
import android.os.Looper;

/**
 * 主线程的Handler
 *
 * @author kongfei
 */
class MainLooper extends Handler {

    private static MainLooper instance = new MainLooper(Looper.getMainLooper());

    private MainLooper(Looper looper) {
        super(looper);
    }

    static MainLooper getInstance() {
        return instance;
    }

    static void runOnUiThread(Runnable runnable) {
        if (Looper.getMainLooper().equals(Looper.myLooper())) {
            runnable.run();
        } else {
            instance.post(runnable);
        }

    }
}
