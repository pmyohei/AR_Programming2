package com.ar.ar_programming;

import static android.content.Context.MODE_PRIVATE;

import static com.ar.ar_programming.Common.PREFIX_TUTORIAL_NAME;
import static com.ar.ar_programming.Common.TUTORIAL_FINISH;
import static com.ar.ar_programming.Common.TUTORIAL_LAST;
import static com.ar.ar_programming.Gimmick.NO_USABLE_LIMIT_NUM;
import static com.ar.ar_programming.GimmickManager.GIMMICK_DELIMITER_TUTORIAL_NAME;
import static com.ar.ar_programming.character.CharacterNode.ACTION_FAILURE;
import static com.ar.ar_programming.character.CharacterNode.ACTION_SUCCESS;
import static com.ar.ar_programming.character.CharacterNode.ACTION_WAITING;
import static com.ar.ar_programming.GimmickManager.GIMMICK_MAIN_ANIMAL;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ar.ar_programming.character.AnimalNode;
import com.ar.ar_programming.character.CharacterNode;
import com.ar.ar_programming.character.VehicleNode;
import com.ar.ar_programming.databinding.FragmentArBinding;
import com.ar.ar_programming.process.Block;
import com.ar.ar_programming.process.IfElseIfElseBlock;
import com.ar.ar_programming.process.IfElseBlock;
import com.ar.ar_programming.process.IfBlock;
import com.ar.ar_programming.process.LoopBlock;
import com.ar.ar_programming.process.NestBlock;
import com.ar.ar_programming.process.ProcessBlock;
import com.ar.ar_programming.process.ExecuteBlock;
import com.ar.ar_programming.process.StartBlock;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.NodeParent;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.RenderableInstance;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformationSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class ARFragment extends Fragment implements ARActivity.MenuClickListener, ARActivity.PlayControlListener,
        Block.MarkerAreaListener, Block.DropBlockListener, ProcessBlock.ProgrammingListener,
        CharacterNode.CollisionDetectListener {

    //---------------------------
    // 定数
    //---------------------------
    // 画面遷移Key
    public static final String KEY_CURRENT_STAGE = "current_stage";

    // プログラミング終了ステータス
    public static final int PROGRAMMING_NOT_END = 0;
    public static final int PROGRAMMING_SUCCESS = 1;
    public static final int PROGRAMMING_FAILURE = -1;
    public static final int PROGRAMMING_REDO    = -2;

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
    public static final float NODE_SIZE_TMP_RATIO = 1f;
    public static final float NODE_SIZE_S = 0.02f;
    public static final float NODE_SIZE_M = 0.5f;
    public static final float NODE_SIZE_L = 1.0f;
    public static final float NODE_SIZE_XL = 5.0f;

    // Play状態
    private final int PLAY_STATE_INIT = 0;              // ステージ配置前
    private final int PLAY_STATE_PRE_PLAY = 1;          // プログラミング中（ゲーム開始前）
    private final int PLAY_STATE_PLAYING = 2;           // ゲーム中

    // チュートリアルガイド表示タイミング
    private final int TUTORIAL_GUIDE_SHOW_TAP = 0;                  // ARドットタップ時
    private final int TUTORIAL_GUIDE_SHOW_STAGE_DESCRIPTION = 1;    // ステージ説明参照終了時
    private final int TUTORIAL_GUIDE_SHOW_PROGRAMMING_AREA = 2;     // プログラミングエリア展開

    //---------------------------
    // フィールド変数
    //---------------------------
    private FragmentArBinding binding;
    private com.google.ar.sceneform.ux.ArFragment arFragment;
    private ModelRenderable mStageRenderable;
    private ModelRenderable mCharacterRenderable;
    private ModelRenderable mGoalRenderable;
    private ModelRenderable mSuccessRenderable;
    private ViewRenderable mGuideViewRenderable;
    private ViewRenderable mActionRenderable;
    private ArrayList<ModelRenderable> mObjectRenderable;
    private ArrayList<ModelRenderable> mObjectReplaceRenderable;

    private CharacterNode mCharacterNode;
    private Block mMarkedBlock;             // ブロック下部追加マーカーの付与されている処理ブロック

    private int mPlayState;                 // Play状態
    private Gimmick mGimmick;               // ステージギミックID

    private FloatingActionButton mPlayControlFab;
    private ActivityResultLauncher<Intent> mSettingRegistrationLauncher;
    private ActivityResultLauncher<Intent> mStageSelectLancher;

    private boolean mIsTutorialGuideShow = false;

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

        //-------------------
        // 状態設定
        //-------------------
        // ArFragmentを保持
        arFragment = (com.google.ar.sceneform.ux.ArFragment) getChildFragmentManager().findFragmentById(R.id.sceneform_fragment);
        // ゲーム状態初期化
        mPlayState = PLAY_STATE_INIT;

        //-------------------
        // リスト初期化
        //-------------------
        mObjectRenderable = new ArrayList<>();
        mObjectReplaceRenderable = new ArrayList<>();

        //-------------------
        // ギミック
        //-------------------
        // ステージギミックを選出
        String stageName = getNextChallengeStage();
        setGimmick(stageName);

        //-------------------------------
        // 画面遷移ランチャーを生成
        //-------------------------------
        setSettingRegistrationLauncher();
        setStageSelectLancher();

        //-------------------
        // UI関連
        //-------------------
        // 生成元Activityのmenuアクションリスナーを設定
        setMenuAction();
        // プログラミングUIの設定
        setProgrammingUI();
        // 3Dモデルレンダリング初期生成
        initRenderable();
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

        // プログラミングエリアボトムシート設定
        initProgrammingArea();
        // マーカ―はスタートブロックに付与
        initStartBlock();
        // ブロック削除エリア設定
        setRemoveBlockArea();
    }

    /*
     * ゴール説明ダイアログの表示
     */
    private void showGoalGuideDialog() {

        GoalGuideDialog dialog = new GoalGuideDialog(mGimmick.goalGuideIdList, mGimmick.mTutorial);

        // ダイアログ終了リスナー
        dialog.setOnDestroyListener(new GoalGuideDialog.OnDestroyListener() {
            @Override
            public void onDestroy() {
                showTutorialGuide(TUTORIAL_GUIDE_SHOW_STAGE_DESCRIPTION);
            }
        });

        dialog.show(getActivity().getSupportFragmentManager(), "goalGuide");
    }

    /*
     * プログラミングエリアボトムシート設定
     */
    private void initProgrammingArea() {

        // ガイド表示設定
        ConstraintLayout bottomSheet = binding.getRoot().findViewById(R.id.cl_programmingAreaBottomSheet);
        BottomSheetBehavior<ConstraintLayout> behavior = BottomSheetBehavior.from(bottomSheet);

        // 表示済みとする
        mIsTutorialGuideShow = true;

        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                //---------------
                // 処理不要判定
                //---------------
                // チュートリアル以外は不要
                if (mGimmick.mTutorial == TUTORIAL_FINISH) {
                    return;
                }
                // 表示可能状態でなければしない
                if (mIsTutorialGuideShow) {
                    return;
                }


                // ボトムシートが開いたら、ガイドを表示する
                if (newState == STATE_EXPANDED) {
                    showTutorialGuide(TUTORIAL_GUIDE_SHOW_PROGRAMMING_AREA);

                    // 一度表示させたら、以降は表示なし
                    mIsTutorialGuideShow = true;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // do nothing
            }
        });
    }

    /*
     * スタートブロック初期化処理
     */
    private void initStartBlock() {
        Block startBlock = binding.getRoot().findViewById(R.id.pb_chartTop);
        startBlock.setLayout(R.layout.block_start);
        startBlock.setMarkAreaListerner(this);
        startBlock.setDropBlockListerner(this);

        // マーカ―ブロック設定
        mMarkedBlock = startBlock;
    }

    /*
     * 画面遷移ランチャーの設定：設定画面遷移用
     */
    private void setSettingRegistrationLauncher() {

        // ! 初回リリース未使用
        mSettingRegistrationLauncher = registerForActivityResult(
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
                            initPreGameState();
                        }
                    }
                });
    }

    /*
     * ステージ選択画面遷移ランチャーを生成
     */
    private void setStageSelectLancher() {

        mStageSelectLancher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        // ResultCodeの取得
                        int resultCode = result.getResultCode();
                        if (resultCode != StageSelectActivity.RESULT_SELECT_STAGE) {
                            // 未選択なら何もしない
                            return;
                        }

                        // 選択ステージを取得
                        Intent intent = result.getData();
                        String selectedStage = intent.getStringExtra(StageSelectActivity.KEY_SELECT_STAGE);

                        Log.i("ステージ選択", "intent受け取り=" + selectedStage);

                        // ギミックをリセット
                        resetGimmick(selectedStage);
                    }
                });
    }

    /*
     * ギミック設定
     * 　・チュートリアル中　：次に挑戦するチュートリアル
     * 　・チュートリアル終了：未クリアのステージで一番先頭にあるステージ
     *
     *  　＜戻り値＞
     *     ・保持中のギミックが更新されるなら、true
     */
    private boolean setGimmick() {

        Context context = getContext();

        //-----------------
        // ステージ名
        //-----------------
        String stageName;

        boolean finishTutorial = Common.isCompleteTutorial(context);
        if (finishTutorial) {
            // チュートリアル完了
            // クリアしていないステージの内、一番先頭にあるステージ名を取得
            ArrayList<String> stageNameList = GimmickManager.getStageNameList(context, R.xml.gimmick_select);
            stageName = Common.getHeadNotSuccessStageName(context, stageNameList);

        } else {
            // チュートリアル中
            stageName = Common.getNextTutorialName(context);
        }

        //-----------------
        // ギミック生成
        //-----------------
        mGimmick = GimmickManager.getGimmick(context, stageName);
        return true;
    }

    /*
     * 次に挑戦するステージ名の取得
     * 　・チュートリアル中　：次に挑戦するチュートリアル
     * 　・チュートリアル終了：未クリアのステージで一番先頭にあるステージ
     */
    private String getNextChallengeStage() {

        Context context = getContext();

        //-----------------
        // ステージ名
        //-----------------
        String stageName;

        boolean finishTutorial = Common.isCompleteTutorial(context);
        if (finishTutorial) {
            // チュートリアル完了
            // クリアしていないステージの内、一番先頭にあるステージ名を取得
            ArrayList<String> stageNameList = GimmickManager.getStageNameList(context, R.xml.gimmick_select);
            stageName = Common.getHeadNotSuccessStageName(context, stageNameList);

        } else {
            // チュートリアル中
            stageName = Common.getNextTutorialName(context);
        }

        return stageName;
    }

    /*
     * ギミック設定
     *
     *  　＜戻り値＞
     *     ・保持中のギミックが更新されるなら、true
     */
    private boolean setGimmick(String stageName) {

        Context context = getContext();

        // 既にギミックを保持しているなら、何もしない
        if ((mGimmick != null) && (mGimmick.name.equals(stageName))) {
            return false;
        }

        //-----------------
        // ギミック生成
        //-----------------
        mGimmick = GimmickManager.getGimmick(context, stageName);
        return true;
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
        // プログラミング実行前処理
        //----------------------
        // ステージ上の置きかえ対象Node置き換え
        replaceNodeOnStage();

        //----------------------
        // プログラミング実行開始
        //----------------------
        ViewGroup root = binding.getRoot();
        StartBlock startBlock = root.findViewById(R.id.pb_chartTop);

        // スタートブロック下のブロック処理開始
        ProcessBlock block = (ProcessBlock) startBlock.getBelowBlock();
        runProcessBlock(block);

        // ゲーム状態更新
        mPlayState = PLAY_STATE_PLAYING;
        // Fabアイコンを切り替え
        fab.setImageResource(R.drawable.baseline_replay);
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
                        returnGameToStart( true );
                        // Fabアイコンを切り替え
                        fab.setImageResource(R.drawable.baseline_play);
                    }
                })
                .setNegativeButton(getString(R.string.ar_dialog_negative), null)
                .show();
    }

    /*
     * ブロック削除エリア設定
     */
    private void setRemoveBlockArea() {

        Context context = getContext();

        // 削除エリアアイコン
        ViewGroup root = binding.getRoot();
        ImageView iv_removeBlock = root.findViewById(R.id.iv_removeBlock);

        //----------------------
        // ゴミ箱アイコンリソース
        //----------------------
        // 通常アイコン
        final Drawable normalImageDrawable = iv_removeBlock.getDrawable();

        // ブロックドラッグ中アイコン
        String draggedImage = "baseline_block_remove_drag";
        Resources resources = getResources();
        String packageName = context.getPackageName();

        int drawableId = resources.getIdentifier(draggedImage, "drawable", packageName);
        final Drawable draggedImageDrawable = context.getDrawable(drawableId);

        //----------------------
        // ブロックドロップ処理
        //----------------------
        iv_removeBlock.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {

                switch (dragEvent.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                    case DragEvent.ACTION_DRAG_LOCATION:
                    case DragEvent.ACTION_DRAG_ENDED:
                        // 処理なし
                        return true;

                    case DragEvent.ACTION_DRAG_ENTERED:
                        // ドラッグ中アイコンに変更
                        iv_removeBlock.setImageDrawable(draggedImageDrawable);
                        return true;

                    case DragEvent.ACTION_DRAG_EXITED:
                        // アイコンを通常状態に変更
                        iv_removeBlock.setImageDrawable(normalImageDrawable);
                        return true;

                    case DragEvent.ACTION_DROP:
                        // ドラッグ中ブロックを処理ラインから削除
                        removeBlockFromLine((ProcessBlock) dragEvent.getLocalState());
                        // アイコンを通常状態に変更
                        iv_removeBlock.setImageDrawable(normalImageDrawable);
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
    private void returnGameToStart( boolean interruption) {

        if( interruption ){
            // キャラクターにプログラミング中断通知を送る
            mCharacterNode.notifyInterruptionProgramming();
        }

        // キャラクター位置リセット
        mCharacterNode.initStatus();
        // ゲーム状態をゲーム開始前にする
        mPlayState = PLAY_STATE_PRE_PLAY;
    }

    /*
     * ブロック開始
     */
    private void runProcessBlock(ProcessBlock block) {

        Log.i("ループ処理", "" + (new Object() {
        }.getClass().getEnclosingMethod().getName()));

        // ブロック処理を開始
        block.startProcess(mCharacterNode);
    }

    /*
     * 処理ブロック選択肢リスト設定
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
            public void onBlockClick(Gimmick.XmlBlockInfo xmlBlockInfo) {
                // クリックされたブロックを生成
                createProcessBlock(xmlBlockInfo);
            }
        });
    }

    /*
     * 処理ブロック選択肢リストの削除
     */
    private void clearSelectBlockList() {

        // 現状のギミックのブロック選択肢数
        int blockNum = mGimmick.xmlBlockInfoList.size();

        // 選択肢アダプタから全て削除
        ViewGroup root = binding.getRoot();
        RecyclerView rv_selectBlock = root.findViewById(R.id.rv_selectBlock);
        UserBlockSelectListAdapter adapter = (UserBlockSelectListAdapter) rv_selectBlock.getAdapter();

        adapter.clearBlockList();
        adapter.notifyItemRangeRemoved(0, blockNum);
    }

    /*
     * チャート最下部に処理ブロックを生成する
     */
    private void createProcessBlock(Gimmick.XmlBlockInfo xmlBlockInfo) {

        ProcessBlock newBlock;
        Context context = getContext();

        //-----------------
        // 処理ブロック生成
        //-----------------
        switch (xmlBlockInfo.type) {
            // 単体処理
            case GimmickManager.BLOCK_TYPE_EXE:
                newBlock = new ExecuteBlock(context, getParentFragmentManager(), xmlBlockInfo);
                break;

            // ネスト処理
            case GimmickManager.BLOCK_TYPE_IF:
                newBlock = new IfBlock(context, xmlBlockInfo);
                break;

            case GimmickManager.BLOCK_TYPE_IF_ELSE:
                newBlock = new IfElseBlock(context, xmlBlockInfo);
                break;

            case GimmickManager.BLOCK_TYPE_IE_ELSEIF:
                newBlock = new IfElseIfElseBlock(context, xmlBlockInfo);
                break;

            case GimmickManager.BLOCK_TYPE_LOOP:
                newBlock = new LoopBlock(context, xmlBlockInfo);
                break;

            default:
                // 種別指定がおかしければ、何もしない
//                Log.i("ブロックxml", "blockType=" + blockType);
                return;
        }

        //----------------------
        // リスナー設定
        //----------------------
        // 全ブロック共通
        newBlock.setMarkAreaListerner(this);
        newBlock.setDropBlockListerner(this);
        newBlock.setProgrammingListener(this);

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
        buildRenderableObject(mGimmick);
        buildRenderableReplaceObject(mGimmick);
        // ゴール
        buildRenderableGoal(mGimmick);
        // ゴール成功演出
        buildRenderableSuccess(mGimmick);
        // ゴール説明UI
        buildRenderableGuideView(mGimmick);
        // キャラクターアクション（吹き出し）
        buildRenderableView();
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
        String type = block.getType();
        if (!type.equals(GimmickManager.BLOCK_TYPE_EXE)) {
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
        // 選択肢ブロックリストのアダプタ
        RecyclerView rv_selectBlock = binding.getRoot().findViewById(R.id.rv_selectBlock);
        UserBlockSelectListAdapter adapter = (UserBlockSelectListAdapter) rv_selectBlock.getAdapter();

        // 削除ブロックの上下ブロック保持情報の更新
        rewriteAboveBelowBlockOnRemove(removeBlock);
        // 積み上げられたブロックから削除ブロックを削除
        removeBlock.removeOnChart(mGimmick, adapter);
        // 削除ブロックの上ブロックから更新を行う
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
     * Renderableロードエラーメッセージ表示
     */
    private void showRenderableLoadErrorMessage() {
        Snackbar.make(binding.getRoot(), getString(R.string.snackbar_please_return_home), Snackbar.LENGTH_SHORT).show();
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
                            showRenderableLoadErrorMessage();
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
                            showRenderableLoadErrorMessage();
                            return null;
                        });

    }

    /*
     * 3Dモデルレンダリング「ステージ上の物体」の生成
     */
    private void buildRenderableObject(Gimmick gimmick) {

        Context context = getContext();

        //----------------------
        // Renderableリスト初期化
        //----------------------
        mObjectRenderable.clear();
        // 必要なモデル数だけ、空のリストを用意
        int objectNum = gimmick.objectGlbList.size();
        for (int i = 0; i < objectNum; i++) {
            mObjectRenderable.add(null);
        }

        //-------------------
        // Renderable生成
        //-------------------
        for (int i = 0; i < objectNum; i++) {

            // gleファイル名（パス付き）
            String glbFilename = gimmick.objectGlbList.get(i);

            // 生成指定した順番でリストに格納されるようにする
            int setIndex = i;

            // Renderable生成
            ModelRenderable
                    .builder()
                    .setSource(context, Uri.parse(glbFilename))
                    .setIsFilamentGltf(true)    // glbファイルを読み込む必須
                    .build()
                    .thenAccept(
                            renderable -> {
                                mObjectRenderable.set(setIndex, renderable);
                            }
                    )
                    .exceptionally(
                            throwable -> {
                                showRenderableLoadErrorMessage();
                                return null;
                            });

        }
    }

    /*
     * 3Dモデルレンダリング「ステージ上の物体（置き換え後）」の生成
     */
    private void buildRenderableReplaceObject(Gimmick gimmick) {

        Context context = getContext();

        //----------------------
        // Renderableリスト初期化
        //----------------------
        mObjectReplaceRenderable.clear();
        // 必要なモデル数だけ、空のリストを用意
        int objectNum = gimmick.objectReplaceNameList.size();
        for (int i = 0; i < objectNum; i++) {
            mObjectReplaceRenderable.add(null);
        }

        //-------------------
        // Renderable生成
        //-------------------
        for (int i = 0; i < objectNum; i++) {

            // gleファイル名（パス付き）
            String glbFilename = gimmick.objectReplaceGlbList.get(i);

            // 生成指定した順番でリストに格納されるようにする
            int setIndex = i;

            // Renderable生成
            ModelRenderable
                    .builder()
                    .setSource(context, Uri.parse(glbFilename))
                    .setIsFilamentGltf(true)    // glbファイル読み込みに必須
                    .build()
                    .thenAccept(renderable -> {
                        mObjectReplaceRenderable.set(setIndex, renderable);
                    })
                    .exceptionally(
                            throwable -> {
                                showRenderableLoadErrorMessage();
                                return null;
                            });
        }
    }

    /*
     * 3Dモデルレンダリング「敵」の生成
     */
    private void buildRenderableEnemy(Gimmick gimmick) {


    }

    /*
     * 3Dモデルレンダリング「ゴール」の生成
     */
    private void buildRenderableGoal(Gimmick gimmick) {

        Context context = getContext();
        mGoalRenderable = null;

        //----------------
        // 生成チェック
        //----------------
        String goalGlb = gimmick.goalGlb;
        if (goalGlb == null) {
            // 生成なし
            return;
        }

        //-------------------
        // レンダリング生成
        //-------------------
        ModelRenderable
                .builder()
                .setSource(context, Uri.parse(gimmick.goalGlb))
                .setIsFilamentGltf(true)    // glbファイルを読み込む必須
                .build()
                .thenAccept(renderable -> mGoalRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            showRenderableLoadErrorMessage();
                            return null;
                        });
    }

    /*
     * 3Dモデルレンダリング「ステージクリア演出モデル」の生成
     */
    private void buildRenderableSuccess(Gimmick gimmick) {

        Context context = getContext();
        mSuccessRenderable = null;

        //-------------------
        // ステージクリア演出モデル
        //-------------------
        ModelRenderable
                .builder()
                .setSource(context, Uri.parse("models/balloon.glb"))
                .setIsFilamentGltf(true)    // glbファイルを読み込む必須
                .build()
                .thenAccept(renderable -> mSuccessRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            showRenderableLoadErrorMessage();
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
        // ゴール説明view
        //-------------------
        ViewRenderable
                .builder()
                .setView(context, R.layout.goal_guide_ui)
                .build()
                .thenAccept(renderable -> mGuideViewRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            showRenderableLoadErrorMessage();
                            return null;
                        });
    }

    /*
     * 3Dモデルレンダリング「キャラクターアクション（吹き出し）」の生成
     */
    private void buildRenderableView() {

        Context context = getContext();
        mActionRenderable = null;

        //-------------------------------
        // キャラクターアクション（吹き出し）
        //-------------------------------
        ViewRenderable
                .builder()
                .setView(context, R.layout.character_action)
                .build()
                .thenAccept(renderable -> mActionRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            showRenderableLoadErrorMessage();
                            return null;
                        });
    }

    /*
     * キャラクターノード生成
     */
    private void createNodeCharacter(AnchorNode anchorNode) {

        // キャラクターサイズ
        // 「* 0.1f」は暫定処理。3Dモデルの大きさに合わせる
        float scale = getNodeScale() * NODE_SIZE_TMP_RATIO;

        //------------------------------------
        // キャラクター生成と他Nodeとの重複なしの配置
        //------------------------------------
        CharacterNode characterNode = createTemporaryCharacterNode(anchorNode, scale);

        //-----------------------------
        // ステージ上キャラクターとして保持
        //-----------------------------
        // キャラクター衝突リスナーの設定
        characterNode.setOnCollisionDetectListener(this);
        mCharacterNode = characterNode;
    }

    /*
     * キャラクターノード生成：
     */
    private CharacterNode createTemporaryCharacterNode(AnchorNode anchorNode, float scale) {

        //------------------------------------
        // キャラクターの生成位置／向く方向／サイズ
        //------------------------------------
        // 四辺の内の配置辺
//        Random random = new Random();
//        int side = random.nextInt(STAGE_4_SIDE);

/*
        // キャラクター初期位置
        Vector3 position = getCharacterInitPosition(side);
        // キャラクターに向かせる角度
        float angle = getCharacterInitFacingAngle(side);
        // キャラクターに向かせる方向のQuaternion値
        Quaternion facingDirection = getCharacterInitFacingDirection(angle);
*/

        // キャラクターに向かせる角度
        float angle = mGimmick.characterAngle;
        Quaternion facingDirection = convertAngleToQuaternion(angle);
        // 位置
        Vector3 position = new Vector3(mGimmick.characterPositionVec.x * scale, mGimmick.characterPositionVec.y * scale, mGimmick.characterPositionVec.z * scale);

        //------------------------
        // Node生成
        //------------------------
        TransformationSystem transformationSystem = arFragment.getTransformationSystem();

        // AnchorNodeを親として、モデル情報からNodeを生成
        CharacterNode characterNode = createCharacterNode(transformationSystem);
        characterNode.getScaleController().setMinScale(scale);
        characterNode.getScaleController().setMaxScale(scale * 2);
        characterNode.setLocalScale(new Vector3(scale, scale, scale));
        characterNode.setParent(anchorNode);
        characterNode.setLocalPosition(position);
        characterNode.setLocalRotation(facingDirection);
        characterNode.setRenderable(mCharacterRenderable);

        // アニメーション初期化処理：必須
        characterNode.initAnimation();
        characterNode.startPosData(position, angle);

        // キャラクターアクション表記Nodeの作成
        characterNode.createActionRenderable(mActionRenderable, angle);
        characterNode.setActionWord(ACTION_WAITING);

        return characterNode;
    }

    /*
     * キャラクターノード生成：
     */
    private CharacterNode createCharacterNode(TransformationSystem transformationSystem) {

        // ギミックの指定キャラクターに応じて、生成
        if (mGimmick.character.equals(GIMMICK_MAIN_ANIMAL)) {
            return new AnimalNode(transformationSystem, mGimmick);
        } else {
            return new VehicleNode(transformationSystem, mGimmick);
        }
    }

    /*
     * ステージ上のオブジェクト生成
     */
    private void createNodeObject(AnchorNode anchorNode) {

        // Nodeスケール
        final float scale = getNodeScale();
        Vector3 scaleVector = new Vector3(scale, scale, scale);

        // ステージの広さ
        float stageScale = getStageScale();

        //----------------------------------
        // Node生成
        //----------------------------------
        int objIndex = 0;       // 全体数Index
        int objKindIndex = 0;   // 種別用Index

        //--------------------
        // モデルの種類数
        //--------------------
        for (ModelRenderable renderable : mObjectRenderable) {

            // オブジェクトの種類をNode名として設定する
            String objectName = mGimmick.objectNameList.get(objKindIndex);
            int objectNum = mGimmick.objectNumList.get(objKindIndex);

            //----------------------------
            // モデルの種類に対するギミック指定数
            //----------------------------
            for (int num = 0; num < objectNum; num++) {

                //-------------
                // 位置
                //-------------
                Vector3 pos;
                if (mGimmick.objectPositionRandom) {
                    // ランダム位置を生成
                    pos = getRandomPosition(stageScale);
                } else {
                    // 指定位置に設定
                    pos = mGimmick.objectPositionVecList.get(objIndex);
                    pos = new Vector3(pos.x * scale, pos.y * scale, pos.z * scale);
                }

                //-------------
                // Node生成
                //-------------
                Node node = new Node();
                node.setName(objectName);
                Log.i("ギミック", "setName()=" + objectName);
                node.setLocalScale(scaleVector);
                node.setParent(anchorNode);
                node.setLocalPosition(pos);
                node.setRenderable(renderable);

                // 角度指定あれば設定
                if (mGimmick.objectAngleList.size() > 0) {
                    // Quaternion算出
                    float angle = mGimmick.objectAngleList.get(objIndex);
                    Quaternion facingDirection = convertAngleToQuaternion(angle);
                    // Nodeに設定
                    node.setLocalRotation(facingDirection);
                }

                // 全体数indexを次へ
                objIndex++;
            }

            // 種別用Indexを加算
            objKindIndex++;
        }
    }

    /*
     * 目標説明UIの生成
     */
    private void createNodeGoalGuideUI(AnchorNode anchorNode) {

        // 配置位置
        final float GUIDE_POS_Y = 0.05f;    // 高さ
        final float GUIDE_POS_Z = -0.6f;  // 奥方向へ
        Vector3 pos = new Vector3(0f, GUIDE_POS_Y, GUIDE_POS_Z);

        // Node生成
        Node node = new Node();
        node.setName(GimmickManager.NODE_NAME_GOAL_GUIDE_UI);
        node.setParent(anchorNode);
        node.setLocalPosition(pos);
        node.setRenderable(mGuideViewRenderable);

        //------------------------
        // タッチ時のゴール目標表示
        //------------------------
        node.setOnTapListener(new Node.OnTapListener() {
            @Override
            public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                // ゴール説明ダイアログの表示
                showGoalGuideDialog();
                // アンカーから本UIを削除
                anchorNode.removeChild(node);
            }
        });
    }

    /*
     * ステージNode生成
     */
    private void createNodeStage(AnchorNode anchorNode) {

        // Nodeスケール
        final float scale = getNodeScale();
        Vector3 scaleVector = new Vector3(scale, scale, scale);

        /*// ステージの広さ
        float stageScale = getStageScale();
        // ランダム位置を生成
        Vector3 pos = getRandomPosition(stageScale);
        */

        // Node生成
        Node node = new Node();
        node.setName(GimmickManager.NODE_NAME_STAGE);
        node.setLocalScale(scaleVector);
        node.setParent(anchorNode);
        node.setLocalPosition(new Vector3(0f, 0f, 0f));
        node.setRenderable(mStageRenderable);
    }

    /*
     * ステージ上のゴールNode生成
     */
    private void createNodeGoal(AnchorNode anchorNode) {

        //----------------
        // 生成チェック
        //----------------
        if (mGoalRenderable == null) {
            // 生成なし
            return;
        }

        //----------------
        // Node生成
        //----------------
        // Nodeスケール
        final float scale = getNodeScale();
        Vector3 scaleVector = new Vector3(scale, scale, scale);

        /*// ステージの広さ
        float stageScale = getStageScale();
        // ランダム位置を生成
        Vector3 pos = getRandomPosition(stageScale);
        */

        // ゴールの角度
        float angle = mGimmick.goalAngle;
        Quaternion facingDirection = convertAngleToQuaternion(angle);
        // 位置
        Vector3 scalePos = new Vector3(mGimmick.goalPositionVec.x * scale, mGimmick.goalPositionVec.y * scale, mGimmick.goalPositionVec.z * scale);

        // Node生成
        Node node = new Node();
        node.setName(mGimmick.goalName);
        node.setLocalScale(scaleVector);
        node.setParent(anchorNode);
        node.setLocalPosition(scalePos);
        node.setLocalRotation(facingDirection);
        node.setRenderable(mGoalRenderable);
    }

    /*
     * ステージ上のNode置き換え
     */
    private void replaceNodeOnStage() {

        // 置き換えありのギミックでなければ、何もしない
        if (mGimmick.objectReplaceNameList.size() == 0) {
            return;
        }

        //-------------------------------------
        // 削除対象（置き換え対象）のNodeリストを生成
        //-------------------------------------
        List<Node> removeNodeList = new ArrayList<>();

        NodeParent parent = mCharacterNode.getParent();
        List<Node> nodes = parent.getChildren();
        for (Node node : nodes) {

            // 置き換え対象でなければ、次へ
            String nodeName = node.getName();
            if (!nodeName.equals(GimmickManager.NODE_NAME_REPLACE)) {
                continue;
            }

            // Node削除リストへ追加
            removeNodeList.add(node);
        }

        //-------------------------------------------------------
        // Node置き換え（「置き換え前Node」削除 → 「置き換え後Node」生成）
        //-------------------------------------------------------
        int replaceIndex = 0;
        for (Node removeNode : removeNodeList) {

            //--------------
            // 置き換え用情報
            //--------------
            // 置き換えNodeの情報
            final float scale = getNodeScale();
            Vector3 position = removeNode.getLocalPosition();
            Quaternion angle = removeNode.getLocalRotation();
            Vector3 scaleVector = removeNode.getLocalScale();

            // 置き換え後Nodeの情報
            String newNodeName = mGimmick.objectReplaceNameList.get(replaceIndex);
            Renderable newRenderable = mObjectReplaceRenderable.get(replaceIndex);

            //-----------
            // Node削除
            //-----------
            parent.removeChild(removeNode);

            //-----------
            // Node生成
            //-----------
            Node newNode = new Node();
            Log.i("Node検索", "replaceNodeOnStage() newNodeName=" + newNodeName);
            newNode.setName(newNodeName);
            newNode.setLocalScale(scaleVector);
            newNode.setParent(parent);
            newNode.setLocalPosition(position);
            newNode.setLocalRotation(angle);
            newNode.setRenderable(newRenderable);

            replaceIndex++;
        }
    }

    /*
     * ステージ上のステージクリア演出Node生成
     */
    private void createNodeSuccess(AnchorNode anchorNode) {

        // Nodeスケール
        final float scale = getNodeScale();
        Vector3 scaleVector = new Vector3(scale, scale, scale);

        Vector3 pos = new Vector3(0f, 0f, 0f);

        // Node生成
        Node node = new Node();
        node.setLocalScale(scaleVector);
        node.setParent(anchorNode);
        node.setLocalPosition(pos);
        node.setRenderable(mSuccessRenderable);

        //----------------------------
        // ３Dモデルのアニメーションを開始
        //----------------------------
        RenderableInstance renderableInstance = node.getRenderableInstance();
        // 全アニメーション分
        int last = renderableInstance.getAnimationCount() - 1;
        int[] arr = IntStream.rangeClosed(0, last).toArray();
        ObjectAnimator mModelAnimator = renderableInstance.animate(arr);
        // 開始
        mModelAnimator.setRepeatMode(ValueAnimator.RESTART);
        mModelAnimator.start();

        mModelAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }
            @Override
            public void onAnimationCancel(Animator animator) {
            }
            @Override
            public void onAnimationRepeat(Animator animator) {
            }
            @Override
            public void onAnimationEnd(Animator animator) {
            }
        });
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
     * 指定角度をQuaternion値に変換
     */
    private Quaternion convertAngleToQuaternion(float angle) {

        // w／y値
        float w = CharacterNode.calcQuaternionWvalue(angle);
        float y = CharacterNode.calcQuaternionYvalue(angle);

        // 向きたい方向のQuaternion情報を生成
        return (new Quaternion(0.0f, y, 0.0f, w));
    }

    /*
     * ARフラグメント：平面タッチリスナーの設定
     */
    private void setTapPlaneListener(com.google.ar.sceneform.ux.ArFragment arFragment) {

        arFragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
            @Override
            public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {

                //----------------------------------
                // 配置してよいか判定
                //----------------------------------
                // レンダラブル生成済みか
                if (!isPreparedRenderable()) {
                    Snackbar.make(binding.getRoot(), getString(R.string.snackbar_please_wait), Snackbar.LENGTH_SHORT).show();
                    return;
                }
                // 既にフィールド配置済みか
                if (isPlacedStage()) {
                    Snackbar.make(binding.getRoot(), getString(R.string.snackbar_field_placed), Snackbar.LENGTH_SHORT).show();
                    return;
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
                anchorNode.setName(GimmickManager.NODE_NAME_ANCHOR);
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
                // 処理ブロック
                //----------------------------------
                // 処理ブロック選択肢をギミックに応じたものに設定
                setSelectProcessBlockList();

                //----------------------------------
                // 状態管理
                //----------------------------------
                // ステージ配置前⇒プログラミング中へ
                mPlayState = PLAY_STATE_PRE_PLAY;

                //----------------------------------
                // ステージ出現時のガイド表示
                //----------------------------------
                showTutorialGuide(TUTORIAL_GUIDE_SHOW_TAP);

                // プログラミングエリア表示時のガイドを表示可能にする
                mIsTutorialGuideShow = false;
            }
        });
    }


    /*
     * ARフラグメント：レンダラブル生成済みかどうか
     */
    private boolean isPreparedRenderable() {

        //------------------------------
        // 配置必須レンダラブル
        //------------------------------
        // 配置必須レンダラブルのどれか一つでもnullなら、Node生成不可
        if ((mCharacterRenderable == null) ||
                (mStageRenderable == null) ||
                (mGuideViewRenderable == null) ||
                (mActionRenderable == null)) {
            return false;
        }

        //------------------------------
        // 配置任意レンダラブル
        //------------------------------
        // Goalレンダラブル判定
        if (mGimmick.goalGlb != null) {
            if (mGoalRenderable == null) {
                return false;
            }
        }

        //-------------------------------
        // Object
        //-------------------------------
        // 「生成済みRenderable数」
        // 「ギミックにて指定されているglb数」
        // の数の整合性をチェック
        int renderableNum = mObjectRenderable.size();
        int glbNum = mGimmick.objectGlbList.size();
        if (renderableNum < glbNum) {
            // ギミックで指定されている数よりも、生成したRenderableが少ない場合、エラー
            return false;
        }

        return true;
    }


    /*
     * ARフラグメント：ステージ配置済みかどうか
     */
    private boolean isPlacedStage() {

        //------------------------
        // 既にフィールドを生成しているか
        //------------------------
        // フィールド配置前のSceneの子Node数
        // ※AnchorNodeが加わると、Sceneの子Node数=2 となる
        final int BEFORE_FIERD_GENERATION_NODE_NUM = 1;

        Scene scene = arFragment.getArSceneView().getScene();
        if (scene.getChildren().size() > BEFORE_FIERD_GENERATION_NODE_NUM) {
            // 配置済み
            return true;
        }

        // 未配置
        return false;
    }

    /*
     * チュートリアルガイドの表示
     */
    private void showTutorialGuide(int showTimming) {

        // チュートリアル1が終了しているなら、処理なし
        int tutorial = mGimmick.mTutorial;
        if (tutorial >= TUTORIAL_FINISH) {
            return;
        }

        //-------------------
        // ヘルプページリスト
        //-------------------
        int pageListID = -1;
        switch (tutorial) {
            case 1:
                //---------------------------------------
                // チュートリアル１の場合は
                // ユーザー操作に応じて、ガイド内容を切り分け
                //---------------------------------------
                if (showTimming == TUTORIAL_GUIDE_SHOW_TAP) {
                    pageListID = R.array.tutorial_1_tap;

                } else if (showTimming == TUTORIAL_GUIDE_SHOW_STAGE_DESCRIPTION) {
                    pageListID = R.array.tutorial_1_stageDescription;

                } else {
                    pageListID = R.array.tutorial_1_openProgrammingArea;
                }
                break;

            case 2:
                if (showTimming == TUTORIAL_GUIDE_SHOW_PROGRAMMING_AREA) {
                    pageListID = R.array.tutorial_2_stageDescription;
                }
                break;

            case 3:
                if (showTimming == TUTORIAL_GUIDE_SHOW_PROGRAMMING_AREA) {
                    pageListID = R.array.tutorial_3_stageDescription;
                }
                break;

            case 4:
                if (showTimming == TUTORIAL_GUIDE_SHOW_PROGRAMMING_AREA) {
                    pageListID = R.array.tutorial_4_stageDescription;
                }
                break;

            case 5:
                if (showTimming == TUTORIAL_GUIDE_SHOW_PROGRAMMING_AREA) {
                    pageListID = R.array.tutorial_5_stageDescription;
                }
                break;

            case 6:
                if (showTimming == TUTORIAL_GUIDE_SHOW_PROGRAMMING_AREA) {
                    pageListID = R.array.tutorial_6_stageDescription;
                }
                break;

            default:
                pageListID = -1;
        }

        // 対象外なら終了
        if (pageListID == -1) {
            return;
        }

        //--------------------------
        // チュートリアルガイドの表示
        //--------------------------
        HelpDialog helpDialog = new HelpDialog();
        helpDialog.show(getActivity().getSupportFragmentManager(), "help");

        // onStart()終了リスナーの設定
        int finalPageListID = pageListID;
        helpDialog.setOnStartEndListerner(new HelpDialog.HelpDialogListener() {
            @Override
            public void onStartEnd() {
                // ページを設定
                helpDialog.setupHelpPage(finalPageListID);
            }
            @Override
            public void onDismiss() {
            }
        });
    }

    /*
     * ドロップ可否判定
     *  @para1：ドロップ先ブロック
     *  @para2：ドラッグ中ブロック
     *
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
     * ゲームをステージ配置前の状態に戻す
     * ・フィールドをクリア
     * ・プログラミング処理ブロックを全て削除
     */
    public void initPreGameState() {
        // フィールドをクリア
        removeField();
        // プログラミングを初期化（ブロック全削除）
        initProgramming();
    }

    /*
     * フィールドをクリアする（配置した３Dモデルを全て削除する）
     */
    public void removeField() {

        // ステージ配置前なら何もしない
        if (mPlayState < PLAY_STATE_PRE_PLAY) {
            Log.i("ステージ選択", "removeField()処理なし");
            return;
        }

        Log.i("ステージ選択", "removeField()");

        //---------------------
        // キャラクター状態リセット
        //---------------------
        // ステージから除外する前に、状態をリセットしておく
        returnGameToStart( false );

        //------------------
        // Node全削除
        //------------------
        // Scene内のAnchorNodeを削除することで、全Nodeを削除
        AnchorNode anchorNode = searchAnchorNode();
        if (anchorNode != null) {
            Scene scene = arFragment.getArSceneView().getScene();
            scene.removeChild(anchorNode);
        }

        //----------------------------------
        // 処理ブロック
        //----------------------------------
        // 選択肢ブロックリストを削除
        clearSelectBlockList();

        //----------------------------------
        // 状態管理
        //----------------------------------
        // プログラミング中⇒ステージ配置前
        mPlayState = PLAY_STATE_INIT;

        // FabをPlay可能状態に変更
        if (mPlayControlFab != null) {
            mPlayControlFab.setImageResource(R.drawable.baseline_play);
        }
    }

    /*
     * プログラミングエリアを初期化（積み上げたブロックを全て削除）
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

        //---------------------------
        // 使用可能数リセット
        //---------------------------
        // 選択肢ブロックアダプタ
        ViewGroup root = binding.getRoot();
        RecyclerView rv_selectBlock = root.findViewById(R.id.rv_selectBlock);
        UserBlockSelectListAdapter adapter = (UserBlockSelectListAdapter) rv_selectBlock.getAdapter();

        // 使用可能数の更新が必要な選択肢ブロックを更新
        int xmlNum = mGimmick.xmlBlockInfoList.size();
        for (int index = 0; index < xmlNum; index++) {

            Gimmick.XmlBlockInfo item = mGimmick.xmlBlockInfoList.get(index);

            //-----------
            // 対応不要判定
            //-----------
            // 制限なしなら何もしない
            if (item.usableLimitNum == NO_USABLE_LIMIT_NUM) {
                continue;
            }
            // 使用可能数が上限のままなら、何もしない
            if (item.usableNum == item.usableLimitNum) {
                continue;
            }

            //-----------
            // リセット
            //-----------
            // 使用可能数を使用可能上限数に戻す
            item.usableNum = item.usableLimitNum;
            // 更新通知
            adapter.notifyItemChanged(index);
        }
    }

    /*
     * ステージのクリア状態保存
     */
    private void saveStageSuccessState() {

        Context context = getContext();

        // クリアしたステージをクリア状態に保存
        Common.saveStageSuccess(context, mGimmick.name);
    }

    /*
     * ステージクリア成功
     */
    private void stageSuccess() {

        //------------------
        // Node演出
        //------------------
        // ステージクリア演出Nodeの生成
        createNodeSuccess((AnchorNode) mCharacterNode.getParentNode());
        // キャラクターアクション表記を成功にする
        mCharacterNode.setActionWord(ACTION_SUCCESS);

        //---------------------
        // ステージ成功情報を更新
        //---------------------
        boolean showMessage = showFinishTutorialMessage();
        saveStageSuccessState();

        //--------------------
        // ステージ成功ダイアログ
        //--------------------
        if( !showMessage ){
            // チュートリアル終了メッセージが表示されているなら、ここで成功ダイアログは表示させない
            showSuccessDialog();
        }
    }


    /*
     * チュートリアル終了時のメッセージを表示
     */
    private boolean showFinishTutorialMessage() {

        //-------------------------------
        // チュートリアル完了判定
        //   今回のクリアでチュートリアルが完了したかどうかを確認
        //-------------------------------
        // クリアしたステージが、最後のチュートリアルでないなら、対象外
        String lastTutorial = (PREFIX_TUTORIAL_NAME + GIMMICK_DELIMITER_TUTORIAL_NAME + Integer.toString(TUTORIAL_LAST));
        if( !mGimmick.name.equals( lastTutorial ) ){
            return false;
        }

        // 最後のチュートリアルの状態が「完了済み」なら、対象外
        boolean lastTutorialState = Common.getTutorialState( getContext(), mGimmick.name );
        if( lastTutorialState ){
            return false;
        }

        //-------------------------------
        // メッセージ表示
        //-------------------------------
        // チュートリアル終了のメッセージを表示する
        // !この処理は、「最後のチュートリアル」が完了した１回のみ行われる
        HelpDialog helpDialog = new HelpDialog();
        helpDialog.show(getActivity().getSupportFragmentManager(), "help");

        // onStart()終了リスナーの設定
        helpDialog.setOnStartEndListerner( new HelpDialog.HelpDialogListener() {
            @Override
            public void onStartEnd() {
                // ページを設定
                helpDialog.setupHelpPage( R.array.complete_tutorial );
            }
            @Override
            public void onDismiss() {
                // ステージ成功ダイアログの表示
                showSuccessDialog();
            }
        });

        return true;
    }

    /*
     * ステージクリア成功時のダイアログ
     */
    private void showSuccessDialog() {

        // ダイアログ表示
        StageSuccessDialog successDialog = new StageSuccessDialog();
        successDialog.setCancelable(false);
        successDialog.show(getActivity().getSupportFragmentManager(), "success");

        // 休憩リスナー設定
        successDialog.setOnBreakListerner(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                breakPlay();
            }
        });
        // 次のチュートリアル設定
        successDialog.setOnNextTutorialListerner(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 次のチュートリアルへ進む
                onNextTutorialClicked();
            }
        });
        // 別ステージ選択リスナー設定
        successDialog.setOnOtherStageListerner(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stageSelect();
            }
        });

    }

    /*
     * ステージクリア失敗
     */
    private void stageFailure() {

        //------------------
        // Node演出
        //------------------
        // キャラクターアクション表記を失敗にする
        mCharacterNode.setActionWord(ACTION_FAILURE);

        //--------------------
        // ダイアログ生成
        //--------------------
        showFailureDialog();
    }

    /*
     * ステージクリア失敗時のダイアログ
     */
    private void showFailureDialog() {

        StageFailureDialog failureDialog = new StageFailureDialog();
        failureDialog.setCancelable(false);
        failureDialog.show(getActivity().getSupportFragmentManager(), "failure");

        // リトライリスナー設定
        failureDialog.setOnRetryListerner(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRetryClicked();
            }
        });
        // 休憩リスナー設定
        failureDialog.setOnBreakListerner(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                breakPlay();
            }
        });
        // 別ステージ選択リスナー設定
        failureDialog.setOnOtherStageListerner(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stageSelect();
            }
        });
    }

    /*
     * ゴール結果ダイアログ：「再挑戦」
     */
    public void onRetryClicked() {
        // ゲームをリセット
        returnGameToStart( false );
        // Fabアイコンを切り替え
        mPlayControlFab.setImageResource(R.drawable.baseline_play);
    }

    /*
     * ステージ選択画面遷移
     */
    public void stageSelect() {
        // 画面遷移
        Intent intent = new Intent(getActivity(), StageSelectActivity.class);
        intent.putExtra(KEY_CURRENT_STAGE, mGimmick.name);

        mStageSelectLancher.launch(intent);
    }

    /*
     * ギミックリセット
     */
    public void resetGimmick( String stageName ) {

        // ステージ配置前の状態に戻す
        initPreGameState();

        // ステージギミックを選出
        boolean isUpdate = setGimmick(stageName);
        if( isUpdate ){
            // 3Dモデルレンダリング初期生成
            initRenderable();
        }
    }

    /*
     * ゴール結果ダイアログ：「次のチュートリアルへ」
     */
    public void onNextTutorialClicked() {

        String stageName = getNextChallengeStage();
        resetGimmick( stageName );
    }

    /*
     * ゴール結果ダイアログ：「休憩」
     */
    public void breakPlay() {
        // ARアクティビティ終了
        getActivity().finish();
    }

    /*
     * 指定Node検索
     */
    public static Node searchNodeOnStage(AnchorNode anchorNode, String nodeName) {

        //----------------------
        // 対象Node検索
        //----------------------
        // AnchorNode配下のNodeを検索
        List<Node> stageOnNodes = anchorNode.getChildren();
        for (Node stageOnNode : stageOnNodes) {

            // 指定Nodeがあれば、それを返して終了
            if (stageOnNode.getName().contains(nodeName)) {
                return stageOnNode;
            }
        }

        return null;
    }

    /*
     * キャラクターの向いている方向にいるNode検索
     */
    public static Node searchNodeCharacterFacingOnStage(AnchorNode anchorNode, String nodeName, CharacterNode characterNode) {

        List<Node> notSearchList = characterNode.getNotSearchNodeList();

        //----------------------
        // 対象Node検索
        //----------------------
        // AnchorNode配下のNodeを検索
        List<Node> stageOnNodes = anchorNode.getChildren();
        for (Node stageOnNode : stageOnNodes) {

            // 検索候補のNode名ではない
            if ( !stageOnNode.getName().contains(nodeName) ) {
                continue;
            }

            //---------------
            // 検索対象外判定
            //---------------
            // 名称は検索対象Nodeでも、検索対象外リストに該当すれば、対象外
            boolean isNotSearch = false;
            for( Node notSearch: notSearchList ){
                if( stageOnNode.equals( notSearch ) ) {
                    isNotSearch = true;
                    break;
                }
            }
            if( isNotSearch ){
                continue;
            }

            // 向いているか判定
            boolean isFacing = characterNode.isFacingToNode( stageOnNode );
            if( isFacing ){
                // キャラクターの方向にNodeが位置しているのであれば、それを返す
                return stageOnNode;
            }
        }

        // 該当なし
        return null;
    }

    /*
     * AnchorNode検索
     */
    private AnchorNode searchAnchorNode() {

        // Sceneに追加されたNodeを全て取得
        Scene scene = arFragment.getArSceneView().getScene();
        List<Node> nodes = scene.getChildren();

        // SceneからAnchorNodeを検索
        for (Node node : nodes) {
            if (node.getName().equals(GimmickManager.NODE_NAME_ANCHOR)) {
                return (AnchorNode) node;
            }
        }

        return null;
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
                if ( !isDropable(dropBlock, (Block) dragEvent.getLocalState()) ) {
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
     * キャラクター衝突リスナー
     */
    //!キャラクターで実装してもよい
    @Override
    public void onCollisionDetect(String collisionNode, ValueAnimator processAnimator) {

        //----------------
        // 衝突時の共通処理
        //----------------
        // 処理ブロック側のアニメーション終了
        processAnimator.cancel();
    }

    /*
     * プログラミング終了リスナー
     */
    @Override
    public void onProgrammingEnd(int programmingEndState) {

        if( programmingEndState == PROGRAMMING_SUCCESS ){
            // ステージクリア
            stageSuccess();

        } else {
            // ステージクリア失敗
            stageFailure();
        }
    }

    /*
     * menuアクションリスナー：遊び方
     */
    @Override
    public void onMenuHowToClick() {
        Intent intent = new Intent(getActivity(), HowToPlayActivity.class);
        startActivity(intent);
    }

    /*
     * menuアクションリスナー：設定
     */
    @Override
    public void onMenuSettingClick() {

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
        mSettingRegistrationLauncher.launch( intent );
    }

    /*
     * menuアクションリスナー：ステージ選択
     */
    @Override
    public void onMenuSelectStage() {

        // プログラム実行中なら、不可メッセージを表示
        if( mPlayState == PLAY_STATE_PLAYING ){
            Snackbar.make(binding.getRoot(), getString(R.string.snackbar_please_finish_game), Snackbar.LENGTH_SHORT).show();
            return;
        }

        // ステージ選択
        stageSelect();
    }

    /*
     * menuアクションリスナー：ゴール説明
     */
    @Override
    public void onMenuGoalGuide() {

        // ステージ配置前なら何もしない
        if( !isPlacedStage() ){
            Snackbar.make(binding.getRoot(), getString(R.string.snackbar_please_place_stage), Snackbar.LENGTH_SHORT).show();
            return;
        }

        // ゴール説明ダイアログの表示
        showGoalGuideDialog();
    }

    /*
     * menuアクションリスナー：フィールド削除クリック
     */
    @Override
    public void onMenuClearFieldClick() {

        // ステージ配置前なら何もしない
        if( !isPlacedStage() ){
            Snackbar.make(binding.getRoot(), getString(R.string.snackbar_please_place_stage), Snackbar.LENGTH_SHORT).show();
            return;
        }
        // プログラム実行中なら、不可メッセージを表示
        if( mPlayState == PLAY_STATE_PLAYING ){
            Snackbar.make(binding.getRoot(), getString(R.string.snackbar_please_finish_game), Snackbar.LENGTH_SHORT).show();
            return;
        }

        // ステージの全クリア
        initPreGameState();
    }

    /*
     * menuアクションリスナー：「初めからプログラミング」クリック
     */
    @Override
    public void onMenuInitProgrammingClick() {

        // ステージ配置前なら、不可メッセージを表示
        if( !isPlacedStage() ){
            Snackbar.make(binding.getRoot(), getString(R.string.snackbar_please_place_stage), Snackbar.LENGTH_SHORT).show();
            return;
        }
        // プログラム実行中なら、不可メッセージを表示
        if( mPlayState == PLAY_STATE_PLAYING ){
            Snackbar.make(binding.getRoot(), getString(R.string.snackbar_please_finish_game), Snackbar.LENGTH_SHORT).show();
            return;
        }

        //---------------
        // 全クリア
        //---------------
        // 積み上げたブロックを全クリア
        initProgramming();
    }

    /*
     * ゲーム制御Fabクリックリスナー
     */
    @Override
    public void onPlayControlClick( FloatingActionButton fab ) {

        //---------------------
        // ステージ未配置
        //---------------------
        if( mPlayState == PLAY_STATE_INIT ){
            Snackbar.make(binding.getRoot(), getString(R.string.snackbar_please_programming), Snackbar.LENGTH_SHORT).show();
            return;
        }

        //---------------------
        // プレイ制御
        //---------------------
        if( mPlayState == PLAY_STATE_PRE_PLAY){
            // ゲーム開始
            startGame( fab );
        } else {
            // ゲームリトライ確認
            confirmRetryGame( fab );
        }

        //---------------------
        // プレイ制御Fab
        //---------------------
        // ここでFabを保持する
        if( mPlayControlFab == null ){
            mPlayControlFab = fab;
        }
    }
}
