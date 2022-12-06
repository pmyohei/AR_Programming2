package com.ar.ar_programming.process;

import static com.ar.ar_programming.process.SingleProcessBlock.PROCESS_CONTENTS_BACK;
import static com.ar.ar_programming.process.SingleProcessBlock.PROCESS_CONTENTS_FORWARD;
import static com.ar.ar_programming.process.SingleProcessBlock.PROCESS_CONTENTS_LEFT_ROTATE;
import static com.ar.ar_programming.process.SingleProcessBlock.PROCESS_CONTENTS_RIGHT_ROTATE;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

    //---------------------------
    // フィールド変数
    //---------------------------
    public int mProcessType;
    public int mProcessContents;


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
     * 処理ブロックの内容を書き換え
     */
    public abstract void rewriteProcessContents(int contents);

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
     * マーカ―設定
     */
    @Override
    public void setMarker(boolean enable) {

        // 表示or非表示
        int visible;
        if (enable) {
            visible = VISIBLE;
        } else {
            visible = GONE;
        }

        // マークアイコン表示設定
        ImageView iv_bottomMark = findViewById(R.id.iv_bottomMark);
        iv_bottomMark.setVisibility(visible);
    }

    /*
     * ブロック下部追加マーカーの有無
     */
    @Override
    public boolean isMarked() {
        // マーカー表示中なら、マーク中と判断
        ImageView iv_bottomMark = findViewById(R.id.iv_bottomMark);
        return (iv_bottomMark.getVisibility() == VISIBLE);
    }

    /*
     * マークエリアリスナーの設定
     */
    @Override
    public void setMarkAreaListerner(BottomMarkerAreaListener listener) {

        Block myself = this;

        ViewGroup cl_bottomMarkArea = findViewById(R.id.cl_bottomMarkArea);
        cl_bottomMarkArea.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // リスナーコール
                listener.onBottomMarkerAreaClick(myself);
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
