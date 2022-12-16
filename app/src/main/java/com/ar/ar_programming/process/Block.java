package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
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
    // ブロック種別
    public static final int PROCESS_TYPE_START = 0;
    public static final int PROCESS_TYPE_SINGLE = 1;
    public static final int PROCESS_TYPE_IF = 2;
    public static final int PROCESS_TYPE_IF_ELSE = 3;
    public static final int PROCESS_TYPE_LOOP = 4;

    // ドラッグ中（選択中）状態の半透明値
    public static final float TRANCE_DRAG = 0.6f;
    public static final float TRANCE_NOT_DRAG = 1.0f;


    //---------------------------
    // フィールド変数
    //---------------------------
    public int mProcessType;
    // 本ブロックが組み込まれているネストブロック（ない場合はnull）
    public NestProcessBlock mOwnNestBlock;
    private Block mAboveBlock;
    private Block mBelowBlock;


    public Block(Context context) {
        this(context, null);
    }

    public Block(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public Block(Context context, AttributeSet attrs, int defStyle, int type) {
        super(context, attrs, defStyle);
        mProcessType = type;
        mAboveBlock = null;
        mBelowBlock = null;
    }

    /*
     * レイアウト最上位ビューを取得
     */
    public abstract View getLayoutRootView();

    /*
     * マークエリアビューIDを取得
     */
    public abstract int getMarkAreaViewID();

    /*
     * マークエリアのマークイメージIDを取得
     */
    public abstract int getMarkImageViewID();

    /*
     * ブロック追加ラインビューIDを取得
     */
    public abstract int getDropLineViewID();

    /*
     * 処理ブロックタイプ設定
     */
    public int getProcessType() {
        return mProcessType;
    }

    /*
     * 自身の一つ上にあるブロックを設定
     */
    public void setAboveBlock(Block block) {
        mAboveBlock = block;
    }

    /*
     * 自身の一つ下にあるブロックを設定
     */
    public void setBelowBlock(Block block) {
        mBelowBlock = block;
    }

    /*
     * 自身の一つ上にあるブロックを取得
     */
    public Block getAboveBlock() {
        return mAboveBlock;
    }

    /*
     * 自身の一つ下にあるブロックを取得
     */
    public Block getBelowBlock() {
        return mBelowBlock;
    }

    /*
     * ブロック半透明化
     */
    public void tranceDrag() {
        setAlpha(TRANCE_DRAG);
    }

    /*
     * ブロック位置移動：上
     */
    public void upChartPosition(int trancelate) {

        // マージンを再設定し、位置を下げる
        ViewGroup.MarginLayoutParams belowMlp = (ViewGroup.MarginLayoutParams) getLayoutParams();
        belowMlp.setMargins(belowMlp.leftMargin, belowMlp.topMargin - trancelate, belowMlp.rightMargin, belowMlp.bottomMargin);

        // 本ブロックの上ブロックも上げる
        Block aboveBlock = getBelowBlock();
        if (aboveBlock != null) {
            aboveBlock.upChartPosition(trancelate);
        }
    }

    /*
     * ブロック位置移動：下
     */
    public void downChartPosition(int trancelate) {

        // マージンを再設定し、位置を下げる
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) getLayoutParams();
        mlp.setMargins(mlp.leftMargin, mlp.topMargin + trancelate, mlp.rightMargin, mlp.bottomMargin);
        setLayoutParams( mlp );

        // アニメーションを付与
/*        animate().translationY(1f)
                 .setDuration(300)
                 .setListener(null);*/

        // 本ブロックの下ブロックも下げる
        Block belowBlock = getBelowBlock();
        if (belowBlock != null) {
            belowBlock.downChartPosition(trancelate);
        }
    }

    /*
     * ブロック削除
     */
    public void removeOnChart(){

        Log.i("クラスメソッド", "removeOnChart   ID=" + this.getId());

        int height = getHeight();

        // 自身をチャートから削除
        ViewGroup chart = (ViewGroup) getParent();
        chart.removeView( this );

        // 下ブロックを上に移動させる
        Block belowBlock = getBelowBlock();
        if (belowBlock != null) {
            belowBlock.upChartPosition(height);
            Log.i("クラスメソッド", "上に移動 type=" + belowBlock.getProcessType());
            Log.i("クラスメソッド", "上に移動   ID=" + belowBlock.getId());
            Log.i("クラスメソッド", "=======================");
        }
    }

    /*
     * ネスト内に指定されたブロックがあるか
     * ※Blockクラスは入れ子を持たないため、必ずfalseを返す
     */
    public boolean hasBlock(Block checkBlock ) {
        return false;
    }

    /*
     * 自身がネスト内にあるかどうか
     */
    public boolean inNest() {
        // 親ネストブロックを保持しているかどうかで判定
        return (getOwnNestBlock() != null);
    }

    /*
     * 自身の下にブロックがあるかどうか
     * ※ネスト内であれば、ネスト内において自身の下にブロックがあるかどうか
     */
    public boolean hasBelowBlock() {
        // 下にブロックがあるかどうかで判定
        return (getBelowBlock() != null);
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
        int markImageID = getMarkImageViewID();
        ImageView iv_bottomMark = findViewById( markImageID );
        iv_bottomMark.setVisibility(visible);
    }

    /*
     * マーカ―有無
     */
    public boolean isMarked(){
        // マーカー表示中なら、マーク中と判断
        ImageView iv_bottomMark = findViewById(R.id.iv_mark);
        return (iv_bottomMark.getVisibility() == VISIBLE);
    }

    /*
     * マークエリアリスナー設定
     */
    public void setMarkAreaListerner(MarkerAreaListener listener){

        // 本ブロック
        Block selfBlock = this;

        int markAreaID = getMarkAreaViewID();
        ViewGroup markArea = getLayoutRootView().findViewById( markAreaID );
        markArea.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // リスナーコール
                listener.onBottomMarkerAreaClick(selfBlock);
            }
        });
    }

    /*
     * 処理ブロックドロップリスナーの設定
     */
    public void setDropBlockListerner(DropBlockListener listener) {

        // 本ブロック
        Block selfBlock = this;

        getLayoutRootView().setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                return listener.onDropBlock( selfBlock, dragEvent );
            }
        });
    }

    public interface MarkerAreaListener {
        // マーカー処理ブロック下部への移動アイコンクリックリスナー
        void onBottomMarkerAreaClick(Block markedBlock);
    }
    public interface DropBlockListener {
        // 処理ブロックドロップリスナー
        boolean onDropBlock(Block dropBlock, DragEvent dragEvent);
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
