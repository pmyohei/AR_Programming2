package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ar.ar_programming.CharacterNode;
import com.ar.ar_programming.R;


/*
 * ネストあり処理ブロック（if文）
 */
public class IfElseProcessBlock extends NestProcessBlock {

    //---------------------------
    // 定数
    //---------------------------
    public static final int PROCESS_CONTENTS_IF_ELSE_BLOCK = 0;

    //---------------------------
    // フィールド変数
    //---------------------------
    private int mBlockInNestIndex;
    private int tmploopCount = 0;
    private boolean isConditionState;
    private ViewGroup mNestRoot;


    /*
     * コンストラクタ
     */
    public IfElseProcessBlock(Context context, int contents) {
        this(context, null, contents);
    }
    public IfElseProcessBlock(Context context, AttributeSet attrs, int contents) {
        this(context, attrs, 0, contents);
    }
    public IfElseProcessBlock(Context context, AttributeSet attrs, int defStyle, int contents) {
        super(context, attrs, defStyle, PROCESS_TYPE_IF_ELSE, contents);
        setLayout( R.layout.process_block_if_else );
        init();
    }

    /*
     * 初期化処理
     */
    private void init() {
        // ネスト内処理indexを初期化
        mBlockInNestIndex = 0;
        // ネスト親レイアウト
        // ※初期状態では、ifルートにしておく
        mNestRoot = findViewById(R.id.ll_firstNestRoot);
        isConditionState = true;
        // ネスト内スタートブロック初期設定
        initStartBlockInNest( R.layout.process_block_start_in_nest );
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
            case PROCESS_CONTENTS_IF_ELSE_BLOCK:
                contentId = R.string.block_contents_if_else_block;
                break;
            default:
                contentId = R.string.block_contents_if_else_block;
                break;
        }

        // 文言IDをレイアウトに設定
        TextView tv_contents = findViewById(R.id.tv_contents);
        tv_contents.setText(contentId);
    }

    /*
     * ネスト内スタートブロック初期設定
     */
    @Override
    public void initStartBlockInNest( int layoutID ) {
        super.initStartBlockInNest( layoutID );

        //------------------
        // else側のネスト設定
        //------------------
        // レイアウト設定
        StartBlock pb_startSecond = findViewById( R.id.pb_startSecond );
        pb_startSecond.setLayout( layoutID );
        // マーカー無効化
        pb_startSecond.setMarker( false );
        // スタートブロックにネスト情報を設定
        pb_startSecond.setOwnNestBlock( this );
    }

    /*
     * ネスト内マークエリアリスナーの設定
     */
    @Override
    public void setMarkAreaInNestListerner(MarkerAreaListener listener) {
        super.setMarkAreaInNestListerner( listener );

        //------------------
        // else側のネスト設定
        //------------------
        StartBlock pb_startSecond = findViewById( R.id.pb_startSecond );
        ViewGroup cl_bottomMarkArea = pb_startSecond.findViewById(R.id.cl_bottomMarkArea);
        cl_bottomMarkArea.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // リスナーコール
                listener.onBottomMarkerAreaClick(pb_startSecond);
            }
        });
    }

    /*
     * ネスト内の処理ブロック数を取得
     */
    @Override
    public int getBlockSizeInNest() {
        return mNestRoot.getChildCount();
    }

    /*
     * ネスト内の処理ブロックを取得
     * 　返す対象は、先頭から順番に行う。
     * 　ただし、以下の状況にある場合、nullを返す
     *   ・ネスト内の処理ブロック数が0
     *   ・ネスト内の処理ブロックを最後まで返した
     */
    @Override
    public ProcessBlock getBlockInNest() {

        //--------------------
        // 取得処理ブロックチェック
        //--------------------
        int blockInNestNum = mNestRoot.getChildCount();
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
        ProcessBlock block = (ProcessBlock) mNestRoot.getChildAt( mBlockInNestIndex );
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
    public boolean isCondition(CharacterNode characterNode) {

        tmploopCount++;
//        boolean tmp = (tmploopCount == 2);
        boolean tmp = true;

        // 条件の真偽値に応じたネストルートレイアウトを取得
        if( tmp ){
            mNestRoot = findViewById(R.id.ll_firstNestRoot);
        } else{
            mNestRoot = findViewById(R.id.ll_secondNestRoot);
        }

        // 判定結果を保持
        isConditionState = tmp;
        return isConditionState;
    }

}


