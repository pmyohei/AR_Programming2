package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
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
    private int mBlockInLoopIndex;
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
        setLayout( R.layout.process_block_loop );
        init();
    }

    /*
     * 初期化処理
     */
    private void init() {
        // ネスト内処理indexを初期化
        mBlockInLoopIndex = 0;

        // ネスト内スタートブロック初期設定
//        initStartBlockInNest( R.layout.process_block_start_in_nest );
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
     * ネスト内の処理ブロックを取得
     * 　返す対象は、先頭から順番に行う。
     * 　最後の処理ブロックまで返した場合、再度、先頭の処理ブロックから返す
     */
    @Override
    public ProcessBlock getBlockInNest() {

        //--------------------
        // ネスト内処理ブロック
        //--------------------
        // ネスト内処理ブロックをコールされた順に応じて返す
        ViewGroup ll_insideRoot = findViewById(R.id.ll_firstNestRoot);
        ProcessBlock block = (ProcessBlock) ll_insideRoot.getChildAt(mBlockInLoopIndex);

        //--------------------------
        // 返す処理ブロックIndexを次へ
        //--------------------------
        // 最後のindexまで到達した場合、先頭indexに戻す
        mBlockInLoopIndex++;
        int blockInNestNum = ll_insideRoot.getChildCount();
        if (mBlockInLoopIndex >= blockInNestNum) {
            mBlockInLoopIndex = 0;
        }

        return block;
    }

    /*
     * 次に処理するブロックが、ネスト内の先頭処理かどうか
     *   @return：true -先頭
     *   @return：false-先頭以外
     */
    public boolean isNextProcessTop() {
        return ( mBlockInLoopIndex == 0 );
    }

    /*
     * 条件成立判定
     *   @return：ループ継続（ループ条件成立）- true
     *   @return：ループ終了（ループ条件不成立　）- false
     */
    @Override
    public boolean isCondition(CharacterNode characterNode) {

        tmploopCount++;
        boolean tmp = (tmploopCount == 2);
        return tmp;

/*        // 条件成立判定は、ネストindexが先頭を指している時のみ行う
        if( mBlockInNestIndex > 0 ){
            return false;
        }

        //----------------
        // 条件成立判定
        //----------------
        switch ( mProcessKind ) {
            // ゴールしているかどうか
            case ProcessBlock.PROC_KIND_LOOP_GOAL:
                return !characterNode.isGoaled();

            // 障害物と衝突中
            case ProcessBlock.PROC_KIND_LOOP_OBSTACLE:
                return false;
        }

        return false;*/
    }

}

