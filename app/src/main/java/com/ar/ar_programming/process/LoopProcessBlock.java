package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ar.ar_programming.CharacterNode;
import com.ar.ar_programming.R;


/*
 * ネストあり処理ブロック（ループ）
 */
public class LoopProcessBlock extends NestProcessBlock {

    //---------------------------
    // 定数
    //---------------------------
    public static final int PROCESS_CONTENTS_LOOP_GOAL = 0;
    public static final int PROCESS_CONTENTS_LOOP_BLOCK = 1;

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

        tmploopCount++;
        Log.i("チャート動作チェック", "tmploopCount=" + tmploopCount);
        return (tmploopCount <= 2);
    }

}

