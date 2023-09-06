package com.ar.ar_programming.process;

import static com.ar.ar_programming.Gimmick.VOLUME_LIMIT_NONE;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import com.ar.ar_programming.CharacterNode;
import com.ar.ar_programming.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.ar.sceneform.Node;

import java.util.List;


/*
 * 単体処理ビュー
 */
public class SingleBlock extends ProcessBlock {

    //---------------------------
    // 定数
    //---------------------------
    public static final int PROCESS_CONTENTS_FORWARD = 0;
    public static final int PROCESS_CONTENTS_BACK = 1;
    public static final int PROCESS_CONTENTS_RIGHT_ROTATE = 2;
    public static final int PROCESS_CONTENTS_LEFT_ROTATE = 3;
    public static final int PROCESS_CONTENTS_EAT = 4;
    public static final int PROCESS_CONTENTS_THROW_AWAY = 5;

    private final String VOLUME_FORMAT = "%03d";

    //---------------------------
    // フィールド変数
    //---------------------------
    private FragmentManager mFragmentManager;
    private int mProcessVolume;
    private int mProcessVolumeLimit;

    /*
     * コンストラクタ
     */
    public SingleBlock(Context context, FragmentManager fragmentManager, int contents, int valueLimit) {
        this(context, (AttributeSet) null, contents, valueLimit);
        mFragmentManager = fragmentManager;
        Log.i("ブロックxml", "SingleBlock 1 valueLimit=" + valueLimit);
    }

    public SingleBlock(Context context, int valueLimit) {
        this(context, (AttributeSet) null, 0, valueLimit);
        Log.i("ブロックxml", "SingleBlock 2");
    }

    public SingleBlock(Context context, AttributeSet attrs, int contents, int valueLimit) {
        this(context, attrs, 0, contents, valueLimit);
        Log.i("ブロックxml", "SingleBlock 3 valueLimit=" + valueLimit);
    }

    public SingleBlock(Context context, AttributeSet attrs, int defStyle, int contents, int valueLimit) {
        super(context, attrs, defStyle, PROCESS_TYPE_SINGLE, contents);
        Log.i("ブロックxml", "SingleBlock 4 valueLimit=" + valueLimit);
        init(valueLimit);
        setLayout(R.layout.process_block_single);
    }

    /*
     * 初期化
     */
    private void init(int valueLimit) {
        mProcessVolume = valueLimit;
        mProcessVolumeLimit = valueLimit;
    }

    /*
     * レイアウト設定
     */
    @Override
    public void setLayout(int layoutID) {
        super.setLayout(layoutID);

        // ブロック内の内容を書き換え
        rewriteProcessContents(mProcessContents);
        // ブロックレイアウト設定
        setBlockLayout(mProcessContents);
        // ブロックタッチリスナー
        setBlockTouchListerer();
    }


    /*
     * 単体ブロックレイアウト設定
     */
    private void setBlockLayout(int contents) {

        switch (contents) {
            //------------------------
            // 処理量をユーザーが変更可能
            //------------------------
            case PROCESS_CONTENTS_FORWARD:
            case PROCESS_CONTENTS_BACK:
            case PROCESS_CONTENTS_LEFT_ROTATE:
            case PROCESS_CONTENTS_RIGHT_ROTATE:
                // 処理量設定リスナー
                setVolumeListener();
                break;

            //------------------------
            // 処理量なし
            //------------------------
            case PROCESS_CONTENTS_EAT:
            case PROCESS_CONTENTS_THROW_AWAY:
            default:
                // 処理量なしのブロックレイアウト設定
                setNoVolumeLayout();
                break;
        }
    }

    /*
     * 処理量設定リスナー
     */
    private void setNoVolumeLayout() {
        // 処理量viewを非表示
        TextView tv_volume = findViewById(R.id.tv_volume);
        tv_volume.setVisibility( GONE );
    }

    /*
     * 処理量設定リスナー
     */
    private void setVolumeListener() {

        //--------------------
        // ダイアログ表示設定
        //--------------------
        TextView tv_volume = findViewById(R.id.tv_volume);
        tv_volume.setText(String.format(VOLUME_FORMAT, mProcessVolume));
        tv_volume.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                // 処理量制限の有無に応じて、設定可否を変える
                if( mProcessVolumeLimit != VOLUME_LIMIT_NONE ){
                    // 処理量変更不可のメッセージを表示
                    Snackbar.make(getRootView(), R.string.diable_set_volume, Snackbar.LENGTH_SHORT).show();
                } else {
                    // 処理量設定ダイアログの表示
                    showVolumeSetDialog( tv_volume );
                }
            }
        });
    }

    /*
     * 処理量設定ダイアログの表示
     */
    private void showVolumeSetDialog(TextView tv_volume) {

        //--------------------
        // 処理量種別種別
        //--------------------
        int volumeKind;
        if ((mProcessContents == PROCESS_CONTENTS_RIGHT_ROTATE) || (mProcessContents == PROCESS_CONTENTS_LEFT_ROTATE)) {
            volumeKind = VolumeDialog.VOLUME_KIND_ANGLE;
        } else {
            volumeKind = VolumeDialog.VOLUME_KIND_CM;
        }

        //--------------------
        // ダイアログ
        //--------------------
        // 設定中の処理量
        String volume = tv_volume.getText().toString();

        // 処理量設定ダイアログを表示
        VolumeDialog dialog = VolumeDialog.newInstance();
        dialog.setVolume(volumeKind, volume);
        dialog.setOnPositiveClickListener(new VolumeDialog.PositiveClickListener() {
                @Override
                public void onPositiveClick(int volume) {
                    // 入力された処理量を保持
                    mProcessVolume = volume;
                    // 入力された処理量をビューに反映
                    tv_volume.setText(String.format(VOLUME_FORMAT, volume));
                }
            }
        );
        dialog.show(mFragmentManager, "SHOW");
    }

    /*
     * 「プログラミング処理量」の取得
     */
    public int getProcessVolume() {
        return mProcessVolume;
    }

    /*
     * 処理ブロック内の内容を書き換え
     */
    @Override
    public void rewriteProcessContents(int contents) {

        // 処理内容文字列ID
        int contentId;

        // 種別に応じた文言IDを取得
        switch (contents) {
            case PROCESS_CONTENTS_FORWARD:
                contentId = R.string.block_contents_forward;
                break;
            case PROCESS_CONTENTS_BACK:
                contentId = R.string.block_contents_back;
                break;
            case PROCESS_CONTENTS_LEFT_ROTATE:
                contentId = R.string.block_contents_rorate_left;
                break;
            case PROCESS_CONTENTS_RIGHT_ROTATE:
                contentId = R.string.block_contents_rorate_right;
                break;
            case PROCESS_CONTENTS_EAT:
                contentId = R.string.block_contents_eat;
                break;
            case PROCESS_CONTENTS_THROW_AWAY:
                contentId = R.string.block_contents_throw_away;
                break;
            default:
                contentId = R.string.block_contents_forward;
                break;
        }

        // 文言IDをレイアウトに設定
        TextView tv_contents = findViewById(R.id.tv_contents);
        tv_contents.setText(contentId);
    }

    /*
     * 処理ブロックアニメーターの生成
     */
    public ValueAnimator createProcessBlockAnimator(CharacterNode characterNode, int contents, float volume, long duration) {

        //------------------------------------------------------
        // 処理種別と処理量からアニメーション量とアニメーション時間を取得
        //------------------------------------------------------
        // 処理に対応するアニメーションプロパティ名を取得
        // ※ setXXX()の「XXX」を取得
        String methodPropertyName = CharacterNode.getPropertyName(contents);

        //----------------------------------
        // アニメーションの生成／開始：処理ブロック用
        //----------------------------------
        // アニメーション生成
        ValueAnimator processAnimator = ObjectAnimator.ofFloat(characterNode, methodPropertyName, volume);
        processAnimator.setDuration(duration);

        return processAnimator;
    }

    /*
     * 処理開始
     */
    @Override
    public void startProcess(CharacterNode characterNode) {

        //----------------------------------------
        // ブロックアニメーションデータからアニメータを生成
        //----------------------------------------
        // 処理種別と処理量
        int contents = getProcessContents();
        int setVolume = getProcessVolume();
        // アニメーション量とアニメーション時間
        float volume = characterNode.getAnimationVolume(contents, setVolume);
        long duration = characterNode.getAnimationDuration(contents, setVolume);

        // 今回の処理用アニメーターを保持させる
        ValueAnimator processAnimator = createProcessBlockAnimator(characterNode, contents, volume, duration);
        characterNode.setAnimator(this, processAnimator, contents, volume);

        // 処理ブロックアニメーション開始
        processAnimator.start();

        //----------------------------------------
        // アニメーションの開始：モデルアニメーション用
        //----------------------------------------
        // モデルに用意されたアニメーションを開始
        characterNode.startModelAnimation(contents, duration);
        Log.i("不具合", "contents=" + contents);
        Log.i("不具合", "volume=" + volume);
        Log.i("不具合", "duration=" + duration);
        Log.i("不具合", "===========");

        //----------------------------------------
        // キャラクターアクションの内容設定
        //----------------------------------------
        characterNode.setActionWord( mProcessContents );
    }

}
