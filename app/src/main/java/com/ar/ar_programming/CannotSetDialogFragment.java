package com.ar.ar_programming;

import static android.content.Context.WINDOW_SERVICE;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

public class CannotSetDialogFragment extends DialogFragment {

    // ユーザーのチュートリアル状況
    // 例）この値が１の時、そのユーザーが次にやるチュートリアルが１ということ
    //    チュートリアル１が完了しているというわけではないため注意
    private int mTutorial;

    public CannotSetDialogFragment(int tutorial) {
        mTutorial = tutorial;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_cannot_set, container, false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

/*
        //背景を透明にする(デフォルトテーマに付いている影などを消す) ※これをしないと、画面横サイズまで拡張されない
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // ダイアログ表示時、背景を暗くさせない
        dialog.getWindow().clearFlags( WindowManager.LayoutParams.FLAG_DIM_BEHIND );
*/

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog == null) {
            return;
        }

        // 完了したチュートリアルをviewに反映
        setFinishTutorialInfo(dialog);


/*        // ダイアログサイズ設定
        setupDialogSize(dialog);

        // 設定文言ID
        int major = mGoalExplanationIdList.get(Gimmick.GOAl_EXP_MAJOR_POS);
        int sub = mGoalExplanationIdList.get(Gimmick.GOAl_EXP_SUB_POS);
        int contents = mGoalExplanationIdList.get(Gimmick.GOAl_EXP_CONTENTS_POS);
        int explanation = mGoalExplanationIdList.get(Gimmick.GOAl_EXP_EXPLANATION_POS);

        // 説明内容にギミック用xmlの内容を反映
        ((TextView) dialog.findViewById(R.id.tv_majorTitle)).setText(major);
        ((TextView) dialog.findViewById(R.id.tv_subTitle)).setText(sub);
        ((TextView) dialog.findViewById(R.id.tv_goalContents)).setText(contents);
        ((TextView) dialog.findViewById(R.id.tv_explanationContents)).setText(explanation);*/
    }


    /*
     * 完了したチュートリアルをviewに反映
     */
    private void setFinishTutorialInfo(Dialog dialog) {

        TextView tv_statusNow = dialog.findViewById(R.id.tv_statusNow);
        ImageView iv_star1 = dialog.findViewById(R.id.iv_star1);
        ImageView iv_star2 = dialog.findViewById(R.id.iv_star2);
        ImageView iv_star3 = dialog.findViewById(R.id.iv_star3);
        ImageView iv_star4 = dialog.findViewById(R.id.iv_star4);

        // 星イメージをリスト化
        ArrayList<ImageView> starList = new ArrayList<>();
        starList.add( iv_star1 );
        starList.add( iv_star2 );
        starList.add( iv_star3 );
        starList.add( iv_star4 );

        // 完了イメージ
        Resources resources = getResources();
        Drawable starImage = resources.getDrawable( R.drawable.baseline_star_24, null);

        //-------------------------------
        // 現在のチュートリアル進行状況を反映
        //-------------------------------
        // テキスト
        int finishNum = mTutorial - 1;
        tv_statusNow.setText( Integer.toString(finishNum) );

        // イメージ
        int seqTutorial = getResources().getInteger(R.integer.saved_tutorial_block);;
        for( ImageView star: starList ){
            // 完了しているチュートリアルのイメージを変更
            if( seqTutorial < mTutorial  ){
                star.setImageDrawable(starImage);
            }
            seqTutorial++;
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
        float widthRatio = ( (orientation == Configuration.ORIENTATION_PORTRAIT) ? PORTRAIT_RATIO : LANDSCAPE_RATIO );

        // 画面サイズの取得
        int screeenWidth = getScreenWidth( getContext() );
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.width = (int) (screeenWidth * widthRatio);

        // サイズ反映
        window.setAttributes(lp);
    }

    /*
     *　スクリーン横幅を取得
     */
    private int getScreenWidth( Context context ) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
            WindowMetrics windowMetrics = windowManager.getCurrentWindowMetrics();
            return windowMetrics.getBounds().width();
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity)context).getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
            return displayMetrics.widthPixels;
        }
    }
}
