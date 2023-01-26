package com.ar.ar_programming;

import static android.content.Context.MODE_PRIVATE;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ar.ar_programming.databinding.FragmentArBinding;
import com.ar.ar_programming.process.Block;
import com.ar.ar_programming.process.IfElseIfElseBlock;
import com.ar.ar_programming.process.IfElseBlock;
import com.ar.ar_programming.process.IfBlock;
import com.ar.ar_programming.process.LoopBlock;
import com.ar.ar_programming.process.NestBlock;
import com.ar.ar_programming.process.ProcessBlock;
import com.ar.ar_programming.process.SingleBlock;
import com.ar.ar_programming.process.StartBlock;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.PlaneRenderer;
import com.google.ar.sceneform.rendering.Texture;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.ux.TransformationSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ArMainFragment extends Fragment implements ARActivity.MenuClickListener, ARActivity.PlayControlListener, Block.MarkerAreaListener, Block.DropBlockListener, ProcessBlock.ProcessListener {

    //---------------------------
    // 定数
    //---------------------------
    // Node名
    private final String NODE_NAME_ANCHOR = "anchorNode";
    public static final String NODE_NAME_STAGE = "stageNode";
    public static final String NODE_NAME_GOAL = "goalNode";
    public static final String NODE_NAME_BLOCK = "blockNode";
    public static final String NODE_NAME_OBSTACLE = "obstacleNode";
    public static final String NODE_NAME_GOAL_GUIDE_UI = "goalGuideUINode";

    // ステージ4辺
    private final int STAGE_BOTTOM = 0;
    private final int STAGE_TOP = 1;
    private final int STAGE_LEFT = 2;
    private final int STAGE_RIGHT = 3;
    private final int STAGE_4_SIDE = 4;

    // ステージサイズ
    private final float STAGE_SIZE_S = 0.3f;
    private final float STAGE_SIZE_M = 0.6f;
    private final float STAGE_SIZE_L = 1.0f;
    // ステージサイズ倍率
    private final float STAGE_RATIO_S = 1.0f;
    private final float STAGE_RATIO_M = 5.0f;
    private final float STAGE_RATIO_L = 10.0f;
    private final float STAGE_RATIO_XL = 50.0f;
    // ノードサイズ
//    public static final float NODE_SIZE_TMP_RATIO = 0.1f;
    public static final float NODE_SIZE_TMP_RATIO = 1f;
    public static final float NODE_SIZE_S = 0.02f;
    public static final float NODE_SIZE_M = 0.5f;
    public static final float NODE_SIZE_L = 1.0f;
    public static final float NODE_SIZE_XL = 5.0f;

    // Play状態
    private final int PLAY_STATE_INIT = 0;              // ステージ配置前
    private final int PLAY_STATE_PROGRAMMING = 1;       // プログラミング中（ゲーム開始前）
    private final int PLAY_STATE_PLAYING = 2;           // ゲーム中

    //---------------------------
    // フィールド変数
    //---------------------------
    private FragmentArBinding binding;
    private ArFragment arFragment;
    private ModelRenderable mStageRenderable;
    private ModelRenderable mCharacterRenderable;
    private ModelRenderable mGoalRenderable;
    private ViewRenderable mGuideViewRenderable;
    private ArrayList<ModelRenderable> mObjectRenderable;

    private CharacterNode mCharacterNode;
    private Block mMarkedBlock;         // ブロック下部追加マーカーの付与されている処理ブロック

    private int mPlayState;             // Play状態
    private Gimmick mGimmick;             // ステージギミックID

    private ActivityResultLauncher<Intent> mSettingRegistrationLancher;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentArBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ArFragmentを保持
        arFragment = (com.google.ar.sceneform.ux.ArFragment) getChildFragmentManager().findFragmentById(R.id.sceneform_fragment);
        // ゲーム状態初期化
        mPlayState = PLAY_STATE_INIT;
        // 障害物Renderableリスト
        mObjectRenderable = new ArrayList<>();
        // ステージギミックを選出
        mGimmick = GimmickManager.getGimmick(getContext());

        // 画面遷移ランチャー生成
        setSettingRegistrationLancher();

        // 生成元Activityのmenuアクションリスナーを設定
        setMenuAction();
        // プログラミングUIの設定
        setProgrammingUI();
        // 3Dモデルレンダリング初期生成
        initRenderable();
        // お試し：平面ドットのビジュアル変更
        setPlaneVisual(view.getContext());
        // 平面タップリスナーの設定
        setTapPlaneListener(arFragment);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /*
     * 生成元Activityのmenuアクションリスナー設定
     */
    private void setMenuAction() {

        ARActivity arActivity = (ARActivity) getActivity();
        // Menuリスナー
        arActivity.setOnMenuClickListener(this);
        // ゲーム制御Fabリスナー
        arActivity.setPlayControlListener(this);
    }

    /*
     * プログラミングUIの設定
     */
    private void setProgrammingUI() {

        // マーカ―はスタートブロックに付与
        initStartBlock();
        // 処理ブロック削除エリア設定
        setRemoveBlockArea();
        // 処理ブロックリストアダプタの設定
        setSelectProcessBlockList();
    }

    /*
     * ゴール説明ダイアログの表示
     */
    private void showGoalGuideDialog() {
        DialogFragment newFragment = new GoalExplanationDialogFragment( mGimmick.goalExplanationIdList );
        newFragment.show(getActivity().getSupportFragmentManager(), "goalGuide");
    }

    /*
     * スタートブロック初期化処理
     */
    private void initStartBlock() {
        Block startBlock = binding.getRoot().findViewById(R.id.pb_chartTop);
        startBlock.setLayout(R.layout.process_block_start_ver2);
        startBlock.setMarkAreaListerner(this);
        startBlock.setDropBlockListerner(this);

        // マーカ―ブロック設定
        mMarkedBlock = startBlock;
    }

    /*
     * 画面遷移ランチャーの設定：設定画面遷移用
     */
    private void setSettingRegistrationLancher() {

        mSettingRegistrationLancher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        // ResultCodeの取得
                        int resultCode = result.getResultCode();
                        if (resultCode != SettingActivity.RESULT_SETTING) {
                            // フェールセーフ
                            return;
                        }

                        // 設定変更された場合
                        Intent intent = result.getData();
                        boolean isChanged = intent.getBooleanExtra(SettingActivity.IS_CHANGED_KEY, false);
                        if (isChanged) {
                            // ゲーム状態を初期化
                            initGameState();
                        }
                    }
                });
    }


    /*
     * ゲーム開始可能か判定
     */
    private boolean enableStartGame() {

        //----------------------
        // 処理ブロック数チェック
        //----------------------
        // ブロックがなにもないなら、メッセージを表示して終了
        ViewGroup root = binding.getRoot();
        StartBlock startBlock = root.findViewById(R.id.pb_chartTop);
        if (!startBlock.hasBelowBlock()) {
            Snackbar.make(root, getString(R.string.snackbar_please_programming), Snackbar.LENGTH_SHORT).show();
            return false;
        }

        //-------------------------------
        // ループブロック内ブロック数チェック
        //-------------------------------
        //★優先度低；ループ内ブロック数がない場合はじく
//        ViewGroup ll_UIRoot = binding.getRoot().findViewById(R.id.ll_UIRoot);
//        int childNum = ll_UIRoot.getChildCount();
//        for( int i = 0; i < childNum; i++ ){
//            View childView = ll_UIRoot.getChildAt( i );
//            // ループブロックに対して、ネスト内ブロック数をチェック
//            if (childView instanceof LoopProcessBlock) {
//
//            }
//        }

        return true;
    }

    /*
     * ゲーム開始
     */
    private void startGame(FloatingActionButton fab) {

        //----------------------
        // ゲーム開始可能判定
        //----------------------
        if (!enableStartGame()) {
            return;
        }

        //----------------------
        // プログラミング開始
        //----------------------
        ViewGroup root = binding.getRoot();
        StartBlock startBlock = root.findViewById(R.id.pb_chartTop);

        // 処理開始
        ProcessBlock block = (ProcessBlock) startBlock.getBelowBlock();
        runProcessBlock(block);

        // ゲーム状態更新
        mPlayState = PLAY_STATE_PLAYING;

        // Fabアイコンを切り替え
        fab.setImageResource(R.drawable.baseline_replay_24);
    }

    /*
     * ゲームリトライ確認
     */
    private void confirmRetryGame(FloatingActionButton fab) {

        // リトライ確認ダイアログを表示
        new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle)
                .setTitle(getString(R.string.ar_dialog_title))
                .setMessage(getString(R.string.ar_dialog_contents))
                .setPositiveButton(getString(R.string.ar_dialog_positive), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // ゲームをリセット
                        returnGameToStart();
                        // Fabアイコンを切り替え
                        fab.setImageResource(R.drawable.baseline_play_24);
                    }
                })
                .setNegativeButton(getString(R.string.ar_dialog_negative), null)
                .show();
    }

    /*
     * ブロック削除エリア設定
     */
    private void setRemoveBlockArea() {

        // 削除エリア
        ViewGroup root = binding.getRoot();
        View iv_removeBlock = root.findViewById(R.id.iv_removeBlock);

        iv_removeBlock.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {

                switch (dragEvent.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                    case DragEvent.ACTION_DRAG_ENTERED:
                    case DragEvent.ACTION_DRAG_LOCATION:
                    case DragEvent.ACTION_DRAG_EXITED:
                    case DragEvent.ACTION_DRAG_ENDED:
                        return true;

                    case DragEvent.ACTION_DROP:
                        // ドラッグ中ブロックを処理ラインから削除
                        removeBlockFromLine((ProcessBlock) dragEvent.getLocalState());
                        return true;

                    default:
                        break;
                }

                return false;
            }
        });
    }

    /*
     * ゲームリセット
     *   キャラクター位置、プログラミング状態を初期状態に戻す
     */
    private void returnGameToStart() {

        // キャラクター位置リセット
        mCharacterNode.positionReset();

    }

    /*
     * 処理ブロック開始
     */
    private void runProcessBlock(ProcessBlock block) {

        Log.i("ループ処理", "" + (new Object() {
        }.getClass().getEnclosingMethod().getName()));

        // ブロック処理を開始
        block.startProcess(mCharacterNode);
    }

    /*
     * 処理ブロックリスト設定
     */
    private void setSelectProcessBlockList() {

        ViewGroup root = binding.getRoot();

        //---------------------
        // 処理ブロック選択リスト
        //---------------------
        // 処理ブロックイメージリストを取得し、アダプタを生成
        UserBlockSelectListAdapter adapter = new UserBlockSelectListAdapter(mGimmick.xmlBlockInfoList);

        // スクロール方向を用意
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        // アダプタ設定
        RecyclerView rv_selectBlock = root.findViewById(R.id.rv_selectBlock);
        rv_selectBlock.setAdapter(adapter);
        rv_selectBlock.setLayoutManager(linearLayoutManager);

        // 処理ブロッククリックリスナーの設定
        adapter.setOnBlockClickListener(new UserBlockSelectListAdapter.BlockClickListener() {
            @Override
            public void onBlockClick(int selectBlockType, int selectBlockContents, int valueLimit) {
                // クリックされたブロックを生成
                createProcessBlock(selectBlockType, selectBlockContents, valueLimit);
            }
        });
    }

    /*
     * チャート最下部に処理ブロックを生成する
     */
    private void createProcessBlock(int blockType, int blockContents, int valueLimit) {

        ProcessBlock newBlock;
        Context context = getContext();

        //-----------------
        // 処理ブロック生成
        //-----------------
        switch (blockType) {
            // 単体処理
            case Block.PROCESS_TYPE_SINGLE:
                newBlock = new SingleBlock(context, getParentFragmentManager(), blockContents, valueLimit);
                break;

            // ネスト処理
            case Block.PROCESS_TYPE_IF:
                newBlock = new IfBlock(context, blockContents);
                break;

            case Block.PROCESS_TYPE_IF_ELSE:
                newBlock = new IfElseBlock(context, blockContents);
                break;

            case Block.PROCESS_TYPE_IF_ELSEIF_ELSE:
                newBlock = new IfElseIfElseBlock(context, blockContents);
                break;

            case Block.PROCESS_TYPE_LOOP:
                newBlock = new LoopBlock(context, blockContents);
                break;

            default:
                // 種別指定がおかしければ、何もしない
                Log.i("ブロックxml", "blockType=" + blockType);
                return;
        }

        //----------------------
        // リスナー設定
        //----------------------
        // 全ブロック共通
        newBlock.setMarkAreaListerner(this);
        newBlock.setDropBlockListerner(this);
        newBlock.setProcessListener(this);

        //-------------------------------
        // ネスト情報の書き換え
        //-------------------------------
        NestBlock nestBlock = mMarkedBlock.getOwnNestBlock();
        newBlock.setOwnNestBlock(nestBlock);

        //----------------------
        // チャートに追加
        //----------------------
        // 「マークブロック」の下に追加
        insertNewBlock(mMarkedBlock, newBlock);

        // マーカーブロックを新ブロックに変更
        changeMarkerBlock(newBlock);
    }

    /*
     * ブロックの下に指定されたブロックを挿入する
     *   @para1:挿入先ブロック（本ブロックの下にブロックが挿入される）
     *   @para2:挿入ブロック
     */
    private void insertNewBlock(Block aboveBlock, ProcessBlock newBlock) {

        //-------------------------
        // 上下ブロックの保持情報更新
        //-------------------------
        rewriteAboveBelowBlockOnInsert(aboveBlock, newBlock);

        //-------------------------------
        // ブロックをレイアウトに追加
        //-------------------------------
        // !WRAP_CONTENT必須（未指定の場合、MATCH_PARENTが適用され、ブロックの高さが親レイアウトと同じになるため）
        FrameLayout ll_UIRoot = binding.getRoot().findViewById(R.id.ll_UIRoot);
        ViewGroup.MarginLayoutParams mlp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        ll_UIRoot.addView(newBlock, mlp);

        // 生成するブロックがネストブロックの場合
        if (newBlock instanceof NestBlock) {
            // ネスト内スタートブロックの配置
            ((NestBlock) newBlock).deployStartBlock(ll_UIRoot);
        }

        // 生成ブロック位置を更新
        newBlock.updatePosition();
    }


    /*
     * 3Dモデルレンダリング初期生成
     */
    private void initRenderable() {

        //----------------------------------------
        // ギミックに応じたRenderableを生成
        //----------------------------------------
        // ステージ
        buildRenderableStage(mGimmick);
        // キャラクター
        buildRenderableCharacter(mGimmick);
        // オブジェクト
        buildRenderableObjects(mGimmick);
        // ゴール
        buildRenderableGoal(mGimmick);
        // ゴール説明UI
        buildRenderableGuideView(mGimmick);
    }

    /*
     * ギミックリスト候補を取得
     */
    private TypedArray getGimmickList() {

        // ユーザーの設定しているキャラクターと難易度
        SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);
        int defaultCharacter = getResources().getInteger(R.integer.saved_character_default_key);
        int defaultDifficulty = getResources().getInteger(R.integer.saved_difficulty_default_key);
        int character = sharedPref.getInt(getString(R.string.saved_character_key), defaultCharacter);
        int difficulty = sharedPref.getInt(getString(R.string.saved_difficulty_key), defaultDifficulty);

        String characterStr = (character == SettingActivity.CHARACTER_ANIMAL ? "animal" : "vehicle");
        String difficultyStr = (difficulty == SettingActivity.PLAY_DIFFICULTY_EASY ? "easy" : "difficult");

        String arrayName = "gimmick_list_" + characterStr + "_" + difficultyStr;

        int arrayId = getResources().getIdentifier(arrayName, "array", getActivity().getPackageName());

        Log.i("ギミック", "arrayName=" + arrayName);

        return getResources().obtainTypedArray(arrayId);
    }

    /*
     * 上下ブロック保持情報の更新（ブロック挿入時）
     */
    private void rewriteAboveBelowBlockOnInsert(Block aboveBlock, Block insertBlock) {

        // 挿入前の「挿入ブロックの上ブロック」の下ブロック
        Block belowBlock = aboveBlock.getBelowBlock();

        // 挿入ブロックの保持情報を更新
        insertBlock.setAboveBlock(aboveBlock);
        insertBlock.setBelowBlock(belowBlock);

        // 「新規ブロックの上のブロック」の下ブロックを「新規ブロック」にする
        aboveBlock.setBelowBlock(insertBlock);

        // 「新規ブロックの１つ下ブロック（あれば）」の上ブロックを「新規ブロック」にする
        if (belowBlock != null) {
            belowBlock.setAboveBlock(insertBlock);
        }
    }

    /*
     * 上下ブロック保持情報の更新（ブロック削除時）
     */
    private void rewriteAboveBelowBlockOnRemove(Block removeBlock) {

        // 削除ブロックの上下ブロック
        Block aboveBlock = removeBlock.getAboveBlock();
        Block belowBlock = removeBlock.getBelowBlock();

        // 上下ブロックの保持情報を更新
        aboveBlock.setBelowBlock(belowBlock);
        if (belowBlock != null) {
            belowBlock.setAboveBlock(aboveBlock);
        }
    }

    /*
     * 上下ブロック保持情報の更新（ブロック移動時）
     */
    private void rewriteAboveBelowBlockOnDrop(Block dropBlock, Block moveBlock) {

        //-------------------
        // 上下情報の更新
        //-------------------
        // 移動先の下ブロック
        Block dropBelowBlock = dropBlock.getBelowBlock();

        // 移動ブロックの上下ブロック
        Block moveAboveBlock = moveBlock.getAboveBlock();
        Block moveBelowBlock = moveBlock.getBelowBlock();

        // 移動ブロックを移動先に差し込む
        dropBlock.setBelowBlock(moveBlock);
        moveBlock.setAboveBlock(dropBlock);
        moveBlock.setBelowBlock(dropBelowBlock);
        if (dropBelowBlock != null) {
            dropBelowBlock.setAboveBlock(moveBlock);
        }

        // 移動ブロックの移動前の上下ブロックを繋げる
        moveAboveBlock.setBelowBlock(moveBelowBlock);
        if (moveBelowBlock != null) {
            moveBelowBlock.setAboveBlock(moveAboveBlock);
        }
    }

    /*
     * マーカー入れ替え判定
     *   引数に指定されたブロックを削除するとき、
     *   マーカーを入れ替える必要があるか判定する
     */
    private boolean isNeedChangeMark(ProcessBlock block) {

        // マーク付きなら入れ替え
        if (block.isMarked()) {
            return true;
        }

        //------------------
        // ネスト処理ブロック
        //------------------
        int type = block.getProcessType();
        if (type != ProcessBlock.PROCESS_TYPE_SINGLE) {
            // ネスト内にマークブロックがあるかどうか
            return block.hasBlock(mMarkedBlock);
        }

        return false;
    }

    /*
     * 処理ブロックのドラッグ移動
     *   「ドラッグされたブロック」を「ドロップ先ブロック」の下に移動させる
     */
    private void dragMoveUnderDrop(Block dropBlock, ProcessBlock moveBlock) {
        // 上下情報の更新
        rewriteAboveBelowBlockOnDrop(dropBlock, moveBlock);
        // 親ネストの更新
        moveBlock.setOwnNestBlock(dropBlock.getOwnNestBlock());
        // ブロック位置をチャート先頭から更新
        updatePositionFromTop();
    }

    /*
     * チャート先頭ブロック位置更新
     */
    private void updatePositionFromTop() {

        // チャートスタートブロックの下ブロックを取得
        StartBlock pb_chartTop = binding.getRoot().findViewById(R.id.pb_chartTop);
        Block updateStart = pb_chartTop.getBelowBlock();
        if (updateStart == null) {
            // ブロックなしなら処理なし
            return;
        }

        // 位置更新
        updateStart.updatePosition();
    }

    /*
     * 指定ブロックを処理ラインから削除する
     */
    private void removeBlockFromLine(ProcessBlock removeBlock) {

        //-------------------
        // マーカー変更判定
        //-------------------
        // 削除ブロックがマーカー or 削除ブロック内にマーカーブロックあり
        if (isNeedChangeMark(removeBlock)) {
            // 削除対象の1つ上のブロックにマークを設定する
            Block aboveBlock = removeBlock.getAboveBlock();
            changeMarkerBlock(aboveBlock);
        }

        //-------------------
        // ブロック削除
        //-------------------
        // 上下ブロック保持情報の更新
        rewriteAboveBelowBlockOnRemove(removeBlock);
        // 自身をチャートから削除
        removeBlock.removeOnChart();
        // 削除ブロックの上ブロックから更新
        Block aboveBlock = removeBlock.getAboveBlock();
        aboveBlock.updatePosition();
    }

    /*
     * マーカーブロックの変更
     */
    public void changeMarkerBlock(Block newMarkerBlock) {
        // 現在のマーカーを切り替え
        mMarkedBlock.setMarker(false);
        newMarkerBlock.setMarker(true);

        // 新しいマーカーブロックを保持
        mMarkedBlock = newMarkerBlock;
    }

    /*
     * 3Dモデルレンダリング「ModelRenderable」の生成：ステージ
     */
    private void buildRenderableStage(Gimmick gimmick) {

        Context context = getContext();
        mStageRenderable = null;

        // Renderable生成
        ModelRenderable
                .builder()
                .setSource(context, Uri.parse(gimmick.stageGlb))
                .setIsFilamentGltf(true)    // glbファイルを読み込む必須
                .build()
                .thenAccept(renderable -> mStageRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            //!string
                            Toast toast = Toast.makeText(context, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

    }

    /*
     * 3Dモデルレンダリング「ModelRenderable」の生成
     */
    private void buildRenderableCharacter(Gimmick gimmick) {

        Context context = getContext();

        Log.i("ギミックxml", "gimmick.goalGlb=" + gimmick.goalGlb);
        Log.i("ギミックxml", "gimmick.stageGlb=" + gimmick.stageGlb);
        Log.i("ギミックxml", "gimmick.characterGlb=" + gimmick.characterGlb);
        Log.i("ギミックxml", "gimmick.x=" + gimmick.characterPositionVec.x);
        Log.i("ギミックxml", "gimmick.y=" + gimmick.characterPositionVec.y);
        Log.i("ギミックxml", "gimmick.z=" + gimmick.characterPositionVec.z);

        mCharacterRenderable = null;

        // Renderable生成
        ModelRenderable
                .builder()
                .setSource(context, Uri.parse(gimmick.characterGlb))
                .setIsFilamentGltf(true)    // glbファイルを読み込む必須
                .build()
                .thenAccept(renderable -> mCharacterRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            //!string
                            Toast toast = Toast.makeText(context, "失敗 Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

    }

    /*
     * 3Dモデルレンダリング「ステージ上障害物」の生成
     */
    private void buildRenderableObjects(Gimmick gimmick) {

        Context context = getContext();

        mObjectRenderable.clear();

        //---------------------------------------------
        // ギミックのオブジェクトリストからRenderable生成
        //---------------------------------------------
        // オブジェクトの種類分レンダラブルを生成
        int objectNum = gimmick.objectGlbList.size();
        for (int i = 0; i < objectNum; i++) {

            // gleファイル名（パス付き）
            String glbFilename = gimmick.objectGlbList.get(i);

            Log.i("ギミックxml", "glbFilename=" + glbFilename);

            // Renderable生成
            ModelRenderable
                    .builder()
                    .setSource(context, Uri.parse(glbFilename))
                    .setIsFilamentGltf(true)    // glbファイルを読み込む必須
                    .build()
                    .thenAccept(renderable -> mObjectRenderable.add(renderable))
                    .exceptionally(
                            throwable -> {
                                //!
                                Toast toast = Toast.makeText(context, "Unable to load andy renderable", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                return null;
                            });

        }
    }

    /*
     * 3Dモデルレンダリング「ゴール」の生成
     */
    private void buildRenderableGoal(Gimmick gimmick) {

        Context context = getContext();
        mGoalRenderable = null;

        //-------------------
        // ゴール
        //-------------------
        ModelRenderable
                .builder()
                .setSource(context, Uri.parse(gimmick.goalGlb))
                .setIsFilamentGltf(true)    // glbファイルを読み込む必須
                .build()
                .thenAccept(renderable -> mGoalRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            //!
                            Toast toast = Toast.makeText(context, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });
    }

    /*
     * 3Dモデルレンダリング「ゴール説明view」の生成
     */
    private void buildRenderableGuideView(Gimmick gimmick) {

        Context context = getContext();
        mGuideViewRenderable = null;

        //-------------------
        // ゴール
        //-------------------
        ViewRenderable
                .builder()
                .setView(context, R.layout.goal_guide_ui)
                .build()
                .thenAccept(renderable -> mGuideViewRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            //!
                            Toast toast = Toast.makeText(context, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });
    }

    /*
     * キャラクターノード生成
     */
    private void createNodeCharacter(AnchorNode anchorNode) {

        Scene scene = arFragment.getArSceneView().getScene();

        // キャラクターサイズ
        // 「* 0.1f」は暫定処理。3Dモデルの大きさに合わせる
        float scale = getNodeScale() * NODE_SIZE_TMP_RATIO;

        //------------------------------------
        // キャラクター生成と他Nodeとの重複なしの配置
        //------------------------------------
        CharacterNode characterNode = createTemporaryCharacterNode(anchorNode, scale);

        // 重複しない配置になるまで、繰り返し
/*        while (true) {
            // 生成
            characterNode = createTemporaryCharacterNode(anchorNode, scale);

            // 他のNodeと重複していなければ、生成終了
            ArrayList<Node> nodes = scene.overlapTestAll(characterNode);
            if (nodes.size() < 1) {
                break;
            }

            Log.i("ギミックxml", "createNodeCharacter() 再配置中");
            for( Node node: nodes ){
                Log.i("ギミックxml", "衝突判定=" + node.getName());
            }
            Log.i("ギミックxml", "=========================");

            // 重複していれば、そのキャラクターは削除してもう一度配置をやり直し
            anchorNode.removeChild(characterNode);
        }*/

        // ステージ上のキャラクターとして保持
        mCharacterNode = characterNode;

//        Log.i("向いている方向ロジック", "scene判定 arFragment=" + scene);
//        Log.i("向いている方向ロジック", "scene判定 mCharacterNode=" + mCharacterNode.getScene());

        // 衝突検知リスナーの設定
        mCharacterNode.setOnCollisionDetectListener(new CharacterNode.CollisionDetectListener() {
            @Override
            public void onCollisionDetect(int collisionType, ValueAnimator animator) {

                // 衝突に応じた処理
                switch (collisionType) {

                    case CharacterNode.COLLISION_TYPE_GOAL:
                        // ゴール成功処理
                        // アニメーション終了
                        animator.cancel();
                        Snackbar.make(binding.getRoot(), "Goal", Snackbar.LENGTH_SHORT).show();
                        break;

                    case CharacterNode.COLLISION_TYPE_OBSTACLE:
                        // ゴール失敗処理
                        animator.cancel();
                        Snackbar.make(binding.getRoot(), "失敗", Snackbar.LENGTH_SHORT).show();
                        break;

                    case CharacterNode.COLLISION_TYPE_BLOCK:
                        // 本アニメーション終了
//                        animator.end();
//                        Snackbar.make(binding.getRoot(), "衝突（継続可）", Snackbar.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    /*
     * キャラクターノード生成：
     */
    private CharacterNode createTemporaryCharacterNode(AnchorNode anchorNode, float scale) {

        //------------------------------------
        // キャラクターの生成位置／向く方向／サイズ
        //------------------------------------
        // 四辺の内の配置辺
        Random random = new Random();
        int side = random.nextInt(STAGE_4_SIDE);

/*
        // キャラクター初期位置
        Vector3 position = getCharacterInitPosition(side);
        // キャラクターに向かせる角度
        float angle = getCharacterInitFacingAngle(side);
        // キャラクターに向かせる方向のQuaternion値
        Quaternion facingDirection = getCharacterInitFacingDirection(angle);
*/

        // キャラクターに向かせる角度
        //!xmlで管理するかも
        float angle = 180;
        // キャラクターに向かせる方向のQuaternion値
        Quaternion facingDirection = getCharacterInitFacingDirection(angle);

        // 下辺配置を前提とする
        Vector3 position = new Vector3(mGimmick.characterPositionVec.x * scale, mGimmick.characterPositionVec.y * scale, mGimmick.characterPositionVec.z * scale);

        //------------------------
        // キャラクターの生成
        //------------------------
        TransformationSystem transformationSystem = arFragment.getTransformationSystem();

        // AnchorNodeを親として、モデル情報からNodeを生成
        CharacterNode characterNode = new CharacterNode(transformationSystem);
        characterNode.getScaleController().setMinScale(scale);
        characterNode.getScaleController().setMaxScale(scale * 2);
        characterNode.setLocalScale(new Vector3(scale, scale, scale));
        characterNode.setParent(anchorNode);
        characterNode.setLocalPosition(position);
        characterNode.setLocalRotation(facingDirection);
        characterNode.setRenderable(mCharacterRenderable);
        characterNode.select();

        // アニメーション初期化処理：必須
        characterNode.initAnimation();
        characterNode.startPosData(position, angle);

        return characterNode;
    }

    /*
     * ステージ上のオブジェクト生成
     */
    private void createNodeObject(AnchorNode anchorNode) {

        TransformationSystem transformationSystem = arFragment.getTransformationSystem();

        // Nodeスケール
        final float scale = getNodeScale();
        Vector3 scaleVector = new Vector3(scale, scale, scale);

        // ステージの広さ
        float stageScale = getStageScale();

        //-----------------------------
        // オブジェクトのNode生成
        //-----------------------------
        int objKindIndex = 0;
        for (ModelRenderable renderable : mObjectRenderable) {

            // オブジェクトの種類をNode名として設定する
            String objectKind = mGimmick.objectKindList.get(objKindIndex);
            int objectNum = mGimmick.objectNumList.get(objKindIndex);

            // Node生成
            for (int num = 0; num < objectNum; num++) {

                Vector3 pos;
                if (mGimmick.objectPositionRandom) {
                    // ランダム位置を生成
                    pos = getRandomPosition(stageScale);
                } else {
                    // 指定位置に設定
                    pos = mGimmick.objectPositionVecList.get(objKindIndex);
                    pos = new Vector3(pos.x * scale, pos.y * scale, pos.z * scale);
                }

                // Node生成
                TransformableNode node = new TransformableNode(transformationSystem);
                node.setName(objectKind);
                node.getScaleController().setMinScale(scale);
                node.getScaleController().setMaxScale(scale * 2);
                node.setLocalScale(scaleVector);
                node.setParent(anchorNode);
                node.setLocalPosition(pos);
                node.setRenderable(renderable);
                node.select();
            }

            objKindIndex++;
        }
    }


    /*
     * 目標説明UIの生成
     */
    private void createNodeGoalGuideUI(AnchorNode anchorNode) {

        TransformationSystem transformationSystem = arFragment.getTransformationSystem();

        // 配置位置
        final float GUIDE_POS_Y = 0.05f;    // 高さ
        final float GUIDE_POS_Z = -0.6f;  // 奥方向へ
        Vector3 pos = new Vector3(0f, GUIDE_POS_Y, GUIDE_POS_Z);

        // Node生成
        TransformableNode node = new TransformableNode(transformationSystem);
        node.setName(NODE_NAME_GOAL_GUIDE_UI);
//        node.getScaleController().setMinScale(1);
//        node.getScaleController().setMaxScale(1);
//        node.setLocalScale(scaleVector);
        node.setParent(anchorNode);
        node.setLocalPosition(pos);
        node.setRenderable(mGuideViewRenderable);
        node.select();

        //------------------------
        // タッチ時のゴール目標表示
        //------------------------
        node.setOnTapListener(new Node.OnTapListener() {
            @Override
            public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                // ゴール説明ダイアログの表示
                showGoalGuideDialog();
                // アンカーから本UIを削除
                anchorNode.removeChild( node );
            }
        });
    }

    /*
     * ステージNode生成
     */
    private void createNodeStage(AnchorNode anchorNode) {

        TransformationSystem transformationSystem = arFragment.getTransformationSystem();

        // Nodeスケール
        final float scale = getNodeScale();
        Vector3 scaleVector = new Vector3(scale, scale, scale);

        /*// ステージの広さ
        float stageScale = getStageScale();
        // ランダム位置を生成
        Vector3 pos = getRandomPosition(stageScale);
        */

        Log.i("ギミックxml", "createNodeStage()");

        // Node生成
        TransformableNode node = new TransformableNode(transformationSystem);
        node.setName(NODE_NAME_STAGE);
        node.getScaleController().setMinScale(scale);
        node.getScaleController().setMaxScale(scale * 2);
        node.setLocalScale(scaleVector);
        node.setParent(anchorNode);
        node.setLocalPosition(new Vector3(0f, 0f, 0f));
        node.setRenderable(mStageRenderable);
        node.select();
    }

    /*
     * ステージ上のゴールNode生成
     */
    private void createNodeGoal(AnchorNode anchorNode) {

        TransformationSystem transformationSystem = arFragment.getTransformationSystem();

        // Nodeスケール
        final float scale = getNodeScale();
        Vector3 scaleVector = new Vector3(scale, scale, scale);

        /*// ステージの広さ
        float stageScale = getStageScale();
        // ランダム位置を生成
        Vector3 pos = getRandomPosition(stageScale);
        */

        // キャラクターに向かせる角度
        //!xmlで管理するかも
        float angle = mGimmick.goalAngle;
        // キャラクターに向かせる方向のQuaternion値
        Quaternion facingDirection = getCharacterInitFacingDirection(angle);

        Log.i("ギミックxml", "createNodeGoal()");
        Log.i("ギミックxml", "mGimmick.goalPositionVecx" + mGimmick.goalPositionVec.x);
        Log.i("ギミックxml", "mGimmick.goalPositionVecy" + mGimmick.goalPositionVec.y);
        Log.i("ギミックxml", "mGimmick.goalPositionVecz" + mGimmick.goalPositionVec.z);

        Vector3 scalePos = new Vector3(mGimmick.goalPositionVec.x * scale, mGimmick.goalPositionVec.y * scale, mGimmick.goalPositionVec.z * scale);

        // Node生成
        TransformableNode node = new TransformableNode(transformationSystem);
        node.setName(NODE_NAME_GOAL);
        node.getScaleController().setMinScale(scale);
        node.getScaleController().setMaxScale(scale * 2);
        node.setLocalScale(scaleVector);
        node.setParent(anchorNode);
//        node.setLocalPosition( mGimmick.goalPositionVec );
        node.setLocalPosition(scalePos);
        node.setLocalRotation(facingDirection);
        node.setRenderable(mGoalRenderable);
        node.select();
    }

    /*
     * ユーザー指定のNodeサイズの取得
     */
    private float getNodeScale() {

        // ユーザーの指定したフィールドサイズを取得
        SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);
        int defaultValue = getResources().getInteger(R.integer.saved_field_size_default_key);
        int fieldSize = sharedPref.getInt(getString(R.string.saved_field_size_key), defaultValue);

        switch (fieldSize) {
            case SettingActivity.FIELD_SIZE_TABLE:
                return NODE_SIZE_S;
            case SettingActivity.FIELD_SIZE_LIVING:
                return NODE_SIZE_M;
            case 2:
                return NODE_SIZE_L;
            case 3:
            default:
                return NODE_SIZE_XL;
        }
    }

    /*
     * ステージサイズの倍率を取得
     */
    private float getStageScaleRatio() {

        // ユーザーの指定したフィールドサイズを取得
        SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);
        int defaultValue = getResources().getInteger(R.integer.saved_field_size_default_key);
        int fieldSize = sharedPref.getInt(getString(R.string.saved_field_size_key), defaultValue);

        switch (fieldSize) {
            case SettingActivity.FIELD_SIZE_TABLE:
                return STAGE_RATIO_S;
            case SettingActivity.FIELD_SIZE_LIVING:
                return STAGE_RATIO_M;
            case 2:
                return STAGE_RATIO_L;
            case 3:
            default:
                return STAGE_RATIO_XL;
        }
    }

    /*
     * ユーザー指定のステージサイズの取得
     */
    private float getStageScale() {

        // ユーザーの指定したフィールドサイズを取得
        SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);
        int defaultValue = getResources().getInteger(R.integer.saved_field_size_default_key);
        int fieldSize = sharedPref.getInt(getString(R.string.saved_field_size_key), defaultValue);

        // ステージサイズの倍率
        float stageRatio = getStageScaleRatio();

        // ステージサイズを算出
        switch (fieldSize) {
            case SettingActivity.FIELD_SIZE_TABLE:
                return (STAGE_SIZE_S * stageRatio);
            case SettingActivity.FIELD_SIZE_LIVING:
                return (STAGE_SIZE_M * stageRatio);
            case 2:
            default:
                return (STAGE_SIZE_L * stageRatio);
        }
    }

    /*
     * ステージ上のランダム位置の取得
     */
    private Vector3 getRandomPosition(float stageScale) {

        int scale = (int) (stageScale * 100f) + 1;

        Random random = new Random();
        float positionx = random.nextInt(scale) / 100f;
        float positionz = random.nextInt(scale) / 100f;

        Log.i("ランダム位置", "positionx=" + positionx);
        Log.i("ランダム位置", "positionz=" + positionz);
        Log.i("ランダム位置", "----------------");

        return new Vector3(-positionx, -0.0f, -positionz);
    }

    /*
     * ランダムにキャラクター初期位置の取得
     */
    private Vector3 getCharacterInitPosition(int side) {

        // ステージサイズ
        final float stageSize = getStageScale();

        // 位置
        Vector3 position = new Vector3();
        position.y = 0.0f;

        float posx;
        float posz;

        // 1辺の間でのランダム値を取得
        Random random = new Random();
        float randomPos = random.nextInt((int) (stageSize * 100) + 1) / 100f;

        // 4辺毎にランダム位置を切り分け
        switch (side) {
            case STAGE_BOTTOM:
                posx = randomPos;
                posz = 0.0f;
                break;

            case STAGE_TOP:
                posx = randomPos;
                posz = stageSize;
                break;

            case STAGE_LEFT:
                posx = stageSize;
                posz = randomPos;
                break;

            case STAGE_RIGHT:
                posx = 0.0f;
                posz = randomPos;
                break;

            default:
                posx = randomPos;
                posz = 0.0f;
                break;
        }

        // ステージの右下を原点としており、ｘ：左方向／ｚ：奥方向 とするため、マイナス設定
        position.x = posx * (-1);
        position.z = posz * (-1);

        return position;
    }

    /*
     * キャラクターが配置された四辺に応じて、向きを取得
     */
    private float getCharacterInitFacingAngle(int side) {

        // 角度
        float angle;

        // 4辺毎にランダム位置を切り分け
        switch (side) {
            case STAGE_BOTTOM:
                angle = 180f;
                break;
            case STAGE_TOP:
                angle = 0f;
                break;
            case STAGE_LEFT:
                angle = 90f;
                break;
            case STAGE_RIGHT:
                angle = 270f;
                break;
            default:
                angle = 180f;
                break;
        }

        return angle;
    }

    /*
     * キャラクターが配置された四辺に応じて、向きを設定するためのQuaternion値を取得
     */
    private Quaternion getCharacterInitFacingDirection(float angle) {

        // w／y値
        float w = CharacterNode.calcQuaternionWvalue(angle);
        float y = CharacterNode.calcQuaternionYvalue(angle);

        // 向きたい方向のQuaternion情報を生成
        return (new Quaternion(0.0f, y, 0.0f, w));
    }

    /*
     * ARフラグメント：平面タッチリスナーの設定
     */
    private void setTapPlaneListener(ArFragment arFragment) {

        arFragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
            @Override
            public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {

                //!ガード：レンダラブル未生成チェック
                //!ガード：タップして配置済みチェック
                // Node生成可能か判定
                if( !enableCreateNode() ){
                    Snackbar.make(binding.getRoot(), getString(R.string.snackbar_please_wait), Snackbar.LENGTH_SHORT).show();

                    //!失敗した場合のリカバリをどうするか
                }

                //----------------------------------
                // AnchorNodeの生成／Sceneへの追加
                //----------------------------------
                // ARScene
                Scene scene = arFragment.getArSceneView().getScene();

                //!２つ以上生成されないようにする
                // アンカーノードを生成して、Sceneに追加
                Anchor anchor = hitResult.createAnchor();
                AnchorNode anchorNode = new AnchorNode(anchor);
                anchorNode.setName(NODE_NAME_ANCHOR);
                anchorNode.setParent(scene);

                //----------------------------------
                // Node生成
                //----------------------------------
                // オブジェクト
                createNodeObject(anchorNode);
                // ゴール
                createNodeGoal(anchorNode);
                // キャラクターNode生成
                // ！他のNode生成の後に行うこと（重複をさけて配置しているため）！
                createNodeCharacter(anchorNode);
                // ステージ
                createNodeStage(anchorNode);
                // 目標説明view
                createNodeGoalGuideUI(anchorNode);

                //----------------------------------
                // 状態管理
                //----------------------------------
                // ステージ配置前⇒プログラミング中へ
                mPlayState = PLAY_STATE_PROGRAMMING;
            }
        });
    }


    /*
     * ARフラグメント：平面タッチリスナーの設定
     */
    private boolean enableCreateNode() {

        //!ガード：タップして配置済みチェック




        //------------------------------
        // レンダラブル生成済みチェック
        //------------------------------
        // レンダラブルのどれか一つでもnullなら、Node生成不可
        if( (mCharacterRenderable == null) || (mStageRenderable == null) || (mGoalRenderable == null) || (mGuideViewRenderable == null) ){
            return false;
        }

        // 生成済みのobjectRenderableの数が、生成予定数よりも少ない場合、Node生成不可
        int objectRenderableNum = mObjectRenderable.size();
        int gimmickobjectNum = mGimmick.objectGlbList.size();
        if( objectRenderableNum < gimmickobjectNum ){
            return false;
        }

        return true;
    }

    /*
     * お試し：平面ドットのビジュアル変更
     */
    private void setPlaneVisual(Context context) {

        ArSceneView arSceneView = arFragment.getArSceneView();

        Texture.Sampler sampler =
                Texture.Sampler.builder()
                        .setMinFilter(Texture.Sampler.MinFilter.LINEAR)
                        .setWrapMode(Texture.Sampler.WrapMode.REPEAT)
                        .build();

        // R.drawable.custom_texture is a .png file in src/main/res/drawable
        Texture.builder()
                .setSource(context, R.drawable.green_square)
                .setSampler(sampler)
                .build()
                .thenAccept(texture -> {
                    arSceneView.getPlaneRenderer()
                            .getMaterial().thenAccept(material -> {
                                material.setTexture(PlaneRenderer.MATERIAL_TEXTURE, texture);
                                Log.i("平面", "ドット変更");
                            });
                })
                .exceptionally(
                        throwable -> {
//                            Toast toast =
//                                    Toast.makeText( context, "Unable to load andy renderable", Toast.LENGTH_LONG);
//                            toast.setGravity(Gravity.CENTER, 0, 0);
//                            toast.show();
//                            Log.i("平面", "平面　失敗");
                            return null;
                        });
    }

    /*
     * ドロップ必要判定
     * 　以下の場合、ドロップ処理は行わない
     * 　・「ドロップ先ブロック」と「ドラッグ中のブロック」が同じ
     * 　・「ドロップ先ブロック」が「ドラッグ中のブロック」の一つ上に位置する
     * 　・（ネストブロックのみ）「ネストブロック」をネスト内にドロップしようとしている
     */
    private boolean isDropable(Block dropBlock, Block dragBlock) {

        //------------------------------------------
        // 「ドロップ先」と「ドラッグ中」が同じ
        //------------------------------------------
        if (dropBlock.getId() == dragBlock.getId()) {
            Log.i("isDropable", "getId()一致");
            return false;
        }

        //------------------------------------------
        //  ネスト内のブロックにドロップしようとしている
        //------------------------------------------
        if (dragBlock.hasBlock(dropBlock)) {
            Log.i("isDropable", "hasBlock()");
            return false;
        }

        //------------------------------------------
        // 「ドロップ先」が「ドラッグ中」の１つ上かどうか
        //------------------------------------------
        // ドラッグ中ブロックの１つ上のブロック
        Block aboveBlock = dragBlock.getAboveBlock();
        if (dropBlock.getId() == aboveBlock.getId()) {
            Log.i("isDropable", "aboveBlock");
            // ドロップ先がドラッグブロックの１つ上の場合は、不可
            return false;
        }

        // ドロップ可能
        return true;
    }

    /*
     * ドラッグされたビューのドラッグ状態を解除
     * （半透明な状態から、透明な状態にする）
     */
    public void dragBlockTranceOff(DragEvent dragEvent) {

        // 既に解除ずみなら何もしない
        Block draggedView = (Block) dragEvent.getLocalState();
        if (draggedView.getAlpha() >= Block.TRANCE_OFF_DRAG) {
            return;
        }

        // ドラッグされたビューのドラッグ状態を解除
        draggedView.tranceOffDrag();
    }

    /*
     * ゲームを初期状態に戻す
     * ・フィールドをクリア
     * ・プログラミング処理ブロックを全て削除
     */
    public void initGameState() {

        // フィールドをクリア
        clearField();
        // プログラミングを初期化（ブロック全削除）
        initProgramming();
    }

    /*
     * フィールドをクリアする（配置した３Dモデルを全て削除する）
     */
    public void clearField() {

        // ステージ配置前なら何もしない
        if( mPlayState < PLAY_STATE_PROGRAMMING ){
            return;
        }

        //------------------
        // Node全削除
        //------------------
        // Sceneに追加されたNodeを全て取得
        Scene scene = arFragment.getArSceneView().getScene();
        List<Node> nodes = scene.getChildren();

        // Scene内のAnchorNodeを削除
        for (Node node : nodes) {
            if (node.getName().equals(NODE_NAME_ANCHOR)) {
                scene.removeChild(node);
                return;//★いらないかも。アンカー複数作られない実装ならいらない
            }
        }

        // キャラクタークリア
        //!通る？★
        mCharacterNode = null;

        //----------------------------------
        // 状態管理
        //----------------------------------
        // プログラミング中⇒ステージ配置前
        mPlayState = PLAY_STATE_INIT;
    }

    /*
     * プログラミングを初期化する（処理ブロックを全て削除する）
     */
    public void initProgramming() {

        //------------------
        // 処理ブロック全削除
        //------------------
        // Startブロックより後の処理ブロックを全て削除
        ViewGroup ll_UIRoot = binding.getRoot().findViewById(R.id.ll_UIRoot);
        int lastIndex = ll_UIRoot.getChildCount() - 1;
        ll_UIRoot.removeViews(1, lastIndex);

        //---------------------------
        // マークをスタートブロックに設定
        //---------------------------
        mMarkedBlock = binding.getRoot().findViewById(R.id.pb_chartTop);
        mMarkedBlock.setMarker(true);
        // 下ブロックをなしに
        mMarkedBlock.setBelowBlock(null);
    }


    /*
     * 【処理ブロック内リスナー設定】マーカーエリアクリック処理
     */
    @Override
    public void onBottomMarkerAreaClick(Block markedBlock) {
        // マーカーブロックを更新
        changeMarkerBlock(markedBlock);
    }

    /*
     * 【処理ブロック内リスナー設定】処理ブロックドロップリスナー
     *   @para1：ドロップされる可能性のあるブロック
     *   @para2：ドラッグ中のブロック（タッチ移動されているブロック）
     *
     */
    @Override
    public boolean onDropBlock(Block dropBlock, DragEvent dragEvent) {

        // ドロップ予定ラインビュー
        int dropLineID = dropBlock.getDropLineViewID();
        View v_dropLine = dropBlock.findViewById(dropLineID);

        switch (dragEvent.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
            case DragEvent.ACTION_DRAG_LOCATION:
                return true;

            case DragEvent.ACTION_DRAG_ENTERED:
                //------------------
                // ドロップ可能判定
                //------------------
                if (!isDropable(dropBlock, (Block) dragEvent.getLocalState())) {
                    // ドロップ対象外なら何もしない
                    return true;
                }

                // ドロップラインを表示
                v_dropLine.setVisibility(View.VISIBLE);

                return true;

            case DragEvent.ACTION_DRAG_EXITED:
                // ブロック追加ラインを非表示
                v_dropLine.setVisibility(View.INVISIBLE);

                return true;

            case DragEvent.ACTION_DROP:
                // ブロック追加ラインを非表示
                v_dropLine.setVisibility(View.INVISIBLE);

                //------------------
                // ドロップ可能判定
                //------------------
                if (!isDropable(dropBlock, (Block) dragEvent.getLocalState())) {
                    // ドロップ対象外なら何もしない
                    return true;
                }

                //---------------
                // ブロック移動処理
                //---------------
                dragMoveUnderDrop(dropBlock, (ProcessBlock) dragEvent.getLocalState());

                return true;

            case DragEvent.ACTION_DRAG_ENDED:

                // ブロック追加ラインを非表示
                v_dropLine.setVisibility(View.INVISIBLE);
                // ドラッグされてきたブロックの半透明化を解除
                dragBlockTranceOff(dragEvent);

                return true;

            default:
                break;
        }

        return false;
    }

    /*
     * ブロック処理終了リスナー
     */
    @Override
    public void onProcessEnd() {
    }

    /*
     * menuアクションリスナー：遊び方
     */
    @Override
    public void onHowToClick() {

    }

    /*
     * menuアクションリスナー：設定
     */
    @Override
    public void onSettingClick() {

        //------------------
        // 設定変更可能か判定
        //------------------
        // ゲーム中は設定変更不可
        if( mPlayState == PLAY_STATE_PLAYING ){
            Snackbar.make(binding.getRoot(), getString(R.string.snackbar_please_finish_game), Snackbar.LENGTH_SHORT).show();
            return;
        }

        //------------------
        // 画面遷移
        //------------------
        Intent intent = new Intent(getActivity(), SettingActivity.class);
        mSettingRegistrationLancher.launch( intent );
    }

    /*
     * menuアクションリスナー：ゴール説明
     */
    @Override
    public void onGoalGuide() {

        // ステージ配置前なら何もしない
        if( mPlayState < PLAY_STATE_PROGRAMMING ){
            return;
        }

        // ゴール説明ダイアログの表示
        showGoalGuideDialog();
    }

    /*
     * menuアクションリスナー：フィールドクリアクリック
     */
    @Override
    public void onClearFieldClick() {
        clearField();
    }

    /*
     * menuアクションリスナー：「初めからプログラミング」クリック
     */
    @Override
    public void onInitProgrammingClick() {
        initProgramming();
    }

    /*
     * ゲーム制御Fabクリックリスナー
     */
    @Override
    public void onPlayControlClick( FloatingActionButton fab ) {

        if( mPlayState == PLAY_STATE_PROGRAMMING ){
            // ゲーム開始
            startGame( fab );
        } else {
            // ゲームリトライ確認
            confirmRetryGame( fab );
        }
    }
}
