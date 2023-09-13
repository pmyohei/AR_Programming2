package com.ar.ar_programming;

import static com.ar.ar_programming.GimmickManager.BLOCK_CONTENTS_POS;
import static com.ar.ar_programming.GimmickManager.BLOCK_TYPE_POS;
import static com.ar.ar_programming.GimmickManager.BLOCK_VALUE_LIMIT_POS;
import static com.ar.ar_programming.process.SingleBlock.PROCESS_CONTENTS_BACK;
import static com.ar.ar_programming.process.SingleBlock.PROCESS_CONTENTS_FORWARD;
import static com.ar.ar_programming.process.SingleBlock.PROCESS_CONTENTS_LEFT_ROTATE;
import static com.ar.ar_programming.process.SingleBlock.PROCESS_CONTENTS_RIGHT_ROTATE;
import static com.ar.ar_programming.process.SingleBlock.PROCESS_CONTENTS_THROW_AWAY;

import android.content.Context;
import android.content.res.Resources;

import com.ar.ar_programming.process.Block;
import com.ar.ar_programming.process.IfElseIfElseBlock;
import com.ar.ar_programming.process.IfElseBlock;
import com.ar.ar_programming.process.IfBlock;
import com.ar.ar_programming.process.LoopBlock;
import com.ar.ar_programming.process.SingleBlock;
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

    // glbファイル：物体名（処理ブロックに埋め込まれる）
    public Map<String, Integer> GLB_NODENAME_MAP;

    /*
     * ブロックプロパティ情報
     */
    public class XmlBlockInfo {

        //----------------------
        // 例) single_forward_1
        //      type    ：single
        //      contents：forward
        //      userSelectDrawableId  ：singleブロック用のDrawableID
        //      userSelectStringId    ：ブロック内文字列
        //      valueLimit：1
        //----------------------
        public int type;                // ブロック種別（Single, Loop, If,,,）
        public int contents;            // ブロック内容（「前へ進む」、「食べる」、、）
        public int drawableId;          // ブロックイメージID
        public int stringId;            // ブロック文字列ID
        public int stringIdElseIf;      // ブロック文字列ID（else if 条件文）
        public int nodeNameStringId;    // ブロック文字列内Node名ID
        public int volumeLimit;         // 処理量制限値
        public boolean existsVolume;    // 処理量があるブロックかどうか（例えば、「前へ進む」は”あり”。「食べる」であれば”なし”。）

        public XmlBlockInfo() {
            nodeNameStringId = -1;
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

        // glbファイル名とNode名の対応マップ
        GLB_NODENAME_MAP = new HashMap<String, Integer>() {
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
     * tmp
     */
    private void tmp() {
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
            case "single":
                return Block.PROCESS_TYPE_SINGLE;

            case "loop":
                return Block.PROCESS_TYPE_LOOP;

            case "if":
                return Block.PROCESS_TYPE_IF;

            case "if-else":
                return Block.PROCESS_TYPE_IF_ELSE;

            case "if-elseif-else":
                return Block.PROCESS_TYPE_IF_ELSEIF_ELSE;
        }

        return Block.PROCESS_TYPE_SINGLE;
    }

    /*
     * ブロック内容を解析して、xml情報として設定
     */
    private void setXmlBlockInfo(String[] blockSplit, XmlBlockInfo xmlBlockInfo) {

        // ブロック識別値
        final int blockType = convertBlockType(blockSplit[BLOCK_TYPE_POS]);
        // 設定
        xmlBlockInfo.type = blockType;

        String blockContentsStr = blockSplit[BLOCK_CONTENTS_POS];

        switch (blockType) {
            case Block.PROCESS_TYPE_SINGLE:
                setXmlSingleBlockInfo(blockSplit, xmlBlockInfo);
                break;

            case Block.PROCESS_TYPE_LOOP:
                setXmlLoopBlockInfo(blockContentsStr, xmlBlockInfo);
                break;

            case Block.PROCESS_TYPE_IF:
                setXmlIfBlockInfo(blockContentsStr, xmlBlockInfo);
                break;

            case Block.PROCESS_TYPE_IF_ELSE:
                setXmlIfElseBlockInfo(blockContentsStr, xmlBlockInfo);
                break;

            case Block.PROCESS_TYPE_IF_ELSEIF_ELSE:
            default:
                setXmlIfElseIfBlockInfo(blockContentsStr, xmlBlockInfo);
                break;
        }
    }

    /*
     * ブロック内容を解析して、xml情報として設定：Single処理用
     */
    private void setXmlSingleBlockInfo(String[] blockSplit, XmlBlockInfo xmlBlockInfo) {

        int contents;
        int drawableId;
        int blockStringId;
        int nodeNameInBlockStringId = -1;
        boolean existsVolume = true;

        String blockContentsStr = blockSplit[BLOCK_CONTENTS_POS];
        switch (blockContentsStr) {
            case "forward":
                contents = SingleBlock.PROCESS_CONTENTS_FORWARD;
                drawableId = R.drawable.baseline_block_forward_24;
                blockStringId = R.string.block_contents_forward;
                break;

            case "back":
                contents = SingleBlock.PROCESS_CONTENTS_BACK;
                drawableId = R.drawable.baseline_block_back_24;
                blockStringId = R.string.block_contents_back;
                break;

            case "rotateRight":
                contents = SingleBlock.PROCESS_CONTENTS_RIGHT_ROTATE;
                drawableId = R.drawable.baseline_block_rotate_right_24;
                blockStringId = R.string.block_contents_rorate_right;
                break;

            case "rotateLeft":
                contents = SingleBlock.PROCESS_CONTENTS_LEFT_ROTATE;
                drawableId = R.drawable.baseline_block_rotate_left_24;
                blockStringId = R.string.block_contents_rorate_left;
                break;

            case "eat":
                contents = SingleBlock.PROCESS_CONTENTS_EAT;
                drawableId = R.drawable.baseline_eat_24;
                blockStringId = R.string.block_contents_eat;
                nodeNameInBlockStringId = getObjectNameInBlock( "eatable", objectKindList ,objectGlbList );
                existsVolume = false;
                break;

            case "throwAway":
                contents = SingleBlock.PROCESS_CONTENTS_THROW_AWAY;
                drawableId = R.drawable.baseline_throw_away_24;
                blockStringId = R.string.block_contents_throw_away;
                nodeNameInBlockStringId = getObjectNameInBlock( "throwAway", objectKindList ,objectGlbList );
                existsVolume = false;
                break;

            case "attack":
                contents = SingleBlock.PROCESS_CONTENTS_ATTACK;
                drawableId = R.drawable.baseline_attack_24;
                blockStringId = R.string.block_contents_attack;
                nodeNameInBlockStringId = getObjectNameInBlock( "enemy", objectKindList ,objectGlbList );
                existsVolume = false;
                break;

            default:
                contents = SingleBlock.PROCESS_CONTENTS_FORWARD;
                drawableId = R.drawable.baseline_block_forward_24;
                blockStringId = R.string.block_contents_forward;
                break;
        }

        // 処理量制限情報取得
        int valueSettingLimit = getBlockValueSettingLimit(blockSplit);

//        Log.i("ブロックxml", "valueSettingLimit=" + valueSettingLimit);

        // 設定
        xmlBlockInfo.contents = contents;
        xmlBlockInfo.drawableId = drawableId;
        xmlBlockInfo.stringId = blockStringId;
        xmlBlockInfo.nodeNameStringId = nodeNameInBlockStringId;
        xmlBlockInfo.volumeLimit = valueSettingLimit;
        xmlBlockInfo.existsVolume = existsVolume;
    }

    /*
     * ブロック内容を解析して、xml情報として設定：Loop
     */
    private void setXmlLoopBlockInfo(String blockContentsStr, XmlBlockInfo xmlBlockInfo) {

        int contents;
        int blockStringId;
        int nodeNameInBlockStringId;

        switch (blockContentsStr) {
            case "facing-goal":
                contents = LoopBlock.PROCESS_CONTENTS_LOOP_FACING_GOAL;
                blockStringId = R.string.block_contents_loop_facing_goal;
                nodeNameInBlockStringId = getObjectNameInBlock(goalGlb);
                break;

            case "facing-enemy":
                contents = LoopBlock.PROCESS_CONTENTS_LOOP_FACING_ENEMY;
                blockStringId = R.string.block_contents_loop_facing_enemy;
                nodeNameInBlockStringId = getObjectNameInBlock( "enemy", objectKindList ,objectGlbList );
                break;
                
//            case "facing-obstacle":
//                contents = LoopBlock.PROCESS_CONTENTS_LOOP_FACING_OBSTACLE;
//                blockStringId = R.string.block_contents_loop_facing_obstacle;
//                break;

            case "arrival-goal":
            default:
                contents = LoopBlock.PROCESS_CONTENTS_LOOP_ARRIVAL_GOAL;
                blockStringId = R.string.block_contents_loop_arrival_goal;
                nodeNameInBlockStringId = getObjectNameInBlock(goalGlb);
                break;

//            case "arrival-obstacle":
//            default:
//                contents = LoopBlock.PROCESS_CONTENTS_LOOP_ARRIVAL_OBSTACLE;
//                blockStringId = R.string.block_contents_loop_arrival_block;
//                nodeNameInBlockStringId = getObjectNameInBlock( goalGlb );
//                break;
        }

        // 設定
        xmlBlockInfo.contents = contents;
        xmlBlockInfo.drawableId = R.drawable.baseline_block_loop_24;
        xmlBlockInfo.stringId = blockStringId;
        xmlBlockInfo.nodeNameStringId = nodeNameInBlockStringId;
    }

    /*
     * ブロック内容を解析して、xml情報として設定：If
     */
    private void setXmlIfBlockInfo(String blockContentsStr, XmlBlockInfo xmlBlockInfo) {

        int contents;
        int stringId;
        int nodeNameInBlockStringId = -1;

        switch (blockContentsStr) {
            case "collision-obstacle":
                contents = IfBlock.PROCESS_CONTENTS_IF_COLLISION_OBSTACLE;
                stringId = R.string.block_contents_if_block;
                break;

            case "eatable":
                contents = IfBlock.PROCESS_CONTENTS_IF_EATABLE;
                stringId = R.string.block_contents_if_eatable;
                nodeNameInBlockStringId = getObjectNameInBlock( "eatable", objectKindList ,objectGlbList );
                break;

            case "poison":
                contents = IfBlock.PROCESS_CONTENTS_IF_POISON;
                stringId = R.string.block_contents_if_poison;
                nodeNameInBlockStringId = getObjectNameInBlock( "throwAway", objectKindList ,objectGlbList );
                break;

            default:
                contents = IfBlock.PROCESS_CONTENTS_IF_COLLISION_OBSTACLE;
                stringId = R.string.block_contents_if_block;
                break;
        }

        // 設定
        xmlBlockInfo.drawableId = R.drawable.baseline_block_if_24;
        xmlBlockInfo.contents = contents;
        xmlBlockInfo.stringId = stringId;
        xmlBlockInfo.nodeNameStringId = nodeNameInBlockStringId;
    }

    /*
     * ブロック内容を解析して、xml情報として設定：IfElse
     */
    private void setXmlIfElseBlockInfo(String blockContentsStr, XmlBlockInfo xmlBlockInfo) {

        int contents;
        int stringId;
        int nodeNameInBlockStringId = -1;

        switch (blockContentsStr) {
            case "eatable":
                contents = IfElseBlock.PROCESS_CONTENTS_IF_ELSE_EATABLE_IN_FRONT;
                stringId = R.string.block_contents_if_else_eatable;
                nodeNameInBlockStringId = getObjectNameInBlock( "eatable", objectKindList ,objectGlbList );
                break;

            case "nothing":
                contents = IfElseBlock.PROCESS_CONTENTS_IF_ELSE_NOTHING_IN_FRONT;
                stringId = R.string.block_contents_if_else_nothing;
                break;

            default:
                contents = IfElseBlock.PROCESS_CONTENTS_IF_ELSE_EATABLE_IN_FRONT;
                stringId = R.string.block_contents_if_else_eatable;
                break;
        }

        // 設定
        xmlBlockInfo.drawableId = R.drawable.baseline_block_if_else_24;
        xmlBlockInfo.contents = contents;
        xmlBlockInfo.stringId = stringId;
        xmlBlockInfo.nodeNameStringId = nodeNameInBlockStringId;
    }

    /*
     * ブロック内容を解析して、xml情報として設定：IfElseifElse
     */
    private void setXmlIfElseIfBlockInfo(String blockContentsStr, XmlBlockInfo xmlBlockInfo) {

        int contents;
        int stringId;
        int stringIdElseIf;

        switch (blockContentsStr) {
            case "eatable-poison":
            default:
                contents = IfElseIfElseBlock.PROCESS_CONTENTS_IF_ELSEIF_ELSE_EATABLE_POISON;
                stringId = R.string.block_contents_if_eatable;
                stringIdElseIf = R.string.block_contents_if_poison;
                break;
        }

        // 設定
        xmlBlockInfo.contents = contents;
        xmlBlockInfo.stringId = stringId;
        xmlBlockInfo.stringIdElseIf = stringIdElseIf;
        xmlBlockInfo.drawableId = R.drawable.baseline_block_if_else_24;
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
        return GLB_NODENAME_MAP.get(glbName);
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
