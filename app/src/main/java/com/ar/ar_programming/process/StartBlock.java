package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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

        Log.i("ドロップリスナー", "getDropLineViewID Start側取得");
        return R.id.v_dropLineStart;
    }

    /*
     *
     */
    @Override
    public void updatePosition() {

        if( !shouldUpdate(null) ){
            return;
        }

        // 現在位置を更新
        setPositionMlp();

        // アニメーションを付与
        startUpdatePositionAnimation();

        if( hasBelowBlock() ){
            post(() -> {
                getBelowBlock().updatePosition();
            });
        }
    }

    /*
     *
     */
    @Override
    public boolean shouldUpdate( Block aboveBlock ) {
        // 現在位置と更新位置
        int currentTop = getTop();

        NestProcessBlock parentNest = getOwnNestBlock();
        int updateTop = parentNest.getStartBlockTopMargin();

        // 現在位置と更新位置が違えば、更新する
        return ( currentTop != updateTop );
    }

    /*
     *
     */
    public void setPositionMlp() {

        NestProcessBlock parentNest = getOwnNestBlock();

        // 上と左マージンを取得
        int left = parentNest.getStartBlockLeftMargin();
        int top = parentNest.getStartBlockTopMargin();

        // スタートブロック位置を更新
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) getLayoutParams();
        if( mlp == null ){
            return;
        }
        mlp.setMargins(left, top, mlp.rightMargin, mlp.bottomMargin);
        setLayoutParams( mlp );
    }


}
