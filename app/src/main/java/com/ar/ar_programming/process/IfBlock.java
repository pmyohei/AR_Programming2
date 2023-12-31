package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.ar.ar_programming.GimmickManager;
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
        this(context, attrs, 0, xmlBlockInfo, R.layout.block_if);
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
     * 処理開始
     */
    @Override
    public void startProcess(CharacterNode characterNode) {

        Log.i("ブロック処理の流れ", "If startProcess()開始 type=" + mXmlBlockInfo.type);
        Log.i("ブロック処理の流れ", "If startProcess()開始 action=" + mXmlBlockInfo.action);

        //-----------------------------
        // ネスト内処理ブロック数チェック
        //-----------------------------
        // ネスト内に処理ブロックがなければ
        if ( !hasNestBlock() ) {
            // 次の処理ブロックへ
            tranceNextBlock(characterNode);
            return;
        }

        //-----------------------------
        // 条件判定
        //-----------------------------
        // 条件成立の場合
        if ( isCondition(characterNode) ) {

            // ネスト内の処理ブロックを実行
            ProcessBlock nextBlock = (ProcessBlock) mNestStartBlockFirst.getBelowBlock();
            nextBlock.startProcess(characterNode);
            return;
        }

        // 条件成立の場合、ネストブロックの下のブロックへ
        tranceNextBlock(characterNode);
    }

    /*
     * 条件成立判定
     *   @return：条件成立- true
     *   @return：条件不成立- false
     */
    @Override
    public boolean isCondition(CharacterNode characterNode) {

        boolean result;

        Log.i("ブロック処理の流れ", "ifブロック isCondition()開始");

        //-----------------------------
        // if文：動作　に応じた判定
        //-----------------------------
        switch ( mXmlBlockInfo.action ) {

            // 「xxxが目の前にあるか」
            case GimmickManager.BLOCK_CONDITION_FRONT:
                result = isConditionFront(characterNode);
                break;

            // 「xxxの方をキャラクターが向いてるか」
            case GimmickManager.BLOCK_CONDITION_FACING:
                result = isConditionFacing(characterNode, mXmlBlockInfo.targetNode_1);
                Log.i("強い敵", "ifブロック isCondition() 対象Node名=" + mXmlBlockInfo.targetNode_1);
                Log.i("強い敵", "ifブロック isCondition() 判定結果=" + result);
                break;

            // 「xxxを倒したことがあるか」
            case GimmickManager.BLOCK_CONDITION_DEFEAT:
                result = isConditionRemoved(characterNode);
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
    public boolean isConditionFront(CharacterNode characterNode) {

        //----------------
        // 物体で条件判定
        //----------------
        Log.i("ブロック処理の流れ", "ifブロック isConditionFacing()");
        return characterNode.isNodeCollision( mXmlBlockInfo.targetNode_1 );
    }
}
