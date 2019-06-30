package com.olc.uhf;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.olc.reader.BaseActivity;
import com.olc.reader.R;
import com.olc.reader.ReaderCtrlApp;
import com.olc.reader.service.IReaderCurrentTypeCallback;
import com.olc.util.CLog;

public class FuncationActivity extends BaseActivity implements View.OnClickListener {

    static final String TAG = "FuncationActivity";
    private byte[] EPC;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_funcation);

            setTitle(R.string.uhf_function);
            EPC = getIntent().getByteArrayExtra("EPC");

            findViewById(R.id.btn_read).setOnClickListener(this);
            findViewById(R.id.btn_write).setOnClickListener(this);
            findViewById(R.id.btn_lock).setOnClickListener(this);
            findViewById(R.id.btn_kill).setOnClickListener(this);

            findViewById(R.id.btn_read).setEnabled(false);
            findViewById(R.id.btn_write).setEnabled(false);
            findViewById(R.id.btn_lock).setEnabled(false);
            findViewById(R.id.btn_kill).setEnabled(false);

            setReaderTriggerStatus((byte) 0x05);
        }catch (Exception e){
            CLog.e(TAG, "", e);
        }

    }

    @Override
    public void onClick(View v) {
        try {
            if (EPC == null) {
                Toast.makeText(this, "EPC datas is null", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent();
            switch (v.getId()) {
                case R.id.btn_read:
                    intent.setClass(this, UHFReadActivity.class);
                    intent.putExtra("EPC", EPC);
                    startActivity(intent);
                    break;
                case R.id.btn_write:
                    intent.setClass(this, UHFWriteActivity.class);
                    intent.putExtra("EPC", EPC);
                    startActivity(intent);
                    break;
                case R.id.btn_lock:
                    intent.setClass(this, UHFLockActivity.class);
                    intent.putExtra("EPC", EPC);
                    startActivity(intent);
                    break;
                case R.id.btn_kill:
                    intent.setClass(this, UHFKillActivity.class);
                    intent.putExtra("EPC", EPC);
                    startActivity(intent);
                    break;
            }
        }catch (Exception e){
            CLog.e(TAG, "", e);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setReaderTriggerStatus((byte) 0x06);
    }

    private void setReaderTriggerStatus(final byte type) {
        if (ReaderCtrlApp.iService != null) {
            IReaderCurrentTypeCallback readerCurrentTypeCallback = new IReaderCurrentTypeCallback.Stub() {
                @Override
                public void response(byte ErrorCode) {
                    refreshUI(type, ErrorCode);
                }
            };
            ReaderCtrlApp.iService.setReaderCurrentType(readerCurrentTypeCallback, type);
        }
    }

    private void refreshUI(final byte type, final byte ErrorCode) {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ErrorCode == (byte) 0x10) {
                        if (type == (byte) 0x05) {
                            findViewById(R.id.btn_read).setEnabled(true);
                            findViewById(R.id.btn_write).setEnabled(true);
                            findViewById(R.id.btn_lock).setEnabled(true);
                            findViewById(R.id.btn_kill).setEnabled(true);
                        } else {
                            finish();
                        }
                    }
                    if (ErrorCode == (byte) 0x36) {

                    }
                }
            });
        }catch (Exception e){
            CLog.e(TAG, "", e);
        }

    }
}
