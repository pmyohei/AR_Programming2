package com.ar.ar_programming;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;

public class GimmickManager {

    //---------------------------
    // 定数
    //---------------------------

    //---------------------------
    // フィールド変数
    //---------------------------

    public GimmickManager() {
    }


    /*
     * tmp
     */
    private void tmp() {
    }

    /*
     * ギミックの取得
     */
    public static Gimmick getGimmick(Context context) {

        Resources resources = context.getResources();

        // チュートリアル終了値
        final int TUTORIAL_END = resources.getInteger(R.integer.saved_tutorial_end);

        // 現在のチュートリアル進行状況を取得
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), MODE_PRIVATE);
        int defaultValue = resources.getInteger(R.integer.saved_tutorial_default_key);
        int tutorial = sharedPref.getInt(context.getString(R.string.saved_tutorial_key), defaultValue);

        // チュートリアルかチュートリアル終了しているかでギミック選定を分ける
        if (tutorial < TUTORIAL_END) {
            // チュートリアルからギミックを生成
            return makeTutorialGimmick(context, tutorial);
        } else {
            // ユーザー設定に応じたギミックを生成
            return makeUserGimmick(context, sharedPref);
        }
    }


    /*
     * 指定parserからギミック情報を読み込み、指定ギミックに設定する
     */
    private static void readGimmickData(Gimmick gimmick, XmlResourceParser parser) {

        String characterPosition = parser.getAttributeValue(null, "characterPosition");
        String goalPosition = parser.getAttributeValue(null, "goalPosition");
        String objectName = parser.getAttributeValue(null, "objectName");
        String objectNum = parser.getAttributeValue(null, "objectNum");
        String objectKind = parser.getAttributeValue(null, "objectKind");
        String block = parser.getAttributeValue(null, "block");

        // ギミックにreadデータを設定
        gimmick.successCondition = parser.getAttributeValue(null, "successCondition");
        gimmick.stageName = parser.getAttributeValue(null, "stageName");
        gimmick.characterName = parser.getAttributeValue(null, "characterName");
        gimmick.setCharacterPosition(characterPosition);
        gimmick.goalName = parser.getAttributeValue(null, "goalName");
        gimmick.setGoalPosition(goalPosition);
        gimmick.setObjectName(objectName);
        gimmick.setObjectNum(objectNum);
        gimmick.setObjectKind(objectKind);
        gimmick.setBlock(block);
    }

    /*
     * ユーザーのチュートリアル状態に応じたギミックXML名を生成
     */
    private static Gimmick makeTutorialGimmick(Context context, int tutorial) {

        Resources resources = context.getResources();
        XmlResourceParser parser = resources.getXml(R.xml.gimmick_tutorial);

        Log.i("ギミック", "getAttributeCount()=" + parser.getAttributeCount());

        //-----------------------------------------
        // 該当するチュートリアルのギミック情報まで進める
        //-----------------------------------------
        try {
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                // 開始タグでタグ名が「gimmick」の場合、読み込み
                if ((eventType == XmlPullParser.START_TAG) && (Objects.equals(parser.getName(), "gimmick"))) {
                    int sequence = parser.getAttributeIntValue(null, "sequence", 1);
                    if (sequence == tutorial) {
                        break;
                    }
                }

                // 次の要素を読み込む
                parser.next();
                eventType = parser.getEventType();
            }
        } catch (XmlPullParserException | IOException ignored) {
            //★エラー対応検討
        }

        //-----------------------------------------
        // ギミック生成
        //-----------------------------------------
        Gimmick gimmick = new Gimmick();
        readGimmickData(gimmick, parser);

        // parser閉じる
        parser.close();

        return gimmick;
    }

    /*
     * ユーザー設定に応じたギミックXML名を生成
     */
    private static Gimmick makeUserGimmick(Context context, SharedPreferences sharedPref) {

        Resources resources = context.getResources();

        // ユーザー設定に応じたギミックXMLファイルIDを取得
        int xmlID = getUserGimmickXmlFileNameID(context, sharedPref);
        XmlResourceParser parser = resources.getXml(xmlID);

        // ギミックをランダムに取得して返す
        return getGimmickRandomly(parser);
    }

    /*
     * ギミックリスト内からランダムにギミックを取得する
     */
    private static Gimmick getGimmickRandomly(XmlResourceParser parser) {

        //------------------------
        // 取得ギミック位置
        //------------------------
        // ギミック数を取得
        int gimmickNum = getGimmickNum(parser);
        // 対象ギミック位置をランダムに選定
        Random random = new Random();
        int gimmickIndex = random.nextInt(gimmickNum);

        //------------------------
        // 該当位置のギミックを取得
        //------------------------
        try {
            int index = 0;
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                // 開始タグでタグ名が「gimmick」の場合、読み込み
                if ((eventType == XmlPullParser.START_TAG) && (Objects.equals(parser.getName(), "gimmick"))) {
                    // 選定対象のギミックindexまできたら終了
                    if (index == gimmickIndex) {
                        break;
                    }
                    index++;
                }

                // 次の要素を読み込む
                parser.next();
                eventType = parser.getEventType();
            }
        } catch (XmlPullParserException | IOException ignored) {
            //★エラー対応検討
        }

        //-----------------------------------------
        // ギミック生成
        //-----------------------------------------
        Gimmick gimmick = new Gimmick();
        readGimmickData(gimmick, parser);

        // parser閉じる
        parser.close();

        return gimmick;
    }

    /*
     * ギミックリスト内に定義されているギミック数を取得
     */
    private static int getGimmickNum(XmlResourceParser parser) {

        try {
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                // 開始タグでタグ名が「ギミックリスト」の場合、読み込み
                if ((eventType == XmlPullParser.START_TAG) && (Objects.equals(parser.getName(), "gimmick_list"))) {
                    return parser.getAttributeIntValue(null, "dataNum", 1);
                }

                // 次の要素を読み込む
                parser.next();
                eventType = parser.getEventType();
            }

        } catch (XmlPullParserException | IOException ignored) {
            //★エラー対応検討
        }

        // （未想定ルート）
        // 1つはあるものとして処理を進める
        return 1;
    }

    /*
     * ユーザー設定に応じたギミックXMLファイルIDを取得
     */
    private static int getUserGimmickXmlFileNameID( Context context, SharedPreferences sharedPref ){

        Resources resources = context.getResources();

        //------------------
        // xmlファイルidの判定
        //------------------
        // ユーザーの設定しているキャラクターと難易度
        int defaultCharacter = resources.getInteger(R.integer.saved_character_default_key);
        int defaultDifficulty = resources.getInteger(R.integer.saved_difficulty_default_key);
        int character = sharedPref.getInt(context.getString(R.string.saved_character_key), defaultCharacter);
        int difficulty = sharedPref.getInt(context.getString(R.string.saved_difficulty_key), defaultDifficulty);

        // xmlファイルidを判定
        int xmlID;
        if (character == SettingActivity.CHARACTER_ANIMAL) {
            if (difficulty == SettingActivity.PLAY_DIFFICULTY_EASY) {
                xmlID = R.xml.gimmick_animal_easy;
            } else {
                xmlID = R.xml.gimmick_animal_difficult;
            }
        } else {
            if (difficulty == SettingActivity.PLAY_DIFFICULTY_EASY) {
                xmlID = R.xml.gimmick_vehicle_easy;
            } else {
                xmlID = R.xml.gimmick_vehicle_difficult;
            }
        }

        return xmlID;
    }
}
