package com.olc.reader;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.olc.reader.service.IReaderCtrlService;
import com.olc.reader.service.IReaderCurrentTypeCallback;
import com.olc.reader.service.bt.BluetoothConnectionListener;
import com.olc.reader.service.bt.BluetoothStateChangeListener;
import com.olc.reader.service.bt.BluetoothState;
import com.olc.util.CLog;
import com.olc.util.SharedPreferencesHelper;
import com.olc.view.ViewPagerSlide;

public class ReaderCtrlApp extends Application {

    public static final String TAG = ReaderCtrlApp.class.getSimpleName();

    /**
     * Bluetooth heartbeat packet interval
     */
    public static final int OLCREADER_HEART_INTERVALTIME = 10 * 1000;
    /**
     * Bluetooth heartbeat package
     */
    public static final String ReaderCtrl_Heart_Service = "ReaderCtrl_Heart_Service";

    public Handler appHandler;

    public static ViewPagerSlide mViewPager;
    private SharedPreferencesHelper sh = null;
    public static IBluetoothCallback bluetoothCallback;

    public static IReaderCtrlService iService;
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name,
                                                    IBinder service) {
            // TODO Auto-generated method stub
            iService = IReaderCtrlService.Stub.asInterface(service);
            CLog.e(TAG, "onServiceConnected");
            iService.setBluetoothConnectionListener(bluetoothConnectionListener);
            iService.setBluetoothStateChangeListener(bluetoothStateListener);
                iService.getReaderCurrentType(mReaderCurrentTypeCallback);

            if (!"".equals(getBluetoothDevice())) {
                if (iService.getBluetoothState() != BluetoothState.STATE_CONNECTED) {
                    iService.connect(getBluetoothAddress());
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            CLog.e(TAG, "onServiceDisconnected");
            iService = null;
        }
    };

    BluetoothConnectionListener bluetoothConnectionListener = new BluetoothConnectionListener.Stub() {
        @Override
        public void onDeviceConnected(String name, String address)  {
            CLog.i(TAG, "Connected to " + name + ", address : " + address);
            String msg = name+";"+address;
            sendMessage(BluetoothState.STATE_CONNECTED, msg);
            setBluetoothDevice(name, address);

//            registerAlarmManager(Constant.OLCREADER_HEART_INTERVALTIME);
        }

        @Override
        public void onDeviceDisconnected(String name, String address)  {
            CLog.i(TAG, "Connection lost  name " + name + ", address : " + address);
            String msg = name+";"+address;
            sendMessage(BluetoothState.STATE_DISCONNECTED, msg);

//            unRegisterAlarmManager();
        }

        @Override
        public void onDeviceConnectionFailed(String name, String address)  {
            CLog.i(TAG, "Unable to connect name : " + name + ", address : " + address);
            String msg = name+";"+address;
            sendMessage(BluetoothState.STATE_CONNECTION_FAILED, msg);

//            unRegisterAlarmManager();
        }
    };

    BluetoothStateChangeListener bluetoothStateListener = new BluetoothStateChangeListener.Stub() {
        @Override
        public void onServiceStateChanged(int state) {
            try {
                if(bluetoothCallback != null)
                    bluetoothCallback.onServiceStateChanged(state);

                if(appHandler != null)
                    appHandler.sendEmptyMessage(state);
            }catch (Exception e){
                CLog.e(TAG, "", e);
            }

        }
    };

    IReaderCurrentTypeCallback mReaderCurrentTypeCallback = new IReaderCurrentTypeCallback.Stub() {
        @Override
        public void response(final byte ErrorCode)  {
            CLog.d(TAG, "ReaderCurrentType = " + ErrorCode);
            if(mViewPager == null){
                CLog.d(TAG, "mViewPager is null");
            }
            switch (ErrorCode){
                case 0://scan
                    mViewPager.setCurrentItem(2);
                    break;
                case 1://uhf
                    mViewPager.setCurrentItem(1);
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        sh = new SharedPreferencesHelper(getApplicationContext(), "ReaderCtrl");

        bindReaderCtrlService(conn);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        CLog.d(TAG, "onTerminate");
//        unRegisterAlarmManager();
    }

    public void bindReaderCtrlService(ServiceConnection conn){
        final Intent intent = new Intent();
        intent.setAction("com.olc.reader.service.ReaderCtrlService");
        intent.setPackage("com.olc.reader");
        bindService(intent, conn, Service.BIND_AUTO_CREATE);
    }

    /**
     * AlarmManger
     */
    public void registerAlarmManager(int intervalTime) {
        CLog.d(TAG, "registerAlarmManager");
        try {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            PendingIntent heartBeatPendingIntent = PendingIntent.getService(this, 0,
                    new Intent(ReaderCtrl_Heart_Service),
                    PendingIntent.FLAG_UPDATE_CURRENT);
            long triggerAtTime = System.currentTimeMillis() + intervalTime;

            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtTime, heartBeatPendingIntent);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtTime, heartBeatPendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtTime, heartBeatPendingIntent);
                }
            }
        }catch (Exception e){
            CLog.e(TAG, "", e);
        }

    }

    /**
     * AlarmManger
     */
    public void unRegisterAlarmManager() {
        CLog.e(TAG, "unRegisterAlarmManager");
        try {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            PendingIntent heartBeatPendingIntent = PendingIntent.getService(this, 0,
                    new Intent(ReaderCtrl_Heart_Service),
                    PendingIntent.FLAG_UPDATE_CURRENT);
            if (alarmManager != null) {
                alarmManager.cancel(heartBeatPendingIntent);
            }
        }catch (Exception e){
            CLog.e(TAG, "", e);
        }

    }

    void sendMessage(int what, String msgStr){
        Message msg = new Message();
        msg.what = what;
        msg.obj = msgStr;
        if(appHandler != null)
            appHandler.sendMessage(msg);
    }

    public String getBluetoothDevice() {
        return sh.getSharedPreference("bt", "").toString();
    }

    public void setBluetoothDevice(String name, String address) {
        sh.put("bt", name + ";" + address);
    }

    public void removeDevice() {
        sh.remove("bt");
    }

    public String getBluetoothAddress() {
        String address = sh.getSharedPreference("bt", "").toString();
        address = address.substring(address.indexOf(";") + 1);
        return address;
    }

    public String getBluetoothName() {
        String name = sh.getSharedPreference("bt", "").toString();
        name = name.substring(0, name.indexOf(";"));
        return name;
    }

}
