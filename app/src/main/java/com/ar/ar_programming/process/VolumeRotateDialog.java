package com.ar.ar_programming.process;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.ar.ar_programming.R;

public class VolumeRotateDialog extends DialogFragment {

    // 処理量
    public int mDegrees;
    // クリックリスナー
    private PositiveClickListener mPositiveClickListener;

    // 空のコンストラクタ（DialogFragmentのお約束）
    public VolumeRotateDialog() {
    }

    // インスタンス作成
    public static VolumeRotateDialog newInstance() {
        return new VolumeRotateDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_volume_rotate);

        // Number Pickerの設定
        setNumberPicker( dialog );
        // ユーザーOKイメージ押下設定
        setPositiveImage( dialog );

        return dialog;
    }

    /*
     * Number Pickerの設定
     */
    private void setNumberPicker(Dialog dialog ) {

        NumberPicker np_degree_100 = dialog.findViewById(R.id.np_degree_100);
        NumberPicker np_degree_10 = dialog.findViewById(R.id.np_degree_10);
        NumberPicker np_degree_1 = dialog.findViewById(R.id.np_degree_1);

        //----------------------------------
        // NumberPicker初期設定
        //----------------------------------
        // 時間の範囲を設定
        np_degree_100.setMaxValue(3);
        np_degree_100.setMinValue(0);
        np_degree_10.setMaxValue(9);
        np_degree_10.setMinValue(0);
        np_degree_1.setMaxValue(9);
        np_degree_1.setMinValue(0);

        // 数値フォーマット設定
/*
        np_degree_100.setFormatter( new PickerFormatter() );
        np_degree_10.setFormatter( new PickerFormatter() );
        np_degree_1.setFormatter( new PickerFormatter() );
*/

        //----------------------------------
        // 時分秒情報をPickerに反映
        //----------------------------------
        int degree_100 = (mDegrees / 100);
        int degree_10 = (mDegrees / 10) % 10;
        int degree_1 = mDegrees - ( (degree_100 * 100) + (degree_10 * 10) );

        Log.i("クリックリスナー", "mDegrees=" + mDegrees);
        Log.i("クリックリスナー", "degree_100=" + degree_100);
        Log.i("クリックリスナー", "degree_10=" + degree_10);
        Log.i("クリックリスナー", "degree_1=" + degree_1);

        // Pickerに反映
        np_degree_100.setValue( degree_100 );
        np_degree_10.setValue( degree_10 );
        np_degree_1.setValue( degree_1 );
    }

    /*
     * ユーザーOKイメージ押下設定
     */
    private void setPositiveImage( Dialog dialog ) {

        ImageView iv_save = dialog.findViewById(R.id.iv_save);
        iv_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ユーザーが設定した量
                int volume = getUserVolume();
                // 範囲チェック
                if( volume > 360 ){
                    // 範囲外ならエラーメッセージを表示
                    TextView tv_error = dialog.findViewById(R.id.tv_error);
                    tv_error.setVisibility( View.VISIBLE );
                    return;
                }

                mPositiveClickListener.onPositiveClick( volume );
                dismiss();
            }
        });
    }

    /*
     * ユーザーが入力した処理量を取得
     */
    private int getUserVolume() {

        Dialog dialog = getDialog();

        // 設定された角度を取得
        NumberPicker np_degree_100 = dialog.findViewById(R.id.np_degree_100);
        NumberPicker np_degree_10 = dialog.findViewById(R.id.np_degree_10);
        NumberPicker np_degree_1 = dialog.findViewById(R.id.np_degree_1);

        Integer degree_100 = np_degree_100.getValue() * 100;
        Integer degree_10 = np_degree_10.getValue() * 10;
        Integer degree_1 = np_degree_1.getValue();

        // 角度を返す
        return (degree_100 + degree_10 + degree_1);
    }

    /*
     * 処理量の設定
     */
    public void setVolume(String degrees ) {
        mDegrees = Integer.parseInt( degrees );;
    }

    /*
     * クリックリスナーの設定
     */
    public void setOnPositiveClickListener( PositiveClickListener listener ) {
        mPositiveClickListener = listener;
    }

    /*
     * クリック検出用インターフェース
     */
    public interface PositiveClickListener {
        // クリックリスナー
        void onPositiveClick( int volume );
    }

    /*
     * NumberPicker Formatter
     */
    private class PickerFormatter implements NumberPicker.Formatter {

        public PickerFormatter() {}

        @Override
        public String format(int num) {

            // 値が１桁の場合は、２桁目を０埋め
            if( num <= 9 ){
                return "0" + Integer.toString( num );
            }

            // 値が２桁の場合は、変更なし
            return Integer.toString( num );
        }

    }
}
