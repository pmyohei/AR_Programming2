package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;

import com.ar.ar_programming.character.CharacterNode;
import com.ar.ar_programming.Gimmick;
import com.ar.ar_programming.GimmickManager;
import com.ar.ar_programming.R;
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

        if( mXmlBlockInfo.conditionMotion.equals( GimmickManager.BLOCK_CONDITION_ARRIVAL ) ) {

            switch ( mXmlBlockInfo.conditionObject ) {
                case GimmickManager.NODE_NAME_GOAL:
                    // ゴール到達＝終了であるため、常にtrue（条件成立）を返す
                    return true;

                default:
                    return false;
            }

        } else if (mXmlBlockInfo.conditionMotion.equals( GimmickManager.BLOCK_CONDITION_FACING )) {

            switch ( mXmlBlockInfo.conditionObject ) {
                case GimmickManager.NODE_NAME_GOAL:
                case GimmickManager.NODE_NAME_OBSTACLE:
                case GimmickManager.NODE_NAME_EATABLE:
                case GimmickManager.NODE_NAME_ENEMY:
                    // キャラクター方向判定
                    // ※向いている＝ループ終了であるため、true（向いている）⇒false（ループ終了／条件不成立）に変換して返す
                    return !isConditionFacing( characterNode, mXmlBlockInfo.nodeNameId );

                default:
                    return false;
            }

        } else if (mXmlBlockInfo.conditionMotion.equals( GimmickManager.BLOCK_CONDITION_COLLECT )) {

            switch ( mXmlBlockInfo.conditionObject ) {
                case GimmickManager.NODE_NAME_OBSTACLE:
                case GimmickManager.NODE_NAME_EATABLE:
                case GimmickManager.NODE_NAME_ENEMY:
                    // キャラクター方向判定
                    // ※向いている＝ループ終了であるため、true（向いている）⇒false（ループ終了／条件不成立）に変換して返す
                    return isConditionCollect( characterNode, mXmlBlockInfo.nodeNameId );

                default:
                    return false;
            }
        }

        return false;
    }


    /*
     * ループ条件：指定オブジェクト方向をキャラクターが向いているか判定
     */
    public boolean isConditionFacing(CharacterNode characterNode, String contents) {

        //------------------
        // 判定対象Nodeを取得
        //------------------
        // AR上のNodeは、全てanchorNodeを親としているため、characterNodeの親Node（=anchorNode）を検索用に渡す
        NodeParent parentNode = characterNode.getParentNode();
        Node targetNode = getFacingTargetNode( parentNode, contents );
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
    public boolean isConditionCollect(CharacterNode characterNode, int contents) {

        // 収集対象
        // !後で

        //---------------
        //
        //---------------
        // 全て収集していなければ、falseを返す
        boolean exists = characterNode.existsNodeOnScene( GimmickManager.NODE_NAME_EATABLE );
        return exists;
    }

    /*
     * 条件コンテンツに該当するNodeを取得
     * 　@para1：！anchorNodeを渡すこと
     * 　@para2：ループ条件コンテンツ
     */
    public Node getFacingTargetNode(NodeParent parentNode, String contents) {

        //----------------------
        // 対象Node名を取得
        //----------------------
        String nodeName;

        switch (contents) {
            case PROCESS_CONTENTS_LOOP_FACING_GOAL:
                nodeName = GimmickManager.NODE_NAME_GOAL;
                break;

            case PROCESS_CONTENTS_LOOP_FACING_OBSTACLE:
                nodeName = GimmickManager.NODE_NAME_OBSTACLE;
                break;

            case PROCESS_CONTENTS_LOOP_FACING_ENEMY:
                nodeName = GimmickManager.NODE_NAME_ENEMY;
                break;

            case PROCESS_CONTENTS_LOOP_FACING_EATABLE:
                nodeName = GimmickManager.NODE_NAME_EATABLE;
                break;

            default:
                nodeName = GimmickManager.NODE_NAME_GOAL;
                break;
        }

        //----------------------
        // 対象Nodeを検索
        //----------------------
        List<Node> nodes = parentNode.getChildren();
        for (Node node : nodes) {
            if (node.getName().equals( nodeName )) {
                return node;
            }
        }

        // なければnull（想定していないルート）
        return null;
    }



}

