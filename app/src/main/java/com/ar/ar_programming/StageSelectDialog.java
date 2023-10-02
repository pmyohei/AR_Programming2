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
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/*
 * ステージ選択ダイアログ
 */
public class StageSelectDialog extends DialogFragment {

    // リスナー
    private View.OnClickListener mPlayClickListener;

    /*
     * ステージリスト情報
     */
    public static class StageList {
        public String mStageName;
        public boolean mIsClear;

        public StageList( String stageName ) {
            mStageName = stageName;
        }
    }



    //空のコンストラクタ
    //※必須（画面回転等の画面再生成時にコールされる）
    public StageSelectDialog() {
        //do nothing
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_stage_select, container, false);
    }

    /**
     * The system calls this only when creating the layout in a dialog.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        //背景を透明にする(デフォルトテーマに付いている影などを消す) ※これをしないと、画面横サイズまで拡張されない
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

        // ダイアログサイズ設定
        setupDialogSize(dialog);
        // リスナー設定
        setListerner(dialog);
        // ステージ選択リスト設定
        setStageList(dialog);
    }

    /*
     * ダイアログサイズ設定
     */
    private void setupDialogSize(Dialog dialog) {

        //-------------------
        // スマホ画面に対する割合
        //-------------------
        final float PORTRAIT_RATIO = 0.8f;  //縦画面時
        final float LANDSCAPE_RATIO = 0.5f; //横画面時

        //-------------------
        // サイズ設定
        //-------------------
        // 画面向きを取得
        int orientation = getResources().getConfiguration().orientation;
        float widthRatio = ((orientation == Configuration.ORIENTATION_PORTRAIT) ? PORTRAIT_RATIO : LANDSCAPE_RATIO);

        // 画面サイズの取得
        int screeenWidth = getScreenWidth(getContext());
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.width = (int) (screeenWidth * widthRatio);

        // サイズ反映
        window.setAttributes(lp);

        //--------------
        // 可視情報
        //--------------
        // ダイアログ背景の暗転を無効化
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    /*
     * リスナー設定
     */
    private void setListerner(Dialog dialog) {
        TextView tv_play = dialog.findViewById(R.id.tv_play);

        //-----------------
        // Play
        //-----------------
//        tv_play.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mPlayClickListener.onClick(view);
//                dismiss();
//            }
//        });
    }


    /*
     * ステージリスト設定
     */
    private void setStageList(Dialog dialog) {

        Context context = getContext();

        //---------------------
        // ステージリスト情報
        //---------------------
        // ステージ名リストの取得
        ArrayList<String> stageNameList = GimmickManager.getStageNameList( context );

        // ステージリストにステージ名を設定
        ArrayList<StageList> stageList = new ArrayList<>();
        for( String name: stageNameList ){
            stageList.add( new StageList( name ) );
        }

        // ユーザーのクリア情報を設定
        Common.setUserStageClearInfo( context, stageList );

        //---------------------
        // アダプタ設定
        //---------------------
        // ステージ選択リストアダプタの生成
        StageSelectListAdapter adapter = new StageSelectListAdapter( stageList );

        // スクロール方向を用意
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager( context );
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        // アダプタ設定
        RecyclerView rv_selectStage = dialog.findViewById(R.id.rv_selectStage);
        rv_selectStage.setAdapter(adapter);
        rv_selectStage.setLayoutManager(linearLayoutManager);

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
     * Playリスナー設定
     */
    public void setOnPlayListerner(View.OnClickListener listerner) {
        mPlayClickListener = listerner;
    }
}
