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
    // ブロックリストリスト
    private final ArrayList<Gimmick.XmlBlockInfo> mXmlBlockInfo;
    // 選択肢ブロッククリックリスナー
    private BlockClickListener mBlockClickListener;
    // ブロック追加の可不可
    private boolean mCanSelectBlock;

    /*
     * ブロック
     */
    class ProcessBlockViewHolder extends RecyclerView.ViewHolder {

        // 使用可能数０向けデザイン
        private final int FRAME_NOT_USE = R.drawable.frame_block_not_use;

        // レイアウト内View
        private final ConstraintLayout cl_parent;
        private final ImageView iv_blockImage;
        private final TextView tv_title;
        private final TextView tv_usableNum;
        private final TextView tv_setVolumeNum;

        public ProcessBlockViewHolder(View itemView, int position) {
            super(itemView);

            cl_parent = itemView.findViewById(R.id.cl_parent);
            iv_blockImage = itemView.findViewById(R.id.iv_blockImage);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_usableNum = itemView.findViewById(R.id.tv_usableNum);
            tv_setVolumeNum = itemView.findViewById(R.id.tv_setVolumeNum);
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
            setBlockBackground(cl_parent, xmlBlockInfo.type, xmlBlockInfo.usableNum);

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

            // 設定量の設定
            setSettingVolume(cl_parent, xmlBlockInfo);

            // ブロッククリックリスナー
            cl_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //-----------------
                    // ブロック選択可 判定
                    //-----------------
                    if( !mCanSelectBlock ){
                        return;
                    }

                    //-----------------
                    // 使用可能数
                    //-----------------
                    ViewGroup cl_parent = (ViewGroup) view;
                    TextView tv_usableNum = cl_parent.findViewById(R.id.tv_usableNum);
                    String usableNumStr = tv_usableNum.getText().toString();
                    if (!usableNumStr.equals("-")) { //!リソースと一致させたい

                        //------------------------------
                        // 選択時点の使用可能数に応じた処理
                        //------------------------------
                        int usableNum = Integer.parseInt(usableNumStr);
                        if (usableNum == 0) {
                            // 使用可能数が0になっているなら、ブロック追加なし
                            return;

                        } else if (usableNum == 1) {

                            // 今回の選択で使用可能数が0になるなら、レイアウトを変更
                            @SuppressLint("UseCompatLoadingForDrawables")
                            Drawable notUseDesign = context.getDrawable(FRAME_NOT_USE);
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
        private void setBlockBackground(ViewGroup parent, String type, int usableNum) {

            Context context = parent.getContext();

            //---------------------
            // 使用可能数０向けデザイン
            //---------------------
            if (usableNum == 0) {
                @SuppressLint("UseCompatLoadingForDrawables")
                Drawable design = context.getDrawable(FRAME_NOT_USE);
                parent.setBackground(design);

                return;
            }

            //---------------------
            // ブロック種別デザイン
            //---------------------
            int drawableId;
            switch (type) {
                case BLOCK_TYPE_EXE:
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
            Drawable design = context.getDrawable(drawableId);
            parent.setBackground(design);
        }


        /*
         * ブロック設定量の設定
         */
        private void setSettingVolume(ViewGroup parent, Gimmick.XmlBlockInfo XmlBlockInfo) {

            String type = XmlBlockInfo.type;
            String action = XmlBlockInfo.action;
            int fixVolume = XmlBlockInfo.fixVolume;

            //-----------------
            // 対応不要判定
            //-----------------
            // 実行ブロック以外は無関係
            if( !type.equals( BLOCK_TYPE_EXE ) ){
                return;
            }

            // 処理量のあるアクション出なければ、何もしない
            if( !action.equals( BLOCK_EXE_FORWARD ) &&
                !action.equals( BLOCK_EXE_BACK ) &&
                !action.equals( BLOCK_EXE_ROTATE_RIGHT ) &&
                !action.equals( BLOCK_EXE_ROTATE_LEFT )
            ) {
                return;
            }


            //-----------------
            // 設定量を設定
            //-----------------
            Context context = parent.getContext();

            // 固定処理量なし（設定値は自由）なら、自由の文字列を設定
            if( fixVolume == VOLUME_LIMIT_NONE ){
                String volume = context.getString( R.string.block_set_volume_free );
                tv_setVolumeNum.setText( volume );
                return;
            }

            // 固定処理量なし（設定値は自由）なら、固定処理量と単位を設定
            String fixStr = Integer.toString( fixVolume );

            String unit;
            if( action.equals( BLOCK_EXE_FORWARD ) || action.equals( BLOCK_EXE_BACK ) ){
                unit = context.getString( R.string.block_unit_walk );
            } else {
                unit = context.getString( R.string.block_unit_rotate );
            }

            String volume = fixStr + unit;

            tv_setVolumeNum.setText( volume );
        }
    }

    /*
     * コンストラクタ
     */
    public UserBlockSelectListAdapter( ArrayList<Gimmick.XmlBlockInfo> blockList ) {
        mXmlBlockInfo = new ArrayList<>();
        mXmlBlockInfo.addAll( blockList );

        mCanSelectBlock = true;
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

        //-------------------
        // レイアウトID
        //-------------------
        int layoutID;
        // ブロック種別に応じて選択
        String type = mXmlBlockInfo.get( position ).type;
        if( type.equals( BLOCK_TYPE_EXE ) ){
            layoutID = R.layout.block_choices_exe_item;
        } else {
            layoutID = R.layout.block_choices_nest_item;
        }

        //-------------------
        // レイアウト生成
        //-------------------
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(layoutID, viewGroup, false);

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
     * ブロック選択 可／不可
     */
    public void setCanSelectBlock(boolean enable ) {
        mCanSelectBlock = enable;
    }

    /*
     * ブロックリストクリア
     */
    public void clearBlockList() {
        mXmlBlockInfo.clear();
    }


    /*
     * ブロッククリックリスナーの設定
     */
    public void setOnBlockClickListener(BlockClickListener listener ) {
        mBlockClickListener = listener;
    }

    /*
     * 処理結果通知用のインターフェース
     */
    public interface BlockClickListener {
        // ブロッククリックリスナー
        void onBlockClick( Gimmick.XmlBlockInfo xmlBlockInfo );
    }
}
