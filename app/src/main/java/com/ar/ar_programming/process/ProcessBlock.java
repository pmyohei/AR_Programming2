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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ar.ar_programming.R;


/*
 * 処理ブロック
 * 　　「SingleProcessView」「NestProcessView」等の基本クラス
 */
public class ProcessBlock extends ConstraintLayout {

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
    public int mProcessType;
    public int mProcessKind;
    public int mProcessContent;
    public NestProcessBlock mParentNestBlock;        // 自身が入っている直上のネスト処理ブロック（ない場合はnull）


    public ProcessBlock(Context context) {
        this(context, null);
    }

    public ProcessBlock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProcessBlock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // 処理ブロック共通初期化処理
        init();
    }

    /*
     * 初期化処理
     */
    private void init() {
        // ID設定
        setId(View.generateViewId());

        // ロングクリックリスナーの設定
        setLongClickListerner();
        // onDragリスナーの設定
//        setDragAndDropListerner();

        // 親ネストブロックを初期化
        mParentNestBlock = null;
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
                        createAddImageBlock();

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
     * 処理イメージブロックの生成
     */
    private void createAddImageBlock() {

        //-------------------------------
        // 処理イメージブロックをレイアウトに追加
        //-------------------------------
        // 処理イメージブロックを生成
        View imageBlock = new View(getContext());
        imageBlock.setBackgroundColor(getResources().getColor(R.color.black_50));
        imageBlock.setTag(TAG_ADD_IMAGE_VIEW);

        // 親レイアウト or 親ネストブロックから見た時の自分の子ビューとしてのindexを取得
        int myselfIndex = getMyselfChildIndex();

        Log.i("ドラッグテスト block", "myselfIndex=" + myselfIndex);

        // 自処理ブロックサイズ
        int width = getWidth();
        int height = getHeight();

        // 親レイアウトに追加
        ViewGroup parentView = (ViewGroup) getParent();
        parentView.addView(imageBlock, myselfIndex + 1, new ViewGroup.LayoutParams(width, height));

        // アニメーション付きで生成
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.create_block);
        imageBlock.startAnimation(animation);

        //--------------------------------------
        // 処理イメージブロックを他の処理と左揃えにする
        //--------------------------------------
        // 左揃えにするために、自処理ブロックの左マージンを取得
        MarginLayoutParams parentMlp = (MarginLayoutParams) getLayoutParams();
        final int anchorLeft = parentMlp.leftMargin;

        // 処理イメージブロックに左マージンを設定
        MarginLayoutParams mlp = (MarginLayoutParams) imageBlock.getLayoutParams();
        mlp.setMargins(anchorLeft, mlp.topMargin, mlp.rightMargin, mlp.bottomMargin);
    }

    /*
     * 処理ブロックの生成
     */
    private void createProcessBlock(DragEvent dragEvent) {

        //-------------------------------
        // 処理ブロックをレイアウトに追加
        //-------------------------------
        // ドラッグされてきた処理ブロックを取得
        ProcessBlock processView = (ProcessBlock) dragEvent.getLocalState();
        // 処理ブロック生成
        createProcessBlock(processView);
    }

    /*
     * 処理ブロックの生成
     */
    public void createProcessBlock(ProcessBlock newProcessBlock) {

        //-------------------------------
        // 生成する処理ブロックの親ネスト設定
        //-------------------------------
        // ネスト内になければ、nullが設定される
        NestProcessBlock nestBlock = getParentNestBlock();
        newProcessBlock.setParentNestBlock(nestBlock);

        //-------------------------------
        // 処理ブロックをレイアウトに追加
        //-------------------------------
        // 親レイアウトから見た時の自分の子ビューindexを取得
        int myselfIndex = getMyselfChildIndex();

        // 親レイアウトに追加
        ViewGroup parentView = (ViewGroup) getParent();
        parentView.addView(newProcessBlock, myselfIndex + 1);

        // アニメーション付きで生成
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.create_block);
        newProcessBlock.startAnimation(animation);

        //--------------------------------------
        // 生成処理ブロックを他の処理ブロックと左揃えにする
        //--------------------------------------
        // 左揃えにするために、自分の左マージンを取得
        MarginLayoutParams parentMlp = (MarginLayoutParams) getLayoutParams();
        final int anchorLeft = parentMlp.leftMargin;

        // 処理ブロックに左マージンを設定
        MarginLayoutParams mlp = (MarginLayoutParams) newProcessBlock.getLayoutParams();
        mlp.setMargins(anchorLeft, mlp.topMargin, mlp.rightMargin, mlp.bottomMargin);
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
     * 親レイアウトから見た時の本ビューのchildIndexを取得
     */
    private int getMyselfChildIndex() {

        // 自分のレイアウトID
        int myID = getId();

        // 子レイアウトの数
        ViewGroup parentView = getBlockParentView();
        int childNum = parentView.getChildCount();

        Log.i("処理イメージ", "追加 childNum=" + childNum);

        // 親レイアウトの子ビュー分繰り返し
        for (int i = 0; i < childNum; i++) {
            // 自分と同じIDがあれば、その時のindexを変えす
            int checkID = parentView.getChildAt(i).getId();
            if (myID == checkID) {
                Log.i("処理イメージ", "子index=" + i);
                return i;
            }
        }

        return -1;
    }

    /*
     * 親レイアウトor親ネストを返す
     *   親ネストがあれば親ネスト、なければ、親レイアウトを返す
     */
    private ViewGroup getBlockParentView() {

        NestProcessBlock parentView = getParentNestBlock();
        if( parentView != null ){
            return parentView;
        }
        return (ViewGroup)getParent();
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
    public void cancelDraggingState( DragEvent dragEvent ){

        // 既に解除ずみなら何もしない
        View draggedView = (View) dragEvent.getLocalState();
        if( draggedView.getAlpha() >= NOT_DRAGGING_TRANCE ){
            return;
        }

        // ドラッグされたビューのドラッグ状態を解除
        draggedView.setAlpha( NOT_DRAGGING_TRANCE );
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
    public void setProcessType(int type ) {
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
    public void setProcessKind(int processKind ) {
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
     * 「親ネスト処理ブロック」の設定
     */
    public void setParentNestBlock( NestProcessBlock block ) {
        mParentNestBlock = block;
    }
    /*
     * 「親ネスト処理ブロック」の取得
     */
    public NestProcessBlock getParentNestBlock() {
        return mParentNestBlock;
    }


    /*
     * 「処理ブロック内容」の設定
     */
    public void setProcessContent(int processContent ) {
        // 処理種別の設定
        mProcessContent = processContent;
    }
}
