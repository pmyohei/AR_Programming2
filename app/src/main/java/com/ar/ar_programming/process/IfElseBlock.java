package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.ar.ar_programming.UserBlockSelectListAdapter;
import com.ar.ar_programming.character.CharacterNode;
import com.ar.ar_programming.Gimmick;
import com.ar.ar_programming.R;


/*
 * ネストあり処理ブロック（if文）
 */
public class IfElseBlock extends IfBlock {

    //---------------------------
    // 定数
    //---------------------------

    //---------------------------
    // フィールド変数
    //---------------------------
    public StartBlock mNestStartBlockSecond;


    /*
     * コンストラクタ
     */
    public IfElseBlock(Context context, Gimmick.XmlBlockInfo xmlBlockInfo) {
        this(context, null, xmlBlockInfo, R.layout.block_if_else);
    }
    public IfElseBlock(Context context, AttributeSet attrs, Gimmick.XmlBlockInfo xmlBlockInfo, int layout) {
        this(context, attrs, 0, xmlBlockInfo, layout);
    }
    public IfElseBlock(Context context, AttributeSet attrs, int defStyle, Gimmick.XmlBlockInfo xmlBlockInfo, int layout) {
        super(context, attrs, defStyle, xmlBlockInfo, layout);
    }

    /*
     * ブロック半透明化
     */
    @Override
    public void tranceOnDrag() {
        super.tranceOnDrag();

        //------------------------------
        // else内ブロックを半透明化
        //------------------------------
        Block block = getStartBlockForNest(NEST_SECOND);
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
        // else内ブロックの透明化を解除
        //--------------------------
        Block block = getStartBlockForNest(NEST_SECOND);
        while (block != null) {
            block.tranceOffDrag();
            block = block.getBelowBlock();
        }
    }

    /*
     * ネストスタートブロック生成
     */
    @Override
    public void createStartBlock() {
        super.createStartBlock();

        //------------------------------------------
        // else内ブロック(2ネスト目)にスタートブロックを配置
        //------------------------------------------
        mNestStartBlockSecond = new StartBlock(getContext());
        mNestStartBlockSecond.setId(View.generateViewId());
        mNestStartBlockSecond.setLayout(R.layout.block_start_in_nest);
        mNestStartBlockSecond.setOwnNestBlock(this);

        // 位置配置のタイミングで可視化
        mNestStartBlockSecond.setVisibility(View.INVISIBLE);
    }

    /*
     * ネストスタートブロック配置
     */
    @Override
    public void deployStartBlock(ViewGroup chartRoot) {
        super.deployStartBlock(chartRoot);

        // チャートに追加
        ViewGroup.MarginLayoutParams mlp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        chartRoot.addView(mNestStartBlockSecond, mlp);
    }

    /*
     * ブロック位置更新
     */
    @Override
    public void updatePosition() {
        super.updatePosition();

        post(() -> {
            mNestStartBlockSecond.updatePosition();
        });
    }

    /*
     * ネストサイズの変更
     */
    @Override
    public void resizeNestHeight(Block calledBlock) {
        super.resizeNestHeight(calledBlock);

        //--------------------------------------------------------------
        // 他ネスト（リサイズ対象ネストよりも下にあるネスト）内ブロックの位置更新
        //--------------------------------------------------------------
        // リサイズ対象がネスト１の場合、ネスト２のブロック位置を更新
        int targetNest = whichInNest(calledBlock);
        if (targetNest == NEST_FIRST) {

            // ネスト１view確定時、ネスト２位置を更新
            ViewGroup nestView = getNestViewForNest(targetNest);
            nestView.post(() -> {
                mNestStartBlockSecond.updatePosition();
            });
        }
    }

    /*
     * ブロック削除
     */
    @Override
    public void removeOnChart(Gimmick gimmick, UserBlockSelectListAdapter adapter) {
        super.removeOnChart( gimmick, adapter );

        //------------------------
        // elseネスト内ブロックを削除
        //------------------------
        Block block = getStartBlockForNest(NEST_SECOND);
        while (block != null) {
            block.removeOnChart( gimmick, adapter );
            block = block.getBelowBlock();
        }
    }

    /*
     * ネスト内に指定されたブロックがあるか
     */
    @Override
    public boolean hasBlock(Block checkBlock) {
        //---------------------
        // if側のネストをチェック
        //---------------------
        // あれば判定終了
        boolean firstNest = super.hasBlock(checkBlock);
        if (firstNest) {
            return true;
        }

        //---------------------
        // else側のネストをチェック
        //---------------------
        // else側のネストを検索
        return searchBlockInNest(NEST_SECOND, checkBlock);
    }

    /*
     * 指定ブロックがどのネストにいるか
     */
    @Override
    public int whichInNest(Block calledBlock) {

        //----------------
        // ネスト１内を検索
        //----------------
        Block block = mNestStartBlockFirst;
        while (block != null) {
            if (block == calledBlock) {
                // 指定ブロックがあれば、ネスト１を返す
                return NEST_FIRST;
            }
            block = block.getBelowBlock();
        }

        // ネスト１内で見つからなければ、ネスト２を返す
        return NEST_SECOND;
    }

    /*
     * 指定ネストのスタートブロックを取得
     */
    @Override
    public Block getStartBlockForNest(int nest) {
        return (nest == NEST_FIRST ? mNestStartBlockFirst : mNestStartBlockSecond);
    }

    /*
     * 指定ネストのネストviewを取得
     */
    @Override
    public ViewGroup getNestViewForNest(int nest) {
        int id = (nest == NEST_FIRST ? R.id.ll_firstNestRoot : R.id.ll_secondNestRoot);
        return findViewById(id);
    }

    /*
     * ブロック処理開始
     */
    @Override
    public void startProcess(CharacterNode characterNode) {

        Log.i("ブロック処理の流れ", "if-else startProcess()開始 action=" + mXmlBlockInfo.action);
        Log.i("ブロック処理の流れ", "if-else startProcess()開始 targetNode_1=" + mXmlBlockInfo.targetNode_1);

        // 条件判定
        boolean isFirstNest = isCondition(characterNode);

        //-----------------------------
        // ネスト内処理ブロック数チェック
        //-----------------------------
        Block passNestStartBlock;
        if( isFirstNest ){
            passNestStartBlock = mNestStartBlockFirst;
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

    /*
     * 処理ブロックドロップリスナーの設定
     */
    @Override
    public void setDropBlockListerner(DropBlockListener listener) {
        super.setDropBlockListerner(listener);

        // ネストスタートブロックにも設定
        mNestStartBlockSecond.setDropBlockListerner(listener);
    }

    /*
     * マークエリアリスナー設定
     */
    @Override
    public void setMarkAreaListerner(MarkerAreaListener listener){
        super.setMarkAreaListerner(listener);

        // ネストスタートブロックにも設定
        mNestStartBlockSecond.setMarkAreaListerner(listener);
    }
}


