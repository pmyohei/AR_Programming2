package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
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

    private StartBlock mNestStartBlockSecond;


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
     * ブロック半透明化
     */
    @Override
    public void tranceOnDrag(){
        super.tranceOnDrag();

        //------------------------------
        // ネスト(2つ目)内ブロックを半透明化
        //------------------------------
        Block block = getSecondNestStartBlock();
        while( block != null ){
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
        Block block = getSecondNestStartBlock();
        while (block != null) {
            block.tranceOffDrag();
            block = block.getBelowBlock();
        }
    }

    /*
     * ブロック位置移動：上
     */
    @Override
    public void upChartPosition( int trancelate ){
        super.upChartPosition( trancelate );

        //------------------------
        // ネスト内ブロックを移動
        //------------------------
        Block block = getSecondNestStartBlock();
        block.upChartPosition( trancelate );
        /*while( block != null ){
            block.upChartPosition( trancelate );
            block = block.getBelowBlock();
        }*/
    }

    /*
     * ブロック位置移動：下
     */
    @Override
    public void downChartPosition( int trancelate ){
        super.downChartPosition( trancelate );

        //------------------------
        // elseネスト内ブロックを移動
        //------------------------
        Block block = getSecondNestStartBlock();
        block.downChartPosition( trancelate );
        /*while( block != null ){
            block.downChartPosition( trancelate );
            block = block.getBelowBlock();
        }*/
    }

    /*
     * ブロック削除
     */
    @Override
    public void removeOnChart(){
        super.removeOnChart();

        //------------------------
        // elseネスト内ブロックを削除
        //------------------------
        Block block = getSecondNestStartBlock();
        while( block != null ){
            block.removeOnChart();
            block = block.getBelowBlock();
        }
    }

    /*
     * ネスト内に指定されたブロックがあるか
     */
    @Override
    public boolean hasBlock(Block checkBlock ) {
        boolean firstNest = super.hasBlock( checkBlock );
        if( firstNest ){
            return true;
        }

        //---------------------
        // else側のネストをチェック
        //---------------------
        Block nestBlock = getSecondNestStartBlock();
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
     * ネストサイズの変更
     */
    @Override
    public int resizeNestHeight(Block block, int scaling) {
        int trancelate = super.resizeNestHeight( block, scaling );

        //------------------------
        // elseネスト内ブロックを移動
        //------------------------
        // リサイズネストが１つ目の場合
        if ( isBlockInFirstNest(block) ) {
            Block seocndStartBlock = getSecondNestStartBlock();

            // 変化量を絶対値にする
            trancelate = Math.abs(trancelate);

            if( scaling == NEST_EXPAND ){
                seocndStartBlock.downChartPosition( trancelate );
            } else {
                seocndStartBlock.upChartPosition( trancelate );
            }
        }

        return trancelate;
    }

    /*
     * ネストサイズ変更対象のネスト
     */
    @Override
    public ViewGroup getResizeNest( Block block ){

        if( isBlockInFirstNest(block) ){
            return findViewById( R.id.ll_firstNestRoot );
        } else {
            return findViewById( R.id.ll_secondNestRoot );
        }

/*        // １つ目のネストをチェック
        Block nestBlock = getNestStartBlock();
        while( nestBlock != null ){
            if( nestBlock == block ){
                return findViewById( R.id.ll_firstNestRoot );
            }
            nestBlock = nestBlock.getBelowBlock();
        }

        // １つ目になければ２つ目のネスト
        return findViewById( R.id.ll_secondNestRoot );*/
    }

    /*
     * 指定ブロックがFirstネストにあるかどうか
     */
    private boolean isBlockInFirstNest(Block block ){

        // １つ目のネストをチェック
        Block nestBlock = getNestStartBlock();
        while( nestBlock != null ){
            if( nestBlock == block ){
                return true;
            }
            nestBlock = nestBlock.getBelowBlock();
        }

        // １つ目になければ２つ目のネスト
        return false;
    }

    /*
     * ネストビューの取得
     */
    public ViewGroup getSecondNestView() {
        return findViewById( R.id.ll_secondNestRoot );
    }

    /*
     * ネスト内スタートブロックの設定
     */
    public void setSecondNestStartBlock(StartBlock block ) {
        mNestStartBlockSecond = block;
    }
    /*
     * ネスト内スタートブロックの取得
     */
    public StartBlock getSecondNestStartBlock() {
        return mNestStartBlockSecond;
    }

    /*
     * elseネスト内スタートブロックを取得
     */
    private StartBlock getStartBlockInSecondNest() {
        return mNestStartBlockSecond;
    }

    /*
     * ネスト内スタートブロック初期設定
     */
/*
    @Override
    public void initStartBlockInNest( int layoutID ) {
        super.initStartBlockInNest( layoutID );

        //------------------
        // else側のネスト設定
        //------------------
        StartBlock pb_startSecond = getStartBlockInSecondNest();

        // IDを動的に設定（他のネストブロックと重複しないようにするため）
        pb_startSecond.setId(View.generateViewId());
        // レイアウト設定
        pb_startSecond.setLayout( layoutID );
        // マーカー無効化
        pb_startSecond.setMarker( false );
        // スタートブロックにネスト情報を設定
        pb_startSecond.setOwnNestBlock( this );
    }
*/

    /*
     * ネスト内マークエリアリスナーの設定
     */
/*    @Override
    public void setMarkAreaInNestListerner(MarkerAreaListener listener) {
        super.setMarkAreaInNestListerner( listener );

        //------------------
        // else側のネスト設定
        //------------------
        StartBlock pb_startSecond = getStartBlockInSecondNest();
        ViewGroup cl_markAreaInStart = pb_startSecond.findViewById(R.id.cl_markAreaInStart);
        cl_markAreaInStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // リスナーコール
                listener.onBottomMarkerAreaClick(pb_startSecond);
            }
        });
    }*/

    /*
     * ネスト内ドロップリスナーの設定
     */
/*    @Override
    public void setDropInNestListerner(DropBlockListener listener) {
        super.setDropInNestListerner( listener );

        //------------------
        // else側のネスト設定
        //------------------
        StartBlock pb_startSecond = getStartBlockInSecondNest();
        pb_startSecond.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                return listener.onDropBlock( (Block)view, dragEvent );
            }
        });
    }*/

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

        int i = 0;

        Block below = mNestStartBlock.getBelowBlock();
        while( below != null ){
            below = below.getBelowBlock();
            i++;
        }

        return ( i % 2 != 0 );

/*        tmploopCount++;
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
        return isConditionState;*/
    }

    /*
     * 処理開始
     */
    @Override
    public void startProcess(CharacterNode characterNode) {

        // 条件判定
        boolean isFirstNest = isCondition(characterNode);

        //-----------------------------
        // ネスト内処理ブロック数チェック
        //-----------------------------
        Block passNestStartBlock;
        if( isFirstNest ){
            passNestStartBlock = mNestStartBlock;
        } else {
            passNestStartBlock = mNestStartBlockSecond;
        }

        // ネスト内ブロックがなければ、次の処理へ
        if( !passNestStartBlock.hasBelowBlock() ){
            // 次の処理ブロックへ
            tranceNextBlock(characterNode);
            return;
        }

        //-----------------------------
        // ネスト内処理ブロックの実行
        //-----------------------------
        // ネスト内の処理ブロックを実行
        ProcessBlock nextBlock = (ProcessBlock)passNestStartBlock.getBelowBlock();
        nextBlock.startProcess( characterNode );
    }

}


