package com.ar.ar_programming.process;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ar.ar_programming.R;


/*
 * 処理ブロック
 * 　　「SingleProcessView」「NestProcessView」等の基本クラス
 */
public class TestBlock extends ConstraintLayout {

    //---------------------------
    // 定数
    //----------------------------
    public final String TAG_ADD_IMAGE_VIEW = "AddImageView";

    // 処理ブロック種別
    public static final int PROCESS_TYPE_SINGLE = 0;
    public static final int PROCESS_TYPE_IF = 1;
    public static final int PROCESS_TYPE_IF_ELSE = 2;
    public static final int PROCESS_TYPE_LOOP = 3;

    // 処理ブロック内容
    // 単体処理
    public static final int PROC_KIND_FORWARD = 0;
    public static final int PROC_KIND_BACK = 1;
    public static final int PROC_KIND_RIGHT_ROTATE = 2;
    public static final int PROC_KIND_LEFT_ROTATE = 3;
    // ネスト処理
    public static final int PROC_KIND_IF = 4;
    public static final int PROC_KIND_IF_ELSE = 5;
    public static final int PROC_KIND_LOOP_GOAL = 6;            // ゴールするまで
    public static final int PROC_KIND_LOOP_OBSTACLE = 7;        // 障害物と衝突するまで

    // 処理ブロック内容
    public static final int PROCESS_KIND_SIGNLE = 0;
    public static final int PROCESS_KIND_NEST_IF = 1;
    public static final int PROCESS_KIND_NEST_LOOP = 2;
    public static final int PROCESS_KIND_IF = 1;
    public static final int PROCESS_KIND_IF_ELSE = 2;
    public static final int PROCESS_KIND_LOOP = 3;

    // ドラッグ中（選択中）状態の半透明値
    public final float DRAGGING_TRANCE = 0.6f;
    public final float NOT_DRAGGING_TRANCE = 1.0f;

    //---------------------------
    // フィールド変数
    //----------------------------
    // マーカーエリアクリックリスナー
    private RemoveBlockClickListener mRemoveBlockClickListener;
    private MoveBelowMarkerClickListener mMoveBelowMarkerClickListener;
    public BottomMarkerAreaClickListener mMarkerAreaClickListener;


    public int mProcessType;
    public int mProcessKind;
    public int mProcessContent;
    public String tmpStr;


    public TestBlock(Context context) {
        this(context, null);
    }

    public TestBlock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TestBlock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        View.inflate(context, R.layout.process_block_test_tmp, this);
        // 処理ブロック共通初期化処理
        init();
    }

    /*
     * 初期化処理
     */
    private void init() {

        TextView et_value = findViewById(R.id.et_value);
        et_value.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("クリックチェック", "テキスト１");
            }
        });
    }

    /*
     *
     */
    public void setText( String test ) {
        TextView et_value = findViewById(R.id.et_value);
        et_value.setText( test );
    }


    /*
     * onLongClickリスナーの設定
     */
    public void setLongClickListerner() {

        // ドラッグ用
        setOnLongClickListener(new OnLongClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onLongClick(View view) {

                //--------------------------
                // 当処理ブロックを半透明にする
                //--------------------------
                view.setAlpha(DRAGGING_TRANCE);

                //--------------------------
                // ドロップ先へ渡すデータ生成
                //--------------------------
                // ClipDataとしてビューIDを渡す
                ClipData.Item item = new ClipData.Item(Integer.toString(view.getId()));
                ClipData dragData = new ClipData(
                        (CharSequence) Integer.toString(view.getId()),
                        new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN},
                        item);

                //--------------------------
                // ドラッグ処理
                //--------------------------
                DragShadowBuilder myShadow = new DragShadowBuilder(view);
                view.startDragAndDrop(dragData, myShadow, view, 0);

                return true;
            }
        });
    }

    /*
     * onDragリスナーの設定
     *   ドロップを受ける際の処理
     */
    public void setDragAndDropListerner() {

        // レイアウト最上位にリスナーを設定
        setOnDragListener(new OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {

                switch (dragEvent.getAction()) {

                    case DragEvent.ACTION_DRAG_STARTED:
                        Log.i("ドラッグテスト block", "ACTION_DRAG_STARTED id=" + getId());
                        return true;

                    case DragEvent.ACTION_DRAG_ENTERED:
                        Log.i("ドラッグテスト block", "ACTION_DRAG_ENTERED id=" + getId());

                        //----------------------
                        // 同一ビューチェック
                        //----------------------
                        // ドラッグされてきたビューが、同じビューであれば何もしない
                        if (isSameDraggedView(dragEvent)) {
                            return false;
                        }

                        //-------------------------
                        // 処理イメージブロックの生成
                        //-------------------------
//                        createAddImageBlock();

                        return true;

                    case DragEvent.ACTION_DRAG_LOCATION:
                        Log.i("ドラッグテスト block", "ACTION_DRAG_LOCATION id=" + getId());
                        return true;

                    case DragEvent.ACTION_DRAG_EXITED:
                        Log.i("ドラッグテスト block", "ACTION_DRAG_EXITED id=" + getId());

                        // 処理イメージブロックの削除
                        removeAddImageView((ViewGroup) getParent());

                        return true;

                    case DragEvent.ACTION_DROP:
                        Log.i("ドラッグテスト block", "ACTION_DROP id=" + getId());

                        //----------------------
                        // 同一ビューチェック
                        //----------------------
                        // ドラッグされてきたビューが、同じビューであれば何もしない
                        if (isSameDraggedView(dragEvent)) {
                            return false;
                        }

                        // 処理イメージブロックの削除
                        removeAddImageView((ViewGroup) getParent());
                        // ドロップされた処理を元の位置から削除
                        removeProcessView(dragEvent);
                        // ドロップされた処理を生成
                        createProcessBlock(dragEvent);

                        return true;

                    case DragEvent.ACTION_DRAG_ENDED:
                        Log.i("ドラッグテスト block", "ACTION_DRAG_ENDED id=" + getId());

                        // ドラッグ対象のビューの半透明を解除
                        cancelDraggingState(dragEvent);

                        return true;

                    default:
                        Log.i("ドラッグテスト block", "default id=" + getId());
                        break;
                }

                Log.i("ドラッグテスト", "来てる？");
                return false;
            }
        });
    }

    /*
     * ドラッグされてきたビュー内に、自身自身のビューが存在しているかチェック
     *   @return：true：あり：ドロップ不可
     */
    public boolean isSameDraggedView(DragEvent dragEvent) {

        // 自身のビューID
        int myselfID = getId();

        //---------------------------------------------------------
        // ドラッグされてきたビュー内に、自身のビューが存在しているかチェック
        //---------------------------------------------------------
        ViewGroup draggedView = (ViewGroup) dragEvent.getLocalState();
        View myselfView = draggedView.findViewById(myselfID);

        // 取得できれば存在しているため、trueを返す
        return (myselfView != null);
    }

    /*
     * 処理ブロックの生成
     */
    private void createProcessBlock(DragEvent dragEvent) {

        //-------------------------------
        // 処理ブロックをレイアウトに追加
        //-------------------------------
        // ドラッグされてきた処理ブロックを取得
        TestBlock processView = (TestBlock) dragEvent.getLocalState();
        // 処理ブロック生成
//        createProcessBlock(processView);
    }


    /*
     * 処理ブロックをレイアウトに追加する
     */
    public void addProcessBlockToLayout(ViewGroup parentView, int addIndex) {
        // 指定レイアウトに本ブロックを追加
        parentView.addView(this, addIndex);
    }

    /*
     * 処理ブロックをレイアウトから削除する
     */
    public void removeProcessBlockFromLayout() {
        // 子ビューから検索して、該当ビューを削除
        ViewGroup parentView = (ViewGroup) getParent();
        parentView.removeView(this);
    }

    /*
     * 処理ブロックの削除
     *   ※ない場合はなにもしない
     */
    public void removeProcessView(DragEvent dragEvent) {

        // 削除対象（ドラッグされたビュー）のID
        View draggedView = (View) dragEvent.getLocalState();
        int draggedID = draggedView.getId();

        // 子ビューから検索して、該当ビューを削除
        ViewGroup parentView = (ViewGroup) draggedView.getParent();
        if (parentView == null) {
            // 新規追加の場合は、親レイアウトなしのため処理終了
            return;
        }

        int childNum = parentView.getChildCount();
        for (int i = 0; i < childNum; i++) {
            View target = parentView.getChildAt(i);
            int id = target.getId();

            // IDの一致するビューがあれば削除
            if (id == draggedID) {
                parentView.removeView(target);
                return;
            }
        }
    }




    /*
     * 処理イメージブロックの削除
     */
    public void removeAddImageView(ViewGroup parentView) {

        // 子レイアウトの数
        int childNum = parentView.getChildCount();

        Log.i("処理イメージ", "削除 childNum=" + childNum);

        // 親レイアウトの子ビュー分繰り返し
        for (int i = 0; i < childNum; i++) {
            View target = parentView.getChildAt(i);
            String tag = (String) target.getTag();

            // タグ未設定のビューは対象外
            if (tag == null) {
                continue;
            }
            // タグが処理イメージブロックの時
            if (tag.equals(TAG_ADD_IMAGE_VIEW)) {
                parentView.removeView(target);
                return;
            }
        }
    }

    /*
     * ドラッグされたビューのドラッグ状態を解除
     * （半透明な状態から、透明な状態にする）
     */
    public void cancelDraggingState(DragEvent dragEvent) {

        // 既に解除ずみなら何もしない
        View draggedView = (View) dragEvent.getLocalState();
        if (draggedView.getAlpha() >= NOT_DRAGGING_TRANCE) {
            return;
        }

        // ドラッグされたビューのドラッグ状態を解除
        draggedView.setAlpha(NOT_DRAGGING_TRANCE);
    }

    /*
     * 「処理ブロック種別」の取得
     */
    public int getProcessType() {
        return mProcessType;
    }

    /*
     * 「処理ブロック種別」の設定
     */
    public void setProcessType(int type) {
        mProcessType = type;
    }

    /*
     * 「処理ブロック種別」の取得
     */
    public int getProcessKind() {
        return mProcessKind;
    }

    /*
     * 「処理ブロック種別」の設定
     */
    public void setProcessKind(int processKind) {
        // 処理種別の設定
        mProcessKind = processKind;
    }

    /*
     * 「処理ブロック内容」の取得
     */
    public int getProcessContent() {
        return mProcessContent;
    }


    /*
     * 「処理ブロック内容」の設定
     */
    public void setProcessContent(int processContent) {
        // 処理種別の設定
        mProcessContent = processContent;
    }




    /*
     * ブロック削除アイコンクリックリスナー設定
     */
    public void setRemoveBlockClickListener( RemoveBlockClickListener listerner ){
        mRemoveBlockClickListener = listerner;
    }
    /*
     * マーカー処理ブロック下への移動アイコンクリックリスナー設定
     */
    public void setMoveBelowMarkerClickListener( MoveBelowMarkerClickListener listerner ){
        mMoveBelowMarkerClickListener = listerner;
    }
    /*
     * マーカーエリアクリックリスナー設定
     */
    public void setBottomMarkerAreaClickListener( BottomMarkerAreaClickListener listerner ){
        mMarkerAreaClickListener = listerner;
    }

    /*
     * ブロック削除クリックインターフェース
     */
    public interface RemoveBlockClickListener {
        // マーカーエリアクリックリスナー
        void onRemoveBlockClick( TestBlock markedBlock );
    }

    /*
     * マーカー処理ブロック下への移動アイコンクリックインターフェース
     */
    public interface MoveBelowMarkerClickListener {
        // マーカー処理ブロック下部への移動アイコンクリックリスナー
        void onMoveBelowMarkerClick( TestBlock markedBlock );
    }

    /*
     * マーカーエリアクリックインターフェース
     */
    public interface BottomMarkerAreaClickListener {
        // マーカーエリアクリックリスナー
        void onBottomMarkerAreaClick( TestBlock markedBlock );
    }


}
