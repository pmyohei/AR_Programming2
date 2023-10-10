package com.ar.ar_programming.process;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.ar.ar_programming.R;

public class VolumeDialog extends DialogFragment {

    //---------------------------
    // 定数
    //---------------------------
    // 処理量設定種別
    public static final int VOLUME_KIND_CM = 0;
    public static final int VOLUME_KIND_ANGLE = 1;

    // 入力上限
    public static final int VOLUME_LIMIT_CM = 100;
    public static final int VOLUME_LIMIT_ANGLE = 360;

    //------------------
    // アクション共通
    //------------------
    // 処理量
    public int mVolume;
    // クリックリスナー
    private PositiveClickListener mPositiveClickListener;

    //------------------
    // アクション別
    //------------------
    // 処理量上限
    public int mVolumeLimit;
    // 処理量単位
    public int mUnitStringID;
    // 処理量単位
    public int mRangeErrorStringID;


    /*
     * コンストラクタ
     */
    // 空のコンストラクタ（DialogFragmentのお約束）
    public VolumeDialog() {
    }

    // インスタンス作成
    public static VolumeDialog newInstance() {
        return new VolumeDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_volume);

        // Number Pickerの設定
        setNumberPicker(dialog);
        // ユーザーOKイメージ押下設定
        setPositiveImage(dialog);
        // 単位表記設定
        setUnitStr(dialog);

        return dialog;
    }

    /*
     * Number Pickerの設定
     */
    private void setNumberPicker(Dialog dialog) {

        NumberPicker np_100 = dialog.findViewById(R.id.np_100);
        NumberPicker np_10 = dialog.findViewById(R.id.np_10);
        NumberPicker np_1 = dialog.findViewById(R.id.np_1);

        //----------------------------------
        // NumberPicker初期設定
        //----------------------------------
        // 範囲設定
        np_100.setMaxValue(9);
        np_100.setMinValue(0);
        np_10.setMaxValue(9);
        np_10.setMinValue(0);
        np_1.setMaxValue(9);
        np_1.setMinValue(0);

        //----------------------------------
        // 設定情報をPickerに反映
        //----------------------------------
        int degree_100 = (mVolume / 100);
        int degree_10 = (mVolume / 10) % 10;
        int degree_1 = mVolume - ((degree_100 * 100) + (degree_10 * 10));

        // Pickerに反映
        np_100.setValue(degree_100);
        np_10.setValue(degree_10);
        np_1.setValue(degree_1);
    }

    /*
     * ユーザーOKイメージ押下設定
     */
    private void setPositiveImage(Dialog dialog) {

        ImageView iv_save = dialog.findViewById(R.id.iv_save);
        iv_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //------------------------------
                // ユーザーが設定した量の範囲チェック
                //------------------------------
                int volume = getUserVolume();
                if ((volume < 1) || (volume > mVolumeLimit) ) {
                    // 範囲外ならエラーメッセージを表示
                    TextView tv_rangeError = dialog.findViewById(R.id.tv_rangeError);
                    tv_rangeError.setText( mRangeErrorStringID );
                    tv_rangeError.setVisibility(View.VISIBLE);
                    return;
                }

                //-------------
                // 設定完了
                //-------------
                mPositiveClickListener.onPositiveClick(volume);
                dismiss();
            }
        });
    }

    /*
     * 単位表記設定
     */
    private void setUnitStr(Dialog dialog) {
        // 書き換え
        TextView tv_unit = dialog.findViewById(R.id.tv_unit);
        tv_unit.setText( mUnitStringID );
    }

    /*
     * ユーザーが入力した処理量を取得
     */
    private int getUserVolume() {

        Dialog dialog = getDialog();

        // 設定された角度を取得
        NumberPicker np_degree_100 = dialog.findViewById(R.id.np_100);
        NumberPicker np_degree_10 = dialog.findViewById(R.id.np_10);
        NumberPicker np_degree_1 = dialog.findViewById(R.id.np_1);

        Integer degree_100 = np_degree_100.getValue() * 100;
        Integer degree_10 = np_degree_10.getValue() * 10;
        Integer degree_1 = np_degree_1.getValue();

        // 入力値を返す
        return (degree_100 + degree_10 + degree_1);
    }

    /*
     * 処理種別の設定
     */
    public void setVolumeKind( int volumeKind ) {

        //----------------------------------
        // 処理種別に応じて保持するデータを切り分け
        //----------------------------------
        if( volumeKind == VOLUME_KIND_CM ){

            //-------------
            // 前進／後退
            //-------------
            // 処理量上限
            mVolumeLimit = VOLUME_LIMIT_CM;
            // 単位
            mUnitStringID = R.string.block_unit_walk;
            // 判定エラーメッセージ
            mRangeErrorStringID = R.string.process_volume_walk_error;

        } else {

            //-------------
            // 回転
            //-------------
            // 処理量上限
            mVolumeLimit = VOLUME_LIMIT_ANGLE;
            // 単位
            mUnitStringID = R.string.block_unit_rotate;
            // 判定エラーメッセージ
            mRangeErrorStringID = R.string.process_volume_degree_error;
        }

    }

    /*
     * 処理量の設定
     */
    public void setVolume( String volume ) {
        // 処理量
        mVolume = Integer.parseInt( volume );
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
