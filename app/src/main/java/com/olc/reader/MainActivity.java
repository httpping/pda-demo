package com.olc.reader;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.olc.fragment.BluetoothFragment;
import com.olc.fragment.ScanFragment;
import com.olc.fragment.UHFFragment;
import com.olc.reader.service.IReaderCurrentTypeCallback;
import com.olc.reader.service.bt.BluetoothState;
import com.olc.reader.service.uhf.IReaderTemperatureCallback;
import com.olc.util.CLog;
import com.olc.view.ViewPagerSlide;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import viewpagerindicator.TabPageIndicator;

public class MainActivity extends FragmentActivity implements View.OnClickListener{

    private static final String TAG = "MainActivity";
    private String[] mTabArrays;
    private TextView tv_title;
    private ViewPagerSlide mViewPager;
    private ArrayList<Fragment> mFragmentList;
    private MyFragmentPagerAdapter mMyFrageStatePagerAdapter;
    private static boolean isOutPaper = false;

    private BluetoothAdapter mBluetoothAdapter;
    private ReaderCtrlApp app;
    private long firstTime = 0;

    private BluetoothFragment bluetoothFragment;
    private UHFFragment uhfFragment;
    private ScanFragment scanFragment;

    AlertDialog alertDialog;
    public View view;
    List<String> data = new ArrayList<>();
    ListView listView;
    ArrayAdapter<String> adapter;

    public static final long TWO_SECOND = 2 * 1000;
    long preTime;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION};

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            verifyStoragePermissions(this);
            setContentView(R.layout.activity_main);
            app = (ReaderCtrlApp) getApplication();
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            InitializationFragmentView();
        }catch (Exception e){
            CLog.e(TAG,"",e);
        }
    }

    private void InitializationFragmentView() {
        mTabArrays = new String[]{getResources().getString(R.string.tab_page_title_first), getResources().getString(R.string.tab_page_title_uhf), getResources().getString(R.string.tab_page_title_scan)};

        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setText(R.string.tab_page_title_first);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.tab_page_title_uhf);
        toolbar.setTitleTextColor(Color.WHITE);
        TabPageIndicator tabPageIndicator = (TabPageIndicator) findViewById(R.id.indicator);
        mViewPager = (ViewPagerSlide) findViewById(R.id.viewpager);
        findViewById(R.id.btn_more).setOnClickListener(this);

        mFragmentList = new ArrayList<Fragment>();

        bluetoothFragment = new BluetoothFragment();
        scanFragment = new ScanFragment();
        uhfFragment = new UHFFragment();
        mFragmentList.add(bluetoothFragment);
        mFragmentList.add(uhfFragment);
        mFragmentList.add(scanFragment);

        mMyFrageStatePagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), mFragmentList);
        mViewPager.setAdapter(mMyFrageStatePagerAdapter);
        //Prevent frequent destruction of views
        mViewPager.setOffscreenPageLimit(2);
        tabPageIndicator.setViewPager(mViewPager);

        ReaderCtrlApp.mViewPager = mViewPager;

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {}
            @Override
            public void onPageSelected(int i) {
//                setCurrentPageSelect(i);
            }
            @Override
            public void onPageScrollStateChanged(int i) { }
        });

        mViewPager.setCurrentItem(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (!isBluetoothAvailable()) {
                Toast.makeText(getApplicationContext()
                        , R.string.toast_bluetooth_available
                        , Toast.LENGTH_SHORT).show();
                finish();
            }

            if (!isBluetoothEnabled()) {
                mBluetoothAdapter.enable();
            }

            if (!"".equals(app.getBluetoothDevice())) {
                if (ReaderCtrlApp.iService != null) {
                    if (ReaderCtrlApp.iService.getBluetoothState() == BluetoothState.STATE_CONNECTED) {
                        mViewPager.setCurrentItem(1);
                    } else {
                        ReaderCtrlApp.iService.connect(app.getBluetoothAddress());
                    }
                }
            }
        }catch (Exception e){
            CLog.e(TAG,"",e);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ReaderCtrlApp.iService != null) {
            ReaderCtrlApp.iService.disconnect();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long currentTime = new Date().getTime();
            if ((currentTime - preTime) > TWO_SECOND) {
                Toast.makeText(this, R.string.toast_app_exit, Toast.LENGTH_SHORT).show();
                preTime = currentTime;
                return true;
            } else {
                if (ReaderCtrlApp.iService != null) {
                    ReaderCtrlApp.iService.disconnect();
                }
                ActivityTack.getInstanse().exit(this);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_more:
                showListDialog();
                break;
        }
    }

    private void setCurrentPageSelect(int i) {
        IReaderCurrentTypeCallback mReaderCurrentTypeCallback = new IReaderCurrentTypeCallback.Stub() {
            @Override
            public void response(byte ErrorCode)  {
                CLog.d(TAG, "ErrorCode = " + ErrorCode);
            }
        };
        if (i == 0)
            tv_title.setText(R.string.tab_page_title_first);
        if (i == 1) {
            tv_title.setText(R.string.tab_page_title_scan);
            if (ReaderCtrlApp.iService != null) {
                ReaderCtrlApp.iService.setReaderCurrentType(mReaderCurrentTypeCallback, (byte) 0x00);
            }
        }
        if (i == 2) {
            tv_title.setText(R.string.tab_page_title_uhf);
            if (ReaderCtrlApp.iService != null) {
                ReaderCtrlApp.iService.setReaderCurrentType(mReaderCurrentTypeCallback, (byte) 0x01);
            }
        }
    }

    private void showListDialog() {
        final String[] items = { getResources().getString(R.string.bluetooth_btn_clean_device),
                getResources().getString(R.string.bluetooth_btn_clean_log),
                getResources().getString(R.string.dialog_more_item_reader_temperature)};
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(this);
        listDialog.setTitle(R.string.dialog_more_title);
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // ...To-do
                switch (which){
                    case 0:
                        clearDevices();
                        break;
                    case 1:
                        CLog.delLog();
                        break;
                    case 2:
                        getReaderTemperature();
                        break;
                }
                HideSoftInputFromWindow(MainActivity.this);
            }
        });
        listDialog.show();
    }

    private void clearDevices() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_device_remove_title);
        builder.setMessage(R.string.dialog_device_remove_msg);
        builder.setPositiveButton(R.string.dialog_device_remove_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                app.removeDevice();
                disconnect();
            }
        });
        builder.setNegativeButton(R.string.dialog_device_remove_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    private void getReaderTemperature() {
        if(ReaderCtrlApp.iService != null){
            if (ReaderCtrlApp.iService.getBluetoothState() != BluetoothState.STATE_CONNECTED) {
                Toast.makeText(this, R.string.reader_temperature_bluetooth_discon_toast,Toast.LENGTH_SHORT).show();
            } else {
                IReaderTemperatureCallback readerTemperatureCallback = new IReaderTemperatureCallback.Stub() {
                    @Override
                    public void success(final byte PlusMinus, final byte Temp)  {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String tag = (PlusMinus == 0) ? "-" : "+";
                                int temperature = (int)Temp;
                                String msg = String.format(getResources().getString(R.string.reader_temperature), tag, temperature);
                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void failed(byte ErrorCode) {

                    }
                };
                ReaderCtrlApp.iService.GetReaderTemperature(readerTemperatureCallback);
            }
        }
    }

    public boolean isBluetoothAvailable() {
        try {
            if (mBluetoothAdapter == null || mBluetoothAdapter.getAddress().equals(null))
                return false;
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }

    private void disconnect(){
        if (ReaderCtrlApp.iService != null) {
            ReaderCtrlApp.iService.disconnect();
        }
    }

    public boolean isBluetoothEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    public static void HideSoftInputFromWindow(Activity activity) {
        // TODO Auto-generated method stub
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen=imm.isActive();//isOpen若返回true，则表示输入法打开
        if(isOpen && activity.getCurrentFocus() != null){
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getApplicationWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        ArrayList<Fragment> list;

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            super.destroyItem(container, position, object);
        }

        public MyFragmentPagerAdapter(FragmentManager fm, ArrayList<Fragment> list) {
            super(fm);
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Fragment getItem(int arg0) {
            return list.get(arg0);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabArrays[position];
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public float getPageWidth(int position) {
            return super.getPageWidth(position);
        }
    }

}

