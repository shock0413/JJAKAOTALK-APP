package com.example.user.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "JJAKAOTALK";
    static final int DATABASE_VERSION = 1;
    Context context;

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS friends (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id INTEGER NOT NULL," +    // 서버 내의 사용자 고유 번호
                "account_id TEXT NOT NULL," +
                "phone_number TEXT NOT NULL," +
                "name TEXT NOT NULL," +
                "nick_name TEXT NOT NULL," +
                "email TEXT NOT NULL" +
                ");";
        db.execSQL(sql);

        sql = "CREATE TABLE IF NOT EXISTS chat_logs (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "chat_id INTEGER NOT NULL," +   // 채팅방 아이디, chat_rooms(id)
                "user_id INTEGER NOT NULL," +   // 보낸 사람의 고유 번호
                "message TEXT NOT NULL," +
                "type INTEGER NOT NULL," +  // 0이면 친구가 보낸 메시지, 1이면 자신이 보낸 메시지
                "create_at DATETIME NOT NULL" + // 생성일자 + 시간
                ");";
        db.execSQL(sql);

        sql = "CREATE TABLE IF NOT EXISTS chat_rooms (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id INTEGER NOT NULL," +
                "members TEXT NOT NULL" +
                ");";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion == DATABASE_VERSION) {
            String sql = "DROP TABLE IF EXISTS friends;";
            db.execSQL(sql);
            sql = "DROP TABLE IF EXISTS chat_logs;";
            db.execSQL(sql);
            sql = "DROP TABLE IF EXISTS chat_rooms;";
            db.execSQL(sql);
            onCreate(db);
        }
    }
}