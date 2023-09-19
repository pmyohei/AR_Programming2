package com.ar.ar_programming.process;

import static com.ar.ar_programming.Gimmick.VOLUME_LIMIT_NONE;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import com.ar.ar_programming.GimmickManager;
import com.ar.ar_programming.character.CharacterNode;
import com.ar.ar_programming.Gimmick;
import com.ar.ar_programming.R;
import com.google.android.material.snackbar.Snackbar;


/*
 * 実行ブロック
 */
public class ExecuteBlock extends ProcessBlock {

    //---------------------------
    // 定数
    //---------------------------
    private final String VOLUME_FORMAT = "%03d";

    //---------------------------
    // フィールド変数
    //---------------------------
    private FragmentManager mFragmentManager;
    private int mProcessVolume;

    /*
     * コンストラクタ
     */
    public ExecuteBlock(Context context, FragmentManager fragmentManager, Gimmick.XmlBlockInfo xmlBlockInfo) {
        this(context, (AttributeSet) null, xmlBlockInfo);
        mFragmentManager = fragmentManager;
    }

    public ExecuteBlock(Context context) {
        this(context, (AttributeSet) null, null);
        Log.i("ブロックxml", "SingleBlock 2");
    }

    public ExecuteBlock(Context context, AttributeSet attrs, Gimmick.XmlBlockInfo xmlBlockInfo) {
        this(context, attrs, 0, xmlBlockInfo);
    }

    public ExecuteBlock(Context context, AttributeSet attrs, int defStyle, Gimmick.XmlBlockInfo xmlBlockInfo) {
        super(context, attrs, defStyle, xmlBlockInfo);
//        Log.i("ブロックxml", "SingleBlock 4 valueLimit=" + valueLimit);
        init( xmlBlockInfo.fixVolume);
        setLayout(R.layout.process_block_exe);
    }

    /*
     * 初期化
     */
    private void init(int valueLimit) {
        mProcessVolume = valueLimit;
    }

    /*
     * レイアウト設定
     */
    @Override
    public void setLayout(int layoutID) {
        super.setLayout(layoutID);

        // ブロック内の内容を書き換え
        rewriteProcessContents();
        // ブロックレイアウト設定
        setBlockLayout( mXmlBlockInfo.action );
        // ブロックタッチリスナー
        setBlockTouchListerer();
    }


    /*
     * 単体ブロックレイアウト設定
     */
    private void setBlockLayout(String action) {

        switch (action) {
            //------------------------
            // 処理量をユーザーが変更可能
            //------------------------
            case GimmickManager.BLOCK_EXE_FORWARD:
            case GimmickManager.BLOCK_EXE_BACK:
            case GimmickManager.BLOCK_EXE_ROTATE_RIGHT:
            case GimmickManager.BLOCK_EXE_ROTATE_LEFT:
                // 処理量設定リスナー
                setVolumeListener();
                break;

            //------------------------
            // 処理量なし
            //------------------------
            default:
                // 処理量なしのブロックレイアウト設定
                setNoVolumeLayout();
                break;
        }
    }

    /*
     * 処理量なし設定リスナー
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
                if( mXmlBlockInfo.fixVolume != VOLUME_LIMIT_NONE ){
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
        String action = mXmlBlockInfo.action;
        if ((action.equals( GimmickManager.BLOCK_EXE_ROTATE_RIGHT )) || (action.equals( GimmickManager.BLOCK_EXE_ROTATE_LEFT ))) {
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
     * 処理ブロックアニメーターの生成
     */
    public ValueAnimator createProcessBlockAnimator(CharacterNode characterNode, String action, float volume, long duration) {

        //------------------------------------------------------
        // 処理種別と処理量からアニメーション量とアニメーション時間を取得
        //------------------------------------------------------
        // 処理に対応するアニメーションプロパティ名を取得
        // ※ setXXX()の「XXX」を取得
        String methodPropertyName = characterNode.getMethodPropertyName(action);

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

        Log.i("ブロック処理の流れ", "exe startProcess()開始 action=" + mXmlBlockInfo.action);
        Log.i("ブロック処理の流れ", "exe startProcess()開始 targetNode_1=" + mXmlBlockInfo.targetNode_1);

        //----------------------------------------
        // ブロックアニメーションデータからアニメータを生成
        //----------------------------------------
        // 処理種別と処理量
        String action = mXmlBlockInfo.action;
        int setVolume = getProcessVolume();
        // アニメーション量とアニメーション時間
        float volume = characterNode.getAnimationVolume(action, setVolume);
        long duration = characterNode.getAnimationDuration(action, setVolume);

        // 今回の処理用アニメーターをキャラクターに保持させる
        ValueAnimator processAnimator = createProcessBlockAnimator(characterNode, action, volume, duration);
        characterNode.setAnimator(this, processAnimator, action, volume);

        // 処理ブロックアニメーション開始
        processAnimator.start();

        //----------------------------------------
        // アニメーションの開始：モデルアニメーション用
        //----------------------------------------
        // 3Dモデルに用意されたアニメーションを開始
        String animationName = characterNode.getModelAnimationName(action);
        characterNode.startModelAnimation(animationName, duration);

        //----------------------------------------
        // キャラクターアクションの内容設定
        //----------------------------------------
        characterNode.setActionWord( mXmlBlockInfo.action );
        characterNode.setTargetNode( mXmlBlockInfo.targetNode_1 );
    }

}
