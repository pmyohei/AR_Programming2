package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
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

    /*
     * マークエリアリスナーの設定
     */
    @Override
    public void setMarkAreaListerner( BottomMarkerAreaListener listener ) {

        Block myself = this;

        ViewGroup cl_bottomMarkArea = findViewById(R.id.cl_bottomMarkArea);
        // マークを付与
        cl_bottomMarkArea.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // リスナーコール
                listener.onBottomMarkerAreaClick(myself);
            }
        });
    }

    /*
     * マーカ―設定
     */
    @Override
    public void setMarker(boolean enable) {

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
     * ブロック下部追加マーカーの有無
     */
    @Override
    public boolean isMarked() {
        // マーカー表示中なら、マーク中と判断
        ImageView iv_bottomMark = findViewById(R.id.iv_bottomMark);
        return (iv_bottomMark.getVisibility() == VISIBLE);
    }
}
