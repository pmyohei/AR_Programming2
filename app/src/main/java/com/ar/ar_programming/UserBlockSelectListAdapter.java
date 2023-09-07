package com.ar.ar_programming;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/*
 * ユーザーブロック選択リストアダプタ
 */
public class UserBlockSelectListAdapter extends RecyclerView.Adapter<UserBlockSelectListAdapter.ProcessBlockViewHolder> {

    //---------------------------
    // 定数
    //----------------------------

    //---------------------------
    // フィールド変数
    //----------------------------
    // 処理ブロックリストリスト
    private final ArrayList<Gimmick.XmlBlockInfo> mXmlBlockInfo;
    // クリックリスナー
    private BlockClickListener mBlockClickListener;


    /*
     * 処理ブロック
     */
    class ProcessBlockViewHolder extends RecyclerView.ViewHolder {

        private final ConstraintLayout cl_parent;
        private final ImageView iv_blockImage;
        private final TextView tv_title;

        public ProcessBlockViewHolder(View itemView, int position) {
            super(itemView);

            cl_parent = itemView.findViewById(R.id.cl_parent);
            iv_blockImage = itemView.findViewById(R.id.iv_blockImage);
            tv_title = itemView.findViewById(R.id.tv_title);
        }

        /*
         * ビューの設定
         */
        public void setView(int position) {

            Resources resources = cl_parent.getContext().getResources();

            // ブロック情報
            final Gimmick.XmlBlockInfo xmlBlockInfo = mXmlBlockInfo.get(position);

            // リストitemviewの設定
            Drawable image = resources.getDrawable(xmlBlockInfo.drawableId, null);
            String description = resources.getString(xmlBlockInfo.stringId);
            iv_blockImage.setImageDrawable(image);
            tv_title.setText(description);

            // ブロッククリックリスナー
            cl_parent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i("ブロックxml", "xmlBlockInfo.type=" + xmlBlockInfo.type);
                        Log.i("ブロックxml", "xmlBlockInfo.contents=" + xmlBlockInfo.contents);
                        mBlockClickListener.onBlockClick(xmlBlockInfo);
                    }
                }
            );
        }
    }

    /*
     * コンストラクタ
     */
    public UserBlockSelectListAdapter( ArrayList<Gimmick.XmlBlockInfo> blockList ) {
        mXmlBlockInfo = new ArrayList<>();
        mXmlBlockInfo.addAll( blockList );
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
        // ブロック数を返す
        return mXmlBlockInfo.size();
    }

    /*
     * 処理ブロックリストクリア
     */
    public void clearBlockList() {
        mXmlBlockInfo.clear();
    }

    /*
     * 処理ブロッククリックリスナーの設定
     */
    public void setOnBlockClickListener(BlockClickListener listener ) {
        mBlockClickListener = listener;
    }

    /*
     * 処理結果通知用のインターフェース
     */
    public interface BlockClickListener {
        // 処理ブロッククリックリスナー
        void onBlockClick( Gimmick.XmlBlockInfo xmlBlockInfo );
    }
}
