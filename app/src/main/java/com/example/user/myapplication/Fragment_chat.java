package com.example.user.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class Fragment_chat extends Fragment {
    View view;
    ListView listView;
    public static ArrayAdapter<String> adapter;
    ArrayList<String> list = new ArrayList<String>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_chats, container, false);
        listView = view.findViewById(R.id.chatsList);

        if (SubActivity.chat_room_list.size() == 0)
            list.add("채팅없음");

        for (int i = 0; i < SubActivity.chat_room_list.size(); i++) {
            list.add("채팅방 ID : " + SubActivity.chat_room_list.get(i).chat_id + ", 구성원 : " + SubActivity.chat_room_list.get(i).members);
        }

        adapter = new ArrayAdapter(view.getContext(), android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapter.getItem(position).equals("채팅없음"))
                    return;

                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra("chat_type", "fragment_chat");
                intent.putExtra("chat_id", SubActivity.chat_room_list.get(position).chat_id);
                intent.putExtra("members", SubActivity.chat_room_list.get(position).members);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        list.clear();
        if (SubActivity.chat_room_list.size() == 0)
            list.add("채팅없음");

        for (int i = 0; i < SubActivity.chat_room_list.size(); i++) {
            list.add("채팅방 ID : " + SubActivity.chat_room_list.get(i).chat_id + ", 구성원 : " + SubActivity.chat_room_list.get(i).members);
        }
        adapter.notifyDataSetChanged();
    }
}