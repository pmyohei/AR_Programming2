package com.ar.ar_programming;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Admob
        initAdmob();
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
        Intent intent = new Intent(this, HowToPlayActivity.class);
        startActivity(intent);
    }

    /*
     * 「設定」押下時処理
     */
    public void onSettingClicked(View view) {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    /*
     * GoogleMobileAdsSDKの初期化
     */
    private void initAdmob(){
        // AdMob初期化
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        //----------------------------
        // 子ども向け広告のリクエスト設定
        //----------------------------
        RequestConfiguration requestConfiguration =
                MobileAds.getRequestConfiguration()
                    .toBuilder()
                    .setTagForChildDirectedTreatment(
                        RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                    .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
                    .build();
        MobileAds.setRequestConfiguration(requestConfiguration);
        
        //----------------------------
        // load
        //----------------------------
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }


    //========================================================
//    public void onDebugTutorialUpClicked(View view) {
//        TextView tv_tutorialSet = findViewById(R.id.tv_tutorialSet);
//
//        String tutorial = tv_tutorialSet.getText().toString();
//        int t = Integer.parseInt( tutorial );
//
//        if( t < 7 ){
//            t++;
//        }
//
//        tv_tutorialSet.setText( Integer.toString( t ) );
//    }
//    public void onDebugTutorialDownClicked(View view) {
//        TextView tv_tutorialSet = findViewById(R.id.tv_tutorialSet);
//
//        String tutorial = tv_tutorialSet.getText().toString();
//        int t = Integer.parseInt( tutorial );
//
//        if( t > 1 ){
//            t--;
//        }
//
//        tv_tutorialSet.setText( Integer.toString( t ) );
//    }
//
//    public void onDebugTutorialSetClicked(View view) {
//        TextView tv_tutorialSet = findViewById(R.id.tv_tutorialSet);
//
//        String tutorial = tv_tutorialSet.getText().toString();
//        int t = Integer.parseInt( tutorial );
//
//        Common.setTutorialDebug(this, t);
//    }
    //========================================================


}