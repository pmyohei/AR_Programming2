package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
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
        super(context, attrs, defStyle);
//        View.inflate(context, R.layout.process_block_start_ver2, this);
    }

    /*
     * レイアウト設定
     */
    @Override
    public void setLayout( int layoutID ) {
        super.setLayout( layoutID );
    }

}
