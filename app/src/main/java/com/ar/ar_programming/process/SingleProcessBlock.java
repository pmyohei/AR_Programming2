package com.ar.ar_programming.process;

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
import com.ar.ar_programming.FirstFragment;
import com.ar.ar_programming.R;
import com.google.ar.sceneform.Node;

import java.util.ArrayList;


/*
 * 単体処理ビュー
 */
public class SingleProcessBlock extends ProcessBlock {

    //---------------------------
    // 定数
    //---------------------------
    public static final int PROCESS_CONTENTS_FORWARD = 0;
    public static final int PROCESS_CONTENTS_BACK = 1;
    public static final int PROCESS_CONTENTS_RIGHT_ROTATE = 2;
    public static final int PROCESS_CONTENTS_LEFT_ROTATE = 3;

    //---------------------------
    // フィールド変数
    //---------------------------
    private FragmentManager mFragmentManager;
    private int mProcessVolume;

    /*
     * コンストラクタ
     */
    public SingleProcessBlock(Context context, FragmentManager fragmentManager, int contents) {
        this(context, (AttributeSet) null, contents);
        mFragmentManager = fragmentManager;
    }

    public SingleProcessBlock(Context context) {
        this(context, (AttributeSet) null, 0);
    }

    public SingleProcessBlock(Context context, AttributeSet attrs, int contents) {
        this(context, attrs, 0, contents);
    }

    public SingleProcessBlock(Context context, AttributeSet attrs, int defStyle, int contents) {
        super(context, attrs, defStyle, PROCESS_TYPE_SINGLE, contents);
        setLayout(R.layout.process_block_single_ver3);
        init();
    }

    /*
     * 初期化
     */
    private void init() {
        mProcessVolume = 0;
    }

    /*
     * レイアウト設定
     */
    @Override
    public void setLayout(int layoutID) {
        super.setLayout(layoutID);

        // 処理ブロック内の内容を書き換え
        rewriteProcessContents(mProcessContents);
        // 処理量設定リスナー
        setVolumeListener();
        // 処理ブロックタッチリスナー
        setBlockTouchListerer();
    }

    /*
     * 処理量設定リスナー
     */
    private void setVolumeListener() {

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
        // ダイアログ表示設定
        //--------------------
        TextView tv_volume = findViewById(R.id.tv_volume);
        tv_volume.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

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
                                                          tv_volume.setText(String.format("%03d", volume));
                                                      }
                                                  }
                );
                dialog.show(mFragmentManager, "SHOW");
            }
        });
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
            default:
                contentId = R.string.block_contents_rorate_right;
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
/*
        // 処理種別と処理量
        int contents = getProcessContents();
        int setVolume = getProcessVolume();

        // アニメーション量とアニメーション時間
        float volume = characterNode.getAnimationVolume(contents, setVolume);
        long duration = characterNode.getAnimationDuration(contents, setVolume);
*/

        // 処理に対応するアニメーションプロパティ名を取得
        String propertyName = CharacterNode.getPropertyName(contents);

        Log.i("アニメーション", "アニメーション開始--------------------");
        Log.i("アニメーション", "volume=" + volume);
        Log.i("アニメーション", "duration=" + duration);

        //----------------------------------
        // アニメーションの生成／開始：処理ブロック用
        //----------------------------------
        // アニメーション生成
        ValueAnimator processAnimator = ObjectAnimator.ofFloat(characterNode, propertyName, volume);
        processAnimator.setDuration(duration);

        return processAnimator;
    }

    /*
     * 処理開始
     */
    @Override
    public void startProcess(CharacterNode characterNode) {

        // 処理種別と処理量
        int contents = getProcessContents();
        int setVolume = getProcessVolume();
        // アニメーション量とアニメーション時間
        float volume = characterNode.getAnimationVolume(contents, setVolume);
        long duration = characterNode.getAnimationDuration(contents, setVolume);

        // 今回の処理用アニメーターを保持させる
        ValueAnimator processAnimator = createProcessBlockAnimator(characterNode, contents, volume, duration);
        characterNode.setAnimator(processAnimator);

        // リスナ―設定：アニメーション終了のみ
        processAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }

            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {

                // ゴールしているなら、チャート処理はここで終了
                boolean isGoal = characterNode.isGoaled();
                if (isGoal) {
                    return;
                }

                Log.i("ループ処理", "処理アニメーション終了");

                //-------------------------------
                // アニメーション終了時の位置を保持
                //-------------------------------
                characterNode.setEndProcessAnimation(contents, volume);

                //==================================
//                characterNode.tmpCallFace();
                //==================================


                //-------------------------------
                // 次の処理へ
                //-------------------------------
                tranceNextBlock( characterNode );
            }
        });

        // 処理ブロックアニメーション開始
        processAnimator.start();

        //----------------------------------------
        // アニメーションの開始：モデルアニメーション用
        //----------------------------------------
        // モデルに用意されたアニメーションを開始
        characterNode.startModelAnimation(contents, duration);
    }


}
