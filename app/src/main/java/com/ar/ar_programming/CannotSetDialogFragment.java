package com.ar.ar_programming;

import static com.ar.ar_programming.Common.TUTORIAL_DEFAULT;

import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        // 完了したチュートリアルをviewに反映
        setFinishTutorialInfo(dialog);
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
        ImageView iv_star5 = dialog.findViewById(R.id.iv_star5);

        // 星イメージをリスト化
        ArrayList<ImageView> starList = new ArrayList<>();
        starList.add( iv_star1 );
        starList.add( iv_star2 );
        starList.add( iv_star3 );
        starList.add( iv_star4 );
        starList.add( iv_star5 );

        // 完了イメージ
        Resources resources = getResources();
        Drawable starImage = resources.getDrawable( R.drawable.baseline_star, null);

        //-------------------------------
        // 現在のチュートリアル進行状況を反映
        //-------------------------------
        // テキスト
        int finishNum = mTutorial - 1;
        tv_statusNow.setText( Integer.toString(finishNum) );

        // イメージ
        int seqTutorial = TUTORIAL_DEFAULT;
        for( ImageView star: starList ){
            // 完了しているチュートリアルのイメージを変更
            if( seqTutorial < mTutorial  ){
                star.setImageDrawable(starImage);
            }
            seqTutorial++;
        }
    }
}
