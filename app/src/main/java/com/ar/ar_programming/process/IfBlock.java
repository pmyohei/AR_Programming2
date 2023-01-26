package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ar.ar_programming.CharacterNode;
import com.ar.ar_programming.R;


/*
 * ネストあり処理ブロック（if文）
 */
public class IfBlock extends NestBlock {

    //---------------------------
    // 定数
    //---------------------------
    public static final int PROCESS_CONTENTS_IF_COLLISION_OBSTACLE = 0;

    //---------------------------
    // フィールド変数
    //---------------------------

    /*
     * コンストラクタ
     */
    public IfBlock(Context context, int contents) {
        this(context, null, contents);
    }
    public IfBlock(Context context, AttributeSet attrs, int contents) {
        this(context, attrs, 0, contents);
    }
    public IfBlock(Context context, AttributeSet attrs, int defStyle, int contents) {
        super(context, attrs, defStyle, PROCESS_TYPE_IF, contents);
        setLayout( R.layout.process_block_if );
    }

    /*
     * レイアウト設定
     */
    @Override
    public void setLayout(int layoutID) {
        super.setLayout( layoutID );

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
            case PROCESS_CONTENTS_IF_COLLISION_OBSTACLE:
                contentId = R.string.block_contents_if_block;
                break;
            default:
                contentId = R.string.block_contents_if_block;
                break;
        }

        // 文言IDをレイアウトに設定
        TextView tv_contents = findViewById(R.id.tv_contents);
        tv_contents.setText(contentId);
    }

    /*
     * 条件成立判定
     *   @return：条件成立- true
     *   @return：条件不成立- false
     */
    @Override
    public boolean isCondition(CharacterNode characterNode) {

        int i = 0;

        Block below = mNestStartBlockFirst.getBelowBlock();
        while( below != null ){
            below = below.getBelowBlock();
            i++;
        }

        return ( i % 2 != 0 );
    }
}
