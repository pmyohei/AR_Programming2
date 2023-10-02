package com.ar.ar_programming;

import static com.ar.ar_programming.Gimmick.NO_USABLE_LIMIT_NUM;
import static com.ar.ar_programming.Gimmick.VOLUME_LIMIT_NONE;
import static com.ar.ar_programming.GimmickManager.BLOCK_EXE_BACK;
import static com.ar.ar_programming.GimmickManager.BLOCK_EXE_FORWARD;
import static com.ar.ar_programming.GimmickManager.BLOCK_EXE_ROTATE_LEFT;
import static com.ar.ar_programming.GimmickManager.BLOCK_EXE_ROTATE_RIGHT;
import static com.ar.ar_programming.GimmickManager.BLOCK_TYPE_EXE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/*
 * ユーザーブロック選択リストアダプタ
 */
public class StageSelectListAdapter extends RecyclerView.Adapter<StageSelectListAdapter.StageItemViewHolder> {

    //---------------------------
    // 定数
    //----------------------------

    //---------------------------
    // フィールド変数
    //----------------------------
    // ステージリスト
    private final ArrayList<StageSelectDialog.StageList> mStageList;
    // クリックリスナー
    private BlockClickListener mBlockClickListener;
    // 選択中位置
    private int mCheckPosition = -1;

    /*
     * 選択ステージ
     */
    class StageItemViewHolder extends RecyclerView.ViewHolder {

        // レイアウト内View
        private final ConstraintLayout cl_parent;
        private final RadioButton rb_stage;
        private final ImageView iv_clear;

        public StageItemViewHolder(View itemView, int position) {
            super(itemView);

            cl_parent = itemView.findViewById(R.id.cl_parent);
            rb_stage = itemView.findViewById(R.id.rb_stage);
            iv_clear = itemView.findViewById(R.id.iv_clear);
        }

        /*
         * ビューの設定
         */
        public void setView(int position) {

            Context context = cl_parent.getContext();

            // ステージ情報
            final StageSelectDialog.StageList stageList = mStageList.get(position);

            //-----------------------------
            // リストitem view レイアウト設定
            // ----------------------------
            // ステージ名を設定
            setStageName(context, stageList.mStageName);
            // 選択中状態の設定
            setSelectedState(position);
            // クリア状態に応じたアイコンの設定
            setClearInfo(stageList.mIsClear);

            // ステージクリックリスナー
            rb_stage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // 同じステージが選択
                    if( mCheckPosition == position ){
                        // 処理なし
                        return;
                    }

                    //-------------------
                    // 選択中ステージ変更
                    //-------------------
                    int preChecked = mCheckPosition;
                    mCheckPosition = position;

                    // 選択中ステージの選択を解除
                    notifyItemChanged( preChecked );

                    // クリック対象のステージを選択中に変更
                    rb_stage.setChecked( true );
                    notifyItemChanged( position );

                }
            });
        }

        /*
         * ステージ名の設定
         */
        private void setStageName(Context context, String stageName) {

            // StringIDを取得（R.string.xxx　を生成）
            int stringId = context.getResources().getIdentifier(stageName, "string", context.getPackageName());
            String name = context.getString( stringId );
            // ステージ名として設定
            rb_stage.setText( name );
        }

        /*
         * 選択中状態の設定
         */
        private void setSelectedState(int position) {
            // ステージ名として設定
            rb_stage.setChecked( (position == mCheckPosition) );
        }

        /*
         * ステージクリア状態の設定
         */
        private void setClearInfo(boolean isClear) {

            // クリアしていないなら、何もしない（デフォルトのまま）
            if( !isClear ){
                return;
            }

            Context context = iv_clear.getContext();

            // アイコンをクリア状態に変更
            @SuppressLint("UseCompatLoadingForDrawables")
            Drawable design = context.getDrawable( R.drawable.baseline_trophy_24 );
            iv_clear.setBackground(design);
        }

    }

    /*
     * コンストラクタ
     */
    public StageSelectListAdapter( ArrayList<StageSelectDialog.StageList> stageList ) {
        mStageList = stageList;
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
    public StageItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {

        //-------------------
        // レイアウト生成
        //-------------------
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.stage_select_item, viewGroup, false);

        return new StageItemViewHolder(view, position);
    }

    /*
     * ViewHolderの設定
     *  ページ毎の表示設定
     */
    @Override
    public void onBindViewHolder(@NonNull StageItemViewHolder viewHolder, final int i) {
        // ビューの設定
        viewHolder.setView( i );
    }

    /*
     * データ数取得
     */
    @Override
    public int getItemCount() {
        // ステージ数を返す
        return mStageList.size();
    }

    /*
     * 処理ブロックリストクリア
     */
    public void clearBlockList() {
        mStageList.clear();
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
