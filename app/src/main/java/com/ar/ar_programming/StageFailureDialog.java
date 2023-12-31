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

/*
 * ステージクリア失敗時のダイアログ
 */
public class StageFailureDialog extends DialogFragment {

    // リスナー
    private View.OnClickListener mRetryClickListener;
    private View.OnClickListener mBreakClickListener;
    private View.OnClickListener mOtherStageClickListener;


    //空のコンストラクタ
    //※必須（画面回転等の画面再生成時にコールされる）
    public StageFailureDialog() {
        //do nothing
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_failure, container, false);
    }

    /**
     * The system calls this only when creating the layout in a dialog.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        //背景を透明にする(デフォルトテーマに付いている影などを消す) ※これをしないと、画面横サイズまで拡張されない
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // ダイアログ表示時、背景を暗くさせない
//        dialog.getWindow().clearFlags( WindowManager.LayoutParams.FLAG_DIM_BEHIND );

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

        // チュートリアル中の場合
        boolean finishTutorial = Common.isCompleteTutorial( getContext() );
        if( !finishTutorial ){
            dialog.findViewById(R.id.tv_otherStage).setVisibility( View.GONE );
        }
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
        window.clearFlags( WindowManager.LayoutParams.FLAG_DIM_BEHIND );
    }

    /*
     * リスナー設定
     */
    private void setListerner(Dialog dialog) {
        TextView tv_retry = dialog.findViewById(R.id.tv_retry);
        TextView tv_break = dialog.findViewById(R.id.tv_break);
        TextView tv_otherStage = dialog.findViewById(R.id.tv_otherStage);

        //-----------------
        // リトライ
        //-----------------
        tv_retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRetryClickListener.onClick(view);
                dismiss();
            }
        });

        //-----------------
        // 休憩
        //-----------------
        tv_break.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBreakClickListener.onClick(view);
                dismiss();
            }
        });

        //-----------------
        // 別ステージ選択
        //-----------------
        tv_otherStage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOtherStageClickListener.onClick(view);
                dismiss();
            }
        });
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
     * リトライリスナー設定
     */
    public void setOnRetryListerner(View.OnClickListener listerner) {
        mRetryClickListener = listerner;
    }
    /*
     * 休憩リスナー設定
     */
    public void setOnBreakListerner(View.OnClickListener listerner) {
        mBreakClickListener = listerner;
    }
    /*
     * 別ステージ選択リスナー設定
     */
    public void setOnOtherStageListerner(View.OnClickListener listerner) {
        mOtherStageClickListener = listerner;
    }
}
