package com.ar.ar_programming;

import static android.content.Context.WINDOW_SERVICE;

import static com.ar.ar_programming.GimmickManager.GOAl_EXP_CONTENTS_POS;
import static com.ar.ar_programming.GimmickManager.GOAl_EXP_EXPLANATION_POS;
import static com.ar.ar_programming.GimmickManager.GOAl_EXP_MAJOR_POS;
import static com.ar.ar_programming.GimmickManager.GOAl_EXP_SUB_POS;

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

import java.util.ArrayList;

public class GoalExplanationDialogFragment extends DialogFragment {

    private ArrayList<Integer> mGoalExplanationIdList;
    private OnDestroyListener mOnDestroyListener;

    //空のコンストラクタ
    //※必須（画面回転等の画面再生成時にコールされる）
    public GoalExplanationDialogFragment() {
        //do nothing
    }

    public GoalExplanationDialogFragment(ArrayList<Integer> idList) {
        mGoalExplanationIdList = idList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_goal_explanation, container, false);
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
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

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

        // 設定文言ID
        int major = mGoalExplanationIdList.get(GOAl_EXP_MAJOR_POS);
        int sub = mGoalExplanationIdList.get(GOAl_EXP_SUB_POS);
        int contents = mGoalExplanationIdList.get(GOAl_EXP_CONTENTS_POS);
        int explanation = mGoalExplanationIdList.get(GOAl_EXP_EXPLANATION_POS);

        // 説明内容にギミック用xmlの内容を反映
        ((TextView) dialog.findViewById(R.id.tv_majorTitle)).setText(major);
        ((TextView) dialog.findViewById(R.id.tv_subTitle)).setText(sub);
        ((TextView) dialog.findViewById(R.id.tv_goalContents)).setText(contents);
        ((TextView) dialog.findViewById(R.id.tv_explanationContents)).setText(explanation);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mOnDestroyListener.onDestroy();
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
     *　onDestroy()検出インターフェースの設定
     */
    public void setOnDestroyListener( OnDestroyListener listener ) {
        mOnDestroyListener = listener;
    }

    /*
     * onDestroy()検出インターフェース
     */
    public interface OnDestroyListener {
        void onDestroy();
    }

}
