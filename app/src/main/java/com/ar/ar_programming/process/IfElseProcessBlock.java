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
 * ネストあり処理ブロック（if文）
 */
public class IfElseProcessBlock extends NestProcessBlock {

    //---------------------------
    // 定数
    //---------------------------

    //---------------------------
    // フィールド変数
    //---------------------------
    private int mBlockInNestIndex;
    private int tmploopCount = 0;
    private ViewGroup mNestRoot;

    /*
     * コンストラクタ
     */
    public IfElseProcessBlock(Context context) {
        this(context, null);
    }

    public IfElseProcessBlock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IfElseProcessBlock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        View.inflate(context, R.layout.process_block_if_else, this);

        // ネスト処理ブロック初期処理
        init();
    }

    /*
     * 初期化処理
     */
    private void init() {
        mProcessType = PROCESS_TYPE_IF_ELSE;

        // ネスト内処理indexを初期化
        mBlockInNestIndex = 0;

        // ネスト親レイアウト
        // ※初期状態では、ifルートにしておく
        mNestRoot = findViewById(R.id.ll_firstNestRoot);

        //-----------------------
        // onDragリスナー
        //-----------------------
        // 本ブロック自身
        setDragAndDropListerner();
        // 本ブロック内のネスト
        setDragAndDropFirstNestListerner();
        setDragAndDropSecondNestListerner();
    }


    /*
     * onDragリスナーの設定（ネスト２つ目の親レイアウト）
     */
    public void setDragAndDropSecondNestListerner() {

        // 入れ子の親レイアウトにリスナーを設定
        ViewGroup ll_secondNestRoot = findViewById(R.id.ll_secondNestRoot);
        ll_secondNestRoot.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                switch (dragEvent.getAction()) {

                    case DragEvent.ACTION_DRAG_STARTED:
                        Log.i("ドラッグテスト nest2", "ACTION_DRAG_STARTED");
                        return true;

                    case DragEvent.ACTION_DRAG_ENTERED:
                        Log.i("ドラッグテスト nest2", "ACTION_DRAG_ENTERED");

                        //----------------------
                        // 同一ビューチェック
                        //----------------------
                        // ドラッグされてきたビューが、本ビューであれば何もしない
                        if (isSameDraggedView(dragEvent)) {
                            return false;
                        }

                        //-------------------------
                        // 処理追加イメージビューの生成
                        //-------------------------
                        createAddImageViewToNestParent();

                        return true;

                    case DragEvent.ACTION_DRAG_LOCATION:
                        Log.i("ドラッグテスト nest2", "ACTION_DRAG_LOCATION");
                        return true;

                    case DragEvent.ACTION_DRAG_EXITED:
                        Log.i("ドラッグテスト nest2", "ACTION_DRAG_EXITED");

                        // 処理追加イメージビューの削除
//                        ViewGroup parent = findViewById(R.id.ll_secondNestRoot);
                        removeAddImageView( ll_secondNestRoot );

                        return true;

                    case DragEvent.ACTION_DROP:
                        Log.i("ドラッグテスト nest2", "ACTION_DROP");

                        //----------------------
                        // 同一ビューチェック
                        //----------------------
                        // ドラッグされてきたビューが、同じビューであれば何もしない
                        if (isSameDraggedView(dragEvent)) {
                            return false;
                        }

                        // 処理追加イメージビューの削除
//                        parent = findViewById(R.id.ll_secondNestRoot);
                        removeAddImageView( ll_secondNestRoot );
                        // ドロップされた処理を元の位置から削除
                        removeProcessView(dragEvent);
                        // ドロップされた処理を生成
                        createProcessViewToNestParent(dragEvent);

                        return true;

                    case DragEvent.ACTION_DRAG_ENDED:
                        Log.i("ドラッグテスト nest2", "ACTION_DRAG_ENDED");
                        return true;

                    default:
                        Log.i("ドラッグテスト nest2", "default");
                        break;
                }

                return false;
            }
        });
    }


    /*
     * 処理文言を設定
     */
    private void setProcessWording() {

        // 処理内容と単位の文言ID
        int contentId;

        // 種別に応じた文言IDを取得
        switch (mProcessKind) {
            case PROC_KIND_IF:
                contentId = R.string.block_contents_if;
                break;

            case PROC_KIND_IF_ELSE:
                contentId = R.string.block_contents_if;
                break;

            default:
                contentId = R.string.block_contents_if;
                break;
        }

        // 文言IDをレイアウトに設定
        TextView tv_contents = findViewById(R.id.tv_contents);
        tv_contents.setText(contentId);
    }


    /*
     * 「プログラミング処理種別」の設定
     */
    @Override
    public void setProcessKind(int processKind) {
        super.setProcessKind(processKind);

        // 種別に応じた文言に変更
        setProcessWording();
    }

    /*
     * ネスト内の処理ブロック数を取得
     */
    public int getProcessInNestNum( boolean isCondition ) {

        ViewGroup nestRoot;

        // 条件の真偽値に応じたネストルートレイアウトを取得
        if( isCondition ){
            nestRoot = findViewById(R.id.ll_firstNestRoot);
        } else{
            nestRoot = findViewById(R.id.ll_secondNestRoot);
        }

        return nestRoot.getChildCount();
    }

    /*
     * ネスト内の処理ブロックを取得
     * 　返す対象は、先頭から順番に行う。
     * 　ただし、以下の状況にある場合、nullを返す
     *   ・ネスト内の処理ブロック数が0
     *   ・ネスト内の処理ブロックを最後まで返した
     */
    @Override
    public ProcessBlock getProcessInNest() {

        //--------------------
        // 取得処理ブロックチェック
        //--------------------
        int blockInNestNum = mNestRoot.getChildCount();
        if( blockInNestNum == 0 ){
            // 処理ブロックなし
            return null;
        }
        if( mBlockInNestIndex >= blockInNestNum ){
            // 処理ブロック最後まで取得
            return null;
        }

        //--------------------
        // ネスト内処理ブロック
        //--------------------
        // ネスト内処理ブロックをコールされた順に応じて返す
        ProcessBlock block = (ProcessBlock) mNestRoot.getChildAt( mBlockInNestIndex );
        // 次回コールでは次の処理ブロックを返すために、indexを進める
        mBlockInNestIndex++;

        return block;
    }


    /*
     * 条件成立判定
     *   @return：条件成立- true
     *   @return：条件不成立- false
     */
    @Override
    public boolean isConditionTrue(CharacterNode characterNode) {

        tmploopCount++;
//        boolean tmp = (tmploopCount == 2);
        boolean tmp = true;

        // 条件の真偽値に応じたネストルートレイアウトを取得
        if( tmp ){
            mNestRoot = findViewById(R.id.ll_firstNestRoot);
        } else{
            mNestRoot = findViewById(R.id.ll_secondNestRoot);
        }

        return tmp;
    }

}

