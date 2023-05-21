package com.example.user.myapplication;

public class User {
    int id;
    String account_id;
    String name;
    String phone_number;
    String email;
    String nick_name;

    public User(int id, String account_id, String name, String phone_number, String email, String nick_name) {
        this.id = id;
        this.account_id = account_id;
        this.name = name;
        this.phone_number = phone_number;
        this.email = email;
        this.nick_name = nick_name;
    }
}
