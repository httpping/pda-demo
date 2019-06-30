package com.olc.reader;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.olc.reader.service.bt.BluetoothState;
import com.olc.util.CLog;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    public ReaderCtrlApp app;

    public BluetoothAdapter mBtAdapter;

    public static String local_bt_name = "";
    public static String local_bt_address = "";
    public static String remote_bt_name = "";
    public static String remote_bt_address = "";

    public Handler mHandler = null;

    public BaseHandler baseHandler = new BaseHandler();

    protected Boolean isfinish = false;
    protected ActivityTack tack = ActivityTack.getInstanse();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            app = (ReaderCtrlApp) getApplication();
            app.appHandler = baseHandler;

            mBtAdapter = BluetoothAdapter.getDefaultAdapter();
            tack.addActivity(this);
        }catch (Exception e){
            CLog.e(TAG, "", e);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            local_bt_name = mBtAdapter.getName();
            local_bt_address = mBtAdapter.getAddress();
        }catch (Exception e){
            CLog.e(TAG, "", e);
        }
    }

    @Override
    public void finish() {
        super.finish();
        tack.removeActivity(this);
    }

    class BaseHandler extends Handler{
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            try {
                remote_bt_name = msg.getData().getString("name");
                remote_bt_address  = msg.getData().getString("address");
                switch (msg.what){
                    case BluetoothState.STATE_CONNECTED:
                        setTitle(getResources().getString(R.string.title_bluetooth_conn_success) + remote_bt_name);
                        Toast.makeText(BaseActivity.this, R.string.toast_bluetooth_conn_success, Toast.LENGTH_SHORT).show();;
                        break;
                    case BluetoothState.STATE_CONNECTION_FAILED:
                        setTitle(getResources().getString(R.string.title_bluetooth_conn_failed)  + remote_bt_name);
                        Toast.makeText(BaseActivity.this, R.string.toast_bluetooth_conn_failed, Toast.LENGTH_SHORT).show();;
                        break;
                    case BluetoothState.STATE_DISCONNECTED:
                        tack.exit(BaseActivity.this);

                        setTitle(getResources().getString(R.string.title_bluetooth_conn_discon)  + remote_bt_name );
                        Toast.makeText(BaseActivity.this, R.string.toast_bluetooth_conn_discon, Toast.LENGTH_SHORT).show();;
                        break;
                }
            }catch (Exception e){
                CLog.e(TAG, "", e);
            }

        }
    }
}
