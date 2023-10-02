package com.ar.ar_programming;

/*
 * ステージ選択リスト
 */
public class StageList {

    public String mStageName;
    public boolean mIsClear;
    public boolean mIsSelect;

    public StageList( String stageName ) {
        mStageName = stageName;
        mIsClear = false;
        mIsSelect = false;
    }
}
