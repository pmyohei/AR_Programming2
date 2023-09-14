package com.ar.ar_programming.character;

import static com.ar.ar_programming.ArMainFragment.PROGRAMMING_FAILURE;
import static com.ar.ar_programming.ArMainFragment.PROGRAMMING_NOT_END;
import static com.ar.ar_programming.ArMainFragment.PROGRAMMING_SUCCESS;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ar.ar_programming.ArMainFragment;
import com.ar.ar_programming.Gimmick;
import com.ar.ar_programming.GimmickManager;
import com.ar.ar_programming.R;
import com.ar.ar_programming.process.ProcessBlock;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.NodeParent;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.rendering.ViewSizer;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.ux.TransformationSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class CharacterNode extends TransformableNode {

    //---------------------------
    // 定数
    //---------------------------
    // アニメーションプロパティ名：キャラクター共通
    public static final String PROPERTY_NONE = "";
    public static final String PROPERTY_WALK = "walk";
    public static final String PROPERTY_ROTATE = "rotate";
    public static final String PROPERTY_THROW_AWAY = "throwAway";
    public static final String PROPERTY_ATTACK = "attack";

    // モデルアニメーション名（Blendarで命名）：キャラクター共通
    public static final String MODEL_ANIMATION_STR_NONE = "";
    public static final String MODEL_ANIMATION_STR_GOAL = "goal";
    public static final String MODEL_ANIMATION_STR_WALK = "walk";
    public static final String MODEL_ANIMATION_STR_ROTATE_LEFT = "rotate_left";
    public static final String MODEL_ANIMATION_STR_ROTATE_RIGHT = "rotate_right";
    public static final String MODEL_ANIMATION_STR_THROW_AWAY = "throwAway";
    public static final String MODEL_ANIMATION_STR_ATTACK = "attack";
    public static final String MODEL_ANIMATION_STR_ERROR = "error";

    // 移動1cm当たりのアニメーション時間(ms)
    private final float WALK_TIME_PER_CM = 100f;
    // 回転1度当たりのアニメーション時間(ms)
    private final float ROTATE_TIME_PER_ANGLE = 5f;

    // 処理量のない処理の仮量と仮時間(ms)
    private final float NO_VOLUME_TIME = 3000f;
    private final float NO_VOLUME_VALUE = 1000f;
    private final float NO_VOLUME_START_RATIO = 0.6f;
    public final float NO_VOLUME_START_VALUE = NO_VOLUME_VALUE * NO_VOLUME_START_RATIO;

    // 指定Node衝突判定結果
    public final int COLLISION_RET_NONE = -1;       // （ステージを除いて）衝突中Nodeなし
    public final int COLLISION_RET_OTHRE = -2;      // （ステージを除いて）指定Node以外と衝突中

    // アクションワード種別
    public static final String ACTION_WAITING = "waiting";
    public static final String ACTION_SUCCESS = "success";
    public static final String ACTION_FAILURE = "failure";

    // ブロック処理とアクションワード紐づけ
    public Map<String, Integer> ACTION_CONTENTS_MAP;

    //---------------------------
    // フィールド変数
    //---------------------------
    private Scene mScene;
    private Gimmick mGimmick;
    // 衝突中のNode
    public String mCollisionNodeName;
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
    public boolean mfinishNoneVolume;
    // アクション成否
    public boolean mSuccessAction;
    // アクション表記Renderable
    private ViewRenderable mActionRenderable;


    public CharacterNode(TransformationSystem transformationSystem, Gimmick gimmick) {
        super(transformationSystem);

        //----------------------------------------
        // アニメーション終了時の処理用保持用変数を初期化
        //----------------------------------------
        mCurrentPosition = new Vector3(0f, 0f, 0f);
        mCurrentDegree = 0f;
        mCollisionNodeName = GimmickManager.NODE_NAME_NONE;
        mfinishNoneVolume = false;          // 処理量なしアクション：未完了
        mSuccessAction = true;              // アクション成否：成功
        mGimmick = gimmick;

        // アクションワード初期化
        initActionWords();
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
     * キャラクターアクション表記Nodeの生成
     */
    public void createActionRenderable(ViewRenderable renderable) {

        //------------------
        // 生成済み判定
        //------------------
        // 生成済みなら何もしない
        if (hasActionWordNode()) {
            return;
        }

        //---------------------------
        // 実行中アクション表記Node生成
        //---------------------------
        mActionRenderable = renderable;

        // 位置
        Vector3 pos = new Vector3(0f, 3.5f, 0.0f);
        // サイズ設定：固定サイズ
        // !リビングサイズ用も用意しないとダメ？
        mActionRenderable.setSizer(new ViewSizer() {
            @Override
            public Vector3 getSize(View view) {
                return new Vector3(4.0f, 2.5f, 2.5f);
            }
        });

        // Node生成
        TransformationSystem transformationSystem = getTransformationSystem();
        TransformableNode node = new TransformableNode(transformationSystem);
        node.setParent(this);
        node.setLocalPosition(pos);
        node.setRenderable(mActionRenderable);
    }

    /*
     * アクションワードNodeを既に持っているか
     */
    private boolean hasActionWordNode() {

        // レンダラブルなしなら確実に持っていない
        if (mActionRenderable == null) {
            return false;
        }

        // 子Nodeが１つもなければ、未生成
        if (getChildren().size() == 0) {
            return false;
        }

        // あり
        return true;
    }

    /*
     * アクション表記ワードの設定
     */
    public void setActionWord(String action) {

        // アクション表記View
        ViewGroup view = (ViewGroup) mActionRenderable.getView();
        TextView tv_action = view.findViewById(R.id.tv_action);

        //------------------
        // 更新要否の判定
        //------------------
        // 表記中ワード
        String currentStr = tv_action.getText().toString();
        // ブロックに対応するアクションワードを取得
        String word = getContentsActionWord(view.getContext(), action);

        // ワードに変化がなければ何もしない
        if (currentStr.equals(word)) {
            return;
        }

        //------------------
        // アクションワード設定
        //------------------
        tv_action.setText(word);
    }

    /*
     * 指定ブロックコンテンツに対応するアクションワードを取得
     */
    private String getContentsActionWord(Context context, String action) {

        // ブロックコンテンツIDに対応するワードを返す
        Integer stringID = ACTION_CONTENTS_MAP.get(action);
        return context.getResources().getString(stringID);
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
                Log.i("Eat", "detectCollision() ゴールと衝突");
                startModelAnimation(MODEL_ANIMATION_STR_GOAL, 2000);
                break;

            } else if (collisionNode.equals(GimmickManager.NODE_NAME_OBSTACLE)) {
                Log.i("Eat", "detectCollision() 障害物と衝突");
                startModelAnimation(MODEL_ANIMATION_STR_ERROR, 2000);
                break;

            } else if (collisionNode.equals(GimmickManager.NODE_NAME_EATABLE)) {
                Log.i("Eat", "detectCollision() 食べ物と衝突");
//                startModelAnimation(MODEL_ANIMATION_STR_ERROR, 2000);
                break;

            } else if (collisionNode.equals(GimmickManager.NODE_NAME_POISON)) {
                Log.i("Eat", "detectCollision() 捨てるものと衝突");
//                startModelAnimation(MODEL_ANIMATION_STR_ERROR, 2000);
                break;

            } else if (collisionNode.equals(GimmickManager.NODE_NAME_ENEMY)) {
//                startModelAnimation(MODEL_ANIMATION_STR_ERROR, 2000);
                break;

            } else if (collisionNode.equals(GimmickManager.NODE_NAME_PICKUP)) {
//                startModelAnimation(MODEL_ANIMATION_STR_ERROR, 2000);
                break;
            }

            // 衝突中ノードクリア
            collisionNode = GimmickManager.NODE_NAME_NONE;
        }

        //----------------
        // 衝突リスナーコール
        //----------------
        mCollisionNodeName = collisionNode;

        Log.i("Eat", "detectCollision() 衝突後　mCollisionNodeName=" + mCollisionNodeName);

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
        // 角度を反映：キャラクター
        //----------------------------------
        Quaternion q = getLocalRotation();
        q.set(0f, y, 0f, w);
        setLocalRotation(q);

        //----------------------------------
        // 角度を反映：アクション表記Node
        //----------------------------------
        // キャラクターと逆の回転をさせる
        Quaternion qAction = getLocalRotation();
        qAction.set(0f, -y, 0f, w);

        Node node = getChildren().get(0);
        node.setLocalRotation(qAction);
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
     * 捨てる
     */
    private void throwAway() {

        //----------------
        // 失敗判定
        //----------------
        int index = getCollisionIndex(GimmickManager.NODE_NAME_POISON);
        // 衝突中Nodeなし or 食べられないNodeと衝突中
        if ((index == COLLISION_RET_NONE) || (index == COLLISION_RET_OTHRE)) {
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
        removeNodeFromScene(index);
        // 衝突中Node情報クリア
        mCollisionNodeName = GimmickManager.NODE_NAME_NONE;
    }


    /*
     * 攻撃
     */
    private void attack() {

        //----------------
        // 失敗判定
        //----------------
        int index = getCollisionIndex(GimmickManager.NODE_NAME_ENEMY);
        // 衝突中Nodeなし or 敵ではないNodeと衝突中
        if ((index == COLLISION_RET_NONE) || (index == COLLISION_RET_OTHRE)) {
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
        removeNodeFromScene(index);
        // 衝突中Node情報クリア
        mCollisionNodeName = GimmickManager.NODE_NAME_NONE;
    }

    /*
     * 攻撃アニメーションメソッド
     *   ※ プロパティ名：attack
     *   ※ 本メソッド内でアニメーションのための漸次的な処理はなし
     *      一定時間経過でブロック処理を実行させるために利用する。
     */
    public void setAttack(float volume) {

        // 一定時間を超過したとき、ブロック処理を実行する
        if ((volume >= NO_VOLUME_START_VALUE) && !mfinishNoneVolume) {
            // 処理完了に
            mfinishNoneVolume = true;
            // 攻撃
            attack();
        }
    }

    /*
     * SceneからNodeを削除
     */
    public void removeNodeFromScene(int index) {
        ArrayList<Node> nodes = mScene.overlapTestAll(this);
        Node deleteNode = nodes.get(index);
        NodeParent parent = deleteNode.getParent();
        parent.removeChild(deleteNode);
    }

    /*
     * （ステージを除いた）衝突中判定
     *   指定Nodeと衝突しているか判定する
     */
    public int getCollisionIndex(String nodeName) {

        ArrayList<Node> nodes = mScene.overlapTestAll(this);
        for (int i = 0; i < nodes.size(); i++) {

            String overlapNode = nodes.get(i).getName();

            if (overlapNode.equals(GimmickManager.NODE_NAME_STAGE)) {
                // ステージの場合は、次のNodeをチェック

            } else if (overlapNode.equals(nodeName)) {
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
    public void setEndProcessAnimation(String contents, float volume) {

        // 状態クリア
//        mCollisionNodeName = GimmickManager.NODE_NAME_NONE;
        mfinishNoneVolume = false;          // 処理量なしアクション：未完了
        mSuccessAction = true;              // アクション成否：成功

        Log.i("成功判定", "処理ブロックアニメーション終了処理");

        // アニメーション終了時の変化後の値を保持
        setAnimationEndValue(contents, volume);
    }

    /*
     * アニメーション終了時の変化後の値を保持
     */
    public void setAnimationEndValue(String contents, float volume) {

        // 処理種別に応じた保存処理
        switch (contents) {
            // 移動
            case GimmickManager.BLOCK_EXE_FORWARD:
            case GimmickManager.BLOCK_EXE_BACK:
                saveCurrentPosition();
                return;

            // 回転
            case GimmickManager.BLOCK_EXE_ROTATE_RIGHT:
            case GimmickManager.BLOCK_EXE_ROTATE_LEFT:
                saveCurrentAngle(volume);
                return;

            // それ以外
            default:
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
        // 初期位置に戻す
        //----------------------------------
        // 位置
        setLocalPosition(mStartPosition);

        // 角度
        // Quaternionのy/wの値を算出
        float w = calcQuaternionWvalue(mStartDegree);
        float y = calcQuaternionYvalue(mStartDegree);
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

        //----------------------------------
        // アクションワードを初期状態へ
        //----------------------------------
        setActionWord(ACTION_WAITING);
        // 向きを初期状態にする
        Node node = getChildren().get(0);
        node.setLocalRotation(q);

        //----------------------------------
        // 衝突情報をリセット
        //----------------------------------
        mCollisionNodeName = GimmickManager.NODE_NAME_NONE;
    }

    /*
     * 処理種別に応じたメソッドのプロパティ名の取得
     */
    public String getPropertyName(String contents) {

        // 処理種別に応じたメソッドのプロパティ名を取得
        switch (contents) {
            case GimmickManager.BLOCK_EXE_FORWARD:
            case GimmickManager.BLOCK_EXE_BACK:
                return PROPERTY_WALK;

            case GimmickManager.BLOCK_EXE_ROTATE_RIGHT:
            case GimmickManager.BLOCK_EXE_ROTATE_LEFT:
                return PROPERTY_ROTATE;

            case GimmickManager.BLOCK_EXE_THROW_AWAY:
                return PROPERTY_THROW_AWAY;

            case GimmickManager.BLOCK_EXE_ATTACK:
                return PROPERTY_ATTACK;
        }

        return PROPERTY_NONE;
    }

    /*
     * 処理ブロックに応じたアニメーション量を取得
     */
    public float getAnimationVolume(String contents, int procVolume) {

        //-----------------------------------------
        // 前進／後退
        //-----------------------------------------
        // 単位変換：cm → m
        if (contents.equals(GimmickManager.BLOCK_EXE_FORWARD)) {
            return (float) procVolume / 100f;
        }
        if (contents.equals(GimmickManager.BLOCK_EXE_BACK)) {
            return (procVolume / 100f) * -1;
        }

        //-----------------------------------------
        // 回転
        //-----------------------------------------
        if (contents.equals(GimmickManager.BLOCK_EXE_ROTATE_LEFT)) {
            return (float) procVolume;
        }
        if (contents.equals(GimmickManager.BLOCK_EXE_ROTATE_RIGHT)) {
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
    public long getAnimationDuration(String contents, int procVolume) {

        //-------------------
        // 前進／後退
        //-------------------
        if ((contents.equals(GimmickManager.BLOCK_EXE_FORWARD)) || (contents.equals(GimmickManager.BLOCK_EXE_BACK))) {
            // スケールに応じた処理時間に変換
            Vector3 scale = getLocalScale();
            float ratio = (ArMainFragment.NODE_SIZE_S * ArMainFragment.NODE_SIZE_TMP_RATIO) / scale.x;

            return (long) (procVolume * WALK_TIME_PER_CM * ratio);
        }

        //-------------------
        // 回転
        //-------------------
        if ((contents.equals(GimmickManager.BLOCK_EXE_ROTATE_RIGHT)) || (contents.equals(GimmickManager.BLOCK_EXE_ROTATE_LEFT))) {
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
    public void setAnimator(ProcessBlock executeBlock, ValueAnimator animator, String contents, float volume) {
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

                //------------------------
                // プログラミング途中終了判定
                //------------------------
                // 途中終了する条件が成立しているかどうか
                int programmingEndState = shouldFinishProgram();
                if (programmingEndState != PROGRAMMING_NOT_END) {
                    // ステージ終了処理へ
                    executeBlock.end(programmingEndState);
                    return;
                }

                //----------------------
                // 全ブロック終了判定
                //----------------------
                // 実行ブロックが一番最後のブロックの場合
                Log.i("ギミック変更", "onAnimationEnd() 全ブロック終了判定 直前");
                if (executeBlock.isBottomBlock()) {
                    int resultProgramming = isCompleteSuccessCondition();
                    executeBlock.end(resultProgramming);
                    return;
                }

                //-------------------------------
                // 次の処理ブロックへ
                //-------------------------------
                // アニメーション終了時の位置を保持
                setEndProcessAnimation(contents, volume);
                // 次の処理ブロックへ
                executeBlock.tranceNextBlock(characterNode);
            }
        });
    }

    /*
     * モデルアニメーションの開始
     *   Blenderにて実装した３Dモデル独自のアニメーションを開始する
     */
    public void startModelAnimation(String animationName, long duration) {

        // アニメーションがないなら何もしない
        if (getRenderableInstance().getAnimationCount() == 0) {
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
        if (mModelAnimator != null) {
            mModelAnimator.setDuration(duration);
            mModelAnimator.setRepeatCount(0);
            mModelAnimator.start();
        }
    }

    /*
     * Blender側で命名された３Dアニメーション名を取得
     */
    public String getModelAnimationName(String contents) {

        String animationName = "";

        // アニメーション名を取得
        switch (contents) {
            case GimmickManager.BLOCK_EXE_FORWARD:
            case GimmickManager.BLOCK_EXE_BACK:
                animationName = MODEL_ANIMATION_STR_WALK;
                break;

            case GimmickManager.BLOCK_EXE_ROTATE_RIGHT:
                animationName = MODEL_ANIMATION_STR_ROTATE_RIGHT;
                break;

            case GimmickManager.BLOCK_EXE_ROTATE_LEFT:
                animationName = MODEL_ANIMATION_STR_ROTATE_LEFT;
                break;

            case GimmickManager.BLOCK_EXE_THROW_AWAY:
                animationName = MODEL_ANIMATION_STR_THROW_AWAY;
                break;

            default:
                animationName = MODEL_ANIMATION_STR_NONE;
                break;
        }

        return animationName;
    }

    /*
     * プログラムの実行を終了する条件が満了したかどうか
     */
    private int shouldFinishProgram() {

        // ゴール判定
        if (isGoaled()) {
            return PROGRAMMING_SUCCESS;
        }
        // アクション成否判定
        if (!mSuccessAction) {
            return PROGRAMMING_FAILURE;
        }
        // 障害物衝突判定
        if (isFrontNode(GimmickManager.NODE_NAME_OBSTACLE)) {
            return PROGRAMMING_FAILURE;
        }

        return PROGRAMMING_NOT_END;
    }

    /*
     * プログラムの結果判定
     */
    public int isCompleteSuccessCondition() {

        int result = PROGRAMMING_FAILURE;

        Log.i("ギミック変更", "judgeProgrammingResult()");

        switch (mGimmick.successCondition) {

            case GimmickManager.SUCCESS_CONDITION_ALL_EAT:
                // 全て食べているなら、成功
                boolean leftovers = existsNodeOnScene(GimmickManager.NODE_NAME_EATABLE);
                Log.i("ギミック変更", "leftovers=" + leftovers);
                if (!leftovers) {
                    result = PROGRAMMING_SUCCESS;
                }

            default:
                break;
        }

        return result;
    }

    /*
     * ゴール判定
     */
    public boolean isGoaled() {

        //---------------------------
        // ゴール前のクリア条件達成判定
        //---------------------------
        boolean isPreGoal = isPreGoal();
        if( !isPreGoal ){
            // 未達成なら、未ゴール
            return false;
        }

        //-------------------
        // ゴール判定
        //-------------------
        return (mCollisionNodeName.equals(GimmickManager.NODE_NAME_GOAL));
    }


    /*
     * ゴール前のクリア条件達成判定
     */
    public boolean isPreGoal() {

        boolean isAchieved;

        switch (mGimmick.successCondition) {

            // ゴール前に「敵を撃破」する必要あり
            case GimmickManager.SUCCESS_CONDITION_ATTACK_AND_GOAL:
                isAchieved = isAllEraseNode( GimmickManager.NODE_NAME_ENEMY );
                break;

            // ゴール前に「物を拾う」する必要あり
            case GimmickManager.SUCCESS_CONDITION_PICKUP_AND_GOAL:
                isAchieved = isAllEraseNode( GimmickManager.NODE_NAME_PICKUP );
                break;

            default:
                // 成功条件にゴール前条件がなければ、条件達成されている状態
                isAchieved = true;
                break;
        }

        return isAchieved;
    }

    /*
     * 指定NodeをSceneから全て削除したかどうか
     */
    public boolean isAllEraseNode( String nodeName ) {

        // 全Node検索
        List<Node> nodes = getParent().getChildren();
        for (Node node : nodes) {
            if (node.getName().equals( nodeName )) {
                // 敵NodeがScene上にあれば、未撃破
                return false;
            }
        }

        // 全削除
        return true;
    }

    /*
     * 指定NodeがScene上に存在しているかどうか
     */
    public boolean existsNodeOnScene( String searchNodeName ) {

        // 全Node検索
        List<Node> nodes = getParent().getChildren();
        for (Node node : nodes) {
            if ( node.getName().equals( searchNodeName ) ) {
                // Scene上にあり
                return true;
            }
        }

        // Scene上になし
        return false;
    }

    /*
     * 目の前に指定Nodeがあるかどうか
     */
    public boolean isFrontNode( String nodeName ) {
        // 指定Nodeと衝突中かどうか
        return ( mCollisionNodeName.equals( nodeName ));
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

    /*
     * アクションワード紐づけ初期化
     */
    public abstract void initActionWords();

}
