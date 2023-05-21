package com.example.user.myapplication;

public class ChatRoom {
    int chat_id;
    String members;

    public ChatRoom() {
        chat_id = 0;
        members = null;
    }

    public ChatRoom(int id, String members) {
        this.chat_id = id;
        this.members = members;
    }
}