package com.ar.ar_programming;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

/*
 * 共通処理
 */
public class Common {

    public static int TUTORIAL_DEFAULT = 1;                     // ユーザーデータ取得エラー時のチュートリアル値（データがないなら、初めのチュートリアルから行う）
    public static int TUTORIAL_LAST = 6;                        // チュートリアル最終番号
    public static int TUTORIAL_FINISH = TUTORIAL_LAST + 1;      // チュートリアル終了値（チュートリアルは『１～「この値 - 1」』）

    /*
     * チュートリアルシーケンスの取得
     */
    public static int getTutorialSequence(Context context) {

        // 現在のチュートリアル進行状況を取得
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), MODE_PRIVATE);

        // チュートリアルの先頭から参照し、クリアしていない番号を取得
        int i;
        for( i = TUTORIAL_DEFAULT; i <= TUTORIAL_LAST; i++ ){
            String tutorialNum = Integer.toString( i );
            boolean isClear = sharedPref.getBoolean(tutorialNum, false);
            if( !isClear ){
                return i;
            }
        }

        return i;
    }

    /*
     * チュートリアル終了済み判定
     */
    public static boolean isFisishTutorial(Context context) {

        // 最終チュートリアルをクリアしていれば、チュートリアルは終了状態にある
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), MODE_PRIVATE);

        String lastTutorialNum = Integer.toString( TUTORIAL_LAST );
        return sharedPref.getBoolean(lastTutorialNum, false);
    }

    /*
     * チュートリアルを次に進める
     */
//    public static void proceedNextTutorial(Context context) {
//
//        //------------
//        // 更新対象
//        //------------
//        // 未クリア → クリア となるチュートリアル
//        int sequence = getTutorialSequence(context);
//        String clearTutorial = Integer.toString( sequence );
//
//        //------------
//        // 保存
//        //------------
//        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putBoolean(clearTutorial, true);
//        editor.apply();
//    }

    /*
     * ステージクリア保存
     */
    public static void saveStageClear(Context context, String stageName) {

        // 指定されたステージをクリア状態として保存
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(stageName, true);
        editor.apply();
    }

    /*
     * ユーザーのクリアリスト情報設定
     */
    public static void setUserClearInfo(Context context, ArrayList<StageList> stageList) {

        for (StageList stage : stageList) {
            // ステージのクリア状況を取得
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), MODE_PRIVATE);
            // リストに反映
            stage.mIsClear = sharedPref.getBoolean(stage.mStageName, false);
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
