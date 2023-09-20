package com.ar.ar_programming;

import static com.ar.ar_programming.Gimmick.NO_USABLE_LIMIT_NUM;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
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

        // 使用可能数０向けデザイン
        private final int FRAME_NOT_USE = R.drawable.frame_block_not_use;

        // レイアウト内View
        private final ConstraintLayout cl_parent;
        private final ImageView iv_blockImage;
        private final TextView tv_title;
        private final TextView tv_usableNum;

        public ProcessBlockViewHolder(View itemView, int position) {
            super(itemView);

            cl_parent = itemView.findViewById(R.id.cl_parent);
            iv_blockImage = itemView.findViewById(R.id.iv_blockImage);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_usableNum = itemView.findViewById(R.id.tv_usableNum);
        }

        /*
         * ビューの設定
         */
        public void setView(int position) {

            Context context = cl_parent.getContext();

            // ブロック情報
            final Gimmick.XmlBlockInfo xmlBlockInfo = mXmlBlockInfo.get(position);

            //-----------------------------
            // リストitem view レイアウト設定
            // ----------------------------
            // 背景色
            setBlockBackground( cl_parent, xmlBlockInfo.type, xmlBlockInfo.usableNum );

            // アイコン／アクション文
            Drawable image = GimmickManager.getBlockIcon(context, xmlBlockInfo.type, xmlBlockInfo.action);
            String statement = GimmickManager.getBlockStatement(context, xmlBlockInfo.type, xmlBlockInfo.action, xmlBlockInfo.targetNode_1);

            iv_blockImage.setImageDrawable(image);
            tv_title.setText(statement);

            // 使用可能数の設定
            if (xmlBlockInfo.usableLimitNum != NO_USABLE_LIMIT_NUM) {
                String usableNum = Integer.toString(xmlBlockInfo.usableNum);
                tv_usableNum.setText(usableNum);
            }

            // ブロッククリックリスナー
            cl_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //-----------------
                    // 使用可能数
                    //-----------------
                    ViewGroup cl_parent = (ViewGroup) view;
                    TextView tv_usableNum = cl_parent.findViewById(R.id.tv_usableNum);
                    String usableNumStr = tv_usableNum.getText().toString();
                    if ( !usableNumStr.equals("-") ) { //!リソースと一致させたい

                        //------------------------------
                        // 選択時点の使用可能数に応じた処理
                        //------------------------------
                        int usableNum = Integer.parseInt(usableNumStr);
                        if( usableNum == 0 ){
                            // 使用可能数が0になっているなら、ブロック追加なし
                            return;

                        } else if ( usableNum == 1 ){

                            // 今回の選択で使用可能数が0になるなら、レイアウトを変更
                            @SuppressLint("UseCompatLoadingForDrawables")
                            Drawable notUseDesign = context.getDrawable( FRAME_NOT_USE );
                            cl_parent.setBackground(notUseDesign);
                        }

                        //--------------
                        // 使用可能数変更
                        //--------------
                        // 使用可能数を減算
                        usableNum--;
                        tv_usableNum.setText(Integer.toString(usableNum));

                        // 元データに反映
                        xmlBlockInfo.usableNum = usableNum;
                    }

                    //-----------------
                    // ブロック追加処理
                    //-----------------
                    mBlockClickListener.onBlockClick(xmlBlockInfo);
                }
            });
        }

        /*
         * ブロック背景色の設定
         */
        private void setBlockBackground( ViewGroup parent, String type, int usableNum) {

            Context context = parent.getContext();

            //---------------------
            // 使用可能数０向けデザイン
            //---------------------
            if( usableNum == 0 ){
                @SuppressLint("UseCompatLoadingForDrawables")
                Drawable design = context.getDrawable( FRAME_NOT_USE );
                parent.setBackground( design );

                return;
            }

            //---------------------
            // ブロック種別デザイン
            //---------------------
            int drawableId;
            switch ( type ){
                case GimmickManager.BLOCK_TYPE_EXE:
                    drawableId = R.drawable.frame_block_exe;
                    break;

                case GimmickManager.BLOCK_TYPE_LOOP:
                    drawableId = R.drawable.frame_block_loop;
                    break;

                case GimmickManager.BLOCK_TYPE_IF:
                case GimmickManager.BLOCK_TYPE_IF_ELSE:
                case GimmickManager.BLOCK_TYPE_IE_ELSEIF:
                    drawableId = R.drawable.frame_block_if;
                    break;

                default:
                    drawableId = R.drawable.frame_block_exe;
                    break;
            }

            // 背景色の設定
            @SuppressLint("UseCompatLoadingForDrawables")
            Drawable design = context.getDrawable( drawableId );
            parent.setBackground( design );
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
        View view = inflater.inflate(R.layout.block_choices_item, viewGroup, false);

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
