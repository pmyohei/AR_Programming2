package com.ar.ar_programming;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ar.ar_programming.process.NestProcessBlock;
import com.ar.ar_programming.process.ProcessBlock;
import com.ar.ar_programming.process.SingleProcessBlock;
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

public class FirstFragment extends Fragment {

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

    //---------------------------
    // フィールド変数
    //---------------------------
    private FragmentFirstBinding binding;
    private ArFragment arFragment;
    private ModelRenderable mCharacterRenderable;
    private ModelRenderable mRedSphereRenderable;
    private ModelRenderable mBlueSphereRenderable;
    private ModelRenderable mRedCubeRenderable;
    private ModelRenderable mBlueCubeRenderable;
    private ViewRenderable mTextViewRenderable;

    private ArrayList<ModelRenderable> mObjOnStageRenderable;
    private ArrayList<Vector3> mObjOnStagePosition;
    private ArrayList<String> mObjOnStageName;

    private CharacterNode mCharacterNode;
    private int mDoProcIndex;


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
        // ステージ上の物体
        createObjOnStageRenderable(view.getContext());

        //------------------------------------------
        // お試し：平面ドットのビジュアル変更
        //------------------------------------------
        setPlaneVisual(view.getContext());

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
                // キャラクターNode生成
                createCharacterNode(anchorNode);

                // ステージ上オブジェクトのNode生成
                createObjOnStageNode(anchorNode);


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

        // プログラミング開始設定
        setStartProgramming();
        // ステージクリア設定
        setClearStage();
        // チャートクリア設定
        setClearChart();

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
        sampleRemoveProcessBlock();

        // 処理ブロックリストアダプタの設定
        setSelectProcessBlockList();
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
                ViewGroup ll_UIRoot = root.findViewById(R.id.ll_UIRoot);
                if (ll_UIRoot.getChildCount() == 0) {
                    return;
                }

                //----------------------
                // 先頭の処理ブロック開始
                //----------------------
                // 処理ブロックの先頭を指定（子ビューの先頭はStartBlock）
                mDoProcIndex = 1;
                SingleProcessBlock block = (SingleProcessBlock) ll_UIRoot.getChildAt(mDoProcIndex);

                // 処理開始
                startNodeAnimation(block);
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
                // Startブロックより後の処理ブロックを全て削除
                ViewGroup ll_UIRoot = root.findViewById(R.id.ll_UIRoot);
                int lastIndex = ll_UIRoot.getChildCount() - 1;
                ll_UIRoot.removeViews( 1, lastIndex );
            }
        });
    }

    /*
     * 次の処理ブロック開始
     */
    private void nextProcBlock() {

        // 開始する処理ブロックIndexを次へ
        mDoProcIndex++;

        //-----------------------
        // 処理ブロック数チェック
        //-----------------------
        // 処理をすべて実施していれば終了
        ViewGroup ll_UIRoot = binding.getRoot().findViewById(R.id.ll_UIRoot);
        int blockNum = ll_UIRoot.getChildCount();
        if (blockNum <= mDoProcIndex) {
            return;
        }

        //-----------------------
        // 処理ブロックの実行
        //-----------------------
        // 処理ブロックを取得して開始
        SingleProcessBlock block = (SingleProcessBlock) ll_UIRoot.getChildAt(mDoProcIndex);
        startNodeAnimation(block);
    }

    /*
     * Nodeアニメーションの開始
     *   キャラクターに処理ブロック通りの動きをさせる
     */
    private void startNodeAnimation(SingleProcessBlock block) {

        //------------------------------------------------------
        // 処理種別と処理量からアニメーション量とアニメーション時間を取得
        //------------------------------------------------------
        // 処理種別と処理量
        int procKind = block.getProcessKind();
        int procVolume = block.getProcessVolume();

        // アニメーション量とアニメーション時間
        float volume = CharacterNode.getAnimationVolume(procKind, procVolume);
        long duration = CharacterNode.getAnimationDuration(procKind, procVolume);

        // 処理に対応するアニメーションプロパティ名を取得
        String propertyName = CharacterNode.getPropertyName(procKind);

        Log.i("アニメーション", "アニメーション開始--------------------");
        Log.i("アニメーション", "volume=" + volume);
        Log.i("アニメーション", "duration=" + duration);

        //----------------------------------
        // アニメーションの生成／開始
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

                Log.i("アニメーション", "onAnimationEnd");

                //-------------------------------
                // アニメーション終了時の位置を保持
                //-------------------------------
                mCharacterNode.setAnimationEndValue(procKind, volume);

                //-------------------------------
                // 次の処理へ
                //-------------------------------
                nextProcBlock();
            }
        });

        // アニメーション開始
        characterAnimator.start();
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
                NestProcessBlock ProcessBlock = new NestProcessBlock(view.getContext());
                ProcessBlock.setProcessKind(ProcessBlock.PROC_KIND_IF);
                // ドラッグ中の影を生成
                View.DragShadowBuilder myShadow = new View.DragShadowBuilder(view);

                // ドラッグ開始
                view.startDragAndDrop(null, myShadow, ProcessBlock, 0);
                return false;
            }
        });
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
            public void onColorClick(int selectProcess) {
                // クリックされた処理の処理ブロックを生成
                createProcessBlock(selectProcess);
            }
        });
    }

    /*
     * チャート最下部に処理ブロックを生成する
     */
    private void createProcessBlock(int processKind) {

        ProcessBlock newBlock;

        // 処理種別に応じた処理ブロックの生成
        switch (processKind) {
            // 単体処理
            case ProcessBlock.PROC_KIND_FORWARD:
            case ProcessBlock.PROC_KIND_BACK:
            case ProcessBlock.PROC_KIND_LEFT_ROTATE:
            case ProcessBlock.PROC_KIND_RIGHT_ROTATE:
                newBlock = new SingleProcessBlock(getContext(), getParentFragmentManager());
                break;

            // ネスト処理
            case ProcessBlock.PROC_KIND_IF:
            case ProcessBlock.PROC_KIND_IF_ELSE:
            case ProcessBlock.PROC_KIND_LOOP:
            default:
                newBlock = new NestProcessBlock(getContext());
                break;
        }
        // 処理種別を設定
        newBlock.setProcessKind(processKind);

        // チャート最下部のブロックに追加
        ProcessBlock bottomBlock = getMostBottomBlock();
        bottomBlock.createProcessBlock(newBlock);
    }

    /*
     *
     */
    private void sampleRemoveProcessBlock() {

        ViewGroup root = binding.getRoot();

        // 削除エリア
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
                        Log.i("ドラッグテスト", "削除エリアへドロップ");

                        // ドロップされたビューをレイアウトから削除
                        removeProcessBlock(dragEvent);
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
                        .setSource(context, Uri.parse("models/box_03.glb"))
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
     * 3Dモデルレンダリング「ステージ上の物体」の生成
     */
    private void createObjOnStageRenderable(Context context) {

        mObjOnStageRenderable = new ArrayList<>();
        mObjOnStagePosition = new ArrayList<>();
        mObjOnStageName = new ArrayList<>();

        //----------------------------
        // glbファイルpath文字列
        //----------------------------
        ArrayList<String> glbPath = new ArrayList<>();
        glbPath.add("models/goal_01.glb");
        glbPath.add("models/block_01.glb");
        glbPath.add("models/cone_01.glb");

        //------------------------
        // 多分変更するロジック
        mObjOnStageName.add(NODE_NAME_GOAL);
        mObjOnStageName.add(NODE_NAME_BLOCK);
        mObjOnStageName.add(NODE_NAME_OBSTACLE);

        //----------------------------
        // ステージオブジェクト位置
        //----------------------------
        mObjOnStagePosition.add(new Vector3(-0.0f, -0.0f, -0.0f));
        mObjOnStagePosition.add(new Vector3(-0.3f, -0.0f, -0.0f));
        mObjOnStagePosition.add(new Vector3(-0.0f, -0.0f, -0.3f));

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

        // AnchorNodeを親として、モデル情報からNodeを生成
        TransformationSystem transformationSystem = arFragment.getTransformationSystem();

//                CharacterNode modelNode1 = new CharacterNode( transformationSystem );
/*                TransformableNode modelNode1 = new TransformableNode(transformationSystem);
                modelNode1.setName("Node1");
                modelNode1.setParent(anchorNode);
                modelNode1.setRenderable(mglbRenderable);
                modelNode1.select();*/

        //------------------------------
        // キャラクターの生成位置／向く方向
        //------------------------------
        // 四辺の内の配置辺
        Random random = new Random();
        int side = random.nextInt(STAGE_4_SIDE);

        // キャラクター初期位置
        Vector3 position = getCharacterInitPosition(side);
        // キャラクターに向かせる角度
        float angle = getCharacterInitFacingAngle(side);
        // キャラクターに向かせる方向のQuaternion値
        Quaternion facingDirection = getCharacterInitFacingDirection(side, angle);

        //------------------------
        // キャラクターの生成位置の辺
        //------------------------
        mCharacterNode = new CharacterNode(transformationSystem);
        mCharacterNode.setName("Node2");
        mCharacterNode.getScaleController().setMinScale(0.2f);
        mCharacterNode.setLocalScale(new Vector3(0.2f, 0.2f, 0.2f));
        mCharacterNode.setParent(anchorNode);
        mCharacterNode.setLocalPosition(position);
        mCharacterNode.setLocalRotation(facingDirection);
        mCharacterNode.setRenderable(mCharacterRenderable);
        mCharacterNode.select();

        // アニメーション初期化処理：必須
        mCharacterNode.initAnimation();
        mCharacterNode.initCurrentData( position, angle );

/*
        Log.i("ピンチ操作", "null? → getScaleController=" + mCharacterNode.getScaleController());
        Log.i("ピンチ操作", "null? → getTranslationController=" + mCharacterNode.getTranslationController());
        Log.i("ピンチ操作", "null? → getRotationController=" + mCharacterNode.getRotationController());

        ScaleController cont = mCharacterNode.getScaleController();
        Log.i("スケール操作", "getScaleController getMaxScale()=" + cont.getMaxScale());
        Log.i("スケール操作", "getScaleController getMinScale()=" + cont.getMinScale());
*/

        mCharacterNode.setOnTapListener(new Node.OnTapListener() {
            @Override
            public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                        /*Quaternion q = mCharacterNode.getLocalRotation();
                        Log.i("回転", "q.w=" + q.w + " q.x=" + q.x + " q.y=" + q.y + " q.z=" + q.z);
                        */

                Vector3 scale = mCharacterNode.getLocalScale();
                Log.i("サイズ間", "scale x=" + scale.x + " y=" + scale.y + " z=" + scale.z);
            }
        });

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
//                        animator.end();

                        break;

                    case CharacterNode.COLLISION_TYPE_OBSTACLE:
                        // ゴール失敗処理

                        break;
                }
            }
        });
    }


    /*
     * ステージ上オブジェクトのNode生成
     */
    private void createObjOnStageNode(AnchorNode anchorNode) {

        TransformationSystem transformationSystem = arFragment.getTransformationSystem();

        // Nodeスケール
        final float minScale = 0.10f;
        Vector3 scaleVector = new Vector3(minScale, minScale, minScale);

        //------------------------------
        // ステージ上オブジェクトのNode生成
        //-----------------------------
        int i = 0;
        for (ModelRenderable renderable : mObjOnStageRenderable) {

            CharacterNode node = new CharacterNode(transformationSystem);

            node.setName( mObjOnStageName.get(i) );
            node.getScaleController().setMinScale(minScale);
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
        final float stageSize = 0.3f;

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

        Log.i("ランダム位置生成", "side=" + side);
        Log.i("ランダム位置生成", "x=" + position.x + " z=" + position.z);

        return position;
    }

    /*
     * キャラクターが配置された四辺に応じて、向きを取得
     */
    private float getCharacterInitFacingAngle( int side ) {

        // 角度
        float angle;

        // 4辺毎にランダム位置を切り分け
        switch ( side ) {
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
    private Quaternion getCharacterInitFacingDirection( int side, float angle ) {

        // 軸
        Vector3 axis = new Vector3();
        axis.x = 0.0f;
        axis.z = 0.0f;

        // 4辺毎にランダム位置を切り分け
        switch ( side ) {
            case STAGE_BOTTOM:
                axis.y = CharacterNode.calcQuaternionYvalue( angle );
                break;

            case STAGE_TOP:
                axis.y = CharacterNode.calcQuaternionYvalue( angle );
                break;

            case STAGE_LEFT:
                axis.y = CharacterNode.calcQuaternionYvalue( angle );
                break;

            case STAGE_RIGHT:
                axis.y = CharacterNode.calcQuaternionYvalue( angle );
                break;

            default:
                axis.y = CharacterNode.calcQuaternionYvalue( angle );
                break;
        }

        Log.i("ランダム位置生成", "angle=" + angle);

        // 向きたい方向のQuaternion情報を返す
        return (new Quaternion( axis , angle));
    }

    /*
     * お試し：平面ドットのビジュアル変更
     */
    private void setPlaneVisual( Context context ) {

        ArSceneView arSceneView = arFragment.getArSceneView();

        Texture.Sampler sampler =
                Texture.Sampler.builder()
                        .setMinFilter(Texture.Sampler.MinFilter.LINEAR)
                        .setWrapMode(Texture.Sampler.WrapMode.REPEAT)
                        .build();

        // R.drawable.custom_texture is a .png file in src/main/res/drawable
        Texture.builder()
                .setSource( context, R.drawable.green_square)
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
}