package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ar.ar_programming.R;


/*
 * 処理ブロック
 * 　　「SingleProcessView」「NestProcessView」等の基本クラス
 */
public abstract class ProcessBlock extends Block {

    //---------------------------
    // 定数
    //---------------------------
    // 処理ブロック種別
    public static final int PROCESS_TYPE_SINGLE = 0;
    public static final int PROCESS_TYPE_IF = 1;
    public static final int PROCESS_TYPE_IF_ELSE = 2;
    public static final int PROCESS_TYPE_LOOP = 3;

    // ドラッグ中（選択中）状態の半透明値
    public static final float TRANCE_DRAG = 0.6f;
    public static final float TRANCE_NOT_DRAG = 1.0f;


    //---------------------------
    // フィールド変数
    //---------------------------
    public int mProcessType;
    public int mProcessContents;
    private boolean mDragFlg;


    public ProcessBlock(Context context) {
        this(context, null);
    }
    public ProcessBlock(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0, 0);
    }
    public ProcessBlock(Context context, AttributeSet attrs, int defStyle, int type, int contents) {
        super(context, attrs, defStyle);
        mProcessType = type;
        mProcessContents = contents;
        setId(View.generateViewId());
    }

    /*
     * 処理ブロックの内容を書き換え
     */
    public abstract void rewriteProcessContents(int contents);

    /*
     * 処理ブロックタイプ設定
     */
    public int getProcessType() {
        return mProcessType;
    }
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
    public View getLayoutRootView(){
        return findViewById( R.id.ll_root );
    }

    /*
     * マークエリアビューIDを取得
     */
    @Override
    public int getMarkAreaViewID(){
        return R.id.cl_markArea;
    }

    /*
     * マークエリアのマークイメージIDを取得
     */
    @Override
    public int getMarkImageViewID(){
        return R.id.iv_mark;
    }

    /*
     * ドロップラインビューIDを取得
     */
    @Override
    public int getDropLineViewID(){
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
        int childNum = ((ViewGroup)getParent()).getChildCount();

        // 最後尾にいるなら、真を返す
        return (childIndex == (childNum - 1));
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

                switch ( motionEvent.getAction() ){
                    case MotionEvent.ACTION_DOWN:
                        // フラグon
                        mDragFlg = true;
                        break;

                    case MotionEvent.ACTION_MOVE:

                        // フラグon の場合
                        if( mDragFlg ){
                            //--------------------------
                            // 本処理ブロックを半透明化
                            //--------------------------
                            selfBlock.setAlpha(TRANCE_DRAG);

                            //--------------------------
                            // ドラッグ開始
                            //--------------------------
                            // ドラッグ中のビューとして本ブロックを設定
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
     * 本ブロック位置を１つ上げるリスナー設定
     */
    public void setBlockControlListener( BlockControlListener listerner ){
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

    }

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
    public interface BlockControlListener {
        // 本ブロック位置変更（１つ上に移動）
        void onUpBlock( ProcessBlock markedBlock );
        // 本ブロック位置変更（１つ下に移動）
        void onDownBlock( ProcessBlock markedBlock );
        // 本ブロック削除
        void onRemoveBlock( ProcessBlock markedBlock );
        // 本ブロック位置を「マークブロック」下に移動
        void onMoveBelowMarker( ProcessBlock markedBlock );
    }

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
