package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

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
    public static final int PROCESS_CONTENTS_IF_COLLISION_OBSTACLE = 0;
    public static final int PROCESS_CONTENTS_IF_EATABLE = 1;
    public static final int PROCESS_CONTENTS_IF_POISON = 2;

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
        this(context, attrs, 0, xmlBlockInfo);
    }
    public IfBlock(Context context, AttributeSet attrs, int defStyle, Gimmick.XmlBlockInfo xmlBlockInfo) {
        super(context, attrs, defStyle, xmlBlockInfo);
        setLayout( R.layout.process_block_if );
    }

    /*
     * レイアウト設定
     */
    @Override
    public void setLayout(int layoutID) {
        super.setLayout( layoutID );

        // 処理ブロック内の内容を書き換え
        rewriteProcessContents(mXmlBlockInfo.stringId );
    }

    /*
     * 処理ブロック内の内容を書き換え
     */
    @Override
    public void rewriteProcessContents(int stringID) {

        // 文言IDをレイアウトに設定
        TextView tv_contents = findViewById(R.id.tv_contents);
        tv_contents.setText(stringID);
    }

    /*
     * 条件成立判定
     *   @return：条件成立- true
     *   @return：条件不成立- false
     */
    @Override
    public boolean isCondition(CharacterNode characterNode) {

        //--------------------
        // if文に応じた条件判定
        //--------------------
        switch (mXmlBlockInfo.contents) {
            // 障害物と衝突中かどうか
            case PROCESS_CONTENTS_IF_COLLISION_OBSTACLE:
                return characterNode.isObstacle();

            // 目の前に食べ物があるかどうか
            case PROCESS_CONTENTS_IF_EATABLE:
                return ((AnimalNode)characterNode).isEatable();

            // 目の前に毒があるかどうか
            case PROCESS_CONTENTS_IF_POISON:
                return ((AnimalNode)characterNode).isPoison();

            default:
                break;
        }

        return false;
    }
}
