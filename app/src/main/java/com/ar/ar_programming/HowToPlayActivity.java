package com.ar.ar_programming;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class HowToPlayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_play);
    }

    /*
     * ヘルプダイアログの表示
     */
    private void showHelpDialog( int pageArrayResID ) {

        HelpDialog helpDialog = new HelpDialog();
        helpDialog.show(this.getSupportFragmentManager(), "help");

        // onStart()終了リスナーの設定
        helpDialog.setOnStartEndListerner( new HelpDialog.HelpDialogListener() {
            @Override
            public void onStartEnd() {
                // ページを設定
                helpDialog.setupHelpPage( pageArrayResID );
            }
            @Override
            public void onDismiss() {
            }
        });
    }

    /*
     * 押下処理：「ARの始め方」
     */
    public void onHowToStartARClicked(View view) {
        showHelpDialog( R.array.how_to_ar );
    }

    /*
     * 押下処理：「ブロック基本操作」
     */
    public void onHowToBlockOperation(View view) {
        showHelpDialog( R.array.how_to_block_operation );
    }

    /*
     * 押下処理：「実行ブロックとは？」
     */
    public void onWhatIsExeBlock(View view) {
        showHelpDialog( R.array.about_exe_block );
    }

    /*
     * 押下処理：「Loopブロックとは？」
     */
    public void onWhatIsLoopBlock(View view) {
        showHelpDialog( R.array.about_loop_block );
    }

    /*
     * 押下処理：「ifブロックとは？」
     */
    public void onWhatIsIfBlock(View view) {
        showHelpDialog( R.array.about_if_block );
    }

    /*
     * 押下処理：「if elseブロックとは？」
     */
    public void onWhatIsIfElseBlock(View view) {
        showHelpDialog( R.array.about_if_else_block );
    }

    /*
     * 押下処理：「if elseifブロックとは？」
     */
    public void onWhatIsIfElseIfBlock(View view) {
        showHelpDialog( R.array.about_if_elseif_block );
    }

}