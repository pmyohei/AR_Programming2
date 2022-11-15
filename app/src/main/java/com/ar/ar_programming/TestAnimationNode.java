package com.ar.ar_programming;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.ux.TransformationSystem;

public class TestAnimationNode extends TransformableNode {

    Scene mScene;

    public TestAnimationNode(TransformationSystem transformationSystem) {
        super(transformationSystem);
    }

    /*
     * アニメーション初期化処理
     *   Sceneを保持していなければ保持する
     * 　※アニメーション前に必ずコールすること
     */
    public void initAnimation(){

        if( mScene != null ){
            return;
        }
        mScene = getScene();
    }

    /*
     * 衝突検知
     */
    private void detectionCollision(){

        Node node1 = mScene.overlapTest( this );
        if( node1 != null ){
//            Log.i("衝突検知", "衝突検出=" + node1.getName() );
        } else {
//            Log.i("衝突検知", "衝突検出なし" );
        }
    }

    public void setPosY( float posY ){
        Vector3 vec3 = getLocalPosition();
        vec3.y = posY;
        super.setLocalPosition( vec3 );

        // 衝突検知
        detectionCollision();
    }

    public void setPosZ( float posZ ){
        Vector3 vec3 = getLocalPosition();
        vec3.z = posZ;
        super.setLocalPosition( vec3 );
    }


}
