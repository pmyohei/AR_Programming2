package com.mark.ar_sample;

import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.ux.RotationController;
import com.google.ar.sceneform.ux.ScaleController;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.ux.TransformationSystem;
import com.google.ar.sceneform.ux.TranslationController;

public class TestAnimationNode extends TransformableNode {

    public TestAnimationNode(TransformationSystem transformationSystem) {
        super(transformationSystem);



    }


    public void setPosY( float posY ){
        Vector3 vec3 = getLocalPosition();
        vec3.y = posY;
        super.setLocalPosition( vec3 );
    }

    public void setPosZ( float posZ ){
        Vector3 vec3 = getLocalPosition();
        vec3.z = posZ;
        super.setLocalPosition( vec3 );
    }


}
