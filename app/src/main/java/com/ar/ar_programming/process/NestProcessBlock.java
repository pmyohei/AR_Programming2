package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
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
    public static final int NEST_FIRST = 0;
    public static final int NEST_SECOND = 1;

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

        createStartBlock();
    }

    /*
     * ネスト内条件判定
     */
    public abstract boolean isCondition(CharacterNode characterNode);

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
     * ブロック削除
     */
    @Override
    public void removeOnChart() {
        super.removeOnChart();

        //---------------------
        // ネスト内ブロックを削除
        //---------------------
        Block block = getStartBlockForNest( NEST_FIRST );
        while (block != null) {
            block.removeOnChart();
            block = block.getBelowBlock();
        }
    }

    /*
     * ネストスタートブロック生成
     */
    public void createStartBlock() {
        mNestStartBlock = new StartBlock(getContext());
        mNestStartBlock.setId(View.generateViewId());
        mNestStartBlock.setLayout(R.layout.process_block_start_in_nest);
        mNestStartBlock.setOwnNestBlock(this);

        // 位置配置のタイミングで可視化
        mNestStartBlock.setVisibility(View.INVISIBLE);
    }

    /*
     * ネストスタートブロック配置
     */
    public void deployStartBlock( ViewGroup chartRoot ) {
        // チャートに追加
        ViewGroup.MarginLayoutParams mlp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        chartRoot.addView(mNestStartBlock, mlp);
    }

    /*
     * ブロック位置更新
     */
    @Override
    public void updatePosition() {
        super.updatePosition();

        // 自身の位置が確定したら、ネスト内スタートブロックを更新
        post(() -> {
            mNestStartBlock.updatePosition();
        });
    }

    /*
     * ネスト内スタートブロックを配置するためのTopマージンを取得
     */
    public int getStartBlockTopMargin( int nest ) {
        ViewGroup nestView = getNestViewForNest( nest );
        return getTop() + nestView.getTop();
    }

    /*
     * ネスト内スタートブロックを配置するためのLeftマージンを取得
     */
    public int getStartBlockLeftMargin( int nest ) {
        ViewGroup nestView = getNestViewForNest( nest );
        return getLeft() + nestView.getLeft();
    }

    /*
     * ネストサイズの変更
     *   @para1：本メソッドをコールしたブロック
     * 　　　　　　（リサイズするネストを識別するために必要）
     */
    public void resizeNestHeight(Block calledBlock) {

        //-------------------
        // 対象ネスト
        //-------------------
        int targetNest = inWhichNest( calledBlock );
        Block block = getStartBlockForNest( targetNest );
        ViewGroup nestView = getNestViewForNest( targetNest );

        //-------------------
        // 自ネストサイズの変更
        //-------------------
        // ネストサイズの計算
        int height = 0;
        while (block != null) {
            height += block.getHeight();
            block = block.getBelowBlock();
        }

        // 更新チェック
        ViewGroup.LayoutParams lp = nestView.getLayoutParams();
        if (lp.height == height) {
            return;
        }

        // ネストサイズの変更
        lp.height = height;
        nestView.setLayoutParams(lp);

        //----------------------
        // 親ネストのリサイズ処理
        //----------------------
        nestView.post(() -> {
            // 親ネストのリサイズ処理
            if (inNest()) {
                getOwnNestBlock().resizeNestHeight( this );
            }

            // 下ブロック位置を更新
            if (hasBelowBlock()) {
                getBelowBlock().updatePosition();
            }
        });
    }

    /*
     * 指定ブロックがどのネストにいるか
     * ！本クラスでは、ネスト１を固定で返す
     * ！ネストを複数持つクラス実装時は、実装に合わせてOverrideが必要
     */
    public int inWhichNest( Block calledBlock ) {
        return NEST_FIRST;
    }

    /*
     * 指定ネストのスタートブロックを取得
     * ！本クラスでは、ネスト１を固定で返す
     * ！ネストを複数持つクラス実装時は、実装に合わせてOverrideが必要
     */
    public Block getStartBlockForNest( int nest ) {
        return mNestStartBlock;
    }

    /*
     * 指定ネストのネストviewを取得
     * ！本クラスでは、ネスト１を固定で返す
     * ！ネストを複数持つクラス実装時は、実装に合わせてOverrideが必要
     */
    public ViewGroup getNestViewForNest( int nest ) {
        return findViewById(R.id.ll_firstNestRoot);
    }

    /*
     * ネスト内に指定されたブロックがあるか
     * ！本クラスでは、ネスト１内を検索
     * ！ネストを複数持つクラス実装時は、実装に合わせてOverrideが必要
     */
    @Override
    public boolean hasBlock(Block checkBlock) {

        //---------------------
        // ネスト１内の検索
        //---------------------
        Block block = getStartBlockForNest( NEST_FIRST );
        while (block != null) {
            // ネスト内ブロックが指定ブロックの場合、ありとして終了
            if (block == checkBlock) {
                return true;
            }

            // ネスト内ブロックの中に対象ブロックがあれば、ありとして終了
            if (block.hasBlock(checkBlock)) {
                return true;
            }

            // 次のブロックへ
            block = block.getBelowBlock();
        }

        // なし
        return false;
    }

    /*
     * ネスト内ブロックの有無
     * ！本クラスでは、ネスト１内を判定
     * ！ネストを複数持つクラス実装時は、実装に合わせてOverrideが必要
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
        if (!hasNestBlock()) {
            // 次の処理ブロックへ
            tranceNextBlock(characterNode);
            return;
        }

        //-----------------------------
        // 条件判定
        //-----------------------------
        // 条件未成立の場合
        if (!isCondition(characterNode)) {
            // 次の処理ブロックへ
            tranceNextBlock(characterNode);
            return;
        }

        //-----------------------------
        // ネスト内処理ブロックの実行
        //-----------------------------
        // ネスト内の処理ブロックを実行
        ProcessBlock nextBlock = (ProcessBlock) mNestStartBlock.getBelowBlock();
        nextBlock.startProcess(characterNode);
    }

    /*
     * 処理ブロックドロップリスナーの設定
     */
    @Override
    public void setDropBlockListerner(DropBlockListener listener) {
        super.setDropBlockListerner(listener);

        // ネストスタートブロックにも設定
        mNestStartBlock.setDropBlockListerner(listener);
    }

    /*
     * マークエリアリスナー設定
     */
    @Override
    public void setMarkAreaListerner(MarkerAreaListener listener){
        super.setMarkAreaListerner(listener);

        // ネストスタートブロックにも設定
        mNestStartBlock.setMarkAreaListerner(listener);
    }
}

