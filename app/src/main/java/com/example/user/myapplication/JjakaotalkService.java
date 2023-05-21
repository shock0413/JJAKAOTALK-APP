package com.example.user.myapplication;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.view.Display;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.transform.Result;

public class JjakaotalkService extends Service {
    private static Socket mSocket;
    private Handler mHandler;
    private static InputStream mInputStream;
    private static OutputStream mOutputStream;
    private boolean mRunning = true;
    private IBinder mBinder = new LocalBinder();
    public static ICallback mCallback;
    private DataBaseHelper dbHelper;
    public static SQLiteDatabase db;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    IntentFilter intentFilter = new IntentFilter();
    private boolean isScreenOn = true;
    private boolean isLocked = false;
    private PowerManager pm;
    private KeyguardManager km;
    private KeyguardManager.KeyguardLock kl;

    public interface ICallback {
        void remoteCall(String msg);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DataBaseHelper(JjakaotalkService.this);
        db = dbHelper.getWritableDatabase();
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
    }

    public JjakaotalkService() {
        mHandler = new Handler();
        mSocket = new Socket();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mSocket.setSoTimeout(60000);
                    mSocket.setTcpNoDelay(true);
                    mSocket.connect(new InetSocketAddress("172.30.1.29", 8888), 1000);

                    mInputStream = mSocket.getInputStream();
                    mOutputStream = mSocket.getOutputStream();

                    new Receiver().start();
                } catch (Exception e) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(JjakaotalkService.this, "접속할 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    stopSelf();
                }
            }
        }).start();
    }

    public void registerCallback(ICallback callback) {
        mCallback = callback;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mRunning = false;

        while (!Thread.currentThread().isInterrupted()) {
            Thread.currentThread().interrupt();
        }

        db.close();
        dbHelper.close();

        try {
            mSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static OutputStream getOutputStream() {
        return mOutputStream;
    }

    public static InputStream getInputStream() {
        return mInputStream;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class LocalBinder extends Binder {
        JjakaotalkService getService() {
            return JjakaotalkService.this;
        }
    }

    class Receiver extends Thread {
        byte[] buffer = new byte[1024];
        int bufferPosition = 0;

        @Override
        public void run() {
            super.run();

            while (mRunning) {
                try {
                    int byteAvailable = mInputStream.available();

                    if (byteAvailable > 0) {
                        byte[] packetBytes = new byte[byteAvailable];
                        mInputStream.read(packetBytes);

                        for (int i=0; i<byteAvailable; i++) {
                            byte b = packetBytes[i];

                            if (b == '\n') {
                                byte[] encodeBytes = new byte[bufferPosition];
                                System.arraycopy(buffer, 0, encodeBytes, 0, encodeBytes.length);
                                final String data = new String(encodeBytes, "EUC-KR");

                                if (data.equals("#,SYS,CLOSE,&")) {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(JjakaotalkService.this, "서버가 종료되었습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    String sql = "DELETE FROM friends";
                                    db.execSQL(sql);

                                    sql = "UPDATE sqlite_sequence SET SEQ = 1 WHERE NAME = 'friends'";  // AUTOINCREMENT를 1로 초기화
                                    db.execSQL(sql);

                                    sql = "DELETE FROM chat_logs";
                                    db.execSQL(sql);

                                    sql = "UPDATE sqlite_sequence SET SEQ = 1 WHERE NAME = 'chat_logs'";  // AUTOINCREMENT를 1로 초기화
                                    db.execSQL(sql);

                                    sql = "DELETE FROM chat_rooms";
                                    db.execSQL(sql);

                                    sql = "UPDATE sqlite_sequence SET SEQ = 1 WHERE NAME = 'chat_rooms'";  // AUTOINCREMENT를 1로 초기화
                                    db.execSQL(sql);

                                    stopSelf();
                                }

                                if (data.startsWith("#,CHAT,ID,")) {
                                    preferences = getSharedPreferences("auto", MODE_PRIVATE);
                                    editor = preferences.edit();

                                    if (preferences.getString("account_id", null) != null &&
                                        preferences.getString("account_pwd", null) != null) {

                                        final int chat_id = Integer.parseInt(data.substring(10, data.indexOf(",SENDER_ID,")));
                                        final int sender_id = Integer.parseInt(data.substring(data.indexOf(",SENDER_ID,") + 11, data.indexOf(",MEMBERS,")));
                                        final String members = data.substring(data.indexOf(",MEMBERS,") + 9, data.indexOf(",MSG,"));
                                        final String message = data.substring(data.indexOf(",MSG,") + 5, data.indexOf(",&"));

                                        String sql = "select * from chat_rooms;";
                                        Cursor c = JjakaotalkService.db.rawQuery(sql, null);

                                        if (c.getCount() == 0) {
                                            sql = "insert into chat_rooms(id, members) values(" + chat_id + ", '" + members + "');";
                                            JjakaotalkService.db.execSQL(sql);
                                        }

                                        sql = "insert into chat_logs(chat_id, user_id, message, type, create_at) values(" + chat_id + ", " +
                                                sender_id + ", '" + message + "', " + 0 + ", datetime('now', 'localtime'));";
                                        JjakaotalkService.db.execSQL(sql);

                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
                                                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                                                        .setLargeIcon(bitmap)
                                                        .setSmallIcon(R.drawable.logo)
                                                        .setContentTitle(sender_id + "")
                                                        .setContentText(message)
                                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                                        .setAutoCancel(true)
                                                        .setTicker(message);
                                                manager.notify((int)(System.currentTimeMillis() / 1000), builder.build());

                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH)
                                                    isScreenOn = pm.isInteractive();
                                                else
                                                    isScreenOn = pm.isScreenOn();

                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
                                                    isLocked = km.isDeviceLocked();
                                                else
                                                    isLocked = km.isKeyguardLocked();

                                                if (isScreenOn || !isLocked) {   // 켜져있을 때
                                                    Intent intent = new Intent(getApplicationContext(), MessagePopupActivity.class);
                                                    intent.putExtra("name", sender_id + "");
                                                    intent.putExtra("msg", message);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                                    PendingIntent pen = PendingIntent.getActivity(getApplicationContext(), 0,
                                                            intent, PendingIntent.FLAG_ONE_SHOT);
                                                    try {
                                                        pen.send();
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                } else if (!isScreenOn || isLocked) {    // 꺼져있을 때
                                                    Intent intent = new Intent(getApplicationContext(), MessagePopup2.class);
                                                    intent.putExtra("name", sender_id + "");
                                                    intent.putExtra("msg", message);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                    /*
                                                    PendingIntent pen = PendingIntent.getActivity(getApplicationContext(), 0,
                                                            intent, PendingIntent.FLAG_ONE_SHOT);
                                                    try {
                                                        pen.send();
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    */
                                                }
                                            }
                                        });

                                        c.close();
                                    }
                                }

                                mCallback.remoteCall(data);

                                bufferPosition = 0;
                            } else {
                                buffer[bufferPosition++] = b;
                            }
                        }
                    }

                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}