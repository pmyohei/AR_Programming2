package com.mark.ar_sample;

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
    private Renderable mRenderable;
    private ModelRenderable mRedSphereRenderable;
    private ModelRenderable mBlueSphereRenderable;
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

        //ArFragmentを保持
        arFragment = (ArFragment)getChildFragmentManager().findFragmentById(R.id.sceneform_fragment);

        //明示的に完了できる非同期処理クラス
        //まずは、「AR上に表示させるオブジェクトモデル」のCompletableFutureを生成する
        CompletableFuture<ModelRenderable> completableFuture =
            ModelRenderable
            .builder()
            //.setSource(view.getContext(), Uri.parse("models/tree.glb"))
            .setSource(view.getContext(), Uri.parse("models/halloween.glb"))
            .setIsFilamentGltf(true)    //これは上のファイルを読み込む場合は必要なよう
            .build();

        //非同期処理にて、AR上に表示させるオブジェクトモデルを作成
        completableFuture
            .thenAccept( renderable -> mRenderable = renderable )
            .exceptionally(
                    throwable -> {
                        Toast toast =
                                Toast.makeText(view.getContext(), "Unable to load andy renderable", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                        Log.i("AR調査", "ModelRenderable　失敗");
                        return null;
                    });

        //非同期処理にて、AR上に表示させるオブジェクトモデルを作成
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
                });


        ViewRenderable
                .builder()
                .setView(view.getContext(), R.layout.sample_card)
                .build()
                .thenAccept(renderable -> mTextViewRenderable = renderable);

        if( arFragment == null ){
            Log.i("AR調査", "arFragment == null");
            return;
        }

        //平面タップリスナーの設定
        arFragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
                @Override
                public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {

                    //Renderable未生成なら、処理なし
                    if( mRenderable == null || mRedSphereRenderable == null || mBlueSphereRenderable == null ){
                        return;
                    }

                    //何を描画するかはランダム
                    Random random = new Random();
                    int num = random.nextInt(3);

                    Renderable renderable;
                    switch (num){
                        case 0:
                            renderable = mRenderable;
                            break;
                        case 1:
                            renderable = mRedSphereRenderable;
                            break;
                        case 2:
                        default:
                            renderable = mBlueSphereRenderable;
                            break;
                    }

//                    Renderable renderable = mRenderable;
                    /*Renderable renderable = mRedSphereRenderable;
                    if (renderable == null) {
                        return;
                    }*/

                    Log.i("AR調査", "リスナー働いている2");

                    //AnchorNodeの生成
                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent( arFragment.getArSceneView().getScene() );

                    //ノードに対するジェスチャ
                    TransformationSystem transformationSystem = arFragment.getTransformationSystem();

                    //AnchorNodeを親として、モデル情報からNodeを生成
                    TransformableNode node = new TransformableNode( transformationSystem );
                    node.setLocalScale( new Vector3( 0.2f, 0.2f, 0.2f ) );
                    node.setParent(anchorNode);
                    node.setRenderable( renderable );
                    node.select();

                    node.setOnTapListener(new Node.OnTapListener() {
                            @Override
                            public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                                Log.i("onTap", "onTapを検出");
                                //node.setLocalScale( new Vector3( 2, 3, 1 ) );

                                if( mTextViewRenderable == null ){
                                    return;
                                }

                                //ノードサイズ
                                Vector3 vector3 = node.getLocalScale();

                                //テキストノードを生成して、タッチされたノードの少し上に表示させる
                                TransformableNode textNode = new TransformableNode( transformationSystem );
                                textNode.setParent( node );
                                textNode.setLocalPosition( new Vector3( 0f, vector3.y + 0.0f, 0f ) );
                                textNode.setRenderable(mTextViewRenderable);
                                textNode.select();
                            }
                        }
                    );
                }
            }
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}