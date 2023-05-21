package com.example.user.myapplication;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.renderscript.ScriptGroup;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class SubActivity extends AppCompatActivity {
    private ImageButton friends_Button;
    public static ImageButton chats_Button;
    FragmentManager fm;
    FragmentTransaction transaction;
    Fragment_friend fragment_friend;
    Fragment_chat fragment_chat;
    private final static int FRAG_FRIENDS = 1;
    private final static int FRAG_CHATS = 2;
    private boolean mBound = false;
    private JjakaotalkService mService;
    public static OutputStream mOutputStream;
    // MainActivity mainActivity = (MainActivity) MainActivity.mainActivity;
    private int selected = FRAG_FRIENDS;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    public static ArrayList<User> newfriend_list = new ArrayList<User>();
    public static ArrayList<User> friend_list = new ArrayList<User>();
    public static ArrayList<ChatRoom> chat_room_list = new ArrayList<ChatRoom>();
    Handler mHandler;
    Intent mIntent;
    public static User user;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            JjakaotalkService.LocalBinder binder = (JjakaotalkService.LocalBinder) service;
            mService = binder.getService();
            mOutputStream = mService.getOutputStream();
            mBound = true;

            Cursor c = JjakaotalkService.db.rawQuery("select * from friends", null);    // rawQuery() 함수는 select 즉, 검색을 할 때 사용한다. execSQL()은 create, drop, alter, update, delete를 수행할 때 사용한다.

            if (c.getCount() == 0) {
                try {
                    mOutputStream.write(("#,REQUEST,NEWFRIEND," + user.account_id + ",&\n").getBytes()); // 새로운 친구 목록
                    mOutputStream.write(("#,REQUEST,FRIENDS," + user.account_id + ",&\n").getBytes());  // 친구 목록 요청
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                while (c.moveToNext()) {
                    int friend_id = c.getInt(c.getColumnIndex("_id"));
                    String friend_account_id = c.getString(c.getColumnIndex("account_id"));
                    String friend_name = c.getString(c.getColumnIndex("name"));
                    String friend_phone_number = c.getString(c.getColumnIndex("phone_number"));
                    String friend_email = c.getString(c.getColumnIndex("email"));
                    String friend_nick_name = c.getString(c.getColumnIndex("nick_name"));
                    friend_list.add(new User(friend_id, friend_account_id, friend_name, friend_phone_number,
                            friend_email, friend_nick_name));
                }
            }

            c = JjakaotalkService.db.rawQuery("select * from chat_rooms", null);

            if (c.getCount() == 0) {
                try {
                    mOutputStream.write(("#,REQUEST,CHATROOMS," + user.id + ",&\n").getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                while (c.moveToNext())
                    chat_room_list.add(new ChatRoom(c.getInt(c.getColumnIndex("_id")), c.getString(c.getColumnIndex("members"))));
            }

            c.close();
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
        setContentView(R.layout.activity_sub);

        // MainActivity.mainActivity.finish();

        mIntent = getIntent();

        user = new User(mIntent.getIntExtra("id", 0), MainActivity.userIdText.getText().toString(),
                mIntent.getStringExtra("name"), mIntent.getStringExtra("phone_number"),
                mIntent.getStringExtra("email"), mIntent.getStringExtra("nick_name"));  // 유저 정보

        mHandler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mBound) {
                    Intent service = new Intent(SubActivity.this, JjakaotalkService.class);
                    bindService(service, mConnection, BIND_ABOVE_CLIENT);
                }
            }
        }).start();

        friends_Button = findViewById(R.id.friendButton);
        chats_Button = findViewById(R.id.chatsButton);

        fragment_friend = new Fragment_friend();
        fragment_chat = new Fragment_chat();

        friends_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBound) {
                    Toast.makeText(SubActivity.this, "종료되어있음", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (selected != FRAG_FRIENDS) {
                    setFragment(FRAG_FRIENDS);
                    friends_Button.setImageResource(R.drawable.friends2);
                    chats_Button.setImageResource(R.drawable.chat);
                    selected = FRAG_FRIENDS;
                }
            }
        });

        chats_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selected != FRAG_CHATS) {
                    setFragment(FRAG_CHATS);
                    chats_Button.setImageResource(R.drawable.chat2);
                    friends_Button.setImageResource(R.drawable.friends);
                    selected = FRAG_CHATS;
                }
            }
        });

        preferences = getSharedPreferences("auto", MODE_PRIVATE);
        editor = preferences.edit();

        if (preferences.getInt("selection", 1) > 0)
            selected = preferences.getInt("selection", 1);

        if (selected == FRAG_FRIENDS) {
            setFragment(FRAG_FRIENDS);
            friends_Button.setImageResource(R.drawable.friends2);
        }

        else if (selected == FRAG_CHATS) {
            setFragment(FRAG_CHATS);
            chats_Button.setImageResource(R.drawable.chat2);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        while (!Thread.currentThread().isInterrupted())
            Thread.currentThread().interrupt();

        unbindService(mConnection);
        editor.putInt("selection", selected);
        editor.commit();

        if (!preferences.getBoolean("check", false)) {
            editor.clear();
            editor.commit();
        }

        if (!MainActivity.autoLoginCheckBox.isChecked() && JjakaotalkService.db.isOpen())
        {
            String sql = "DELETE FROM friends";
            JjakaotalkService.db.execSQL(sql);

            sql = "UPDATE sqlite_sequence SET SEQ = 1 WHERE NAME = 'friends'";  // AUTOINCREMENT를 1로 초기화
            JjakaotalkService.db.execSQL(sql);

            sql = "DELETE FROM chat_logs";
            JjakaotalkService.db.execSQL(sql);

            sql = "UPDATE sqlite_sequence SET SEQ = 1 WHERE NAME = 'chat_logs'";  // AUTOINCREMENT를 1로 초기화
            JjakaotalkService.db.execSQL(sql);

            sql = "DELETE FROM chat_rooms";
            JjakaotalkService.db.execSQL(sql);

            sql = "UPDATE sqlite_sequence SET SEQ = 1 WHERE NAME = 'chat_rooms'";  // AUTOINCREMENT를 1로 초기화
            JjakaotalkService.db.execSQL(sql);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        friend_list.clear();
        newfriend_list.clear();
        chat_room_list.clear();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (JjakaotalkService.mCallback != mCallback) {
                    if (mService != null)
                        mService.registerCallback(mCallback);
                }
            }
        }).start();

        if (mBound) {
            Cursor c = JjakaotalkService.db.rawQuery("select * from friends", null);

            if (c.getCount() == 0) {
                try {
                    mOutputStream.write(("#,REQUEST,NEWFRIEND," + user.account_id + ",&\n").getBytes()); // 새로운 친구 목록
                    mOutputStream.write(("#,REQUEST,FRIENDS," + user.account_id + ",&\n").getBytes());  // 친구 목록 요청
                    mOutputStream.write(("#,REQUEST,CHATROOMS," + user.account_id + ",&\n").getBytes());  // 채팅방 목록 요청
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                while (c.moveToNext()) {
                    int friend_id = c.getInt(c.getColumnIndex("id"));
                    String friend_account_id = c.getString(c.getColumnIndex("account_id"));
                    String friend_name = c.getString(c.getColumnIndex("name"));
                    String friend_phone_number = c.getString(c.getColumnIndex("phone_number"));
                    String friend_email = c.getString(c.getColumnIndex("email"));
                    String friend_nick_name = c.getString(c.getColumnIndex("nick_name"));
                    friend_list.add(new User(friend_id, friend_account_id, friend_name, friend_phone_number, friend_email, friend_nick_name));
                }
            }

            c.close();

            c = JjakaotalkService.db.rawQuery("select * from chat_rooms", null);

            while (c.moveToNext()) {
                chat_room_list.add(new ChatRoom(c.getInt(c.getColumnIndex("id")), c.getString(c.getColumnIndex("members"))));
            }

            if (selected == FRAG_CHATS) {
                Toast.makeText(this, "새로고침", Toast.LENGTH_SHORT).show();
                Fragment_chat.adapter.setNotifyOnChange(true);
                Fragment_chat.adapter.notifyDataSetChanged();
            } else if (selected == FRAG_FRIENDS) {
                Toast.makeText(this, "새로고침", Toast.LENGTH_SHORT).show();
                setFragment(FRAG_FRIENDS);
            }

            c.close();
        }
    }

    public void setFragment(int flag) {
        fm = getSupportFragmentManager();
        transaction = fm.beginTransaction();
        switch (flag) {
            case FRAG_FRIENDS :
                transaction.replace(R.id.main_frame, fragment_friend);
                transaction.commitAllowingStateLoss();  // 메세지 팝업창을 지우고 난 후 Can not perform this action after onSaveInstanceState. 에러 발생 방지
                break;
            case FRAG_CHATS :
                transaction.replace(R.id.main_frame, fragment_chat);
                transaction.commitAllowingStateLoss();
                break;
        }
    }

    private JjakaotalkService.ICallback mCallback = new JjakaotalkService.ICallback() {
        @Override
        public void remoteCall(final String msg) {
            if (msg.startsWith("#,NEWFRIEND,ID,")) {    // 새로운 친구 정보 받기
                int newfriend_id = Integer.parseInt(msg.substring(15, msg.indexOf(",ACCOUNT_ID,")));
                String newfriend_account_id = msg.substring(msg.indexOf(",ACCOUNT_ID,") + 12, msg.indexOf(",NAME,"));
                String newfriend_name = msg.substring(msg.indexOf(",NAME,") + 6, msg.indexOf(",PHONENUMBER,"));
                String newfriend_phone_number = msg.substring(msg.indexOf(",PHONENUMBER,") + 12, msg.indexOf(",EMAIL,"));
                String newfriend_email = msg.substring(msg.indexOf(",EMAIL,") + 7, msg.indexOf(",NICKNAME,"));
                String newfriend_nick_name = msg.substring(msg.indexOf(",NICKNAME,") + 10, msg.length() - 2);
                newfriend_list.add(new User(newfriend_id, newfriend_account_id, newfriend_name, newfriend_phone_number,
                        newfriend_email, newfriend_nick_name));

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (selected == FRAG_FRIENDS)
                            fragment_friend.onResume();
                    }
                });
            } else if (msg.equals("#,FRIENDS,NO,&")) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SubActivity.this, "친구가 없습니다.", Toast.LENGTH_SHORT).show();
                        if (selected == FRAG_FRIENDS)
                            fragment_friend.onResume();
                    }
                });
            } else if (msg.startsWith("#,FRIENDS,ID,")) {
                final int friend_id = Integer.parseInt(msg.substring(13, msg.indexOf(",ACCOUNT_ID,")));
                final String friend_account_id = msg.substring(msg.indexOf(",ACCOUNT_ID,") + 12, msg.indexOf(",PHONE_NUMBER,"));
                final String friend_phone_number = msg.substring(msg.indexOf(",PHONE_NUMBER,") + 14, msg.indexOf(",NAME,"));
                final String friend_name = msg.substring(msg.indexOf(",NAME,") + 6, msg.indexOf(",EMAIL,"));
                final String friend_email = msg.substring(msg.indexOf(",EMAIL,") + 7, msg.indexOf(",NICK_NAME,"));
                final String friend_nick_name = msg.substring(msg.indexOf(",NICK_NAME,") + 11, msg.length() - 2);

                friend_list.add(new User(friend_id, friend_account_id, friend_name, friend_phone_number, friend_email, friend_nick_name));

                String sql = "insert into friends(id, account_id, phone_number, name, nick_name, email)" +
                        "values(" + friend_id + ", '" + friend_account_id + "','" + friend_phone_number + "','" +
                        friend_name + "','" + friend_nick_name + "','" + friend_email + "')";
                JjakaotalkService.db.execSQL(sql);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (selected == FRAG_FRIENDS)
                            fragment_friend.onResume();
                    }
                });
            } else if (msg.startsWith("#,CHATROOMS,ID,")) {
                int chat_id = Integer.parseInt(msg.substring(15, msg.indexOf(",MEMBERS,")));
                String members = msg.substring(msg.indexOf(",MEMBERS,") + 9, msg.indexOf(",&"));
                String sql = "insert into chat_rooms(id, members) values(" + chat_id + ", '" + members + "');";
                JjakaotalkService.db.execSQL(sql);
                chat_room_list.add(new ChatRoom(chat_id, members));

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (selected == FRAG_CHATS)
                            fragment_chat.onResume();
                    }
                });
            }else if (msg.startsWith("#,CHAT,ID,")) {
                chat_room_list.clear();
                String sql = "select * from chat_rooms;";
                Cursor c = JjakaotalkService.db.rawQuery(sql, null);
                while (c.moveToNext()) {
                    chat_room_list.add(new ChatRoom(c.getInt(c.getColumnIndex("id")), c.getString(c.getColumnIndex("members"))));
                }

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (selected == FRAG_CHATS)
                            fragment_chat.onResume();
                    }
                });
            }
        }
    };
}