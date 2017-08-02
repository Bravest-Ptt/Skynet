package bravest.ptt.skynet.app;

import android.app.Application;

/**
 * Created by pengtian on 2017/8/2.
 */

public class App extends Application {

    private static App sInstance;

    public static App getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }
}
