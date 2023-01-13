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
}
