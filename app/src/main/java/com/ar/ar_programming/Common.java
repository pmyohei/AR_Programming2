package com.ar.ar_programming;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

import kotlin.UNINITIALIZED_VALUE;

/*
 * 共通処理
 */
public class Common {

    public static int TUTORIAL_DEFAULT = 1;     // ユーザーデータ取得エラー時のチュートリアル値（データがないなら、初めのチュートリアルから行う）
    public static int TUTORIAL_FINISH = 7;      // チュートリアル終了値（チュートリアルは『１～「この値 - 1」』）

    /*
     * チュートリアルシーケンスの取得
     */
    public static int getTutorialSequence(Context context) {

        // 現在のチュートリアル進行状況を取得
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), MODE_PRIVATE);
        int tutorial = sharedPref.getInt(context.getString(R.string.saved_tutorial_key), TUTORIAL_DEFAULT);

        return tutorial;
    }

    /*
     * チュートリアル終了済み判定
     */
    public static boolean isFisishTutorial(Context context) {

        // 現在のチュートリアル進行状況を取得
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), MODE_PRIVATE);
        int tutorial = sharedPref.getInt(context.getString(R.string.saved_tutorial_key), TUTORIAL_DEFAULT);

        // チュートリアル終了しているかどうか
        return (tutorial >= TUTORIAL_FINISH);
    }


    /*
     * チュートリアルを次に進める
     */
    public static void proceedNextTutorial(Context context) {

        // チュートリアルを次に進める
        int tutorial = getTutorialSequence(context);
        tutorial++;

        // 保存
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(context.getString(R.string.saved_tutorial_key), tutorial);
        editor.apply();
    }


    /*
     * ユーザーのステージクリア情報設定
     */
    public static void setUserStageClearInfo(Context context, ArrayList<StageList> stageList){

        for( StageList stage: stageList ){

            // ステージのクリア状況を取得
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), MODE_PRIVATE);
            boolean isClear = sharedPref.getBoolean( stage.mStageName, false);

            // リストに反映
            stage.mIsClear = isClear;
        }
    }

    /*
     * ステージ未クリアのステージ名を取得
     *   ※未クリアのステージの内、gimmick-xml上一番先頭にあるステージ名を返す
     */
    public static String getHeadNotClearStageName(Context context, ArrayList<String> stageList){

        for( String stageName: stageList ){

            // ステージのクリア状況を取得
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), MODE_PRIVATE);
            boolean isClear = sharedPref.getBoolean( stageName, false);
            if( !isClear ){
                return stageName;
            }
        }

        // 全てクリア済みなら、先頭のステージ名を返す
        return stageList.get(0);
    }

    /*
     * !デバッグ用
     *   チュートリアル状況　任意設定
     */
    public static void setTutorialDebug(Context context, int tutorial){

        // 保存
        SharedPreferences sharedPref = context.getSharedPreferences( context.getString(R.string.preference_file_key), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt( context.getString(R.string.saved_tutorial_key), tutorial);
        editor.apply();
    }
}
