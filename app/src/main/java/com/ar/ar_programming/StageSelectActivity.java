package com.ar.ar_programming;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StageSelectActivity extends AppCompatActivity {

    //---------------------------
    // 定数
    //---------------------------

    //---------------------------
    // フィールド変数
    //---------------------------

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stage_select);

        // ステージリスト
        setStageList();
    }

    /*
     * Play押下時処理
     */
    public void onSelectStageClicked(View view) {



    }

    /*
     * ステージリスト設定
     */
    private void setStageList() {

        //---------------------
        // ステージリスト情報
        //---------------------
        // ステージ名リストの取得
        ArrayList<String> stageNameList = GimmickManager.getStageNameList( this );

        // ステージリストにステージ名を設定
        ArrayList<StageSelectDialog.StageList> stageList = new ArrayList<>();
        for( String name: stageNameList ){
            stageList.add( new StageSelectDialog.StageList( name ) );
        }

        // ユーザーのクリア情報を設定
        Common.setUserStageClearInfo( this, stageList );

        //---------------------
        // アダプタ設定
        //---------------------
        // ステージ選択リストアダプタの生成
        StageSelectListAdapter adapter = new StageSelectListAdapter( stageList );

        // スクロール方向を用意
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager( this );
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        // アダプタ設定
        RecyclerView rv_selectStage = findViewById(R.id.rv_selectStage);
        rv_selectStage.setAdapter(adapter);
        rv_selectStage.setLayoutManager(linearLayoutManager);

    }


}