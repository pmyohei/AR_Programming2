package com.ar.ar_programming;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StageSelectActivity extends AppCompatActivity {

    //---------------------------
    // 定数
    //---------------------------
    // 画面遷移Key
    public static final String KEY_SELECT_STAGE = "select_stage";
    public static final int RESULT_SELECT_STAGE = 200;


    //---------------------------
    // フィールド変数
    //---------------------------
    ArrayList<StageList> mStageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stage_select);

        // ステージリスト設定
        setStageList();
    }

    /*
     * Play押下時処理
     */
    public void onSelectStageClicked(View view) {
        // ステージ選択終了
        setFinishIntent();
    }

    /*
     * ステージリスト設定
     */
    private void setStageList() {

        // ステージリスト取得
        mStageList = getStageList();
        // 選択中ステージ名
        String currentStageName = getCurrentStageName();

        //---------------------
        // アダプタ設定
        //---------------------
        // ステージ選択リストアダプタの生成
        StageSelectAdapter adapter = new StageSelectAdapter(mStageList, currentStageName);

        // スクロール方向を用意
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        // アダプタ設定
        RecyclerView rv_selectStage = findViewById(R.id.rv_selectStage);
        rv_selectStage.setAdapter(adapter);
        rv_selectStage.setLayoutManager(linearLayoutManager);
    }

    /*
     * ステージリスト設定
     */
    private ArrayList<StageList> getStageList() {

        ArrayList<StageList> stageList = new ArrayList<>();

        //---------------------
        // ステージリスト情報
        //---------------------
        // ステージ名リストの取得
        ArrayList<String> stageNameList = GimmickManager.getStageNameList(this);

        // ステージリストにステージ名を設定
        for (String name : stageNameList) {
            stageList.add(new StageList(name));
        }

        // ユーザーのクリア情報を設定
        Common.setUserStageClearInfo(this, stageList);

        return stageList;
    }

    /*
     * 現在選択中のステージ名を取得
     */
    private String getCurrentStageName() {
        // 画面遷移元の選択中ステージ名を返す
        Intent intent = getIntent();
        return intent.getStringExtra( ARFragment.KEY_CURRENT_STAGE );
    }

    /*
     * 画面終了のindentデータを設定
     */
    private void setFinishIntent() {

        // 選択されたステージ名を取得
        String stageName = getUserSelectStage();

        // resultコード設定
        Intent intent = getIntent();
        intent.putExtra(KEY_SELECT_STAGE, stageName);
        setResult(RESULT_SELECT_STAGE, intent);
    }


    /*
     * ユーザーの選択したステージを取得
     */
    private String getUserSelectStage() {

        for( StageList stage: mStageList ){
            if( stage.mIsSelect ){
                return stage.mStageName;
            }
        }

        return "";
    }
}