package com.example.user.myapplication;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.*;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.*;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    public static EditText userIdText, userPwText;
    private Button loginButton;
    private TextView registLinkText;
    public static CheckBox autoLoginCheckBox;
    private OutputStream mOutputStream;
    private boolean isExist = false;
    private boolean mBound = false;
    private JjakaotalkService mService;
    private Handler mHandler;
    public Intent service;
    // public static Activity mainActivity;
    private Intent mIntent;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            JjakaotalkService.LocalBinder binder = (JjakaotalkService.LocalBinder) service;
            mService = binder.getService();
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
        setContentView(R.layout.activity_main);

        // mainActivity = MainActivity.this;

        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : am.getRunningServices(Integer.MAX_VALUE)) {
            if (JjakaotalkService.class.getName().equals(service.getClass().getName())) {   // 현재 Jjakotalk서비스가 실행 중인지 확인함
                isExist = true;
            }
        }

        service = new Intent(MainActivity.this, JjakaotalkService.class);

        if (!isExist)   // Jjakaotalk서비스가 실행 중이지 않다면
            startService(service);  // Jjakotalk서비스를 실행

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mBound) {
                    bindService(service, mConnection, BIND_ABOVE_CLIENT);   // 실행 중인 Jjakaotalk서비스와 연결함
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        mIntent = new Intent(MainActivity.this, SubActivity.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  // 액티비티 스택을 비워줌

        userIdText = findViewById(R.id.userId);
        userPwText = findViewById(R.id.userPw);
        loginButton = findViewById(R.id.loginButton);
        autoLoginCheckBox = findViewById(R.id.autoLogin);
        registLinkText = findViewById(R.id.registLink);

        mHandler = new Handler();

        preferences = getSharedPreferences("auto", MODE_PRIVATE);
        editor = preferences.edit();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userIdText.getText().toString().equals("")) {
                    createDialog("아이디를 입력해주세요.");
                    return;
                }

                else if (userIdText.getText().toString().length() < 3 || userIdText.getText().toString().length() > 20) {
                    createDialog("아이디를 3~20자로 입력해주세요.");
                    return;
                }

                else if (userPwText.getText().toString().equals("")) {
                    createDialog("비밀번호를 입력해주세요.");
                    return;
                }

                else if (userPwText.getText().toString().length() < 6 || userPwText.getText().toString().length() > 20) {
                    createDialog("비밀번호를 6~20자로 입력해주세요.");
                    return;
                }

                try {
                    mOutputStream.write(("#,LOG,ID,"+userIdText.getText().toString()+",&\n").getBytes());
                    mOutputStream.write(("#,LOG,PW,"+userPwText.getText().toString()+",&\n").getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Pattern pattern = Pattern.compile("아직 회원이 아니십니까\\?");

        Linkify.TransformFilter transform = new Linkify.TransformFilter() {
            @Override
            public String transformUrl(Matcher match, String url) {
                return "";
            }
        };

        Linkify.MatchFilter match = new Linkify.MatchFilter() {
            @Override
            public boolean acceptMatch(CharSequence s, int start, int end) {
                return true;
            }
        };

        Linkify.addLinks(registLinkText, pattern, "registActivity://", match, transform);

        if (preferences.getString("account_id", null) != null &&
                preferences.getString("account_pwd", null) != null) {
            userIdText.setText(preferences.getString("account_id", null));
            userPwText.setText(preferences.getString("account_pwd", null));
            autoLoginCheckBox.setChecked(preferences.getBoolean("check", false));

            mIntent.putExtra("id", preferences.getInt("id", 0));
            startActivity(mIntent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);

        while (!Thread.currentThread().isInterrupted()) {
            Thread.currentThread().interrupt();
        }
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

    private JjakaotalkService.ICallback mCallback = new JjakaotalkService.ICallback() {
        @Override
        public void remoteCall(final String msg) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (msg.startsWith("#,LOG,OK,ID,")) {
                        int id = Integer.parseInt(msg.substring(12, msg.indexOf(",NAME,")));
                        String name = msg.substring(msg.indexOf(",NAME,") + 6, msg.indexOf(",PHONENUMBER,"));
                        String phone_number = msg.substring(msg.indexOf(",PHONENUMBER,") + 13, msg.indexOf(",EMAIL,"));
                        String email = msg.substring(msg.indexOf(",EMAIL,") + 7, msg.indexOf(",NICKNAME,"));
                        String nick_name = msg.substring(msg.indexOf(",NICKNAME,") + 10, msg.length() - 2);

                        editor.putInt("id", id);
                        editor.putString("account_id", userIdText.getText().toString());
                        editor.putString("account_pwd", userPwText.getText().toString());
                        editor.putBoolean("check", autoLoginCheckBox.isChecked());
                        editor.commit();

                        mIntent.putExtra("id", id);
                        mIntent.putExtra("name", name);
                        mIntent.putExtra("phone_number", phone_number);
                        mIntent.putExtra("email", email);
                        mIntent.putExtra("nick_name", nick_name);
                        startActivity(mIntent);
                        finish();
                    } else if (msg.equals("#,LOG,NO,&")) {
                        createDialog("일치하는 정보가 없습니다.");
                    }
                }
            });
        }
    };

    @Override
    protected void onResume() { // 회원가입 액티비티에서 서비스의 mCallback 변수를 변동시키기 때문에 메인 액티비티가 갱신될 때마다 가져오기 위함
        super.onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (JjakaotalkService.mCallback != mCallback) {
                    if (mService != null)
                        mService.registerCallback(mCallback);
                }
            }
        }).start();
    }
}