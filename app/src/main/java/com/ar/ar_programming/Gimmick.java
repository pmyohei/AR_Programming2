package com.ar.ar_programming;

import static com.ar.ar_programming.Common.TUTORIAL_FINISH;
import static com.ar.ar_programming.GimmickManager.BLOCK_ACTION_ATTACHED_POS;
import static com.ar.ar_programming.GimmickManager.BLOCK_ACTION_DATA_POS;
import static com.ar.ar_programming.GimmickManager.BLOCK_ACTION_POS;
import static com.ar.ar_programming.GimmickManager.BLOCK_CONDITION_POS;
import static com.ar.ar_programming.GimmickManager.BLOCK_TYPE_POS;
import static com.ar.ar_programming.GimmickManager.NODE_NAME_REPLACE;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.google.ar.sceneform.math.Vector3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.regex.Pattern;

public class Gimmick {

    //---------------------------
    // 定数
    //---------------------------
    public static final int VOLUME_LIMIT_NONE = 0;
    public static final int NO_DATA_GOAL_ANGLE = 0;
    public static final int NO_USABLE_LIMIT_NUM = -1;

    //---------------------------
    // フィールド変数
    //---------------------------
    public Context mContext;
    public int mTutorial;       // xmlからは取得しない。内部保存データがソース。

    //-----------------
    // ギミックプロパティ
    //-----------------
    public String name;
    public String successCondition;
    public String successRemoveTarget;
    public String character;        // 動物 or 乗り物
    public String stageGlb;
    // キャラクター
    public String characterGlb;
    public Vector3 characterPositionVec;
    public float characterAngle;
    // ゴール
    public ArrayList<Integer> goalExplanationIdList;
    public String goalGlb;
    public Vector3 goalPositionVec;
    public float goalAngle;
    public String goalName;
    // オブジェクト
    public boolean objectPositionRandom;
    public ArrayList<String> objectGlbList;
    public ArrayList<Integer> objectNumList;
    public ArrayList<String> objectNameList;
    public ArrayList<String> objectReplaceNameList;
    public ArrayList<String> objectReplaceGlbList;
    public ArrayList<Vector3> objectPositionVecList;
    public ArrayList<Float> objectAngleList;
    public String objectObstacle;
    // 敵
    public boolean enemyNumRandom;
    public ArrayList<String> enemyGlbList;
    public ArrayList<Integer> enemyNumList;
    public ArrayList<String> enemyKindList;
    public ArrayList<Vector3> enemyPositionVecList;
    public Vector3 enemyEndPositionVec;
    // ブロック
    public ArrayList<XmlBlockInfo> xmlBlockInfoList;
    public ArrayList<String> blockList;

    /*
     * ブロックプロパティ情報
     */
    public static class XmlBlockInfo {

        //-----------
        // ブロック情報
        //-----------
        public String type;             // ブロック種別（Single, Loop, If,,,）
        public String action;           // アクション／条件：動作
        public String targetNode_1;     // 対象Node1
        public String targetNode_2;     // 対象Node2 elseif
        public int fixVolume;           // 固定処理量

        //-----------
        // 使用可能数
        //-----------
        public int usableLimitNum;      // 使用可能上限
        public int usableNum;           // 使用可能残数

        /*
         * コンストラクタ
         */
        public XmlBlockInfo() {
            // 初期状態は「スタートブロック」扱いとする
            type = GimmickManager.BLOCK_TYPE_START;
            // 空
            action = "";
            targetNode_1 = "";
            targetNode_2 = "";
            // 固定処理量なし
            fixVolume = VOLUME_LIMIT_NONE;
            // 使用可能数なし
            usableLimitNum = NO_USABLE_LIMIT_NUM;
            usableNum = NO_USABLE_LIMIT_NUM;
        }
    }

    /*
     * コンストラクタ
     */
    public Gimmick(Context context) {

        mContext = context;

        //-------------------
        // チュートリアル
        //-------------------
        mTutorial = TUTORIAL_FINISH;

        //-------------------
        // リスト関連
        //-------------------
        goalExplanationIdList = new ArrayList<>();

        objectGlbList = new ArrayList<>();
        objectNumList = new ArrayList<>();
        objectNameList = new ArrayList<>();
        objectReplaceNameList = new ArrayList<>();
        objectReplaceGlbList = new ArrayList<>();
        objectPositionVecList = new ArrayList<>();
        objectAngleList = new ArrayList<>();

        enemyGlbList = new ArrayList<>();
        enemyNumList = new ArrayList<>();
        enemyKindList = new ArrayList<>();
        enemyPositionVecList = new ArrayList<>();

        xmlBlockInfoList = new ArrayList<>();
        blockList = new ArrayList<>();
    }

    /*
     * ギミックのチュートリアル値を設定
     */
    public void setTutorial(int tutorial) {
        Log.i("ステージ選択", "setTutorial()=" + tutorial);
        mTutorial = tutorial;
    }

    /*
     * ギミックxmlの値デリミタで文字列を分割
     *   想定デリミタ："半角空白（0文字以上）,半角空白（0文字以上）"
     *   例）","、", "、" ,"、" , "
     */
    private String[] splitGimmickValueDelimiter(String str) {
        return str.split(GimmickManager.GIMMICK_DELIMITER_INFO);
    }

    /*
     * ギミックxmlの位置情報用デリミタで文字列を分割
     *   想定デリミタ："半角空白（0文字以上）:半角空白（0文字以上）"
     *   例）":"、": "、" :"、" : "
     */
    private String[] splitGimmickPositionDelimiter(String str) {

        //----------------------
        // dataなしの場合
        //----------------------
        // 明示的に異常フォーマットとする
        if (str == null) {
            return new String[]{""};
        }

        // 分割
        return str.split(GimmickManager.GIMMICK_DELIMITER_COORDINATE);
    }

    /*
     * ギミックxmlの値ブロック情報デリミタで文字列を分割
     *   想定デリミタ："_"
     *   例）"_"
     */
    public static String[] splitGimmickWordDelimiter(String str) {
        return str.split(GimmickManager.GIMMICK_DELIMITER_WORD);
    }

    /*
     * ブロック内容を解析して、xml情報として設定
     */
    private void setXmlBlockInfo(String[] blockSplit, XmlBlockInfo xmlBlockInfo) {

        //----------------
        // ブロック種別
        //----------------
        xmlBlockInfo.type = blockSplit[BLOCK_TYPE_POS];

        //------------------------------------
        // ブロック種別に応じて、設定を切り分け
        //------------------------------------
        switch (xmlBlockInfo.type) {

            //----------------------
            // 実行ブロック
            //----------------------
            case GimmickManager.BLOCK_TYPE_EXE:
                setXmlExeBlockInfo(blockSplit, xmlBlockInfo);
                break;

            //----------------------
            // ネストブロック（条件１つ）
            //----------------------
            case GimmickManager.BLOCK_TYPE_LOOP:
            case GimmickManager.BLOCK_TYPE_IF:
            case GimmickManager.BLOCK_TYPE_IF_ELSE:
                setXmlOneConditionBlockInfo(blockSplit, xmlBlockInfo);
                break;

            //----------------------
            // ネストブロック（条件２つ）
            //----------------------
            case GimmickManager.BLOCK_TYPE_IE_ELSEIF:
                setXmlTwoConditionBlockInfo(blockSplit, xmlBlockInfo);
                break;

            default:
                break;
        }

        //----------------
        // 全ブロック共通
        //----------------
        // 使用可能上限数
        if (blockSplit.length > GimmickManager.BLOCK_USABLE_NUM_POS) {
            int usableLimitNum = Integer.parseInt(blockSplit[GimmickManager.BLOCK_USABLE_NUM_POS]);

            xmlBlockInfo.usableLimitNum = usableLimitNum;
            xmlBlockInfo.usableNum = usableLimitNum;
        }
    }

    /*
     * ブロック内容を解析して、xml情報として設定：Exeブロック
     */
    private void setXmlExeBlockInfo(String[] blockSplit, XmlBlockInfo xmlBlockInfo) {

        //---------------------------
        // アクション
        //---------------------------
        // アクション情報を分割
        // 例）「forward-100」   → [0]forward  [1]100
        // 例）「pickup-sweets」 → [0]pickup  [1]sweets
        String[] actionAndData = blockSplit[BLOCK_ACTION_DATA_POS].split(GimmickManager.GIMMICK_DELIMITER_CONDITION);

        xmlBlockInfo.action = actionAndData[BLOCK_ACTION_POS];

        //---------------------------
        // アクション付随情報
        //---------------------------
        if (actionAndData.length <= BLOCK_ACTION_ATTACHED_POS) {
            // 付随情報なしなら、終了
            return;
        }

        // アクション付随情報が数字で構成されているかどうかで、
        // 「固定処理量」か「対象Node」か判定
        String actionAttachedData = actionAndData[BLOCK_ACTION_ATTACHED_POS];

        // 正規表現で数字判定
        Pattern pattern = Pattern.compile("^[0-9]+$|-[0-9]+$");
        boolean isVolume = pattern.matcher(actionAttachedData).matches();
        if (isVolume) {
            // 「固定処理量」
            xmlBlockInfo.fixVolume = Integer.parseInt(actionAttachedData);
        } else {
            // 「対象Node」
            xmlBlockInfo.targetNode_1 = actionAttachedData;
        }
    }

    /*
     * ブロック内容を解析して、xml情報として設定：条件１つブロック
     */
    private void setXmlOneConditionBlockInfo(String[] blockSplit, XmlBlockInfo xmlBlockInfo) {

        //-------------
        // 条件文分割
        //-------------
        // 条件文情報を「条件：動作」と「条件：対象①」に分割
        // 例)「facing-eatable」 ⇒ [0]facing  [1]eatable
        String condition = blockSplit[BLOCK_CONDITION_POS];
        String[] conditionData = condition.split(GimmickManager.GIMMICK_DELIMITER_CONDITION);

        // 設定
        xmlBlockInfo.action = conditionData[GimmickManager.BLOCK_CONDITION_ACTION_POS];
        xmlBlockInfo.targetNode_1 = conditionData[GimmickManager.BLOCK_CONDITION_OBJECT_POS];
    }

    /*
     * ブロック内容を解析して、xml情報として設定：条件２つブロック
     */
    private void setXmlTwoConditionBlockInfo(String[] blockSplit, XmlBlockInfo xmlBlockInfo) {

        //-------------
        // 条件文分割
        //-------------
        // 条件文情報を「条件：動作」と「条件：対象①」「条件：対象②」に分割
        // 例)「front-eatable-poison」 ⇒ [0]facing  [1]eatable  [2]poison
        String condition = blockSplit[BLOCK_CONDITION_POS];
        String[] conditionData = condition.split(GimmickManager.GIMMICK_DELIMITER_CONDITION);

        // 設定
        xmlBlockInfo.action = conditionData[GimmickManager.BLOCK_CONDITION_ACTION_POS];
        xmlBlockInfo.targetNode_1 = conditionData[GimmickManager.BLOCK_CONDITION_OBJECT_POS];
        xmlBlockInfo.targetNode_2 = conditionData[GimmickManager.BLOCK_CONDITION_ELSEIF_OBJECT_POS];
    }

    /*
     * キャラクター座標を設定
     */
    public void setCharacterPosition(String position) {

        String[] positions = splitGimmickPositionDelimiter(position);

        // 正常フォーマットの場合
        if (positions.length == 3) {
            // 位置形式に変換
            float x = Float.parseFloat(positions[0]);
            float y = Float.parseFloat(positions[1]);
            float z = Float.parseFloat(positions[2]);
            characterPositionVec = new Vector3(x, y, z);
            return;
        }

        // フォーマット異常の場合
        characterPositionVec = new Vector3(0f, 0f, 0f);
    }

    /*
     * キャラクターモデルの角度を設定
     */
    public void setCharacterAngle(String angle) {

        // 設定なしなら、未設定用値を設定して終了
        if ((angle == null) || angle.isEmpty()) {
            characterAngle = NO_DATA_GOAL_ANGLE;
            return;
        }

        // float変換
        characterAngle = Float.parseFloat(angle);
    }

    /*
     * ゴール座標を設定
     */
    public void setGoalPosition(String position) {

        String[] strs = splitGimmickPositionDelimiter(position);

        // 正常フォーマットの場合
        if (strs.length == 3) {
            // 位置形式に変換
            float x = Float.parseFloat(strs[0]);
            float y = Float.parseFloat(strs[1]);
            float z = Float.parseFloat(strs[2]);
            goalPositionVec = new Vector3(x, y, z);
            return;
        }

        // フォーマット異常の場合
        goalPositionVec = new Vector3(1f, 1f, 1f);
    }

    /*
     * ゴールモデルの角度を設定
     */
    public void setGoalAngle(String angle) {

        // 設定なしなら、未設定用値を設定して終了
        if ((angle == null) || angle.isEmpty()) {
            goalAngle = NO_DATA_GOAL_ANGLE;
            return;
        }

        // float変換
        goalAngle = Float.parseFloat(angle);
    }

    /*
     * ゴールNode名を設定
     */
    public void setGoalName(String name) {

        // 設定なしなら、未設定用値を設定して終了
        if (name == null) {
            goalName = GimmickManager.NODE_NAME_NONE;
            return;
        }

        goalName = name;
    }

    /*
     * ゴール説明文言リストの設定
     *   文言からstringIDを生成
     */
    public void setGoalExplanation(String goalExplanation) {
        Resources resources = mContext.getResources();

        goalExplanationIdList.clear();

        // ゴール説明ID文字列をintに変換して、リストに格納
        String[] strs = splitGimmickValueDelimiter(goalExplanation);
        for (String word : strs) {
            // 文字列からID値に変換
            int stringId = resources.getIdentifier(word, "strings-stage-guide", mContext.getPackageName());
            goalExplanationIdList.add(stringId);
        }
    }

    /*
     * オブジェクト名を設定
     */
    public void setObjectGlb(String objectGlb) {

        // プロパティなし
        if (objectGlb == null) {
            return;
        }

        // オブジェクト名を分割
        String[] strs = splitGimmickValueDelimiter(objectGlb);

        // リスト生成
        objectGlbList.clear();
        Collections.addAll(objectGlbList, strs);
    }

    /*
     * オブジェクト数を設定
     */
    public void setObjectNum(String objectNum) {

        // プロパティなし
        if (objectNum == null) {
            return;
        }

        // オブジェクト名を分割
        String[] strs = splitGimmickValueDelimiter(objectNum);

        // リスト生成
        objectNumList.clear();
        for (String num : strs) {
            objectNumList.add(Integer.parseInt(num));
        }
    }

    /*
     * オブジェクト名を設定
     */
    public void setObjectName(String objectName) {

        // プロパティなし
        if (objectName == null) {
            return;
        }

        // オブジェクト名を分割
        String[] strs = splitGimmickValueDelimiter(objectName);

        // リスト生成
        objectNameList.clear();
        Collections.addAll(objectNameList, strs);
    }

    /*
     * オブジェクト名／glb（置き換え後）を設定
     */
    public void setObjectReplaceInfo(String objectReplaceName, String objectReplaceGlb) {

        // プロパティなし
        if ((objectReplaceName == null) || (objectReplaceGlb == null)) {
            return;
        }

        // リストクリア
        objectReplaceNameList.clear();
        objectReplaceGlbList.clear();

        // オブジェクト名／glb（置き換え後）を情報数で分割
        // 例）「mushroomPoison-carrot, choco-donuts」 → [0]mushroomPoison-carrot  [1]choco-donuts
        String[] nameList = splitGimmickValueDelimiter(objectReplaceName);
        String[] glbList = splitGimmickValueDelimiter(objectReplaceGlb);

        Random random = new Random();

        //------------------
        // 置き換え情報の決定
        //------------------
        // 置き換え前のNode数
        int replaceInfoNum = getReplaceNum();
        // 置き換え後の候補情報リスト 参照用Index
        int afterReplaceIndex = 0;
        // 置き換え後の候補情報リスト 最後尾Index
        int afterReplaceLastIndex = nameList.length - 1;

        for (int replaceCount = 0; replaceCount < replaceInfoNum; replaceCount++) {

            Log.i("置き換え", "replaceCount=" + replaceCount);

            //--------------------
            // 置き換え後情報
            //--------------------
            // 置き換え候補の取得
            // name例)「mushroomPoison-carrot」
            //  glb例)「xxx/xxx/mushroom_poison.glb-xxx/xxx/carrot.glb」
            String name = nameList[afterReplaceIndex];
            String glb = glbList[afterReplaceIndex];

            // 置き換え候補に分割
            // 例）「mushroomPoison-carrot」 →  [0]mushroomPoison  [1]carrot
            String[] nameCandidate = name.split(GimmickManager.GIMMICK_DELIMITER_CONDITION);
            String[] glbCandidate = glb.split(GimmickManager.GIMMICK_DELIMITER_CONDITION);

            // 置き換え候補の中から、ランダムにいずれかを選択
            int candidateNum = nameCandidate.length;
            int select = random.nextInt(candidateNum);

            // 置き換え後情報リストに追加
            objectReplaceNameList.add(nameCandidate[select]);
            objectReplaceGlbList.add(glbCandidate[select]);

            //----------------------------------------
            // 置き換え後の候補情報リスト 参照Indexを更新
            //----------------------------------------
            if( afterReplaceLastIndex > afterReplaceIndex ){
                afterReplaceIndex++;
            }
        }
    }

    /*
     * replace数（置き換え前Node数）取得
     */
    public int getReplaceNum() {

        // リスト上の"replace"の位置を取得
        int replacePos = 0;
        for( String name: objectNameList ){
            if( name.equals( NODE_NAME_REPLACE ) ){
                break;
            }
            replacePos++;
        }

        // "replace"の生成数を返す
        return objectNumList.get(replacePos);
    }

    /*
     * オブジェクト座標のランダムの有無
     */
    public void setObjectPositionRandom(String random) {

        // プロパティなし
        if (random == null) {
            objectPositionRandom = false;
            return;
        }

        objectPositionRandom = random.equals(GimmickManager.GIMMICK_TRUE);
    }

    /*
     * オブジェクト座標を設定
     */
    public void setObjectPositionVecList(String position) {

        // プロパティなし
        if (position == null) {
            return;
        }

        // 位置情報データ毎に分割
        // 例)"0:0:0, 1:1:1" → 「0:0:0」「1:1:1」
        String[] valueSplit = splitGimmickValueDelimiter(position);

        //-----------------------
        // 位置情報分、リストに格納
        //-----------------------
        for (String posStr : valueSplit) {

            // 位置情報を座標毎に分割
            // 例)「0:0:0」 → 「0」「0」「0」
            String[] posSplit = splitGimmickPositionDelimiter(posStr);

            // 正常フォーマットの場合
            Vector3 pos;
            if (posSplit.length == 3) {
                // 位置形式に変換
                float x = Float.parseFloat(posSplit[0]);
                float y = Float.parseFloat(posSplit[1]);
                float z = Float.parseFloat(posSplit[2]);
                pos = new Vector3(x, y, z);
            } else {
                // フォーマット異常の場合
                pos = new Vector3(0, 0, 0);
            }

            objectPositionVecList.add(pos);
        }
    }

    /*
     * オブジェクト角度を設定
     */
    public void setObjectAngle(String objectAngle) {

        // プロパティなし
        if (objectAngle == null) {
            return;
        }

        // オブジェクト名を分割
        String[] strs = splitGimmickValueDelimiter(objectAngle);

        // リスト生成
        objectAngleList.clear();
        for (String angleOrg : strs) {
            Float angle = Float.parseFloat(angleOrg);
            objectAngleList.add(angle);
        }
    }

    /*
     * オブジェクト（障害物名）を設定
     */
    public void setObjectObstacle(String name) {

        // 設定なし
        if (name == null) {
            return;
        }

        objectObstacle = name;
    }

    /*
     * 敵名を設定
     */
    public void setEnemyGlb(String enemyGlb) {

        // プロパティなし
        if (enemyGlb == null) {
            return;
        }

        // 敵名を分割
        String[] strs = splitGimmickValueDelimiter(enemyGlb);

        // リスト生成
        enemyGlbList.clear();
        Collections.addAll(enemyGlbList, strs);
    }

    /*
     * 敵数を設定
     */
    public void setEnemyNum(String enemyNum) {

        // プロパティなし
        if (enemyNum == null) {
            return;
        }

        // 敵名を分割
        String[] strs = splitGimmickValueDelimiter(enemyNum);

        // リスト生成
        enemyNumList.clear();
        for (String num : strs) {
            enemyNumList.add(Integer.parseInt(num));
        }
    }

    /*
     * 敵数ランダムの有無を設定
     */
    public void setEnemyNumRandom(String random) {

        // プロパティなし
        if (random == null) {
            return;
        }

        // ランダム有無を反映
        enemyNumRandom = random.equals(GimmickManager.GIMMICK_TRUE);
    }

    /*
     * 敵種別を設定
     */
    public void setEnemyKind(String enemyKind) {

        // プロパティなし
        if (enemyKind == null) {
            return;
        }

        // 敵名を分割
        String[] strs = splitGimmickValueDelimiter(enemyKind);

        // リスト生成
        enemyKindList.clear();
        Collections.addAll(enemyKindList, strs);
    }

    /*
     * 敵座標を設定
     */
    public void setEnemyPositionVecList(String position) {

        // プロパティなし
        if (position == null) {
            return;
        }

        // 位置情報データ毎に分割
        // 例)"0:0:0, 1:1:1" → 「0:0:0」「1:1:1」
        String[] valueSplit = splitGimmickValueDelimiter(position);

        //-----------------------
        // 位置情報分、リストに格納
        //-----------------------
        for (String posStr : valueSplit) {

            // 位置情報を座標毎に分割
            // 例)「0:0:0」 → 「0」「0」「0」
            String[] posSplit = splitGimmickPositionDelimiter(posStr);

            // 正常フォーマットの場合
            Vector3 pos;
            if (posSplit.length == 3) {
                // 位置形式に変換
                float x = Float.parseFloat(posSplit[0]);
                float y = Float.parseFloat(posSplit[1]);
                float z = Float.parseFloat(posSplit[2]);
                pos = new Vector3(x, y, z);
            } else {
                // フォーマット異常の場合
                pos = new Vector3(0, 0, 0);
            }

            enemyPositionVecList.add(pos);
        }
    }

    /*
     * 敵移動終点座標を設定
     */
    public void setEnemyEndPosition(String position) {

        String[] strs = splitGimmickPositionDelimiter(position);

        // 正常フォーマットの場合
        if (strs.length == 3) {
            // 位置形式に変換
            float x = Float.parseFloat(strs[0]);
            float y = Float.parseFloat(strs[1]);
            float z = Float.parseFloat(strs[2]);
            enemyEndPositionVec = new Vector3(x, y, z);
            return;
        }

        // フォーマット異常の場合
        enemyEndPositionVec = new Vector3(1f, 1f, 1f);
    }

    /*
     * ギミックで使用可能なブロックリストを設定
     */
    public void setBlock(String block) {
        //----------------------
        // 各ブロック情報をリスト化
        //----------------------
        // デリミタで分割して配列化
        // 例）"single_forward, single_eat" → [0]single_forward  [1]single_eat
        String[] blocks = splitGimmickValueDelimiter(block);

        // リスト生成
        blockList.clear();
        Collections.addAll(blockList, blocks);

        //----------------------
        // 各ブロック情報を詳細化
        //----------------------
        // xmlブロック情報リストを生成する
        setXmlBlockInfoList(blockList);
    }

    /*
     * ギミックで使用可能なブロックリストを設定
     */
    private void setXmlBlockInfoList(ArrayList<String> blockList) {

        //---------------------
        // ブロック情報分繰り返し
        //---------------------
        for (String blockItemStr : blockList) {

            // xml情報新規生成
            XmlBlockInfo xmlBlockInfo = new XmlBlockInfo();

            // ブロック文字列を分割
            // 例)「single_rotate-right_1」⇒「single」「rotate-right」「1」
            String[] blockSplit = Gimmick.splitGimmickWordDelimiter(blockItemStr);
            // ブロック文字列をxml情報として解析して設定
            setXmlBlockInfo(blockSplit, xmlBlockInfo);

            // リストに追加
            xmlBlockInfoList.add(xmlBlockInfo);
        }
    }

    /*
     * ギミックで使用可能なブロックリストを設定
     */
    public int getBlockXmlIndex(XmlBlockInfo xmlBlockInfo) {

        int index = 0;
        for( XmlBlockInfo item: xmlBlockInfoList ){

            // 指定された情報と同じブロック情報
            if( (xmlBlockInfo.type.equals( item.type )) &&
                (xmlBlockInfo.action.equals( item.action )) &&
                (xmlBlockInfo.targetNode_1.equals( item.targetNode_1 )) ){

                return index;
            }

            index++;
        }

        return -1;
    }
}
