package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.ar.ar_programming.ARFragment;
import com.ar.ar_programming.UserBlockSelectListAdapter;
import com.ar.ar_programming.character.CharacterNode;
import com.ar.ar_programming.Gimmick;
import com.ar.ar_programming.R;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;


/*
 * ネストあり処理ブロック基底クラス
 */
public abstract class NestBlock extends ProcessBlock {

    //---------------------------
    // 定数
    //---------------------------
    public static final int NEST_FIRST = 0;
    public static final int NEST_SECOND = 1;
    public static final int NEST_THIRD = 2;

    //---------------------------
    // フィールド変数
    //---------------------------
    public StartBlock mNestStartBlockFirst;

    /*
     * コンストラクタ
     */
    public NestBlock(Context context) {
        this(context, null);
    }

    public NestBlock(Context context, AttributeSet attrs) {
        this(context, attrs, 0, null);
    }

    public NestBlock(Context context, AttributeSet attrs, int defStyle, Gimmick.XmlBlockInfo xmlBlockInfo) {
        super(context, attrs, defStyle, xmlBlockInfo);
        createStartBlock();
    }

    /*
     * ネスト内条件判定
     *   @return：条件成立　 - true
     *          　条件不成立 - false
     *          　とすること！
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
        rewriteProcessContents();
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
        Block block = mNestStartBlockFirst;
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
        Block block = mNestStartBlockFirst;
        while (block != null) {
            block.tranceOffDrag();
            block = block.getBelowBlock();
        }
    }

    /*
     * ブロック削除
     */
    @Override
    public void removeOnChart(Gimmick gimmick, UserBlockSelectListAdapter adapter) {
        super.removeOnChart( gimmick, adapter );

        //---------------------
        // ネスト内ブロックを削除
        //---------------------
        Block block = getStartBlockForNest(NEST_FIRST);
        while (block != null) {
            block.removeOnChart( gimmick, adapter );
            block = block.getBelowBlock();
        }
    }

    /*
     * ネストスタートブロック生成
     */
    public void createStartBlock() {
        mNestStartBlockFirst = new StartBlock(getContext());
        mNestStartBlockFirst.setId(View.generateViewId());
        mNestStartBlockFirst.setLayout(R.layout.block_start_in_nest);
        mNestStartBlockFirst.setOwnNestBlock(this);

        // 位置配置のタイミングで可視化
        mNestStartBlockFirst.setVisibility(View.INVISIBLE);
    }

    /*
     * ネストスタートブロック配置
     */
    public void deployStartBlock(ViewGroup chartRoot) {
        // チャートに追加
        ViewGroup.MarginLayoutParams mlp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        chartRoot.addView(mNestStartBlockFirst, mlp);
    }

    /*
     * ブロック位置更新
     */
    @Override
    public void updatePosition() {
        super.updatePosition();

        // 自身の位置が確定したら、ネスト内スタートブロックを更新
        post(() -> {
            mNestStartBlockFirst.updatePosition();
        });
    }

    /*
     * ネスト内スタートブロックを配置するためのTopマージンを取得
     */
    public int getStartBlockTopMargin(int nest) {
        ViewGroup nestView = getNestViewForNest(nest);
        return getTop() + nestView.getTop();
    }

    /*
     * ネスト内スタートブロックを配置するためのLeftマージンを取得
     */
    public int getStartBlockLeftMargin(int nest) {
        ViewGroup nestView = getNestViewForNest(nest);
        return getLeft() + nestView.getLeft();
    }

    /*
     * ネストサイズの変更
     *   @para1：本メソッドをコールしたブロック
     * 　　　　　　（リサイズするネストを識別するために必要）
     */
    public void resizeNestHeight(Block calledBlock) {

        // 対象ネストview
        int targetNest = whichInNest(calledBlock);
        ViewGroup nestView = getNestViewForNest(targetNest);

        //----------------------
        // ネストサイズを更新
        //----------------------
        int height = calcNestHeight(targetNest);
        updateNestViewHeight(nestView, height);

        //----------------------
        // 親ネストのリサイズ処理
        //----------------------
        nestView.post(() -> {
            // 親ネストのリサイズ処理
            if (inNest()) {
                getOwnNestBlock().resizeNestHeight(this);
            }

            // 下ブロック位置を更新
            if (hasBelowBlock()) {
                getBelowBlock().updatePosition();
            }
        });
    }

    /*
     * 新ネストサイズの計算
     */
    private int calcNestHeight(int nest) {

        // 対象ネスト
        Block block = getStartBlockForNest(nest);

        //-------------------
        // ネストサイズ計算
        //-------------------
        // ネスト内ブロックの高さを加算していく
        int height = 0;
        while (block != null) {
            height += block.getHeight();
            block = block.getBelowBlock();
        }

        return height;
    }

    /*
     * 新ネストサイズの計算
     */
    private void updateNestViewHeight(ViewGroup nestView, int height) {
        // 現在のサイズと異なっていれば更新
        ViewGroup.LayoutParams lp = nestView.getLayoutParams();
        if (lp.height != height) {
            lp.height = height;
            nestView.setLayoutParams(lp);
        }
    }


    /*
     * 指定ブロックがどのネストにいるか
     * ！本クラスでは、ネスト１を固定で返す
     * ！ネストを複数持つクラス実装時は、実装に合わせてOverrideが必要
     */
    public int whichInNest(Block calledBlock) {
        return NEST_FIRST;
    }

    /*
     * 指定ネストのスタートブロックを取得
     * ！本クラスでは、ネスト１を固定で返す
     * ！ネストを複数持つクラス実装時は、実装に合わせてOverrideが必要
     */
    public Block getStartBlockForNest(int nest) {
        return mNestStartBlockFirst;
    }

    /*
     * 指定ネストのネストviewを取得
     * ！本クラスでは、ネスト１を固定で返す
     * ！ネストを複数持つクラス実装時は、実装に合わせてOverrideが必要
     */
    public ViewGroup getNestViewForNest(int nest) {
        return findViewById(R.id.ll_firstNestRoot);
    }

    /*
     * ネスト内に指定されたブロックがあるか
     * ！本クラスでは、ネスト１内を検索
     * ！ネストを複数持つクラス実装時は、実装に合わせてOverrideが必要
     */
    @Override
    public boolean hasBlock(Block checkBlock) {
        // ネスト１内の検索
        return searchBlockInNest(NEST_FIRST, checkBlock);
    }

    /*
     * 指定ネスト内のブロック検索
     */
    public boolean searchBlockInNest(int nest, Block checkBlock) {

        //---------------------
        // ネスト内検索
        //---------------------
        Block block = getStartBlockForNest(nest);
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
        return mNestStartBlockFirst.hasBelowBlock();
    }

    /*
     * 処理ブロックドロップリスナーの設定
     */
    @Override
    public void setDropBlockListerner(DropBlockListener listener) {
        super.setDropBlockListerner(listener);

        // ネストスタートブロックにも設定
        mNestStartBlockFirst.setDropBlockListerner(listener);
    }

    /*
     * マークエリアリスナー設定
     */
    @Override
    public void setMarkAreaListerner(MarkerAreaListener listener) {
        super.setMarkAreaListerner(listener);

        // ネストスタートブロックにも設定
        mNestStartBlockFirst.setMarkAreaListerner(listener);
    }

    /*
     * 条件判定：対象Nodeの方をキャラクターが向いているか判定
     */
    public boolean isConditionFacing(CharacterNode characterNode, String targetNodeName) {

        //------------------
        // 判定対象Nodeを取得
        //------------------
        // AR上のNodeは、全てanchorNodeを親としているため、characterNodeの親Node（=anchorNode）を検索用に渡す
        AnchorNode anchorNode = (AnchorNode) characterNode.getParentNode();
        Node targetNode = ARFragment.searchNodeCharacterFacingOnStage(anchorNode, targetNodeName, characterNode);
        if (targetNode == null) {
            // そもそも対象Nodeがなければ、条件不成立（向いていない）とみなす
            return false;
        }

        // 見つかれば、true
        Log.i("isConditionFacing", "向いているNode発見=" + targetNode.getName());
        return true;
    }

    /*
     * 条件判定：対象Nodeを除外したことがあるかどうか
     */
    public boolean isConditionRemoved(CharacterNode characterNode) {

        // ステージから除外したことがあるか
        boolean isEverRemovedNode = characterNode.isEverRemovedNodeContain( mXmlBlockInfo.targetNode_1 );
        if( isEverRemovedNode ){
            // あるなら、検索対象外リストはクリアする
            characterNode.clearNotSearchNodeList();
        }

        return isEverRemovedNode;
    }
}

