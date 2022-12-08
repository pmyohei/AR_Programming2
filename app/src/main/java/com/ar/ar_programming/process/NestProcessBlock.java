package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
import android.view.DragEvent;
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

    //---------------------------
    // フィールド変数
    //---------------------------

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
        super.setLayout( layoutID );

        // 処理ブロックタッチリスナー
        setBlockTouchListerer();
        // 処理ブロック内の内容を書き換え
        rewriteProcessContents(mProcessContents);
    }

    /*
     * ネスト内スタートブロック初期設定
     */
    public void initStartBlockInNest( int layoutID ) {

        // レイアウト設定
        StartBlock startBlock = findViewById( R.id.pb_start );
        startBlock.setLayout( layoutID );
        // マーカー無効化
        startBlock.setMarker( false );
        // スタートブロックにネスト情報を設定
        startBlock.setOwnNestBlock( this );
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
     * ネスト内マークエリアリスナーの設定
     */
    public void setMarkAreaInNestListerner(MarkerAreaListener listener) {

        StartBlock startBlock = findViewById( R.id.pb_start );
        ViewGroup cl_bottomMarkArea = startBlock.findViewById(R.id.cl_bottomMarkArea);
        cl_bottomMarkArea.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // リスナーコール
                listener.onBottomMarkerAreaClick(startBlock);
            }
        });
    }

    /*
     * ネスト内ドロップリスナーの設定
     */
    public void setDropInNestListerner(DropBlockListener listener) {

        StartBlock startBlock = findViewById( R.id.pb_start );
        startBlock.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                return listener.onDropBlock( view, dragEvent );
            }
        });
    }

}

