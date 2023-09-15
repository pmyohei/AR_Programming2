package com.ar.ar_programming.character;


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

        ACTION_CONTENTS_MAP = new HashMap<String, Integer>() {
            {
                put( ACTION_WAITING, R.string.action_wait);
                put( ACTION_SUCCESS, R.string.action_success);
                put( ACTION_FAILURE, R.string.action_failure);
                put( GimmickManager.BLOCK_EXE_FORWARD, R.string.action_walk_animal);
                put( GimmickManager.BLOCK_EXE_BACK, R.string.action_walk_animal);
                put( GimmickManager.BLOCK_EXE_ROTATE_RIGHT, R.string.action_rotate);
                put( GimmickManager.BLOCK_EXE_ROTATE_LEFT, R.string.action_rotate);
                put( GimmickManager.BLOCK_EXE_EAT, R.string.action_eat);
                put( GimmickManager.BLOCK_EXE_THROW_AWAY, R.string.action_throw_away);
                put( GimmickManager.BLOCK_EXE_ATTACK, R.string.action_attack);
            }
        };
    }

    /*
     * 処理種別に応じたメソッドのプロパティ名の取得
     */
    @Override
    public String getMethodPropertyName(String contents) {

        //-----------------
        // キャラクター共通か
        //-----------------
        String name = super.getMethodPropertyName(contents);
        if (!name.equals(PROPERTY_NONE)) {
            return name;
        }

        //-----------------
        // 本クラス固有か
        //-----------------
        // 処理種別に応じたメソッドのプロパティ名を取得
        switch (contents) {
            case GimmickManager.BLOCK_EXE_EAT:
                return PROPERTY_EAT;

        }

        return PROPERTY_EAT;
    }

    /*
     * Blender側で命名された３Dアニメーション名を取得
     */
    @Override
    public String getModelAnimationName(String contents) {

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
            case GimmickManager.BLOCK_EXE_EAT:
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
        String targetCollisionNode = GimmickManager.NODE_NAME_EATABLE;
        deleteNodeAction( targetCollisionNode );
    }
}
