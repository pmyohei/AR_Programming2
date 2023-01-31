package com.ar.ar_programming;

import static com.ar.ar_programming.ArMainFragment.PROGRAMMING_END_ACTION_FAILURE;
import static com.ar.ar_programming.ArMainFragment.PROGRAMMING_END_ALL_DONE;
import static com.ar.ar_programming.ArMainFragment.PROGRAMMING_END_GOAL;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.util.Log;

import com.ar.ar_programming.process.ProcessBlock;
import com.ar.ar_programming.process.SingleBlock;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.NodeParent;
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
    public static final String PROPERTY_EAT = "eat";
    public static final String PROPERTY_THROW_AWAY = "throwAway";

    // モデルアニメーション名（Blendarで命名）
    public static final String MODEL_ANIMATION_STR_GOAL = "goal";
    public static final String MODEL_ANIMATION_STR_WALK = "walk";
    public static final String MODEL_ANIMATION_STR_ROTATE_LEFT = "rotate_left";
    public static final String MODEL_ANIMATION_STR_ROTATE_RIGHT = "rotate_right";
    public static final String MODEL_ANIMATION_STR_EAT = "eat";
    public static final String MODEL_ANIMATION_STR_THROW_AWAY = "throwAway";
    public static final String MODEL_ANIMATION_STR_ERROR = "error";

    // 移動1cm当たりのアニメーション時間(ms)
    private final float WALK_TIME_PER_CM = 100f;
    // 回転1度当たりのアニメーション時間(ms)
    private final float ROTATE_TIME_PER_ANGLE = 10f;

    // 処理量のない処理の仮量と仮時間(ms)
    private final float NO_VOLUME_TIME = 3000f;
    private final float NO_VOLUME_VALUE = 1000f;
    private final float NO_VOLUME_START_RATIO = 0.6f;
    private final float NO_VOLUME_START_VALUE = NO_VOLUME_VALUE * NO_VOLUME_START_RATIO;

    // 指定Node衝突判定結果
    private final int COLLISION_RET_NONE = -1;       // （ステージを除いて）衝突中Nodeなし
    private final int COLLISION_RET_OTHRE = -2;      // （ステージを除いて）指定Node以外と衝突中

    //---------------------------
    // フィールド変数
    //---------------------------
    private Scene mScene;
    // 衝突中のNode
    private String mCollisionNodeName;
    // 衝突検知リスナー
    private CollisionDetectListener mCollisionDetectListener;
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
    // 処理量なしのアクションメソッド完了フラグ
    private boolean mfinishNoneVolume;
    // アクション成否
    private boolean mSuccessAction;


    //tmp
    Node mptGoal;


    public CharacterNode(TransformationSystem transformationSystem) {
        super(transformationSystem);

        //----------------------------------------
        // アニメーション終了時の処理用保持用変数を初期化
        //----------------------------------------
        mCurrentPosition = new Vector3(0f, 0f, 0f);
        mCurrentDegree = 0f;
        mCollisionNodeName = GimmickManager.NODE_NAME_NONE;
        mfinishNoneVolume = false;          // 処理量なしアクション：未完了
        mSuccessAction = true;              // アクション成否：成功
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
    private String detectCollision() {

        // 衝突の有無
        String collisionNode = GimmickManager.NODE_NAME_NONE;

        //------------
        // 衝突の検証
        //------------
        ArrayList<Node> nodes = mScene.overlapTestAll(this);
        for (Node collidingNode : nodes) {

            // 衝突中のノード名
            collisionNode = collidingNode.getName();

            if (collisionNode.equals(GimmickManager.NODE_NAME_GOAL)) {
                startModelAnimation(MODEL_ANIMATION_STR_GOAL, 2000);
                break;

            } else if (collisionNode.equals(GimmickManager.NODE_NAME_OBSTACLE)) {
                startModelAnimation(MODEL_ANIMATION_STR_ERROR, 2000);
                break;

            } else if (collisionNode.equals(GimmickManager.NODE_NAME_EATABLE)) {
                startModelAnimation(MODEL_ANIMATION_STR_ERROR, 2000);
                break;

            } else if (collisionNode.equals(GimmickManager.NODE_NAME_THROW_AWAY)) {
                startModelAnimation(MODEL_ANIMATION_STR_ERROR, 2000);
                break;
            }

            // 衝突中ノードクリア
            collisionNode = GimmickManager.NODE_NAME_NONE;
        }

        //----------------
        // 衝突リスナーコール
        //----------------
        mCollisionNodeName = collisionNode;

        // リスナーコール判定
        if (!collisionNode.equals(GimmickManager.NODE_NAME_NONE)) {
            mCollisionDetectListener.onCollisionDetect(collisionNode, mProcessAnimator);
        }

        return collisionNode;
    }


    /*
     * 前進／後退アニメーションメソッド
     *   ※プロパティ名：walk
     */
    public void setWalk(float volume) {

        // 衝突中は、処理なし
        if (!mCollisionNodeName.equals(GimmickManager.NODE_NAME_NONE)) {
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
//        mCollisionNodeName = detectCollision();
        detectCollision();
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
     * 捨てるアニメーションメソッド
     *   ※ プロパティ名：throwAway
     *   ※ 本メソッド内でアニメーションのための漸次的な処理はなし
     *      一定時間経過でブロック処理を実行させるために利用する。
     */
    public void setThrowAway(float volume) {

        // 一定時間を超過したとき、ブロック処理を実行する
        if ((volume >= NO_VOLUME_START_VALUE) && !mfinishNoneVolume) {
            // 処理完了に
            mfinishNoneVolume = true;
            // 捨てる
            throwAway();
        }
    }


    /*
     * 食べる
     */
    private void eat() {

        // アクション成否：成功
        mSuccessAction = true;

        //--------------------------------
        // 衝突中の食事可能NodeをSceneから削除
        //--------------------------------
        int index = getCollisionIndex(GimmickManager.NODE_NAME_EATABLE);
        // 衝突中Nodeなし
        if (index == COLLISION_RET_NONE) {
            return;
        }

        // アクション対象外Nodeと衝突中
        if (index == COLLISION_RET_OTHRE) {
            // アクション失敗
            mSuccessAction = false;
            return;
        }

        // アクション対象外Nodeと衝突中なら、Sceneから削除
        removeNodeFromScene( index );
    }

    /*
     * 捨てる
     */
    private void throwAway() {

        // アクション成否：成功
        mSuccessAction = true;

        //--------------------------------
        // 衝突中の捨てる対象NodeをSceneから削除
        //--------------------------------
        int index = getCollisionIndex(GimmickManager.NODE_NAME_THROW_AWAY);
        // 衝突中Nodeなし
        if (index == COLLISION_RET_NONE) {
            return;
        }

        // アクション対象外Nodeと衝突中
        if (index == COLLISION_RET_OTHRE) {
            // アクション失敗
            mSuccessAction = false;
            return;
        }

        // アクション対象外Nodeと衝突中なら、Sceneから削除
        removeNodeFromScene( index );
    }

    /*
     * SceneからNodeを削除
     */
    private void removeNodeFromScene(int index) {
        ArrayList<Node> nodes = mScene.overlapTestAll(this);
        Node deleteNode = nodes.get(index);
        NodeParent parent = deleteNode.getParent();
        parent.removeChild(deleteNode);
    }

    /*
     * （ステージを除いた）衝突中判定
     *   指定Nodeと衝突しているか判定する
     */
    private int getCollisionIndex(String nodeName ) {

        ArrayList<Node> nodes = mScene.overlapTestAll(this);
        for( int i = 0; i < nodes.size(); i++ ){

            String overlapNode = nodes.get(i).getName();

            if (overlapNode.equals(GimmickManager.NODE_NAME_STAGE)) {
                // ステージの場合は、次のNodeをチェック

            } else if ( overlapNode.equals(nodeName) ) {
                // 指定Nodeと衝突している場合、indexを返す
                return i;

            } else {
                // 「ステージ、指定Node」以外と衝突している場合
                return COLLISION_RET_OTHRE;
            }
        }

        // (ステージを除いて)衝突中Nodeなし
        return COLLISION_RET_NONE;
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

        // 状態クリア
        mCollisionNodeName = GimmickManager.NODE_NAME_NONE;
        mfinishNoneVolume = false;          // 処理量なしアクション：未完了
        mSuccessAction = true;              // アクション成否：成功

        Log.i("成功判定", "処理ブロックアニメーション終了処理");

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
            case SingleBlock.PROCESS_CONTENTS_FORWARD:
            case SingleBlock.PROCESS_CONTENTS_BACK:
                saveCurrentPosition();
                return;

            // 回転
            case SingleBlock.PROCESS_CONTENTS_RIGHT_ROTATE:
            case SingleBlock.PROCESS_CONTENTS_LEFT_ROTATE:
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
            case SingleBlock.PROCESS_CONTENTS_FORWARD:
            case SingleBlock.PROCESS_CONTENTS_BACK:
                return PROPERTY_WALK;

            case SingleBlock.PROCESS_CONTENTS_RIGHT_ROTATE:
            case SingleBlock.PROCESS_CONTENTS_LEFT_ROTATE:
                return PROPERTY_ROTATE;

            case SingleBlock.PROCESS_CONTENTS_EAT:
                return PROPERTY_EAT;

            case SingleBlock.PROCESS_CONTENTS_THROW_AWAY:
                return PROPERTY_THROW_AWAY;
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
        if (procKind == SingleBlock.PROCESS_CONTENTS_FORWARD) {
            return (float) procVolume / 100f;
        }
        if (procKind == SingleBlock.PROCESS_CONTENTS_BACK) {
            return (procVolume / 100f) * -1;
        }

        //-----------------------------------------
        // 回転
        //-----------------------------------------
        if (procKind == SingleBlock.PROCESS_CONTENTS_LEFT_ROTATE) {
            return (float) procVolume;
        }
        if (procKind == SingleBlock.PROCESS_CONTENTS_RIGHT_ROTATE) {
            return (float) procVolume * -1;
        }

        //-----------------------------------------
        // その他：処理量のない処理
        //-----------------------------------------
        return (float) NO_VOLUME_VALUE;
    }

    /*
     * 処理ブロックに応じたアニメーション時間を取得
     */
    public long getAnimationDuration(int procKind, int procVolume) {

        //-------------------
        // 前進／後退
        //-------------------
        if ((procKind == SingleBlock.PROCESS_CONTENTS_FORWARD) || (procKind == SingleBlock.PROCESS_CONTENTS_BACK)) {
            // スケールに応じた処理時間に変換
            Vector3 scale = getLocalScale();
            float ratio = (ArMainFragment.NODE_SIZE_S * ArMainFragment.NODE_SIZE_TMP_RATIO) / scale.x;

            return (long) (procVolume * WALK_TIME_PER_CM * ratio);
        }

        //-------------------
        // 回転
        //-------------------
        if ((procKind == SingleBlock.PROCESS_CONTENTS_LEFT_ROTATE) || (procKind == SingleBlock.PROCESS_CONTENTS_RIGHT_ROTATE)) {
            return (long) (procVolume * ROTATE_TIME_PER_ANGLE);
        }

        //-----------------------------------------
        // その他：処理量のない処理
        //-----------------------------------------
        return (long) NO_VOLUME_TIME;
    }

    /*
     * 本NodeのアニメーションメソッドをコールするAnimatorを設定
     *   @para1：実行ブロック
     */
    public void setAnimator(ProcessBlock executeBlock, ValueAnimator animator, int contents, float volume) {
        mProcessAnimator = animator;

        CharacterNode characterNode = this;

        //-----------------
        // リスナ―設定
        //-----------------
        mProcessAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationCancel(Animator animator) {
            }
            @Override
            public void onAnimationRepeat(Animator animator) {
            }
            @Override
            public void onAnimationStart(Animator animator) {
            }

            /*
             * ブロックアニメーション終了時
             * ※setWalk()等のコール満了時
             */
            @Override
            public void onAnimationEnd(Animator animator) {

                //----------------------
                // プログラミング途中終了
                //----------------------
                // ゴール判定
                if ( isGoaled() ) {
                    executeBlock.end( PROGRAMMING_END_GOAL );
                    return;
                }
                // アクション成否判定
                if ( !mSuccessAction ) {
                    executeBlock.end( PROGRAMMING_END_ACTION_FAILURE );
                    return;
                }
                // 障害物衝突判定
                if ( isObstacle() ) {
                    //!必要に応じて、それ用の値設定
                    executeBlock.end( PROGRAMMING_END_ACTION_FAILURE );
                    return;
                }

                //----------------------
                // プログラミング終了
                //----------------------
                // 実行ブロックが一番最後のブロックの場合
                if( executeBlock.isBottomBlock() ){
                    executeBlock.end( PROGRAMMING_END_ALL_DONE );
                    return;
                }

                //-------------------------------
                // 次の処理ブロックへ
                //-------------------------------
                // アニメーション終了時の位置を保持
                setEndProcessAnimation(contents, volume);
                // 次の処理ブロックへ
                executeBlock.tranceNextBlock( characterNode );
            }
        });
    }

    /*
     * モデルアニメーションの開始
     */
    public void startModelAnimation(String animationName, long duration) {

        // アニメーションがないなら何もしない
        if( getRenderableInstance().getAnimationCount() == 0 ){
            return;
        }

        //---------------------------------
        // 既にアニメーションが動いているかチェック
        //---------------------------------
        if ((mModelAnimator != null) && (mModelAnimator.isStarted())) {
            // 動いているアニメーションは終了
            mModelAnimator.end();
        }

        //---------------------------------
        // 新しい（指定された）アニメーションを開始
        //---------------------------------
        // モデルアニメーション開始
        mModelAnimator = getRenderableInstance().animate(animationName);
        if( mModelAnimator != null ){
            mModelAnimator.setDuration(duration);
            mModelAnimator.setRepeatCount(0);
            mModelAnimator.start();
        }

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
            case SingleBlock.PROCESS_CONTENTS_FORWARD:
            case SingleBlock.PROCESS_CONTENTS_BACK:
                animationName = MODEL_ANIMATION_STR_WALK;
                break;

            case SingleBlock.PROCESS_CONTENTS_RIGHT_ROTATE:
                animationName = MODEL_ANIMATION_STR_ROTATE_RIGHT;
                break;

            case SingleBlock.PROCESS_CONTENTS_LEFT_ROTATE:
                animationName = MODEL_ANIMATION_STR_ROTATE_LEFT;
                break;

            case SingleBlock.PROCESS_CONTENTS_EAT:
                animationName = MODEL_ANIMATION_STR_EAT;
                break;

            case SingleBlock.PROCESS_CONTENTS_THROW_AWAY:
                animationName = MODEL_ANIMATION_STR_THROW_AWAY;
                break;
        }

        // モデルアニメーション開始
        startModelAnimation(animationName, duration);
    }

    /*
     * ゴール判定
     */
    private boolean isGoaled() {
        return ( mCollisionNodeName.equals( GimmickManager.NODE_NAME_GOAL ));
    }

    /*
     * 障害物判定
     */
    private boolean isObstacle() {
        return ( mCollisionNodeName.equals( GimmickManager.NODE_NAME_OBSTACLE ));
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
        mCollisionDetectListener = listener;
    }

    /*
     * クリック検出用インターフェース
     */
    public interface CollisionDetectListener {
        // 衝突検知リスナー
        void onCollisionDetect( String collisionNode, ValueAnimator processAnimator );
    }
}
