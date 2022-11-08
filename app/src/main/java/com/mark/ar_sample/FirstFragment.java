package com.mark.ar_sample;

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
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.ux.TransformationSystem;
import com.mark.ar_sample.databinding.FragmentFirstBinding;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private ArFragment arFragment;
    private ModelRenderable mglbRenderable;
    private ModelRenderable mRedSphereRenderable;
    private ModelRenderable mBlueSphereRenderable;
    private ModelRenderable mRedCubeRenderable;
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
                        .setSource(view.getContext(), Uri.parse("models/sample_bear_ver3_born_anim.glb"))
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
        CompletableFuture<Material> materialCompletableFuture =
                MaterialFactory.makeOpaqueWithColor( view.getContext(), new Color(android.graphics.Color.RED) );

        //円のモデル生成完了時、生成されたMaterialからモデルを生成して保持する
        materialCompletableFuture
                .thenAccept(
                        material -> {
                            //半径／中心／素材
                            mRedSphereRenderable = ShapeFactory.makeSphere(0.1f, new Vector3(0.0f, 0.15f, 0.0f), material);

                            //！ここで色を変えると、前に作成したRenderableにも影響がでる
                            //material.setFloat3( MATERIAL_COLOR , new Color(android.graphics.Color.BLUE) );
                            mBlueSphereRenderable = ShapeFactory.makeSphere(0.05f, new Vector3(0.0f, 0.0f, 0.0f), material);

                            mRedCubeRenderable = ShapeFactory.makeCube(
                                    new Vector3(1.2f, 0.3f, 0.4f),
                                    new Vector3(0.1f, 0.1f, 0.1f),
                                    material);
                        });

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
                if( mglbRenderable == null || mRedSphereRenderable == null || mBlueSphereRenderable == null || mRedCubeRenderable == null ){
                    return;
                }

                // 何を描画するかはランダム
                Random random = new Random();
                int num = random.nextInt(3);

                Renderable renderable;
                switch (num){
                    case 0:
                        renderable = mglbRenderable;
                        break;

                    case 1: renderable = mRedSphereRenderable;
                        break;

                    case 2:
                    default: renderable = mBlueSphereRenderable;
                        break;
                }

//                    renderable = mRedCubeRenderable;
                renderable = mglbRenderable;

//                    Renderable renderable = mRenderable;
                    /*Renderable renderable = mRedSphereRenderable;
                    if (renderable == null) {
                        return;
                    }*/

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
//                TestAnimationNode modelNode = new TestAnimationNode( transformationSystem );
                TransformableNode modelNode = new TransformableNode( transformationSystem );
                modelNode.setParent(anchorNode);
                modelNode.setRenderable( renderable );
                modelNode.select();

                //----------------------------------
                // 3DモデルNodeへのアニメーション設定
                //----------------------------------
                final int durationInMilliseconds = 1000;
                final float minimumIntensity = 0.0f;
                final float maximumIntensity = 1.0f;

                // 上下移動のアニメーション
//                ValueAnimator intensityAnimator = ObjectAnimator.ofFloat( modelNode, "posY", minimumIntensity, maximumIntensity);
//                intensityAnimator.setDuration(durationInMilliseconds);
//                intensityAnimator.setRepeatCount(ValueAnimator.INFINITE);
//                intensityAnimator.setRepeatMode(ValueAnimator.REVERSE);
//                intensityAnimator.start();

                Log.i("アニメーション", "getAnimationCount()=" + modelNode.getRenderableInstance().getAnimationCount() );

                // モデルに付与されたアニメーションの実行
                modelNode.getRenderableInstance().animate(true).start();
                Log.i("AR調査", "アニメーション数=" + modelNode.getRenderableInstance().getAnimationCount());

                //---------------------------
                // ノードタッチ検出とビューの生成
                // ※実験
                //---------------------------
                modelNode.setOnTapListener(new Node.OnTapListener() {
                    @Override
                    public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                        Log.i("onTap", "onTapを検出");
                        //modelNode.setLocalScale( new Vector3( 2, 3, 1 ) );

                        Vector3 pos = modelNode.getLocalPosition();
                        Vector3 posw = modelNode.getWorldScale();
                        Vector3 scale = modelNode.getLocalScale();
                        Vector3 scaleW = modelNode.getWorldScale();

                        Log.i("メソッド調査", "ノード位置(local) x=" + pos.x + " y=" + pos.y + " z="  + pos.z);
                        Log.i("メソッド調査", "ノード位置(world) x=" + posw.x + " y=" + posw.y + " z="  + posw.z);
                        Log.i("メソッド調査", "ノードスケール(local) x=" + scale.x + " y=" + scale.y + " z="  + scale.z);
                        Log.i("メソッド調査", "ノードスケール(world) x=" + scaleW.x + " y=" + scaleW.y + " z="  + scaleW.z);
                        Log.i("メソッド調査", "ノードスケール(world) x=" + scaleW.x + " y=" + scaleW.y + " z="  + scaleW.z);

                        if( mTextViewRenderable == null ){
                            return;
                        }

                        //ノードサイズ
                        Vector3 vector3 = modelNode.getLocalScale();

                        //テキストノードを生成して、タッチされたノードの少し上に表示させる
                        TransformableNode textNode = new TransformableNode( transformationSystem );
                        textNode.setParent( modelNode );
                        textNode.setLocalPosition( new Vector3( -0.5f, 0.3f + 0.0f, 0f ) );
                        textNode.setRenderable(mTextViewRenderable);
                        textNode.select();
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