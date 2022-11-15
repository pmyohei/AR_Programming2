package com.ar.ar_programming;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.ux.TransformationSystem;
import com.ar.ar_programming.databinding.FragmentFirstBinding;

import java.util.concurrent.CompletableFuture;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private ArFragment arFragment;
    private ModelRenderable mglbRenderable;
    private ModelRenderable mRedSphereRenderable;
    private ModelRenderable mBlueSphereRenderable;
    private ModelRenderable mRedCubeRenderable;
    private ModelRenderable mBlueCubeRenderable;
    private ViewRenderable mTextViewRenderable;


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
        arFragment = (ArFragment)getChildFragmentManager().findFragmentById(R.id.sceneform_fragment);

        //------------------------------------------
        // 3Dモデルレンダリング「ModelRenderable」の生成
        // ------------------------------------------
        // レンダリングは非同期で生成する
        CompletableFuture<ModelRenderable> modelCompletableFuture =
                ModelRenderable
                        .builder()
//                      .setSource(view.getContext(), Uri.parse("models/test_anim.glb"))
//                      .setSource(view.getContext(), Uri.parse("models/tree.glb"))
//                      .setSource(view.getContext(), Uri.parse("models/halloween.glb"))
//                        .setSource(view.getContext(), Uri.parse("models/sample_bear_small2.glb"))
//                        .setSource(view.getContext(), Uri.parse("models/sample_bear_ver3_born_anim.glb"))
//                        .setSource(view.getContext(), Uri.parse("models/box_02.glb"))
                        .setSource(view.getContext(), Uri.parse("models/box_01.glb"))
//                      .setSource(view.getContext(), Uri.parse("models/steampunk_vehicle.gltf"))
                        .setIsFilamentGltf(true)    // これは上のファイルを読み込む場合は必要なよう
                        .build();

        // 非同期処理結果として、指定したレンダリングを受け取る
        modelCompletableFuture
                .thenAccept( renderable -> mglbRenderable = renderable )
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(view.getContext(), "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();

                            Log.i("AR調査", "ModelRenderable　失敗");
                            return null;
                        });

        //----------------------------------------------
        // 単純な図形のレンダリング「Material」の生成
        // ----------------------------------------------
        // 非同期にてMaterialを生成
        MaterialFactory.makeOpaqueWithColor( view.getContext(), new Color(android.graphics.Color.RED) )
                        .thenAccept( material ->  {
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
        MaterialFactory.makeOpaqueWithColor( view.getContext(), new Color(android.graphics.Color.BLUE) )
                .thenAccept( material ->  {
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
                .setView(view.getContext(), R.layout.sample_card)
                .build()
                .thenAccept(renderable -> mTextViewRenderable = renderable);

        if( arFragment == null ){
            Log.i("AR調査", "arFragment == null");
            return;
        }

        //----------------------------------------------
        // 平面タップリスナーの設定
        // ----------------------------------------------
        arFragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
            @Override
            public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {

                // Renderable未生成なら、処理なし
//                if( mglbRenderable == null || mRedSphereRenderable == null || mBlueSphereRenderable == null || mRedCubeRenderable == null ){
//                    return;
//                }

                Log.i("AR調査", "リスナー働いている2");

                //----------------------------------
                // AnchorNodeの生成／Sceneへの追加
                //----------------------------------
                // ARScene
                Scene scene = arFragment.getArSceneView().getScene();
                // アンカーノードを生成して、Sceneに追加
                Anchor anchor = hitResult.createAnchor();
                AnchorNode anchorNode = new AnchorNode(anchor);
                anchorNode.setParent( scene );

                Vector3 posa = anchorNode.getLocalPosition();
                Log.i("メソッド調査", "アンカーノード位置 x=" + posa.x + " y=" + posa.y + " z="  + posa.z);

                //----------------------------------
                // 3DモデルNodeの生成
                //----------------------------------
                // AnchorNodeを親として、モデル情報からNodeを生成
                TransformationSystem transformationSystem = arFragment.getTransformationSystem();

//                TestAnimationNode modelNode1 = new TestAnimationNode( transformationSystem );
                TransformableNode modelNode1 = new TransformableNode( transformationSystem );
                modelNode1.setName( "Node1" );
                modelNode1.setParent(anchorNode);
                modelNode1.setRenderable( mglbRenderable );
                modelNode1.select();

//                TransformableNode modelNode2 = new TransformableNode( transformationSystem );
                TestAnimationNode modelNode2 = new TestAnimationNode( transformationSystem );
                modelNode2.setName( "Node2" );
                modelNode2.setParent(modelNode1);
                modelNode2.setLocalPosition( new Vector3( 0f, 0.2f, 0f ) );
                modelNode2.setRenderable( mBlueCubeRenderable );
                modelNode2.select();

                Log.i("ピンチ操作", "null? → getScaleController=" + modelNode2.getScaleController() );
                Log.i("ピンチ操作", "null? → getTranslationController=" + modelNode2.getTranslationController() );
                Log.i("ピンチ操作", "null? → getRotationController=" + modelNode2.getRotationController() );

//                DisplayMetrics metrics = getResources().getDisplayMetrics();
//                ScaleController testscale = new ScaleController( modelNode1, new PinchGestureRecognizer( new GesturePointersUtility( metrics ) ));

//                Node node1 = scene.overlapTest( modelNode2 );
//                Node node2 = scene.overlapTest( modelNode1 );
//
//                Log.i("衝突検知", "modelNode2 と衝突したNode=" + node1.getName() );
//                Log.i("衝突検知", "modelNode1 と衝突したNode=" + node2.getName() );

                //----------------------------------
                // 3DモデルNodeへのアニメーション設定
                //----------------------------------
                final int durationInMilliseconds = 1000;
                final float minimumIntensity = 0.0f;
                final float maximumIntensity = 1.0f;

                // アニメーション初期化処理：必須
                modelNode2.initAnimation();

                // 上下移動のアニメーション
                ValueAnimator intensityAnimator = ObjectAnimator.ofFloat( modelNode2, "posY", minimumIntensity, maximumIntensity);
                intensityAnimator.setDuration(durationInMilliseconds);
                intensityAnimator.setRepeatCount(ValueAnimator.INFINITE);
                intensityAnimator.setRepeatMode(ValueAnimator.REVERSE);
//                intensityAnimator.start();

//                Log.i("アニメーション", "getAnimationCount()=" + modelNode1.getRenderableInstance().getAnimationCount() );

                // モデルに付与されたアニメーションの実行
//                modelNode1.getRenderableInstance().animate(true).start();
//                Log.i("AR調査", "アニメーション数=" + modelNode1.getRenderableInstance().getAnimationCount());

                //---------------------------
                // ノードタッチ検出とビューの生成
                // ※実験
                //---------------------------
                modelNode1.setOnTapListener(new Node.OnTapListener() {
                    @Override
                    public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                        Log.i("onTap", "onTapを検出");
                        //modelNode1.setLocalScale( new Vector3( 2, 3, 1 ) );

                        Vector3 localPosition = modelNode1.getLocalPosition();
                        Vector3 worldPosition = modelNode1.getWorldPosition();
                        Vector3 scale = modelNode1.getLocalScale();
                        Vector3 scaleW = modelNode1.getWorldScale();

                        Log.i("メソッド調査", "ノード位置(local) x=" + localPosition.x + " y=" + localPosition.y + " z="  + localPosition.z);
                        Log.i("メソッド調査", "ノード位置(world) x=" + worldPosition.x + " y=" + worldPosition.y + " z="  + worldPosition.z);
                        Log.i("メソッド調査", "ノードスケール(local) x=" + scale.x + " y=" + scale.y + " z="  + scale.z);
                        Log.i("メソッド調査", "ノードスケール(world) x=" + scaleW.x + " y=" + scaleW.y + " z="  + scaleW.z);

                        if( mTextViewRenderable == null ){
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
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}