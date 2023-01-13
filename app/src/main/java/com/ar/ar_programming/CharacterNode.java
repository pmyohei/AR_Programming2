package com.ar.ar_programming;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.util.Log;

import com.ar.ar_programming.process.SingleProcessBlock;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.ux.TransformationSystem;

import java.util.ArrayList;

public class CharacterNode extends TransformableNode {

    //---------------------------
    // 定数
    //---------------------------
    // アニメーションプロパティ名
    public static final String PROPERTY_WALK = "walk";
    public static final String PROPERTY_ROTATE = "rotate";
    public static final String PROPERTY_FORWARD = "forward";
    public static final String PROPERTY_BACK = "back";
    public static final String PROPERTY_RIGHT_ROTATE = "right_rotate";
    public static final String PROPERTY_LEFT_ROTATE = "left_rotate";

    // モデルアニメーション名（Blendarで命名）
    public static final String MODEL_ANIMATION_STR_GOAL = "goal";
    public static final String MODEL_ANIMATION_STR_WALK = "walk";
    public static final String MODEL_ANIMATION_STR_ROTATE_LEFT = "rotate_left";
    public static final String MODEL_ANIMATION_STR_ROTATE_RIGHT = "rotate_right";
    public static final String MODEL_ANIMATION_STR_ERROR = "error";

    // 移動1s当たりのアニメーション移動量
//    public static final float MOVE_VOLUME_PER_SECOND = 0.08f;
    // 移動1cm当たりのアニメーション時間(ms)
    public static final float WALK_TIME_PER_CM = 250f;
    // 回転1度当たりのアニメーション時間(ms)
    public static final float ROTATE_TIME_PER_ANGLE = 10f;

    // 衝突検知の種別
    public static final int COLLISION_TYPE_NONE = -1;
    public static final int COLLISION_TYPE_GOAL = 0;
    public static final int COLLISION_TYPE_BLOCK = 1;
    public static final int COLLISION_TYPE_OBSTACLE = 2;

    //---------------------------
    // フィールド変数
    //---------------------------
    private Scene mScene;
    private int mCollisionType;
    // 初期位置の情報
    private Vector3 mStartPosition;
    private float mStartDegree;
    // 処理ブロックアニメーション終了時点の情報
    private Vector3 mCurrentPosition;
    private float mCurrentDegree;           // ※キャラクターが下を向いている角度（一般的には２７０度方向）を０度とする
    // 処理ブロック用アニメーション
    private ValueAnimator mProcessAnimator;
    // モデルアニメーションの開始と終了を制御する用
    private ObjectAnimator mModelAnimator;
    // 衝突検知リスナー
    private CollisionDetectListener mCollisionDetectListenerListener;

    //tmp
    Node mptGoal;


    public CharacterNode(TransformationSystem transformationSystem) {
        super(transformationSystem);

        //----------------------------------------
        // アニメーション終了時の処理用保持用変数を初期化
        //----------------------------------------
        mCurrentPosition = new Vector3(0f, 0f, 0f);
        mCurrentDegree = 0f;
        mCollisionType = COLLISION_TYPE_NONE;
    }

    /*
     * アニメーション初期化処理
     *   Sceneを保持していなければ保持する
     * 　※アニメーション前に必ずコールすること
     */
    public void initAnimation() {

        if (mScene != null) {
            return;
        }
        mScene = getScene();
    }

    /*
     * tmp ゴールノード取得
     */
    public void tmpsetGoalNode(Node goal) {
        mptGoal = goal;
    }


    /*
     * 衝突検知
     */
    private int detectCollision() {

        // 衝突検知種別
        int collisionType = COLLISION_TYPE_NONE;

        // 衝突の検証
        ArrayList<Node> nodes = mScene.overlapTestAll(this);
        for (Node collidingNode : nodes) {

            // 衝突したノードを判別
            String collidingNodeName = collidingNode.getName();

            if (collidingNodeName.equals(FirstFragment.NODE_NAME_GOAL)) {
                collisionType = COLLISION_TYPE_GOAL;
                startModelAnimation(MODEL_ANIMATION_STR_GOAL, 2000);
                break;

            } else if (collidingNodeName.equals(FirstFragment.NODE_NAME_OBSTACLE)) {
                collisionType = COLLISION_TYPE_OBSTACLE;
                startModelAnimation(MODEL_ANIMATION_STR_ERROR, 2000);
                break;

            } else if (collidingNodeName.equals(FirstFragment.NODE_NAME_BLOCK)) {

                collisionType = COLLISION_TYPE_BLOCK;
                break;
            }
        }

        // 衝突したとき、リスナーをコール
        if (collisionType != COLLISION_TYPE_NONE) {
            mCollisionDetectListenerListener.onCollisionDetect(collisionType, mProcessAnimator);
        }

        return collisionType;
    }


    /*
     * 前進／後退アニメーションメソッド
     *   ※プロパティ名：walk
     */
    public void setWalk(float volume) {

        // ブロックと衝突中は、処理なし
        if (mCollisionType == COLLISION_TYPE_BLOCK) {
            return;
        }

        //----------------------
        // 横と奥行の位置を更新
        //----------------------
        // 前回終了位置を加味して、設定値を取得
        float setX = (float) (mCurrentPosition.x + calcXvolume(volume));
        float setZ = (float) (mCurrentPosition.z + calcZvolume(volume));

        // 位置を更新
        Vector3 vec3 = getLocalPosition();
        vec3.x = setX;
        vec3.z = setZ;
        setLocalPosition(vec3);

        //----------------------
        // 衝突検知
        //----------------------
        mCollisionType = detectCollision();
    }


    /*
     * 回転アニメーションメソッド
     * ※ プロパティ名：rotate
     *
     * ※ Quaternionの角度におけるw/y値参考
     *         ： w    y
     *      0度：1.0  0.0
     *    180度：0.0  1.0
     *    360度：-1.0 0.0
     *
     *    ⇒360度で値を一巡
     *
     * ※ q.set(new Vector3(0f, y, 0f), setDegree); は使用しない
     *    １周するタイミングで一度停止する状態になるため（原因不明）
     */
    public void setRotate(float volume) {

        //----------------------------------
        // 設定情報の算出
        //----------------------------------
        // 設定角：前回の終了時の角度を加味
        float setDegree = calcQuaternionLapDegree(mCurrentDegree, volume);
        // Quaternionのy/wの値を算出
        float w = calcQuaternionWvalue(setDegree);
        float y = calcQuaternionYvalue(setDegree);

        //----------------------------------
        // 角度を反映
        //----------------------------------
        Quaternion q = getLocalRotation();
        q.set(0f, y, 0f, w);
        setLocalRotation(q);

//        Log.i("回転アニメーション", "volume=\t" + volume + "\tsetDegree=\t" + setDegree + "\ty=\t" + y);
//        Log.i("回転アニメーション", "setDegree=" + setDegree);
//        Log.i("回転アニメーション", "y=" + y);
    }

    /*
     * z軸（奥行）の位置を取得
     */
    public double calcZvolume(float volume) {

        // 本ノードが向いている角度のラジアンを取得
        double radian = getFrontRadian();
        // z軸（奥行）の位置を計算
        return Math.sin(radian) * volume;
    }

    /*
     * x軸（横軸）の位置を取得
     */
    public double calcXvolume(float volume) {

        // 奥側への移動量はマイナス値であるため、正値にして計算
        volume *= -1;

        // 本ノードが向いている角度のラジアンを取得
        double radian = getFrontRadian();
        // x軸（横軸）の位置を計算
        return Math.cos(radian) * volume;
    }

    /*
     * 90度を0度したときのラジアンを取得
     */
    private double getFrontRadian() {

        // 正面を向いている状態が0度であるため、調整のために90度加算
        double degree = (mCurrentDegree + 90) % 360;
        return Math.toRadians(degree);
    }

    /*
     * 指定角度におけるQuaternionのY値を計算する
     */
    public static float calcQuaternionYvalue(float degree) {

        degree %= 360f;

/*
        if (degree <= 180) {
            // 0 ~ 180 ： 0.0 ～ 1.0
            return (degree / 180f);

        } else if (degree <= 360) {
            // 180 ~ 360 ： 1.0 ～ 0.0
            degree -= 180;
            return (1 - (degree / 180f));

        } else if (degree <= 540) {
            // 360 ~ 540 ： 0.0 ～ -1.0
            degree -= 360;
            return -(degree / 180f);

        } else {
            // 540 ~ 720 ： -1.0 ～ 0.0
            degree -= 540;
            return -(1 - (degree / 180f));
        }
*/

        if (degree <= 180) {
            // 0 ~ 180 ： 0.0 ～ 1.0
            return (degree / 180f);

        } else {
            // 180 ~ 360 ： 1.0 ～ 0.0
            degree -= 180;
            return (1 - (degree / 180f));

        }
    }

    /*
     * 指定角度におけるQuaternionのW値を計算する
     */
    public static float calcQuaternionWvalue(float degree) {

        degree %= 360f;

/*
        if (degree <= 180) {
            // 0 ~ 180 ： 1.0 ～ 0.0
            return (1 - (degree / 180f));

        } else if (degree <= 360) {
            // 180 ~ 360 ： 0.0 ～ -1.0
            degree -= 180;
            return -(degree / 180f);

        } else if (degree <= 540) {
            // 360 ~ 540 ： -1.0 ～ 0.0
            degree -= 360;
            return -(1 - (degree / 180f));

        } else {
            // 540 ~ 720 ： 0.0 ～ 1.0
            degree -= 540;
            return (degree / 180f);
        }
*/

        if (degree <= 180) {
            // 0 ~ 180 ： 1.0 ～ 0.0
            return (1 - (degree / 180f));
        } else {
            // 180 ~ 360 ： 0.0 ～ -1.0
            degree -= 180;
            return -(degree / 180f);
        }
    }

    /*
     * Quaternionにおける１周を加味した角度を取得する
     *   ※１周：0度 ~ 360度 の範囲内で角度を丸める
     */
    public float calcQuaternionLapDegree(float baseDegree, float addDegree) {

        float resultDegree = baseDegree + addDegree;
        if (resultDegree > 360f) {
            // ベース角度と加算角度の合計値が360度を上回れば、その超過分を角度とする
            // 例）400度 → 40度
            resultDegree -= 360f;

        } else if (resultDegree < 0) {
            // ベース角度と加算角度の合計値が0度を下回れば、その分360から戻す
            // 例）-60度 → 300度
            resultDegree += 360;
        }

        return resultDegree;
    }

    /*
     * 処理ブロックアニメーション終了処理
     */
    public void setEndProcessAnimation(int processKind, float volume) {

        // 衝突状態をクリア
        mCollisionType = COLLISION_TYPE_NONE;

        Log.i("startModelAnimation", "処理ブロックアニメーション終了処理");

        // アニメーション終了時の変化後の値を保持
        setAnimationEndValue(processKind, volume);
    }

    /*
     * アニメーション終了時の変化後の値を保持
     */
    public void setAnimationEndValue(int processKind, float volume) {

        // 処理種別に応じた保存処理
        switch (processKind) {
            // 移動
            case SingleProcessBlock.PROCESS_CONTENTS_FORWARD:
            case SingleProcessBlock.PROCESS_CONTENTS_BACK:
                saveCurrentPosition();
                return;

            // 回転
            case SingleProcessBlock.PROCESS_CONTENTS_RIGHT_ROTATE:
            case SingleProcessBlock.PROCESS_CONTENTS_LEFT_ROTATE:
                saveCurrentAngle(volume);
                return;
        }
    }

    /*
     * 現在位置情報の保持
     */
    public void saveCurrentPosition() {
        // 現在位置情報を保持する
        mCurrentPosition = getLocalPosition();
    }

    /*
     * 現在角度の設定
     */
    private void saveCurrentAngle(float angle) {
        // 現在角度を保持
        mCurrentDegree = calcQuaternionLapDegree(mCurrentDegree, angle);
    }

    /*
     * 開始位置の保持
     */
    public void startPosData(Vector3 position, float degree) {
        // 開始位置を保持
        mStartPosition = position;
        mStartDegree = degree;
        // 開始位置を現在情報として設定
        mCurrentPosition = position;
        mCurrentDegree = degree;
    }

    /*
     * 本キャラクターを初期位置にリセット
     */
    public void positionReset() {

        //----------------------------------
        // 位置を初期位置に戻す
        //----------------------------------
        setLocalPosition(mStartPosition);

        //----------------------------------
        // 角度を初期位置に戻す
        //----------------------------------
        // Quaternionのy/wの値を算出
        float w = calcQuaternionWvalue(mCurrentDegree);
        float y = calcQuaternionYvalue(mCurrentDegree);
        // Quaternion生成
        Quaternion q = getLocalRotation();
        q.set(0f, y, 0f, w);
        // 角度の設定
        setLocalRotation(q);

        //----------------------------------
        // 終了情報をリセット
        //----------------------------------
        // 開始位置を現在情報として設定
        mCurrentPosition = mStartPosition;
        mCurrentDegree = mStartDegree;
    }

    /*
     * 処理種別に応じたメソッドのプロパティ名の取得
     */
    public static String getPropertyName(int procKind) {

        // 処理種別に応じたメソッドのプロパティ名を取得
        switch (procKind) {
            case SingleProcessBlock.PROCESS_CONTENTS_FORWARD:
            case SingleProcessBlock.PROCESS_CONTENTS_BACK:
                return PROPERTY_WALK;

            case SingleProcessBlock.PROCESS_CONTENTS_RIGHT_ROTATE:
            case SingleProcessBlock.PROCESS_CONTENTS_LEFT_ROTATE:
                return PROPERTY_ROTATE;
        }

        return PROPERTY_WALK;
    }

    /*
     * 処理ブロックに応じたアニメーション量を取得
     */
    public float getAnimationVolume(int procKind, int procVolume) {

        //-----------------------------------------
        // 前進／後退
        //-----------------------------------------
        // 単位変換：cm → m
        if (procKind == SingleProcessBlock.PROCESS_CONTENTS_FORWARD) {
            return (float) procVolume / 100f;
        }
        if (procKind == SingleProcessBlock.PROCESS_CONTENTS_BACK) {
            return (procVolume / 100f) * -1;
        }

        //-----------------------------------------
        // 回転
        //-----------------------------------------
        if (procKind == SingleProcessBlock.PROCESS_CONTENTS_LEFT_ROTATE) {
            return (float) procVolume;
        }

        return (float) procVolume * -1;

        //-----------------------------------------
        // 回転
        //-----------------------------------------
        // ブロックの処理量がそのままアニメーション量となる
/*        if (procKind == ProcessBlock.PROC_KIND_LEFT_ROTATE) {
            return (float) procVolume;
        }
        if (procKind == ProcessBlock.PROC_KIND_RIGHT_ROTATE) {
            return (float) procVolume * -1;
        }*/

        //-----------------------------------------
        // 前進／後退
        //-----------------------------------------
        // ブロックの処理量がそのままアニメーション量となる
/*
        if (procKind == ProcessBlock.PROC_KIND_FORWARD) {
            return (float) procVolume;
        }
        if (procKind == ProcessBlock.PROC_KIND_BACK) {
            return (float) procVolume * -1;
        }

        return (float) procVolume;
*/

/*        // 処理時間からアニメーション量を算出
        float volume = (float) (procVolume * MOVE_VOLUME_PER_SECOND);
        if (procKind == ProcessBlock.PROC_KIND_BACK) {
            // 後退の場合、処理量はマイナス
            volume *= -1;
        }
        return volume;
*/
    }

    /*
     * 処理ブロックに応じたアニメーション時間を取得
     */
    public long getAnimationDuration(int procKind, int procVolume) {

        //-------------------
        // 前進／後退
        //-------------------
        if ((procKind == SingleProcessBlock.PROCESS_CONTENTS_FORWARD) || (procKind == SingleProcessBlock.PROCESS_CONTENTS_BACK)) {
            // スケールに応じた処理時間に変換
            Vector3 scale = getLocalScale();
            float ratio = (FirstFragment.NODE_SIZE_S * FirstFragment.NODE_SIZE_TMP_RATIO) / scale.x;

            Log.i("歩行", "scale.x=" + scale.x);
            Log.i("歩行", "ratio=" + ratio);
            Log.i("歩行", "WALK_TIME_PER_CM=" + (WALK_TIME_PER_CM * ratio));

            return (long) (procVolume * WALK_TIME_PER_CM * ratio);
        }

        //-------------------
        // 回転
        //-------------------
        return (long) (procVolume * ROTATE_TIME_PER_ANGLE);

/*
        // 前進／後退の場合は、ブロックの処理量がそのままアニメーション時間
        if ((procKind == ProcessBlock.PROC_KIND_FORWARD) || (procKind == ProcessBlock.PROC_KIND_BACK)) {
//            return (long) procVolume * MILL_SECOND;
            return (long)(procVolume * WALK_TIME_PER_ANGLE);
        }
*/

        //-----------------------------------------
        // 回転の場合は、角度からアニメーション時間を算出
        //-----------------------------------------
//        return (long)(procVolume * ROTATE_TIME_PER_ANGLE);
    }

    /*
     * 本NodeのアニメーションメソッドをコールするAnimatorを設定
     */
    public void setAnimator(ValueAnimator animator) {
        mProcessAnimator = animator;
    }


    /*
     * モデルアニメーションの開始
     */
    public void startModelAnimation(String animationName, long duration) {

        // アニメーション中なら、終了
        if ((mModelAnimator != null) && (mModelAnimator.isStarted())) {
            mModelAnimator.end();
        }

        // モデルアニメーション開始
        mModelAnimator = getRenderableInstance().animate(animationName);
        mModelAnimator.setDuration(duration);
        mModelAnimator.setRepeatCount(0);
        mModelAnimator.start();

        Log.i("startModelAnimation", "animationName=" + animationName);
    }

    /*
     * 3Dモデルアニメーションの開始
     *   Blender側で命名された３Dアニメーション名を取得
     */
    public void startModelAnimation(int procKind, long duration) {

        String animationName = "";

        // アニメーション名を取得
        switch (procKind) {
            case SingleProcessBlock.PROCESS_CONTENTS_FORWARD:
            case SingleProcessBlock.PROCESS_CONTENTS_BACK:
                animationName = MODEL_ANIMATION_STR_WALK;
                break;

            case SingleProcessBlock.PROCESS_CONTENTS_RIGHT_ROTATE:
                animationName = MODEL_ANIMATION_STR_ROTATE_RIGHT;
                break;

            case SingleProcessBlock.PROCESS_CONTENTS_LEFT_ROTATE:
                animationName = MODEL_ANIMATION_STR_ROTATE_LEFT;
                break;
        }

        // モデルアニメーション開始
        startModelAnimation(animationName, duration);
    }

    /*
     * 衝突検知リスナーの設定
     */
    public boolean isGoaled() {
        return (mCollisionType == COLLISION_TYPE_GOAL);
    }


    /*
     * 本キャラクターが指定Nodeの方向を向いているかどうかを判定
     */
    public boolean isFacingToNode(Node node) {

        //---------------------
        // 角度情報
        //---------------------
        // キャラクターとNodeを結ぶ直線の傾きから角度を算出
        double toNodeDegree = calcDegreeFromSlopeToNode(node);
        // キャラクターが向いている方向（角度）
        double characterFacingDegree = calcDegreeCharacterFacing();

        //------------------------
        // Nodeを向いているかどうか判定
        //------------------------
        // 判定範囲
        // （キャラクターの向いている角度の±0.5度以内なら向いていると判定する）
        double minRange = characterFacingDegree - 0.5;
        double maxRange = characterFacingDegree + 0.5;

        // 判定範囲内なら、向いているとみなす
        if( ( minRange <= toNodeDegree ) && ( toNodeDegree <= maxRange ) ){
//            Log.i("向いている方向ロジック", "〇 傾きからの角度=" + toNodeDegree);
//            Log.i("向いている方向ロジック", "〇 キャラの向き=" + characterFacingDegree);
            return true;
        } else {
//            Log.i("向いている方向ロジック", "× 傾きからの角度=" + toNodeDegree);
//            Log.i("向いている方向ロジック", "× キャラの向き=" + characterFacingDegree);
            return false;
        }
    }

    /*
     * 本キャラクターノードと指定Nodeを結ぶ直線の傾きから角度を算出
     * 　@return：角度範囲（－180～180）
     * 　　　　　　※例）270度に相当する角度は、「-90度」で返す
     */
    private double calcDegreeFromSlopeToNode(Node node) {

        //-------------------------
        // キャラクターとNodeの位置情報
        //-------------------------
        // キャラクター位置
        Vector3 selfPos = getLocalPosition();
        float selfPosX = selfPos.x;
        float selfPosZ = selfPos.z;
        // Node位置
        Vector3 nodePos = node.getLocalPosition();
        float nodePosX = nodePos.x;
        float nodePosZ = nodePos.z;

        // 奥く方向を正としたいため、符号を反転（※sceneformでは、奥がマイナス方向）
        selfPosZ *= -1;
        nodePosZ *= -1;

        Log.i("向いている方向ロジック", "座標 Char=" + selfPosX + ", " + selfPosZ);
        Log.i("向いている方向ロジック", "座標 Node=" + nodePosX + ", " + nodePosZ);

        //-------------------------
        // 角度算出
        //-------------------------
        // 2点を結ぶ直線のラジアンを取得
        double radian = Math.atan2((nodePosZ - selfPosZ), (nodePosX - selfPosX));
        // ラジアン⇒角度にして返す
        return Math.toDegrees(radian);
    }


    /*
     * 本キャラクターノードが向いている角度を返す
     * 　@return：角度範囲（－180～180）
     * 　　　　　　※例）270度に相当する角度は、「-90度」で返す
     * 　　　　　　※キャラクターが右を向いている状態：「0度」
     * 　　　　　　※キャラクターが奥を向いている状態：「90度」
     */
    private double calcDegreeCharacterFacing() {

        // キャラクターが右方向を向いている状態を0度として扱いたいため、保持中の角度を調整
        double degree = mCurrentDegree - 90;

        // 180度以下なら、その値をそのまま返す
        if( degree <= 180 ){
            return degree;
        }

        // 180度超過分は、「0度～-180度」の範囲に変換して返す
        return degree - 360;
    }

    /*
     * 衝突検知リスナーの設定
     */
    public void setOnCollisionDetectListener( CollisionDetectListener listener ) {
        mCollisionDetectListenerListener = listener;
    }

    /*
     * クリック検出用インターフェース
     */
    public interface CollisionDetectListener {
        // 衝突検知リスナー
        void onCollisionDetect( int collisionType, ValueAnimator animator );
    }
}
