package com.olc.fragment;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SearchView;
import android.widget.TextView;

import com.olc.model.ScanModel;
import com.olc.reader.IBluetoothCallback;
import com.olc.reader.MainActivity;
import com.olc.reader.R;
import com.olc.reader.ReaderCtrlApp;
import com.olc.reader.service.IReaderCurrentTypeCallback;
import com.olc.reader.service.scan.IScanCallback;
import com.olc.reader.service.bt.BluetoothState;
import com.olc.util.CLog;
import com.olc.view.FlexibleListView;

import java.util.ArrayList;
import java.util.List;

public class ScanFragment extends LazyFragment implements View.OnClickListener, IBluetoothCallback {

    static final String TAG = "ScanFragment";

    // Flag bit, the flag has been initialized.
    private boolean isPrepared;

    private ReaderCtrlApp app;
    private View mView;
    private FlexibleListView fl_scan;
    private Button btn_scan;

    SearchView search;

    Toolbar toolbar;
    TextView tv_title;
    CheckBox cb_continue;
    Handler scanHandler = new Handler();
    long delayMillis = 0;

    private int index = 1;
    private List<ScanModel> models = new ArrayList<ScanModel>();
    private ListViewAdapter scanAdapter = new ListViewAdapter();

    IScanCallback scanCallback = new IScanCallback.Stub() {

        @Override
        public void success(String barcode)  {
            try {
                ScanModel model = new ScanModel();
                model.setBarcode(barcode);

                Message msg = new Message();
                msg.what = 0;
                msg.obj = model;
                refreshScanHandler.sendMessage(msg);

                if (cb_continue.isChecked() &&
                        btn_scan.getText().toString().equals(getResources().getString(R.string.scan_stop_btn))) {
                    scanHandler.postDelayed(scanRun, delayMillis);
                } else {
                    updateUI(1);
                }
            } catch (Exception e) {
                CLog.e(TAG, "", e);
            }

        }

        @Override
        public void failed(final int ErrorCode)  {
            try {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (cb_continue.isChecked() &&
                                btn_scan.getText().toString().equals(getResources().getString(R.string.scan_stop_btn))) {
                            scanHandler.postDelayed(scanRun, delayMillis);
                        } else {
                            //cmd failed
                            if (ErrorCode == (byte) 0xFF) {
                                updateUI(1);
                            }
                            //Reader trigger trigger
                            if (ErrorCode == (byte) 0x01) {
                                updateUI(0);
                            }
                        }
                    }
                });
            } catch (Exception e) {
                CLog.e(TAG, "", e);
            }

        }
    };

    @SuppressLint("HandlerLeak")
    private Handler refreshScanHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            try {
                ScanModel model = (ScanModel) msg.obj;
                addModel(model);

                scanAdapter.refresh(models);
                scanAdapter.notifyDataSetChanged();
                fl_scan.setSelection(models.size() - 1);
            } catch (Exception e) {
                CLog.e(TAG, "", e);
            }

        }
    };

    int state = 0;
    boolean isVisibleToUser;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        try {
            this.isVisibleToUser = isVisibleToUser;
            if (isVisibleToUser) {
                ReaderCtrlApp.bluetoothCallback = this;
                if (ReaderCtrlApp.iService != null) {
                    state = ReaderCtrlApp.iService.getBluetoothState();
                }
            }
        } catch (Exception e) {
            CLog.e(TAG, "", e);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        try {
            mView = inflater.inflate(R.layout.fragment_scan, container, false);

            isPrepared = true;
            lazyLoad();
        } catch (Exception e) {
            CLog.e(TAG, "", e);
        }

        return mView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan:
                doScan();
                break;
            case R.id.btn_clear:
                clean();
                break;
        }
    }

    @Override
    protected void lazyLoad() {
        if (!isPrepared || !isVisible) {
            return;
        }
        app = (ReaderCtrlApp) getActivity().getApplication();
        ReaderCtrlApp.bluetoothCallback = this;

        fl_scan = (FlexibleListView) mView.findViewById(R.id.fl_scan);

        mView.findViewById(R.id.btn_scan).setOnClickListener(this);
        mView.findViewById(R.id.btn_clear).setOnClickListener(this);
        mView.findViewById(R.id.btn_scan).setEnabled(false);
        mView.findViewById(R.id.btn_clear).setEnabled(false);
        btn_scan = (Button) mView.findViewById(R.id.btn_scan);
        cb_continue = (CheckBox) mView.findViewById(R.id.cb_continue);

        cb_continue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btn_scan.setText(R.string.scan_continue_btn);
                } else {
                    btn_scan.setText(R.string.scan_btn);
                }
            }
        });

        search = (SearchView) mView.findViewById(R.id.search);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                doSearch(newText);
                return false;
            }
        });

        MainActivity activity = (MainActivity) getActivity();
        toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        tv_title = (TextView) activity.findViewById(R.id.tv_title);

        tv_title.setText(R.string.tab_page_title_scan);

        scanAdapter.refresh(models);
        fl_scan.setAdapter(scanAdapter);

        if (ReaderCtrlApp.iService != null) {
            ReaderCtrlApp.iService.setScanCallback(scanCallback);
            int state = ReaderCtrlApp.iService.getBluetoothState();
            onServiceStateChanged(state);

            if (state == BluetoothState.STATE_CONNECTED) {
                setCurrentPageSelect();
            }
        }
    }

    @Override
    public void onServiceStateChanged(final int state) {
        if (getActivity() == null)
            return;
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (state) {
                        case BluetoothState.STATE_CONNECTED:
                            mView.findViewById(R.id.btn_scan).setEnabled(true);
                            mView.findViewById(R.id.btn_clear).setEnabled(true);
                            break;
                        case BluetoothState.STATE_NONE:
                        case BluetoothState.STATE_LISTEN:
                        case BluetoothState.STATE_CONNECTING:
                        case BluetoothState.STATE_CONNECTION_FAILED:
                        case BluetoothState.STATE_DISCONNECTED:
                            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                            mView.findViewById(R.id.btn_scan).setEnabled(false);
                            mView.findViewById(R.id.btn_clear).setEnabled(false);
                            ReaderCtrlApp.mViewPager.setCurrentItem(0);
                            break;
                    }
                }
            });
        } catch (Exception e) {
            CLog.e(TAG, "", e);
        }

    }

    private void setCurrentPageSelect() {
        IReaderCurrentTypeCallback mReaderCurrentTypeCallback = new IReaderCurrentTypeCallback.Stub() {
            @Override
            public void response(final byte ErrorCode)  {
                if(getActivity() == null){
                    switch (ErrorCode){
                        case 0://scan
                            ReaderCtrlApp.mViewPager.setCurrentItem(2);
                            break;
                        case 1://uhf
                            ReaderCtrlApp.mViewPager.setCurrentItem(1);
                            break;
                    }
                    return;
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mView.findViewById(R.id.btn_scan).setEnabled(true);
                        mView.findViewById(R.id.btn_clear).setEnabled(true);

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
                });
                CLog.d(TAG, "ErrorCode = " + ErrorCode);
            }
        };

        if (ReaderCtrlApp.iService != null) {
            ReaderCtrlApp.iService.setReaderCurrentType(mReaderCurrentTypeCallback, (byte) 0x00);
        }
    }

    private void doScan() {
        try {
            if (cb_continue.isChecked()) {
                if (btn_scan.getText().toString().equals(getResources().getString(R.string.scan_continue_btn))) {
                    scanHandler.post(scanRun);
                    btn_scan.setText(R.string.scan_stop_btn);
                } else {
                    scanHandler.removeCallbacks(scanRun);
                    btn_scan.setText(R.string.scan_continue_btn);
                }
            } else {
                scanHandler.post(scanRun);
                btn_scan.setText(R.string.scan_btn);
            }

        } catch (Exception e) {
            CLog.e(TAG, "", e);
        }

    }

    private Runnable scanRun = new Runnable() {
        @Override
        public void run() {
            if (ReaderCtrlApp.iService != null) {
                updateUI(0);

                ReaderCtrlApp.iService.doScan();
            }
        }
    };

    private void refresh() {
        scanAdapter.notifyDataSetChanged();
        fl_scan.setAdapter(scanAdapter);
    }

    private void clean() {
        try {
            models = new ArrayList<>();
            scanAdapter.notifyDataSetChanged();
            scanAdapter.refresh(models);
            fl_scan.setAdapter(scanAdapter);
        } catch (Exception e) {
            CLog.e(TAG, "", e);
        }

    }

    private void addModel(ScanModel model) {
        try {
            boolean flag = false;
            for (int i = 0; i < models.size(); i++) {
                String barcode = models.get(i).getBarcode();
                long count = models.get(i).getCount();
                if (barcode.equals(model.getBarcode())) {
                    models.get(i).setCount(count + 1);
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                models.add(model);
            }
        } catch (Exception e) {
            CLog.e(TAG, "", e);
        }

    }

    private void updateUI(final int type) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //cmd before type = 0
                    if (type == 0) {
                        tv_title.setText(R.string.scan_hint_scaning);
                        toolbar.setBackgroundColor(getResources().getColor(R.color.scanning_color));

                        if (ReaderCtrlApp.mViewPager != null) {
                            ReaderCtrlApp.mViewPager.setCurrentItem(2);
                            ReaderCtrlApp.mViewPager.setSlide(false);
                        }
                    } else if (type == 1) {//cmd end type = 0
                        tv_title.setText(R.string.tab_page_title_scan);
                        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                        if (ReaderCtrlApp.mViewPager != null) {
                            ReaderCtrlApp.mViewPager.setCurrentItem(2);
                            ReaderCtrlApp.mViewPager.setSlide(true);
                        }
                    }

                    scanAdapter.notifyDataSetChanged();
                    fl_scan.setAdapter(scanAdapter);
                }
            });
        } catch (Exception e) {
            CLog.e(TAG, "", e);
        }

    }

    @SuppressLint("StaticFieldLeak")
    private void doSearch(final String data) {
        try {
            if (!TextUtils.isEmpty(data)) {
                new AsyncTask<Void, Void, List<ScanModel>>() {
                    @Override
                    protected List<ScanModel> doInBackground(Void... voids) {
                        List<ScanModel> tempModels = new ArrayList<>();
                        for (ScanModel model : models) {
                            if (model.getBarcode().contains(data)) {
                                tempModels.add(model);
                            }
                        }
                        return tempModels;
                    }

                    @Override
                    protected void onPostExecute(List<ScanModel> scanModels) {
                        super.onPostExecute(scanModels);
                        scanAdapter.refresh(scanModels);
                        fl_scan.setAdapter(scanAdapter);
                    }
                }.execute();
            } else {
                scanAdapter.refresh(models);
                fl_scan.setAdapter(scanAdapter);
            }
        } catch (Exception e) {
            CLog.e(TAG, "", e);
        }

    }

    @Override
    protected void doDiscon() {
        super.doDiscon();
        scanHandler.removeCallbacks(scanRun);
    }

    class ListViewAdapter extends BaseAdapter {

        private List<ScanModel> models = new ArrayList<>();

        public void refresh(List<ScanModel> tempModels) {
            models = tempModels;
        }

        @Override
        public int getCount() {
            return models.size();
        }

        @Override
        public Object getItem(int position) {
            return models.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            try {
                ListViewAdapter.ViewHolder holder = null;
                if (convertView == null) {
                    convertView = LayoutInflater.from(getActivity()).inflate(
                            R.layout.scan_item_view, null);
                    holder = new ListViewAdapter.ViewHolder();
                    holder.tv_scan = (TextView) convertView.findViewById(R.id.tv_scan);
                    holder.tv_count = (TextView) convertView.findViewById(R.id.tv_count);
                    convertView.setTag(holder);
                } else {
                    holder = (ListViewAdapter.ViewHolder) convertView.getTag();
                }

                ScanModel model = models.get(position);
                holder.tv_scan.setText((position + 1) + " . " + model.getBarcode());
                holder.tv_count.setText("" + model.getCount());
            } catch (Exception e) {
                CLog.e(TAG, "", e);
            }

            return convertView;
        }

        public final class ViewHolder {
            public TextView tv_scan;
            public TextView tv_count;
        }
    }
}
