package com.example.user.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class MessagePopupActivity extends Activity {
    TextView name_text;
    TextView msg_text;
    Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIntent = getIntent();

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);   // 뒷 배경 터치 가능하도록 하기
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_message_popup);

        Display display = getWindow().getWindowManager().getDefaultDisplay();
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.x = Gravity.CENTER_HORIZONTAL;
        params.y = (int) (Gravity.TOP * 1.2);
        int width = (int) (display.getWidth() * 0.7);
        int height = (int) (display.getHeight() * 0.15);
        getWindow().setLayout(width, height);
        getWindow().setAttributes(params);
        getWindow().setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        // getWindow().setDimAmount(10.0f);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setFinishOnTouchOutside(false); // 화면 밖 터치 시 팝업창 닫히지 않게 하기

        name_text = (TextView) findViewById(R.id.name);
        msg_text = (TextView) findViewById(R.id.message);

        name_text.setText(mIntent.getStringExtra("name"));
        msg_text.setText(mIntent.getStringExtra("msg"));

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /*
    @Override
    public boolean onTouchEvent(MotionEvent event) {    // 화면 밖 터치 시 팝업창 닫히지 않게 하기
        // return super.onTouchEvent(event);
        return false;
    }
    */
}
