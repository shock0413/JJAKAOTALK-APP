package com.example.user.myapplication;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class NewFriendActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<String> arrayAdapter;
    JjakaotalkService mService;
    OutputStream mOutputStream;
    private boolean mBound;
    private ArrayList<String> temp_list = new ArrayList<String>();

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            JjakaotalkService.LocalBinder binder = (JjakaotalkService.LocalBinder) service;
            mService = binder.getService();
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
        setContentView(R.layout.activity_new_friend);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mBound) {
                    Intent service = new Intent(NewFriendActivity.this, JjakaotalkService.class);
                    bindService(service, mConnection, BIND_ABOVE_CLIENT);
                }
            }
        }).start();

        listView = findViewById(R.id.listView);

        for (int i = 0; i < SubActivity.newfriend_list.size(); i++)
            temp_list.add(SubActivity.newfriend_list.get(i).account_id + "(" + SubActivity.newfriend_list.get(i).name + ")");

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, temp_list);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                createDialog(SubActivity.newfriend_list.get(position).account_id, position);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    public void createDialog(final String data, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("알림")
                .setMessage("친구 추가하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            mOutputStream.write(("#,REQUEST,USERID," + MainActivity.userIdText.getText().toString() +
                                    ",FRIEND,ADD," + data + ",&\n").getBytes());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        SubActivity.newfriend_list.remove(position);
                        finish();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}