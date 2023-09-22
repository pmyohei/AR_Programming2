package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.ar.ar_programming.ArMainFragment;
import com.ar.ar_programming.character.CharacterNode;
import com.ar.ar_programming.Gimmick;
import com.ar.ar_programming.GimmickManager;
import com.ar.ar_programming.R;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;


/*
 * ネストあり処理ブロック（ループ）
 */
public class LoopBlock extends NestBlock {

    //---------------------------
    // 定数
    //---------------------------

    //---------------------------
    // フィールド変数
    //---------------------------

    /*
     * コンストラクタ
     */
    public LoopBlock(Context context, Gimmick.XmlBlockInfo xmlBlockInfo) {
        this(context, null, xmlBlockInfo);
    }

    public LoopBlock(Context context, AttributeSet attrs, Gimmick.XmlBlockInfo xmlBlockInfo) {
        this(context, attrs, 0, xmlBlockInfo);
    }

    public LoopBlock(Context context, AttributeSet attrs, int defStyle, Gimmick.XmlBlockInfo xmlBlockInfo) {
        super(context, attrs, defStyle, xmlBlockInfo);
        setLayout(R.layout.block_loop);
    }

    /*
     * レイアウト設定
     */
    @Override
    public void setLayout(int layoutID) {
        super.setLayout(layoutID);

        // 処理ブロック内の内容を書き換え
        rewriteProcessContents();
    }

    /*
     * 条件成立判定
     *   @return：ループ継続（ループ条件成立）- true
     *   @return：ループ終了（ループ条件不成立　）- false
     */
    @Override
    public boolean isCondition(CharacterNode characterNode) {

        boolean result;

        Log.i("ブロック処理の流れ", "Loop 条件判定開始　isCondition()");

        //-----------------------------
        // ループ条件：動作　に応じた判定
        //-----------------------------
        switch (mXmlBlockInfo.action) {

            // xxxに到着するまでループ
            case GimmickManager.BLOCK_CONDITION_ARRIVAL:
                // 到達している場合、trueを受け取る。
                // 到達していない場合、Loop内の処理をするため、結果を反転させる
                result = !isConditionArrival(characterNode);
                break;

            // xxxの方を向くまでループ
            case GimmickManager.BLOCK_CONDITION_FACING:
                // 向いている場合、trueを受け取る。
                // 向いていない場合、falseを受け取る。
                // 向いていない場合、ループを継続させるため、結果を反転させる
                result = !isConditionFacing(characterNode, mXmlBlockInfo.targetNode_1);
                Log.i("Node検索", "isConditionFacing() targetNode_1=" + mXmlBlockInfo.targetNode_1);
                Log.i("Node検索", "isConditionFacing() 継続 result=" + result);
                break;

            // xxxをすべて集めるまでループ
            // xxxをすべて食べるまでループ
            // xxxをすべて倒すまでループ
            case GimmickManager.BLOCK_CONDITION_COLLECT:
            case GimmickManager.BLOCK_CONDITION_EAT:
            case GimmickManager.BLOCK_CONDITION_DEFEAT:
                // すべてに対応している場合、trueを受け取る。
                // 対応していない場合、Loop内の処理をするため、結果を反転させる
                result = !isConditionEverything( characterNode );
                break;

            default:
                // ループ内の処理なし
                result = false;
                break;
        }

        return result;
    }

    /*
     * ループ条件：指定Nodeに到達しているかどうか
     */
    public boolean isConditionArrival(CharacterNode characterNode) {
        // 対象Nodeと衝突中であれば、到達したとみなす
        return characterNode.isNodeCollision( mXmlBlockInfo.targetNode_1 );
    }

    /*
     * ループ条件：対象Nodeの方をキャラクターが向いているか判定
     */
/*    public boolean isConditionFacing(CharacterNode characterNode) {

        //------------------
        // 判定対象Nodeを取得
        //------------------
        // 検索対象外Nodeリスト
        List<Node> notSearchNodeList = characterNode.getNotSearchNodeList();

        // AR上のNodeは、全てanchorNodeを親としているため、characterNodeの親Node（=anchorNode）を検索用に渡す
        AnchorNode anchorNode = (AnchorNode)characterNode.getParentNode();
        Node targetNode = ArMainFragment.searchNodeOnStage( anchorNode, mXmlBlockInfo.targetNode_1, notSearchNodeList );
        if( targetNode == null ){
            // そもそも対象Nodeがなければ、条件不成立とみなす
            return false;
        }

        //---------------------------------
        // キャラクターがNodeを向いているか判定
        //---------------------------------
        return characterNode.isFacingToNode( targetNode );
    }*/

    /*
     * ループ条件：指定Nodeを全てxxx（集める、食べる、、、）しているか判定
     */
    public boolean isConditionEverything(CharacterNode characterNode) {

        //---------------------------
        // Sceneに存在しているかどうか
        //---------------------------
        // 全てに対応していれば（ステージに指定Nodeが１つもなければ）、trueを返す
        AnchorNode anchorNode = (AnchorNode)characterNode.getParentNode();
        Node node = ArMainFragment.searchNodeOnStage( anchorNode, mXmlBlockInfo.targetNode_1 );

        Log.i("ブロック処理の流れ", "Loop isConditionEverything() targetNode_1=" + mXmlBlockInfo.targetNode_1);

        return ( node == null );
    }
}

