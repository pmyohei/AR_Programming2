package com.ar.ar_programming.character;

import static com.ar.ar_programming.process.SingleBlock.PROCESS_CONTENTS_BACK;
import static com.ar.ar_programming.process.SingleBlock.PROCESS_CONTENTS_FORWARD;
import static com.ar.ar_programming.process.SingleBlock.PROCESS_CONTENTS_LEFT_ROTATE;
import static com.ar.ar_programming.process.SingleBlock.PROCESS_CONTENTS_RIGHT_ROTATE;
import static com.ar.ar_programming.process.SingleBlock.PROCESS_CONTENTS_THROW_AWAY;

import com.ar.ar_programming.Gimmick;
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

        ACTION_CONTENTS_MAP = new HashMap<Integer, Integer>() {
            {
                put((Integer) ACTION_WAITING, R.string.action_wait);
                put((Integer) ACTION_SUCCESS, R.string.action_success);
                put((Integer) ACTION_FAILURE, R.string.action_failure);
                put((Integer) PROCESS_CONTENTS_FORWARD, R.string.action_walk_vehicle);
                put((Integer) PROCESS_CONTENTS_BACK, R.string.action_walk_vehicle);
                put((Integer) PROCESS_CONTENTS_RIGHT_ROTATE, R.string.action_rotate);
                put((Integer) PROCESS_CONTENTS_LEFT_ROTATE, R.string.action_rotate);
                put((Integer) PROCESS_CONTENTS_THROW_AWAY, R.string.action_throw_away);
            }
        };
    }
}
