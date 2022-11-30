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
 * ネストあり処理ビュー（ループ／分岐処理）
 */
public class NestProcessBlock extends ProcessBlock {

    //---------------------------
    // 定数
    //---------------------------


    //---------------------------
    // フィールド変数
    //---------------------------
    private int mBlockInNestIndex;

    /*
     * コンストラクタ
     */
    public NestProcessBlock(Context context) {
        this(context, null);
    }

    public NestProcessBlock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestProcessBlock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        View.inflate(context, R.layout.process_block_nest, this);

        // ネスト処理ビュー初期処理
        init();
    }

    /*
     * 初期化処理
     */
    private void init() {
        // ネスト内処理indexを初期化
        mBlockInNestIndex = 0;

        // onDragリスナーの設定（入れ子の親レイアウト側）
        setDragAndDropNestListerner();
        // onDragリスナーの設定
        setDragAndDropListerner();
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

            case PROC_KIND_LOOP_OBSTACLE:
                contentId = R.string.block_contents_loop;
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
     * onDragリスナーの設定（入れ子の親レイアウト側）
     */
    private void setDragAndDropNestListerner() {

        // 入れ子の親レイアウトにリスナーを設定
        ViewGroup ll_insideRoot = findViewById(R.id.ll_insideRoot);
        ll_insideRoot.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                switch (dragEvent.getAction()) {

                    case DragEvent.ACTION_DRAG_STARTED:
                        Log.i("ドラッグテスト2", "ACTION_DRAG_STARTED");
                        return true;

                    case DragEvent.ACTION_DRAG_ENTERED:
                        Log.i("ドラッグテスト2", "ACTION_DRAG_ENTERED");

                        //----------------------
                        // 同一ビューチェック
                        //----------------------
                        // ドラッグされてきたビューが、同じビューであれば何もしない
                        if (isSameDraggedView(dragEvent)) {
                            return false;
                        }

                        //-------------------------
                        // 処理追加イメージビューの生成
                        //-------------------------
                        createAddImageViewToNestParent();

                        return true;

                    case DragEvent.ACTION_DRAG_LOCATION:
                        Log.i("ドラッグテスト2", "ACTION_DRAG_LOCATION");
                        return true;

                    case DragEvent.ACTION_DRAG_EXITED:
                        Log.i("ドラッグテスト2", "ACTION_DRAG_EXITED");

                        // 処理追加イメージビューの削除
                        ViewGroup parent = findViewById(R.id.ll_insideRoot);
                        removeAddImageView(parent);

                        return true;

                    case DragEvent.ACTION_DROP:
                        Log.i("ドラッグテスト2", "ACTION_DROP");

                        //----------------------
                        // 同一ビューチェック
                        //----------------------
                        // ドラッグされてきたビューが、同じビューであれば何もしない
                        if (isSameDraggedView(dragEvent)) {
                            return false;
                        }

                        // 処理追加イメージビューの削除
                        parent = findViewById(R.id.ll_insideRoot);
                        removeAddImageView(parent);
                        // ドロップされた処理を元の位置から削除
                        removeProcessView(dragEvent);
                        // ドロップされた処理を生成
                        createProcessViewToNestParent(dragEvent);

                        return true;

                    case DragEvent.ACTION_DRAG_ENDED:
                        Log.i("ドラッグテスト2", "ACTION_DRAG_ENDED");
                        return true;

                    default:
                        Log.i("ドラッグテスト2", "default");
                        break;
                }

                return false;
            }
        });
    }

    /*
     * 処理追加イメージビューの生成（入れ子の親レイアウト側）
     */
    private void createAddImageViewToNestParent() {

        //-------------------------------
        // 処理追加イメージビューをレイアウトに追加
        //-------------------------------
        // 処理追加イメージビューを生成
        View addImageView = new View(getContext());
        addImageView.setBackgroundColor(getResources().getColor(R.color.black_50));
        addImageView.setTag(TAG_ADD_IMAGE_VIEW);

        // 親レイアウトに追加
        ViewGroup parentView = findViewById(R.id.ll_insideRoot);
        parentView.addView(addImageView, 0, new ViewGroup.LayoutParams(200, 80));
    }

    /*
     * 処理ビューの生成（入れ子の親レイアウト側）
     */
    private void createProcessViewToNestParent(DragEvent dragEvent) {

        //-------------------------------
        // 処理ビューをレイアウトに追加
        //-------------------------------
        // ドラッグされてきた処理ビューを取得
        View processView = (View) dragEvent.getLocalState();

        // 親レイアウトに追加
        ViewGroup parentView = findViewById(R.id.ll_insideRoot);
        parentView.addView(processView, 0);
    }


    /*
     * ネスト内の処理ブロックを取得
     */
    public ProcessBlock getProcessInNest( int childIndex ) {
        // 指定された位置の処理ブロックを返す
        ViewGroup ll_insideRoot = findViewById(R.id.ll_insideRoot);
        return (ProcessBlock)ll_insideRoot.getChildAt( childIndex );
    }

    /*
     * ネスト内の処理ブロック数を取得
     */
    public ProcessBlock getProcessInNest() {

        //--------------------
        // ネスト内処理ブロック
        //--------------------
        // ネスト内処理ブロックをコールされた順に応じて返す
        ViewGroup ll_insideRoot = findViewById(R.id.ll_insideRoot);
        ProcessBlock block = (ProcessBlock)ll_insideRoot.getChildAt( mBlockInNestIndex );

        //--------------------------
        // 返す処理ブロックIndexの更新
        //--------------------------
        // 最後のindexまで到達した場合、先頭indexに戻す
        mBlockInNestIndex++;
        int blockInNestNum = ll_insideRoot.getChildCount();
        if( mBlockInNestIndex >= blockInNestNum ){
            mBlockInNestIndex = 0;
        }

        return block;
    }

    /*
     * ネスト内の処理ブロック数を取得
     */
    public int getProcessInNestNum() {
        // 指定された位置の処理ブロックを返す
        ViewGroup ll_insideRoot = findViewById(R.id.ll_insideRoot);
        return ll_insideRoot.getChildCount();
    }

    /*
     * 条件成立判定
     *   @return：ループ終了（ループ条件不成立）- true
     *   @return：ループ継続（ループ条件成立　）- false
     */
    public boolean isFinishLoop(CharacterNode characterNode) {

        // 条件成立判定は、ネストindexが先頭を指している時のみ行う
        if( mBlockInNestIndex > 0 ){
            return false;
        }

        //----------------
        // 条件成立判定
        //----------------
        switch ( mProcessKind ) {
            // ゴールしているかどうか
            case ProcessBlock.PROC_KIND_LOOP_GOAL:
                return !characterNode.isGoaled();

            // 障害物と衝突中
            case ProcessBlock.PROC_KIND_LOOP_OBSTACLE:
                return false;
        }

        return false;
    }

}

