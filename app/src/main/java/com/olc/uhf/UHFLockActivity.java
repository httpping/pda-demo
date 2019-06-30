package com.olc.uhf;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.olc.reader.BaseActivity;
import com.olc.reader.R;
import com.olc.reader.ReaderCtrlApp;
import com.olc.reader.service.uhf.ILockCallback;
import com.olc.reader.service.uhf.IReadCallback;
import com.olc.reader.service.uhf.ITagSelectCallback;
import com.olc.util.CLog;
import com.olc.util.Cmd;
import com.olc.util.ErrorCodeInfo;
import com.olc.util.Util;
import com.olc.view.RadioGroup;

public class UHFLockActivity extends BaseActivity implements View.OnClickListener {

    static final String TAG = "UHFLockActivity";
    byte[] mEPC = null;

    TextView tv_selected_epc;
    TextView tv_info;
    RadioGroup rg_Lock_MemBank, rg_LockType;
    RadioButton rb_Lock_UserMemory, rb_Lock_TidMemory, rb_Lock_EPCMemory, rb_Lock_AccessPassword, rb_Lock_KillPassword;
    RadioButton rb_LockType_Open, rb_LockType_Lock, rb_LockType_PermanentOpen, rb_LockType_PermanentLock;

    EditText et_PassWord;

    boolean TagSelectState;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.uhf_lock);

            setTitle(R.string.uhf_function_lock);

            mEPC = getIntent().getByteArrayExtra("EPC");
            tv_selected_epc = (TextView) findViewById(R.id.tv_selected_epc);
            if (mEPC != null)
                tv_selected_epc.setText(Util.bytesToHexString(mEPC));

            tv_info = (TextView) findViewById(R.id.tv_info);
            tv_info.setMovementMethod(ScrollingMovementMethod.getInstance());

            rg_Lock_MemBank = (RadioGroup) findViewById(R.id.rg_Lock_MemBank);
            rb_Lock_UserMemory = (RadioButton) findViewById(R.id.rb_Lock_UserMemory);
            rb_Lock_EPCMemory = (RadioButton) findViewById(R.id.rb_Lock_EPCMemory);
            rb_Lock_TidMemory = (RadioButton) findViewById(R.id.rb_Lock_TidMemory);
            rb_Lock_AccessPassword = (RadioButton) findViewById(R.id.rb_Lock_AccessPassword);
            rb_Lock_KillPassword = (RadioButton) findViewById(R.id.rb_Lock_KillPassword);

            rg_LockType = (RadioGroup) findViewById(R.id.rg_LockType);
            rb_LockType_Open = (RadioButton) findViewById(R.id.rb_LockType_Open);
            rb_LockType_Lock = (RadioButton) findViewById(R.id.rb_LockType_Lock);
            rb_LockType_PermanentOpen = (RadioButton) findViewById(R.id.rb_LockType_PermanentOpen);
            rb_LockType_PermanentLock = (RadioButton) findViewById(R.id.rb_LockType_PermanentLock);

            et_PassWord = (EditText)findViewById(R.id.et_PassWord);

            findViewById(R.id.btn_tag_select).setOnClickListener(this);
            findViewById(R.id.btn_lock).setOnClickListener(this);
            findViewById(R.id.btn_clear).setOnClickListener(this);
            findViewById(R.id.btn_readTest).setOnClickListener(this);

            findViewById(R.id.btn_lock).setEnabled(false);
            findViewById(R.id.btn_readTest).setEnabled(false);
        }catch (Exception e){
            CLog.e(TAG, "", e);
        }

    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.btn_tag_select:
                    tagSelect();
                    break;
                case R.id.btn_lock:
                    lock();
                    break;
                case R.id.btn_readTest:
                    read();
                    break;
                case R.id.btn_clear:
                    tv_info.setText("");
                    break;
            }
        }catch (Exception e){
            CLog.e(TAG, "", e);
        }

    }

    private void tagSelect() {
        try {
            if (mEPC == null) {
                Toast.makeText(this, R.string.uhf_read_toast_tagselect, Toast.LENGTH_SHORT).show();
                return;
            }
            if (ReaderCtrlApp.iService != null) {
                ITagSelectCallback tagSelectCallback = new ITagSelectCallback.Stub() {
                    @Override
                    public void response(final byte ErrorCode)  {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
	                            String ErrorCodeStr = ErrorCodeInfo.getErrorInfo(UHFLockActivity.this, ErrorCode);
	                            ErrorCodeStr = String.format(getResources().getString(R.string.uhf_read_tagseecct_info), ErrorCodeStr);
	                            String info = tv_info.getText().toString().trim();
                                if (ErrorCode == Cmd.COMMOND_SUCCESS) {
                                    findViewById(R.id.btn_readTest).setEnabled(true);
                                    TagSelectState = true;
                                } else {
                                    findViewById(R.id.btn_readTest).setEnabled(false);
                                    TagSelectState = false;
                                }
                                if (!"".equals(info)) {
                                    info = info + "\r\n";
                                }
                                tv_info.setText(info + ErrorCodeStr);
                            }
                        });
                    }
                };
                ReaderCtrlApp.iService.SetTagSelected(tagSelectCallback, mEPC);
            }
        }catch (Exception e){
            CLog.e(TAG, "", e);
        }

    }

    private void lock() {
        try {
            String PassWordStr = et_PassWord.getText().toString().trim();
            if (PassWordStr.length() != 8) {
                Toast.makeText(this, R.string.uhf_read_toast_pwd, Toast.LENGTH_SHORT).show();
                return;
            }
            PassWordStr = "".equals(PassWordStr) ? "00000000" : PassWordStr;
            byte[] PassWord = Util.stringToBytes(PassWordStr);

            byte MemBank_Lock = 0x01;
            if (rb_Lock_UserMemory.isChecked())
                MemBank_Lock = 0x01;
            if (rb_Lock_TidMemory.isChecked())
                MemBank_Lock = 0x02;
            if (rb_Lock_EPCMemory.isChecked())
                MemBank_Lock = 0x03;
            if (rb_Lock_AccessPassword.isChecked())
                MemBank_Lock = 0x04;
            if (rb_Lock_KillPassword.isChecked())
                MemBank_Lock = 0x05;

            byte LockType = 0x00;
            if (rb_LockType_Open.isChecked())
                LockType = 0x00;
            if (rb_LockType_Lock.isChecked())
                LockType = 0x01;
            if (rb_LockType_PermanentOpen.isChecked())
                LockType = 0x02;
            if (rb_LockType_PermanentLock.isChecked())
                LockType = 0x03;

            if (ReaderCtrlApp.iService != null) {
                ReaderCtrlApp.iService.Lock(new ILockCallback.Stub() {
                    @Override
                    public void success(byte[] PC, byte[] EPC, byte ErrCode) {
                        result(ErrCode);
                    }

                    @Override
                    public void failed(byte ErrorCode) {
                        result(ErrorCode);
                    }

                    void result(final byte ErrorCode){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String info = tv_info.getText().toString().trim();
                                if(!"".equals(info)){
                                    info = info + "\r\n";
                                }
	                                String ErrorCodeStr = ErrorCodeInfo.getErrorInfo(UHFLockActivity.this, ErrorCode);
	                                ErrorCodeStr = String.format(getResources().getString(R.string.uhf_lock_info), ErrorCodeStr);
                                tv_info.setText(info + ErrorCodeStr);
                            }
                        });
                    }
                }, PassWord, MemBank_Lock, LockType);
            }
        }catch (Exception e){
            CLog.e(TAG, "", e);
        }

    }

    private void read(){
        try {
            if(!TagSelectState){
                Toast.makeText(this, R.string.uhf_read_toast_tagselect, Toast.LENGTH_SHORT).show();
                return;
            }

            byte MemBank = 0x00;

	        String WordAddStr = "2";
	        WordAddStr = "".equals(WordAddStr) ? "0" : WordAddStr;
			byte[] WordAdd = Util.shortToByte(Short.parseShort(WordAddStr));

            String WordCntStr = "2";
            byte WordCnt =  Byte.parseByte(WordCntStr);

            String PassWordStr = "00000000";
            PassWordStr = "".equals(PassWordStr) ? "00000000" : PassWordStr;
            byte[] PassWord = Util.stringToBytes(PassWordStr);

            if(ReaderCtrlApp.iService != null){
                final IReadCallback readCallback = new IReadCallback.Stub() {
                    @Override
                    public void success(byte[] PC, byte[] EPC, final byte[] ReadData)  {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String info = tv_info.getText().toString().trim();
                                if(!"".equals(info)){
                                    info = info + "\r\n";
                                }
                                String RESERVED = Util.bytesToHexString(ReadData);
                                String hint = "";
                                if("00000000".equals(RESERVED)){
                                    hint = "\r\n"+getResources().getString(R.string.uhf_lock_info_pwd);
                                    findViewById(R.id.btn_lock).setEnabled(false);
                                }else{
                                    findViewById(R.id.btn_lock).setEnabled(true);
                                    et_PassWord.setText(RESERVED);
                                }
                                tv_info.setText(String.format(getResources().getString(R.string.uhf_lock_info_readpwd), info, RESERVED, hint));
                            }
                        });
                    }

                    @Override
                    public void failed(final byte ErrorCode) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String info = tv_info.getText().toString().trim();
                                if(!"".equals(info)){
                                    info = info + "\r\n";
                                }
	                            String ErrorCodeStr = ErrorCodeInfo.getErrorInfo(UHFLockActivity.this, ErrorCode);
	                            ErrorCodeStr = String.format(getResources().getString(R.string.uhf_read_info), ErrorCodeStr);
	                            tv_info.setText(info + ErrorCodeStr);
                            }
                        });
                    }
                };

                ReaderCtrlApp.iService.Read(readCallback, MemBank, WordAdd, WordCnt, PassWord);
            }
        }catch (Exception e){
            CLog.e(TAG, "", e);
        }

    }
}
