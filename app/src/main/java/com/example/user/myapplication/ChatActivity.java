package com.example.user.myapplication;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    JjakaotalkService mService;
    private OutputStream mOutputStream;
    boolean mBound;
    private ListView listView;
    private CustomAdapter mAdapter;
    private EditText message_Text;
    private Button send_button;
    private Intent mIntent;
    private ArrayList<User> members = new ArrayList<User>();
    private Handler mHandler;
    private boolean isCreated = false;
    private ChatRoom chatRoom = new ChatRoom();

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
        setContentView(R.layout.activity_chat);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mBound) {
                    Intent service = new Intent(ChatActivity.this, JjakaotalkService.class);
                    bindService(service, mConnection, BIND_ABOVE_CLIENT);
                }
            }
        }).start();

        mIntent = getIntent();
        mHandler = new Handler();
        mAdapter = new CustomAdapter();

        String chat_type = mIntent.getStringExtra("chat_type");

        if (chat_type.equals("fragment_friend")) {   // 친구 목록 프래그먼트에서 넘어왔을 경우
            chatRoom.chat_id = mIntent.getIntExtra("chat_id", 0);
            chatRoom.members = mIntent.getStringExtra("members");

            if (chatRoom.chat_id > 0) {
                String sql = "select * from chat_logs where chat_id = " + chatRoom.chat_id + ";";
                Cursor c = JjakaotalkService.db.rawQuery(sql, null);

                while (c.moveToNext()) {
                    String message = c.getString(c.getColumnIndex("message"));
                    int type = c.getInt(c.getColumnIndex("type"));
                    mAdapter.add(message, type);
                }

                c.close();

                isCreated = true;

                chatRoom.members = chatRoom.members.substring(chatRoom.members.indexOf("[") + 1,
                        chatRoom.members.indexOf("]"));
                String[] members_split = chatRoom.members.split(",");

                for (int i = 0; i < members_split.length; i++) {
                    if (SubActivity.user.id == Integer.parseInt(members_split[i]))
                        continue;

                    String _sql = "select * from friends where id = " + members_split[i] + ";";
                    Cursor _c = JjakaotalkService.db.rawQuery(_sql, null);

                    if (_c.getCount() > 0) {
                        while (_c.moveToNext()) {
                            int member_id = _c.getInt(_c.getColumnIndex("id"));
                            String member_account_id = _c.getString(_c.getColumnIndex("account_id"));
                            String member_name = _c.getString(_c.getColumnIndex("name"));
                            String member_phone_number = _c.getString(_c.getColumnIndex("phone_number"));
                            String member_email = _c.getString(_c.getColumnIndex("email"));
                            String member_nick_name = _c.getString(_c.getColumnIndex("nick_name"));
                            members.add(new User(member_id, member_account_id, member_name, member_phone_number, member_email, member_nick_name));
                        }
                    }

                    _c.close();
                }
                /*
                String _sql = "select * from chat_logs where chat_id = " + chatRoom.chat_id;
                Cursor _c = JjakaotalkService.db.rawQuery(_sql, null);
                Log.e("count", _c.getCount() + "");
                while (_c.moveToNext()) {
                    String message = _c.getString(c.getColumnIndex("message"));
                    int type = _c.getInt(_c.getColumnIndex("type"));
                    Log.e("message", message);
                    mAdapter.add(message, type);
                }

                mAdapter.notifyDataSetChanged();

                _c.close();
                */
            } else {
                int friend_id = mIntent.getIntExtra("friend_id", 0);
                String sql = "select * from friends where id = " + friend_id;
                Cursor c = JjakaotalkService.db.rawQuery(sql, null);

                while (c.moveToNext()) {
                    String friend_account_id = c.getString(c.getColumnIndex("account_id"));
                    String friend_name = c.getString(c.getColumnIndex("name"));
                    String friend_phone_number = c.getString(c.getColumnIndex("phone_number"));
                    String friend_email = c.getString(c.getColumnIndex("email"));
                    String friend_nick_name = c.getString(c.getColumnIndex("nick_name"));
                    members.add(new User(friend_id, friend_account_id, friend_name, friend_phone_number, friend_email, friend_nick_name));
                }

                c.close();
            }
        } else if (chat_type.equals("fragment_chat")) {  // 채팅방 목록 프래그먼트에서 넘어왔을 경우
            isCreated = true;
            chatRoom.chat_id = mIntent.getIntExtra("chat_id", 0);
            chatRoom.members = mIntent.getStringExtra("members");
            chatRoom.members = chatRoom.members.substring(chatRoom.members.indexOf("[") + 1,
                    chatRoom.members.indexOf("]"));
            String[] members_split = chatRoom.members.split(",");

            for (int i = 0; i < members_split.length; i++) {
                String sql = "select * from friends where id = " + members_split[i];
                Cursor c = JjakaotalkService.db.rawQuery(sql, null);
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        int member_id = c.getInt(c.getColumnIndex("id"));
                        String member_account_id = c.getString(c.getColumnIndex("account_id"));
                        String member_name = c.getString(c.getColumnIndex("name"));
                        String member_phone_number = c.getString(c.getColumnIndex("phone_number"));
                        String member_email = c.getString(c.getColumnIndex("email"));
                        String member_nick_name = c.getString(c.getColumnIndex("nick_name"));
                        members.add(new User(member_id, member_account_id, member_name, member_phone_number, member_email, member_nick_name));
                    }
                }

                c.close();
            }

            String sql = "select * from chat_logs where chat_id = " + chatRoom.chat_id;
            Cursor c = JjakaotalkService.db.rawQuery(sql, null);
            Log.e("count", c.getCount() + "");
            while (c.moveToNext()) {
                String message = c.getString(c.getColumnIndex("message"));
                int type = c.getInt(c.getColumnIndex("type"));
                Log.e("message", message);
                mAdapter.add(message, type);
            }

            mAdapter.notifyDataSetChanged();

            c.close();
        }

        listView = findViewById(R.id.listView);
        send_button = findViewById(R.id.send);
        message_Text = findViewById(R.id.message);
        send_button.setEnabled(false);

        listView.setAdapter(mAdapter);

        message_Text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals(""))
                    send_button.setEnabled(false);
                else
                    send_button.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isCreated) {
                    String members_str = "[" + SubActivity.user.id + ",";

                    for (int i = 0; i < members.size(); i++) {
                        if (i == (members.size() - 1)) {
                            members_str += members.get(i).id + "]";
                            break;
                        }
                        members_str += members.get(i).id + ",";
                    }

                    try {
                        mOutputStream.write(("#,NEW,CHATROOM,MEMBERS," + members_str + ",&\n").getBytes());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    String members_str = "[" + SubActivity.user.id + ",";

                    for (int i = 0; i < members.size(); i++) {
                        if (i == (members.size() - 1)) {
                            members_str += members.get(i).id + "]";
                            break;
                        }
                        members_str += members.get(i).id + ",";
                    }

                    String sql = "insert into chat_logs(chat_id, user_id, message, type, create_at) values(" + chatRoom.chat_id + ", " + SubActivity.user.id +
                            ",'" + message_Text.getText() + "', " + 1 + ", datetime('now', 'localtime'));";
                    JjakaotalkService.db.execSQL(sql);
                    
                    try {
                        mOutputStream.write(("#,CHAT,ID," + chatRoom.chat_id + ",SENDER_ID," + SubActivity.user.id + ",MEMBERS," + members_str + ",MSG," +
                                message_Text.getText() + ",&\n").getBytes());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.add(message_Text.getText().toString(), 1);
                            mAdapter.notifyDataSetChanged();
                            message_Text.setText("");
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    class CustomAdapter extends BaseAdapter {
        public class ListContent {
            String msg;
            int type;

            ListContent(String _msg, int _type) {
                this.msg = _msg;
                this.type = _type;
            }
        }

        private ArrayList<ListContent> mList;
        public CustomAdapter() {
            mList = new ArrayList();
        }

        public void add(String _msg, int _type) {
            mList.add(new ListContent(_msg, _type));
        }

        public void remove(int _position) {
            mList.remove(_position);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int pos = position;
            final Context context = parent.getContext();

            TextView text = null;
            ViewHolder holder = null;
            LinearLayout layout = null;
            View viewRight = null;
            View viewLeft = null;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.chatitem, parent, false);

                layout = convertView.findViewById(R.id.layout);
                text = convertView.findViewById(R.id.text);
                viewRight = convertView.findViewById(R.id.imageViewRight);
                viewLeft = convertView.findViewById(R.id.imageViewLeft);

                holder = new ViewHolder();
                holder.m_TextView = text;
                holder.layout = layout;
                holder.viewRight = viewRight;
                holder.viewLeft = viewLeft;
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
                text = holder.m_TextView;
                layout = holder.layout;
                viewRight = holder.viewRight;
                viewLeft = holder.viewLeft;
            }

            text.setText(mList.get(position).msg);

            if(mList.get(position).type == 0) {
                text.setBackgroundResource(R.drawable.inbox2);
                layout.setGravity(Gravity.LEFT);
                viewRight.setVisibility(View.GONE);
                viewLeft.setVisibility(View.GONE);
            } else if (mList.get(position).type == 1) {
                text.setBackgroundResource(R.drawable.outbox2);
                layout.setGravity(Gravity.RIGHT);
                viewRight.setVisibility(View.GONE);
                viewLeft.setVisibility(View.GONE);
            } else if (mList.get(position).type == 2) {
                text.setBackgroundResource(R.drawable.datebg);
                layout.setGravity(Gravity.CENTER);
                viewRight.setVisibility(View.VISIBLE);
                viewLeft.setVisibility(View.VISIBLE);
            }

            return convertView;
        }

        private class ViewHolder {
            TextView m_TextView;
            LinearLayout layout;
            View viewRight;
            View viewLeft;
        }
    }

    private JjakaotalkService.ICallback mCallback = new JjakaotalkService.ICallback() {
        @Override
        public void remoteCall(String msg) {
            if (msg.startsWith("#,NEW,CHATROOM,ID,")) {
                chatRoom.chat_id = Integer.parseInt(msg.substring(18, msg.length() - 2));
                String members_str = "[" + SubActivity.user.id + ",";

                for (int i = 0; i < members.size(); i++) {
                    if (i == (members.size() - 1)) {
                        members_str += members.get(i).id + "]";
                        continue;
                    }
                    members_str += members.get(i).id + ",";
                }

                String sql = "insert into chat_rooms(id, members) values(" + chatRoom.chat_id + ",'" + members_str + "');";
                JjakaotalkService.db.execSQL(sql);

                sql = "insert into chat_logs(chat_id, user_id, message, type, create_at) values(" + chatRoom.chat_id + ", " + SubActivity.user.id +
                        ",'" + message_Text.getText() + "', " + 1 + ", datetime('now', 'localtime'));";
                JjakaotalkService.db.execSQL(sql);

                members_str = "[" + SubActivity.user.id + ",";
                for (int i = 0; i < members.size(); i++) {
                    if (i == (members.size() - 1)) {
                        members_str += members.get(i).id + "]";
                        break;
                    }
                    members_str += members.get(i).id + ",";
                }

                try {
                    mOutputStream.write(("#,CHAT,ID," + chatRoom.chat_id + ",SENDER_ID," + SubActivity.user.id + ",MEMBERS," + members_str + ",MSG," +
                            message_Text.getText() + ",&\n").getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.add(message_Text.getText().toString(), 1);
                        mAdapter.notifyDataSetChanged();
                        message_Text.setText("");
                    }
                });

                isCreated = true;
            } else if (msg.startsWith("#,CHAT,ID,")) {
                final int _chat_id = Integer.parseInt(msg.substring(10, msg.indexOf(",SENDER_ID,")));
                final int sender_id = Integer.parseInt(msg.substring(msg.indexOf(",SENDER_ID,") + 11, msg.indexOf(",MEMBERS,")));
                final String members = msg.substring(msg.indexOf(",MEMBERS,") + 9, msg.indexOf(",MSG,"));
                final String message = msg.substring(msg.indexOf(",MSG,") + 5, msg.indexOf(",&"));

                if (sender_id != SubActivity.user.id) {
                    /*
                    String sql = "select * from chat_rooms;";
                    Cursor c = JjakaotalkService.db.rawQuery(sql, null);

                    if (c.getCount() == 0) {
                        sql = "insert into chat_rooms(id, members) values(" + _chat_id + ", '" + members + "');";
                        JjakaotalkService.db.execSQL(sql);
                    }

                    c.close();
                    */

                    if (chatRoom.chat_id == _chat_id) {
                        /*
                        sql = "insert into chat_logs(chat_id, user_id, message, type, create_at) values(" + chat_id + ", " + SubActivity.user.id +
                                ",'" + message + "', " + 0 + ", datetime('now', 'localtime'));";
                        JjakaotalkService.db.execSQL(sql);
                        */
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.add(message, 0);
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            }
        }
    };
}
