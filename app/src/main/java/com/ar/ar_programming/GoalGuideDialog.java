package com.ar.ar_programming;

import static android.content.Context.WINDOW_SERVICE;

import static com.ar.ar_programming.Common.TUTORIAL_FINISH;
import static com.ar.ar_programming.GimmickManager.GOAl_EXP_CONTENTS_POS;
import static com.ar.ar_programming.GimmickManager.GOAl_EXP_EXPLANATION_POS;
import static com.ar.ar_programming.GimmickManager.GOAl_EXP_MAJOR_POS;
import static com.ar.ar_programming.GimmickManager.GOAl_EXP_SUB_POS;
import static com.ar.ar_programming.GimmickManager.PRE_REPLACE_WORD;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

public class GoalGuideDialog extends DialogFragment {

    private ArrayList<Integer> mGoalGuideIdList;
    private int mTutorial;
    private OnDestroyListener mOnDestroyListener;

    //空のコンストラクタ
    //※必須（画面回転等の画面再生成時にコールされる）
    public GoalGuideDialog() {
        //do nothing
    }

    public GoalGuideDialog(ArrayList<Integer> idList, int tutorial) {
        mGoalGuideIdList = idList;
        mTutorial = tutorial;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_goal_guide, container, false);
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
        int major = mGoalGuideIdList.get(GOAl_EXP_MAJOR_POS);
        int sub = mGoalGuideIdList.get(GOAl_EXP_SUB_POS);
        int contents = mGoalGuideIdList.get(GOAl_EXP_CONTENTS_POS);
        int explanation = mGoalGuideIdList.get(GOAl_EXP_EXPLANATION_POS);

        // チュートリアル番号の設定
        Log.i("ステージ選択", "major=" + major);
        String majorStr = getMajorString( dialog.getContext(), major );

        // 説明内容にギミック用xmlの内容を反映
        ((TextView) dialog.findViewById(R.id.tv_majorTitle)).setText(majorStr);
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
     * チュートリアル番号の設定
     */
    private String getMajorString(Context context, int titleID) {

        String major = context.getString( titleID );

        //----------------
        // チュートリアル終了
        //----------------
        if (mTutorial >= TUTORIAL_FINISH) {
            // 文字列加工なし
            return major;
        }

        //----------------
        // チュートリアル中
        //----------------
        // タイトル文字列を現在のチュートリアル番号に置き換え
        return major.replace( PRE_REPLACE_WORD, Integer.toString( mTutorial ) );
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
