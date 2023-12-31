package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ar.ar_programming.GimmickManager;
import com.ar.ar_programming.UserBlockSelectListAdapter;
import com.ar.ar_programming.character.CharacterNode;
import com.ar.ar_programming.Gimmick;
import com.ar.ar_programming.R;


/*
 * ネストあり処理ブロック（if-elseif-else文）
 */
public class IfElseIfElseBlock extends IfElseBlock {

    //---------------------------
    // 定数
    //---------------------------
    public static final int PROCESS_CONTENTS_IF_ELSEIF_ELSE_EATABLE_POISON = 0;

    //---------------------------
    // フィールド変数
    //---------------------------
    private StartBlock mNestStartBlockThird;


    /*
     * コンストラクタ
     */
    public IfElseIfElseBlock(Context context, Gimmick.XmlBlockInfo xmlBlockInfo) {
        this(context, null, xmlBlockInfo);
    }

    public IfElseIfElseBlock(Context context, AttributeSet attrs, Gimmick.XmlBlockInfo xmlBlockInfo) {
        this(context, attrs, 0, xmlBlockInfo);
    }

    public IfElseIfElseBlock(Context context, AttributeSet attrs, int defStyle, Gimmick.XmlBlockInfo xmlBlockInfo) {
        super(context, attrs, defStyle, xmlBlockInfo, R.layout.block_if_elseif_else);
        // !レイアウトの設定は「IfElseBlock」クラスで行う
        init();
    }

    /*
     * 初期化処理
     */
    private void init() {
        // 処理ブロック内（else if文）の内容を書き換え
        rewriteElseIfContents();
    }

    /*
     * 処理ブロック内の内容を書き換え：else if文
     */
    public void rewriteElseIfContents() {

        //------------------------
        // else if文
        //------------------------
        // 文言IDをレイアウトに設定
        TextView tv_elseIfContents = findViewById(R.id.tv_elseIfContents);
        String statement = GimmickManager.getBlockStatement(getContext(), mXmlBlockInfo.type, mXmlBlockInfo.action, mXmlBlockInfo.targetNode_2);
        tv_elseIfContents.setText(statement);
    }

    /*
     * ブロック半透明化
     */
    @Override
    public void tranceOnDrag() {
        super.tranceOnDrag();

        //------------------------------
        // else内ブロック(3ネスト目)を半透明化
        //------------------------------
        Block block = getStartBlockForNest(NEST_THIRD);
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

        //----------------------------------
        // else内ブロック(3ネスト目)の透明化を解除
        //----------------------------------
        Block block = getStartBlockForNest(NEST_THIRD);
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
        // else内ブロック(3ネスト目)にスタートブロックを配置
        //------------------------------------------
        mNestStartBlockThird = new StartBlock(getContext());
        mNestStartBlockThird.setId(View.generateViewId());
        mNestStartBlockThird.setLayout(R.layout.block_start_in_nest);
        mNestStartBlockThird.setOwnNestBlock(this);

        // 位置配置のタイミングで可視化
        mNestStartBlockThird.setVisibility(View.INVISIBLE);
    }

    /*
     * ネストスタートブロック配置
     */
    @Override
    public void deployStartBlock(ViewGroup chartRoot) {
        super.deployStartBlock(chartRoot);

        // チャートに追加
        MarginLayoutParams mlp = new MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        chartRoot.addView(mNestStartBlockThird, mlp);
    }

    /*
     * ブロック位置更新
     */
    @Override
    public void updatePosition() {
        super.updatePosition();

        post(() -> {
            mNestStartBlockThird.updatePosition();
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
        // リサイズがネスト３（下にネストがない）なら処理なし
        int targetNest = whichInNest(calledBlock);
        if (targetNest == NEST_THIRD) {
            return;
        }

        // ネスト１or２のview確定時、ネスト３位置を更新
        ViewGroup nestView = getNestViewForNest(targetNest);
        nestView.post(() -> {
            mNestStartBlockThird.updatePosition();
        });
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
        Block block = getStartBlockForNest(NEST_THIRD);
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
        // if側/elseif側のネストをチェック
        //---------------------
        // あれば判定終了
        boolean firstSecondNest = super.hasBlock(checkBlock);
        if ( firstSecondNest ) {
            return true;
        }

        //---------------------
        // else側のネストをチェック
        //---------------------
        // else側のネストを検索
        return searchBlockInNest(NEST_THIRD, checkBlock);
    }

    /*
     * 指定ブロックがどのネストにいるか判定する
     */
    @Override
    public int whichInNest(Block calledBlock) {

        //----------------------------------
        // ネスト１が返れば、ネスト１にあること確定
        //----------------------------------
        int nest = super.whichInNest(calledBlock);
        if (nest == NEST_FIRST) {
            return nest;
        }

        //----------------
        // ネスト２内を検索
        //----------------
        Block block = mNestStartBlockSecond;
        while (block != null) {
            if (block == calledBlock) {
                // 指定ブロックがあれば、ネスト２を返す
                return NEST_SECOND;
            }
            block = block.getBelowBlock();
        }

        // ネスト２内で見つからなければ、ネスト３にあり
        return NEST_THIRD;
    }

    /*
     * 指定ネストのスタートブロックを取得
     */
    @Override
    public Block getStartBlockForNest(int nest) {

        switch (nest) {
            case NEST_FIRST:
                return mNestStartBlockFirst;

            case NEST_SECOND:
                return mNestStartBlockSecond;

            case NEST_THIRD:
                return mNestStartBlockThird;

            default:
                // ありえないルート
                return mNestStartBlockFirst;
        }
    }

    /*
     * 指定ネストのネストviewを取得
     */
    @Override
    public ViewGroup getNestViewForNest(int nest) {

        int id;

        switch (nest) {
            case NEST_FIRST:
                id = R.id.ll_firstNestRoot;
                break;

            case NEST_SECOND:
                id = R.id.ll_secondNestRoot;
                break;

            case NEST_THIRD:
                id = R.id.ll_thirdNestRoot;
                break;

            default:
                // ありえないルート
                id = R.id.ll_firstNestRoot;
        }

        return findViewById(id);
    }

    /*
     * ネストルート判定
     *   3分岐の内、どのルートを通る状態であるかを判定する
     */
    public int judgeTrueNestRoot(CharacterNode characterNode) {

        switch (mXmlBlockInfo.action) {

            // 目の前にxxxがあるか
            case GimmickManager.BLOCK_CONDITION_FRONT:
                return judgeTrueNestRootOnFront(characterNode);

            // 「xxxの方をキャラクターが向いてるか」
            case GimmickManager.BLOCK_CONDITION_FACING:
                return judgeTrueNestRootOnFacing(characterNode);

            default:
                break;
        }

        return NEST_FIRST;
    }

    /*
     * ネストルート判定：条件動作：目の前にxxxがあるかどうか
     */
    public int judgeTrueNestRootOnFront(CharacterNode characterNode) {

        Log.i("条件判定エラー", "judgeTrueNestRootOnFront()");
        Log.i("条件判定エラー", "mXmlBlockInfo.targetNode_1=" + mXmlBlockInfo.targetNode_1);
        Log.i("条件判定エラー", "mXmlBlockInfo.targetNode_2=" + mXmlBlockInfo.targetNode_2);

        //--------------
        // 条件１判定
        //--------------
        boolean isFirstCondition = characterNode.isNodeCollision(mXmlBlockInfo.targetNode_1);
        if (isFirstCondition) {
            Log.i("条件判定エラー", "if通る判定");
            // ifブロックを通る状態
            return NEST_FIRST;
        }

        //--------------
        // 条件２判定
        //--------------
        boolean isSecondCondition = characterNode.isNodeCollision(mXmlBlockInfo.targetNode_2);
        if (isSecondCondition) {
            Log.i("条件判定エラー", "else if通る判定");
            // else ifブロックを通る状態
            return NEST_SECOND;
        }

        // elseブロックを通る状態
        return NEST_THIRD;
    }

    /*
     * ネストルート判定：条件動作：xxxの方をキャラクターが向いてるか
     */
    public int judgeTrueNestRootOnFacing(CharacterNode characterNode) {

        //--------------
        // 条件１判定
        //--------------
        boolean isFirstCondition = isConditionFacing(characterNode, mXmlBlockInfo.targetNode_1);
        if (isFirstCondition) {
            // ifブロックを通る状態
            return NEST_FIRST;
        }

        //--------------
        // 条件２判定
        //--------------
        boolean isSecondCondition = isConditionFacing(characterNode, mXmlBlockInfo.targetNode_2);
        if (isSecondCondition) {
            // else ifブロックを通る状態
            return NEST_SECOND;
        }

        // elseブロックを通る状態
        return NEST_THIRD;
    }

    /*
     * ブロック処理開始
     */
    @Override
    public void startProcess(CharacterNode characterNode) {

        // 条件が真のネストルート
        int nestRoot = judgeTrueNestRoot(characterNode);

        //-----------------------------
        // ネスト内処理ブロック数チェック
        //-----------------------------
        Block passNestStartBlock;
        if( nestRoot == NEST_FIRST ){
            passNestStartBlock = mNestStartBlockFirst;
        } else if( nestRoot == NEST_SECOND ) {
            passNestStartBlock = mNestStartBlockSecond;
        } else {
            passNestStartBlock = mNestStartBlockThird;
        }

        // ネスト内にブロックがなければ、次の処理ブロックへ
        if( !passNestStartBlock.hasBelowBlock() ){
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
     * ブロックドロップリスナーの設定
     */
    @Override
    public void setDropBlockListerner(DropBlockListener listener) {
        super.setDropBlockListerner(listener);

        // ネストスタートブロックにも設定
        mNestStartBlockThird.setDropBlockListerner(listener);
    }

    /*
     * マークエリアリスナー設定
     */
    @Override
    public void setMarkAreaListerner(MarkerAreaListener listener){
        super.setMarkAreaListerner(listener);

        // ネストスタートブロックにも設定
        mNestStartBlockThird.setMarkAreaListerner(listener);
    }
}


