package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.ar.ar_programming.CharacterNode;
import com.ar.ar_programming.R;


/*
 * 処理ブロック
 * 　　「SingleProcessView」「NestProcessView」等の基本クラス
 */
public abstract class ProcessBlock extends Block {

    //---------------------------
    // 定数
    //---------------------------


    //---------------------------
    // フィールド変数
    //---------------------------
    public int mProcessContents;
    private boolean mDragFlg;
    private ProcessListener mProcessListener;


    public ProcessBlock(Context context) {
        this(context, null);
    }

    public ProcessBlock(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0, 0);
    }

    public ProcessBlock(Context context, AttributeSet attrs, int defStyle, int type, int contents) {
        super(context, attrs, defStyle, type);
        mProcessContents = contents;
        setId(View.generateViewId());
    }

    /*
     * 処理ブロックの内容を書き換え
     */
    public abstract void rewriteProcessContents(int contents);

    /*
     * 処理ブロック内容取得
     */
    public int getProcessContents() {
        return mProcessContents;
    }

    /*
     * レイアウト最上位ビューIDを取得
     */
    @Override
    public View getLayoutRootView() {
        return findViewById(R.id.ll_root);
    }

    /*
     * マークエリアビューIDを取得
     */
    @Override
    public int getMarkAreaViewID() {
        return R.id.cl_markArea;
    }

    /*
     * マークエリアのマークイメージIDを取得
     */
    @Override
    public int getMarkImageViewID() {
        return R.id.iv_mark;
    }

    /*
     * ドロップラインビューIDを取得
     */
    @Override
    public int getDropLineViewID() {
        Log.i("ドロップリスナー", "getDropLineViewID Process側取得");
        return R.id.v_dropLine;
    }

    /*
     * 処理ラインの先頭判定
     *   ※「スタートブロック」の次に位置するブロックを「先頭」としている
     */
    public boolean isTop() {
        int childIndex = getOwnChildIndex();
        return (childIndex == 1);
    }

    /*
     * 自身が子ビューとして最後尾にいるかどうか
     */
    public boolean isBottom() {

        // 自身のChildIndexとブロック数
        int childIndex = getOwnChildIndex();
        int childNum = ((ViewGroup) getParent()).getChildCount();

        // 最後尾にいるなら、真を返す
        return (childIndex == (childNum - 1));
    }


    /*
     * 現在位置から指定ブロックの下に移動する
     */
    public void moveToUnderBlock(Block aboveBlock ) {

        int height = getHeight();

        boolean moveDown = existsBelow( aboveBlock );

        Log.i("チャート確定問題", "moveDown=" + moveDown);

        Block moveStartBlock;
        Block moveEndBlock;
        if( moveDown ){
            moveStartBlock = getBelowBlock();
            moveEndBlock = aboveBlock;
        } else {
            moveStartBlock = aboveBlock.getBelowBlock();
            moveEndBlock = getAboveBlock();
        }
        Block tmpbelow = aboveBlock.getBelowBlock();
        moveEndBlock.setBelowBlock( null );

        if( moveDown ){
            moveStartBlock.upChartPosition(height);
        } else {
            moveStartBlock.downChartPosition(height);
        }

        Log.i("チャート確定問題", "aboveBlock.getTop()=" + aboveBlock.getTop());

        // 自分の位置を変更
        setInsertMLB( aboveBlock, moveDown );



//        Block aboveBelowBlock = aboveBlock.getBelowBlock();
        Block aboveBelowBlock = tmpbelow;
        Block selfAbove = getAboveBlock();
        Block selfBelow = getBelowBlock();

        selfAbove.setBelowBlock( selfBelow );
        if( selfBelow != null ){
            selfBelow.setAboveBlock( selfAbove );
        }

        aboveBlock.setBelowBlock( this );
        if( aboveBelowBlock != null ){
            aboveBelowBlock.setAboveBlock( this );
        }

        setAboveBlock( aboveBlock );
        setBelowBlock( aboveBelowBlock );

//        Log.i("チャート確定問題", "" + aboveBlock.getId() + "の上は" + aboveBlock.getAboveBlock().getId());
//        Log.i("チャート確定問題", "" + aboveBlock.getId() + "の下は" + aboveBlock.getBelowBlock().getId());

        if( getAboveBlock() != null ){
            Log.i("チャート確定問題", "" + getId() + "の上は" + getAboveBlock().getId());
        } else {
            Log.i("チャート確定問題", "" + getId() + "の上はなし");
        }
        //おかしい
        if( getBelowBlock() != null ){
            Log.i("チャート確定問題", "" + getId() + "の下は" + getBelowBlock().getId());
        } else {
            Log.i("チャート確定問題", "" + getId() + "の下はなし");
        }
        if( aboveBelowBlock != null ){
            if( aboveBelowBlock.getBelowBlock() != null ){
                Log.i("チャート確定問題", "" + aboveBelowBlock.getId() + "の下は" + aboveBelowBlock.getBelowBlock().getId());
            } else {
                Log.i("チャート確定問題", "" + aboveBelowBlock.getId() + "の下はなし");
            }
        }
        if( selfBelow != null ){
            if( selfBelow.getBelowBlock() != null ){
                Log.i("チャート確定問題", "" + selfBelow.getId() + "の下は" + selfBelow.getBelowBlock().getId());
            } else {
                Log.i("チャート確定問題", "" + selfBelow.getId() + "の下はなし");
            }
        }

/*
        Log.i("チャート確定問題", "aboveBlock=" + aboveBlock.getId());
        Log.i("チャート確定問題", "selfAbove=" + selfAbove.getId());

        if( selfBelow != null ){
            Log.i("チャート確定問題", "selfBelow=" + selfBelow.getId());
        }
        if( tmpbelow != null ){
            Log.i("チャート確定問題", "tmpbelow=" + tmpbelow.getId());
        }
        Log.i("チャート確定問題", "============");
*/


/*        if( moveDown ){
            rewriteAboveBelowBlockOnRemove();
        } else {
            rewriteAboveBelowBlockOnInsert( aboveBlock );
        }*/

/*        if( moveDown ){
            aboveBlock.post(() -> {
                Log.i("チャート確定問題", "aboveBlock.getTop()=" + aboveBlock.getTop());
                // 自分の位置を変更
                setInsertMLB( aboveBlock );
            });

        } else {
            aboveBlock.post(() -> {
                Log.i("チャート確定問題", "aboveBlock.getTop()=" + aboveBlock.getTop());
                // 自分の位置を変更
                setInsertMLB( aboveBlock );
            });
        }*/
    }

    /*
     * 現在位置から指定ブロックの下に移動する
     */
    public void moveToUnderBlock_old(Block aboveBlock ) {

        int height = getHeight();

        // 自分の位置を変更
        setInsertMLB_old( aboveBlock );

        // 本ブロックの下にあるブロックを上げる
        if( hasBelowBlock() ){
            Block belowBlock = getBelowBlock();
            belowBlock.upChartPosition( height );
        }

        // 移動前に自分がネスト内にいた場合
        if( inNest() ){
            // ネストブロック下のブロックを上に移動させる
            NestProcessBlock preNestBlock = getOwnNestBlock();
            preNestBlock.upNestBelowBlock( height );

            // 削除ブロック分、ネストを縮める
            preNestBlock.resizeNestHeight(this, NestProcessBlock.NEST_SHRINK);
        }

        rewriteAboveBelowBlockOnRemove();

        // 挿入先の上ブロックのレイアウト確定待ち
        aboveBlock.post(() -> {

            rewriteAboveBelowBlockOnInsert( aboveBlock );

            // 挿入先ブロックの下ブロックを下げる
            if( aboveBlock.hasBelowBlock() ){
                Block insertBelowBlock = aboveBlock.getBelowBlock();
                insertBelowBlock.downChartPosition( height );
            }

            // 移動先がネスト内の場合
            if( aboveBlock.inNest() ){
                NestProcessBlock newNestBlock = aboveBlock.getOwnNestBlock();
                newNestBlock.downNestBelowBlock(height);

                // ネストブロックサイズを変更
                newNestBlock.resizeNestHeight(this, NestProcessBlock.NEST_EXPAND);
            }

            // 親ネスト情報の書き換え
            setOwnNestBlock( aboveBlock.getOwnNestBlock() );


        });

    }

    /*
     * 指定ブロック下に挿入する際のmlbを取得
     */
    public void setInsertMLB( Block block, boolean moveDown ) {

        int height = 0;
        if( moveDown ){
            height = getHeight();
        }

        // マージン設定
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) getLayoutParams();

        int top = block.getTop() + block.getHeight() - height;
        int left = mlp.leftMargin;
        if (block.inNest()) {
            left = block.getLeft();
        }

        mlp.setMargins(left, top, mlp.rightMargin, mlp.bottomMargin);
        setLayoutParams( mlp );
    }

    /*
     * 指定ブロック下に挿入する際のmlbを取得
     */
    public void setInsertMLB_old( Block block ) {

        // マージン設定
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) getLayoutParams();

        int top = block.getTop() + block.getHeight() - getHeight();
        int left = mlp.leftMargin;
        if (block.inNest()) {
            left = block.getLeft();
        }

        mlp.setMargins(left, top, mlp.rightMargin, mlp.bottomMargin);
        setLayoutParams( mlp );
    }

    /*
     * 上下ブロック保持情報の更新（ブロック削除時）
     */
    private void rewriteAboveBelowBlockOnRemove() {

        // 削除ブロックの上下ブロック
        Block aboveBlock = getAboveBlock();
        Block belowBlock = getBelowBlock();

        // 上下ブロックの保持情報を更新
        aboveBlock.setBelowBlock(belowBlock);
        if (belowBlock != null) {
            belowBlock.setAboveBlock(aboveBlock);
        }
    }

    /*
     * 上下ブロック保持情報の更新（ブロック挿入時）
     */
    private void rewriteAboveBelowBlockOnInsert(Block aboveBlock) {

        // 挿入前の「挿入ブロックの上ブロック」の下ブロック
        Block belowBlock = aboveBlock.getBelowBlock();

        // 挿入ブロックの保持情報を更新
        setAboveBlock(aboveBlock);
        setBelowBlock(belowBlock);

        // 「新規ブロックの上のブロック」の下ブロックを「新規ブロック」にする
        aboveBlock.setBelowBlock(this);

        // 「新規ブロックの１つ下ブロック（あれば）」の上ブロックを「新規ブロック」にする
        if (belowBlock != null) {
            belowBlock.setAboveBlock(this);
        }
    }


    /*
     * タッチリスナー設定
     *   本ブロックがタッチされたとき、ドラッグ移動可能にする
     */
    public void setBlockTouchListerer() {

        // 本ブロック
        Block selfBlock = this;

        getLayoutRootView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // フラグon
                        mDragFlg = true;
                        break;

                    case MotionEvent.ACTION_MOVE:

                        // フラグon の場合
                        if (mDragFlg) {
                            //--------------------------
                            // 本処理ブロックを半透明化
                            //--------------------------
//                            tranceBlock(selfBlock);
                            tranceOnDrag();

                            //--------------------------
                            // ドラッグ開始
                            //--------------------------
                            // 本ブロックをドラッグ中のビューとする
                            View.DragShadowBuilder myShadow = new View.DragShadowBuilder(view);
                            view.startDragAndDrop(null, myShadow, selfBlock, 0);

                            // フラグoff
                            mDragFlg = false;
                        }

                        break;

                    case MotionEvent.ACTION_UP:
                        // do nothing
                        break;
                }
                return true;
            }
        });
    }

    /*
     * 処理開始
     */
    public abstract void startProcess(CharacterNode characterNode);

    /*
     * 次の処理ブロック遷移処理
     */
    public void tranceNextBlock(CharacterNode characterNode) {

        //------------------
        // 下ブロックチェック
        //------------------
        if (hasBelowBlock()) {
            // 下ブロックがあれば、そのブロックの処理を開始
            ProcessBlock nextBlock = (ProcessBlock) getBelowBlock();
            nextBlock.startProcess(characterNode);

            return;
        }

        //--------------------------
        // 下ブロックなし。親ネスト判定
        //--------------------------
        // 本ブロックがネスト内にあり、最後の処理であった場合
        if (inNest()) {
            NestProcessBlock parentNest = getOwnNestBlock();

            if (parentNest.getProcessType() == PROCESS_TYPE_LOOP) {
                // ループの場合は、開始処理から
                parentNest.startProcess(characterNode);
            } else {
                parentNest.tranceNextBlock(characterNode);
            }

            return;
        }

        //--------------------------
        // 下ブロックなし／親ネストなし
        //--------------------------
        // 終了リスナーをコール
        mProcessListener.onProcessEnd();
    }

    /*
     * ブロック処理リスナーの設定
     */
    public void setProcessListener(ProcessListener listener ) {
        mProcessListener = listener;
    }

    /*
     * ブロック処理リスナー
     */
    public interface ProcessListener {
        void onProcessEnd();
    }

    /*
     * 本ブロック位置を１つ上げるリスナー設定
     */
/*    public void setBlockControlListener( BlockControlListener listerner ){
        // 本ブロック
        ProcessBlock myself = this;

        // アイコン
        ImageView iv_up = findViewById(R.id.iv_up);
        ImageView iv_down = findViewById(R.id.iv_down);
        ImageView iv_remove = findViewById(R.id.iv_remove);
        ImageView iv_moveBelowMark = findViewById(R.id.iv_moveBelowMark);

        // 本ブロックを上に移動
        iv_up.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                listerner.onUpBlock( myself );
            }
        });

        // 本ブロックを下に移動
        iv_down.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                listerner.onDownBlock( myself );
            }
        });

        // 本ブロックを削除
        iv_remove.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                listerner.onRemoveBlock( myself );
            }
        });

        // 本ブロックをマークブロックの下に移動
        iv_moveBelowMark.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // リスナーコール
                listerner.onMoveBelowMarker(myself);
            }
        });
    }*/

/*    *//*
     * 本ブロック位置を１つ上げるリスナー設定
     *//*
    public void setOnUpBlockListener( UpBlockListener listerner ){

    }

    *//*
     * 本ブロック位置を１つ下げるリスナー設定
     *//*
    public void setOnDownBlockListener( DownBlockListener listerner ){

    }

    *//*
     * 本ブロック削除リスナー設定
     *//*
    public void setOnRemoveBlockListener( RemoveBlockListener listerner ){

        ProcessBlock myself = this;

        // 本処理ブロックを削除
        ImageView iv_remove = findViewById(R.id.iv_remove);
        iv_remove.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                listerner.onRemoveBlock( myself );
            }
        });
    }

    *//*
     * 「マーカーブロック」の下への移動リスナー設定
     *//*
    public void setOnMoveBelowMarkerListener( MoveBelowMarkerListener listerner ){

        ProcessBlock myself = this;

        // 本処理ブロックをマークありの処理ブロックの下に移動
        ImageView iv_moveBelowMark = findViewById(R.id.iv_moveBelowMark);
        iv_moveBelowMark.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // リスナーコール
                listerner.onMoveBelowMarker(myself);
            }
        });
    }*/

    /*
     * 本ブロック操作インターフェース
     */
/*    public interface BlockControlListener {
        // 本ブロック位置変更（１つ上に移動）
        void onUpBlock( ProcessBlock markedBlock );
        // 本ブロック位置変更（１つ下に移動）
        void onDownBlock( ProcessBlock markedBlock );
        // 本ブロック削除
        void onRemoveBlock( ProcessBlock markedBlock );
        // 本ブロック位置を「マークブロック」下に移動
        void onMoveBelowMarker( ProcessBlock markedBlock );
    }*/

/*    *//*
     * 本ブロック位置を１つ上げるインターフェース
     *//*
    public interface UpBlockListener {
        void onUpBlock( ProcessBlock markedBlock );
    }

    *//*
     * 本ブロック位置を１つ下げるインターフェース
     *//*
    public interface DownBlockListener {
        void onDownBlock( ProcessBlock markedBlock );
    }

    *//*
     * ブロック削除インターフェース
     *//*
    public interface RemoveBlockListener {
        void onRemoveBlock( ProcessBlock markedBlock );
    }

    *//*
     * 「マーカーブロック」の下への移動リスナー設定
     *//*
    public interface MoveBelowMarkerListener {
        void onMoveBelowMarker( ProcessBlock markedBlock );
    }*/


}
