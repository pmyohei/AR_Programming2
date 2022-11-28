package com.ar.ar_programming;

import android.animation.ValueAnimator;
import android.util.Log;

import com.ar.ar_programming.process.ProcessBlock;
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
    //----------------------------
    // アニメーションプロパティ名
    public static final String PROPERTY_WALK = "walk";
    public static final String PROPERTY_ROTATE = "rotate";
    public static final String PROPERTY_FORWARD = "forward";
    public static final String PROPERTY_BACK = "back";
    public static final String PROPERTY_RIGHT_ROTATE = "right_rotate";
    public static final String PROPERTY_LEFT_ROTATE = "left_rotate";

    // 移動1s当たりのアニメーション移動量
    public static final float MOVE_VOLUME_PER_SECOND = 0.08f;
    // 回転1度当たりのアニメーション時間
    public static final float ROTATE_TIME_PER_ANGLE = 0.01f;

    // 衝突検知の種別
    public static final int COLLISION_TYPE_NONE = -1;
    public static final int COLLISION_TYPE_GOAL = 0;
    public static final int COLLISION_TYPE_BLOCK = 1;
    public static final int COLLISION_TYPE_OBSTACLE = 2;

    //---------------------------
    // フィールド変数
    //---------------------------
    private Scene mScene;
    private Vector3 mEndPosition;
    private float mEndDegree;
    private ValueAnimator mAnimator;
    private int mCollisionType;

    // 衝突検知リスナー
    private CollisionDetectListener mCollisionDetectListenerListener;



    public CharacterNode(TransformationSystem transformationSystem) {
        super(transformationSystem);

        //----------------------------------------
        // アニメーション終了時の処理用保持用変数を初期化
        //----------------------------------------
        mEndPosition = new Vector3(0f,0f,0f);
        mEndDegree = 0f;
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
     * 衝突検知
     */
    private int detectCollision() {

        // 衝突検知種別
        int collisionType = COLLISION_TYPE_NONE;

        // 衝突の検証
        ArrayList<Node> nodes = mScene.overlapTestAll(this);
        for( Node collidingNode: nodes ){

            // 衝突したノードを判別
            String collidingNodeName = collidingNode.getName();

            if( collidingNodeName.equals( FirstFragment.NODE_NAME_GOAL ) ){

                Log.i("衝突検知", "衝突検出=ゴール");
                collisionType = COLLISION_TYPE_GOAL;
                break;

            } else if ( collidingNodeName.equals( FirstFragment.NODE_NAME_OBSTACLE ) ) {

                Log.i("衝突検知", "衝突検出=障害物");
                collisionType = COLLISION_TYPE_OBSTACLE;
                break;

            } else if ( collidingNodeName.equals( FirstFragment.NODE_NAME_BLOCK ) ) {

                Log.i("衝突検知", "衝突検出=ブロック");
                collisionType = COLLISION_TYPE_BLOCK;
                break;
            }
        }

        // 衝突したとき、リスナーをコール
        if( collisionType != COLLISION_TYPE_NONE ){
            mCollisionDetectListenerListener.onCollisionDetect( collisionType, mAnimator );
        }

        return collisionType;
    }


    /*
     * 前進／後退アニメーションメソッド
     *   ※プロパティ名：walk
     */
    public void setWalk(float volume) {

        // ブロックと衝突中は、処理なし
        if( mCollisionType == COLLISION_TYPE_BLOCK ){
            return;
        }

        //----------------------
        // 横と奥行の位置を更新
        //----------------------
        // 前回終了位置を加味して、設定値を取得
        float setX = (float)(mEndPosition.x + calcXvolume( volume ));
        float setZ = (float)(mEndPosition.z + calcZvolume( volume ));

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
        float setDegree = calcQuaternionLapDegree(mEndDegree, volume);
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
        return Math.sin( radian ) * volume;
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
        return Math.cos( radian ) * volume;
    }

    /*
     * 90度を0度したときのラジアンを取得
     */
    public double getFrontRadian() {

        // 正面を向いている状態が0度であるため、調整のために90度加算
        double degree = (mEndDegree + 90) % 360;
        return Math.toRadians( degree );
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
    public float calcQuaternionWvalue(float degree) {

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
        if( resultDegree > 360f ){
            // ベース角度と加算角度の合計値が360度を上回れば、その超過分を角度とする
            // 例）400度 → 40度
            resultDegree -= 360f;

        } else if ( resultDegree < 0 ){
            // ベース角度と加算角度の合計値が0度を下回れば、その分360から戻す
            // 例）-60度 → 300度
            resultDegree += 360;
        }

        return resultDegree;
    }

    /*
     * アニメーション終了時の変化後の値を保持
     */
    public void setAnimationEndValue( int processKind, float volume ) {

        // 衝突状態をクリア
        mCollisionType = COLLISION_TYPE_NONE;

        // 処理種別に応じた保存処理
        switch (processKind) {
            // 移動
            case ProcessBlock.PROC_KIND_FORWARD:
            case ProcessBlock.PROC_KIND_BACK:
                saveCurrentPosition();
                return;

            // 回転
            case ProcessBlock.PROC_KIND_RIGHT_ROTATE:
            case ProcessBlock.PROC_KIND_LEFT_ROTATE:
                saveCurrentAngle(volume);
                return;
        }
    }

    /*
     * 現在位置情報の保持
     */
    public void saveCurrentPosition() {
        // 現在位置情報を保持する
        mEndPosition = getLocalPosition();
    }

    /*
     * 現在角度の設定
     */
    public void saveCurrentAngle(float angle) {
        // 現在角度を設定
        mEndDegree = calcQuaternionLapDegree( mEndDegree, angle );
    }

    /*
     * 現在角度の初期化
     */
    public void initCurrentData( Vector3 position, float degree) {
        // 現在情報を設定
        mEndPosition = position;
        mEndDegree = degree;
    }

    /*
     * 処理種別に応じたメソッドのプロパティ名の取得
     */
    public static String getPropertyName(int procKind) {

        // 処理種別に応じたメソッドのプロパティ名を取得
        switch (procKind) {

            case ProcessBlock.PROC_KIND_FORWARD:
            case ProcessBlock.PROC_KIND_BACK:
                return PROPERTY_WALK;

            case ProcessBlock.PROC_KIND_RIGHT_ROTATE:
            case ProcessBlock.PROC_KIND_LEFT_ROTATE:
                return PROPERTY_ROTATE;
        }

        return PROPERTY_WALK;
    }

    /*
     * 処理ブロックに応じたアニメーション量を取得
     */
    public static float getAnimationVolume(int procKind, int procVolume) {

        //-----------------------------------------
        // 回転
        //-----------------------------------------
        // ブロックの処理量がそのままアニメーション量となる
        if ( procKind == ProcessBlock.PROC_KIND_LEFT_ROTATE ) {
            return (float) procVolume;
        }
        if ( procKind == ProcessBlock.PROC_KIND_RIGHT_ROTATE ) {
            return (float) procVolume * -1;
        }

        //-----------------------------------------
        // 前進／後退
        //-----------------------------------------
        // 処理時間からアニメーション量を算出
        float volume = (float) (procVolume * MOVE_VOLUME_PER_SECOND);
        if( procKind == ProcessBlock.PROC_KIND_BACK ){
            // 後退の場合、処理量はマイナス
            volume *= -1;
        }

        return volume;
    }

    /*
     * 処理ブロックに応じたアニメーション時間を取得
     */
    public static long getAnimationDuration(int procKind, int procVolume) {

        // 前進／後退の場合は、ブロックの処理量がそのままアニメーション時間
        if ((procKind == ProcessBlock.PROC_KIND_FORWARD) || (procKind == ProcessBlock.PROC_KIND_BACK)) {
            return (long) procVolume * 1000;
        }

        //-----------------------------------------
        // 回転の場合は、角度からアニメーション時間を算出
        //-----------------------------------------
        return (long) (procVolume * ROTATE_TIME_PER_ANGLE * 1000);
    }

    /*
     * 本NodeのアニメーションメソッドをコールするAnimatorを設定
     */
    public void setAnimator( ValueAnimator animator ) {
        mAnimator = animator;
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
