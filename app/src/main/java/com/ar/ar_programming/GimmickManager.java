package com.ar.ar_programming;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

/*
 * ギミックマネージャ
 */
public class GimmickManager {

    //---------------------------
    // ブロック種別
    //---------------------------
    public static final String BLOCK_TYPE_SINGLE = "exe";
    public static final String BLOCK_TYPE_LOOP = "loop";
    public static final String BLOCK_TYPE_IF = "if";
    public static final String BLOCK_TYPE_IF_ELSE = "if-else";
    public static final String BLOCK_TYPE_IE_ELSEIF = "if-elseif-else";

    //---------------------------
    // ブロック 実行ブロック処理
    //---------------------------
    public static final String BLOCK_EXE_FORWARD = "forward";
    public static final String BLOCK_EXE_BACK = "back";
    public static final String BLOCK_EXE_ROTATE_RIGHT = "rotateRight";
    public static final String BLOCK_EXE_ROTATE_LEFT = "rotateLeft";
    public static final String BLOCK_EXE_EAT = "eat";
    public static final String BLOCK_EXE_THROW_AWAY = "throwAway";
    public static final String BLOCK_EXE_ATTACK = "attack";
    public static final String BLOCK_EXE_PICKUP = "pickup";

    //------------------------------
    // ブロック 制御ブロック  条件：動詞
    //------------------------------
    public static final String BLOCK_CONDITION_FACING = "facing";
    public static final String BLOCK_CONDITION_COLLECT = "collect";
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
    public static final String NODE_NAME_GOAL = "goal";
    public static final String NODE_NAME_OBSTACLE = "obstacle";
    public static final String NODE_NAME_EATABLE = "eatable";
    public static final String NODE_NAME_POISON = "poison";
    public static final String NODE_NAME_ENEMY = "enemy";
    public static final String NODE_NAME_PICKUP = "pickup";

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
    public static final String SUCCESS_CONDITION_COLLECT = "collect";
    public static final String SUCCESS_CONDITION_ALL_EAT = "all_eat";
    public static final String SUCCESS_CONDITION_ATTACK_AND_GOAL = "attack_and_goal";
    public static final String SUCCESS_CONDITION_PICKUP_AND_GOAL = "pickup_and_goal";

    //------------------------------
    // ギミック 真偽値
    //------------------------------
    public static final String GIMMICK_TRUE = "true";
    public static final String GIMMICK_FALSE = "false";

    //------------------------------
    // 具体名
    //------------------------------
    // ゴール関連
    public static final String SPECIFIC_NAME_HOUSE = "house";
    public static final String SPECIFIC_NAME_SWEET_HOUSE = "sweetHouse";
    // 動物
    public static final String SPECIFIC_NAME_RABIT = "rabit";
    public static final String SPECIFIC_NAME_BEAR = "bear";
    public static final String SPECIFIC_NAME_FOX = "fox";
    public static final String SPECIFIC_NAME_SQUIRREL = "squirrel";
    public static final String SPECIFIC_NAME_BEE = "bee";
    // 乗り物
    public static final String SPECIFIC_NAME_CAR = "car";
    public static final String SPECIFIC_NAME_SPORTS_CAR = "sportsCar";
    // 食べ物
    public static final String SPECIFIC_NAME_VEGETABLE = "vegetable";
    public static final String SPECIFIC_NAME_CARROT = "carrot";
    public static final String SPECIFIC_NAME_MASHROOM_POISON = "mushroomPoison";
    public static final String SPECIFIC_NAME_HONEY = "honey";
    public static final String SPECIFIC_NAME_DONUTS = "donuts";
    public static final String SPECIFIC_NAME_CHOCOLATE = "chocolate";
    public static final String SPECIFIC_NAME_GREEN_APPLE = "greenApple";
    public static final String SPECIFIC_NAME_PUMPKIN = "pumpkin";
    // 物体
    public static final String SPECIFIC_NAME_SIGNBOARD_STOP = "signboardStop";
    public static final String SPECIFIC_NAME_PRESENT = "present";

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
    public static final int BLOCK_CONTENTS_POS = 1;                 // 例）「forward」や「facing-eatable」の位置
    public static final int BLOCK_ACTION_POS = 1;                 // 例）「forward」や「facing-eatable」の位置
    public static final int BLOCK_CONDITION_POS = 1;                 // 例）「forward」や「facing-eatable」の位置
    // 実行ブロック  例）single_forward_1
    public static final int BLOCK_VALUE_LIMIT_POS = 2;              // 例）「1」の位置
    // 制御ブロック  例）loop_facing-eatable
    public static final int BLOCK_CONDITION_ACTION_POS = 0;         // 例）「facing」の位置
    public static final int BLOCK_CONDITION_OBJECT_POS = 1;         // 例）「eatable」の位置
    // 制御ブロック  例）if-elseif-else_front-eatable-poison
    public static final int BLOCK_CONDITION_ELSEIF_OBJECT_POS = 2;  // 例）「poison」の位置


    /*
     * コンストラクタ
     */
    public GimmickManager() {
    }

    /*
     * ギミックの取得
     */
    public static Gimmick getGimmick(Context context) {

        // チュートリアルかチュートリアル終了しているかでギミック選定を分ける
        boolean finishTutorial = Common.isFisishTutorial(context);
        if (finishTutorial) {
            // ユーザー設定に応じたギミックを生成
            return makeUserGimmick(context);
        } else {
            // チュートリアルからギミックを生成
            int tutorial = Common.getTutorialSequence(context);
            return makeTutorialGimmick(context, tutorial);
        }
    }

    /*
     * 指定parserからギミック情報を読み込み、指定ギミックに設定する
     */
    private static void readGimmickData(Gimmick gimmick, XmlResourceParser parser) {

        //----------------------
        // リスト変換するプロパティ
        //----------------------
        String goalExplanation = parser.getAttributeValue(null, "goalExplanation");
        // キャラクター
        String characterPosition = parser.getAttributeValue(null, "characterPosition");
        String characterAngle = parser.getAttributeValue(null, "characterAngle");
        // ゴール
        String goalPosition = parser.getAttributeValue(null, "goalPosition");
        String goalAngle = parser.getAttributeValue(null, "goalAngle");
        // オブジェクト
        String objectGlb = parser.getAttributeValue(null, "objectGlb");
        String objectNum = parser.getAttributeValue(null, "objectNum");
        String objectKind = parser.getAttributeValue(null, "objectKind");
        String objectPositionRandom = parser.getAttributeValue(null, "objectPositionRandom");
        String objectPosition = parser.getAttributeValue(null, "objectPosition");
        String objectAngle = parser.getAttributeValue(null, "objectAngle");
        // 敵
        String enemyGlb = parser.getAttributeValue(null, "enemyGlb");
        String enemyNum = parser.getAttributeValue(null, "enemyNum");
        String enemyKind = parser.getAttributeValue(null, "enemyKind");
        String enemyNumRandom = parser.getAttributeValue(null, "enemyNumRandom");
        String enemyPosition = parser.getAttributeValue(null, "enemyPosition");
        String enemyEndPosition = parser.getAttributeValue(null, "enemyEndPosition");
        // ブロック
        String blockNodeWord = parser.getAttributeValue(null, "blockNodeWord");
        String block = parser.getAttributeValue(null, "block");

        //--------------------------
        // ギミックにreadデータを設定
        //--------------------------
        gimmick.successCondition = parser.getAttributeValue(null, "successCondition");
        gimmick.character = parser.getAttributeValue(null, "character");
        gimmick.setGoalExplanation(goalExplanation);
        gimmick.stageGlb = parser.getAttributeValue(null, "stageGlb");
        // キャラクター
        gimmick.characterGlb = parser.getAttributeValue(null, "characterGlb");
        gimmick.setCharacterPosition(characterPosition);
        gimmick.setCharacterAngle(characterAngle);
        // ゴール
        gimmick.goalGlb = parser.getAttributeValue(null, "goalGlb");
        gimmick.setGoalAngle(goalAngle);
        gimmick.setGoalPosition(goalPosition);
        // オブジェクト
        gimmick.setObjectGlb(objectGlb);
        gimmick.setObjectNum(objectNum);
        gimmick.setObjectKind(objectKind);
        gimmick.setObjectPositionRandom(objectPositionRandom);
        gimmick.setObjectPositionVecList(objectPosition);
        gimmick.setObjectAngle(objectAngle);
        // 敵
        gimmick.setEnemyGlb(enemyGlb);
        gimmick.setEnemyNum(enemyNum);
        gimmick.setEnemyKind(enemyKind);
        gimmick.setEnemyNumRandom(enemyNumRandom);
        gimmick.setEnemyPositionVecList(enemyPosition);
        gimmick.setEnemyEndPosition(enemyEndPosition);
        // ブロック
        gimmick.setBlockNodeWord(blockNodeWord);
        gimmick.setBlock(block);
    }

    /*
     * ユーザーのチュートリアル状態に応じたギミックXML名を生成
     */
    private static Gimmick makeTutorialGimmick(Context context, int tutorial) {

        // チュートリアル終了値
        final int TUTORIAL_END = context.getResources().getInteger(R.integer.saved_tutorial_end);

        Resources resources = context.getResources();
        XmlResourceParser parser = resources.getXml(R.xml.gimmick_tutorial);

        //-----------------------------------------
        // 該当するチュートリアルのギミック情報まで進める
        //-----------------------------------------
        try {
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                // 開始タグでタグ名が「gimmick」の場合、読み込み
                if ((eventType == XmlPullParser.START_TAG) && (Objects.equals(parser.getName(), "gimmick"))) {
                    int sequence = parser.getAttributeIntValue(null, "sequence", TUTORIAL_END);
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
        Gimmick gimmick = new Gimmick(context);
        readGimmickData(gimmick, parser);

        // parser閉じる
        parser.close();

        return gimmick;
    }

    /*
     * ユーザー設定に応じたギミックXML名を生成
     */
    private static Gimmick makeUserGimmick(Context context) {

        Resources resources = context.getResources();

        // ユーザー設定に応じたギミックXMLファイルIDを取得
        int xmlID = getUserGimmickXmlFileNameID(context);
        XmlResourceParser parser = resources.getXml(xmlID);

        // ギミックをランダムに取得して返す
        return getGimmickRandomly(parser, context);
    }

    /*
     * ギミックリスト内からランダムにギミックを取得する
     */
    private static Gimmick getGimmickRandomly(XmlResourceParser parser, Context context) {

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
        Gimmick gimmick = new Gimmick(context);
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
    private static int getUserGimmickXmlFileNameID(Context context) {

        Resources resources = context.getResources();
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), MODE_PRIVATE);

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

    /*
     * ブロック文向け文字列の構築
     */
    public static String buildBlockStatementId(Context context, String type, String action, String targetNode) {

        //--------------------------------------
        // 文字列リソースの生成
        //--------------------------------------
        // 文字列リソースIDの文字列を構築
        // 例）block_exe_forward
        final String PREFIX = "block";
        String resourceStr = PREFIX + GIMMICK_DELIMITER_WORD + type + GIMMICK_DELIMITER_WORD + action;

        // 文字列リソースID生成
        Resources resources = context.getResources();
        String packageName = context.getPackageName();

        int statementId = resources.getIdentifier(resourceStr, "string", packageName);
        String statement = context.getString(statementId);

        //-------------------------------------
        // ブロック文内のNode名設定
        //-------------------------------------
        // 置換前のワード
        final String preReplaceWord = "xxx";

        // 置換前のワードがなければ、置換なし
        if (!statement.contains(preReplaceWord)) {
            return statement;
        }

        // 対象Node名を取得
        int targetNodeId = resources.getIdentifier(targetNode, "string", packageName);
        String targetNodeName = context.getString(targetNodeId);

        // ブロック文に対象Node名を埋め込み
        // （「xxx」を「Node名」に置換）
        return statement.replace(preReplaceWord, targetNodeName);
    }

    /*
     * ブロックアイコン向けdrawableリソースIDの構築
     */
    public static Drawable buildBlockIconId( Context context, String type, String action ) {

        //--------------------------------------
        // drawableリソースの生成
        //--------------------------------------
        // drawableリソースIDの文字列を構築
        // 例）block_eat, block_loop
        final String PREFIX = "block";

        // 「block_」に連結するワード
        String word;
        switch ( type ){
            case BLOCK_TYPE_SINGLE:
                word = action;
                break;

            case BLOCK_TYPE_LOOP:
                word = type;
                break;

            case BLOCK_TYPE_IF:
            case BLOCK_TYPE_IF_ELSE:
            case BLOCK_TYPE_IE_ELSEIF:
            default:
                word = "if";
                break;
        }

        // drawableリソースID文字列の生成
        String resourceStr = PREFIX + GIMMICK_DELIMITER_WORD + word;

        // drawableリソースID生成
        Resources resources = context.getResources();
        String packageName = context.getPackageName();

        int drawableId = resources.getIdentifier(resourceStr, "drawable", packageName);
        return context.getDrawable(drawableId);
    }
}
