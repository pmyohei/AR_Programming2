package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import com.ar.ar_programming.R;


/*
 * 単体処理ビュー
 */
public class SingleProcessBlock extends ProcessBlock {

    //---------------------------
    // 定数
    //----------------------------


    //---------------------------
    // フィールド変数
    //----------------------------
    private FragmentManager mFragmentManager;
    //    private int mProcessKind;
    private int mProcessVolume;

    /*
     * コンストラクタ
     */
    public SingleProcessBlock(Context context, FragmentManager fragmentManager) {
        this(context, (AttributeSet) null);

        mFragmentManager = fragmentManager;
    }

    public SingleProcessBlock(Context context) {
        this(context, (AttributeSet) null);
    }

    public SingleProcessBlock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SingleProcessBlock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        View.inflate(context, R.layout.process_block_single, this);

        // 初期化
        init();

        //仮-確認用----
//        final DateFormat df = new SimpleDateFormat("ss");
//        final Date date = new Date(System.currentTimeMillis());
//        final int id = getId();
//        TextView et_value = this.findViewById(R.id.et_value);
//        et_value.setOnClickListener(new OnClickListener() {
//             @Override
//             public void onClick(View view) {
//                 Log.i("タッチチェック", "onClick");
//             }
//        });

        //tmp
//        mProcKind = PROC_KIND_FORWARD;
//        mProcVolume = 2;
    }

    /*
     * 初期化処理
     */
    private void init() {

        mProcessVolume = 0;
        mProcessType = PROCESS_TYPE_SINGLE;
/*
        // 処理ブロック種別
        mProcessContent =
*/

        // onDragリスナーの設定
        setDragAndDropListerner();
    }

    /*
     * 処理量リスナーの設定
     */
    private void setVolumeListener() {

        // クリックリスナー設定
        TextView et_value = findViewById(R.id.et_value);

        // 処理種別に応じて表示するダイアログを切り分け
        switch (mProcessKind) {

            case PROC_KIND_FORWARD:
            case PROC_KIND_BACK:
                setWalkVolumeListener( et_value );
                return;

            case PROC_KIND_RIGHT_ROTATE:
            case PROC_KIND_LEFT_ROTATE:
                setRotateVolumeListener( et_value );
                return;
        }
    }

    /*
     * 処理量（歩行）リスナーの設定
     */
    private void setWalkVolumeListener( TextView et_value ) {

        // 時間設定用のダイアログ表示をリスナーに設定
        et_value.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // 設定中の処理量
                String volume = et_value.getText().toString();

                // 処理量設定ダイアログを表示
                VolumeDialog dialog = VolumeDialog.newInstance();
                dialog.setVolume( VolumeDialog.VOLUME_KIND_CM, volume );
                dialog.setOnPositiveClickListener(new VolumeDialog.PositiveClickListener() {
                        @Override
                        public void onPositiveClick(int volume) {
                            // 入力された処理量を保持
                            mProcessVolume = volume;
                            // 入力された処理量をビューに反映
                            et_value.setText(String.format("%03d", volume));
                        }
                    }
                );
                dialog.show(mFragmentManager, "SHOW");
            }
        });
    }


    /*
     * 処理量（回転）リスナーの設定
     */
    private void setRotateVolumeListener(TextView et_value) {

        // 処理量設定のダイアログリスナーを設定
        et_value.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // 設定中の処理量
                String volume = et_value.getText().toString();

                // 処理量設定ダイアログを表示
                VolumeDialog dialog = VolumeDialog.newInstance();
                dialog.setVolume( VolumeDialog.VOLUME_KIND_ANGLE, volume );
                dialog.setOnPositiveClickListener(new VolumeDialog.PositiveClickListener() {
                        @Override
                        public void onPositiveClick(int volume) {
                            // 入力された処理量を保持
                            mProcessVolume = volume;
                            // 入力された処理量をビューに反映
                            et_value.setText(String.format("%03d", volume));
                        }
                    }
                );
                dialog.show(mFragmentManager, "SHOW");
            }
        });
    }


    /*
     * 処理文言を設定
     */
    private void setProcessWording() {

        // 処理内容と単位の文言ID
        int contentId;
        int unitId;

        // 種別に応じた文言IDを取得
        switch (mProcessKind){
            case PROC_KIND_FORWARD:
                contentId = R.string.block_contents_forward;
                unitId = R.string.block_unit_walk;
                break;

            case PROC_KIND_BACK:
                contentId = R.string.block_contents_back;
                unitId = R.string.block_unit_walk;
                break;

            case PROC_KIND_LEFT_ROTATE:
                contentId = R.string.block_contents_rorate_left;
                unitId = R.string.block_unit_rotate;
                break;

            case PROC_KIND_RIGHT_ROTATE:
                contentId = R.string.block_contents_rorate_right;
                unitId = R.string.block_unit_rotate;
                break;

            default:
                contentId = R.string.block_contents_rorate_right;
                unitId = R.string.block_unit_rotate;
                break;
        }

        // 文言IDをレイアウトに設定
        TextView tv_contents = findViewById(R.id.tv_contents);
        tv_contents.setText( contentId );
    }

    /*
     * 「プログラミング処理種別」の設定
     */
    @Override
    public void setProcessKind(int processKind ) {
        super.setProcessKind( processKind );

        // 種別に応じた文言に変更
        setProcessWording();
        // 処理量リスナー設定
        setVolumeListener();
    }

    /*
     * 「プログラミング処理量」の取得
     */
    public int getProcessVolume() {
        return mProcessVolume;
    }
    /*
     * 「プログラミング処理量」の設定
     */
    public void setProcessVolume(int processVolume ) {
        mProcessVolume = processVolume;
    }


}
