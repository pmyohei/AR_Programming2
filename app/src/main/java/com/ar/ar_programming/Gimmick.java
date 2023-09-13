package com.ar.ar_programming;

import static com.ar.ar_programming.GimmickManager.BLOCK_CONDITION_ARRIVAL;
import static com.ar.ar_programming.GimmickManager.BLOCK_CONDITION_COLLECT;
import static com.ar.ar_programming.GimmickManager.BLOCK_CONDITION_FACING;
import static com.ar.ar_programming.GimmickManager.BLOCK_CONTENTS_POS;
import static com.ar.ar_programming.GimmickManager.BLOCK_TYPE_POS;
import static com.ar.ar_programming.GimmickManager.BLOCK_VALUE_LIMIT_POS;

import android.content.Context;
import android.content.res.Resources;

import com.ar.ar_programming.process.Block;
import com.google.ar.sceneform.math.Vector3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Gimmick {

    //---------------------------
    // 定数
    //---------------------------
    // 異常値
    public static final int VOLUME_LIMIT_NONE = 0;
    public static final int NO_DATA_GOAL_ANGLE = 0;

    //---------------------------
    // フィールド変数
    //---------------------------
    public Context mContext;

    //-----------------
    // ギミックプロパティ
    //-----------------
    public String successCondition;
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
    // オブジェクト
    public boolean objectPositionRandom;
    public ArrayList<String> objectGlbList;
    public ArrayList<Integer> objectNumList;
    public ArrayList<String> objectKindList;
    public ArrayList<Vector3> objectPositionVecList;
    public ArrayList<Float> objectAngleList;
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

    //-----------------
    // 対応表
    //-----------------
    // 実行ブロック「実行内容」　：　ブロック文ID
//    public Map<String, Integer> Map_blockExe__statementId;
    // 条件ブロック」条件（動作）」　：　条件文ID
    public Map<String, Integer> Map_blockConditionMotion__statementId;
    // glbファイル　：　(具体的な)Node名
    public Map<String, Integer> Map_glb__nodeName;




    /*
     * ブロックプロパティ情報
     */
    public class XmlBlockInfo {

        //---------------
        // 共通
        //---------------
        public int type;                // ブロック種別（Single, Loop, If,,,）
        public String contents;         // ブロック内容（「forward」、「facing-goal」、、）
        public int drawableId;          // ブロックイメージID
        public int statementId;         // ブロック文字列ID
        public int nodeNameId;          // ブロック文字列内Node名ID

        //---------------
        // 実行ブロックのみ
        //---------------
        public int volumeLimit;         // 処理量制限値
        public boolean existsVolume;    // 処理量があるブロックかどうか（例えば、「前へ進む」は”あり”。「食べる」であれば”なし”。）

        //---------------
        // 制御ブロックのみ
        //---------------
        public String conditionMotion;  // 条件文：動作（「facing」、、、）
        public String conditionObject;  // 条件文：対象（「eatable」、、、）

        //---------------------------
        // if elseif else ブロックのみ
        //---------------------------
        // else if側の情報
        public String conditionObjectElseIf;    // 条件文：対象（「poison」、、、）
        public int statementElseIfId;           // ブロック文字列ID（else if 条件文）
        public int nodeNameElseIfId;            // ブロック文字列内Node名ID

        /*
         * コンストラクタ
         */
        public XmlBlockInfo() {
            nodeNameId = -1;
        }
    }

    /*
     * コンストラクタ
     */
    public Gimmick(Context context) {

        mContext = context;

        goalExplanationIdList = new ArrayList<>();

        objectGlbList = new ArrayList<>();
        objectNumList = new ArrayList<>();
        objectKindList = new ArrayList<>();
        objectPositionVecList = new ArrayList<>();
        objectAngleList = new ArrayList<>();

        enemyGlbList = new ArrayList<>();
        enemyNumList = new ArrayList<>();
        enemyKindList = new ArrayList<>();
        enemyPositionVecList = new ArrayList<>();

        blockList = new ArrayList<>();
        xmlBlockInfoList = new ArrayList<>();


        //----------------------
        // 対応表
        //----------------------
        Map_blockConditionMotion__statementId = new HashMap<String, Integer>() {
            {
                put(BLOCK_CONDITION_FACING, R.string.block_contents_loop_facing);
                put(BLOCK_CONDITION_COLLECT, R.string.block_contents_loop_collect);
                put(BLOCK_CONDITION_ARRIVAL, R.string.block_contents_loop_arrival);
            }
        };

        Map_glb__nodeName = new HashMap<String, Integer>() {
            {
                put("house.glb", R.string.node_house);
                put("fox_and_tree.glb", R.string.node_squirrel);
                put("carrot.glb", R.string.node_carrot);
                put("mushroom_poison.glb", R.string.node_mushroom_poison);
                put("bee.glb", R.string.node_bee);
                put("honey.glb", R.string.node_honey);
                put("sports_car.glb", R.string.node_sports_car);
                put("signboard_stop.glb", R.string.node_signboard_stop);
            }
        };

    }

    /*
     * ギミックxmlの値デリミタで文字列を分割
     *   想定デリミタ："半角空白（0文字以上）,半角空白（0文字以上）"
     *   例）","、", "、" ,"、" , "
     */
    private String[] splitGimmickValueDelimiter(String str) {
        return str.split(" *, *");
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
        return str.split(" *: *");
    }

    /*
     * ギミックxmlの値ブロック情報デリミタで文字列を分割
     *   想定デリミタ："_"
     *   例）"_"
     */
    public static String[] splitGimmickBlockDelimiter(String str) {
        return str.split("_");
    }

    /*
     * ブロック種別変換
     *   blockプロパティの値の文字列をブロック種別に変換
     *  @para：例)「process」「loop」など
     */
    private int convertBlockType(String blockTypeStr) {

        switch (blockTypeStr) {
            case GimmickManager.BLOCK_TYPE_SINGLE:
                return Block.PROCESS_TYPE_SINGLE;

            case GimmickManager.BLOCK_TYPE_LOOP:
                return Block.PROCESS_TYPE_LOOP;

            case GimmickManager.BLOCK_TYPE_IF:
                return Block.PROCESS_TYPE_IF;

            case GimmickManager.BLOCK_TYPE_IF_ELSE:
                return Block.PROCESS_TYPE_IF_ELSE;

            case GimmickManager.BLOCK_TYPE_IE_ELSEIF:
                return Block.PROCESS_TYPE_IF_ELSEIF_ELSE;
        }

        return Block.PROCESS_TYPE_SINGLE;
    }

    /*
     * ブロック内容を解析して、xml情報として設定
     */
    private void setXmlBlockInfo(String[] blockSplit, XmlBlockInfo xmlBlockInfo) {

        // ブロック種別
        final int blockType = convertBlockType(blockSplit[BLOCK_TYPE_POS]);
        xmlBlockInfo.type = blockType;

        String blockContents = blockSplit[BLOCK_CONTENTS_POS];

        switch (blockType) {
            case Block.PROCESS_TYPE_SINGLE:
                setXmlSingleBlockInfo(blockSplit, xmlBlockInfo);
                break;

            case Block.PROCESS_TYPE_LOOP:
                setXmlLoopBlockInfo(blockContents, xmlBlockInfo);
                break;

            case Block.PROCESS_TYPE_IF:
                setXmlIfBlockInfo(blockContents, xmlBlockInfo);
                break;

            case Block.PROCESS_TYPE_IF_ELSE:
                setXmlIfElseBlockInfo(blockContents, xmlBlockInfo);
                break;

            case Block.PROCESS_TYPE_IF_ELSEIF_ELSE:
            default:
                setXmlIfElseIfBlockInfo(blockContents, xmlBlockInfo);
                break;
        }
    }

    /*
     * ブロック内容を解析して、xml情報として設定：Single処理用
     */
    private void setXmlSingleBlockInfo(String[] blockSplit, XmlBlockInfo xmlBlockInfo) {

        int drawableId;
        int statementId;
        int nodeNameId = -1;
        boolean existsVolume = true;

        String blockContents = blockSplit[BLOCK_CONTENTS_POS];
        switch (blockContents) {
            case GimmickManager.BLOCK_EXE_FORWARD:
                drawableId = R.drawable.baseline_block_forward_24;
                statementId = R.string.block_contents_forward;
                break;

            case GimmickManager.BLOCK_EXE_BACK:
                drawableId = R.drawable.baseline_block_back_24;
                statementId = R.string.block_contents_back;
                break;

            case GimmickManager.BLOCK_EXE_ROTATE_RIGHT:
                drawableId = R.drawable.baseline_block_rotate_right_24;
                statementId = R.string.block_contents_rorate_right;
                break;

            case GimmickManager.BLOCK_EXE_ROTATE_LEFT:
                drawableId = R.drawable.baseline_block_rotate_left_24;
                statementId = R.string.block_contents_rorate_left;
                break;

            case GimmickManager.BLOCK_EXE_EAT:
                drawableId = R.drawable.baseline_eat_24;
                statementId = R.string.block_contents_eat;
                nodeNameId = getObjectNameInBlock( "eatable", objectKindList ,objectGlbList );
                existsVolume = false;
                break;

            case GimmickManager.BLOCK_EXE_THROW_AWAY:
                drawableId = R.drawable.baseline_throw_away_24;
                statementId = R.string.block_contents_throw_away;
                nodeNameId = getObjectNameInBlock( "poison", objectKindList ,objectGlbList );
                existsVolume = false;
                break;

            case GimmickManager.BLOCK_EXE_ATTACK:
                drawableId = R.drawable.baseline_attack_24;
                statementId = R.string.block_contents_attack;
                nodeNameId = getObjectNameInBlock( "enemy", objectKindList ,objectGlbList );
                existsVolume = false;
                break;

            default:
                drawableId = R.drawable.baseline_block_forward_24;
                statementId = R.string.block_contents_forward;
                break;
        }

        // 処理量制限情報取得
        int valueSettingLimit = getBlockValueSettingLimit(blockSplit);

        //--------------------
        // 設定
        //--------------------
        xmlBlockInfo.contents = blockContents;
        xmlBlockInfo.drawableId = drawableId;
        xmlBlockInfo.statementId = statementId;
        xmlBlockInfo.nodeNameId = nodeNameId;
        xmlBlockInfo.volumeLimit = valueSettingLimit;
        xmlBlockInfo.existsVolume = existsVolume;
    }

    /*
     * ブロック内容を解析して、xml情報として設定：Loop
     */
    private void setXmlLoopBlockInfo(String blockContents, XmlBlockInfo xmlBlockInfo) {

        int statementId;
        int nodeNameId = -1;

        //-------------
        // 条件文分割
        //-------------
        String[] conditionData = blockContents.split(GimmickManager.GIMMICK_DELIMITER_CONDITION);
        String conditionMotion = conditionData[GimmickManager.BLOCK_CONDITION_MOTION_POS];
        String conditionObject = conditionData[GimmickManager.BLOCK_CONDITION_OBJECT_POS];

        //-------------
        // 条件文
        //-------------
        // 動作に対応する条件文を取得
        statementId = Map_blockConditionMotion__statementId.get( conditionMotion );

        // 対象に対応するNode名を取得
        switch ( conditionObject ) {
            case GimmickManager.NODE_NAME_GOAL:
                nodeNameId = getObjectNameInBlock(goalGlb);
                break;

            case GimmickManager.NODE_NAME_ENEMY:
            case GimmickManager.NODE_NAME_EATABLE:
                nodeNameId = getObjectNameInBlock( conditionObject, objectKindList ,objectGlbList );
                break;
        }

        //--------------------
        // 設定
        //--------------------
        xmlBlockInfo.contents = blockContents;
        xmlBlockInfo.drawableId = R.drawable.baseline_block_loop_24;
        xmlBlockInfo.statementId = statementId;
        xmlBlockInfo.nodeNameId = nodeNameId;
        xmlBlockInfo.conditionMotion = conditionMotion;
        xmlBlockInfo.conditionObject = conditionObject;
    }

    /*
     * ブロック内容を解析して、xml情報として設定：If
     */
    private void setXmlIfBlockInfo(String blockContents, XmlBlockInfo xmlBlockInfo) {

        int statementId;
        int nodeNameId = -1;

        //-------------
        // 条件文分割
        //-------------
        String[] conditionData = blockContents.split(GimmickManager.GIMMICK_DELIMITER_CONDITION);
        String conditionMotion = conditionData[GimmickManager.BLOCK_CONDITION_MOTION_POS];
        String conditionObject = conditionData[GimmickManager.BLOCK_CONDITION_OBJECT_POS];

        //-------------
        // 条件文
        //-------------
        // 動作に対応する条件文を取得
        statementId = Map_blockConditionMotion__statementId.get( conditionMotion );

        // 対象に対応するNode名を取得
        switch ( conditionObject ) {
            case GimmickManager.NODE_NAME_ENEMY:
            case GimmickManager.NODE_NAME_EATABLE:
            case GimmickManager.NODE_NAME_POISON:
                nodeNameId = getObjectNameInBlock( conditionObject, objectKindList ,objectGlbList );
                break;
        }

        //--------------------
        // 設定
        //--------------------
        xmlBlockInfo.drawableId = R.drawable.baseline_block_if_24;
        xmlBlockInfo.contents = blockContents;
        xmlBlockInfo.statementId = statementId;
        xmlBlockInfo.nodeNameId = nodeNameId;
        xmlBlockInfo.conditionMotion = conditionMotion;
        xmlBlockInfo.conditionObject = conditionObject;
    }

    /*
     * ブロック内容を解析して、xml情報として設定：IfElse
     */
    private void setXmlIfElseBlockInfo(String blockContents, XmlBlockInfo xmlBlockInfo) {

        int statementId;
        int nodeNameId = -1;

        //-------------
        // 条件文分割
        //-------------
        String[] conditionData = blockContents.split(GimmickManager.GIMMICK_DELIMITER_CONDITION);
        String conditionMotion = conditionData[GimmickManager.BLOCK_CONDITION_MOTION_POS];
        String conditionObject = conditionData[GimmickManager.BLOCK_CONDITION_OBJECT_POS];

        //-------------
        // 条件文
        //-------------
        // 動作に対応する条件文を取得
        statementId = Map_blockConditionMotion__statementId.get( conditionMotion );

        // 対象に対応するNode名を取得
        switch ( conditionObject ) {
            case GimmickManager.NODE_NAME_ENEMY:
            case GimmickManager.NODE_NAME_EATABLE:
            case GimmickManager.NODE_NAME_POISON:
                nodeNameId = getObjectNameInBlock( conditionObject, objectKindList ,objectGlbList );
                break;
            case GimmickManager.NODE_NAME_NOTHING:
                break;
        }

        //--------------------
        // 設定
        //--------------------
        xmlBlockInfo.drawableId = R.drawable.baseline_block_if_else_24;
        xmlBlockInfo.contents = blockContents;
        xmlBlockInfo.statementId = statementId;
        xmlBlockInfo.nodeNameId = nodeNameId;
        xmlBlockInfo.conditionMotion = conditionMotion;
        xmlBlockInfo.conditionObject = conditionObject;
    }

    /*
     * ブロック内容を解析して、xml情報として設定：IfElseifElse
     */
    private void setXmlIfElseIfBlockInfo(String blockContents, XmlBlockInfo xmlBlockInfo) {

        int statementId;
        int statementElseIfId;
        int nodeNameId = -1;
        int nodeNameElseIfId = -1;

        //-------------
        // 条件文分割
        //-------------
        String[] conditionData = blockContents.split(GimmickManager.GIMMICK_DELIMITER_CONDITION);
        String conditionMotion = conditionData[GimmickManager.BLOCK_CONDITION_MOTION_POS];
        String conditionObject = conditionData[GimmickManager.BLOCK_CONDITION_OBJECT_POS];
        String conditionElseIfObject = conditionData[GimmickManager.BLOCK_CONDITION_ELSEIF_OBJECT_POS];

        //-------------
        // 条件文
        //-------------
        // 動作に対応する条件文を取得
        statementId = Map_blockConditionMotion__statementId.get( conditionMotion );
        statementElseIfId = statementId;

        // 対象に対応するNode名を取得
        switch ( conditionObject ) {
            case GimmickManager.NODE_NAME_ENEMY:
            case GimmickManager.NODE_NAME_EATABLE:
            case GimmickManager.NODE_NAME_POISON:
                nodeNameId = getObjectNameInBlock( conditionObject, objectKindList ,objectGlbList );
                break;
            case GimmickManager.NODE_NAME_NOTHING:
                break;
        }

        switch ( conditionElseIfObject ) {
            case GimmickManager.NODE_NAME_ENEMY:
            case GimmickManager.NODE_NAME_EATABLE:
            case GimmickManager.NODE_NAME_POISON:
                nodeNameElseIfId = getObjectNameInBlock( conditionObject, objectKindList ,objectGlbList );
                break;
            case GimmickManager.NODE_NAME_NOTHING:
                break;
        }

        //--------------------
        // 設定
        //--------------------
        xmlBlockInfo.drawableId = R.drawable.baseline_block_if_else_24;
        xmlBlockInfo.contents = blockContents;
        xmlBlockInfo.statementId = statementId;
        xmlBlockInfo.statementElseIfId = statementElseIfId;
        xmlBlockInfo.nodeNameId = nodeNameId;
        xmlBlockInfo.nodeNameElseIfId = nodeNameElseIfId;
        xmlBlockInfo.conditionMotion = conditionMotion;
        xmlBlockInfo.conditionObject = conditionObject;
        xmlBlockInfo.conditionObjectElseIf = conditionElseIfObject;
    }

    /*
     * ブロック処理量制限値を取得
     */
    private int getBlockValueSettingLimit(String[] blockSplit) {

        // 制限情報がない場合
        // 例）「single_forward」⇒「制限なし」とみなす
        if (blockSplit.length <= 2) {
            return VOLUME_LIMIT_NONE;
        }

        // 制限情報がある場合、制限値をそのまま返す
        // 例）「single_forward_1」⇒「1」を返す
        return Integer.parseInt(blockSplit[BLOCK_VALUE_LIMIT_POS]);
    }

    /*
     * キャラクター座標を設定
     */
    public void setCharacterPosition(String position) {

        String[] strs = splitGimmickPositionDelimiter(position);

        // 正常フォーマットの場合
        if (strs.length == 3) {
            // 位置形式に変換
            float x = Float.parseFloat(strs[0]);
            float y = Float.parseFloat(strs[1]);
            float z = Float.parseFloat(strs[2]);
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
            int stringId = resources.getIdentifier(word, "string", mContext.getPackageName());
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
     * オブジェクト種別を設定
     */
    public void setObjectKind(String objectKind) {

        // プロパティなし
        if (objectKind == null) {
            return;
        }

        // オブジェクト名を分割
        String[] strs = splitGimmickValueDelimiter(objectKind);

        // リスト生成
        objectKindList.clear();
        Collections.addAll(objectKindList, strs);
    }

    /*
     * オブジェクト座標のランダムの有無
     */
    public void setObjectPositionRandom(String random) {

        // プロパティなし
        if (random == null) {
            return;
        }

        objectPositionRandom = random.equals("true");
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
        enemyNumRandom = random.equals("true");
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

        for (String blockItemStr : blockList) {

            XmlBlockInfo xmlBlockInfo = new XmlBlockInfo();

            // ブロック文字列を分割
            // 例)「single_rotate-right_1」⇒「single」「rotate-right」「1」
            String[] blockSplit = Gimmick.splitGimmickBlockDelimiter(blockItemStr);
            // ブロック文字列をxml情報として解析して設定
            setXmlBlockInfo(blockSplit, xmlBlockInfo);

            // リストに追加
            xmlBlockInfoList.add(xmlBlockInfo);
        }
    }

    /*
     * 処理ブロックに埋め込む物体名の取得
     *   para1：path付きglbファイル名
     *          例)"models/goal/house.glb"
     */
    private int getObjectNameInBlock(String glbNameWithPath) {

        //--------------------------
        // glbファイル名を取得
        //--------------------------
        // Glbプロパティの値を分割
        // 例）"models/goal/house.glb" ⇒ [0]models [1]goal [2]house.glb
        String[] goalGlbStr = glbNameWithPath.split("/");
        int lastIndex = goalGlbStr.length - 1;

        // glbファイル名を取得
        // 例）house.glb を取得
        String glbName = goalGlbStr[lastIndex];

        //---------------------------
        // glbファイル に対応する名前
        //---------------------------
        // これが処理ブロックに埋め込まれる
        return Map_glb__nodeName.get(glbName);
    }

    /*
     * 処理ブロックに埋め込む物体名の取得
     */
    private int getObjectNameInBlock( String kind, ArrayList<String> objectKindList, ArrayList<String> objectGlbList ) {

        int index = 0;
        for( String objectKind: objectKindList ){
            if( objectKind.equals( kind ) ){
                break;
            }
            index++;
        }

        String objectGlb = objectGlbList.get(index);
        return getObjectNameInBlock( objectGlb );
    }
}
