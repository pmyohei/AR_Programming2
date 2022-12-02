package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
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

    //---------------------------
    // フィールド変数
    //---------------------------
    private int mBlockInLoopIndex;
    private int tmploopCount = 0;

    /*
     * コンストラクタ
     */
    public LoopProcessBlock(Context context) {
        this(context, null);
    }

    public LoopProcessBlock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoopProcessBlock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        View.inflate(context, R.layout.process_block_loop, this);

        // ネスト処理ブロック初期処理
        init();
    }

    /*
     * 初期化処理
     */
    private void init() {
        mProcessType = PROCESS_TYPE_LOOP;

        // ネスト内処理indexを初期化
        mBlockInLoopIndex = 0;
    }

    /*
     * 処理文言を設定
     */
    private void setProcessWording() {

        // 処理内容と単位の文言ID
        int contentId;

        // 種別に応じた文言IDを取得
        switch (mProcessKind) {
            case PROC_KIND_LOOP_OBSTACLE:
            default:
                contentId = R.string.block_contents_loop;
                break;
        }

        // 文言IDをレイアウトに設定
        TextView tv_contents = findViewById(R.id.tv_contents);
        tv_contents.setText(contentId);
    }


    /*
     * 「プログラミング処理種別」の設定
     */
    @Override
    public void setProcessKind(int processKind) {
        super.setProcessKind(processKind);

        // 種別に応じた文言に変更
        setProcessWording();
    }


    /*
     * ネスト内の処理ブロックを取得
     * 　返す対象は、先頭から順番に行う。
     * 　最後の処理ブロックまで返した場合、再度、先頭の処理ブロックから返す
     */
    @Override
    public ProcessBlock getProcessInNest() {

        //--------------------
        // ネスト内処理ブロック
        //--------------------
        // ネスト内処理ブロックをコールされた順に応じて返す
        ViewGroup ll_insideRoot = findViewById(R.id.ll_firstNestRoot);
        ProcessBlock block = (ProcessBlock) ll_insideRoot.getChildAt(mBlockInLoopIndex);

        //--------------------------
        // 返す処理ブロックIndexの更新
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
    public boolean isConditionTrue(CharacterNode characterNode) {

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

