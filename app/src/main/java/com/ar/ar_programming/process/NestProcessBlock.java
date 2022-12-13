package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;

import com.ar.ar_programming.CharacterNode;
import com.ar.ar_programming.R;


/*
 * ネストあり処理ブロック基底クラス
 */
public abstract class NestProcessBlock extends ProcessBlock {

    //---------------------------
    // 定数
    //---------------------------
    public static int NEST_EXPAND = 0;
    public static int NEST_SHRINK = 1;

    //---------------------------
    // フィールド変数
    //---------------------------
    private StartBlock mNestStartBlock;

    /*
     * コンストラクタ
     */
    public NestProcessBlock(Context context) {
        this(context, null);
    }

    public NestProcessBlock(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0, 0);
    }

    public NestProcessBlock(Context context, AttributeSet attrs, int defStyle, int type, int contents) {
        super(context, attrs, defStyle, type, contents);
    }

    /*
     * ネスト内条件判定
     */
    public abstract boolean isCondition(CharacterNode characterNode);

    /*
     * ネスト内処理ブロックの取得
     */
    public abstract ProcessBlock getBlockInNest();

    /*
     * レイアウト設定
     */
    @Override
    public void setLayout(int layoutID) {
        super.setLayout(layoutID);

        // 処理ブロックタッチリスナー
        setBlockTouchListerer();
        // 処理ブロック内の内容を書き換え
        rewriteProcessContents(mProcessContents);
    }

    /*
     * ブロック半透明化
     */
    @Override
    public void tranceDrag() {
        super.tranceDrag();

        //------------------------
        // ネスト内ブロックを半透明化
        //------------------------
        Block block = getNestStartBlock();
        while (block != null) {
            block.tranceDrag();
            block = block.getBelowBlock();
        }
    }

    /*
     * ブロック位置移動：上
     */
    @Override
    public void upChartPosition(int trancelate) {
        super.upChartPosition(trancelate);

        //------------------------
        // ネスト内ブロックを移動
        //------------------------
        Block block = getNestStartBlock();
        block.upChartPosition(trancelate);
        /*while (block != null) {
            block.upChartPosition(trancelate);
            block = block.getBelowBlock();
        }*/
    }

    /*
     * ブロック位置移動：下
     */
    @Override
    public void downChartPosition(int trancelate) {
        super.downChartPosition(trancelate);

        //------------------------
        // ネスト内ブロックを移動
        //------------------------
        Block block = getNestStartBlock();
        block.downChartPosition(trancelate);
/*        while (block != null) {
            block.downChartPosition(trancelate);
            block = block.getBelowBlock();
        }*/
    }

    /*
     * ブロック削除
     */
    @Override
    public void removeOnChart() {
        super.removeOnChart();

        //---------------------
        // ネスト内ブロックを削除
        //---------------------
        Block block = getNestStartBlock();
        while (block != null) {
            block.removeOnChart();
            block = block.getBelowBlock();
        }
    }

    /*
     * ネストサイズの変更
     */
    public int resizeNestHeight(Block block, int scaling) {

        // 変更量
        int size = block.getHeight();
        if( scaling == NEST_SHRINK ){
            size *= -1;
        }

        // ネストサイズの変更
        ViewGroup nestView = getResizeNest( block );
        ViewGroup.LayoutParams lp = nestView.getLayoutParams();
        lp.height += size;
        nestView.setLayoutParams(lp);

        // 自身がネスト内にあれば、そのネストもサイズ変更
        NestProcessBlock nestBlock = getOwnNestBlock();
        if (nestBlock != null) {
            nestBlock.resizeNestHeight(block, scaling);
        }

        return size;
    }

    /*
     * ネストサイズ変更対象のネスト
     *   ※本クラスのネストは１つであるため、firstネスト固定で返す
     *   ※本クラスを継承し、ネストを複数用意する場合、本メソッドをOverrideすること
     */
    public ViewGroup getResizeNest( Block block ){
        return findViewById( R.id.ll_firstNestRoot );
    }

    /*
     * ネスト内に指定されたブロックがあるか
     */
    @Override
    public boolean hasBlock(Block checkBlock ) {

        Block nestBlock = getNestStartBlock();
        while( nestBlock != null ){
            // ネスト内ブロックが指定ブロックの場合
            if( nestBlock == checkBlock ){
                // ありとして終了
                return true;
            }

            // 「ネスト内ブロックの中のブロック」をチェック
            if( nestBlock.hasBlock( checkBlock ) ){
                // あれば終了
                return true;
            }

            // 次のネスト内ブロックへ
            nestBlock = nestBlock.getBelowBlock();
        }

        // 見つからないルート
        return false;
    }

    /*
     * ネスト内スタートブロックの設定
     */
    public void setNestStartBlock(StartBlock block ) {
        mNestStartBlock = block;
    }
    /*
     * ネスト内スタートブロックの取得
     */
    public StartBlock getNestStartBlock() {
        return mNestStartBlock;
    }

    /*
     * ネスト内の先頭ブロック（スタートブロックの次のブロック）
     */
    public Block getNestTopBlock() {
        // ネスト内の先頭ブロック
        return mNestStartBlock.getBelowBlock();
    }

    /*
     * ネストビューの取得
     */
    public ViewGroup getNestView() {
        return findViewById( R.id.ll_firstNestRoot );
    }

    /*
     * ネスト内スタートブロック初期設定
     */
    public void initStartBlockInNest( int layoutID ) {

        StartBlock startBlock = mNestStartBlock;

        // IDを動的に設定（他のネストブロックと重複しないようにするため）
        startBlock.setId(View.generateViewId());
        // レイアウト設定
        startBlock.setLayout( layoutID );
        // マーカー無効化
        startBlock.setMarker( false );
        // スタートブロックにネスト情報を設定
        startBlock.setOwnNestBlock( this );
    }

    /*
     * ネスト内処理ブロック数の取得
     */
    public int getBlockSizeInNest() {
        // 指定された位置の処理ブロックを返す
        ViewGroup ll_insideRoot = findViewById(R.id.ll_firstNestRoot);
        return ll_insideRoot.getChildCount();
    }

    /*
     * ネスト内マークエリアリスナーの設定
     */
/*    public void setMarkAreaInNestListerner(MarkerAreaListener listener) {

        StartBlock startBlock = mNestStartBlock;
        int markAreaID = startBlock.getMarkAreaViewID();
        ViewGroup markArea = startBlock.findViewById( markAreaID );

        markArea.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // リスナーコール
                listener.onBottomMarkerAreaClick(startBlock);
            }
        });
    }*/

    /*
     * ネスト内ドロップリスナーの設定
     */
/*    public void setDropInNestListerner(DropBlockListener listener) {

        // ネスト内のスタートブロックにリスナーを設定
        StartBlock startBlock = mNestStartBlock;
        startBlock.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                return listener.onDropBlock( (Block)view, dragEvent );
            }
        });
    }*/

}

