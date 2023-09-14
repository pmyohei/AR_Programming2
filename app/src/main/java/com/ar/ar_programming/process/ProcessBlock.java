package com.ar.ar_programming.process;

import static com.ar.ar_programming.ArMainFragment.PROGRAMMING_FAILURE;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.ar.ar_programming.character.CharacterNode;
import com.ar.ar_programming.Gimmick;
import com.ar.ar_programming.R;


/*
 * 処理ブロック
 */
public abstract class ProcessBlock extends Block {

    //---------------------------
    // 定数
    //---------------------------

    //---------------------------
    // フィールド変数
    //---------------------------
//    public int mProcessContents;
    public Gimmick.XmlBlockInfo mXmlBlockInfo;
    private boolean mDragFlg;
    private ProgrammingListener mProgrammingListener;


    public ProcessBlock(Context context) {
        this(context, null);
    }
    public ProcessBlock(Context context, AttributeSet attrs) {
        this(context, attrs, 0, null);
    }
    public ProcessBlock(Context context, AttributeSet attrs, int defStyle, Gimmick.XmlBlockInfo xmlBlockInfo) {
        super(context, attrs, defStyle, xmlBlockInfo);
        mXmlBlockInfo = xmlBlockInfo;
        setId(View.generateViewId());

        Log.i("置換", "コール？");
    }

    /*
     * 処理ブロックの内容を書き換え
     */
    public void rewriteProcessContents(){

        //------------------
        // ブロック文
        //------------------
        // 文言IDをレイアウトに設定
        TextView tv_contents = findViewById(R.id.tv_contents);
        tv_contents.setText( mXmlBlockInfo.statementId);

        //------------------
        // ブロック内ワード
        //------------------
        String contentsStr = tv_contents.getText().toString();
        Log.i("ワード置き換え", "contentsStr=" + contentsStr);
        Log.i("ワード置き換え", "mXmlBlockInfo.nodeNameId=" + mXmlBlockInfo.nodeNameId);
        String contentsWithNodeName = replaceNodeName( getContext(), contentsStr, mXmlBlockInfo.nodeNameId);
        Log.i("ワード置き換え", "contentsWithNodeName=" + contentsWithNodeName);
        if( contentsWithNodeName != null ){
            tv_contents.setText( contentsWithNodeName );
        }
    }

    /*
     * 処理ブロック内容取得
     */
    public String getProcessContents() {
        return mXmlBlockInfo.contents;
    }

    /*
     * レイアウト最上位ビューIDを取得
     */
    @Override
    public View getLayoutRootView() {
        return findViewById(R.id.ll_root);
    }

    /*
     * マークエリアビューIDを取得
     */
    @Override
    public int getMarkAreaViewID() {
        return R.id.cl_markArea;
    }

    /*
     * マークエリアのマークイメージIDを取得
     */
    @Override
    public int getMarkImageViewID() {
        return R.id.iv_mark;
    }

    /*
     * ドロップラインビューIDを取得
     */
    @Override
    public int getDropLineViewID() {
        Log.i("ドロップリスナー", "getDropLineViewID Process側取得");
        return R.id.v_dropLine;
    }

    /*
     * タッチリスナー設定
     *   本ブロックがタッチされたとき、ドラッグ移動可能にする
     */
    public void setBlockTouchListerer() {

        // 本ブロック
        Block selfBlock = this;

        getLayoutRootView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // フラグon
                        mDragFlg = true;
                        break;

                    case MotionEvent.ACTION_MOVE:

                        // フラグon の場合
                        if (mDragFlg) {
                            //--------------------------
                            // 本処理ブロックを半透明化
                            //--------------------------
                            tranceOnDrag();

                            //--------------------------
                            // ドラッグ開始
                            //--------------------------
                            // 本ブロックをドラッグ中のビューとする
                            View.DragShadowBuilder myShadow = new View.DragShadowBuilder(view);
                            view.startDragAndDrop(null, myShadow, selfBlock, 0);

                            // フラグoff
                            mDragFlg = false;
                        }

                        break;

                    case MotionEvent.ACTION_UP:
                        // do nothing
                        break;
                }
                return true;
            }
        });
    }

    /*
     * 処理開始
     */
    public abstract void startProcess(CharacterNode characterNode);

    /*
     * 次の処理ブロック遷移処理
     */
    public boolean isBottomBlock() {

        if (hasBelowBlock() || inNest()) {
            return false;
        }

        return true;
    }

    /*
     * 次の処理ブロック遷移処理
     */
    public void tranceNextBlock(CharacterNode characterNode) {

        //------------------
        // 下ブロックチェック
        //------------------
        if (hasBelowBlock()) {
            // 下ブロックがあれば、そのブロックの処理を開始
            ProcessBlock nextBlock = (ProcessBlock) getBelowBlock();
            nextBlock.startProcess(characterNode);
            return;
        }

        //--------------------------
        // 下ブロックなし。親ネスト判定
        //--------------------------
        // 本ブロックがネスト内にあり、最後の処理であった場合
        if (inNest()) {
            NestBlock parentNest = getOwnNestBlock();

            if (parentNest.getProcessType() == PROCESS_TYPE_LOOP) {
                // ループの場合は、開始処理から
                parentNest.startProcess(characterNode);
            } else {
                // 条件ブロックの場合は、条件ブロックの下ブロックから
                parentNest.tranceNextBlock(characterNode);
            }

            return;
        }

        //--------------------------
        // 下ブロックなし／親ネストなし
        //--------------------------
        // 終了リスナーをコール
        int resultProgramming = characterNode.isCompleteSuccessCondition();
        mProgrammingListener.onProgrammingEnd( resultProgramming );
    }

    /*
     * ブロック処理リスナーの設定
     */
    public void setProgrammingListener(ProgrammingListener listener ) {
        mProgrammingListener = listener;
    }

    /*
     * ブロック処理リスナーの設定
     */
    public void end( int programmingEndState ) {
        mProgrammingListener.onProgrammingEnd( programmingEndState );
    }


    /*
     *
     */
    public static String replaceNodeName( Context context, String contentsStr, int nodeNameStringId ) {

        //------------
        // 置換可能判定
        //------------
        // ブロック文に置換文字列があり、かつ、置換先文字列を保持している場合
        if( contentsStr.contains( "xxx" ) && nodeNameStringId != -1 ){
            // ブロック文にNode名を埋め込み
            String nodeName = context.getString( nodeNameStringId );
            return contentsStr.replace( "xxx", nodeName );
        }

        // 置換不可
        return null;
    }

    /*
     * ブロック処理リスナー
     */
    public interface ProgrammingListener {
        void onProgrammingEnd( int programmingEndState );
    }
}
