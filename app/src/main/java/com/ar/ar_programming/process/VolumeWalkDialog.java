package com.ar.ar_programming.process;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;

import androidx.fragment.app.DialogFragment;

import com.ar.ar_programming.R;

public class VolumeWalkDialog extends DialogFragment {

    // 処理量文字列
    public String volumeStr;
    // クリックリスナー
    private PositiveClickListener mPositiveClickListener;

    //空のコンストラクタ（DialogFragmentのお約束）
    public VolumeWalkDialog() {
    }

    //インスタンス作成
    public static VolumeWalkDialog newInstance() {
        return new VolumeWalkDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_volume_walk);

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

        NumberPicker np_volume = dialog.findViewById(R.id.np_volume);

        //----------------------------------
        // NumberPicker初期設定
        //----------------------------------
        // 時間の範囲を設定
        np_volume.setMaxValue(59);
        np_volume.setMinValue(0);

        // 数値フォーマット設定
        np_volume.setFormatter( new PickerFormatter() );

        //----------------------------------
        // 時分秒情報をPickerに反映
        //----------------------------------
        int volume = Integer.parseInt( volumeStr );
        np_volume.setValue( volume );
    }

    /*
     * ユーザーOKイメージ押下設定
     */
    private void setPositiveImage( Dialog dialog ) {
        ImageView iv_save = dialog.findViewById(R.id.iv_save);
        iv_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ユーザーが設定した時分秒を返す
                int volume = getUserVolume();
                mPositiveClickListener.onPositiveClick( volume );
                dismiss();
            }
        });
    }

    /*
     * ユーザーが入力した処理量を取得
     */
    private int getUserVolume() {

        // 設定された処理量を取得
        Dialog dialog = getDialog();
        NumberPicker np_volume = dialog.findViewById(R.id.np_volume);

        return np_volume.getValue();
    }

    /*
     * 処理量の設定
     */
    public void setVolume(String mmssStr ) {
        volumeStr = mmssStr;
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
