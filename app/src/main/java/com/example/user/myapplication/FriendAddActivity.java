package com.example.user.myapplication;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;

public class FriendAddActivity extends Activity {
    Intent mIntent;
    TextView id;
    TextView name;
    Button add_Button;
    Button cancel_Button;
    JjakaotalkService mService;
    InputStream mInputStream;
    OutputStream mOutputStream;
    boolean mBound;
    String searchID;
    String searchName;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            JjakaotalkService.LocalBinder binder = (JjakaotalkService.LocalBinder) service;
            mService = binder.getService();
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
        setContentView(R.layout.activity_friendadd);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mBound) {
                    Intent service = new Intent(FriendAddActivity.this, JjakaotalkService.class);
                    bindService(service, mConnection, BIND_ABOVE_CLIENT);
                }
            }
        }).start();

        mIntent = getIntent();

        searchID = mIntent.getStringExtra("id");
        searchName = mIntent.getStringExtra("name");

        id = findViewById(R.id.addId);
        name = findViewById(R.id.addName);
        add_Button = findViewById(R.id.addButton);
        cancel_Button = findViewById(R.id.cancelButton);

        id.setText(id.getText() + searchID);
        name.setText(name.getText() + searchName);

        add_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < SubActivity.friend_list.size(); i++) {
                    if (searchID.equals(SubActivity.friend_list.get(i).id)) {
                        createDialog("이미 친구입니다.");
                        return;
                    }
                }

                try {
                    mOutputStream.write(("#,REQUEST,USERID," + MainActivity.userIdText.getText().toString() +
                            ",FRIEND,ADD," + mIntent.getStringExtra("id") + ",&\n").getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finish();
                FriendSearchActivity.friendSearchActivity.finish();
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
}
