package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.ar.ar_programming.CharacterNode;
import com.ar.ar_programming.ArMainFragment;
import com.ar.ar_programming.R;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.NodeParent;

import java.util.List;


/*
 * ネストあり処理ブロック（ループ）
 */
public class LoopProcessBlock extends NestProcessBlock {

    //---------------------------
    // 定数
    //---------------------------
    public static final int PROCESS_CONTENTS_LOOP_GOAL = 0;
    public static final int PROCESS_CONTENTS_LOOP_BLOCK = 1;
    public static final int PROCESS_CONTENTS_LOOP_FACING_GOAL = 2;
    public static final int PROCESS_CONTENTS_LOOP_FACING_OBSTACLE = 3;

    //---------------------------
    // フィールド変数
    //---------------------------
    private int tmploopCount = 0;

    /*
     * コンストラクタ
     */
    public LoopProcessBlock(Context context, int contents) {
        this(context, null, contents);
    }

    public LoopProcessBlock(Context context, AttributeSet attrs, int contents) {
        this(context, attrs, 0, contents);
    }

    public LoopProcessBlock(Context context, AttributeSet attrs, int defStyle, int contents) {
        super(context, attrs, defStyle, PROCESS_TYPE_LOOP, contents);
        setLayout(R.layout.process_block_loop);
    }

    /*
     * レイアウト設定
     */
    @Override
    public void setLayout(int layoutID) {
        super.setLayout(layoutID);

        // 処理ブロック内の内容を書き換え
        rewriteProcessContents(mProcessContents);
    }

    /*
     * 処理ブロック内の内容を書き換え
     */
    @Override
    public void rewriteProcessContents(int contents) {

        // 処理内容文字列ID
        int contentId;

        // 種別に応じた文言IDを取得
        switch (contents) {
            case PROCESS_CONTENTS_LOOP_GOAL:
                contentId = R.string.block_contents_loop_goal;
                break;
            case PROCESS_CONTENTS_LOOP_BLOCK:
                contentId = R.string.block_contents_loop_block;
                break;
            case PROCESS_CONTENTS_LOOP_FACING_GOAL:
                contentId = R.string.block_contents_loop_facing_goal;
                break;
            case PROCESS_CONTENTS_LOOP_FACING_OBSTACLE:
                contentId = R.string.block_contents_loop_facing_obstacle;
                break;
            default:
                contentId = R.string.block_contents_loop_goal;
                break;
        }

        // 文言IDをレイアウトに設定
        TextView tv_contents = findViewById(R.id.tv_contents);
        tv_contents.setText(contentId);
    }

    /*
     * 条件成立判定
     *   @return：ループ継続（ループ条件成立）- true
     *   @return：ループ終了（ループ条件不成立　）- false
     */
    @Override
    public boolean isCondition(CharacterNode characterNode) {

        switch (mProcessContents) {

            case PROCESS_CONTENTS_LOOP_GOAL:
                break;

            case PROCESS_CONTENTS_LOOP_BLOCK:
                break;

            case PROCESS_CONTENTS_LOOP_FACING_GOAL:
            case PROCESS_CONTENTS_LOOP_FACING_OBSTACLE:
                // キャラクター方向判定
                // ※向いている＝ループ終了であるため、true（向いている）⇒false（ループ終了／条件不成立）に変換して返す
                return !isConditionFacing( characterNode, mProcessContents );

            default:
                break;
        }

        tmploopCount++;
        Log.i("チャート動作チェック", "tmploopCount=" + tmploopCount);
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
                nodeName = ArMainFragment.NODE_NAME_GOAL;
                break;
            case PROCESS_CONTENTS_LOOP_FACING_OBSTACLE:
                nodeName = ArMainFragment.NODE_NAME_OBSTACLE;
                break;
            default:
                nodeName = ArMainFragment.NODE_NAME_GOAL;
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

