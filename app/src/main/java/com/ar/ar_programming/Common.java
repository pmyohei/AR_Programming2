package com.ar.ar_programming;

import static android.content.Context.MODE_PRIVATE;

import static com.ar.ar_programming.GimmickManager.GIMMICK_DELIMITER_TUTORIAL_NAME;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import java.util.ArrayList;

/*
 * 共通処理
 */
public class Common {

    //---------------
    // チュートリアル
    //---------------
    public static int TUTORIAL_DEFAULT = 1;                     // ユーザーデータ取得エラー時のチュートリアル値（データがないなら、初めのチュートリアルから行う）
    public static int TUTORIAL_LAST = 6;                        // チュートリアル最終番号
    public static int TUTORIAL_FINISH = TUTORIAL_LAST + 1;      // チュートリアル終了値（チュートリアルは『１～「この値 - 1」』）

    // チュートリアル名プレフィックス
    public static String PREFIX_TUTORIAL_NAME = "tutorial";

    /*
     * 次に挑戦するチュートリアル番号を取得
     *   例)「tutorial_1」であれば、「1」を返す
     */
    public static int getTutorialNumber(String tutorialName) {

        // チュートリアル名を先頭の固定文字列と番号で分割
        // 例）「tutorial_1」 → [0]"tutorial"   [1]"1"
        String[] splitTutorialName = tutorialName.split(GIMMICK_DELIMITER_TUTORIAL_NAME);

        // 番号部を変換して返す
        return Integer.parseInt(splitTutorialName[1]);
    }

    /*
     * 次に挑戦するチュートリアル番号を取得
     *   例)「tutorial_1」であれば、「1」を返す
     */
    public static int getNextTutorialNumber(Context context) {

        // 次に挑戦するチュートリアル名を取得
        String tutorialName = getNextTutorialName(context);

        // チュートリアル名を先頭の固定文字列と番号で分割
        // 例）「tutorial_1」 → [0]"tutorial"   [1]"1"
        String[] splitTutorialName = tutorialName.split(GIMMICK_DELIMITER_TUTORIAL_NAME);

        // 番号部を変換して返す
        return Integer.parseInt(splitTutorialName[1]);
    }

    /*
     * 次に挑戦するチュートリアル名を取得
     */
    public static String getNextTutorialName(Context context) {

        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), MODE_PRIVATE);

        // チュートリアル名リスト
        Resources res = context.getResources();
        String[] tutorialNameList = res.getStringArray(R.array.tutorial_name);

        // チュートリアルの先頭から参照し、クリアしていないチュートリアルを返す
        for (String tutorialName : tutorialNameList) {
            boolean isClear = sharedPref.getBoolean(tutorialName, false);
            if (!isClear) {
                return tutorialName;
            }
        }

        // 見つからなければ、先頭を返す
        return tutorialNameList[0];
    }

    /*
     * チュートリアル終了済み判定
     */
    public static boolean isCompleteTutorial(Context context) {

        // 最終チュートリアルをクリアしていれば、チュートリアルは終了状態にある
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), MODE_PRIVATE);

        String lastTutorial = (PREFIX_TUTORIAL_NAME + GIMMICK_DELIMITER_TUTORIAL_NAME + Integer.toString(TUTORIAL_LAST));
        return sharedPref.getBoolean(lastTutorial, false);
    }

    /*
     * 指定チュートリアル成功ステータス取得
     */
    public static boolean getTutorialState(Context context, String tutorialName) {

        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), MODE_PRIVATE);
        return sharedPref.getBoolean(tutorialName, false);
    }

    /*
     * ステージクリア保存
     */
    public static void saveStageSuccess(Context context, String stageName) {

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
    public static String getHeadNotSuccessStageName(Context context, ArrayList<String> stageList) {

        for (String stageName : stageList) {
            // ステージのクリア状況を取得
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), MODE_PRIVATE);
            boolean isClear = sharedPref.getBoolean(stageName, false);
            if (!isClear) {
                return stageName;
            }
        }

        // 全てクリア済みなら、先頭のステージ名を返す
        return GimmickManager.getFirstStageParsePosition( context, R.xml.gimmick_select );
    }

    /*
     * !デバッグ用
     *   チュートリアル設定：指定したチュートリアル以降を未クリアにする
     */
    public static void setTutorialDebug(Context context, int tutorial) {

        // チュートリアル名リスト
        Resources res = context.getResources();
        String[] tutorialNameList = res.getStringArray(R.array.tutorial_name);

        // 保存
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        for (int i = TUTORIAL_DEFAULT; i <= TUTORIAL_LAST; i++) {
            boolean clear = (i < tutorial);
            editor.putBoolean(tutorialNameList[i - 1], clear);
        }

        editor.apply();
    }

    /*
     * ステージ名チュートリアル判定
     *   指定されたステージ名がチュートリアルかどうかを判定する
     */
    public static boolean isStageNameTutorial(String stageName){
        return stageName.contains( PREFIX_TUTORIAL_NAME );
    }
}
