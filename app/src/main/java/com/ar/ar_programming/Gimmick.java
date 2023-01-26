package com.ar.ar_programming;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.ar.ar_programming.process.Block;
import com.ar.ar_programming.process.IfElseIfElseBlock;
import com.ar.ar_programming.process.IfElseBlock;
import com.ar.ar_programming.process.IfBlock;
import com.ar.ar_programming.process.LoopBlock;
import com.ar.ar_programming.process.SingleBlock;
import com.google.ar.sceneform.math.Vector3;

import java.util.ArrayList;
import java.util.Collections;

public class Gimmick {

    //---------------------------
    // 定数
    //---------------------------
    // フォーマット位置；ゴール説明
    public static final int GOAl_EXP_MAJOR_POS = 0;
    public static final int GOAl_EXP_SUB_POS = 1;
    public static final int GOAl_EXP_CONTENTS_POS = 2;
    public static final int GOAl_EXP_EXPLANATION_POS = 3;

    // フォーマット位置；Block
    private final int BLOCK_TYPE_POS = 0;
    private final int BLOCK_CONTENTS_POS = 1;
    private final int BLOCK_VALUE_LIMIT_POS = 2;

    // 異常値
    public static final int VOLUME_LIMIT_NONE = 0;
    public static final int NO_DATA_GOAL_ANGLE = 0xFFFF;

    //---------------------------
    // フィールド変数
    //---------------------------
    public Context mContext;

    // ギミックプロパティ
    public String successCondition;
    public String stageGlb;
    public String characterGlb;
    public Vector3 characterPositionVec;
    public String goalGlb;
    public Vector3 goalPositionVec;
    public float goalAngle;
    public boolean objectPositionRandom;
    public ArrayList<String> objectGlbList;
    public ArrayList<Integer> objectNumList;
    public ArrayList<String> objectKindList;
    public ArrayList<Vector3> objectPositionVecList;
    public ArrayList<String> blockList;
    public ArrayList<XmlBlockInfo> xmlBlockInfoList;
    public ArrayList<Integer> goalExplanationIdList;

    /*
     * ブロックプロパティ情報
     */
    public class XmlBlockInfo {
        public int type;
        public int contents;
        public int userSelectDrawableId;
        public int userSelectStringId;
        public int valueLimit;

        public XmlBlockInfo() {
        }
    }

/*    public HashMap mBlockXmlResourseMap = new HashMap() {
        {
            put( "single_forward", new HashMap<Integer, Integer>() {
                {
                    put(Block.PROCESS_TYPE_SINGLE, SingleBlock.PROCESS_CONTENTS_FORWARD);
                    put(R.drawable.baseline_block_forward_24, R.string.block_forward);
                }
            });

            put( "single_back", new HashMap<Integer, Integer>() {
                {
                    put(Block.PROCESS_TYPE_SINGLE, SingleBlock.PROCESS_CONTENTS_BACK);
                    put(R.drawable.baseline_block_back_24, R.string.block_back);
                }
            });

        }
    };*/


    /*
     * コンストラクタ
     */
    public Gimmick(Context context) {

        mContext = context;

        objectGlbList = new ArrayList<>();
        objectNumList = new ArrayList<>();
        objectKindList = new ArrayList<>();
        objectPositionVecList = new ArrayList<>();
        blockList = new ArrayList<>();
        xmlBlockInfoList = new ArrayList<>();
        goalExplanationIdList = new ArrayList<>();
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
        int selectDrawableId;
        int selectStringId;

        String blockContentsStr = blockSplit[BLOCK_CONTENTS_POS];
        switch (blockContentsStr) {
            case "forward":
                contents = SingleBlock.PROCESS_CONTENTS_FORWARD;
                selectDrawableId = R.drawable.baseline_block_forward_24;
                selectStringId = R.string.block_contents_forward;
                break;

            case "back":
                contents = SingleBlock.PROCESS_CONTENTS_BACK;
                selectDrawableId = R.drawable.baseline_block_back_24;
                selectStringId = R.string.block_contents_back;
                break;

            case "rotate-right":
                contents = SingleBlock.PROCESS_CONTENTS_RIGHT_ROTATE;
                selectDrawableId = R.drawable.baseline_block_rotate_right_24;
                selectStringId = R.string.block_contents_rorate_right;
                break;

            case "rotate-left":
            default:
                contents = SingleBlock.PROCESS_CONTENTS_LEFT_ROTATE;
                selectDrawableId = R.drawable.baseline_block_rotate_left_24;
                selectStringId = R.string.block_contents_rorate_left;
                break;
        }

        // 処理量制限情報取得
        int valueSettingLimit = getBlockValueSettingLimit(blockSplit);

        Log.i("ブロックxml", "valueSettingLimit=" + valueSettingLimit);

        // 設定
        xmlBlockInfo.contents = contents;
        xmlBlockInfo.userSelectDrawableId = selectDrawableId;
        xmlBlockInfo.userSelectStringId = selectStringId;
        xmlBlockInfo.valueLimit = valueSettingLimit;
    }

    /*
     * ブロック内容を解析して、xml情報として設定：Loop
     */
    private void setXmlLoopBlockInfo(String blockContentsStr, XmlBlockInfo xmlBlockInfo) {

        int contents;
        int selectDrawableId = R.drawable.baseline_block_loop_24;
        int selectStringId;

        switch (blockContentsStr) {
            case "facing-goal":
                contents = LoopBlock.PROCESS_CONTENTS_LOOP_FACING_GOAL;
                selectStringId = R.string.block_contents_loop_facing_goal;
                break;

            case "facing-obstacle":
                contents = LoopBlock.PROCESS_CONTENTS_LOOP_FACING_OBSTACLE;
                selectStringId = R.string.block_contents_loop_facing_obstacle;
                break;

            case "arrival-goal":
                contents = LoopBlock.PROCESS_CONTENTS_LOOP_ARRIVAL_GOAL;
                selectStringId = R.string.block_contents_loop_arrival_goal;
                break;

            case "arrival-obstacle":
            default:
                contents = LoopBlock.PROCESS_CONTENTS_LOOP_ARRIVAL_OBSTACLE;
                selectStringId = R.string.block_contents_loop_arrival_block;
                break;
        }

        // 設定
        xmlBlockInfo.contents = contents;
        xmlBlockInfo.userSelectDrawableId = selectDrawableId;
        xmlBlockInfo.userSelectStringId = selectStringId;
    }

    /*
     * ブロック内容を解析して、xml情報として設定：If
     */
    private void setXmlIfBlockInfo(String blockContentsStr, XmlBlockInfo xmlBlockInfo) {

        int contents;
        int selectDrawableId = R.drawable.baseline_block_if_24;
        int selectStringId;

        switch (blockContentsStr) {
            case "collision-obstacle":
            default:
                contents = IfBlock.PROCESS_CONTENTS_IF_COLLISION_OBSTACLE;
                selectStringId = R.string.block_contents_if_block;
                break;
        }

        // 設定
        xmlBlockInfo.contents = contents;
        xmlBlockInfo.userSelectDrawableId = selectDrawableId;
        xmlBlockInfo.userSelectStringId = selectStringId;
    }

    /*
     * ブロック内容を解析して、xml情報として設定：IfElse
     */
    private void setXmlIfElseBlockInfo(String blockContentsStr, XmlBlockInfo xmlBlockInfo) {

        int contents;
        int selectDrawableId = R.drawable.baseline_block_if_else_24;
        int selectStringId;

        switch (blockContentsStr) {
            case "collision-obstacle":
            default:
                contents = IfElseBlock.PROCESS_CONTENTS_IF_ELSE_BLOCK;
                selectStringId = R.string.block_contents_if_else_block;
        }

        // 設定
        xmlBlockInfo.contents = contents;
        xmlBlockInfo.userSelectDrawableId = selectDrawableId;
        xmlBlockInfo.userSelectStringId = selectStringId;
    }

    /*
     * ブロック内容を解析して、xml情報として設定：IfElseifElse
     */
    private void setXmlIfElseIfBlockInfo(String blockContentsStr, XmlBlockInfo xmlBlockInfo) {

        int contents;
        int selectDrawableId = R.drawable.baseline_block_if_else_24;
        int selectStringId;

        switch (blockContentsStr) {
            case "collision-obstacle":
            default:
                contents = IfElseIfElseBlock.PROCESS_CONTENTS_IF_ELSEIF_ELSE_BLOCK;
                selectStringId = R.string.block_contents_if_elseif_else_block;
        }

        // 設定
        xmlBlockInfo.contents = contents;
        xmlBlockInfo.userSelectDrawableId = selectDrawableId;
        xmlBlockInfo.userSelectStringId = selectStringId;
    }

    /*
     * ブロック処理量制限値を取得
     */
    private int getBlockValueSettingLimit( String[] blockSplit ) {

        // 制限情報がない場合
        // 例）「single_forward」⇒「制限なし」とみなす
        if( blockSplit.length <= 2 ){
            return VOLUME_LIMIT_NONE;
        }

        // 制限情報がある場合、制限値をそのまま返す
        // 例）「single_forward_1」⇒「1」を返す
        return Integer.parseInt( blockSplit[BLOCK_VALUE_LIMIT_POS] );
    }

    /*
     * キャラクター座標を設定
     */
    public void setCharacterPosition( String position ) {

        String[] strs = splitGimmickPositionDelimiter(position);

        // 正常フォーマットの場合
        if( strs.length == 3 ){
            // 位置形式に変換
            float x = Float.parseFloat( strs[0] );
            float y = Float.parseFloat( strs[1] );
            float z = Float.parseFloat( strs[2] );
            characterPositionVec = new Vector3( x, y, z );
            return;
        }

        // フォーマット異常の場合
        characterPositionVec = new Vector3( 0f, 0f, 0f );
    }

    /*
     * ゴール座標を設定
     */
    public void setGoalPosition( String position ) {

        String[] strs = splitGimmickPositionDelimiter(position);

        // 正常フォーマットの場合
        if( strs.length == 3 ){
            // 位置形式に変換
            float x = Float.parseFloat( strs[0] );
            float y = Float.parseFloat( strs[1] );
            float z = Float.parseFloat( strs[2] );
            goalPositionVec = new Vector3( x, y, z );
            return;
        }

        // フォーマット異常の場合
        goalPositionVec = new Vector3( 1f, 1f, 1f );
    }

    /*
     * ゴールモデルの角度を設定
     */
    public void setGoalAngle( String angle ) {

        // 設定なしなら、未設定用値を設定して終了
        if( angle.isEmpty() ){
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
    public void setGoalExplanation( String goalExplanation ) {
        Resources resources = mContext.getResources();

        goalExplanationIdList.clear();

        // ゴール説明ID文字列をintに変換して、リストに格納
        String[] strs = splitGimmickValueDelimiter(goalExplanation);
        for( String word: strs ){
            // 文字列からID値に変換
            int stringId = resources.getIdentifier(word, "string", mContext.getPackageName());
            goalExplanationIdList.add( stringId );
        }
    }

    /*
     * オブジェクト名を設定
     */
    public void setObjectGlb( String objectGlb ) {

        // オブジェクト名を分割
        String[] strs = splitGimmickValueDelimiter(objectGlb);

        // リスト生成
        objectGlbList.clear();
        Collections.addAll(objectGlbList, strs);
    }

    /*
     * オブジェクト数を設定
     */
    public void setObjectNum( String objectNum ) {

        // オブジェクト名を分割
        String[] strs = splitGimmickValueDelimiter(objectNum);

        // リスト生成
        objectNumList.clear();
        for( String num: strs ){
            objectNumList.add( Integer.parseInt(num) );
        }
    }

    /*
     * オブジェクト種別を設定
     */
    public void setObjectKind( String objectKind ) {

        // オブジェクト名を分割
        String[] strs = splitGimmickValueDelimiter(objectKind);

        // リスト生成
        objectKindList.clear();
        Collections.addAll(objectKindList, strs);
    }

    /*
     * オブジェクト座標のランダムの有無
     */
    public void setObjectPositionRandom( String random ) {
        objectPositionRandom = random.equals( "true" );
    }

    /*
     * オブジェクト座標を設定
     */
    public void setObjectPositionVecList(String position ) {

        // 位置情報データ毎に分割
        // 例)"0:0:0, 1:1:1" → 「0:0:0」「1:1:1」
        String[] valueSplit = splitGimmickValueDelimiter(position);

        //-----------------------
        // 位置情報分、リストに格納
        //-----------------------
        for( String posStr: valueSplit ){

            // 位置情報を座標毎に分割
            // 例)「0:0:0」 → 「0」「0」「0」
            String[] posSplit = splitGimmickPositionDelimiter(posStr);

            // 正常フォーマットの場合
            if( posSplit.length == 3 ){
                // 位置形式に変換
                float x = Float.parseFloat( posSplit[0] );
                float y = Float.parseFloat( posSplit[1] );
                float z = Float.parseFloat( posSplit[2] );
                objectPositionVecList.add( new Vector3( x, y, z ) );
                return;
            }

            // フォーマット異常の場合
            objectPositionVecList.add(new Vector3( 0f, 0f, 0f ));
        }
    }

    /*
     * ギミックで使用可能なブロックリストを設定
     */
    public void setBlock( String block ) {
        // オブジェクト名を分割
        String[] strs = splitGimmickValueDelimiter(block);

        // リスト生成
        blockList.clear();
        Collections.addAll(blockList, strs);

        //xmlブロック情報リストを生成する
        setXmlBlockInfoList( blockList );
    }

    /*
     * ギミックで使用可能なブロックリストを設定
     */
    private void setXmlBlockInfoList( ArrayList<String> blockList ) {

        for( String blockItemStr: blockList ){

            XmlBlockInfo xmlBlockInfo = new XmlBlockInfo();

            // ブロック文字列を分割
            // 例)「single_rotate-right_1」⇒「single」「rotate-right」「1」
            String[] blockSplit = Gimmick.splitGimmickBlockDelimiter( blockItemStr );
            // ブロック文字列をxml情報として解析して設定
            setXmlBlockInfo( blockSplit, xmlBlockInfo );

            // リストに追加
            xmlBlockInfoList.add( xmlBlockInfo );
        }
    }


}
