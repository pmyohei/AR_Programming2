package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ar.ar_programming.CharacterNode;
import com.ar.ar_programming.R;


/*
 * ネストあり処理ブロック（if文）
 */
public class IfProcessBlock extends NestProcessBlock {

    //---------------------------
    // 定数
    //---------------------------

    //---------------------------
    // フィールド変数
    //---------------------------
    private int mBlockInNestIndex;
    private int tmploopCount = 0;

    /*
     * コンストラクタ
     */
    public IfProcessBlock(Context context) {
        this(context, null);
    }

    public IfProcessBlock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IfProcessBlock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        View.inflate(context, R.layout.process_block_if, this);

        // ネスト処理ブロック初期処理
        init();
    }

    /*
     * 初期化処理
     */
    private void init() {
        mProcessType = PROCESS_TYPE_IF;

        // ネスト内処理indexを初期化
        mBlockInNestIndex = 0;

        // onDragリスナーの設定（入れ子の親レイアウト側）
        setDragAndDropFirstNestListerner();
        // onDragリスナーの設定
        setDragAndDropListerner();
    }

    /*
     * 処理文言を設定
     */
    private void setProcessWording() {

        // 処理内容と単位の文言ID
        int contentId;

        // 種別に応じた文言IDを取得
        switch (mProcessKind) {
            case PROC_KIND_IF:
                contentId = R.string.block_contents_if;
                break;

            case PROC_KIND_IF_ELSE:
                contentId = R.string.block_contents_if;
                break;

            default:
                contentId = R.string.block_contents_if;
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
     * 　ただし、以下の状況にある場合、nullを返す
     *   ・ネスト内の処理ブロック数が0
     *   ・ネスト内の処理ブロックを最後まで返した
     */
    @Override
    public ProcessBlock getProcessInNest() {

        ViewGroup ll_insideRoot = findViewById(R.id.ll_firstNestRoot);

        //--------------------
        // 取得処理ブロックチェック
        //--------------------
        int blockInNestNum = ll_insideRoot.getChildCount();
        if( blockInNestNum == 0 ){
            // 処理ブロックなし
            return null;
        }
        if( mBlockInNestIndex >= blockInNestNum ){
            // 処理ブロック最後まで取得
            return null;
        }

        //--------------------
        // ネスト内処理ブロック
        //--------------------
        // ネスト内処理ブロックをコールされた順に応じて返す
        ProcessBlock block = (ProcessBlock)ll_insideRoot.getChildAt( mBlockInNestIndex );
        // 次回コールでは次の処理ブロックを返すために、indexを進める
        mBlockInNestIndex++;

        return block;
    }


    /*
     * 条件成立判定
     *   @return：条件成立- true
     *   @return：条件不成立- false
     */
    @Override
    public boolean isConditionTrue(CharacterNode characterNode) {

        return true;

/*        tmploopCount++;
        boolean tmp = (tmploopCount == 2);
        return tmp;*/
    }

}

