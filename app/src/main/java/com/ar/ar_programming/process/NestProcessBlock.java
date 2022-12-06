package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
     * ネスト内処理ブロック数の取得
     */
    public abstract int getBlockSizeInNest();

    /*
     * ネスト内スタートブロック初期設定
     */
    public abstract void initStartBlockInNest( int layoutID );

    /*
     * マークエリアリスナーの設定
     */
    public abstract void setMarkAreaInNestListerner(BottomMarkerAreaListener listener);

}

