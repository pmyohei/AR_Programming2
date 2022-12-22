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
    public static final float TRANCE_ON_DRAG = 0.6f;
    public static final float TRANCE_OFF_DRAG = 1.0f;


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
    public void tranceOnDrag() {
        setAlpha(TRANCE_ON_DRAG);
    }

    /*
     * ブロック半透明化解除
     */
    public void tranceOffDrag() {
        setAlpha(TRANCE_OFF_DRAG);
    }

    /*
     * チャート上で、指定ブロックが自身よりも下にあるかどうか
     */
    public boolean existsBelow(Block checkBlock) {

        int checkID = checkBlock.getId();

        Log.i("チャート確定問題", "更新対象 existsBelow checkID=" + checkID);

        // 下ブロックを検索
        Block belowBlock = getBelowBlock();
        while (belowBlock != null) {

            Log.i("チャート確定問題", "更新対象 existsBelow belowBlock.getId()=" + belowBlock.getId());

            if (belowBlock.getId() == checkID) {
                return true;
            }
            belowBlock = belowBlock.getBelowBlock();
        }
        return false;
    }

    /*
     * ブロック位置移動：上
     */
    public void upChartPosition(int trancelate) {

        // マージンを再設定し、位置を下げる
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) getLayoutParams();
        mlp.setMargins(mlp.leftMargin, mlp.topMargin - trancelate, mlp.rightMargin, mlp.bottomMargin);
        setLayoutParams(mlp);

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
        setLayoutParams(mlp);

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
     * ブロック位置更新
     */
    public void updatePosition() {

        // 位置に変化がなければ
        Block aboveBlock = getAboveBlock();
        if( shouldUpdate(aboveBlock) ){
            // 位置更新
            setPositionMlp( aboveBlock );
            startUpdatePositionAnimation();

            // ブロック標高設定
            updateBlockElevation();
        }

        post(() -> {
            // 下ブロック位置を更新
            if (hasBelowBlock()) {
                getBelowBlock().updatePosition();
            }

            // ネスト内にいれば、親ネストのリサイズ
            if( inNest() ){
                getOwnNestBlock().resizeNestHeight();
            }
        });


/*        // 位置に変化がなければ
        Block aboveBlock = getAboveBlock();
        if( !shouldUpdate(aboveBlock) ){
            // 親ネストのリサイズだけする
            if( inNest() ){
                getOwnNestBlock().resizeNestHeight();
            }
            return;
        }

        // 位置更新
        setPositionMlp( aboveBlock );
        startUpdatePositionAnimation();

        post(() -> {
            // 下ブロック位置を更新
            if (hasBelowBlock()) {
                getBelowBlock().updatePosition();
            }

            // ネスト内にいれば、親ネストのリサイズ
            if( inNest() ){
                getOwnNestBlock().resizeNestHeight();
            }
        });*/
    }

    /*
     * ブロック位置更新
     */
/*
    public void updatePosition() {

        Block aboveBlock = getAboveBlock();
        aboveBlock.post(() -> {
            setPositionMlp( aboveBlock );

            startUpdatePositionAnimation();

            post(() -> {
                if (hasBelowBlock()) {
                    getBelowBlock().updatePosition();
                }
            });
        });
*/


/*        // 更新判定
        Block aboveBlock = getAboveBlock();
        // 位置更新
        setPositionMlp(aboveBlock);

        // アニメーションを付与
        startUpdatePositionAnimation();

        if (hasBelowBlock()) {
            post(() -> {
                getBelowBlock().updatePosition();
            });
        }*/

/*        Block aboveBlock = getAboveBlock();
        aboveBlock.post(() -> {
            Log.i("位置更新", "id=" + getId() + " setPositionMlp()のコール");
            setPositionMlp( aboveBlock );

            // アニメーションを付与
            setTranslationY(-40f);
            animate().translationY(0f)
                     .setDuration(200)
                     .setListener(null);

            if( hasBelowBlock() ){
                getBelowBlock().updatePosition();
            }
        });*/
    //}

    /*
     *
     */
    public boolean shouldUpdate( Block aboveBlock ) {
        // 現在位置と更新位置
        int currentTop = getTop();
        int updateTop = aboveBlock.getTop() + aboveBlock.getHeight();
        // 現在位置と更新位置が違えば、更新する
        return ( currentTop != updateTop );
    }

    /*
     *
     */
    public void startUpdatePositionAnimation() {
        // アニメーションを付与
        setTranslationY(-20f);
        animate().translationY(0f)
                .setDuration(400)
                .setListener(null);
    }

    /*
     * 重なり値の設定
     */
    public void updateBlockElevation() {

        float elevation = 0f;
        if( inNest()  ){
            elevation = getOwnNestBlock().getElevation() + 1f;
        }

        setElevation( elevation );
    }

    /*
     * ブロック削除
     */
    public void removeOnChart() {
        // 自身をチャートから削除
        ViewGroup chart = (ViewGroup) getParent();
        chart.removeView(this);
    }

    /*
     * ブロック削除
     */
/*    public void removeOnChart() {

        int height = getHeight();

        // 自身をチャートから削除
        ViewGroup chart = (ViewGroup) getParent();
        chart.removeView(this);

        // 下ブロックを上に移動させる
        Block belowBlock = getBelowBlock();
        if (belowBlock != null) {
            belowBlock.upChartPosition(height);
        }
    }*/

    /*
     * 下ブロックを上に移動
     */
    public void upBelowBlock() {

        int height = getHeight();

        // 下ブロックを上に移動させる
        Block belowBlock = getBelowBlock();
        if (belowBlock != null) {
            belowBlock.upChartPosition(height);
        }
    }

    /*
     *
     */
    public void setChartPosition(int left, int top) {

        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) getLayoutParams();
        mlp.setMargins(left, top, mlp.rightMargin, mlp.bottomMargin);
        setLayoutParams( mlp );
    }

    /*
     *
     */
    public void setChartPosition( Block aboveBlock ) {

        Log.i("ネスト移動", "setChartPosition()　コール ブロック側");

        ViewGroup.MarginLayoutParams mlp = getMlp( aboveBlock );
//        mlp.setMargins(mlp.leftMargin, mlp.topMargin, mlp.rightMargin, mlp.bottomMargin);
        setLayoutParams( mlp );
    }

    /*
     *
     */
    public void setPositionMlp(Block aboveBlock) {

        int top = aboveBlock.getTop() + aboveBlock.getHeight();
        int left = aboveBlock.getLeft();

        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) getLayoutParams();
        mlp.setMargins(left, top, mlp.rightMargin, mlp.bottomMargin);

        setLayoutParams( mlp );
    }


    /*
     *
     */
    private ViewGroup.MarginLayoutParams getMlp(Block aboveBlock) {

        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) getLayoutParams();

        int top = aboveBlock.getTop() + aboveBlock.getHeight();
        int left = aboveBlock.getLeft();

        if (aboveBlock.inNest()) {
            left = aboveBlock.getLeft();
        }

        mlp.topMargin = top;
        mlp.leftMargin = left;

        return mlp;
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

/*    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        Log.i("ネスト移動", "onLayout()　Block コール id" + getId());

        // 下ブロック位置を更新
        if (hasBelowBlock()) {
            getBelowBlock().updatePosition();
        }

        // ネスト内にいれば、親ネストのリサイズ
        if( inNest() ){
            getOwnNestBlock().resizeNestHeight();
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure( widthMeasureSpec, heightMeasureSpec );

        Log.i("ネスト移動", "onMeasure()　Block コール id" + getId());
    }*/
}
