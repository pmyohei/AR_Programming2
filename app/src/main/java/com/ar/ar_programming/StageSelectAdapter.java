package com.ar.ar_programming;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/*
 * ユーザーブロック選択リストアダプタ
 */
public class StageSelectAdapter extends RecyclerView.Adapter<StageSelectAdapter.StageItemViewHolder> {

    //---------------------------
    // 定数
    //----------------------------

    //---------------------------
    // フィールド変数
    //----------------------------
    // ステージリスト
    private final ArrayList<StageList> mStageList;
    // 現在の（アダプタ生成時点の）ステージ名
    private final String CurrrentStageName;
    // 選択中位置
    private int mSelectPosition;

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
            final StageList stageList = mStageList.get(position);

            //-----------------------------
            // リストitem view レイアウト設定
            // ----------------------------
            // ステージ名を設定
            setStageName(context, stageList.mStageName);
            // 選択中状態の設定
            setSelectedState(stageList, position);
            // クリア状態に応じたアイコンの設定
            setClearInfo(stageList.mIsClear);

            // ステージクリックリスナー
            rb_stage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // 同じステージが選択
                    if( mSelectPosition == position ){
                        // 処理なし
                        return;
                    }

                    //-------------------
                    // 選択中ステージ変更
                    //-------------------
                    int preChecked = mSelectPosition;
                    mSelectPosition = position;

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
        private void setSelectedState(StageList stageList, int position) {
            // ステージ名として設定
            boolean isSelect = (position == mSelectPosition);

            // 選択状態を反映
            rb_stage.setChecked( isSelect );
            stageList.mIsSelect = isSelect;
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
    public StageSelectAdapter(ArrayList<StageList> stageList, String currrentStageName ) {
        mStageList = stageList;
        CurrrentStageName = currrentStageName;

        //----------------
        // 選択中位置を取得
        //----------------
        mSelectPosition = getSelectedStagePosition();
    }

    /*
     * 選択中ステージの位置を取得
     */
    private int getSelectedStagePosition() {

        int position = 0;
        for( StageList stage: mStageList ){
            if( stage.mStageName.equals( CurrrentStageName ) ){
                return position;
            }
            position++;
        }

        return 0;
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
}
