package com.olc.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.olc.model.InventoryModel;
import com.olc.reader.IBluetoothCallback;
import com.olc.reader.MainActivity;
import com.olc.reader.R;
import com.olc.reader.ReaderCtrlApp;
import com.olc.reader.service.IDataReceptionCompletedCallback;
import com.olc.reader.service.IReaderCurrentTypeCallback;
import com.olc.reader.service.uhf.IInventoryCallback;
import com.olc.reader.service.uhf.ITagSelectCallback;
import com.olc.reader.service.bt.BluetoothState;
import com.olc.uhf.FuncationActivity;
import com.olc.util.CLog;
import com.olc.util.ErrorCodeInfo;
import com.olc.util.Util;
import com.olc.view.FlexibleListView;

import java.util.ArrayList;
import java.util.List;

public class UHFFragment extends LazyFragment implements View.OnClickListener, AdapterView.OnItemClickListener, RadioGroup.OnCheckedChangeListener, IBluetoothCallback {
    static final String TAG = "UHFFragment";

    // Flag bit, the flag has been initialized.
    private boolean isPrepared;

    Toolbar toolbar;
    TextView tv_title;

    private View mView;
    private TextView tv_readcount, tv_totalcount;
    private TextView tv_cmd_execute_time, tv_running_time;
    private RadioGroup rg;
    private RadioButton rb_unrepeat, rb_repeat, rb_tid;
    private FlexibleListView fl_btDevices;
    private CheckBox cb_tid;

    SearchView search;

    Button btn_inventory;
    CheckBox cb_continue;
    Handler uhfHandler = new Handler();
    long delayMillis = 0;

    private ReaderCtrlApp app;
    int index = 0, count = 0;
    long startCmdTime, endCmdTime;
    boolean CONTINUE_INVENTORY = false;

    List<InventoryModel> inventorys = new ArrayList<InventoryModel>();
    InventoryModel epc = new InventoryModel();
    InventoryListViewAdapter adapter = new InventoryListViewAdapter();

    int state = 0;
    boolean isVisibleToUser;

    IInventoryCallback inventoryCallback = new IInventoryCallback.Stub() {
        @Override
        public void tagResponse(byte[] PC, byte[] EPC, byte RSSI)  {
            try {
                InventoryModel model = new InventoryModel();
                model.setPC(PC);
                model.setPCHEX(Util.bytesToHexString(PC));
                model.setEPC(EPC);
                model.setEPCHEX(Util.bytesToHexString(EPC));
                model.setRSSI(RSSI);
                model.setRSSIHEX(Util.byteToHexString(RSSI));

                Message msg = new Message();
                msg.what = 0;
                msg.obj = model;
                refreshInventoryHandler.sendMessage(msg);
            }catch (Exception e){
                CLog.e(TAG, "", e);
            }

        }

        @Override
        public void tagResponseTID(byte[] PC, byte[] EPC, byte[] TID)  {
            try {
                InventoryModel model = new InventoryModel();
                model.setPC(PC);
                model.setPCHEX(Util.bytesToHexString(PC));
                model.setEPC(EPC);
                model.setEPCHEX(Util.bytesToHexString(EPC));
                if (TID != null) {
                    model.setTID(TID);
                    model.setTIDHEX(Util.bytesToHexString(TID));
                } else {
                    model.setTIDHEX(getResources().getString(R.string.uhf_read_tid_failed));
                }

                Message msg = new Message();
                msg.what = 0;
                msg.obj = model;
                refreshInventoryHandler.sendMessage(msg);
            }catch (Exception e){
                CLog.e(TAG, "", e);
            }

        }

        @Override
        public void success(final byte[] TotalRead) {
            try {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        endCmdTime = System.currentTimeMillis();

                        int totalcount = 0;
                        for (InventoryModel model : inventorys) {
                            totalcount += model.getCount();
                        }
                        tv_readcount.setText(String.valueOf(totalcount));
                        tv_totalcount.setText(String.valueOf(inventorys.size()));
                        tv_cmd_execute_time.setText(String.valueOf(endCmdTime - startCmdTime));

                        String runTotalTimeStr = tv_running_time.getText().toString();
                        runTotalTimeStr = "".equals(runTotalTimeStr) ? "0" : runTotalTimeStr;
                        long runTotalTime = Long.parseLong(runTotalTimeStr) + endCmdTime - startCmdTime;
                        tv_running_time.setText(String.valueOf(runTotalTime));

                        //Data reception completed
                        if (ReaderCtrlApp.iService != null) {
                            IDataReceptionCompletedCallback cb = new IDataReceptionCompletedCallback.Stub(){

                                @Override
                                public void response(byte ErrorCode) {
                                    if (cb_continue.isChecked() &&
                                            btn_inventory.getText().toString().equals(getResources().getString(R.string.uhf_stop_btn))) {
                                        uhfHandler.postDelayed(uhfRun, delayMillis);
                                    }else{
                                        updateUI(1);
                                    }
                                }
                            };
                            ReaderCtrlApp.iService.DataReceptionCompleted(cb);
                        }
                    }
                });
            }catch (Exception e){
                CLog.e(TAG, "", e);
            }

        }

        @Override
        public void failed(final byte ErrorCode)  {
            if(getActivity() == null){
                if (ReaderCtrlApp.mViewPager != null) {
                    ReaderCtrlApp.mViewPager.setCurrentItem(1);
                }
                return;
            }
            try {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (ErrorCode == (byte) 0x01) {
                            //Reader trigger trigger
                            startCmdTime = System.currentTimeMillis();

                            updateUI(0);
                            return;
                        } else {
                            //cmd failed
                            updateUI(1);
                        }
                        String errornfo = ErrorCodeInfo.getErrorInfo(getActivity(), ErrorCode);
                        Toast.makeText(getActivity(), errornfo, Toast.LENGTH_SHORT).show();
	                }
	            });
			}catch (Exception e){
                CLog.e(TAG, "", e);
            }

        }
    };

    @SuppressLint("HandlerLeak")
    private Handler refreshInventoryHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            try {
                InventoryModel model = (InventoryModel) msg.obj;
                refreshInventory(model);

                adapter.refresh(inventorys);
                adapter.notifyDataSetChanged();
                fl_btDevices.setSelection(inventorys.size() - 1);
            }catch (Exception e){
                CLog.e(TAG, "", e);
            }

        }
    };

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
        }catch (Exception e){
            CLog.e(TAG, "", e);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        try {
            mView = inflater.inflate(R.layout.fragment_uhf, container, false);

            isPrepared = true;
            lazyLoad();
        }catch (Exception e){
            CLog.e(TAG, "", e);
        }

        return mView;
    }

    @Override
    protected void lazyLoad() {
        if (!isPrepared || !isVisible) {
            return;
        }
        tv_readcount = (TextView) mView.findViewById(R.id.tv_readcount);
        tv_totalcount = (TextView) mView.findViewById(R.id.tv_totalcount);
        tv_cmd_execute_time = (TextView) mView.findViewById(R.id.tv_cmd_execute_time);
        tv_running_time = (TextView) mView.findViewById(R.id.tv_running_time);
        fl_btDevices = (FlexibleListView) mView.findViewById(R.id.fl_btDevices);
        mView.findViewById(R.id.btn_inventory).setOnClickListener(this);
        mView.findViewById(R.id.btn_clear).setOnClickListener(this);
        cb_tid = (CheckBox) mView.findViewById(R.id.cb_tid);
        rg = (RadioGroup) mView.findViewById(R.id.rg);
        rg.setOnCheckedChangeListener(this);
        rb_unrepeat = (RadioButton) mView.findViewById(R.id.rb_unrepeat);
        rb_repeat = (RadioButton) mView.findViewById(R.id.rb_repeat);
        rb_tid = (RadioButton) mView.findViewById(R.id.rb_tid);
        btn_inventory = (Button)mView.findViewById(R.id.btn_inventory);
        cb_continue = (CheckBox)mView.findViewById(R.id.cb_continue);

        cb_continue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    btn_inventory.setText(R.string.uhf_continue_btn);
                }else{
                    btn_inventory.setText(R.string.uhf_read_btn_inventory);
                }
            }
        });

        search = (SearchView)mView.findViewById(R.id.search);
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

        fl_btDevices.setOnItemClickListener(this);
        adapter.refresh(inventorys);
        fl_btDevices.setAdapter(adapter);

//        cb_tid.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                tidChecked(isChecked);
//            }
//        });

        MainActivity activity = (MainActivity) getActivity();
        toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        tv_title = (TextView) activity.findViewById(R.id.tv_title);
        tv_title.setText(R.string.tab_page_title_uhf);

        if (ReaderCtrlApp.iService != null) {
            ReaderCtrlApp.iService.SetInventoryCallback(inventoryCallback);
            int state = ReaderCtrlApp.iService.getBluetoothState();
            onServiceStateChanged(state);

            if (state == BluetoothState.STATE_CONNECTED) {
                setCurrentPageSelect();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_inventory:
                inventory();
                break;
            case R.id.btn_clear:
                clearDatas();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            if (rb_tid.isChecked()) {
                return;
            }

            if(CONTINUE_INVENTORY){
                return;
            }

            InventoryModel model = inventorys.get(position);
            Intent intent = new Intent();
            intent.setClass(getActivity(), FuncationActivity.class);
            intent.putExtra("EPC", model.getEPC());
            startActivity(intent);
        }catch (Exception e){
            CLog.e(TAG, "", e);
        }

    }

    @Override
    public void onServiceStateChanged(final int state) {
        if (getActivity() == null) {
            return;
        }
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (state) {
                        case BluetoothState.STATE_CONNECTED:
                            mView.findViewById(R.id.btn_inventory).setEnabled(true);
                            mView.findViewById(R.id.btn_clear).setEnabled(true);
                            break;
                        case BluetoothState.STATE_NONE:
                        case BluetoothState.STATE_LISTEN:
                        case BluetoothState.STATE_CONNECTING:
                        case BluetoothState.STATE_CONNECTION_FAILED:
                        case BluetoothState.STATE_DISCONNECTED:
                            clearDatas();
                            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                            mView.findViewById(R.id.btn_inventory).setEnabled(false);
                            mView.findViewById(R.id.btn_clear).setEnabled(false);
                            ReaderCtrlApp.mViewPager.setCurrentItem(0);
                            break;
                    }
                }
            });
        }catch (Exception e){
            CLog.e(TAG, "", e);
        }

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        byte type = (byte) 0x02;
        if (checkedId == R.id.rb_repeat) {
            type = (byte) 0x02;
        } else if (checkedId == R.id.rb_unrepeat) {
            type = (byte) 0x03;
        } else if (checkedId == R.id.rb_tid) {
            type = (byte) 0x04;
        }
        clearDatas();

        if (ReaderCtrlApp.iService != null) {
            IReaderCurrentTypeCallback readerCurrentTypeCallback = new IReaderCurrentTypeCallback.Stub() {
                @Override
                public void response(final byte ErrorCode)  {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (ErrorCode == (byte) 0x10) {
                                mView.findViewById(R.id.btn_inventory).setEnabled(true);
                            }
                            if (ErrorCode == (byte) 0x36) {
                                mView.findViewById(R.id.btn_inventory).setEnabled(false);
                            }
                        }
                    });
                }
            };
            ReaderCtrlApp.iService.setReaderCurrentType(readerCurrentTypeCallback, type);
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
                        rb_repeat.setChecked(true);

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
            ReaderCtrlApp.iService.setReaderCurrentType(mReaderCurrentTypeCallback, (byte) 0x01);
        }
    }

    private void tidChecked(boolean isChecked) {
        byte type = (byte) 0x04;
        if (isChecked) {
            type = (byte) 0x04;
            mView.findViewById(R.id.rg).setVisibility(View.GONE);
        } else {
            type = (byte) 0x05;
            mView.findViewById(R.id.rg).setVisibility(View.VISIBLE);
        }
        clearDatas();

        if (ReaderCtrlApp.iService != null) {
            IReaderCurrentTypeCallback readerCurrentTypeCallback = new IReaderCurrentTypeCallback.Stub() {
                @Override
                public void response(final byte ErrorCode) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (ErrorCode == (byte) 0x10) {
                                mView.findViewById(R.id.btn_inventory).setEnabled(true);
                            }
                            if (ErrorCode == (byte) 0x36) {
                                mView.findViewById(R.id.btn_inventory).setEnabled(false);
                            }
                        }
                    });
                }
            };
            ReaderCtrlApp.iService.setReaderCurrentType(readerCurrentTypeCallback, type);
        }
    }

    private void inventory() {
        if (cb_continue.isChecked()) {
            if (btn_inventory.getText().toString().equals(getResources().getString(R.string.uhf_stop_btn))) {
                CONTINUE_INVENTORY = false;
                uhfHandler.removeCallbacks(uhfRun);
                btn_inventory.setText(R.string.uhf_continue_btn);
                return;
            }
        }

        ITagSelectCallback clearTagSelectCallback = new ITagSelectCallback.Stub() {
            @Override
            public void response(byte ErrorCode)  {
                if (ErrorCode == (byte) 0x10) {
                    doInventory();
                } else {
                    String errornfo = ErrorCodeInfo.getErrorInfo(getActivity(), ErrorCode);
                    CLog.e(TAG,errornfo);
                }
            }
        };
        if (ReaderCtrlApp.iService != null) {
            ReaderCtrlApp.iService.ClearTagSelected(clearTagSelectCallback);
        }
    }

    private void doInventory() {
        if (cb_continue.isChecked()) {
            if (btn_inventory.getText().toString().equals(getResources().getString(R.string.uhf_continue_btn))) {
                CONTINUE_INVENTORY = true;
                uhfHandler.post(uhfRun);
                btn_inventory.setText(R.string.uhf_stop_btn);
            } else {
                mView.findViewById(R.id.search).setVisibility(View.VISIBLE);
                CONTINUE_INVENTORY = false;
                uhfHandler.removeCallbacks(uhfRun);
                btn_inventory.setText(R.string.uhf_continue_btn);
            }
        } else {
            CONTINUE_INVENTORY = false;
            uhfHandler.post(uhfRun);
            btn_inventory.setText(R.string.uhf_read_btn_inventory);
        }
    }

    private Runnable uhfRun = new Runnable() {
        @Override
        public void run() {
            count = 0;
            index = 0;
            if (ReaderCtrlApp.iService != null) {
                updateUI(0);

                startCmdTime = System.currentTimeMillis();
                if (rb_tid.isChecked()) {
                    ReaderCtrlApp.iService.Inventory(2);
                } else {
                    if (rb_repeat.isChecked()) {
                        ReaderCtrlApp.iService.Inventory(0);
                    }
                    if (rb_unrepeat.isChecked()) {
                        ReaderCtrlApp.iService.Inventory(1);
                    }
                }
            }
        }
    };

    private void updateUI(final int type) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //cmd before type = 0
                    if (type == 0) {
                        tv_title.setText(R.string.scan_hint_scaning);
                        toolbar.setBackgroundColor(getResources().getColor(R.color.scanning_color));
                        mView.findViewById(R.id.search).setVisibility(View.GONE);

//                    mView.findViewById(R.id.btn_inventory).setEnabled(false);
//                    mView.findViewById(R.id.btn_clear).setEnabled(false);
                        for (int i = 0; i < rg.getChildCount(); i++) {
                            rg.getChildAt(i).setEnabled(false);
                        }
                        if (ReaderCtrlApp.mViewPager != null) {
                            ReaderCtrlApp.mViewPager.setCurrentItem(1);
                            ReaderCtrlApp.mViewPager.setSlide(false);
                        }
                    } else if (type == 1) {//cmd end type = 0
                        tv_title.setText(R.string.tab_page_title_uhf);
                        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        mView.findViewById(R.id.search).setVisibility(View.VISIBLE);

//                    mView.findViewById(R.id.btn_inventory).setEnabled(true);
//                    mView.findViewById(R.id.btn_clear).setEnabled(true);
                        for (int i = 0; i < rg.getChildCount(); i++) {
                            rg.getChildAt(i).setEnabled(true);
                        }
                        if (ReaderCtrlApp.mViewPager != null) {
                            ReaderCtrlApp.mViewPager.setCurrentItem(1);
                            ReaderCtrlApp.mViewPager.setSlide(true);
                        }
                    }
                }
            });
        }catch (Exception e){
            CLog.e(TAG, "", e);
        }

    }

    private void refreshInventory(InventoryModel model) {
        try {
            boolean flag = false;
            for (int i = 0; i < inventorys.size(); i++) {
                String epc = inventorys.get(i).getEPCHEX();
                int count = inventorys.get(i).getCount();
                if (model.getEPCHEX().equals(epc)) {
                    inventorys.get(i).setCount(count + 1);
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                int count = model.getCount();
                model.setCount(count + 1);
                inventorys.add(model);
            }
        }catch (Exception e){
            CLog.e(TAG, "", e);
        }

    }

    private void clearDatas() {
        try {
            count = 0;
            tv_totalcount.setText("");
            tv_readcount.setText("");
            tv_cmd_execute_time.setText("");
            tv_running_time.setText("");
            inventorys = new ArrayList<>();
            adapter.refresh(inventorys);
            fl_btDevices.setAdapter(adapter);
            mView.findViewById(R.id.search).setVisibility(View.GONE);
        }catch (Exception e){
            CLog.e(TAG, "", e);
        }

    }

    @SuppressLint("StaticFieldLeak")
    private void doSearch(final String data){
        try {
            if (!TextUtils.isEmpty(data)) {
                new AsyncTask<Void,Void,List<InventoryModel>>(){
                    @Override
                    protected List<InventoryModel> doInBackground(Void... voids) {
                        List<InventoryModel> models = new ArrayList<InventoryModel>();
                        for(InventoryModel model : inventorys){
                            if(model.getEPCHEX().contains(data)){
                                models.add(model);
                            }
                        }
                        return models;
                    }

                    @Override
                    protected void onPostExecute(List<InventoryModel> inventoryModels) {
                        super.onPostExecute(inventoryModels);
                        adapter.refresh(inventoryModels);
                        fl_btDevices.setAdapter(adapter);
                    }
                }.execute();
            } else {
                adapter.refresh(inventorys);
                fl_btDevices.setAdapter(adapter);
            }
        }catch (Exception e){
            CLog.e(TAG, "", e);
        }
    }

    @Override
    protected void doDiscon() {
        super.doDiscon();
        uhfHandler.removeCallbacks(uhfRun);
    }
    static boolean ITEM_ENABLE = true;
    class InventoryListViewAdapter extends BaseAdapter {

        private List<InventoryModel> inventorys = new ArrayList<>();
        public void refresh(List<InventoryModel> models){
            inventorys = models;
        }

        @Override
        public int getCount() {
            return inventorys.size();
        }

        @Override
        public Object getItem(int position) {
            return inventorys.get(position);
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
                    convertView = LayoutInflater.from(getActivity()).inflate(
                            R.layout.uhf_item_view, null);
                    holder = new ViewHolder();
                    holder.tv_PC = (TextView) convertView.findViewById(R.id.tv_PC);
                    holder.tv_EPC = (TextView) convertView.findViewById(R.id.tv_EPC);
                    holder.tv_count = (TextView) convertView.findViewById(R.id.tv_count);
                    holder.tv_tid = (TextView) convertView.findViewById(R.id.tv_tid);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                InventoryModel model = inventorys.get(position);
                holder.tv_PC.setText((position + 1) + " . " + model.getPCHEX());
                holder.tv_EPC.setText(model.getEPCHEX());
                holder.tv_count.setText("" + model.getCount());
                if (rb_tid.isChecked()) {
                    holder.tv_tid.setVisibility(View.VISIBLE);
                    holder.tv_tid.setText(model.getTIDHEX());
                } else {
                    holder.tv_tid.setVisibility(View.GONE);
                }
            }catch (Exception e){
                CLog.e(TAG, "", e);
            }

            return convertView;
        }

        public final class ViewHolder {
            public TextView tv_PC;
            public TextView tv_EPC;
            public TextView tv_tid;
            public TextView tv_count;
        }
    }

}
