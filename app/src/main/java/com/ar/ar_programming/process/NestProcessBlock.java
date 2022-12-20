package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

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
    public StartBlock mNestStartBlock;

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
    public void tranceOnDrag() {
        super.tranceOnDrag();

        //------------------------
        // ネスト内ブロックを半透明化
        //------------------------
        Block block = mNestStartBlock;
        while (block != null) {
            block.tranceOnDrag();
            block = block.getBelowBlock();
        }
    }

    /*
     * ブロック半透明化解除
     */
    @Override
    public void tranceOffDrag() {
        super.tranceOffDrag();

        //--------------------------
        // ネスト内ブロックの透明化を解除
        //--------------------------
        Block block = mNestStartBlock;
        while (block != null) {
            block.tranceOffDrag();
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
     * ブロック位置更新
     */
    @Override
    public void updatePosition() {
        super.updatePosition();

        // 自身のレイアウトが確定したとき
        post(() -> {
            // ネストスタート前なら、処理なし
            if( mNestStartBlock == null ){
                return;
            }

            // ネスト内スタートブロックの位置を更新
            updateStartBlockPosition();

            // ネスト内スタートブロックに続くブロック位置を更新
            mNestStartBlock.post(() -> {
                if (mNestStartBlock.hasBelowBlock()) {
                    mNestStartBlock.getBelowBlock().updatePosition();
                }
            });

            // ネストリサイズ
            resizeNestHeight();
        });
    }

    /*
     * ネスト内スタートブロックの付与
     */
    public void addStartBlock(StartBlock startBlock, ViewGroup chartRoot) {

        // 保持
        mNestStartBlock = startBlock;

        //---------------
        // マージンの算出
        //---------------
        post(() -> {
            ViewGroup.MarginLayoutParams mlp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            int left = getStartBlockLeftMargin();
            int top = getStartBlockTopMargin();

            mlp.setMargins(left, top, 0,0);

            Log.i("ネスト問題", "初期位置() top=" + top);

            // チャートに追加
            chartRoot.addView(startBlock, mlp);
        });
    }

    /*
     *
     */
    private int getStartBlockTopMargin() {
        return getTop() + getNestView().getTop();
    }

    /*
     *
     */
    private int getStartBlockLeftMargin() {
        return getLeft() + getNestView().getLeft();
    }

    /*
     *
     */
    public void updateStartBlockPosition() {

        // 上と左マージンを取得
        int left = getStartBlockLeftMargin();
        int top = getStartBlockTopMargin();

        Log.i("ネスト問題", "更新位置() top=" + top);

        // スタートブロック位置を更新
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) mNestStartBlock.getLayoutParams();
        mlp.setMargins(left, top, mlp.rightMargin, mlp.bottomMargin);
        mNestStartBlock.setLayoutParams( mlp );
    }

    /*
     *
     */
    @Override
    public void setChartPosition( Block aboveBlock ) {

        Log.i("ネスト移動", "setChartPosition()　コール ネスト側");

        int top = aboveBlock.getTop() + aboveBlock.getHeight();
        int left = aboveBlock.getLeft();

        ViewGroup.MarginLayoutParams currentMlp = (ViewGroup.MarginLayoutParams) getLayoutParams();
        int diffLeft = left - currentMlp.leftMargin;
        int diffTop = top - currentMlp.topMargin;

        super.setChartPosition( aboveBlock );

        Block block = getNestStartBlock();
        while (block != null) {
            ViewGroup.MarginLayoutParams setMlp = (ViewGroup.MarginLayoutParams) block.getLayoutParams();
            block.setChartPosition(setMlp.leftMargin + diffLeft, setMlp.topMargin + diffTop);
            block = block.getBelowBlock();
        }
    }

    /*
     * ネストサイズの変更
     */
    public void resizeNestHeight() {

        int height = 0;

        // ネストサイズの計算
        Block block = mNestStartBlock;
        while (block != null) {
            height += block.getHeight();
            block = block.getBelowBlock();
        }

        // ネストサイズの変更
        ViewGroup nestView = getNestView();
        ViewGroup.LayoutParams lp = nestView.getLayoutParams();
        lp.height = height;
        nestView.setLayoutParams(lp);
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

        // 自身がネスト内にあれば、親ネストもサイズ変更
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
     * 本ネストブロックの下にあるブロックを下げる。
     * また、親ネストがある場合、親ネストにも本メソッドをコールし同じ事をさせる
     */
    public void downNestBelowBlock( int trancelate ) {
        // 本ブロックの下のブロックを下げる
        if( hasBelowBlock() ){
            Block nestBelowBlock = getBelowBlock();
            nestBelowBlock.downChartPosition( trancelate );
        }

        // 「"本ブロックの親ネストブロック"の下のブロック」を下げる
        if( inNest() ){
            NestProcessBlock parentNest = getOwnNestBlock();
            parentNest.downNestBelowBlock( trancelate );
        }
    }

    /*
     * 本ネストブロックの下にあるブロックを上げる。
     * また、親ネストがある場合、親ネストにも本メソッドをコールし同じ事をさせる
     */
    public void upNestBelowBlock( int trancelate ) {
        // 本ブロックの下のブロックを下げる
        if( hasBelowBlock() ){
            Block nestBelowBlock = getBelowBlock();
            nestBelowBlock.upChartPosition( trancelate );
        }

        // 「"本ブロックの親ネストブロック"の下のブロック」を上げる
        if( inNest() ){
            NestProcessBlock parentNest = getOwnNestBlock();
            parentNest.upNestBelowBlock( trancelate );
        }
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
     * ネスト内処理ブロック数の取得
     */
    public int getBlockSizeInNest() {
        // 指定された位置の処理ブロックを返す
        ViewGroup ll_insideRoot = findViewById(R.id.ll_firstNestRoot);
        return ll_insideRoot.getChildCount();
    }

    /*
     * ネスト内処理ブロックの有無
     */
    public boolean hasNestBlock() {
        return mNestStartBlock.hasBelowBlock();
    }

    /*
     * 処理開始
     */
    @Override
    public void startProcess(CharacterNode characterNode) {

        //-----------------------------
        // ネスト内処理ブロック数チェック
        //-----------------------------
        // ネスト内に処理ブロックがなければ
        if( !hasNestBlock() ){
            // 次の処理ブロックへ
            tranceNextBlock(characterNode);
            return;
        }

        //-----------------------------
        // 条件判定
        //-----------------------------
        // 条件未成立の場合
        if ( !isCondition(characterNode) ) {
            // 次の処理ブロックへ
            tranceNextBlock(characterNode);
            return;
        }

        //-----------------------------
        // ネスト内処理ブロックの実行
        //-----------------------------
        // ネスト内の処理ブロックを実行
        ProcessBlock nextBlock = (ProcessBlock)mNestStartBlock.getBelowBlock();
        nextBlock.startProcess( characterNode );
    }

}

