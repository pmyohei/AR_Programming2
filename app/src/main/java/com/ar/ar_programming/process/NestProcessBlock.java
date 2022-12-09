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
        super.setLayout(layoutID);

        // 処理ブロックタッチリスナー
        setBlockTouchListerer();
        // 処理ブロック内の内容を書き換え
        rewriteProcessContents(mProcessContents);
    }

    /*
     * ネスト内に指定されたブロックがあるか
     */
    public boolean hasBlock(Block block ) {
        // 同じIDがあれば保持しているとみなす
        return (findViewById( block.getId() ) != null);
    }

    /*
     * ネスト内スタートブロックを取得
     */
    private StartBlock getStartBlockInNest() {
        // ネスト内の先頭のビューがStartBlock
        ViewGroup parent = findViewById( R.id.ll_firstNestRoot );
        return  (StartBlock)parent.getChildAt(0);
    }

    /*
     * ネスト内スタートブロック初期設定
     */
    public void initStartBlockInNest( int layoutID ) {

        StartBlock startBlock = getStartBlockInNest();

        // IDを動的に設定（他のネストブロックと重複しないようにするため）
        startBlock.setId(View.generateViewId());
        // レイアウト設定
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

        StartBlock startBlock = getStartBlockInNest();
        int markAreaID = startBlock.getMarkAreaViewID();
        ViewGroup markArea = startBlock.findViewById( markAreaID );

        markArea.setOnClickListener(new OnClickListener() {
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

        // ネスト内のスタートブロックにリスナーを設定
        StartBlock startBlock = getStartBlockInNest();
        startBlock.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                return listener.onDropBlock( (Block)view, dragEvent );
            }
        });
    }

}

