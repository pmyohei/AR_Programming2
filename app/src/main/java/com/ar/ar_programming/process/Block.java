package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
    public void setMarker(boolean enable){
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
     * マーカ―有無
     */
    public boolean isMarked(){
        // マーカー表示中なら、マーク中と判断
        ImageView iv_bottomMark = findViewById(R.id.iv_bottomMark);
        return (iv_bottomMark.getVisibility() == VISIBLE);
    }

    /*
     * マークエリアリスナー設定
     */
    public void setMarkAreaListerner(MarkerAreaListener listener){

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
     * 処理ブロックドロップリスナーの設定
     */
    public void setDropBlockListerner(DropBlockListener listener) {

        setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                return listener.onDropBlock( view, dragEvent );
            }
        });
    }

    public interface MarkerAreaListener {
        // マーカー処理ブロック下部への移動アイコンクリックリスナー
        void onBottomMarkerAreaClick(Block markedBlock);
    }
    public interface DropBlockListener {
        // 処理ブロックドロップリスナー
        boolean onDropBlock(View view, DragEvent dragEvent);
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
