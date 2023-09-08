package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

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
    public static final int PROCESS_CONTENTS_LOOP_ARRIVAL_GOAL = 0;
    public static final int PROCESS_CONTENTS_LOOP_ARRIVAL_OBSTACLE = 1;
    public static final int PROCESS_CONTENTS_LOOP_BLOCK = 2;
    public static final int PROCESS_CONTENTS_LOOP_FACING_GOAL = 3;
    public static final int PROCESS_CONTENTS_LOOP_FACING_OBSTACLE = 4;

    //---------------------------
    // フィールド変数
    //---------------------------
    private int tmploopCount = 0;

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
        rewriteProcessContents( mXmlBlockInfo.stringId );
    }

    /*
     * 処理ブロック内の内容を書き換え
     */
    @Override
    public void rewriteProcessContents(int stringID) {

        // 文言IDをレイアウトに設定
        TextView tv_contents = findViewById(R.id.tv_contents);
        tv_contents.setText(stringID);
    }

    /*
     * 条件成立判定
     *   @return：ループ継続（ループ条件成立）- true
     *   @return：ループ終了（ループ条件不成立　）- false
     */
    @Override
    public boolean isCondition(CharacterNode characterNode) {

        switch ( mXmlBlockInfo.contents ) {

            case PROCESS_CONTENTS_LOOP_ARRIVAL_GOAL:
                // ゴール到達＝終了であるため、常にtrue（条件成立）を返す
                return true;

            case PROCESS_CONTENTS_LOOP_BLOCK:
                break;

            case PROCESS_CONTENTS_LOOP_FACING_GOAL:
            case PROCESS_CONTENTS_LOOP_FACING_OBSTACLE:
                // キャラクター方向判定
                // ※向いている＝ループ終了であるため、true（向いている）⇒false（ループ終了／条件不成立）に変換して返す
                return !isConditionFacing( characterNode, mXmlBlockInfo.contents );

            default:
                break;
        }

        tmploopCount++;
//        Log.i("チャート動作チェック", "tmploopCount=" + tmploopCount);
        return (tmploopCount <= 20);
    }


    /*
     * ループ条件：指定オブジェクト方向をキャラクターが向いているか判定
     */
    public boolean isConditionFacing(CharacterNode characterNode, int contents) {

        //------------------
        // 判定対象Nodeを取得
        //------------------
        // AR上のNodeは、全てanchorNodeを親としているため、characterNodeの親Node（=anchorNode）を検索用に渡す
        NodeParent parentNode = characterNode.getParentNode();
        Node targetNode = getFacingTargerNode( parentNode, contents );
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
     * 条件コンテンツに該当するNodeを取得
     * 　@para1：！anchorNodeを渡すこと
     * 　@para2：ループ条件コンテンツ
     */
    public Node getFacingTargerNode(NodeParent parentNode, int contents) {

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

