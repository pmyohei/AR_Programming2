package com.ar.ar_programming;

import static android.content.Context.WINDOW_SERVICE;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowMetrics;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class HelpDialog extends DialogFragment {

    // 空のコンストラクタ
    // ※必須（画面回転等の画面再生成時にコールされる）
    public HelpDialog(){
        // do nothing
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.how_to_play_dialog, container, false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // 背景を透明にする(デフォルトテーマに付いている影などを消す) ※これをしないと、画面横サイズまで拡張されない
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog == null) {
            return;
        }

        //  ダイアログサイズ設定
        setupDialogSize(dialog);

        // ページ設定
        setupHelpPage();
    }

    /*
     * ダイアログサイズ設定
     */
    private void setupDialogSize(Dialog dialog) {

        // -------------------
        //  スマホ画面に対する割合
        // -------------------
        final float PORTRAIT_RATIO = 0.8f;  // 縦画面時
        final float LANDSCAPE_RATIO = 0.5f; // 横画面時

        // -------------------
        //  サイズ設定
        // -------------------
        //  画面向きを取得
        int orientation = getResources().getConfiguration().orientation;
        float widthRatio = ((orientation == Configuration.ORIENTATION_PORTRAIT) ? PORTRAIT_RATIO : LANDSCAPE_RATIO);

        //  画面サイズの取得
        int screeenWidth = getScreenWidth(getContext());
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.width = (int) (screeenWidth * widthRatio);

        //  サイズ反映
        window.setAttributes(lp);

        // --------------
        //  可視情報
        // --------------
        //  ダイアログ背景の暗転を無効化
        window.clearFlags( WindowManager.LayoutParams.FLAG_DIM_BEHIND );
    }

    /*
     *　スクリーン横幅を取得
     */
    private int getScreenWidth(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
            WindowMetrics windowMetrics = windowManager.getCurrentWindowMetrics();
            return windowMetrics.getBounds().width();
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
            return displayMetrics.widthPixels;
        }
    }

    /*
     * ヘルプページレイアウトを設定
     */
    private void setupHelpPage() {

        // ヘルプページリスト
        List<Integer> layoutIdList = new ArrayList<>();
        layoutIdList.add(R.layout.help_page_how_to_add_block);
        layoutIdList.add(R.layout.help_page_how_to_move_block);
        layoutIdList.add(R.layout.help_page_how_to_remove_block);
        layoutIdList.add(R.layout.help_page_how_to_set_volume);

        // ViewPager2にアダプタを割り当て
        Dialog dialog = getDialog();
        ViewPager2 vp2_help = dialog.findViewById(R.id.vp2_help);
        vp2_help.setAdapter( new HelpPageAdapter(layoutIdList) );

        // インジケータの設定
        TabLayout tabLayout = dialog.findViewById(R.id.tab_help);
        new TabLayoutMediator(tabLayout, vp2_help,
                (tab, position) -> tab.setText("")
        ).attach();
    }

    /*
     * ヘルプページ用アダプタ
     */
    public class HelpPageAdapter extends RecyclerView.Adapter<HelpPageAdapter.ViewHolder> {

        // ページレイアウト
        private final List<Integer> mPageList;

        /*
         * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
         */
        class ViewHolder extends RecyclerView.ViewHolder {

            public ViewHolder(View itemView ) {
                super(itemView);
            }
        }

        /*
         * コンストラクタ
         */
        public HelpPageAdapter(List<Integer> layoutIdList) {
            mPageList = layoutIdList;
        }

        /*
         * ここで返した値が、onCreateViewHolder()の第２引数になる
         */
        @Override
        public int getItemViewType(int position) {
            // レイアウトIDを返す
            return position;
        }

        /*
         *　ViewHolderの生成
         */
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {

            // レイアウトを生成
            LayoutInflater inflater = LayoutInflater.from( viewGroup.getContext() );
            View view = inflater.inflate(mPageList.get(position), viewGroup, false);

            return new ViewHolder(view);
        }

        /*
         * ViewHolderの設定
         */
        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        }

        /*
         * 表示データ数の取得
         */
        @Override
        public int getItemCount() {
            // ページ数
            return mPageList.size();
        }
    }
}
