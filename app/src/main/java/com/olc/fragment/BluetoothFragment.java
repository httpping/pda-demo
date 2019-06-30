package com.olc.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.olc.bluetooth.BluetoothDevicesActivity;
import com.olc.model.LoopModel;
import com.olc.reader.AutoSwitchAdapter;
import com.olc.reader.IBluetoothCallback;
import com.olc.reader.MainActivity;
import com.olc.reader.R;
import com.olc.reader.ReaderCtrlApp;
import com.olc.reader.service.IReaderCurrentTypeCallback;
import com.olc.reader.service.bt.BluetoothState;
import com.olc.util.CLog;
import com.olc.view.AutoSwitchView;

import java.util.ArrayList;
import java.util.List;

public class BluetoothFragment extends LazyFragment implements View.OnClickListener ,IBluetoothCallback {
    //
    private static final String TAG = "BluetoothFragment";

    // Flag bit, the flag has been initialized.
    private boolean isPrepared;

    private View mView;
    private AutoSwitchView mAutoSwitchView;
    private AutoSwitchAdapter mAdapter;

    private ReaderCtrlApp app;
    private ImageView iv_btState;
    private TextView tv_state;
    private Button btn_btconn, btn_btdisconn;

    Toolbar toolbar;
    TextView tv_title;

    int state = 0;
    boolean isVisibleToUser;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        try {
            this.isVisibleToUser = isVisibleToUser;
            if(isVisibleToUser){
                ReaderCtrlApp.bluetoothCallback  = this;
                if(ReaderCtrlApp.iService != null){
                    state = ReaderCtrlApp.iService.getBluetoothState();
                }
            }
        }catch (Exception e){
            CLog.e(TAG, "", e);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            mView = inflater.inflate(R.layout.fragment_bluetooth, container, false);

            isPrepared = true;
            lazyLoad();
        }catch (Exception e){
            CLog.e(TAG, "", e);
        }

        return mView;
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void lazyLoad() {
        if(!isPrepared || !isVisible) {
            return;
        }
        app = (ReaderCtrlApp) getActivity().getApplication();
        ReaderCtrlApp.bluetoothCallback = this;

        mAutoSwitchView = mView.findViewById(R.id.loopswitch);
        iv_btState = mView.findViewById(R.id.iv_btState);
        tv_state = (TextView) mView.findViewById(R.id.tv_state);
        btn_btconn = (Button) mView.findViewById(R.id.btn_btconn);
        btn_btdisconn = (Button) mView.findViewById(R.id.btn_btdisconn);

        mView.findViewById(R.id.btn_btconn).setOnClickListener(this);
        mView.findViewById(R.id.btn_btdisconn).setOnClickListener(this);
        mView.findViewById(R.id.btn_scan).setOnClickListener(this);
        mView.findViewById(R.id.btn_uhf).setOnClickListener(this);
        mView.findViewById(R.id.btn_scan).setEnabled(false);
        mView.findViewById(R.id.btn_uhf).setEnabled(false);

        MainActivity activity = (MainActivity)getActivity();
        toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        tv_title = (TextView) activity.findViewById(R.id.tv_title);
        tv_title.setText(R.string.tab_page_title_first);

        if(ReaderCtrlApp.iService != null){
            onServiceStateChanged(ReaderCtrlApp.iService.getBluetoothState());
        }

        new AsyncTask<Integer,Integer,List<LoopModel>>(){
            @Override
            protected List<LoopModel> doInBackground(Integer... integers) {
                List<LoopModel> datas = new ArrayList<LoopModel>();
                LoopModel model = null;
                model = new LoopModel("1", R.drawable.a);
                datas.add(model);
                model = new LoopModel("2", R.drawable.b);
                datas.add(model);
                model = new LoopModel("3", R.drawable.a);
                datas.add(model);
                model = new LoopModel("4", R.drawable.b);
                datas.add(model);
                return datas;
            }

            @Override
            protected void onPostExecute(List<LoopModel> datas) {
                super.onPostExecute(datas);
                mAdapter = new AutoSwitchAdapter(getContext(), datas);
                mAutoSwitchView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(ReaderCtrlApp.iService != null){
            onServiceStateChanged(ReaderCtrlApp.iService.getBluetoothState());
        }
		
        if (ReaderCtrlApp.iService != null) {
			if (ReaderCtrlApp.iService.getBluetoothState() == BluetoothState.STATE_CONNECTED) {
	                    IReaderCurrentTypeCallback cb = new IReaderCurrentTypeCallback.Stub() {
	                        @Override
	                        public void response(byte ErrorCode)  {
	                            CLog.d(TAG, "response = " + ErrorCode);
	                            switch (ErrorCode){
	                                case 0://scan
	                                    ReaderCtrlApp.mViewPager.setCurrentItem(2);
	                                    break;
	                                case 1://uhf
	                                    ReaderCtrlApp.mViewPager.setCurrentItem(1);
	                                    break;
	                            }
	                        }
	                    };
	                    ReaderCtrlApp.iService.getReaderCurrentType(cb);
	        }
		}
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_btconn:
                connect();
                break;
            case R.id.btn_btdisconn:
                disconnect();
                break;
            case R.id.btn_scan:
                if(ReaderCtrlApp.mViewPager != null){
                    ReaderCtrlApp.mViewPager.setCurrentItem(1);
                }
                break;
            case R.id.btn_uhf:
                if(ReaderCtrlApp.mViewPager != null){
                    ReaderCtrlApp.mViewPager.setCurrentItem(2);
                }
                break;
        }
    }

    @Override
    public void onServiceStateChanged(final int state) {
        if(getActivity() == null)
            return;

        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (state) {
                        case BluetoothState.STATE_CONNECTED:
                            iv_btState.setImageResource(R.drawable.w);
                            tv_state.setText(String.format(getResources().getString(R.string.bluetooth_conn_device), app.getBluetoothDevice()));
                            btn_btconn.setEnabled(false);
                            btn_btdisconn.setEnabled(true);

                            mView.findViewById(R.id.btn_scan).setEnabled(true);
                            mView.findViewById(R.id.btn_uhf).setEnabled(true);
                            if (ReaderCtrlApp.mViewPager != null) {
                                ReaderCtrlApp.mViewPager.setSlide(true);

                                ReaderCtrlApp.mViewPager.setCurrentItem(1);
                            }
                            break;
                        case BluetoothState.STATE_NONE:
                        case BluetoothState.STATE_LISTEN:
                        case BluetoothState.STATE_CONNECTING:
                        case BluetoothState.STATE_CONNECTION_FAILED:
                        case BluetoothState.STATE_DISCONNECTED:
                            iv_btState.setImageResource(R.drawable.r);
                            btn_btconn.setEnabled(true);
                            btn_btdisconn.setEnabled(false);
                            mView.findViewById(R.id.btn_scan).setEnabled(false);
                            mView.findViewById(R.id.btn_uhf).setEnabled(false);

                            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                            tv_state.setText(R.string.bluetooth_disconn_device);
                            ReaderCtrlApp.mViewPager.setCurrentItem(0);
                            if (ReaderCtrlApp.mViewPager != null) {
                                ReaderCtrlApp.mViewPager.setSlide(false);
                            }
                            break;
                    }
                }
            });
        }catch (Exception e){
            CLog.e(TAG, "", e);
        }

    }

    private void connect(){
        if(ReaderCtrlApp.iService != null){
            if ("".equals(app.getBluetoothDevice())) {
                if (ReaderCtrlApp.iService.getBluetoothState() != BluetoothState.STATE_CONNECTED) {
                    startActivity(new Intent(getActivity(), BluetoothDevicesActivity.class));
                }
            } else {
                ReaderCtrlApp.iService.connect(app.getBluetoothAddress());
            }
        }
    }

    private void disconnect(){
        if (ReaderCtrlApp.iService != null) {
            ReaderCtrlApp.iService.disconnect();
        }
    }

}
