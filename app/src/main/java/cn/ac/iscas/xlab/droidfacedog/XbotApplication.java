package cn.ac.iscas.xlab.droidfacedog;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by lisongting on 2017/7/10.
 */

public class XbotApplication extends Application {
    public static final String TAG = "XbotApplication";

    public ServiceConnection mServiceConnection;
    public RosConnectionService.ServiceBinder mServiceProxy;
    private Intent intent;

    public XbotApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "XbotApplication -- onCreate()");

        //创建ServiceConnection对象
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.i(TAG, "mServiceConnection--onServiceConnected()");
                mServiceProxy = (RosConnectionService.ServiceBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.i(TAG, "mServiceConnection--onServiceDisconnected()");
            }
        };
        //绑定RosConnectionService
        intent = new Intent(this, RosConnectionService.class);
        startService(intent);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);

    }

    @Override
    public void onTerminate() {
        Log.i(TAG, "XbotApplication -- onTerminate()");
        unbindService(mServiceConnection);
        super.onTerminate();
    }

    public RosConnectionService.ServiceBinder getServiceProxy() {
        if (mServiceProxy != null) {
            return mServiceProxy;
        }
        return null;
    }


}