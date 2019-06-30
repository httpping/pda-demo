package com.olc.reader;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

public class HeartService extends Service {

    public static String TAG = "HeartService";
    private Handler mHanler = new Handler(Looper.getMainLooper());

    private ReaderCtrlApp app = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = (ReaderCtrlApp)getApplication();
//        bt = app.bt;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        mHanler.post(new Runnable() {
//            @Override
//            public void run() {
//                bt.send(CmdBT.OlcReader_Heart());
//                app.registerAlarmManager(Constant.OLCREADER_HEART_INTERVALTIME);
//            }
//        });
        return super.onStartCommand(intent, flags, startId);
    }
}
