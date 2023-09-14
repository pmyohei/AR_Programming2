package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.ar.ar_programming.GimmickManager;
import com.ar.ar_programming.character.AnimalNode;
import com.ar.ar_programming.character.CharacterNode;
import com.ar.ar_programming.Gimmick;
import com.ar.ar_programming.R;


/*
 * ネストあり処理ブロック（if文）
 */
public class IfBlock extends NestBlock {

    //---------------------------
    // 定数
    //---------------------------
//    public static final int PROCESS_CONTENTS_IF_COLLISION_OBSTACLE = 0;
//    public static final int PROCESS_CONTENTS_IF_EATABLE = 1;
//    public static final int PROCESS_CONTENTS_IF_POISON = 2;

    //---------------------------
    // フィールド変数
    //---------------------------

    /*
     * コンストラクタ
     */
    public IfBlock(Context context, Gimmick.XmlBlockInfo xmlBlockInfo) {
        this(context, null, xmlBlockInfo);
    }
    public IfBlock(Context context, AttributeSet attrs, Gimmick.XmlBlockInfo xmlBlockInfo) {
        this(context, attrs, 0, xmlBlockInfo, R.layout.process_block_if_else);
    }
    public IfBlock(Context context, AttributeSet attrs, int defStyle, Gimmick.XmlBlockInfo xmlBlockInfo, int layout) {
        super(context, attrs, defStyle, xmlBlockInfo);
        setLayout(layout);
    }

    /*
     * レイアウト設定
     */
    @Override
    public void setLayout(int layoutID) {
        super.setLayout(layoutID);

        // 処理ブロック内の内容を書き換え
        rewriteProcessContents();
    }

    /*
     * 条件成立判定
     *   @return：条件成立- true
     *   @return：条件不成立- false
     */
    @Override
    public boolean isCondition(CharacterNode characterNode) {

        boolean result;

        Log.i("ギミック変更", "ifブロック isCondition()");

        //-----------------------------
        // if文：動作　に応じた判定
        //-----------------------------
        switch (mXmlBlockInfo.conditionMotion) {

            // 「xxxが目の前にあるか」
            case GimmickManager.BLOCK_CONDITION_FRONT:
                result = isConditionFacing(characterNode);
                break;

            default:
                result = false;
                break;
        }

        return result;
    }


    /*
     * 条件成立判定：目の前にxxxがあるか
     *   @return：条件成立- true
     *   @return：条件不成立- false
     */
    public boolean isConditionFacing(CharacterNode characterNode) {

        //----------------
        // 物体で条件判定
        //----------------
        Log.i("ギミック変更", "ifブロック isConditionFacing()");
        return characterNode.isFrontNode( mXmlBlockInfo.conditionObject );
    }
}
