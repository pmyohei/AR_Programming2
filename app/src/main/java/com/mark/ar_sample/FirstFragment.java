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
import androidx.navigation.fragment.NavHostFragment;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.mark.ar_sample.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private ArFragment arFragment;
    private Renderable mRenderable;
    private ModelRenderable mRedSphereRenderable;

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

        arFragment = (ArFragment)getChildFragmentManager().findFragmentById(R.id.sceneform_fragment);

        ModelRenderable
                .builder()
                //.setSource(view.getContext(), Uri.parse("models/tree.glb"))
                .setSource(view.getContext(), Uri.parse("models/halloween.glb"))
                .setIsFilamentGltf(true)    //これは上のファイルを読み込む場合は必要なよう
                .build()
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


        MaterialFactory.makeOpaqueWithColor(view.getContext(), new Color(android.graphics.Color.RED))
                .thenAccept(
                        material -> {
                            mRedSphereRenderable =
                                    ShapeFactory.makeSphere(
                                            0.1f,
                                            new Vector3(0.0f, 0.15f, 0.0f),
                                            material);
                        });

        if( arFragment == null ){
            Log.i("AR調査", "arFragment == null");
            return;
        }

        arFragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
                @Override
                public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {

                    Renderable renderable = mRenderable;
//                    Renderable renderable = mRedSphereRenderable;
                    if (renderable == null) {
                        return;
                    }

                    Log.i("AR調査", "リスナー働いている2");

                    // Create the Anchor.
                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    // Create the transformable andy and add it to the anchor.
                    TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
                    andy.setParent(anchorNode);
                    andy.setRenderable( renderable );
                    andy.select();
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