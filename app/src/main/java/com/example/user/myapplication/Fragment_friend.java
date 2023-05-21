package com.example.user.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class Fragment_friend extends Fragment {
    View view;
    ListView listView;
    public static ArrayAdapter<String> adapter;
    ArrayList<String> list = new ArrayList<String>();
    ImageButton addfreind_Button;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_friends, container, false);

        listView = view.findViewById(R.id.friendsList);

        addfreind_Button = view.findViewById(R.id.addfreindButton);

        addfreind_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(view.getContext(), FriendSearchActivity.class);
                startActivity(intent);
            }
        });

        list.add("나");
        list.add("새로운 친구 (" + SubActivity.newfriend_list.size() + ")");
        if (SubActivity.friend_list.size() == 0)
            list.add("친구없음");
        for (int i = 0; i < SubActivity.friend_list.size(); i++) {
            list.add("실명 : " + SubActivity.friend_list.get(i).name + ", 닉네임 : " + SubActivity.friend_list.get(i).nick_name);
        }

        adapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0 :
                        Toast.makeText(view.getContext(), position + "", Toast.LENGTH_SHORT).show();
                        break;
                    case 1 :
                        Intent intent = new Intent(view.getContext(), NewFriendActivity.class);
                        startActivity(intent);
                        break;
                    default:
                        createDialog(SubActivity.friend_list.get(position - 2));
                        break;
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        list.clear();
        list.add("나");
        list.add("새로운 친구 (" + SubActivity.newfriend_list.size() + ")");
        if (SubActivity.friend_list.size() == 0)
            list.add("친구없음");
        for (int i = 0; i < SubActivity.friend_list.size(); i++) {
            list.add("실명 : " + SubActivity.friend_list.get(i).name + ", 닉네임 : " + SubActivity.friend_list.get(i).nick_name);
        }
        adapter.notifyDataSetChanged();
    }

    public void createDialog(final User friend) {
        final Dialog dialog = new Dialog(view.getContext());
        dialog.setContentView(R.layout.friend_information);
        Display display = dialog.getWindow().getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = (int) (display.getHeight() * 0.3);
        dialog.getWindow().setLayout(width, height);
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        TextView name_text = dialog.findViewById(R.id.name);
        TextView id_text = dialog.findViewById(R.id.id);
        Button sendMessage_button = dialog.findViewById(R.id.sendMessage);

        name_text.setText(name_text.getText() + friend.name);
        id_text.setText(id_text.getText() + friend.account_id);

        sendMessage_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // SubActivity.chats_Button.callOnClick();  // 단순히 OnClickListener 이벤트 호출
                SubActivity.chats_Button.performClick();    // 터치 효과 및 터치음 발생까지 포함하여 OnClickListener 이벤트 호출
                dialog.dismiss();

                Intent intent = new Intent(view.getContext(), ChatActivity.class);
                intent.putExtra("friend_id", friend.id);
                intent.putExtra("chat_type", "fragment_friend");

                String sql = "select * from chat_rooms;";
                Cursor c = JjakaotalkService.db.rawQuery(sql, null);

                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        String members = c.getString(c.getColumnIndex("members"));
                        int chat_id = c.getInt(c.getColumnIndex("id"));

                        boolean first_case = members.equals("[" + SubActivity.user.id + "," + friend.id + "]");
                        boolean second_case = members.equals("[" + friend.id + "," + SubActivity.user.id + "]");

                        if (first_case | second_case) {
                            intent.putExtra("chat_id", chat_id);
                            intent.putExtra("members", members);
                        }
                    }
                }
                startActivity(intent);
            }
        });

        dialog.show();
    }
}