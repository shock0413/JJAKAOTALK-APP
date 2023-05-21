package com.example.user.myapplication;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;

public class FriendSearchActivity extends AppCompatActivity {
    JjakaotalkService mService;
    InputStream mInputStream;
    OutputStream mOutputStream;
    boolean mBound = false;
    Handler mHandler;
    EditText Id_Text;
    Button submit_Button;
    static FriendSearchActivity friendSearchActivity;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            JjakaotalkService.LocalBinder binder = (JjakaotalkService.LocalBinder) service;
            mService = binder.getService();
            mService.registerCallback(mCallback);
            mInputStream = mService.getInputStream();
            mOutputStream = mService.getOutputStream();
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
        setContentView(R.layout.activity_friendsearch);

        friendSearchActivity = FriendSearchActivity.this;

        mHandler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mBound) {
                    Intent service = new Intent(FriendSearchActivity.this, JjakaotalkService.class);
                    bindService(service, mConnection, BIND_ABOVE_CLIENT);
                }
            }
        }).start();

        Id_Text = findViewById(R.id.inputId);
        submit_Button = findViewById(R.id.submit);

        submit_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Id_Text.getText().toString().equals(MainActivity.userIdText.getText().toString())) {
                    createDialog("자신의 아이디를 검색할 수 없습니다.");
                    return;
                }

                try {
                    mOutputStream.write(("#,SEARCH,ID," + Id_Text.getText().toString() + ",&\n").getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    @Override
    protected void onResume() {
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
        public void remoteCall(String msg) {
            if (msg.equals("#,SEARCH,NO,&")) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        createDialog("존재하는 회원이 없습니다.");
                    }
                });
            } else if (msg.startsWith("#,SEARCH,NAME,")) {
                final String userName = msg.substring(14, msg.length() - 2);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FriendSearchActivity.this, userName+"", Toast.LENGTH_SHORT).show();
                    }
                });
                Intent intent = new Intent(FriendSearchActivity.this, FriendAddActivity.class);
                intent.putExtra("id", Id_Text.getText().toString());
                intent.putExtra("name", userName);
                startActivity(intent);
            }
        }
    };
}
