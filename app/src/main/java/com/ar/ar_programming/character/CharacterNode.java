package com.ar.ar_programming.character;

import static com.ar.ar_programming.ARFragment.PROGRAMMING_FAILURE;
import static com.ar.ar_programming.ARFragment.PROGRAMMING_NOT_END;
import static com.ar.ar_programming.ARFragment.PROGRAMMING_SUCCESS;
import static com.ar.ar_programming.GimmickManager.BLOCK_ACTION_TARGET_NODE_POS;
import static com.ar.ar_programming.GimmickManager.BLOCK_ACTION_TARGET_NODE_STATE_POS;
import static com.ar.ar_programming.GimmickManager.BLOCK_CONDITION_FACING;
import static com.ar.ar_programming.GimmickManager.GIMMICK_DELIMITER_TARGET_NODE_INFO;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ar.ar_programming.ARFragment;
import com.ar.ar_programming.Gimmick;
import com.ar.ar_programming.GimmickManager;
import com.ar.ar_programming.R;
import com.ar.ar_programming.process.ProcessBlock;
import com.google.ar.sceneform.AnchorNode;
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
    public static final String PROPERTY_THROW = "throw";
    public static final String PROPERTY_ATTACK = "attack";
    public static final String PROPERTY_LONG_ATTACK = "farAttack";
    public static final String PROPERTY_PICKUP = "pickup";
    public static final String PROPERTY_CHANGE_TARGET = "changeTarget";

    // モデルアニメーション名（Blendarで命名）：キャラクター共通
    public static final String MODEL_ANIMATION_STR_NONE = "";
    public static final String MODEL_ANIMATION_STR_GOAL = "goal";
    public static final String MODEL_ANIMATION_STR_WALK = "walk";
    public static final String MODEL_ANIMATION_STR_ROTATE_LEFT = "rotate_left";
    public static final String MODEL_ANIMATION_STR_ROTATE_RIGHT = "rotate_right";
    public static final String MODEL_ANIMATION_STR_THROW = "throw";
    public static final String MODEL_ANIMATION_STR_ATTACK = "attack";
    public static final String MODEL_ANIMATION_STR_PICKUP = "pickup";
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
    public Map<String, Integer> Map_contents__actionWord;

    //---------------------------
    // 共通
    //---------------------------
    private Scene mScene;
    private Gimmick mGimmick;

    //-----------------------------
    // プログラミング終了通知の受信有無
    //-----------------------------
    private boolean mNotifyProgrammingEnd;

    //---------------------------
    // 衝突
    //---------------------------
    public String mCollisionNodeName;                           // 衝突中のNode
    private CollisionDetectListener mCollisionDetectListener;   // 衝突検知リスナー

    //---------------------------
    // 位置関連情報
    //---------------------------
    // 初期位置
    private Vector3 mStartPosition;
    private float mStartDegree;
    // 処理ブロックアニメーション終了時点の情報
    private Vector3 mCurrentPosition;
    private float mCurrentDegree;           // ※キャラクターが下を向いている角度（一般的には２７０度方向）を０度とする

    //---------------------------
    // アニメーション関連
    //---------------------------
    private ValueAnimator mProcessAnimator;     // 処理ブロック用アニメーション
    private ObjectAnimator mModelAnimator;      // モデルアニメーションの開始と終了を制御する用

    //---------------------------
    // アクション
    //---------------------------
    public boolean mFinishNoneVolume;           // 処理量なしのアクションメソッド完了フラグ
    public String mTargetNode;                  // アクション対象Node
    public boolean mSuccessAction;              // アクション成否
    private ViewRenderable mActionRenderable;   // アクション表記Renderable
    private List<Node> mNotSearchNodeList;      // 検索対象外Nodeリスト
    private List<Node> mRemovedNodeList;      // ステージから除外したNodeリスト


    /*
     * コンストラクタ
     */
    public CharacterNode(TransformationSystem transformationSystem, Gimmick gimmick) {
        super(transformationSystem);

        //----------
        // 初期化
        //----------
        mNotSearchNodeList = new ArrayList<>();
        mRemovedNodeList = new ArrayList<>();

        // プログラミング終了通知OFF
        mNotifyProgrammingEnd = false;

        //----------------------------------------
        // アニメーション終了時の処理用保持用変数を初期化
        //----------------------------------------
        mCurrentPosition = new Vector3(0f, 0f, 0f);
        mCurrentDegree = 0f;
        mCollisionNodeName = GimmickManager.NODE_NAME_NONE;
        mFinishNoneVolume = false;          // 処理量なしアクション：未完了
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
    public void createActionRenderable(ViewRenderable renderable, float characterAngle) {

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

        //--------
        // 位置情報
        //--------
        Vector3 pos = new Vector3(0f, 3.5f, 0.0f);
        // サイズ設定：固定サイズ
        // !リビングサイズ用も用意しないとダメ？
        mActionRenderable.setSizer(new ViewSizer() {
            @Override
            public Vector3 getSize(View view) {
                return new Vector3(4.0f, 2.5f, 2.5f);
            }
        });

        //--------
        // 角度情報
        //--------
        // キャラクターNodeに設定した角度と反対の角度になるよう、Quaternionを算出
        // 例）キャラクターの初期向きが「90度」なら、「-90度」して正面を向くようにする。という対応
        float w = calcQuaternionWvalue(characterAngle);
        float y = calcQuaternionYvalue(characterAngle);

        Quaternion qAction = getLocalRotation();
        qAction.set(0f, -y, 0f, w);

        //-----------
        // Node生成
        //-----------
//        TransformationSystem transformationSystem = getTransformationSystem();
//        TransformableNode node = new TransformableNode(transformationSystem);
        Node node = new Node();
        node.setParent(this);
        node.setLocalPosition(pos);
        node.setLocalRotation(qAction);
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
     * アクション対象Node名の設定
     */
    public void setTargetNode(String targetNode) {
        mTargetNode = targetNode;
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
        Integer stringID = Map_contents__actionWord.get(action);
        return context.getResources().getString(stringID);
    }

    /*
     * 衝突検知
     */
    private void detectCollision() {

        // 衝突の有無
        String collisionNode = GimmickManager.NODE_NAME_NONE;

        //------------
        // 衝突の検証
        //------------
        ArrayList<Node> nodes = mScene.overlapTestAll(this);
        for (Node overlapNode : nodes) {

            // 衝突中のノード名を取得
            collisionNode = overlapNode.getName();
            if (!collisionNode.equals(GimmickManager.NODE_NAME_STAGE)) {
                // ステージ以外と検出した場合、検証終了
                break;
            }

            // 衝突中ノード名クリア
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

        return;
    }


    /*
     * 前進／後退アニメーションメソッド
     *   ※プロパティ名：walk
     */
    public void setWalk(float volume) {

        // 衝突中は、処理なし
        if (!mCollisionNodeName.equals(GimmickManager.NODE_NAME_NONE)) {
//            Log.i("ブロック処理の流れ", "キャラクター setWalk() 衝突中のため停止 mCollisionNodeName=" + mCollisionNodeName);
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
     *   ※ プロパティ名：throw
     *   ※ 本メソッド内でアニメーションのための漸次的な処理はなし
     *      一定時間経過でブロック処理を実行させるために利用する。
     */
    public void setThrow(float volume) {

        // 一定時間を超過したとき、ブロック処理を実行する
        if ((volume >= NO_VOLUME_START_VALUE) && !mFinishNoneVolume) {
            // 処理完了に
            mFinishNoneVolume = true;
            // 捨てる
            throwAway();
        }
    }


    /*
     * 目の前にある（衝突中の）Node削除アクション共通処理
     */
    public boolean deleteFrontNodeAction(String nodeName) {

        //----------------
        // 失敗判定
        //----------------
        boolean isCollision = mCollisionNodeName.contains(nodeName);
        if (!isCollision) {
            // 対象Nodeと衝突中ではないなら、アクション失敗
            return false;
        }

        //----------------
        // Node削除
        //----------------
        // 衝突中のNode Indexを取得
        int index = getCollisionNodeIndex(nodeName);
        if (index == COLLISION_RET_NONE) {
            // Sceneから取得できない場合、アクション失敗とする（フェールセーフ）
            return false;
        }

        // SceneからNodeを削除
        removeNodeFromScene(index);
        // 衝突中Node情報クリア
        mCollisionNodeName = GimmickManager.NODE_NAME_NONE;

        //---------------
        // 成功処理
        //---------------
        // アクション成否：成功
        return true;
    }

    /*
     * 遠くにある（キャラクターの向いている方向にある）Node削除アクション共通処理
     */
    public boolean deleteFarNodeAction(String nodeName) {

        //---------------------------------------
        // キャラクターの向いている方向にあるNode検索
        //---------------------------------------
        // 検索
        AnchorNode anchorNode = (AnchorNode) getParentNode();
        Node targetNode = ARFragment.searchNodeCharacterFacingOnStage(anchorNode, nodeName, this);
        if (targetNode == null) {
            // 該当Nodeがなければ、条件不成立とみなす
            return false;
        }

        //----------------
        // Node削除
        //----------------
        removeNodeOnStage(targetNode);

        //---------------
        // 成功処理
        //---------------
        // アクション成否：成功
        return true;
    }

    /*
     * 捨てる
     */
    private void throwAway() {
        Log.i("アクション不具合", "throwAway=" + mTargetNode);
        mSuccessAction = deleteFrontNodeAction(mTargetNode);
    }

    /*
     * 攻撃
     */
    private void attack() {
        mSuccessAction = deleteFrontNodeAction(mTargetNode);
    }

    /*
     * 攻撃アニメーションメソッド
     *   ※ プロパティ名：attack
     *   ※ 本メソッド内でアニメーションのための漸次的な処理はなし
     *      一定時間経過でブロック処理を実行させるために利用する。
     */
    public void setAttack(float volume) {

        // 一定時間を超過したとき、ブロック処理を実行する
        if ((volume >= NO_VOLUME_START_VALUE) && !mFinishNoneVolume) {
            // 処理完了に
            mFinishNoneVolume = true;
            // 攻撃
            attack();
        }
    }

    /*
     * 遠距離攻撃
     */
    private void farAttack() {
        mSuccessAction = deleteFarNodeAction(mTargetNode);
    }

    /*
     * 遠距離攻撃アニメーションメソッド
     *   ※ プロパティ名：farAttack
     *   ※ 本メソッド内でアニメーションのための漸次的な処理はなし
     *      一定時間経過でブロック処理を実行させるために利用する。
     */
    public void setFarAttack(float volume) {

        // 一定時間を超過したとき、ブロック処理を実行する
        if ((volume >= NO_VOLUME_START_VALUE) && !mFinishNoneVolume) {
            // 処理完了に
            mFinishNoneVolume = true;
            // 遠距離攻撃
            farAttack();
        }
    }

    /*
     * 拾う
     */
    private void pickup() {
        mSuccessAction = deleteFrontNodeAction(mTargetNode);
    }

    /*
     * 拾うアニメーションメソッド
     *   ※ プロパティ名：pickup
     *   ※ 本メソッド内でアニメーションのための漸次的な処理はなし
     *      一定時間経過でブロック処理を実行させるために利用する。
     */
    public void setPickup(float volume) {

        // 一定時間を超過したとき、ブロック処理を実行する
        if ((volume >= NO_VOLUME_START_VALUE) && !mFinishNoneVolume) {
            // 処理完了に
            mFinishNoneVolume = true;
            // 拾う
            pickup();
        }
    }

    /*
     * ターゲット変更
     */
    private void changeTarget() {

        // 対象Node情報を分解
        String[] targetNodeInfo = mTargetNode.split(GIMMICK_DELIMITER_TARGET_NODE_INFO);
        String state = targetNodeInfo[BLOCK_ACTION_TARGET_NODE_STATE_POS];
        String nodeName = targetNodeInfo[BLOCK_ACTION_TARGET_NODE_POS];

        Node node = null;

        //-----------------------------
        // 対象Node状態に応じて振り分け
        //-----------------------------
        switch (state) {

            //-------------------------------------
            // 対象Node状態：キャラクターの向いている方向
            //-------------------------------------
            case BLOCK_CONDITION_FACING:
                // 該当Nodeを検索
                node = ARFragment.searchNodeCharacterFacingOnStage((AnchorNode) getParentNode(), nodeName, this);
                break;

            default:
                break;
        }

        //----------------------------------
        // 該当NodeをNode検索対象外リストへ追加
        //----------------------------------
        if (node != null) {
            mNotSearchNodeList.add(node);
        }
    }


    /*
     * ターゲット変更アニメーションメソッド
     *   ※ プロパティ名：changeTarget
     *   ※ 本メソッド内でアニメーションのための漸次的な処理はなし
     *      一定時間経過でブロック処理を実行させるために利用する。
     */
    public void setChangeTarget(float volume) {

        // 一定時間を超過したとき、ブロック処理を実行する
        if ((volume >= NO_VOLUME_START_VALUE) && !mFinishNoneVolume) {
            // 処理完了に
            mFinishNoneVolume = true;
            // 遠距離攻撃
            changeTarget();
        }
    }


    /*
     * SceneからNodeを削除
     */
    public void removeNodeFromScene(int index) {

        ArrayList<Node> nodes = mScene.overlapTestAll(this);
        Node deleteNode = nodes.get(index);

        removeNodeOnStage(deleteNode);
    }

    /*
     * SceneからNodeを削除
     */
    public void removeNodeOnStage(Node node) {

        // 除外リストへ追加
        mRemovedNodeList.add(node);

        // ステージから削除
        NodeParent parent = getParentNode();
        parent.removeChild(node);
    }

    /*
     * 衝突中NodeのIndexを取得
     */
    public int getCollisionNodeIndex(String nodeName) {

        ArrayList<Node> nodes = mScene.overlapTestAll(this);
        for (int i = 0; i < nodes.size(); i++) {

            String overlapNode = nodes.get(i).getName();
            if (overlapNode.contains(nodeName)) {
                return i;
            }
        }

        // 衝突中のNodeの中に指定Nodeなし
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
        mFinishNoneVolume = false;          // 処理量なしアクション：未完了
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
     * プログラミングの実行の中断
     */
    public void notifyInterruptionProgramming() {
        // プログラミング中断ON
        mNotifyProgrammingEnd = true;
        // ブロック側のアニメーションを終了
        if( mProcessAnimator != null ){
            mProcessAnimator.end();
        }
    }

    /*
     * 状態を初期化
     */
    public void initStatus() {

        //----------------------------------
        // 位置関連
        //----------------------------------
        // 位置を初期位置に
        setLocalPosition(mStartPosition);

        // 角度を初期位置に
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

        //----------------------------------
        // アクション成否をリセット
        //----------------------------------
        mSuccessAction = true;

        //----------------------------------
        // リストをリセット
        //----------------------------------
        mNotSearchNodeList.clear();
        mRemovedNodeList.clear();

        //----------------------------------
        // 中断情報をリセット
        //----------------------------------
        mNotifyProgrammingEnd = false;
    }

    /*
     * 処理種別に応じたメソッドのプロパティ名の取得
     */
    public String getMethodPropertyName(String contents) {

        // 処理種別に応じたメソッドのプロパティ名を取得
        switch (contents) {
            case GimmickManager.BLOCK_EXE_FORWARD:
            case GimmickManager.BLOCK_EXE_BACK:
                return PROPERTY_WALK;

            case GimmickManager.BLOCK_EXE_ROTATE_RIGHT:
            case GimmickManager.BLOCK_EXE_ROTATE_LEFT:
                return PROPERTY_ROTATE;

            case GimmickManager.BLOCK_EXE_THROW:
                return PROPERTY_THROW;

            case GimmickManager.BLOCK_EXE_ATTACK:
                return PROPERTY_ATTACK;

            case GimmickManager.BLOCK_EXE_LONG_ATTACK:
                return PROPERTY_LONG_ATTACK;

            case GimmickManager.BLOCK_EXE_PICKUP:
                return PROPERTY_PICKUP;

            case GimmickManager.BLOCK_EXE_CHANGE_TARGET:
                return PROPERTY_CHANGE_TARGET;

            default:
                return PROPERTY_NONE;
        }
    }

    /*
     * 処理ブロックに応じたアニメーション量を取得
     */
    public float getAnimationVolume(String action, int volume) {

        //-----------------------------------------
        // 前進／後退
        //-----------------------------------------
        // 単位変換：cm → m
        if (action.equals(GimmickManager.BLOCK_EXE_FORWARD)) {
            return (float) volume / 100f;
        }
        if (action.equals(GimmickManager.BLOCK_EXE_BACK)) {
            return (volume / 100f) * -1;
        }

        //-----------------------------------------
        // 回転
        //-----------------------------------------
        if (action.equals(GimmickManager.BLOCK_EXE_ROTATE_LEFT)) {
            return (float) volume;
        }
        if (action.equals(GimmickManager.BLOCK_EXE_ROTATE_RIGHT)) {
            return (float) volume * -1;
        }

        //-----------------------------------------
        // その他：処理量のない処理
        //-----------------------------------------
        return (float) NO_VOLUME_VALUE;
    }

    /*
     * 処理ブロックに応じたアニメーション時間を取得
     */
    public long getAnimationDuration(String action, int volume) {

        //-------------------
        // 前進／後退
        //-------------------
        if ((action.equals(GimmickManager.BLOCK_EXE_FORWARD)) || (action.equals(GimmickManager.BLOCK_EXE_BACK))) {
            // スケールに応じた処理時間に変換
            Vector3 scale = getLocalScale();
            float ratio = (ARFragment.NODE_SIZE_S * ARFragment.NODE_SIZE_TMP_RATIO) / scale.x;

            return (long) (volume * WALK_TIME_PER_CM * ratio);
        }

        //-------------------
        // 回転
        //-------------------
        if ((action.equals(GimmickManager.BLOCK_EXE_ROTATE_RIGHT)) || (action.equals(GimmickManager.BLOCK_EXE_ROTATE_LEFT))) {
            return (long) (volume * ROTATE_TIME_PER_ANGLE);
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
    public void setAnimator(ProcessBlock executeBlock, ValueAnimator animator, String action, float volume) {
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
             *   ※setWalk()等のコール満了時
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
                    Log.i("プログラミング終了シーケンス", "CharacterNode onAnimationEnd() 途中終了条件成立");
                    executeBlock.end(programmingEndState);
                    return;
                }

                //----------------------
                // 全ブロック終了判定
                //----------------------
                // 実行ブロックが一番最後のブロックの場合
                Log.i("プログラミング終了シーケンス", "CharacterNode onAnimationEnd() 全ブロック終了判定 直前");
                if (executeBlock.isBottomBlock()) {
                    int resultProgramming = isCompleteSuccessCondition();
                    executeBlock.end(resultProgramming);
                    return;
                }

                //-------------------------------
                // 次の処理ブロックへ
                //-------------------------------
                // アニメーション終了時の位置を保持
                setEndProcessAnimation(action, volume);
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

            case GimmickManager.BLOCK_EXE_THROW:
                animationName = MODEL_ANIMATION_STR_THROW;
                break;

            case GimmickManager.BLOCK_EXE_PICKUP:
                animationName = MODEL_ANIMATION_STR_PICKUP;
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

        //----------------------
        // ブロック処理関連
        //----------------------
        // ゴール判定
        if (isGoaled()) {
            Log.i("プログラミング終了シーケンス", "CharacterNode shouldFinishProgram() ゴール到達");
            return PROGRAMMING_SUCCESS;
        }
        // アクション成否判定
        if (!mSuccessAction) {
            Log.i("プログラミング終了シーケンス", "CharacterNode shouldFinishProgram() アクション失敗");
            return PROGRAMMING_FAILURE;
        }
        // 障害物衝突判定
        if ( isNodeCollision(mGimmick.objectObstacle) ) {
            Log.i("プログラミング終了シーケンス", "CharacterNode shouldFinishProgram() 障害物と衝突中");
            return PROGRAMMING_FAILURE;
        }

        //----------------------
        // ステージ場外判定
        //----------------------
        // 距離の上限を2.0mとする
        final float DISTANCE_LIMIT = 2.0f;
        float distance = calcStageDistance();
        if( distance > DISTANCE_LIMIT){
            // キャラクターが超えてしまったら失敗
            return PROGRAMMING_FAILURE;
        }

        // プログラム継続
        return PROGRAMMING_NOT_END;
    }


    /*
     * 自分（キャラクター）とステージとの距離（単純な直線距離）を算出
     *   単位：m　のため、50cm離れるとすると、0.5が算出される
     */
    private float calcStageDistance() {

        // 自分（キャラクター）の位置
        float selfPosX = getLocalPosition().x;
        float selfPosY = getLocalPosition().z;

        // 軸間を二乗（ステージは原点）
        float distanceSquaredX = (selfPosX - 0) * (selfPosX - 0);
        float distanceSquaredY = (selfPosY - 0) * (selfPosY - 0);
        //距離を返す
        return (float) Math.sqrt( distanceSquaredX + distanceSquaredY);
    }

    /*
     * プログラムの結果判定
     */
    public int isCompleteSuccessCondition() {

        int result = PROGRAMMING_FAILURE;

        switch (mGimmick.successCondition) {

            case GimmickManager.SUCCESS_CONDITION_ALL_REMOVE:
                // ステージ上に指定Nodeがあるか検索
                Node node = ARFragment.searchNodeOnStage((AnchorNode) getParentNode(), mGimmick.successRemoveTarget);
                if (node == null) {
                    // 全てステージからなくなっているなら、成功
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

        // ゴール自体ないギミックであれば、未クリア扱い
        if (mGimmick.goalName.isEmpty()) {
            return false;
        }

        //---------------------------
        // ゴール前のクリア条件達成判定
        //---------------------------
        boolean isPreGoal = isPreGoal();
        if (!isPreGoal) {
            // 未達成なら、未ゴール
            return false;
        }

        //-------------------
        // ゴール判定
        //-------------------
        return (mCollisionNodeName.equals(mGimmick.goalName));
    }


    /*
     * ゴール前のクリア条件達成判定
     */
    public boolean isPreGoal() {

        boolean isAchieved = false;

        switch (mGimmick.successCondition) {

            //--------------------------------------
            // ゴール前に「指定ノードを全て削除」する必要あり
            //--------------------------------------
            case GimmickManager.SUCCESS_CONDITION_REMOVE_AND_GOAL:

                // ステージ上に指定Nodeがあるか検索
                Node node = ARFragment.searchNodeOnStage((AnchorNode) getParentNode(), mGimmick.successRemoveTarget);
                if (node == null) {
                    // 全てステージからなくなっているなら、条件達成
                    isAchieved = true;
                }

                Log.i("ゴール達成判定", "isAchieved=" + isAchieved);
                Log.i("ゴール達成判定", "successRemoveTarget=" + mGimmick.successRemoveTarget);

                break;

            default:
                // 成功条件にゴール前条件がなければ、条件達成されている状態
                isAchieved = true;
                break;
        }

        return isAchieved;
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
        if ((minRange <= toNodeDegree) && (toNodeDegree <= maxRange)) {
//            Log.i("向いている方向ロジック", "〇 傾きからの角度=" + toNodeDegree);
//            Log.i("向いている方向ロジック", "〇 キャラの向き=" + characterFacingDegree);
            Log.i("Node検索", "isFacingToNode() 向いている toNodeDegree=" + toNodeDegree);
            return true;
        } else {
//            Log.i("向いている方向ロジック", "× 傾きからの角度=" + toNodeDegree);
//            Log.i("向いている方向ロジック", "× キャラの向き=" + characterFacingDegree);
            Log.i("Node検索", "isFacingToNode() 向いていない toNodeDegree=" + toNodeDegree);
            return false;
        }
    }

    /*
     * 指定Nodeと衝突中かどうか
     */
    public boolean isNodeCollision(String nodeName) {

        if (nodeName == null) {
            return false;
        }

        return mCollisionNodeName.contains(nodeName);
    }

    /*
     * 本キャラクターが指定Nodeをステージから除外したことがあるかどうか
     */
    public boolean isEverRemovedNode(String nodeName) {

        //------------------
        // 除外リストから検索
        //------------------
        for( Node node: mRemovedNodeList ){
            if( node.getName().equals( nodeName ) ){
                // 除外リストにあれば、除外したことあり
                return true;
            }
        }

        // 除外したことなし
        return false;
    }

    /*
     * 検索対象外リストクリア
     */
    public void clearNotSearchNodeList() {
        // 検索対象外リストクリア
        mNotSearchNodeList.clear();
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
     * プログラミング実行中状態の取得
     */
    public boolean getNotifyProgrammingEnd() {
        return mNotifyProgrammingEnd;
    }

    /*
     * 検索対象外Nodeリストを取得
     */
    public List<Node> getNotSearchNodeList() {
        return mNotSearchNodeList;
    }

    /*
     * 除外Nodeリストを取得
     */
    public List<Node> getRemovedNodeList() {
        return mRemovedNodeList;
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
