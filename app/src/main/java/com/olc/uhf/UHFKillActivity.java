package com.olc.uhf;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.olc.reader.BaseActivity;
import com.olc.reader.R;
import com.olc.reader.ReaderCtrlApp;
import com.olc.reader.service.uhf.IKillCallback;
import com.olc.reader.service.uhf.IReadCallback;
import com.olc.reader.service.uhf.ITagSelectCallback;
import com.olc.util.CLog;
import com.olc.util.Cmd;
import com.olc.util.ErrorCodeInfo;
import com.olc.util.Util;

public class UHFKillActivity extends BaseActivity implements View.OnClickListener {

    static final String TAG = "UHFKillActivity";
    byte[] mEPC = null;

    TextView tv_selected_epc;
    TextView tv_info;

    EditText et_PassWord;

    boolean TagSelectState;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.uhf_kill);

            setTitle(R.string.uhf_function_kill);

            mEPC = getIntent().getByteArrayExtra("EPC");
            tv_selected_epc = (TextView) findViewById(R.id.tv_selected_epc);
            if (mEPC != null)
                tv_selected_epc.setText(Util.bytesToHexString(mEPC));

            tv_info = (TextView) findViewById(R.id.tv_info);
            tv_info.setMovementMethod(ScrollingMovementMethod.getInstance());

            et_PassWord = (EditText) findViewById(R.id.et_PassWord);

            findViewById(R.id.btn_tag_select).setOnClickListener(this);
            findViewById(R.id.btn_kill).setOnClickListener(this);
            findViewById(R.id.btn_clear).setOnClickListener(this);
            findViewById(R.id.btn_readTest).setOnClickListener(this);

            findViewById(R.id.btn_kill).setEnabled(false);
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
                case R.id.btn_kill:
                    kill();
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
        if (mEPC == null) {
            Toast.makeText(this, R.string.uhf_kill_toast_epc, Toast.LENGTH_SHORT).show();
            return;
        }
        if (ReaderCtrlApp.iService != null) {
            ITagSelectCallback tagSelectCallback = new ITagSelectCallback.Stub() {
                @Override
                public void response(final byte ErrorCode)  {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String ErrorCodeStr = null;
                            String errornfo = ErrorCodeInfo.getErrorInfo(UHFKillActivity.this, ErrorCode);
                            ErrorCodeStr = String.format(getResources().getString(R.string.uhf_read_tagseecct_info), errornfo);
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
    }

    private void kill() {
        try {
            if (!TagSelectState) {
                Toast.makeText(this, R.string.uhf_read_toast_tagselect, Toast.LENGTH_SHORT).show();
                return;
            }

            String PassWordStr = et_PassWord.getText().toString().trim();
            if (PassWordStr.length() != 8) {
                Toast.makeText(this, R.string.uhf_kill_toast_pwd, Toast.LENGTH_SHORT).show();
                return;
            }
            PassWordStr = "".equals(PassWordStr) ? "00000000" : PassWordStr;
            byte[] PassWord = Util.stringToBytes(PassWordStr);

            if (ReaderCtrlApp.iService != null) {
                ReaderCtrlApp.iService.Kill(new IKillCallback.Stub() {
                    @Override
                    public void success(byte[] PC, byte[] EPC, byte ErrCode) {
                        result(ErrCode);
                    }

                    @Override
                    public void failed(byte ErrorCode)  {
                        result(ErrorCode);
                    }

                    void result(final byte ErrorCode) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String info = tv_info.getText().toString().trim();
                                if (!"".equals(info)) {
                                    info = info + "\r\n";
                                }
                                String ErrorCodeStr = ErrorCodeInfo.getErrorInfo(UHFKillActivity.this, ErrorCode);
                                ErrorCodeStr = String.format(getResources().getString(R.string.uhf_kill_info), ErrorCodeStr);
                                tv_info.setText(info + ErrorCodeStr);
                            }
                        });
                    }
                }, PassWord);
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

            String WordAddStr = "0";
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
                                    findViewById(R.id.btn_kill).setEnabled(false);
                                    hint = "\r\n"+getResources().getString(R.string.uhf_kill_info_pwd);
                                }else{
                                    findViewById(R.id.btn_kill).setEnabled(true);
                                    et_PassWord.setText(RESERVED);
                                }
                                tv_info.setText(String.format(getResources().getString(R.string.uhf_kill_info_readpwd), info, RESERVED, hint));
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
	                            String ErrorCodeStr = ErrorCodeInfo.getErrorInfo(UHFKillActivity.this, ErrorCode);
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
