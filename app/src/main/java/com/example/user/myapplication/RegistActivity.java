package com.example.user.myapplication;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.service.autofill.FieldClassification;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.regex.Pattern;

public class RegistActivity extends AppCompatActivity {
    private EditText userId_Text;
    private EditText userPw_Text;
    private EditText userPwConfirm_Text;
    private EditText userName_Text;
    private EditText userPhone_Text;
    private EditText userEmail_Text;
    private EditText nickname_text;
    private Button submit_Button;
    private Button cancel_Button;
    private Button idcheck_Button;
    private Socket mSocket = null;
    private static InputStream mInputStream;
    private static OutputStream mOutputStream;
    private static boolean isOverlap = true;       // 아이디 중복 체크 true는 중복, false는 중복 아님
    private static boolean mRuuning = true;
    private final static int CASE_ID = 0;
    private final static int CASE_PHONE = 1;
    private final static int CASE_EMAIL = 2;
    private boolean mBound = false;
    private JjakaotalkService mService;
    private Handler mHandler;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            JjakaotalkService.LocalBinder binder = (JjakaotalkService.LocalBinder) service;
            mService = binder.getService();
            mInputStream = mService.getInputStream();
            mOutputStream = mService.getOutputStream();
            mService.registerCallback(mCallback);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);

        userId_Text = findViewById(R.id.userId);
        userPw_Text = findViewById(R.id.userPw);
        userPwConfirm_Text = findViewById(R.id.userPwConfirm);
        userName_Text = findViewById(R.id.userName);
        userPhone_Text = findViewById(R.id.userPhone);
        userEmail_Text = findViewById(R.id.userEmail);
        nickname_text = findViewById(R.id.nickname);
        submit_Button = findViewById(R.id.submit);
        cancel_Button = findViewById(R.id.cancel);
        idcheck_Button = findViewById(R.id.idcheck);

        mHandler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mBound) {
                    Intent service = new Intent(RegistActivity.this, JjakaotalkService.class);
                    bindService(service, mConnection, BIND_ABOVE_CLIENT);   // 실행 중인 Jjakaotalk서비스와 연결함
                }
            }
        }).start();

        userId_Text.addTextChangedListener(new TextWatcher() {  // 에디트텍스트의 내용이 바뀔 때마다 실행
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mRuuning)
                    mRuuning = true;
                if (!isOverlap)
                    isOverlap = true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        idcheck_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userId_Text.getText().toString().equals("")) {
                    createDialog("아이디를 입력해주세요.");
                    return;
                }

                else if (userId_Text.getText().toString().length() < 3 || userId_Text.getText().toString().length() > 20) {
                    createDialog("아이디를 3~20자로 입력해주세요.");
                    return;
                }

                else if (!patternCheck(CASE_ID, userId_Text.getText().toString())) {
                    createDialog("아이디는 영문+숫자로 이루어져야 하며 영문으로 시작되어야 합니다.");
                    return;
                }

                else {
                    try {
                        mOutputStream.write(("#,CHECK,ID," + userId_Text.getText().toString() + ",&\n").getBytes());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        submit_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userId_Text.getText().toString().equals("")) {
                    createDialog("아이디를 입력해주세요.");
                    return;
                }

                else if (userId_Text.getText().toString().length() < 3 || userId_Text.getText().toString().length() > 20) {
                    createDialog("아이디를 3~20자로 입력해주세요.");
                    return;
                }

                else if (!patternCheck(CASE_ID, userId_Text.getText().toString())) {
                    createDialog("아이디는 영문+숫자로 이루어져야 하며 영문으로 시작되어야 합니다.");
                    return;
                }

                else if (isOverlap) {
                    createDialog("아이디 중복 체크를 해주세요.");
                    return;
                }

                else if (userPw_Text.getText().toString().equals("")) {
                    createDialog("비밀번호를 입력해주세요.");
                    return;
                }

                else if (userPw_Text.getText().toString().length() < 6 || userPw_Text.getText().toString().length() > 20) {
                    createDialog("비밀번호를 6~20자로 입력해주세요.");
                    return;
                }

                else if (!userPw_Text.getText().toString().equals(userPwConfirm_Text.getText().toString())) {
                    createDialog("비밀번호를 재확인해주세요.");
                    return;
                }

                else if (userName_Text.getText().toString().equals("")) {
                    createDialog("이름을 입력해주세요.");
                    return;
                }

                else if (userPhone_Text.getText().toString().equals("")) {
                    createDialog("휴대폰 번호를 입력해주세요.");
                    return;
                }

                else if (!patternCheck(CASE_PHONE, userPhone_Text.getText().toString())) {
                    createDialog("휴대폰 번호를 정확히 입력해주세요.");
                    return;
                }

                else if (userEmail_Text.getText().toString().equals("")) {
                    createDialog("이메일 주소를 입력해주세요.");
                    return;
                }

                else if (!patternCheck(CASE_EMAIL, userEmail_Text.getText().toString())) {
                    createDialog("이메일 주소를 정확히 입력해주세요.");
                    return;
                }

                else if (nickname_text.getText().toString().length() <= 0) {
                    createDialog("닉네임을 입력해주세요.");
                    return;
                }

                else {
                    try {
                         mOutputStream.write(("#,REG,ID," + userId_Text.getText().toString() + ",&\n").getBytes());
                         mOutputStream.write(("#,REG,PW," + userPw_Text.getText().toString() + ",&\n").getBytes());
                         mOutputStream.write(("#,REG,NAME," + userName_Text.getText().toString() + ",&\n").getBytes());
                         mOutputStream.write(("#,REG,PHONE," + userPhone_Text.getText().toString() + ",&\n").getBytes());
                         mOutputStream.write(("#,REG,EMAIL," + userEmail_Text.getText().toString() + ",&\n").getBytes());
                        mOutputStream.write(("#,REG,NICKNAME," + nickname_text.getText().toString() + ",&\n").getBytes());
                         finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        cancel_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    public void createDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("알림")
                .setMessage(msg)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public boolean patternCheck(int number, String data) {
        String pattern;
        CharSequence sequence = data.subSequence(0, data.length());
        boolean result;
        switch (number) {
            case CASE_ID :
                pattern = "[a-zA-Z]{1}[a-zA-Z0-9]{2,19}";
                result = Pattern.matches(pattern, sequence);
                return result;
            case CASE_PHONE :
                pattern = "(010)\\d{3,4}\\d{4}";
                result = Pattern.matches(pattern, sequence);
                return result;
            case CASE_EMAIL :
                pattern = "\\w+@\\w+\\.\\w+(\\.\\w+)?";
                result = Pattern.matches(pattern, sequence);
                return result;
            default:
                return false;
        }
    }

    private JjakaotalkService.ICallback mCallback = new JjakaotalkService.ICallback() {
        @Override
        public void remoteCall(String msg) {
            if (msg.equals("#,CHECK,OK,&")) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        createDialog("중복된 아이디입니다.");
                    }
                });
                isOverlap = true;
            } else if (msg.equals("#,CHECK,NO,&")) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        createDialog("사용 가능한 아이디입니다.");
                    }
                });
                isOverlap = false;
            }
        }
    };
}
