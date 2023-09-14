package com.ar.ar_programming.process;

import static com.ar.ar_programming.GimmickManager.NODE_NAME_GOAL;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.ar.ar_programming.character.CharacterNode;
import com.ar.ar_programming.Gimmick;
import com.ar.ar_programming.GimmickManager;
import com.ar.ar_programming.R;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.NodeParent;

import java.util.List;


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
        setLayout(R.layout.process_block_loop);
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

        Log.i("ギミック変更", "Loop 条件判定開始　isCondition()");

        //-----------------------------
        // ループ条件：動作　に応じた判定
        //-----------------------------
        switch (mXmlBlockInfo.conditionMotion) {

            // xxxに到着するまでループ
            case GimmickManager.BLOCK_CONDITION_ARRIVAL:
                result = isConditionArrival(characterNode);
                break;

            // xxxの方を向くまでループ
            case GimmickManager.BLOCK_CONDITION_FACING:
                // キャラクター方向判定
                // ※向いている＝ループ終了であるため、true（向いている）⇒false（ループ終了／条件不成立）に変換して返す
                result = !isConditionFacing(characterNode);
                break;

            // xxxの方を集めるまでループ
            case GimmickManager.BLOCK_CONDITION_COLLECT:
                result = isConditionCollect( characterNode );
                break;

            default:
                result = false;
                break;
        }

        return result;
    }

    /*
     * ループ条件：指定Nodeに到達しているかどうか
     */
    public boolean isConditionArrival(CharacterNode characterNode) {

        boolean result;

        switch ( mXmlBlockInfo.conditionObject ){
            case NODE_NAME_GOAL:
                result = true;
                break;

            default:
                result = false;
                break;
        }

        return result;
    }

    /*
     * ループ条件：指定オブジェクト方向をキャラクターが向いているか判定
     */
    public boolean isConditionFacing(CharacterNode characterNode) {

        //------------------
        // 判定対象Nodeを取得
        //------------------
        // AR上のNodeは、全てanchorNodeを親としているため、characterNodeの親Node（=anchorNode）を検索用に渡す
        AnchorNode anchorNode = (AnchorNode)characterNode.getParentNode();
        Node targetNode = getFacingTargetNode( anchorNode, mXmlBlockInfo.conditionObject );
        if( targetNode == null ){
            // 対象Nodeがなければ、条件不成立とみなす
            return false;
        }

        //---------------------------------
        // キャラクターがNodeを向いているか判定
        //---------------------------------
        return characterNode.isFacingToNode( targetNode );
    }

    /*
     * ループ条件：指定物体を全て収集しているか判定
     *           未収集の物がある場合、ループする必要があるため、trueを返す
     */
    public boolean isConditionCollect(CharacterNode characterNode) {

        //---------------------------
        // Sceneに存在しているかどうか
        //---------------------------
        // 全て収集していなければ、falseを返す
        boolean exists = characterNode.existsNodeOnScene( mXmlBlockInfo.conditionObject );
        return exists;
    }

    /*
     * 条件コンテンツに該当するNodeを取得
     * 　@para1：！anchorNodeを渡すこと（anchorNode配下のNodeが検索対象となるため）
     * 　@para2：向く方向の対象Node名
     */
    public Node getFacingTargetNode(AnchorNode anchorNode, String nodeName) {

        //----------------------
        // 対象Nodeを検索
        //----------------------
        List<Node> nodes = anchorNode.getChildren();
        for (Node node : nodes) {
            if (node.getName().equals( nodeName )) {
                return node;
            }
        }

        // なければnull（想定していないルート）
        return null;
    }



}

