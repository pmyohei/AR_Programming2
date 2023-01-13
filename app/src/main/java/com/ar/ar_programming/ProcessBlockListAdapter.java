package com.ar.ar_programming;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.ar.ar_programming.process.IfElseIfElseProcessBlock;
import com.ar.ar_programming.process.IfElseProcessBlock;
import com.ar.ar_programming.process.IfProcessBlock;
import com.ar.ar_programming.process.LoopProcessBlock;
import com.ar.ar_programming.process.ProcessBlock;
import com.ar.ar_programming.process.SingleProcessBlock;

/*
 * 処理ブロックリストアダプタ
 */
public class ProcessBlockListAdapter extends RecyclerView.Adapter<ProcessBlockListAdapter.ProcessBlockViewHolder> {

    //---------------------------
    // 定数
    //----------------------------
    // 選択した処理ブロック種別
    // !!!! 処理ブロックの並びと合わせること !!!!
    public static final int SELECT_PROCESS_FORWARD = 0;
    public static final int SELECT_PROCESS_BACK = 1;
    public static final int SELECT_PROCESS_ROTATE_LEFT = 2;
    public static final int SELECT_PROCESS_ROTATE_RIGHT = 3;
    public static final int SELECT_PROCESS_LOOP = 4;
    public static final int SELECT_PROCESS_IF = 5;
    public static final int SELECT_PROCESS_IF_ELSE = 6;
    public static final int SELECT_PROCESS_IF_ELSEIF_ELSE = 7;

    //---------------------------
    // フィールド変数
    //----------------------------
    // 処理ブロックリストリスト
    private final TypedArray mProcessBlockImageList;
    private final TypedArray mProcessBlockTitleList;
    // クリックリスナー
    private ProcessBlockClickListener mProcessBlockClickListener;

    /*
     * 処理ブロック
     */
    class ProcessBlockViewHolder extends RecyclerView.ViewHolder {

        private final ConstraintLayout cl_parent;
        private final ImageView iv_blockImage;
        private final TextView tv_title;
        private final int mSelectProcessType;
        private final int mSelectProcessContents;

        public ProcessBlockViewHolder(View itemView, int position) {
            super(itemView);

            cl_parent = itemView.findViewById( R.id.cl_parent);
            iv_blockImage = itemView.findViewById( R.id.iv_remove);
            tv_title = itemView.findViewById( R.id.tv_title);

            // 処理ブロック識別値
            mSelectProcessType = getProcessTypeAtPosition(position);
            mSelectProcessContents = getProcessContentsAtPosition(position);
        }

        /*
         * ビューの設定
         */
        public void setView( int position ){
            // 処理ブロックの設定
            Drawable image = mProcessBlockImageList.getDrawable( position );
            String title = mProcessBlockTitleList.getString( position );
            iv_blockImage.setImageDrawable( image );
            tv_title.setText( title );

            // 処理ブロッククリックリスナー
            cl_parent.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         mProcessBlockClickListener.onBlockClick(mSelectProcessType, mSelectProcessContents);
                     }
                 }
            );
        }
    }

    /*
     * コンストラクタ
     */
    public ProcessBlockListAdapter(TypedArray images, TypedArray titles ) {
        mProcessBlockImageList = images;
        mProcessBlockTitleList = titles;
    }

    /*
     * ここの戻り値が、onCreateViewHolder()の第２引数になる
     */
    @Override
    public int getItemViewType(int position) {
        //positionをそのまま返すとアイテム削除時にちらつくため、pidで管理
        return position;
    }

    @Override
    public long getItemId(int position) {
        //positionをそのまま返すとアイテム削除時にちらつくため、pidで管理
        return position;
    }

    /*
     *　ViewHolderの生成
     */
    @NonNull
    @Override
    public ProcessBlockViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {

        // 1データあたりのレイアウトを生成
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.process_block_list_item, viewGroup, false);

        return new ProcessBlockViewHolder(view, position);
    }

    /*
     * ViewHolderの設定
     *  ページ毎の表示設定
     */
    @Override
    public void onBindViewHolder(@NonNull ProcessBlockViewHolder viewHolder, final int i) {
        // ビューの設定
        viewHolder.setView( i );
    }

    /*
     * データ数取得
     */
    @Override
    public int getItemCount() {
        // 表示データ数を返す
        return mProcessBlockImageList.length();
    }

    /*
     * 処理ブロッククリックリスナーの設定
     */
    public void setOnProcessBlockClickListener(ProcessBlockClickListener listener ) {
        mProcessBlockClickListener = listener;
    }

    /*
     * 処理ブロックの位置に対応する処理ブロック種別を取得する
     */
    private int getProcessTypeAtPosition(int position ) {

        // 位置に応じた処理種別
        switch ( position ){
            case SELECT_PROCESS_FORWARD:
            case SELECT_PROCESS_BACK:
            case SELECT_PROCESS_ROTATE_LEFT:
            case SELECT_PROCESS_ROTATE_RIGHT:
                return ProcessBlock.PROCESS_TYPE_SINGLE;
            case SELECT_PROCESS_LOOP:
                return ProcessBlock.PROCESS_TYPE_LOOP;
            case SELECT_PROCESS_IF:
                return ProcessBlock.PROCESS_TYPE_IF;
            case SELECT_PROCESS_IF_ELSE:
                return ProcessBlock.PROCESS_TYPE_IF_ELSE;
            case SELECT_PROCESS_IF_ELSEIF_ELSE:
                return ProcessBlock.PROCESS_TYPE_IF_ELSEIF_ELSE;
            default:
                return ProcessBlock.PROCESS_TYPE_SINGLE;
        }
    }

    /*
     * 処理ブロックの位置に対応する処理ブロック内容を取得する
     */
    private int getProcessContentsAtPosition(int position ) {

        // 位置に応じた処理内容
        switch ( position ){
            case SELECT_PROCESS_FORWARD:
                return SingleProcessBlock.PROCESS_CONTENTS_FORWARD;
            case SELECT_PROCESS_BACK:
                return SingleProcessBlock.PROCESS_CONTENTS_BACK;
            case SELECT_PROCESS_ROTATE_LEFT:
                return SingleProcessBlock.PROCESS_CONTENTS_LEFT_ROTATE;
            case SELECT_PROCESS_ROTATE_RIGHT:
                return SingleProcessBlock.PROCESS_CONTENTS_RIGHT_ROTATE;
            case SELECT_PROCESS_LOOP:
                return LoopProcessBlock.PROCESS_CONTENTS_LOOP_FACING_GOAL;
            case SELECT_PROCESS_IF:
                return IfProcessBlock.PROCESS_CONTENTS_IF_BLOCK;
            case SELECT_PROCESS_IF_ELSE:
                return IfElseProcessBlock.PROCESS_CONTENTS_IF_ELSE_BLOCK;
            case SELECT_PROCESS_IF_ELSEIF_ELSE:
                return IfElseIfElseProcessBlock.PROCESS_CONTENTS_IF_ELSEIF_ELSE_BLOCK;
            default:
                return SingleProcessBlock.PROCESS_CONTENTS_FORWARD;
        }
    }


    /*
     * 処理結果通知用のインターフェース
     */
    public interface ProcessBlockClickListener {
        // 処理ブロッククリックリスナー
        void onBlockClick(int selectProcessType, int selectProcessContents );
    }
}
