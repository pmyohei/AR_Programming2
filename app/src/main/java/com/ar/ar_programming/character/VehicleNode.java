package com.ar.ar_programming.character;

import com.ar.ar_programming.Gimmick;
import com.ar.ar_programming.GimmickManager;
import com.ar.ar_programming.R;
import com.google.ar.sceneform.ux.TransformationSystem;

import java.util.HashMap;

public class VehicleNode extends CharacterNode {

    /*
     * コンストラクタ
     */
    public VehicleNode(TransformationSystem transformationSystem, Gimmick gimmick) {
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
                put( GimmickManager.BLOCK_EXE_FORWARD, R.string.action_walk_vehicle);
                put( GimmickManager.BLOCK_EXE_BACK, R.string.action_walk_vehicle);
                put( GimmickManager.BLOCK_EXE_ROTATE_RIGHT, R.string.action_rotate);
                put( GimmickManager.BLOCK_EXE_ROTATE_LEFT, R.string.action_rotate);
                put( GimmickManager.BLOCK_EXE_THROW_AWAY, R.string.action_throw_away);
                put( GimmickManager.BLOCK_EXE_PICKUP, R.string.action_pickup);
            }
        };
    }
}
