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
 * ネストあり処理ブロック基底クラス
 */
public abstract class NestProcessBlock_old extends ProcessBlock_old {

    //---------------------------
    // 定数
    //---------------------------

    //---------------------------
    // フィールド変数
    //---------------------------

    /*
     * コンストラクタ
     */
    public NestProcessBlock_old(Context context) {
        this(context, null);
    }

    public NestProcessBlock_old(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestProcessBlock_old(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
//        View.inflate(context, R.layout.process_block_if, this);

        // ネスト処理ブロック初期処理
//        init();
    }

    /*
     * 初期化処理
     */
    private void init() {
        // onDragリスナーの設定（入れ子の親レイアウト側）
        setDragAndDropFirstNestListerner();
        // onDragリスナーの設定
        setDragAndDropListerner();

        setMarkAreaInNestListerner();
    }


    /*
     * マークエリアリスナーの設定
     */
    public void setMarkAreaInNestListerner() {

        ProcessBlock_old myself = this;

        ViewGroup cl_bottomMarkArea = findViewById(R.id.cl_markArea);
        // マークを付与
        cl_bottomMarkArea.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // リスナーコール
                mMarkerAreaClickListener.onBottomMarkerAreaClick(myself);
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
                contentId = R.string.block_contents_if_block;
                break;

            case PROC_KIND_IF_ELSE:
                contentId = R.string.block_contents_if_block;
                break;

            case PROC_KIND_LOOP_OBSTACLE:
                contentId = R.string.block_contents_loop_arrival_goal;
                break;

            default:
                contentId = R.string.block_contents_if_block;
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
     * onDragリスナーの設定（ネスト１つ目の親レイアウト）
     */
    public void setDragAndDropFirstNestListerner() {

        // 入れ子の親レイアウトにリスナーを設定
        ViewGroup ll_firstNestRoot = findViewById(R.id.ll_firstNestRoot);
        ll_firstNestRoot.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {

                Log.i("ドラッグテスト nest", "top id=" + getId());

                switch (dragEvent.getAction()) {

                    case DragEvent.ACTION_DRAG_STARTED:
                        Log.i("ドラッグテスト nest", "ACTION_DRAG_STARTED");
                        return true;

                    case DragEvent.ACTION_DRAG_ENTERED:
                        Log.i("ドラッグテスト nest", "ACTION_DRAG_ENTERED");

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
                        Log.i("ドラッグテスト nest", "ACTION_DRAG_LOCATION");
                        return true;

                    case DragEvent.ACTION_DRAG_EXITED:
                        Log.i("ドラッグテスト nest", "ACTION_DRAG_EXITED");

                        // 処理追加イメージビューの削除
//                        ViewGroup parent = findViewById(R.id.ll_firstNestRoot);
                        removeAddImageView( ll_firstNestRoot );

                        return true;

                    case DragEvent.ACTION_DROP:
                        Log.i("ドラッグテスト nest", "ACTION_DROP");

                        //----------------------
                        // 同一ビューチェック
                        //----------------------
                        // ドラッグされてきたビューが、同じビューであれば何もしない
                        if (isSameDraggedView(dragEvent)) {
                            return false;
                        }

                        // 処理追加イメージビューの削除
//                        parent = findViewById(R.id.ll_firstNestRoot);
                        removeAddImageView( ll_firstNestRoot );
                        // ドロップされた処理を元の位置から削除
                        removeProcessView(dragEvent);
                        // ドロップされた処理を生成
                        createProcessViewToNestParent(dragEvent);

                        return true;

                    case DragEvent.ACTION_DRAG_ENDED:
                        Log.i("ドラッグテスト nest", "ACTION_DRAG_ENDED");
                        return true;

                    default:
                        Log.i("ドラッグテスト nest", "default");
                        break;
                }

                return false;
            }
        });
    }

    /*
     * 処理追加イメージビューの生成（入れ子の親レイアウト側）
     */
    public void createAddImageViewToNestParent() {

        //-------------------------------
        // 処理追加イメージビューをレイアウトに追加
        //-------------------------------
        // 処理追加イメージビューを生成
        View addImageView = new View(getContext());
        addImageView.setBackgroundColor(getResources().getColor(R.color.black_50));
        addImageView.setTag(TAG_ADD_IMAGE_VIEW);

        // 親レイアウトに追加
        ViewGroup parentView = findViewById(R.id.ll_firstNestRoot);
        parentView.addView(addImageView, 0, new ViewGroup.LayoutParams(200, 80));
    }

    /*
     * 処理ブロックの生成（入れ子の親レイアウト側）
     */
    public void createProcessViewToNestParent(DragEvent dragEvent) {

        //-------------------------------
        // 処理ブロックをレイアウトに追加
        //-------------------------------
        // 「ドラッグされてきた処理ブロック」を取得
        ProcessBlock_old draggedProcessBlock = (ProcessBlock_old) dragEvent.getLocalState();
        
        // 「ドラッグされてきた処理ブロック」の親ネストブロックに、本ネストブロックを設定
        draggedProcessBlock.setParentNestBlock( this );

        // 入れ子内のトップに追加
        ViewGroup parentView = findViewById(R.id.ll_firstNestRoot);
        parentView.addView(draggedProcessBlock, 0);
    }

    /*
     * ネスト内の処理ブロック数を取得
     */
    public int getProcessInNestNum() {
        // 指定された位置の処理ブロックを返す
        ViewGroup ll_insideRoot = findViewById(R.id.ll_firstNestRoot);
        return ll_insideRoot.getChildCount();
    }

    /*
     * ネスト内の処理ブロックを取得
     */
    public ProcessBlock_old getProcessInNest(int childIndex ) {
        // 指定された位置の処理ブロックを返す
        ViewGroup ll_insideRoot = findViewById(R.id.ll_firstNestRoot);
        return (ProcessBlock_old)ll_insideRoot.getChildAt( childIndex );
    }

    /*
     * ネスト内の処理ブロックを取得
     *   ※引数なしの本メソッドは、抽象メソッドとする
     */
    public abstract ProcessBlock_old getProcessInNest();

    /*
     * ネスト内条件判定
     */
    public abstract boolean isConditionTrue(CharacterNode characterNode);


}

