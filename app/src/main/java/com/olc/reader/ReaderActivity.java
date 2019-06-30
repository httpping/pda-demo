package com.olc.reader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.olc.reader.service.bt.BluetoothState;
import com.olc.reader.service.bt.BluetoothStateChangeListener;
import com.olc.util.CLog;
import com.olc.view.ConsoleView;
import com.olc.view.FlexibleListView;

import java.util.ArrayList;
import java.util.Set;


public class ReaderActivity extends BaseActivity implements View.OnClickListener {

    private static String TAG = ReaderActivity.class.getSimpleName();

    private TextView tv_msg;
    private ConsoleView cv_msg;
    private EditText et_input;
    private Button btn_send;
    private LinearLayout ll_bottom;
    private FlexibleListView flexiblelist_bluetooth;

    private ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
    private BtDeviceListViewAdapter btAdapter = new BtDeviceListViewAdapter();
    private Set<BluetoothDevice> pairedDevices;
    private ProgressDialog dialog;

    int count = 0;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE ,
            Manifest.permission.ACCESS_FINE_LOCATION };
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    BluetoothStateChangeListener mBluetoothStateChangeListener = new BluetoothStateChangeListener.Stub(){

        @Override
        public void onServiceStateChanged(int state)  {
            cv_msg.clear();
            if(state == BluetoothState.STATE_CONNECTED){
                flexiblelist_bluetooth.setVisibility(View.GONE);
                cv_msg.setVisibility(View.VISIBLE);
                ll_bottom.setVisibility(View.VISIBLE);
            }else{
                flexiblelist_bluetooth.setVisibility(View.VISIBLE);
                cv_msg.setVisibility(View.GONE);
                ll_bottom.setVisibility(View.GONE);
            }
        }
    };


    Handler hhh = new Handler(){
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            String msgstr = cv_msg.getText().toString();
            CLog.d(TAG, "EPC = " +msg.obj.toString() );
            cv_msg.setText(msgstr + "\r\n" + msg.obj.toString());
        }
    };
//    BluetoothDataReceivedListener mBluetoothDataReceivedListener = new BluetoothDataReceivedListener.Stub() {
//
//        @Override
//        public void onDataReceived(String cmd, byte[] datas) {
//            CLog.i(TAG, "Length : " + datas.length + ", datas="+Util.bytesToHexString(datas));
//            if (cmd.equals(Cmd.CMD_ReaderCtrl_doScan)) {
//                cv_msg.append("Scan : " + Util.bytesToStr(datas));
//            } else if (cmd.equals(Cmd.CMD_REAL_TIME_INVENTORY)
//                    || cmd.equals(Cmd.CMD_CUSTOMIZED_SESSION_TARGET_INVENTORY) ){
//                StringBuffer uhf = new StringBuffer();
//
////                try {
////                    if(ReaderCtrlApp.iService == null){
////                        return;
////                    }
////                    InventoryModel model = ReaderCtrlApp.iService.AnalysisInventory(datas);
////                    if (model.getType() == -1) {
////                        cv_msg.append("incomplete data");
////                        return;
////                    }
////                    if (model.getType() == 0) {
////                        uhf.append(model.getPCHEX()).append(" + ");
////                        uhf.append(model.getEPCHEX()).append(" + ");
////                        uhf.append(model.getRSSIHEX());
////                        CLog.d(TAG,"UHF : " + uhf.toString());
////                        cv_msg.append("UHF : " + (++count) + "  " + uhf.toString());
////                    }
////                    if (model.getType() == 1) {
////                        count = 1;
////                        if (model.getTotalReadDEC() == 0) {
////                            cv_msg.append("UHF : No label");
////                            return;
////                        }
////                        uhf.append(model.getAntIDHEX()).append(" + ");
////                        uhf.append(model.getReadRateHEX()).append(" + ");
////                        uhf.append(model.getTotalReadDEC());
////                        cv_msg.append("UHF : " + (++count) + "  " + uhf.toString());
////                    }
////                    if (model.getType() == 2) {
////                        cv_msg.append("UHF : ERROR CODE " + model.getErrorCodeHEX());
////                    }
////                } catch (RemoteException e) {
////                    e.printStackTrace();
////                }
//
//            }
//            else{
//                cv_msg.append(remote_bt_name + " : " + Util.bytesToHexString(datas));
//            }
//        }
//
//        @Override
//        public void onError(String cmd, int ErrCode) {
//            /*String msg = cv_msg.getText().toString();
//            String receiveMsg = "";
//            if (cmd == CmdUtil.bytesToInt(Cmd.CMD_OlcReader_doScan_Res)) {
//                cv_msg.append("Scan : ERROR CODE " + ErrCode);
//            }
//            if (cmd == CmdUtil.bytesToInt(CMD_REAL_TIME_INVENTORY_Res)
//                    || cmd == CmdUtil.bytesToInt(Cmd.CMD_CUSTOMIZED_SESSION_TARGET_INVENTORY_Res) ){
//                receiveMsg = "UHF : ERROR CODE " + ErrCode;
//                cv_msg.append(receiveMsg);
//            }*/
//        }
//    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //super.h1 = mHandler;
        setContentView(R.layout.bt_device_view);
        verifyStoragePermissions(this);
        initview();

        doDiscovery();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if(ReaderCtrlApp.iService != null){
//            try {
//                ReaderCtrlApp.iService.setBluetoothDataReceivedListener(mBluetoothDataReceivedListener);
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
//        }
//        dataReceivedListener = mBluetoothDataReceivedListener;
//        stateChangeListener = mBluetoothStateChangeListener;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CLog.d(TAG,"onDestroy");
        // Make sure we're not doing discovery anymore
        cancelDiscovery();
        dismissProgressDialog();
//        try {
//            if(ReaderCtrlApp.iService != null)
//                ReaderCtrlApp.iService.stopBluetoothService();
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
        this.finish();
    }

    private void initview() {
        flexiblelist_bluetooth = (FlexibleListView)findViewById(R.id.flexiblelist_bluetooth);
        cv_msg = (ConsoleView) findViewById(R.id.cv_msg);
        et_input = (EditText) findViewById(R.id.et_input);
        btn_send = (Button) findViewById(R.id.btn_send);
        ll_bottom = (LinearLayout)findViewById(R.id.ll_bottom);
        findViewById(R.id.btn_clear).setOnClickListener(this);
        findViewById(R.id.btn_test).setOnClickListener(this);
        findViewById(R.id.btn_rw).setOnClickListener(this);

        btn_send.setOnClickListener(this);
        flexiblelist_bluetooth.setOnItemClickListener(mDeviceClickListener);
        flexiblelist_bluetooth.setOnPullListener(new FlexibleListView.OnPullListener(){

            @Override
            public void onPullDown() {
                //下拉刷新
                CLog.d("wpx", "onPullDown");
                devices.clear();
                doDiscovery();
            }

            @Override
            public void onPullUp() {
                //上拉加载更多
                CLog.d("wpx", "onPullUp");
            }
        });

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        this.registerReceiver(mReceiver, filter);
        // Register for broadcasts when a device is discovered
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);
        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        flexiblelist_bluetooth.setVisibility(View.VISIBLE);
        cv_msg.setVisibility(View.GONE);
        ll_bottom.setVisibility(View.GONE);
    }

    private void refreshBtDevices() {
        String strSelectDevice;
        if (devices.size() <= 0) {
            strSelectDevice = "No devices found";
        } else {
            strSelectDevice = "Select a device to connect";
        }
        setTitle(strSelectDevice);
        flexiblelist_bluetooth.setAdapter(btAdapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_send:
//                String msg = cv_msg.getText().toString();
//                String receiveMsg = local_bt_name + " : " + et_input.getText().toString();
//                cv_msg.setText(msg + "\r\n" + receiveMsg);
//                bt.send(et_input.getText().toString());
                break;
            case R.id.btn_test:
//                try {
//                    iBluetoothService.send(new byte[]{0x00,0x01});
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
//                bt.send(CmdScan.testScan());
//                bt.send(CmdUHF.sessionInventory((byte)0x02,(byte)0x00,(byte)0x01));
//                bt.send(CmdUHF.tidInventory(6));
//                if(ReaderCtrlApp.iService != null){
//                    try {
//                        ReaderCtrlApp.iService.Inventory(cb);
//                       T1 mm = ReaderCtrlApp.iService.test();
//
//                       CLog.e(TAG, "mm = " + mm.toString());
//                        cv_msg.setText(mm.toString());
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }
//                }
                break;
            case R.id.btn_rw:
//                startActivity(new Intent(this, ReadWriteActivity.class));
                break;
            case R.id.btn_clear:
                cv_msg.clear();
                count=0;
                break;
        }
    }

    // Start device discover with the BluetoothAdapter
    private void doDiscovery() {
        CLog.d(TAG, "doDiscovery()");
        cancelDiscovery();
        showProgressDialog();
//        pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (!devices.contains(device)) {
                    devices.add(device);
                    refreshBtDevices();
                }
            }
        }
//        if (!isBluetoothEnabled())
//            mBluetoothAdapter.enable();
//        // Request discover from BluetoothAdapter
//        mBluetoothAdapter.startDiscovery();
    }

    private void cancelDiscovery(){
//        if (mBluetoothAdapter.isDiscovering()) {
//            mBluetoothAdapter.cancelDiscovery();
//        }
    }

    private void showProgressDialog(){
        if(dialog != null){
            dialog.dismiss();
            dialog = null;
        }
        dialog = ProgressDialog.show(this, "","");
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                cancelDiscovery();
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancelDiscovery();
            }
        });
    }

    private void dismissProgressDialog(){
        if(dialog != null){
            dialog.dismiss();
            dialog = null;
        }
    }

    // The on-click listener for all devices in the ListViews
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int position, long id) {
            // Cancel discovery because it's costly and we're about to connect
            cancelDiscovery();
            dismissProgressDialog();

            BluetoothDevice device = devices.get(position);
            CLog.d(TAG,"device =" + device );
//            try {
//                if(ReaderCtrlApp.iService != null){
//                    if(ReaderCtrlApp.iService.getBluetoothServiceState() == BluetoothState.STATE_CONNECTED) {
//                        ReaderCtrlApp.iService.bluetoothDisconnect();
//                    } else {
//                        setTitle("Connecting to "+device.getName());
//                        ReaderCtrlApp.iService.bluetoothConnect(device.getAddress());
//                    }
//                }else{
//                    CLog.d(TAG,"iService is NULL" );
//                }
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
        }
    };

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            CLog.d(TAG,"action = " + action);
            // When discovery finds a device
            switch (action){
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    // If it's already paired, skip it, because it's been listed already
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        if(!devices.contains(device)){
                            devices.add(device);
                            refreshBtDevices();
                        }
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    // If there are paired devices, add each one to the ArrayAdapter
//                    pairedDevices = mBluetoothAdapter.getBondedDevices();
//                    for (BluetoothDevice pairedevice : pairedDevices) {
//                        if (!devices.contains(pairedevice)) {
//                            devices.add(pairedevice);
//                            refreshBtDevices();
//                        }
//                    }
//                    dismissProgressDialog();
                    //setProgressBarIndeterminateVisibility(false);
                    break;
            }
        }
    };

    class BtDeviceListViewAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(getApplicationContext()).inflate(
                        R.layout.bt_device_item_view, null);
                holder = new ViewHolder();
                holder.tv_btName = (TextView) convertView  .findViewById(R.id.tv_btName);
                holder.tv_btAddress = (TextView) convertView .findViewById(R.id.tv_btAddress);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            BluetoothDevice device = devices.get(position);
            holder.tv_btName.setText(device.getName());
            holder.tv_btAddress.setText(device.getAddress());

            return convertView;
        }

        public final class ViewHolder {
            public TextView tv_btName;
            public TextView tv_btAddress;
        }
    }

    private ReaderHandler mHandler = new ReaderHandler();
    @SuppressLint("HandlerLeak")
    class ReaderHandler extends Handler {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            cv_msg.clear();
            if(msg.what == BluetoothState.STATE_CONNECTED){
                flexiblelist_bluetooth.setVisibility(View.GONE);
                cv_msg.setVisibility(View.VISIBLE);
                ll_bottom.setVisibility(View.VISIBLE);
            }else{
                flexiblelist_bluetooth.setVisibility(View.VISIBLE);
                cv_msg.setVisibility(View.GONE);
                ll_bottom.setVisibility(View.GONE);
            }
        }
    }
}
