package com.ar.ar_programming.process;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ar.ar_programming.R;


/*
 * 処理ブロック基本クラス
 */
public abstract class Block extends ConstraintLayout {

    //---------------------------
    // 定数
    //---------------------------
    // 本ブロックが組み込まれているネストブロック（ない場合はnull）
    public NestProcessBlock mOwnNestBlock;

    //---------------------------
    // フィールド変数
    //---------------------------


    public Block(Context context) {
        this(context, null);
    }
    public Block(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public Block(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /*
     * マーカ―設定
     */
    public abstract void setMarker(boolean enable);

    /*
     * マーカ―有無
     */
    public abstract boolean isMarked();

    /*
     * マークエリアリスナー設定
     */
    public abstract void setMarkAreaListerner(BottomMarkerAreaListener listener);

    /*
     * マーカーエリアクリックインターフェース
     */
    public interface BottomMarkerAreaListener {
        // マーカー処理ブロック下部への移動アイコンクリックリスナー
        void onBottomMarkerAreaClick(Block markedBlock);
    }

    /*
     * レイアウト設定
     */
    public void setLayout(int layoutID){
        View.inflate(getContext(), layoutID, this);
    }

    /*
     * 親レイアウトから見た時の本ビューのchildIndexを取得
     */
    public int getOwnChildIndex() {

        // 自分のレイアウトID
        int myID = getId();

        // 子レイアウトの数
        ViewGroup parentView = (ViewGroup) getParent();
        int childNum = parentView.getChildCount();

        //----------------
        // 本ブロック検索
        //----------------
        for (int i = 0; i < childNum; i++) {
            int checkID = parentView.getChildAt(i).getId();
            if (myID == checkID) {
                // 自分と同じIDがあれば、その時のindexを返す
                return i;
            }
        }

        return -1;
    }

    /*
     * 「本ブロックが組み込まれているネストブロック」設定
     */
    public void setOwnNestBlock( NestProcessBlock nestBlock ) {
        mOwnNestBlock = nestBlock;
    }
    /*
     * 「本ブロックが組み込まれているネストブロック」取得
     */
    public NestProcessBlock getOwnNestBlock() {
        return mOwnNestBlock;
    }
}
