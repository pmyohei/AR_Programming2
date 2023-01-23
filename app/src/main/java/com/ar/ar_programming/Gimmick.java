package com.ar.ar_programming;

import android.content.Context;
import android.content.Intent;

import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;

import java.util.ArrayList;
import java.util.Collections;

public class Gimmick {

    //---------------------------
    // 定数
    //---------------------------

    //---------------------------
    // フィールド変数
    //---------------------------
    public String successCondition;
    public String stageName;
    public String characterName;
    public Vector3 characterPositionVec;
    public String goalName;
    public Vector3 goalPositionVec;
    public ArrayList<String> objectNameList;
    public ArrayList<Integer> objectNumList;
    public ArrayList<String> objectKindList;
    public ArrayList<String> blockList;


    /*
     * コンストラクタ
     */
    public Gimmick() {
        objectNameList = new ArrayList<>();
        objectNumList = new ArrayList<>();
        objectKindList = new ArrayList<>();
    }

    /*
     * tmp
     */
    private void tmp() {
    }


    /*
     * ギミックxmlの値デリミタで文字列を分割
     *   想定デリミタ："半角空白（0文字以上）,半角空白（0文字以上）"
     *   例）","、", "、" ,"、" , "
     */
    private String[] splitGimmickDelimiter( String str ) {
        return str.split(" *, *");
    }


    /*
     * キャラクター座標を設定
     */
    public void setCharacterPosition( String position ) {

        String[] strs = splitGimmickDelimiter(position);

        // 正常フォーマットの場合
        if( strs.length == 3 ){
            // 位置形式に変換
            float x = Float.parseFloat( strs[0] );
            float y = Float.parseFloat( strs[1] );
            float z = Float.parseFloat( strs[2] );
            characterPositionVec = new Vector3( x, y, z );
            return;
        }

        // フォーマット異常の場合
        characterPositionVec = new Vector3( 0f, 0f, 0f );
    }

    /*
     * ゴール座標を設定
     */
    public void setGoalPosition( String position ) {

        String[] strs = splitGimmickDelimiter(position);

        // 正常フォーマットの場合
        if( strs.length == 3 ){
            // 位置形式に変換
            float x = Float.parseFloat( strs[0] );
            float y = Float.parseFloat( strs[1] );
            float z = Float.parseFloat( strs[2] );
            goalPositionVec = new Vector3( x, y, z );
            return;
        }

        // フォーマット異常の場合
        goalPositionVec = new Vector3( 1f, 1f, 1f );
    }

    /*
     * オブジェクト名を設定
     */
    public void setObjectName( String objectName ) {

        // オブジェクト名を分割
        String[] strs = splitGimmickDelimiter(objectName);

        // リスト生成
        objectNameList.clear();
        Collections.addAll(objectNameList, strs);
    }

    /*
     * オブジェクト数を設定
     */
    public void setObjectNum( String objectNum ) {

        // オブジェクト名を分割
        String[] strs = splitGimmickDelimiter(objectNum);

        // リスト生成
        objectNumList.clear();
        for( String num: strs ){
            objectNumList.add( Integer.parseInt(num) );
        }
    }

    /*
     * オブジェクト種別を設定
     */
    public void setObjectKind( String objectKind ) {

        // オブジェクト名を分割
        String[] strs = splitGimmickDelimiter(objectKind);

        // リスト生成
        objectNameList.clear();
        Collections.addAll(objectNameList, strs);
    }

    /*
     * ギミックで使用可能なブロックリストを設定
     */
    public void setBlock( String block ) {

        // オブジェクト名を分割
        String[] strs = splitGimmickDelimiter(block);

        // リスト生成
        blockList.clear();
        Collections.addAll(blockList, strs);
    }
}
