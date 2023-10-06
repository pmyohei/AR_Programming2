package com.ar.ar_programming;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

/*
 * ギミックマネージャ
 */
public class GimmickManager {

    //---------------------------
    // xmlファイル：タグ名／属性名
    //---------------------------
    public static final String TAG_START_GIMMICK = "gimmick";
    public static final String ATTR_NAME = "name";

    //---------------------------
    // ブロック種別
    //---------------------------
    public static final String BLOCK_TYPE_START = "start";
    public static final String BLOCK_TYPE_EXE = "exe";
    public static final String BLOCK_TYPE_LOOP = "loop";
    public static final String BLOCK_TYPE_IF = "if";
    public static final String BLOCK_TYPE_IF_ELSE = "if-else";
    public static final String BLOCK_TYPE_IE_ELSEIF = "if-elseif-else";

    //---------------------------
    // ブロック 実行ブロック処理
    //---------------------------
    public static final String BLOCK_EXE_FORWARD = "forward";
    public static final String BLOCK_EXE_BACK = "back";
    public static final String BLOCK_EXE_ROTATE_RIGHT = "rotateright";
    public static final String BLOCK_EXE_ROTATE_LEFT = "rotateleft";
    public static final String BLOCK_EXE_EAT = "eat";
    public static final String BLOCK_EXE_THROW = "throw";
    public static final String BLOCK_EXE_ATTACK = "attack";
    public static final String BLOCK_EXE_LONG_ATTACK = "farattack";
    public static final String BLOCK_EXE_PICKUP = "pickup";
    public static final String BLOCK_EXE_CHANGE_TARGET = "changetarget";

    //------------------------------
    // ブロック 制御ブロック  条件：動詞
    //------------------------------
    public static final String BLOCK_CONDITION_FACING = "facing";
    public static final String BLOCK_CONDITION_COLLECT = "collect";
    public static final String BLOCK_CONDITION_EAT = "eat";
    public static final String BLOCK_CONDITION_DEFEAT = "defeat";
    public static final String BLOCK_CONDITION_ARRIVAL = "arrival";
    public static final String BLOCK_CONDITION_FRONT = "front";

    //------------------------------
    // Node名
    //------------------------------
    public static final String NODE_NAME_NONE = "";
    public static final String NODE_NAME_NOTHING = "nothing";   // ※条件文で何もなしを示す
    public static final String NODE_NAME_GOAL_GUIDE_UI = "goalGuideUI";
    public static final String NODE_NAME_ANCHOR = "anchor";
    public static final String NODE_NAME_STAGE = "stage";
    public static final String NODE_NAME_REPLACE = "replace";

    //------------------------------
    // ギミック 主体種別
    //------------------------------
    // characterプロパティの値
    public static final String GIMMICK_MAIN_ANIMAL = "animal";
    public static final String GIMMICK_MAIN_VEHICLE = "vehicle";

    //-----------------------------------
    // ステージ成功条件：successCondition
    //-----------------------------------
    public static final String SUCCESS_CONDITION_GOAL = "goal";
    public static final String SUCCESS_CONDITION_ALL_REMOVE = "all_remove";
    public static final String SUCCESS_CONDITION_REMOVE_AND_GOAL = "remove_and_goal";

    //------------------------------
    // ギミック 真偽値
    //------------------------------
    public static final String GIMMICK_TRUE = "true";
    public static final String GIMMICK_FALSE = "false";

    //------------------------------
    // フォーマット位置関連：デリミタ
    //------------------------------
    // プロパティの各情報
    public static final String GIMMICK_DELIMITER_INFO = " *, *";
    // 座標
    public static final String GIMMICK_DELIMITER_COORDINATE = " *: *";
    // 情報内単語  例）single_forward
    public static final String GIMMICK_DELIMITER_WORD = "_";
    // 制御ブロックの条件　動詞と名詞   例）facing-eatable
    public static final String GIMMICK_DELIMITER_CONDITION = "-";
    // パス
    public static final String GIMMICK_DELIMITER_PATH = "/";
    // 対象Node情報：「Nodeの状態」と「Node名」
    public static final String GIMMICK_DELIMITER_TARGET_NODE_INFO = ":";
    // チュートリアル文字列と番号のデリミタ  例）tutorial_1 の 「_」
    public static final String GIMMICK_DELIMITER_TUTORIAL_NAME = "_";

    //------------------------------
    // フォーマット位置関連：ゴール説明
    //------------------------------
    // 例）guide_major_tutorial_1
    public static final int GOAl_EXP_MAJOR_POS = 0;
    public static final int GOAl_EXP_SUB_POS = 1;
    public static final int GOAl_EXP_CONTENTS_POS = 2;
    public static final int GOAl_EXP_EXPLANATION_POS = 3;

    //------------------------------
    // フォーマット位置関連：ブロック
    //------------------------------
    // ブロック情報共通
    public static final int BLOCK_TYPE_POS = 0;
    public static final int BLOCK_USABLE_NUM_POS = 2;               // 使用可能上限数の位置
    // 実行ブロック  例）exe_forward-1
    public static final int BLOCK_ACTION_DATA_POS = 1;              // アクション/アクション付随情報の位置  例）「forward-1」の位置
    public static final int BLOCK_ACTION_POS = 0;                   // アクション情報の位置    例）「forward」の位置
    public static final int BLOCK_ACTION_ATTACHED_POS = 1;          // アクション付随情報の位置 例）「1」や「eatable」の位置
    // 実行ブロック（対象Node情報付属）  例）changeTarget-facing:enemy
    public static final int BLOCK_ACTION_TARGET_NODE_STATE_POS = 0; // アクション対象Node 状態位置 例）「facing」の位置
    public static final int BLOCK_ACTION_TARGET_NODE_POS = 1;       // アクション対象Node 名称位置 例）「enemy」の位置
    // 制御ブロック  例）loop_facing-eatable
    public static final int BLOCK_CONDITION_POS = 1;                // 例）「forward」や「facing-eatable」の位置
    public static final int BLOCK_CONDITION_ACTION_POS = 0;         // 例）「facing」の位置
    public static final int BLOCK_CONDITION_OBJECT_POS = 1;         // 例）「eatable」の位置
    // 制御ブロック  例）if-elseif-else_front-eatable-poison
    public static final int BLOCK_CONDITION_ELSEIF_OBJECT_POS = 2;  // 例）「poison」の位置

    //------------------------------
    // リソース
    //------------------------------
    // 文字列リソース構築のプレフィックス
    // 例）「block_exe_forward」の「block」
    private static final String PREFIX_STRING_RESOURCE = "block";

    /*
     * コンストラクタ
     */
    public GimmickManager() {
    }

    /*
     * ギミックの取得：ステージ名指定
     */
    public static Gimmick getGimmick(Context context, String stageName) {

        //--------------------------------
        // 該当ギミック情報までParseを進める
        //--------------------------------
        XmlResourceParser parser = getGimmickXmlParse(context, stageName);
        advanceParsePosition(parser, stageName);

        //-----------------
        // ギミック生成
        //-----------------
        Gimmick gimmick = new Gimmick(context);
        readGimmickData(gimmick, parser);
        parser.close();

        // チュートリアル値の設定
        boolean isTutorial = Common.isStageNameTutorial( stageName );
        if( isTutorial ){
            int tutorialNumber = Common.getTutorialNumber(stageName);
            gimmick.setTutorial(tutorialNumber);
        }

        return gimmick;
    }


    /*
     * 指定parserからギミック情報を読み込み、指定ギミックに設定する
     */
    private static void readGimmickData(Gimmick gimmick, XmlResourceParser parser) {

        //----------------------
        // リスト変換するプロパティ
        //----------------------
        String goalGuide = parser.getAttributeValue(null, "goalGuide");
        // キャラクター
        String characterPosition = parser.getAttributeValue(null, "characterPosition");
        String characterAngle = parser.getAttributeValue(null, "characterAngle");
        // ゴール
        String goalPosition = parser.getAttributeValue(null, "goalPosition");
        String goalAngle = parser.getAttributeValue(null, "goalAngle");
        String goalName = parser.getAttributeValue(null, "goalName");
        // オブジェクト
        String objectGlb = parser.getAttributeValue(null, "objectGlb");
        String objectNum = parser.getAttributeValue(null, "objectNum");
        String objectName = parser.getAttributeValue(null, "objectName");
        String objectReplaceName = parser.getAttributeValue(null, "objectReplaceName");
        String objectReplaceGlb = parser.getAttributeValue(null, "objectReplaceGlb");
        String objectPositionRandom = parser.getAttributeValue(null, "objectPositionRandom");
        String objectPosition = parser.getAttributeValue(null, "objectPosition");
        String objectAngle = parser.getAttributeValue(null, "objectAngle");
        String objectObstacle = parser.getAttributeValue(null, "objectObstacle");
        // 敵
        String enemyGlb = parser.getAttributeValue(null, "enemyGlb");
        String enemyNum = parser.getAttributeValue(null, "enemyNum");
        String enemyKind = parser.getAttributeValue(null, "enemyKind");
        String enemyNumRandom = parser.getAttributeValue(null, "enemyNumRandom");
        String enemyPosition = parser.getAttributeValue(null, "enemyPosition");
        String enemyEndPosition = parser.getAttributeValue(null, "enemyEndPosition");
        // ブロック
        String block = parser.getAttributeValue(null, "block");

        //--------------------------
        // ギミックにreadデータを設定
        //--------------------------
        gimmick.name = parser.getAttributeValue(null, "name");
        gimmick.successCondition = parser.getAttributeValue(null, "successCondition");
        gimmick.successRemoveTarget = parser.getAttributeValue(null, "successRemoveTarget");
        gimmick.character = parser.getAttributeValue(null, "character");
        gimmick.setGoalGuide(goalGuide);
        gimmick.stageGlb = parser.getAttributeValue(null, "stageGlb");
        // キャラクター
        gimmick.characterGlb = parser.getAttributeValue(null, "characterGlb");
        gimmick.setCharacterPosition(characterPosition);
        gimmick.setCharacterAngle(characterAngle);
        // ゴール
        gimmick.goalGlb = parser.getAttributeValue(null, "goalGlb");
        gimmick.setGoalAngle(goalAngle);
        gimmick.setGoalName(goalName);
        gimmick.setGoalPosition(goalPosition);
        // オブジェクト
        gimmick.setObjectGlb(objectGlb);
        gimmick.setObjectNum(objectNum);
        gimmick.setObjectName(objectName);
        gimmick.setObjectReplaceInfo(objectReplaceName, objectReplaceGlb);
        gimmick.setObjectPositionRandom(objectPositionRandom);
        gimmick.setObjectPositionVecList(objectPosition);
        gimmick.setObjectAngle(objectAngle);
        gimmick.setObjectObstacle(objectObstacle);
        // 敵
        gimmick.setEnemyGlb(enemyGlb);
        gimmick.setEnemyNum(enemyNum);
        gimmick.setEnemyKind(enemyKind);
        gimmick.setEnemyNumRandom(enemyNumRandom);
        gimmick.setEnemyPositionVecList(enemyPosition);
        gimmick.setEnemyEndPosition(enemyEndPosition);
        // ブロック
        gimmick.setBlock(block);
    }

    /*
     * gimmick向けxmlファイルParseを取得
     */
    public static XmlResourceParser getGimmickXmlParse(Context context, String stageName) {

        int gimmickXmlID;

        boolean isTutorial = Common.isStageNameTutorial(stageName);
        if( isTutorial ){
            // チュートリアル向け
            gimmickXmlID = R.xml.gimmick_tutorial;
        } else {
            // チュートリアル終了向け
            gimmickXmlID = R.xml.gimmick_select;
        }

        Resources resources = context.getResources();
        return resources.getXml( gimmickXmlID );
    }

    /*
     * 解析位置をステージ名の位置まで進める
     */
    public static void advanceParsePosition(XmlResourceParser parser, String stageName) {

        //------------------------------
        // 該当するギミック情報まで進める
        //------------------------------
        try {
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                // 「開始タグ」であり「タグ名=gimmick」の位置
                if ((eventType == XmlPullParser.START_TAG) && (Objects.equals(parser.getName(), TAG_START_GIMMICK))) {

                    // name属性の値を取得
                    String name = parser.getAttributeValue(null, ATTR_NAME);
                    if ( name.equals(stageName) ) {
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
     * 条件ブロックのブロック種別表記を統一
     */
    public static String unificationConditionType(String type) {

        // 「if-else」「if-elseif-else」の場合は「if」と統一させる
        if (type.equals(BLOCK_TYPE_IF_ELSE) || type.equals(BLOCK_TYPE_IE_ELSEIF)) {
            return BLOCK_TYPE_IF;
        }

        // それ以外はそのまま
        return type;
    }

    /*
     * ブロック文向け文字列の構築／文字列の取得
     */
    public static String getBlockStatement(Context context, String type, String action, String targetNode) {

        //-------------------------------
        // 文字列リソースの生成
        //-------------------------------
        // 文字列リソースIDの文字列を構築
        // 例）block_exe_forward
        type = unificationConditionType(type);
        String resourceStr = PREFIX_STRING_RESOURCE + GIMMICK_DELIMITER_WORD + type + GIMMICK_DELIMITER_WORD + action;

        // 文字列リソースID生成
        Resources resources = context.getResources();
        String packageName = context.getPackageName();


        // 対象Node名を取得
        Log.i("ギミック改修", "type=" + type);
        Log.i("ギミック改修", "action=" + action);
        Log.i("ギミック改修", "targetNode=" + targetNode);
        String SpecificNodeName = getSpecificNodeName(targetNode);
        int targetNodeId = resources.getIdentifier(SpecificNodeName, "string", packageName);
        String targetNodeName = "";
        if( targetNodeId != 0x00 ){
            targetNodeName = context.getString(targetNodeId);
        }

        Log.i("ギミック改修", "resourceStr=" + resourceStr);
        int statementId = resources.getIdentifier(resourceStr, "string", packageName);
        return context.getString(statementId, targetNodeName);
    }

    /*
     * 具体的なNode名の抽出
     */
    public static String getSpecificNodeName(String targetNode) {

        // 対象Node情報の区切り文字がなければ、引数のNode名がそのまま具体的なNode名
        if (!targetNode.contains(GIMMICK_DELIMITER_TARGET_NODE_INFO)) {
            return targetNode;
        }

        // 対象Node情報の区切り文字があれば、具体的なNode名を抽出
        String[] nodeInfo = targetNode.split(GIMMICK_DELIMITER_TARGET_NODE_INFO);
        return nodeInfo[BLOCK_ACTION_TARGET_NODE_POS];
    }

    /*
     * ブロックアイコン向けdrawableリソースIDの構築／drawableの取得
     */
    public static Drawable getBlockIcon(Context context, String type, String action) {

        //--------------------------------------
        // drawableリソースの生成
        //--------------------------------------
        // drawableリソースIDの文字列を構築
        // 例）block_eat, block_loop

        // 「プレフィックス」に連結するワード
        String word;
        switch (type) {
            case BLOCK_TYPE_EXE:
                word = action;
                break;

            case BLOCK_TYPE_LOOP:
                word = type;
                break;

            case BLOCK_TYPE_IF:
                word = "if";
                break;

            case BLOCK_TYPE_IF_ELSE:
            case BLOCK_TYPE_IE_ELSEIF:
            default:
                word = "if_else";
                break;
        }

        // drawableリソースID文字列の生成
        String resourceStr = PREFIX_STRING_RESOURCE + GIMMICK_DELIMITER_WORD + word;

        // drawableリソースID生成
        Resources resources = context.getResources();
        String packageName = context.getPackageName();

        Log.i("リソース構築", "drawable resourceStr=" + resourceStr);
        int drawableId = resources.getIdentifier(resourceStr, "drawable", packageName);
        return context.getDrawable(drawableId);
    }

    /*
     * ステージ名をリストで取得
     */
    public static ArrayList<String> getStageNameList( Context context, int gimmickXmlID ) {

        // リスト
        ArrayList<String> stageList = new ArrayList<>();

        Resources resources = context.getResources();
        XmlResourceParser parser = resources.getXml(gimmickXmlID);

        //---------
        // xml解析
        //---------
        try {
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                // 開始タグでタグ名が「gimmick」の場合、読み込み
                if ((eventType == XmlPullParser.START_TAG) && (Objects.equals(parser.getName(), TAG_START_GIMMICK))) {
                    String stageName = parser.getAttributeValue(null, ATTR_NAME);
                    stageList.add( stageName );
                }

                // 次の要素を読み込む
                parser.next();
                eventType = parser.getEventType();
            }
        } catch (XmlPullParserException | IOException ignored) {
            //★エラー対応検討
        }

        parser.close();

        return stageList;
    }
}
