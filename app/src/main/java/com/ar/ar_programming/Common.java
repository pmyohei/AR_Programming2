package com.ar.ar_programming;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

/*
 * 共通処理
 */
public class Common {

    /*
     * チュートリアルシーケンスの取得
     */
    public static int getTutorialSequence(Context context){

        Resources resources = context.getResources();

        // 現在のチュートリアル進行状況を取得
        SharedPreferences sharedPref = context.getSharedPreferences( context.getString(R.string.preference_file_key), MODE_PRIVATE);
        int defaultValue = resources.getInteger(R.integer.saved_tutorial_block);
        int tutorial = sharedPref.getInt( context.getString(R.string.saved_tutorial_key), defaultValue);

        return tutorial;
    }

    /*
     * チュートリアル終了済み判定
     */
    public static boolean isFisishTutorial(Context context){

        Resources resources = context.getResources();

        // チュートリアル終了値
        final int TUTORIAL_END = resources.getInteger(R.integer.saved_tutorial_end);

        // 現在のチュートリアル進行状況を取得
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), MODE_PRIVATE);
        int defaultValue = resources.getInteger(R.integer.saved_tutorial_block);
        int tutorial = sharedPref.getInt(context.getString(R.string.saved_tutorial_key), defaultValue);

        // チュートリアル終了しているかどうか
        return  (tutorial >= TUTORIAL_END);
    }


    /*
     * チュートリアルを次に進める
     */
    public static void proceedNextTutorial(Context context){

        // チュートリアルを次に進める
        int tutorial = getTutorialSequence( context );
        tutorial++;

        // 保存
        SharedPreferences sharedPref = context.getSharedPreferences( context.getString(R.string.preference_file_key), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt( context.getString(R.string.saved_tutorial_key), tutorial);
        editor.apply();
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
