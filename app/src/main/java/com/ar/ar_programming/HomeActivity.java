package com.ar.ar_programming;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //debug-------------------
        Common.setTutorialDebug(this, 1);
        //--------------------
    }

    /*
     * Play押下時処理
     */
    public void onPlayClicked(View view) {
        Intent intent = new Intent(this, ARActivity.class);
        startActivity(intent);
    }

    /*
     * 「遊び方」押下時処理
     */
    public void onHowToPlayClicked(View view) {

    }

    /*
     * 「設定」押下時処理
     */
    public void onSettingClicked(View view) {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }
}