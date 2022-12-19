package com.ar.ar_programming;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ar.ar_programming.process.Block;
import com.ar.ar_programming.process.IfElseProcessBlock;
import com.ar.ar_programming.process.IfProcessBlock;
import com.ar.ar_programming.process.LoopProcessBlock;
import com.ar.ar_programming.process.NestProcessBlock;
import com.ar.ar_programming.process.ProcessBlock;
import com.ar.ar_programming.process.SingleProcessBlock;
import com.ar.ar_programming.process.StartBlock;
import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.PlaneRenderer;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.Texture;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformationSystem;
import com.ar.ar_programming.databinding.FragmentFirstBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class FirstFragment extends Fragment implements Block.MarkerAreaListener, Block.DropBlockListener, ProcessBlock.ProcessListener {

    //---------------------------
    // 定数
    //---------------------------
    // Node名
    private final String NODE_NAME_ANCHOR = "anchorNode";
    public static final String NODE_NAME_GOAL = "goalNode";
    public static final String NODE_NAME_BLOCK = "blockNode";
    public static final String NODE_NAME_OBSTACLE = "obstacleNode";

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
    public static final float NODE_SIZE_TMP_RATIO = 0.1f;
    public static final float NODE_SIZE_S = 0.1f;
    private final float NODE_SIZE_M = 0.5f;
    private final float NODE_SIZE_L = 1.0f;
    private final float NODE_SIZE_XL = 5.0f;

    //---------------------------
    // フィールド変数
    //---------------------------
    private FragmentFirstBinding binding;
    private ArFragment arFragment;
    private ModelRenderable mCharacterRenderable;
    private ModelRenderable mGoalRenderable;
    private ModelRenderable mBlockRenderable;

    // tmp
    private ModelRenderable mRedSphereRenderable;
    private ModelRenderable mBlueSphereRenderable;
    private ModelRenderable mRedCubeRenderable;
    private ModelRenderable mBlueCubeRenderable;
    private ViewRenderable mTextViewRenderable;
    // tmp

    // tmp
    private ArrayList<ModelRenderable> mObjOnStageRenderable;
    private ArrayList<Vector3> mObjOnStagePosition;
    private ArrayList<String> mObjOnStageName;
    // tmp

    private CharacterNode mCharacterNode;
    private Block mMarkedBlock;        // ブロック下部追加マーカーの付与されている処理ブロック

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ArFragmentを保持
        arFragment = (ArFragment) getChildFragmentManager().findFragmentById(R.id.sceneform_fragment);

        //------------------------------------------
        // プログラミングUIの設定
        //------------------------------------------
        setProgrammingUI();

        //------------------------------------------
        // 3Dモデルレンダリング生成
        //------------------------------------------
        // キャラクター
        createModelRenderable(view.getContext());
        // ブロック
        createObjOnStageNode(view.getContext());
        // ステージ上の物体
        createtmpObjOnStageRenderable(view.getContext());

        //------------------------------------------
        // お試し：平面ドットのビジュアル変更
        //------------------------------------------
        setPlaneVisual(view.getContext());


        //お試し
        TextView kidou = binding.getRoot().findViewById(R.id.kidou);
        kidou.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                NestedScrollView nsv_UIRoot = binding.getRoot().findViewById(R.id.nsv_UIRoot);
//                nsv_UIRoot.set

                StartBlock pb_chartTop = binding.getRoot().findViewById(R.id.pb_chartTop);
                pb_chartTop.setTranslationY(pb_chartTop.getTop() + 100);

                Log.i("描画不具合", "pb_chartTop.getHeight()=" + pb_chartTop.getHeight());
                Log.i("描画不具合", "nsv_UIRoot.getHeight()=" + nsv_UIRoot.getHeight());

            }
        });
        //お試し


        //------------------------------------------
        // 平面タップリスナーの設定
        //------------------------------------------
        arFragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
            @Override
            public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {

                //----------------------------------
                // AnchorNodeの生成／Sceneへの追加
                //----------------------------------
                // ARScene
                Scene scene = arFragment.getArSceneView().getScene();

                // アンカーノードを生成して、Sceneに追加
                Anchor anchor = hitResult.createAnchor();
                AnchorNode anchorNode = new AnchorNode(anchor);
                anchorNode.setName(NODE_NAME_ANCHOR);
                anchorNode.setParent(scene);

                //----------------------------------
                // Node生成
                //----------------------------------
                // ステージ上ブロックNode生成
                createBlocksNode(anchorNode);
                // ステージ上ゴールNode生成
                createGoalNode(anchorNode);
                // ステージ上オブジェクトのNode生成
                createObjOnStageNode(anchorNode);
                // キャラクターNode生成
                // ！他のNode生成の後に行うこと（重複をさけて配置しているため）！
                createCharacterNode(anchorNode);


//                DisplayMetrics metrics = getResources().getDisplayMetrics();
//                ScaleController testscale = new ScaleController( modelNode1, new PinchGestureRecognizer( new GesturePointersUtility( metrics ) ));

//                Node node1 = scene.overlapTest( modelNode2 );
//                Node node2 = scene.overlapTest( modelNode1 );
//
//                Log.i("衝突検知", "modelNode2 と衝突したNode=" + node1.getName() );
//                Log.i("衝突検知", "modelNode1 と衝突したNode=" + node2.getName() );

//                Log.i("アニメーション", "getAnimationCount()=" + modelNode1.getRenderableInstance().getAnimationCount() );

                // モデルに付与されたアニメーションの実行
//                modelNode1.getRenderableInstance().animate(true).start();
//                Log.i("AR調査", "アニメーション数=" + modelNode1.getRenderableInstance().getAnimationCount());

                //---------------------------
                // ノードタッチ検出とビューの生成
                // ※実験
                //---------------------------
/*                modelNode1.setOnTapListener(new Node.OnTapListener() {
                    @Override
                    public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                        Log.i("onTap", "onTapを検出");
                        //modelNode1.setLocalScale( new Vector3( 2, 3, 1 ) );

                        Vector3 localPosition = modelNode1.getLocalPosition();
                        Vector3 worldPosition = modelNode1.getWorldPosition();
                        Vector3 scale = modelNode1.getLocalScale();
                        Vector3 scaleW = modelNode1.getWorldScale();

                        Log.i("メソッド調査", "ノード位置(local) x=" + localPosition.x + " y=" + localPosition.y + " z=" + localPosition.z);
                        Log.i("メソッド調査", "ノード位置(world) x=" + worldPosition.x + " y=" + worldPosition.y + " z=" + worldPosition.z);
                        Log.i("メソッド調査", "ノードスケール(local) x=" + scale.x + " y=" + scale.y + " z=" + scale.z);
                        Log.i("メソッド調査", "ノードスケール(world) x=" + scaleW.x + " y=" + scaleW.y + " z=" + scaleW.z);

                        if (mTextViewRenderable == null) {
                            return;
                        }

                        //ノードサイズ
                        Vector3 vector3 = modelNode1.getLocalScale();

                        //テキストノードを生成して、タッチされたノードの少し上に表示させる
//                        TransformableNode textNode = new TransformableNode( transformationSystem );
//                        textNode.setParent( modelNode1 );
//                        textNode.setLocalPosition( new Vector3( 0f, 0.3f + 0.0f, 0f ) );
//                        textNode.setRenderable(mTextViewRenderable);
//                        textNode.select();
                    }
                });*/
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /*
     * プログラミングUIの設定
     */
    private void setProgrammingUI() {

        // マーカ―はスタートブロックに付与
        //★メソッド化
        initStartBlock();

        // プログラミング開始設定
        setStartProgramming();
        // ステージクリア設定
        setClearStage();
        // チャートクリア設定
        setClearChart();
        // キャラクター位置リセット
        resetCharacterPosition();

        //------------------------------------------
        // 「開始ブロック」設定
        //------------------------------------------
//        setStartBlock();

        //------------------------------------------
        // 処理ブロック追加用サンプル
        //------------------------------------------
//        sampleCreateBlock();
//        sampleDragView();

        // 処理ブロック削除エリア設定
        setRemoveBlockArea();

        // 処理ブロックリストアダプタの設定
        setSelectProcessBlockList();

        // プログラミングチャートエリアのスクロール設定
//        setChartAreaScroll();
    }

    /*
     * スタートブロック初期化処理
     */
    private void initStartBlock() {

        Block startBlock = binding.getRoot().findViewById(R.id.pb_chartTop);
        startBlock.setLayout(R.layout.process_block_start_ver2);
        startBlock.setMarkAreaListerner(this);
        startBlock.setDropBlockListerner(this);

        mMarkedBlock = startBlock;

        Log.i("クラスメソッド", "StartBlock   ID=" + startBlock.getId());
    }

    /*
     * プログラミング開始設定
     */
    private void setStartProgramming() {

        ViewGroup root = binding.getRoot();

        // start検知
        TextView tv_start = root.findViewById(R.id.tv_start);
        tv_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //----------------------
                // 処理ブロック数チェック
                //----------------------
                StartBlock startBlock = root.findViewById(R.id.pb_chartTop);
                if (!startBlock.hasBelowBlock()) {
                    Snackbar.make(root, "処理ブロックがありません", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                //----------------------
                // 先頭の処理ブロック開始
                //----------------------
                // 処理開始
                ProcessBlock block = (ProcessBlock) startBlock.getBelowBlock();
                runProcessBlock(block);
            }
        });
    }

    /*
     * ステージクリア設定
     */
    private void setClearStage() {

        ViewGroup root = binding.getRoot();
        TextView tv_clear = root.findViewById(R.id.tv_nodeAllClear);
        tv_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mCharacterNode == null) {
                    return;
                }

                // Sceneに追加されたNodeを全て取得
                Scene scene = arFragment.getArSceneView().getScene();
                List<Node> nodes = scene.getChildren();

                // Scene内のAnchorNodeを削除
                for (Node node : nodes) {
                    if (node.getName().equals(NODE_NAME_ANCHOR)) {
                        scene.removeChild(node);
                        return;
                    }
                }

                // クリア
                mCharacterNode = null;
            }
        });
    }

    /*
     * チャートクリア設定
     */
    private void setClearChart() {

        ViewGroup root = binding.getRoot();
        TextView tv_chartClear = root.findViewById(R.id.tv_chartClear);
        tv_chartClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //------------------
                // 処理ブロック全削除
                //------------------
                // Startブロックより後の処理ブロックを全て削除
                ViewGroup ll_UIRoot = root.findViewById(R.id.ll_UIRoot);
                int lastIndex = ll_UIRoot.getChildCount() - 1;
                ll_UIRoot.removeViews(1, lastIndex);

                //---------------------------
                // マークをスタートブロックに設定
                //---------------------------
                mMarkedBlock = binding.getRoot().findViewById(R.id.pb_chartTop);
                mMarkedBlock.setMarker(true);

                // 下ブロックをなしに
                mMarkedBlock.setBelowBlock( null );
            }
        });
    }

    /*
     * キャラクター位置リセット
     */
    private void resetCharacterPosition() {
        ViewGroup root = binding.getRoot();
        TextView tv_resetCharacter = root.findViewById(R.id.tv_resetCharacter);
        tv_resetCharacter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCharacterNode.positionReset();
            }
        });
    }


    /*
     * 処理ブロック開始
     */
    private void runProcessBlock(ProcessBlock block) {

        Log.i("ループ処理", "" + (new Object() {
        }.getClass().getEnclosingMethod().getName()));

        // ブロック処理を開始
        block.startProcess(mCharacterNode);

        //---------------------------
        // 処理ブロック種別に応じた処理
        //---------------------------
/*        int processType = block.getProcessType();
        switch (processType) {

            // 単体処理ブロック
            case ProcessBlock.PROCESS_TYPE_SINGLE:
                startBlockAnimation((SingleProcessBlock) block);
                block.startProcess(mCharacterNode);
                break;

            // ネスト処理ブロック：if/loop
            case ProcessBlock.PROCESS_TYPE_IF:
            case ProcessBlock.PROCESS_TYPE_LOOP:
                // ネスト数1の処理ブロック移行処理
                oneNestProcessTransition((NestProcessBlock) block);
                break;

            // ネスト処理ブロック：if-else
            case ProcessBlock.PROCESS_TYPE_IF_ELSE:
                // ネスト数2の処理ブロック移行処理
                twoNestProcessTransition((IfElseProcessBlock) block);
                break;

            default:
                // 異常値は処理なし
                break;
        }*/
    }


    /*
     * 処理ブロックに対応したアニメーションの開始
     * （前進や向きの変更等、キャラクターに処理ブロック通りの動きをさせる）
     */
    private void startBlockAnimation(SingleProcessBlock singleBlock) {

        Log.i("ループ処理", "" + (new Object() {
        }.getClass().getEnclosingMethod().getName()));

        //------------------------------------------------------
        // 処理種別と処理量からアニメーション量とアニメーション時間を取得
        //------------------------------------------------------
        // 処理種別と処理量
        int contents = singleBlock.getProcessContents();
        int setVolume = singleBlock.getProcessVolume();

        // アニメーション量とアニメーション時間
        float volume = mCharacterNode.getAnimationVolume(contents, setVolume);
        long duration = mCharacterNode.getAnimationDuration(contents, setVolume);

        // 処理に対応するアニメーションプロパティ名を取得
        String propertyName = CharacterNode.getPropertyName(contents);

        Log.i("アニメーション", "アニメーション開始--------------------");
        Log.i("アニメーション", "volume=" + volume);
        Log.i("アニメーション", "duration=" + duration);

        //----------------------------------
        // アニメーションの生成／開始：処理ブロック用
        //----------------------------------
        // アニメーション生成
        ValueAnimator characterAnimator = ObjectAnimator.ofFloat(mCharacterNode, propertyName, volume);
        characterAnimator.setDuration(duration);

        // 今回の処理用のアニメーターを保持させる
        mCharacterNode.setAnimator(characterAnimator);

        // リスナ―設定：アニメーション終了のみ
        characterAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }

            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {

                // ゴールしているなら、チャート処理はここで終了
                boolean isGoal = mCharacterNode.isGoaled();
                if (isGoal) {
                    return;
                }

                Log.i("ループ処理", "処理アニメーション終了");

                //-------------------------------
                // アニメーション終了時の位置を保持
                //-------------------------------
                mCharacterNode.setEndProcessAnimation(contents, volume);

                //-------------------------------
                // 次の処理へ
                //-------------------------------
                startNextProcess(singleBlock);
            }
        });

        // アニメーション開始
        characterAnimator.start();

        //----------------------------------------
        // アニメーションの開始：モデルアニメーション用
        //----------------------------------------
        // モデルに用意されたアニメーションを開始
        mCharacterNode.startModelAnimation(contents, duration);
    }

    /*
     * 次の処理ブロック開始処理
     *  @para：終了した処理ブロック
     */
    private void startNextProcess(ProcessBlock finishBlock) {

        Log.i("ループ処理", "" + (new Object() {
        }.getClass().getEnclosingMethod().getName()));

        ProcessBlock targetBlock = finishBlock;

        //------------------------------------
        // 実行できる処理ブロックの実行処理までループ
        //------------------------------------
        while (true) {

            // 直上ネストブロックを取得
            NestProcessBlock parentNestBlock = targetBlock.getOwnNestBlock();
            if (parentNestBlock == null) {
                // なければ、メインラインの処理を次へ進める
                proceedNextMainProcess(finishBlock);
                break;
            }

            //-----------------------
            // ネスト内の処理ライン
            //-----------------------
            // ループ処理内にいれば、ループ継続確認
            int type = parentNestBlock.getProcessType();
            if (type == ProcessBlock.PROCESS_TYPE_LOOP) {

                // 次の処理が先頭の処理ブロックでなければ
                boolean isNextProcessTop = ((LoopProcessBlock) parentNestBlock).isNextProcessTop();
                if (!isNextProcessTop) {
                    // ループ継続確認せず、次の処理へ
                    ProcessBlock nextBlockInNest = parentNestBlock.getBlockInNest();
                    runProcessBlock(nextBlockInNest);
                    break;
                }

                // ループ継続判定
                boolean isContinue = ((LoopProcessBlock) parentNestBlock).isCondition(mCharacterNode);
                if (isContinue) {
                    // ループ継続なら、ループ内の次の処理を実行
                    ProcessBlock nextBlockInNest = parentNestBlock.getBlockInNest();
                    runProcessBlock(nextBlockInNest);
                    break;
                }
            }

            // ループ処理内にない、or、ループ終了条件を満たしている場合、親ブロックの次の処理へ
            targetBlock = parentNestBlock;
        }
    }


    /*
     * メインライン（Startブロックの直属ライン）の処理ブロックを次へ進める
     */
    private void proceedNextMainProcess(ProcessBlock finishBlock) {

        Log.i("ループ処理", "" + (new Object() {
        }.getClass().getEnclosingMethod().getName()));

        //-----------------------
        // 処理ブロック数チェック
        //-----------------------
        // 処理をすべて実施していれば終了
        if (!finishBlock.hasBelowBlock()) {
            return;
        }

        //-----------------------
        // 処理ブロックの実行
        //-----------------------
        // 処理ブロックを取得して開始処理へ
        ProcessBlock block = (ProcessBlock) finishBlock.getBelowBlock();
        runProcessBlock(block);
    }

    /*
     * ネスト数１のネストブロックの移行処理：if文、loop文
     */
    private void oneNestProcessTransition(NestProcessBlock nestBlock) {

        Log.i("ループ処理", "" + (new Object() {
        }.getClass().getEnclosingMethod().getName()));

        //-----------------------------
        // ネスト内処理ブロック数チェック
        //-----------------------------
        // ネスト内に処理ブロックがなければ
        if (!nestBlock.hasNestBlock()) {
            // 次の処理ブロックへ
            startNextProcess(nestBlock);
            return;
        }

        //-----------------------------
        // 条件判定
        //-----------------------------
        // 条件未成立の場合
        if (!nestBlock.isCondition(mCharacterNode)) {
            // 次の処理ブロックへ
            startNextProcess(nestBlock);
            return;
        }

        //-----------------------------
        // ネスト内処理ブロックの実行
        //-----------------------------
        // 入れ子内の処理ブロックを実行
        ProcessBlock blockInNest = nestBlock.getBlockInNest();
        runProcessBlock(blockInNest);
    }

    /*
     * if文移行処理
     */
/*
    private void ifProcessTransition(IfProcessBlock ifBlock ) {

        //-----------------------------
        // if文内処理ブロック数チェック
        //-----------------------------
        // if文内に処理ブロックがなければ
        int blockInLoopNum = ifBlock.getProcessInNestNum();
        if (blockInLoopNum == 0) {
            // 次の処理ブロックへ
            startNextProcess(ifBlock);
            return;
        }

        //--------------
        // 条件判定
        //--------------
        // 条件未成立の場合
        if( !ifBlock.isConditionTrue(mCharacterNode) ){
            // 次の処理ブロックへ
            startNextProcess(ifBlock);
            return;
        }

        //--------------
        // ネスト内処理
        //--------------
        // 入れ子内の処理ブロックを実行
        ProcessBlock blockInIf = ifBlock.getProcessInNest();
        runProcessBlock(blockInIf);
    }
*/

    /*
     * ネスト数２の処理ブロックの移行処理：if-else文
     */
    private void twoNestProcessTransition(IfElseProcessBlock nestBlock) {

        // 条件判定を行う
        nestBlock.isCondition(mCharacterNode);

        //-----------------------------
        // 対象ネスト内の処理ブロック数チェック
        //-----------------------------
        // 対象ネスト内に処理ブロックがなければ
        int blockInLoopNum = nestBlock.getBlockSizeInNest();
        if (blockInLoopNum == 0) {
            // 次の処理ブロックへ
            startNextProcess(nestBlock);
            return;
        }

        //--------------
        // ネスト内処理
        //--------------
        // 入れ子内の処理ブロックを実行
        ProcessBlock blockInIf = nestBlock.getBlockInNest();
        runProcessBlock(blockInIf);
    }


    /*
     * 開始ブロック設定
     */
    private void setStartBlock() {

        ViewGroup root = binding.getRoot();

        // ドロップリスナーの設定
//        TextView tv_startBlock = root.findViewById(R.id.tv_startBlock);
//        tv_startBlock.setOnDragListener(new View.OnDragListener() {
//            @Override
//            public boolean onDrag(View view, DragEvent dragEvent) {
//
//                switch (dragEvent.getAction()) {
//                    case DragEvent.ACTION_DRAG_STARTED:
//                    case DragEvent.ACTION_DRAG_ENTERED:
//                    case DragEvent.ACTION_DRAG_LOCATION:
//                    case DragEvent.ACTION_DRAG_EXITED:
//                    case DragEvent.ACTION_DRAG_ENDED:
//                        return true;
//
//                    case DragEvent.ACTION_DROP:
//                        return true;
//
//                    default:
//                        break;
//                }
//
//                return false;
//            }
//        });
    }

    /*
     * 処理チャートの中で、一番下の処理ブロックを取得
     */
    private ProcessBlock getMostBottomBlock() {

        ViewGroup root = binding.getRoot();
        ViewGroup ll_UIRoot = root.findViewById(R.id.ll_UIRoot);

        int lastIndex = ll_UIRoot.getChildCount() - 1;
        return (ProcessBlock) ll_UIRoot.getChildAt(lastIndex);
    }



    /*
     *
     */
/*
    private void sampleCreateBlock() {

        ViewGroup root = binding.getRoot();

        // ドラッグ用
        TextView tx_1 = root.findViewById(R.id.textView1);
        tx_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // チャート最下部のブロックを取得
                ProcessBlock bottomBlock = getMostBottomBlock();

                // 本処理ブロックをチャート最下部のブロックに追加
                SingleProcessBlock ProcessBlock = new SingleProcessBlock(view.getContext(), getParentFragmentManager());
                ProcessBlock.setProcessKind( ProcessBlock.PROC_KIND_FORWARD);
                bottomBlock.createProcessBlock(ProcessBlock);

*/
/*                // 新規処理ブロックの生成
                SingleProcessBlock ProcessBlock = new SingleProcessBlock(view.getContext(), getParentFragmentManager());
                ProcessBlock.setProcKind( ProcessBlock.PROC_KIND_FORWARD );
                // ドラッグ中の影を生成
                View.DragShadowBuilder myShadow = new View.DragShadowBuilder(view);

                // ドラッグ開始
                view.startDragAndDrop(null, myShadow, ProcessBlock, 0);*//*

            }
        });

        // ドラッグ用
        TextView tx_2 = root.findViewById(R.id.textView2);
        tx_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // チャート最下部のブロックを取得
                ProcessBlock bottomBlock = getMostBottomBlock();

                // 本処理ブロックをチャート最下部のブロックに追加
                NestProcessBlock ProcessBlock = new NestProcessBlock(view.getContext());
                ProcessBlock.setProcessKind(ProcessBlock.PROC_KIND_IF);
                bottomBlock.createProcessBlock(ProcessBlock);

*/
/*                // 新規処理ブロックの生成
                NestProcessBlock ProcessBlock = new NestProcessBlock(view.getContext());
                ProcessBlock.setProcKind( ProcessBlock.PROC_KIND_IF );
                // ドラッグ中の影を生成
                View.DragShadowBuilder myShadow = new View.DragShadowBuilder(view);

                // ドラッグ開始
                view.startDragAndDrop(null, myShadow, ProcessBlock, 0);*//*

            }
        });
    }
*/

    /*
     *
     */
/*
    private void sampleDragView() {

        ViewGroup root = binding.getRoot();

        // ドラッグ用
        TextView tx_1 = root.findViewById(R.id.textView1);
        tx_1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                // 新規処理ブロックの生成
                SingleProcessBlock ProcessBlock = new SingleProcessBlock(view.getContext(), getParentFragmentManager());
                ProcessBlock.setProcessKind(ProcessBlock.PROC_KIND_FORWARD);
                // ドラッグ中の影を生成
                View.DragShadowBuilder myShadow = new View.DragShadowBuilder(view);

                // ドラッグ開始
                view.startDragAndDrop(null, myShadow, ProcessBlock, 0);
                return false;
            }
        });

        // ドラッグ用
        TextView tx_2 = root.findViewById(R.id.textView2);
        tx_2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                // 新規処理ブロックの生成
                NestProcessBlock ProcessBlock = new IfProcessBlock(view.getContext());
                ProcessBlock.setProcessKind(ProcessBlock.PROC_KIND_IF);
                // ドラッグ中の影を生成
                View.DragShadowBuilder myShadow = new View.DragShadowBuilder(view);

                // ドラッグ開始
                view.startDragAndDrop(null, myShadow, ProcessBlock, 0);
                return false;
            }
        });
    }
*/

    /*
     * 処理ブロックリスト設定
     */
    private void setSelectProcessBlockList() {

        ViewGroup root = binding.getRoot();

        //---------------------
        // 処理ブロック選択リスト
        //---------------------
        // 処理ブロックイメージリストを取得し、アダプタを生成
        TypedArray images = getResources().obtainTypedArray(R.array.processBlockImageList);
        TypedArray titles = getResources().obtainTypedArray(R.array.processBlockTitleList);
        ProcessBlockListAdapter adapter = new ProcessBlockListAdapter(images, titles);

        // スクロール方向を用意
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        // アダプタ設定
        RecyclerView rv_selectBlock = root.findViewById(R.id.rv_selectBlock);
        rv_selectBlock.setAdapter(adapter);
        rv_selectBlock.setLayoutManager(linearLayoutManager);

        // 処理ブロッククリックリスナーの設定
        adapter.setOnProcessBlockClickListener(new ProcessBlockListAdapter.ProcessBlockClickListener() {
            @Override
            public void onBlockClick(int selectProcessType, int selectProcessContents) {
                // クリックされた処理の処理ブロックを生成
                createProcessBlock(selectProcessType, selectProcessContents);
            }
        });
    }

    /*
     * プログラミングチャートエリアのスクロール設定
     */
/*
    private void setChartAreaScroll() {

        float density = getResources().getDisplayMetrics().density;

        // レイアウトに設定されているチャートサイズ：px→dp
        float scaleX = getResources().getDimension(R.dimen.chart_area_width) / density;
        float scaleY = getResources().getDimension(R.dimen.chart_area_height) / density;

        // 最大マップ移動距離は、チャートエリアの半分
//        mMaxChartAreaScaleX = scaleX / 2f;
//        mMaxChartAreaScaleY = scaleY / 2f;
        mMaxChartAreaScaleX = scaleX;
        mMaxChartAreaScaleY = scaleY;

        // フリング用スクロール生成
        Context context = getContext();
        mFlingScroller = new Scroller(context, new DecelerateInterpolator());
        mScrollGestureDetector = new GestureDetector(context, new ScrollListener());

        Log.i("スクロール値", "mMaxChartAreaScaleX=" + (mMaxChartAreaScaleX) + " mMaxChartAreaScaleY=" + (mMaxChartAreaScaleY));

        ViewGroup root = binding.getRoot();
        FrameLayout ll_UIRoot = root.findViewById(R.id.ll_UIRoot);
        ll_UIRoot.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                Log.i("スクロールテスト", "setOnTouchListener()コール確認");

                //現在のスクロールを停止
                mFlingScroller.forceFinished(true);

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        //タッチ開始時のタッチ位置
                        mPreTouchPosX = motionEvent.getX();
                        mPreTouchPosY = motionEvent.getY();
                        break;

                    case MotionEvent.ACTION_MOVE:

                        float distanceX = motionEvent.getX() - mPreTouchPosX;
                        float distanceY = motionEvent.getY() - mPreTouchPosY;

                        //Log.i("MotionEvent", "preX=" + mPreTouchPosX + " preY=" + mPreTouchPosY);
                        //Log.i("MotionEvent", "getX=" + motionEvent.getX() + " getY=" + motionEvent.getY());

                        //移動先座標
                        float x = ll_UIRoot.getTranslationX() + distanceX;
                        float y = ll_UIRoot.getTranslationY() + distanceY;

                        Log.i("スクロール値", "予定 移動先X=" + (x) + " 移動先Y=" + (y));

                        //マップサイズを超えて移動しようとした場合、最大サイズに丸める（最大移動距離を超えて移動させない）
                        x = ( x >= 0 ?
                                Math.min( x, mMaxChartAreaScaleX ):
                                Math.max( x, -mMaxChartAreaScaleX )
                        );
                        y = ( y >= 0 ?
                                Math.min( y, mMaxChartAreaScaleY ):
                                Math.max( y, -mMaxChartAreaScaleY )
                        );

                        Log.i("スクロール値", "実際 移動先X=" + (x) + " 移動先Y=" + (y));

                        //前回からの移動量を反映
                        ll_UIRoot.setTranslationX(x);
                        ll_UIRoot.setTranslationY(y);

                        //Log.i("マップ移動対応", "実際 移動先X=" + (x) + " 移動先Y=" + (y));
                        //Log.i("マップ移動対応", "-----------------");
                        //Log.i("サイズチェック", "map_max_size(px)=" + (int)getResources().getDimension(R.dimen.map_max_size_x));
                        //Log.i("サイズチェック", "map_max_size(dp)=" + (getResources().getDimension(R.dimen.map_max_size_x) / getResources().getDisplayMetrics().density));

                        mPreTouchPosX = motionEvent.getX();
                        mPreTouchPosY = motionEvent.getY();

                        break;

                    case MotionEvent.ACTION_CANCEL:
                        // something to do
                        break;
                }

                //スクロール操作リスナーをコール
                mScrollGestureDetector.onTouchEvent(motionEvent);

                return true;
            }
        });


    }
*/


    /*
     * チャート最下部に処理ブロックを生成する
     */
    private void createProcessBlock(int processType, int processContents) {

        ProcessBlock newBlock;
        Context context = getContext();

        //-----------------
        // 処理ブロック生成
        //-----------------
        switch (processType) {
            // 単体処理
            case Block.PROCESS_TYPE_SINGLE:
                newBlock = new SingleProcessBlock(context, getParentFragmentManager(), processContents);
                break;

            // ネスト処理
            case Block.PROCESS_TYPE_IF:
                newBlock = new IfProcessBlock(context, processContents);
                break;

            case Block.PROCESS_TYPE_IF_ELSE:
                newBlock = new IfElseProcessBlock(context, processContents);
                break;

            case Block.PROCESS_TYPE_LOOP:
                newBlock = new LoopProcessBlock(context, processContents);
                break;

            default:
                // 種別指定がおかしければ、何もしない
                return;
        }

        //----------------------
        // リスナー設定
        //----------------------
        // 全ブロック共通
        newBlock.setMarkAreaListerner(this);
        newBlock.setDropBlockListerner(this);
        newBlock.setProcessListener(this);

        //----------------------
        // チャートに追加
        //----------------------
        // 「マークブロック」の下に追加
        preparationInsertBlock(mMarkedBlock, newBlock);

        // 生成ブロックがネストブロックなら
        if (processType != Block.PROCESS_TYPE_SINGLE) {
            newBlock.post(() -> {
                // スタートブロックを配置
                deployNestStartBlock(newBlock);
            });
        }

        // マーカーブロックを新ブロックに変更
        changeMarkerBlock(newBlock);
    }

    /*
     * 指定ブロックを「aboveBlock」の下に挿入する
     */
    private void preparationInsertBlock(Block aboveBlock, ProcessBlock newBlock) {

        // 挿入先の上ブロックのレイアウト確定待ち
        aboveBlock.post(() -> {
            // 挿入位置を計算し、チャートに挿入
            ViewGroup.MarginLayoutParams mlp = getNewBlockMlp(aboveBlock);
            insertBlockBelowMark(aboveBlock, newBlock, mlp);
        });
    }

    /*
     * 指定ブロックを「aboveBlock」の下に挿入する
     */
    private void insertBlockBelowMark(Block aboveBlock, ProcessBlock newBlock, ViewGroup.MarginLayoutParams mlp) {

        FrameLayout ll_UIRoot = binding.getRoot().findViewById(R.id.ll_UIRoot);

        //-------------------------------
        // ブロックをレイアウトに追加
        //-------------------------------
        ll_UIRoot.addView(newBlock, mlp);

        // アニメーションを付与
        newBlock.setAlpha(0f);
        newBlock.animate()
                .alpha(1f)
                .setDuration(300)
                .setListener(null);

        //-------------------------
        // 上下ブロックの保持情報更新
        //-------------------------
        rewriteAboveBelowBlockOnInsert(aboveBlock, newBlock);

        // 新ブロックレイアウト確定後
        newBlock.post(() -> {

            //----------------------------------
            // 新ブロックより下のブロックの位置を下げる
            //----------------------------------
            int newBlockHeight = newBlock.getHeight();
            if (newBlock.hasBelowBlock()) {
                Block belowBlock = newBlock.getBelowBlock();
                belowBlock.downChartPosition(newBlockHeight);
            }

            // 追加先がネストブロック内の場合
            if (aboveBlock.inNest()) {
                NestProcessBlock nestBlock = aboveBlock.getOwnNestBlock();
                nestBlock.downNestBelowBlock(newBlockHeight);

                // ネストブロックサイズを変更
                nestBlock.resizeNestHeight(newBlock, NestProcessBlock.NEST_EXPAND);
            }
        });

        //-------------------------------
        // ネスト情報の書き換え
        //-------------------------------
        NestProcessBlock nestBlock = aboveBlock.getOwnNestBlock();
        newBlock.setOwnNestBlock(nestBlock);
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
     * ネスト内スタートブロックの生成と配置
     */
    private void deployNestStartBlock(ProcessBlock newBlock) {

        // 生成ブロックが単体処理ブロックなら、何もしない
        if (newBlock.getProcessType() == Block.PROCESS_TYPE_SINGLE) {
            return;
        }

        FrameLayout ll_UIRoot = binding.getRoot().findViewById(R.id.ll_UIRoot);

        //------------------------------
        // スタートブロック生成（ネスト１つ目）
        //------------------------------
        // スタートブロック生成
        StartBlock startBlock = createNestStartBlock((NestProcessBlock) newBlock);
        // レイアウトに配置
        ViewGroup nestView = ((NestProcessBlock) newBlock).getNestView();
        ViewGroup.MarginLayoutParams startMlp = getNestStartBlockMlp((NestProcessBlock) newBlock, nestView);
        ll_UIRoot.addView(startBlock, startMlp);

        // ネストブロックにスタートブロックを保持させる
        ((NestProcessBlock) newBlock).setNestStartBlock(startBlock);

        //---------------------
        // ネストサイズ設定
        //---------------------
        // レイアウト確定後、ネスト高さをスタートブロックの高さにする
        startBlock.post(() -> {
            ViewGroup targetNest = ((NestProcessBlock) newBlock).getNestView();
            ViewGroup.LayoutParams lp = targetNest.getLayoutParams();
            lp.height = startBlock.getHeight();
            targetNest.setLayoutParams(lp);
        });


        // if-else 以外はここで終了
        if (newBlock.getProcessType() != Block.PROCESS_TYPE_IF_ELSE) {
            return;
        }

        //------------------------------
        // スタートブロック生成（ネスト２つ目）
        //------------------------------
        // スタートブロック生成
        StartBlock startBlockSecond = createNestStartBlock((NestProcessBlock) newBlock);
        // レイアウトに配置
        ViewGroup secondNestView = ((IfElseProcessBlock) newBlock).getSecondNestView();
        startMlp = getNestStartBlockMlp((NestProcessBlock) newBlock, secondNestView);
        ll_UIRoot.addView(startBlockSecond, startMlp);

        // ネストブロックにスタートブロックを保持させる
        ((IfElseProcessBlock) newBlock).setSecondNestStartBlock(startBlockSecond);

        //---------------------
        // ネストサイズ設定
        //---------------------
        // レイアウト確定後、ネスト高さをスタートブロックの高さにする
        startBlockSecond.post(() -> {
            ViewGroup targetSecondNest = ((IfElseProcessBlock) newBlock).getSecondNestView();
            ViewGroup.LayoutParams lp = targetSecondNest.getLayoutParams();
            lp.height = startBlockSecond.getHeight();
            targetSecondNest.setLayoutParams(lp);
        });
    }

    /*
     * ネストスタートブロックの生成
     */
    private StartBlock createNestStartBlock(NestProcessBlock parentNestBlock) {

        StartBlock startBlock = new StartBlock(getContext());
        startBlock.setId(View.generateViewId());
        startBlock.setLayout(R.layout.process_block_start_in_nest);
        startBlock.setMarkAreaListerner(this);
        startBlock.setDropBlockListerner(this);
        startBlock.setOwnNestBlock(parentNestBlock);

        return startBlock;
    }

    /*
     * 新ブロックのMarginLayoutParamsの算出・取得
     */
    private ViewGroup.MarginLayoutParams getNewBlockMlp(Block aboveBlock) {

        ViewGroup.MarginLayoutParams mlp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        int top = aboveBlock.getTop() + aboveBlock.getHeight();
        int left = mlp.leftMargin;

        if (aboveBlock.inNest()) {
            left = aboveBlock.getLeft();
            Log.i("位置更新", "ネスト内スタートブロック getLeft()=" + left);
        }

        mlp.setMargins(left, top, 0, 0);
        return mlp;
    }

    /*
     * ネスト内スタートブロックのMarginLayoutParamsの算出・取得
     */
    private ViewGroup.MarginLayoutParams getNestStartBlockMlp(NestProcessBlock nestBlock, ViewGroup nestView) {

        ViewGroup.MarginLayoutParams mlp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        // トップマージン：「」＋「」＋「」＋「」
        int nestBlockTop = nestBlock.getTop();
        int nestViewTop = nestView.getTop();

        int top = nestBlockTop + nestViewTop;

        // 左マージン：「」＋「」
        int nestBlockLeft = nestBlock.getLeft();
        int nestViewLeft = nestView.getLeft();
        int left = nestBlockLeft + nestViewLeft;

        Log.i("位置更新", "nestBlockTop=" + nestBlockTop);
        Log.i("位置更新", "nestViewTop=" + nestViewTop);
        Log.i("位置更新", "nestBlockLeft=" + nestBlockLeft);
        Log.i("位置更新", "nestViewLeft=" + nestViewLeft);

        mlp.setMargins(left, top, 0, 0);
        return mlp;
    }


    /*
     * 指定ブロックから下にあるブロックを、全て下げる
     */
    private void downBelowBlock(Block downStartBlock, int downSize) {

        //---------------------
        // 下ブロックがなくなるまで
        //---------------------
        while (downStartBlock != null) {

            //---------------
            // ブロックを移動
            //---------------
            // 移動させる位置（「上ブロックの上マージン」＋「上ブロックの高さ」＋「下げる量」）
            int top = downStartBlock.getTop() + downSize;
            // マージンを再設定し、位置を下げる
            ViewGroup.MarginLayoutParams belowMlp = (ViewGroup.MarginLayoutParams) downStartBlock.getLayoutParams();
            belowMlp.setMargins(belowMlp.leftMargin, top, belowMlp.rightMargin, belowMlp.bottomMargin);

            //------------------------
            // ネスト内ブロック移動
            //------------------------
            // ネストブロックの場合
            int type = downStartBlock.getProcessType();
            if ((type == Block.PROCESS_TYPE_IF) || (type == Block.PROCESS_TYPE_IF_ELSE) || (type == Block.PROCESS_TYPE_LOOP)) {

                // ネスト内のブロックを下げる（ネスト１つ目）
                Block nestStartBlock = ((NestProcessBlock) downStartBlock).getNestStartBlock();
                downBelowBlock(nestStartBlock, downSize);

                // ネスト内のブロックを下げる（ネスト２つ目）
                if (type == Block.PROCESS_TYPE_IF_ELSE) {
                    Block secondStartBlock = ((IfElseProcessBlock) downStartBlock).getSecondNestStartBlock();
                    downBelowBlock(secondStartBlock, downSize);
                }
            }

            // 下げる対象のブロックを次へ
            downStartBlock = downStartBlock.getBelowBlock();
        }

        FrameLayout ll_UIRoot = binding.getRoot().findViewById(R.id.ll_UIRoot);
        ll_UIRoot.invalidate();
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
            return ((NestProcessBlock) block).hasBlock(mMarkedBlock);
        }

        return false;
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
        // ネスト内ブロックの削除の場合
        if (removeBlock.inNest()) {
            // ネストブロック下のブロックを上に移動させる
            NestProcessBlock nestBlock = removeBlock.getOwnNestBlock();
            nestBlock.upNestBelowBlock(removeBlock.getHeight());

            // 削除ブロック分、ネストを縮める
            nestBlock.resizeNestHeight(removeBlock, NestProcessBlock.NEST_SHRINK);

//            // ネストブロック下のブロックを上に移動させる
//            if( nestBlock.hasBelowBlock() ){
//                Block belowBlock = nestBlock.getBelowBlock();
//                belowBlock.upChartPosition( removeBlock.getHeight() );
//            }
        }

        // チャートから削除
        // ！ネストの縮小処理の後に行うこと（if-elseの場合、どちらのネストにいたか不明になるため）！
        removeBlock.removeOnChart();
        // 上下ブロック保持情報の更新
        // ！ネストの縮小処理の後に行うこと（if-elseの場合、どちらのネストにいたか不明になるため）！
        rewriteAboveBelowBlockOnRemove(removeBlock);
    }


    /*
     * 指定ブロックの１つ上にあるブロックを取得
     */
    public Block getOneAboveBlock(ProcessBlock block) {
        // 「指定ブロックのchildIndex」の１つ上のブロックを返す
        ViewGroup parentView = (ViewGroup) block.getParent();
        int childIndex = block.getOwnChildIndex();

        return (Block) parentView.getChildAt(childIndex - 1);
    }

    /*
     *
     */
    private void setRemoveBlockArea() {

        // 削除エリア
        ViewGroup root = binding.getRoot();
        View v_removeArea = root.findViewById(R.id.v_removeArea);

        v_removeArea.setOnDragListener(new View.OnDragListener() {
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
     * ドラッグビューをレイアウトから削除
     */
    private void removeProcessBlock(DragEvent dragEvent) {

        // 削除対象（ドラッグされてきたビュー）のID
        View draggedView = (View) dragEvent.getLocalState();
        int draggedID = draggedView.getId();

        // 削除対象（ドラッグされてきたビュー）の親レイアウト
        ViewGroup parent = (ViewGroup) draggedView.getParent();

        //---------------------------
        // 検索してレイアウトから削除
        //---------------------------
        // 子ビューを全て検索
        int childNum = parent.getChildCount();
        for (int i = 0; i < childNum; i++) {
            // 検索対象の子ビューのID
            View target = parent.getChildAt(i);
            int id = target.getId();

            // IDの一致するビューがあれば
            if (id == draggedID) {
                // レイアウトから削除
                parent.removeView(target);
                return;
            }
        }
    }

    /*
     * 3Dモデルレンダリング「ModelRenderable」の生成
     */
    private void createModelRenderable(Context context) {

        // レンダリングは非同期で生成する
        CompletableFuture<ModelRenderable> modelCompletableFuture =
                ModelRenderable
                        .builder()
//                      .setSource(view.getContext(), Uri.parse("models/test_anim.glb"))
//                      .setSource(view.getContext(), Uri.parse("models/tree.glb"))
//                      .setSource(view.getContext(), Uri.parse("models/halloween.glb"))
//                        .setSource(context, Uri.parse("models/sample_bear_small2.glb"))
//                        .setSource(view.getContext(), Uri.parse("models/sample_bear_ver3_born_anim.glb"))
//                        .setSource(view.getContext(), Uri.parse("models/box_02.glb"))
//                        .setSource(context, Uri.parse("models/box_03.glb"))
                        .setSource(context, Uri.parse("models/sample_bear_ver3_born_anim_4num.glb"))
//                      .setSource(view.getContext(), Uri.parse("models/steampunk_vehicle.gltf"))
                        .setIsFilamentGltf(true)    // これは上のファイルを読み込む場合は必要なよう
                        .build();

        // 非同期処理結果として、指定したレンダリングを受け取る
        modelCompletableFuture
                .thenAccept(renderable -> mCharacterRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(context, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();

                            Log.i("AR調査", "ModelRenderable　失敗");
                            return null;
                        });

        //----------------------------------------------
        // 単純な図形のレンダリング「Material」の生成
        // ----------------------------------------------
        // 非同期にてMaterialを生成
        MaterialFactory.makeOpaqueWithColor(context, new Color(android.graphics.Color.RED))
                .thenAccept(material -> {
                    //半径／中心／素材
                    //mRedSphereRenderable = ShapeFactory.makeSphere(0.1f, new Vector3(0.0f, 0.15f, 0.0f), material);

                    //！ここで色を変えると、前に作成したRenderableにも影響がでる
                    //material.setFloat3( MATERIAL_COLOR , new Color(android.graphics.Color.BLUE) );
                    //mBlueSphereRenderable = ShapeFactory.makeSphere(0.05f, new Vector3(0.0f, 0.0f, 0.0f), material);

                    mRedCubeRenderable = ShapeFactory.makeCube(
                            new Vector3(0.2f, 0.2f, 0.2f),
                            new Vector3(0f, 0f, 0f),
                            material);
                });

        // 非同期にてMaterialを生成
        MaterialFactory.makeOpaqueWithColor(context, new Color(android.graphics.Color.BLUE))
                .thenAccept(material -> {
                    mBlueCubeRenderable = ShapeFactory.makeCube(
                            new Vector3(0.2f, 0.2f, 0.2f),
                            new Vector3(0f, 0f, 0f),
                            material);
                });

        //円のモデル生成完了時、生成されたMaterialからモデルを生成して保持する
//        materialCompletableFuture
//                .thenAccept(
//                        material -> {
//                            //半径／中心／素材
//                            mRedSphereRenderable = ShapeFactory.makeSphere(0.1f, new Vector3(0.0f, 0.15f, 0.0f), material);
//
//                            //！ここで色を変えると、前に作成したRenderableにも影響がでる
//                            //material.setFloat3( MATERIAL_COLOR , new Color(android.graphics.Color.BLUE) );
//                            //mBlueSphereRenderable = ShapeFactory.makeSphere(0.05f, new Vector3(0.0f, 0.0f, 0.0f), material);
//
//                            mRedCubeRenderable = ShapeFactory.makeCube(
//                                    new Vector3(1.2f, 0.3f, 0.4f),
//                                    new Vector3(0.1f, 0.1f, 0.1f),
//                                    material);
//                        });

        //----------------------------------------------
        // ビューのレンダリング生成
        // ----------------------------------------------
        ViewRenderable
                .builder()
                .setView(context, R.layout.sample_card)
                .build()
                .thenAccept(renderable -> mTextViewRenderable = renderable);

    }

    /*
     * 3Dモデルレンダリング「ステージ上オブジェクト」の生成
     */
    private void createObjOnStageNode(Context context) {

        //-------------------
        // ブロック
        //-------------------
        ModelRenderable
                .builder()
                .setSource(context, Uri.parse("models/block_01.glb"))
                .setIsFilamentGltf(true)    // glbファイルを読み込む必須
                .build()
                .thenAccept(renderable -> mBlockRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(context, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

        //-------------------
        // ゴール
        //-------------------
        ModelRenderable
                .builder()
                .setSource(context, Uri.parse("models/goal_01.glb"))
                .setIsFilamentGltf(true)    // glbファイルを読み込む必須
                .build()
                .thenAccept(renderable -> mGoalRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(context, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });
    }

    /*
     * 3Dモデルレンダリング「ステージ上の物体」の生成
     */
    private void createtmpObjOnStageRenderable(Context context) {

        mObjOnStageRenderable = new ArrayList<>();
        mObjOnStagePosition = new ArrayList<>();
        mObjOnStageName = new ArrayList<>();

        //----------------------------
        // glbファイルpath文字列
        //----------------------------
        ArrayList<String> glbPath = new ArrayList<>();
//        glbPath.add("models/goal_01.glb");
//        glbPath.add("models/block_01.glb");
        glbPath.add("models/cone_01.glb");

        //------------------------
        // 多分変更するロジック★★★
        //------------------------
//        mObjOnStageName.add(NODE_NAME_GOAL);
//        mObjOnStageName.add(NODE_NAME_BLOCK);
        mObjOnStageName.add(NODE_NAME_OBSTACLE);

        //----------------------------
        // ステージオブジェクト位置
        //----------------------------
        float stageScale = getStageScale();

//        mObjOnStagePosition.add(new Vector3(-0.0f, -0.0f, -0.0f));
//        mObjOnStagePosition.add(new Vector3(-stageScale, -0.0f, -0.0f));
        mObjOnStagePosition.add(new Vector3(-0.0f, -0.0f, -stageScale));

        //----------------------------
        // Renderableの生成
        //----------------------------
        for (String glb : glbPath) {
            ModelRenderable
                    .builder()
                    .setSource(context, Uri.parse(glb))
                    .setIsFilamentGltf(true)    // glbファイルを読み込む必須
                    .build()
                    .thenAccept(renderable -> mObjOnStageRenderable.add(renderable))
                    .exceptionally(
                            throwable -> {
                                Toast toast =
                                        Toast.makeText(context, "Unable to load andy renderable", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                return null;
                            });
        }

    }


    /*
     * キャラクターノード生成
     */
    private void createCharacterNode(AnchorNode anchorNode) {

        Scene scene = arFragment.getArSceneView().getScene();

        // キャラクターサイズ
        // 「* 0.1f」は暫定処理。3Dモデルの大きさに合わせる
        float scale = getNodeScale() * NODE_SIZE_TMP_RATIO;

        //------------------------------------
        // キャラクター生成と他Nodeとの重複なしの配置
        //------------------------------------
        CharacterNode characterNode;

        // 重複しない配置になるまで、繰り返し
        while (true) {
            // 生成
            characterNode = createTmpCharacterNode(anchorNode, scale);

            // 他のNodeと重複していなければ、生成終了
            ArrayList<Node> nodes = scene.overlapTestAll(characterNode);
            if (nodes.size() < 1) {
                break;
            }

            // 重複していれば、そのキャラクターは削除
            anchorNode.removeChild(characterNode);
        }

        // ステージ上のキャラクターとして保持
        mCharacterNode = characterNode;

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
    private CharacterNode createTmpCharacterNode(AnchorNode anchorNode, float scale) {

        //------------------------------------
        // キャラクターの生成位置／向く方向／サイズ
        //------------------------------------
        // 四辺の内の配置辺
        Random random = new Random();
        int side = random.nextInt(STAGE_4_SIDE);

        // キャラクター初期位置
        Vector3 position = getCharacterInitPosition(side);
        // キャラクターに向かせる角度
        float angle = getCharacterInitFacingAngle(side);
        // キャラクターに向かせる方向のQuaternion値
        Quaternion facingDirection = getCharacterInitFacingDirection(angle);

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
     * ステージ上のブロックNode生成
     */
    private void createBlocksNode(AnchorNode anchorNode) {

        TransformationSystem transformationSystem = arFragment.getTransformationSystem();

        // Nodeスケール
        final float scale = getNodeScale();
        Vector3 scaleVector = new Vector3(scale, scale, scale);

        // ステージの広さ
        float stageScale = getStageScale();

        //-----------------------------
        // ブロックNode生成
        //-----------------------------
        for (int i = 0; i < 10; i++) {
            // ランダム位置を生成
            Vector3 pos = getRandomPosition(stageScale);

            // Node生成
            CharacterNode node = new CharacterNode(transformationSystem);
            node.setName(NODE_NAME_BLOCK);
            node.getScaleController().setMinScale(scale);
            node.getScaleController().setMaxScale(scale * 2);
            node.setLocalScale(scaleVector);
            node.setParent(anchorNode);
            node.setLocalPosition(pos);
            node.setRenderable(mBlockRenderable);
            node.select();
        }
    }

    /*
     * ステージ上のゴールNode生成
     */
    private void createGoalNode(AnchorNode anchorNode) {

        TransformationSystem transformationSystem = arFragment.getTransformationSystem();

        // Nodeスケール
        final float scale = getNodeScale();
        Vector3 scaleVector = new Vector3(scale, scale, scale);

        // ステージの広さ
        float stageScale = getStageScale();

        // ランダム位置を生成
        Vector3 pos = getRandomPosition(stageScale);

        // Node生成
        CharacterNode node = new CharacterNode(transformationSystem);
        node.setName(NODE_NAME_GOAL);
        node.getScaleController().setMinScale(scale);
        node.getScaleController().setMaxScale(scale * 2);
        node.setLocalScale(scaleVector);
        node.setParent(anchorNode);
        node.setLocalPosition(pos);
        node.setRenderable(mGoalRenderable);
        node.select();
    }

    /*
     * ユーザー指定のNodeサイズの取得
     */
    private float getNodeScale() {

        EditText et_nodeScale = binding.getRoot().findViewById(R.id.et_nodeScale);
        String value = et_nodeScale.getText().toString();
        int select = Integer.parseInt(value);

        switch (select) {
            case 0:
                return NODE_SIZE_S;
            case 1:
                return NODE_SIZE_M;
            case 2:
                return NODE_SIZE_L;
            case 3:
            default:
                return NODE_SIZE_XL;
        }
    }

    /*
     * ステージ上のランダム位置の取得
     */
    private float getStageScaleRatio() {

        EditText et_nodeScale = binding.getRoot().findViewById(R.id.et_nodeScale);
        String value = et_nodeScale.getText().toString();
        int select = Integer.parseInt(value);

        switch (select) {
            case 0:
                return STAGE_RATIO_S;
            case 1:
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

        // ユーザー指定のNodeサイズ
        EditText et_stageScale = binding.getRoot().findViewById(R.id.et_stageScale);
        String value = et_stageScale.getText().toString();
        int select = Integer.parseInt(value);
        // ステージサイズの倍率
        float stageRatio = getStageScaleRatio();

        // ステージサイズを算出
        switch (select) {
            case 0:
                return (STAGE_SIZE_S * stageRatio);
            case 1:
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
     * ステージ上オブジェクトのNode生成
     */
    private void createObjOnStageNode(AnchorNode anchorNode) {

        TransformationSystem transformationSystem = arFragment.getTransformationSystem();

        // Nodeスケール
        final float scale = getNodeScale();
        Vector3 scaleVector = new Vector3(scale, scale, scale);

        //------------------------------
        // ステージ上オブジェクトのNode生成
        //-----------------------------
        int i = 0;
        for (ModelRenderable renderable : mObjOnStageRenderable) {

            CharacterNode node = new CharacterNode(transformationSystem);

            node.setName(mObjOnStageName.get(i));
            node.getScaleController().setMinScale(scale);
            node.getScaleController().setMaxScale(scale * 2);
            node.setLocalScale(scaleVector);
            node.setParent(anchorNode);
            node.setLocalPosition(mObjOnStagePosition.get(i));
            node.setRenderable(renderable);
            node.select();

            i++;
        }
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
            return false;
        }

        //------------------------------------------
        //  ネスト内のブロックにドロップしようとしている
        //------------------------------------------
        if (dragBlock.hasBlock(dropBlock)) {
            return false;
        }

/*        // ドラッグブロック内に、ドロップ先ブロックがあるか否かで判定
        if( dragBlock.findViewById( dropBlock.getId() ) != null ){
            // あればドロップ不可
            return false;
        }*/

        //------------------------------------------
        // 「ドロップ先」が「ドラッグ中」の１つ上かどうか
        //------------------------------------------
        // ドラッグ中ブロックの１つ上のブロック
        Block aboveBlock = dragBlock.getAboveBlock();
        if (dropBlock.getId() == aboveBlock.getId()) {
            // ドロップ先がドラッグブロックの１つ上の場合は、不可
            return false;
        }

/*        int childIndex = dragBlock.getOwnChildIndex();
        int aboveID = ((ViewGroup)dragBlock.getParent()).getChildAt( childIndex - 1 ).getId();
        if( dropBlock.getId() == aboveID ){
            // ドロップ先がドラッグブロックの１つ上の場合は、不可
            return false;
        }*/

//        Log.i("ドロップリスナー", "ACTION_DRAG_ENTERED dropBlock=" + dropBlock.getId());
//        Log.i("ドロップリスナー", "ACTION_DRAG_ENTERED aboveID=" + aboveID);

        // ドロップ可能
        return true;
    }

    /*
     * 処理ブロックのドラッグ移動
     *   「ドラッグされたブロック」を「ドロップ先ブロック」の下に移動させる
     */
    public void dragMoveUnderDrop(Block dropBlock, ProcessBlock dragBlock) {

        // ドラッグ中ブロックを元の位置から削除
        removeBlockFromLine(dragBlock);

        // ドラッグ中ブロックをドロップブロックの下に生成
        preparationInsertBlock(dropBlock, dragBlock);
    }

    /*
     * ドラッグされたビューのドラッグ状態を解除
     * （半透明な状態から、透明な状態にする）
     */
    public void dragBlockTranceOff(DragEvent dragEvent) {

        // 既に解除ずみなら何もしない
        View draggedView = (View) dragEvent.getLocalState();
        if (draggedView.getAlpha() >= ProcessBlock.TRANCE_NOT_DRAG) {
            return;
        }

        // ドラッグされたビューのドラッグ状態を解除
        draggedView.setAlpha(ProcessBlock.TRANCE_NOT_DRAG);
    }


    //★削除予定
/*    @Override
    public void onUpBlock(ProcessBlock moveBlock) {

        // 先頭にいるなら、何もしない
        if ( moveBlock.isTop() ) {
            return;
        }

        //-----------------------------
        // 上の処理ブロックと位置を入れ替え
        //-----------------------------
        // 現時点のindexの1つ前のindexが、追加先のindex
        int newIndex = moveBlock.getOwnChildIndex() - 1;

        ViewGroup parent = (ViewGroup) moveBlock.getParent();
        // ブロック削除
        parent.removeView( moveBlock );
        // ブロック挿入
        parent.addView( moveBlock, newIndex );

        Log.i( "ブロック移動", "UP newIndex=" + newIndex );
    }*/


    //★削除予定
    /*@Override
    public void onDownBlock(ProcessBlock moveBlock) {

        // 最後尾にいるなら、何もしない
        if ( moveBlock.isBottom() ) {
            return;
        }

        //-----------------------------
        // 上の処理ブロックと位置を入れ替え
        //-----------------------------
        // 現時点のindexの1つ後のindexが、追加先のindex
        int newIndex = moveBlock.getOwnChildIndex() + 1;

        ViewGroup parent = (ViewGroup) moveBlock.getParent();
        // ブロック削除
        parent.removeView( moveBlock );
        // ブロック挿入
        parent.addView( moveBlock, newIndex );

        Log.i( "ブロック移動", "Down newIndex=" + newIndex );
    }*/

    /*
     * 【処理ブロック内リスナー設定】マーカー処理ブロック下への移動アイコンクリックリスナークリック処理
     * 　指定されたブロックを、現在のマーカーブロックの下に移動させる。
     *   また、新しいマーカーブロックを移動したブロックにする。
     */
/*    @Override
    public void onMoveBelowMarker(ProcessBlock moveBlock) {

        //----------------
        // ブロック移動
        //----------------
        // ブロック削除
        ViewGroup parent = (ViewGroup) moveBlock.getParent();
        parent.removeView( moveBlock );

        // マーカーブロックの下に挿入
        insertBlockBelowMark( moveBlock );

        //-------------------
        // マーカーブロック変更
        //-------------------
        // 移動したブロックをマーカーブロックとする
        changeMarkerBlock( moveBlock );
    }*/

    /*
     * 【処理ブロック内リスナー設定】ブロック削除クリック処理
     *
     */
    //★削除予定
/*    @Override
    public void onRemoveBlock(ProcessBlock removeBlock) {
        // ブロック削除
        removeBlockFromLine( removeBlock );
    }*/

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
     * スワイプ操作リスナー
     *   ・スクロール
     *   ・フリング
     */
/*
    private class ScrollListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

//            ViewGroup root = binding.getRoot();
            FrameLayout ll_UIRoot = binding.getRoot().findViewById(R.id.ll_UIRoot);

            float nowx = ll_UIRoot.getTranslationX();
            float nowy = ll_UIRoot.getTranslationY();

            Log.i("スクロール", "nowx=" + nowx + " nowy=" + nowy);

            // スクローラー
            final float SCALE = 2.5f;
            final int MOVE_DURATION = 5000;

            // アニメーションを開始
            mFlingScroller.fling(
                    (int) nowx,                    //scroll の開始位置 (X)
                    (int) nowy,                    //scroll の開始位置 (Y)
                    (int) (velocityX / SCALE),     //初速
                    (int) (velocityY / SCALE),     //初速
                    -(int)mMaxChartAreaScaleX,
                    (int)mMaxChartAreaScaleX,
                    -(int)mMaxChartAreaScaleY,
                    (int)mMaxChartAreaScaleY
            );

            // フリング操作時、加速度をスクロールに反映
            ValueAnimator scrollAnimator = ValueAnimator.ofFloat(0, 1).setDuration(MOVE_DURATION);
            scrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {

                    if (!mFlingScroller.isFinished()) {
                        mFlingScroller.computeScrollOffset();

                        ll_UIRoot.setTranslationX(mFlingScroller.getCurrX());
                        ll_UIRoot.setTranslationY(mFlingScroller.getCurrY());

                    } else {
                        scrollAnimator.cancel();
                    }
                }
            });
            scrollAnimator.start();

            return false;
        }

    }
*/

}
