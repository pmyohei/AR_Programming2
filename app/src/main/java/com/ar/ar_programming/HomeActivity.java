package com.ar.ar_programming;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


/*
        //tmp
        Resources resources = getResources();
        XmlResourceParser parser = resources.getXml(R.xml.gimmick_tutorial);
        int eventType = 0;
        try {
            eventType = parser.getEventType();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        while (eventType != XmlPullParser.END_DOCUMENT) {

            if(eventType == XmlPullParser.START_DOCUMENT) {
                Log.i("ギミック", "START_DOCUMENT");
                Log.i("ギミック", "getAttributeCount()=" + parser.getAttributeCount());

            } else if(eventType == XmlPullParser.START_TAG) {
                Log.i("ギミック", "START_TAG");
                Log.i("ギミック", "TAG=" + parser.getName());
                Log.i("ギミック", "getAttributeCount()=" + parser.getAttributeCount());

            } else if(eventType == XmlPullParser.END_TAG) {
                Log.i("ギミック", "END_TAG");
                Log.i("ギミック", "getAttributeCount()=" + parser.getAttributeCount());

            } else if(eventType == XmlPullParser.TEXT) {
                Log.i("ギミック", "TEXT");
                Log.i("ギミック", "getAttributeCount()=" + parser.getAttributeCount());
            }

            // 次の要素を読み込む
            try {
                parser.next();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            try {
                eventType = parser.getEventType();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
        }

        Log.i("ギミック", "getAttributeCount()=" + parser.getAttributeCount());

        XmlResourceParser parser2 = resources.getXml(R.xml.gimmick_animal_easy);
        Log.i("ギミック", "gimmick_animal_easy getAttributeCount()=" + parser2.getAttributeCount());
*/

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