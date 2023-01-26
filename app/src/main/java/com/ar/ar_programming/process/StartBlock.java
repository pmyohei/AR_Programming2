package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.ar.ar_programming.R;


/*
 * 開始処理ブロック
 */
public class StartBlock extends Block {

    //---------------------------
    // 定数
    //----------------------------

    //---------------------------
    // フィールド変数
    //----------------------------

    /*
     * コンストラクタ
     */
    public StartBlock(Context context) {
        this(context, (AttributeSet) null);
    }
    public StartBlock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public StartBlock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle, PROCESS_TYPE_START);
    }

    /*
     * レイアウト設定
     */
    @Override
    public void setLayout( int layoutID ) {
        super.setLayout( layoutID );
    }

    /*
     * レイアウト最上位ビューIDを取得
     */
    @Override
    public View getLayoutRootView(){
        return findViewById( R.id.ll_startRoot );
    }
    /*
     * マークエリアビューを取得
     */
    @Override
    public int getMarkAreaViewID(){
        return R.id.cl_markAreaInStart;
    }
    /*
     * マークエリアのマークイメージIDを取得
     */
    @Override
    public int getMarkImageViewID(){
        return R.id.iv_markInStart;
    }

    /*
     * ドロップラインビューIDを取得
     */
    @Override
    public int getDropLineViewID(){
        return R.id.v_dropLineStart;
    }

    /*
     * ブロック位置更新
     */
    @Override
    public void updatePosition() {

        //----------------
        // 位置変化判定
        //----------------
        if( shouldUpdatePosition() ){
            // 現在位置を更新
            int direction = setPositionMlp();
            startUpdatePositionAnimation( direction );

            // ブロック標高設定
            updateBlockElevation();

            // 非表示状態（初位置更新）なら、表示させる
            if( getVisibility() == INVISIBLE ){
                setVisibility( VISIBLE );
            }
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
    public boolean shouldUpdatePosition() {

        // 親ネストなし（チャートのスタートブロック）の場合は、位置固定のため更新なし
        NestBlock parentNest = getOwnNestBlock();
        if( parentNest == null ){
            return false;
        }

        // 現在位置と更新位置
        int targetNest = parentNest.whichInNest( this );
        int currentTop = getTop();
        int updateTop = parentNest.getStartBlockTopMargin( targetNest );

        // 現在位置と更新位置が違えば、更新する
        return ( currentTop != updateTop );
    }

    /*
     * MarginLayoutParams設定
     *  ネスト内右上に位置するようにパラメータを設定する
     */
    public int setPositionMlp() {

        NestBlock parentNest = getOwnNestBlock();
        int targetNest = parentNest.whichInNest( this );

        // 上左マージンを取得
        int left = parentNest.getStartBlockLeftMargin( targetNest );
        int top = parentNest.getStartBlockTopMargin( targetNest );

        // スタートブロック位置を更新
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) getLayoutParams();
        if( mlp == null ){
            return BLOCK_POSITION_DOWN;
        }

        int currentTop = mlp.topMargin;
        mlp.setMargins(left, top, mlp.rightMargin, mlp.bottomMargin);
        setLayoutParams( mlp );

        // 位置更新の方向が上か下か
        return ((top - currentTop) > 0 ? BLOCK_POSITION_DOWN : BLOCK_POSITION_UP );
    }
}
