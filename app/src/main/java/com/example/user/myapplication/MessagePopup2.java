package com.example.user.myapplication;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.security.Key;

public class MessagePopup2 extends Activity {
    TextView name_text;
    TextView msg_text;
    Button button1;
    Button button2;
    Intent mIntent;
    PowerManager pm;
    PowerManager.WakeLock wl;
    KeyguardManager km;
    KeyguardManager.KeyguardLock kl;
    IntentFilter intentFilter;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        public static final String screenOff = "android.intent.action.SCREEN_OFF";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(screenOff)) {   // 화면이 꺼지면 액티비티 종료
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_popup2);

        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(receiver, intentFilter);

        km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        kl = km.newKeyguardLock("keyguardlock");

        mIntent = getIntent();

        /*
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "worklock:hi");
        wl.acquire();
        */

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        name_text = (TextView) findViewById(R.id.name);
        msg_text = (TextView) findViewById(R.id.message);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);

        name_text.setText(mIntent.getStringExtra("name"));
        msg_text.setText(mIntent.getStringExtra("msg"));

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MessagePopup2.this, "확인", Toast.LENGTH_SHORT).show();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MessagePopup2.this, "취소", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
