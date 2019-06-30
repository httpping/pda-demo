package com.olc.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.olc.reader.BaseActivity;
import com.olc.reader.IBluetoothCallback;
import com.olc.reader.R;
import com.olc.reader.ReaderCtrlApp;
import com.olc.reader.service.bt.BluetoothState;
import com.olc.util.CLog;
import com.olc.view.FlexibleListView;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothDevicesActivity extends BaseActivity implements View.OnClickListener, IBluetoothCallback {
    static final String TAG = "BluetoothDevicesActivity";
    private FlexibleListView fl_btDevices;

    private BtDeviceListViewAdapter btAdapter = new BtDeviceListViewAdapter();
    private ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
    private Set<BluetoothDevice> pairedDevices;

    private TextView tv_info;
    private ProgressBar progressBar;
    private ReaderCtrlApp app;

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                // When discovery finds a device
                switch (action){
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        break;
                    case BluetoothDevice.ACTION_FOUND:
                        // Get the BluetoothDevice object from the Intent
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                        progressBar.setVisibility(View.VISIBLE);
                        // If it's already paired, skip it, because it's been listed already
                        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                            if(!devices.contains(device)){
                                if(device.getName() != null
                                        && device.getName().startsWith("CM900")){
                                    devices.add(device);
                                    refreshBtDevices();
                                }
                            }
                        }
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        // If there are paired devices, add each one to the ArrayAdapter
                        progressBar.setVisibility(View.INVISIBLE);
                        pairedDevices = mBtAdapter.getBondedDevices();
                        for (BluetoothDevice pairedevice : pairedDevices) {
                            if (!devices.contains(pairedevice)) {
                                if(pairedevice.getName() != null
                                        && pairedevice.getName().startsWith("CM900")){
                                    devices.add(pairedevice);
                                    refreshBtDevices();
                                }
                            }
                        }
                        dismissProgressDialog();
                        break;
                }
            }catch (Exception e){
                CLog.e(TAG, "", e);
            }

        }
    };

    // The on-click listener for all devices in the ListViews
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int position, long id) {
            // Cancel discovery because it's costly and we're about to connect
            try {
                cancelDiscovery();
                dismissProgressDialog();

                BluetoothDevice device = devices.get(position);
                CLog.d(TAG,"device =" + device );
                if (ReaderCtrlApp.iService != null) {
                    if (ReaderCtrlApp.iService.getBluetoothState() == BluetoothState.STATE_CONNECTED) {
                        ReaderCtrlApp.iService.disconnect();
                    } else {
                        setTitle(String.format(getResources().getString(R.string.title_bluetooth_conn_conning), device.getName()));
                        ReaderCtrlApp.iService.connect(device.getAddress());
                    }
                }
            }catch (Exception e){
                CLog.e(TAG, "", e);
            }

        }
    };

    FlexibleListView.OnPullListener pullListener = new FlexibleListView.OnPullListener(){

        @Override
        public void onPullDown() {
            try {
                progressBar.setVisibility(View.VISIBLE);
                devices.clear();
                doDiscovery();
            }catch (Exception e){
                CLog.e(TAG, "", e);
            }

        }
        @Override
        public void onPullUp() { }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.bluetooth_devices_view);
            super.mHandler = mHandler;
            app = (ReaderCtrlApp)getApplication();
            ReaderCtrlApp.bluetoothCallback  = this;

            fl_btDevices = (FlexibleListView)findViewById(R.id.fl_btDevices);
            fl_btDevices.setOnItemClickListener(mDeviceClickListener);
            fl_btDevices.setOnPullListener(pullListener);

            tv_info = (TextView) findViewById(R.id.tv_info);
            progressBar = (ProgressBar) findViewById(R.id.progressBar);

            findViewById(R.id.btn_discovery).setOnClickListener(this);
            findViewById(R.id.btn_cancel).setOnClickListener(this);

            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            this.registerReceiver(mReceiver, filter);
            // Register for broadcasts when a device is discovered
            filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            this.registerReceiver(mReceiver, filter);
            // Register for broadcasts when discovery has finished
            filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            this.registerReceiver(mReceiver, filter);

            doDiscovery();
        }catch (Exception e){
            CLog.e(TAG, "", e);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Make sure we're not doing discovery anymore
        try {
            cancelDiscovery();
            dismissProgressDialog();

            // Unregister broadcast listeners
            this.unregisterReceiver(mReceiver);
        }catch (Exception e){
            CLog.e(TAG, "", e);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_discovery:
                doDiscovery();
                break;
            case R.id.btn_cancel:
                cancelDiscovery();
                break;
        }
    }

    @Override
    public void onServiceStateChanged(int state) {
        try {
            switch (state){
                case BluetoothState.STATE_CONNECTED:
                    finish();
                    break;
                case BluetoothState.STATE_CONNECTION_FAILED:
                case BluetoothState.STATE_DISCONNECTED:
                    break;
            }
        }catch (Exception e){
            CLog.e(TAG, "", e);
        }

    }

    // Start device discover with the BluetoothAdapter
    private void doDiscovery() {
        CLog.d(TAG, "doDiscovery()");
        cancelDiscovery();
        showProgressDialog();
        pairedDevices = mBtAdapter.getBondedDevices();
        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (!devices.contains(device)) {
                    if(device.getName() != null
                            && device.getName().startsWith("CM900")){
                        devices.add(device);
                        refreshBtDevices();
                    }

                }
            }
        }
        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }

    private void cancelDiscovery(){
        dismissProgressDialog();
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
    }

    private void showProgressDialog(){
        progressBar.setVisibility(View.VISIBLE);
    }

    private void dismissProgressDialog(){
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void refreshBtDevices() {
        try {
            String strSelectDevice;
            if (devices.size() <= 0) {
                strSelectDevice = getResources().getString(R.string.title_bluetooth_no_device);
            } else {
                strSelectDevice = getResources().getString(R.string.title_bluetooth_conn_select);;
            }
            setTitle(strSelectDevice);
            fl_btDevices.setAdapter(btAdapter);
        }catch (Exception e){
            CLog.e(TAG, "", e);
        }

    }

    class BtDeviceListViewAdapter extends BaseAdapter {

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
            try {
                ViewHolder holder = null;
                if (convertView == null) {
                    convertView = LayoutInflater.from(getApplicationContext()).inflate(
                            R.layout.bluetooth_devices_item_view, null);
                    holder = new ViewHolder();
                    holder.tv_btName = (TextView) convertView  .findViewById(R.id.tv_btName);
                    holder.tv_btAddress = (TextView) convertView .findViewById(R.id.tv_btAddress);
                    convertView.setTag(holder);
                } else {
                    holder = (BtDeviceListViewAdapter.ViewHolder) convertView.getTag();
                }

                if(devices.size() > 0){
                    BluetoothDevice device = devices.get(position);
                    holder.tv_btName.setText(device.getName());
                    holder.tv_btAddress.setText(device.getAddress());
                }
            }catch (Exception e){
                CLog.e(TAG, "", e);
            }
            return convertView;
        }

        public final class ViewHolder {
            public TextView tv_btName;
            public TextView tv_btAddress;
        }
    }

}
