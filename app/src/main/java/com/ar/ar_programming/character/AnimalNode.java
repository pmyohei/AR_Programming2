package com.ar.ar_programming.character;

import static com.ar.ar_programming.process.SingleBlock.PROCESS_CONTENTS_ATTACK;
import static com.ar.ar_programming.process.SingleBlock.PROCESS_CONTENTS_BACK;
import static com.ar.ar_programming.process.SingleBlock.PROCESS_CONTENTS_EAT;
import static com.ar.ar_programming.process.SingleBlock.PROCESS_CONTENTS_FORWARD;
import static com.ar.ar_programming.process.SingleBlock.PROCESS_CONTENTS_LEFT_ROTATE;
import static com.ar.ar_programming.process.SingleBlock.PROCESS_CONTENTS_RIGHT_ROTATE;
import static com.ar.ar_programming.process.SingleBlock.PROCESS_CONTENTS_THROW_AWAY;

import com.ar.ar_programming.Gimmick;
import com.ar.ar_programming.GimmickManager;
import com.ar.ar_programming.R;
import com.google.ar.sceneform.ux.TransformationSystem;

import java.util.HashMap;

public class AnimalNode extends CharacterNode {

    // アニメーションプロパティ名
    public static final String PROPERTY_EAT = "eat";

    // モデルアニメーション名（Blendarで命名）
    public static final String MODEL_ANIMATION_STR_EAT = "eat";

    /*
     * コンストラクタ
     */
    public AnimalNode(TransformationSystem transformationSystem, Gimmick gimmick) {
        super(transformationSystem, gimmick);
    }

    /*
     * アクションワード紐づけ初期化
     */
    @Override
    public void initActionWords() {

        ACTION_CONTENTS_MAP = new HashMap<Integer, Integer>() {
            {
                put((Integer) ACTION_WAITING, R.string.action_wait);
                put((Integer) ACTION_SUCCESS, R.string.action_success);
                put((Integer) ACTION_FAILURE, R.string.action_failure);
                put((Integer) PROCESS_CONTENTS_FORWARD, R.string.action_walk_animal);
                put((Integer) PROCESS_CONTENTS_BACK, R.string.action_walk_animal);
                put((Integer) PROCESS_CONTENTS_RIGHT_ROTATE, R.string.action_rotate);
                put((Integer) PROCESS_CONTENTS_LEFT_ROTATE, R.string.action_rotate);
                put((Integer) PROCESS_CONTENTS_EAT, R.string.action_eat);
                put((Integer) PROCESS_CONTENTS_THROW_AWAY, R.string.action_throw_away);
                put((Integer) PROCESS_CONTENTS_ATTACK, R.string.action_attack);
            }
        };
    }

    /*
     * 処理種別に応じたメソッドのプロパティ名の取得
     */
    @Override
    public String getPropertyName(int procKind) {

        //-----------------
        // キャラクター共通か
        //-----------------
        String name = super.getPropertyName(procKind);
        if (!name.equals(PROPERTY_NONE)) {
            return name;
        }

        //-----------------
        // 本クラス固有か
        //-----------------
        // 処理種別に応じたメソッドのプロパティ名を取得
        switch (procKind) {
            case PROCESS_CONTENTS_EAT:
                return PROPERTY_EAT;

        }

        return PROPERTY_EAT;
    }

    /*
     * Blender側で命名された３Dアニメーション名を取得
     */
    @Override
    public String getModelAnimationName(int contents) {

        //-----------------
        // キャラクター共通か
        //-----------------
        String animationName = super.getModelAnimationName( contents );
        if ( !animationName.equals(MODEL_ANIMATION_STR_NONE) ) {
            return animationName;
        }

        //-----------------
        // 本クラス固有か
        //-----------------
        // 処理種別に応じたメソッドのプロパティ名を取得
        switch (contents) {
            case PROCESS_CONTENTS_EAT:
            default:
                animationName = MODEL_ANIMATION_STR_EAT;
                break;
        }

        return animationName;
    }


    /*
     * 食事アニメーションメソッド
     *   ※ プロパティ名：eat
     *   ※ 本メソッド内でアニメーションのための漸次的な処理はなし
     *      一定時間経過でブロック処理を実行させるために利用する。
     */
    public void setEat(float volume) {

        // 一定時間を超過したとき、ブロック処理を実行する
        if ((volume >= NO_VOLUME_START_VALUE) && !mfinishNoneVolume) {
            // 処理完了に
            mfinishNoneVolume = true;
            // 食べる
            eat();
        }
    }

    /*
     * 食べる
     */
    private void eat() {

        //----------------
        // 失敗判定
        //----------------
        int index = getCollisionIndex(GimmickManager.NODE_NAME_EATABLE);
        // 衝突中Nodeなし or 食べられないNodeと衝突中
        if ( (index == COLLISION_RET_NONE) || (index == COLLISION_RET_OTHRE) ) {
            // アクション失敗
            mSuccessAction = false;
            return;
        }

        //-----------------
        // 成功
        //-----------------
        // アクション成否：成功
        mSuccessAction = true;
        // アクション対象Nodeと衝突中なら、Sceneから削除
        removeNodeFromScene( index );
        // 衝突中Node情報クリア
        mCollisionNodeName = GimmickManager.NODE_NAME_NONE;
    }

    /*
     * 食べもの判定
     */
    public boolean isEatable() {
        return ( mCollisionNodeName.equals( GimmickManager.NODE_NAME_EATABLE ));
    }

    /*
     * 毒判定
     */
    public boolean isPoison() {
        return ( mCollisionNodeName.equals( GimmickManager.NODE_NAME_POISON));
    }

}
