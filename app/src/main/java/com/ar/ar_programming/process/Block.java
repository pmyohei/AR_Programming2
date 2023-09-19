package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.ar.ar_programming.Gimmick;
import com.ar.ar_programming.R;


/*
 * 処理ブロック基本クラス
 */
public abstract class Block extends ConstraintLayout {

    //---------------------------
    // 定数
    //---------------------------
    // ドラッグ中（選択中）状態の半透明値
    public static final float TRANCE_ON_DRAG = 0.6f;
    public static final float TRANCE_OFF_DRAG = 1.0f;

    // ブロック位置更新方向
    public static final int BLOCK_POSITION_UP = 0;
    public static final int BLOCK_POSITION_DOWN = 1;

    //---------------------------
    // フィールド変数
    //---------------------------
    public Gimmick.XmlBlockInfo mXmlBlockInfo;
    // 本ブロックが組み込まれているネストブロック（ない場合はnull）
    public NestBlock mOwnNestBlock;
    private Block mAboveBlock;
    private Block mBelowBlock;

    /*
     * コンストラクタ
     */
    public Block(Context context) {
        this(context, null);
    }
    public Block(Context context, AttributeSet attrs) {
        this(context, attrs, 0, null);
    }
    public Block(Context context, AttributeSet attrs, int defStyle, Gimmick.XmlBlockInfo xmlBlockInfo) {
        super(context, attrs, defStyle);

        mXmlBlockInfo = xmlBlockInfo;
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
    public String getType() {
        return mXmlBlockInfo.type;
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
     * ブロック位置更新
     */
    public void updatePosition() {

        //----------------
        // 位置変化判定
        //----------------
        // 位置に変化がある場合のみ、更新処理を行う
        Block aboveBlock = getAboveBlock();
        if( shouldUpdatePosition(aboveBlock) ){
            // 位置更新
            int direction = setPositionMlp( aboveBlock );
            startUpdatePositionAnimation( direction );
            // ブロック標高設定
            updateBlockElevation();
        }

        //--------------------
        // 本ブロックレイアウト確定
        //--------------------
        post(() -> {
            // 下ブロック位置を更新
            if (hasBelowBlock()) {
                getBelowBlock().updatePosition();
            }

            // 親ネストのリサイズ
            if( inNest() ){
                getOwnNestBlock().resizeNestHeight( this );
            }
        });
    }

    /*
     * 位置更新すべきか判定
     */
    public boolean shouldUpdatePosition(Block aboveBlock ) {

        // 現在位置と更新位置
        int currentTop = getTop();
        int updateTop = aboveBlock.getTop() + aboveBlock.getHeight();

        // 現在位置と更新位置が違えば、更新する
        return ( currentTop != updateTop );
    }

    /*
     * 位置更新アニメーションを開始
     */
    public void startUpdatePositionAnimation( int trancelationDirection ) {

        //アニメーションのためのY初期値（ブロックの移動方向にあわせる）
        float translationY;
        if( trancelationDirection == BLOCK_POSITION_UP ){
            translationY = 4;
        }else{
            translationY = -10;
        }

        // アニメーションを付与
        setTranslationY( translationY );
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
            // 本ブロックがネスト内にあれば、親ネストより大きい値を設定
            // （確実にネストブロックよりも上にくるようにする）
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
     * MarginLayoutParams設定
     *  指定ブロックの下に位置するようにパラメータを設定する
     */
    public int setPositionMlp(Block aboveBlock) {

        // 上ブロックの左下に位置できる値を取得
        int top = aboveBlock.getTop() + aboveBlock.getHeight();
        int left = aboveBlock.getLeft();

        // 本ブロックのマージンを変更し、位置更新
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) getLayoutParams();
        int currentTop = mlp.topMargin;
        mlp.setMargins(left, top, mlp.rightMargin, mlp.bottomMargin);
        setLayoutParams( mlp );

        // 位置更新の方向が上か下か
        return ((top - currentTop) > 0 ? BLOCK_POSITION_DOWN : BLOCK_POSITION_UP );
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

    /*
     * レイアウト設定
     */
    public void setLayout(int layoutID){
        View.inflate(getContext(), layoutID, this);
    }

    /*
     * 「本ブロックが組み込まれているネストブロック」設定
     */
    public void setOwnNestBlock( NestBlock nestBlock ) {
        mOwnNestBlock = nestBlock;
    }

    /*
     * 「本ブロックが組み込まれているネストブロック」取得
     */
    public NestBlock getOwnNestBlock() {
        return mOwnNestBlock;
    }

    /*
     * interface
     */
    // マーカー処理ブロック下部への移動アイコンクリックリスナー
    public interface MarkerAreaListener {
        void onBottomMarkerAreaClick(Block markedBlock);
    }
    // 処理ブロックドロップリスナー
    public interface DropBlockListener {
        boolean onDropBlock(Block dropBlock, DragEvent dragEvent);
    }
}
