package com.ar.ar_programming;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class HowToPlayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_play);
    }

    /*
     * 押下処理：「ARの始め方」
     */
    public void onHowToStartARClicked(View view) {
        HelpDialog helpDialog = new HelpDialog();
        helpDialog.show(this.getSupportFragmentManager(), "help");

        // onStart()終了リスナーの設定
        helpDialog.setOnStartEndListerner( new HelpDialog.OnStartEndListener() {
            @Override
            public void onStartEnd() {

                // ヘルプページリスト
                List<Integer> pageList = new ArrayList<>();
                pageList.add(R.layout.help_page_about_ar_1);
                pageList.add(R.layout.help_page_about_ar_2);
                pageList.add(R.layout.help_page_about_ar_3);
                pageList.add(R.layout.help_page_about_ar_4);

                // ページを設定
                helpDialog.setupHelpPage( pageList );
            }
        });
    }

    /*
     * 押下処理：「ブロック基本操作」
     */
    public void onHowToBlockOperation(View view) {
        HelpDialog helpDialog = new HelpDialog();
        helpDialog.show(this.getSupportFragmentManager(), "help");

        // onStart()終了リスナーの設定
        helpDialog.setOnStartEndListerner( new HelpDialog.OnStartEndListener() {
            @Override
            public void onStartEnd() {

                // ヘルプページリスト
                List<Integer> pageList = new ArrayList<>();
                pageList.add(R.layout.help_page_how_to_add_block);
                pageList.add(R.layout.help_page_how_to_move_block);
                pageList.add(R.layout.help_page_how_to_remove_block);
                pageList.add(R.layout.help_page_how_to_set_volume);

                // ページを設定
                helpDialog.setupHelpPage( pageList );
            }
        });
    }

    /*
     * 押下処理：「実行ブロックとは？」
     */
    public void onWhatIsExeBlock(View view) {
        HelpDialog helpDialog = new HelpDialog();
        helpDialog.show(this.getSupportFragmentManager(), "help");

        // onStart()終了リスナーの設定
        helpDialog.setOnStartEndListerner( new HelpDialog.OnStartEndListener() {
            @Override
            public void onStartEnd() {

                // ヘルプページリスト
                List<Integer> pageList = new ArrayList<>();
                pageList.add(R.layout.help_page_about_exe_block);

                // ページを設定
                helpDialog.setupHelpPage( pageList );
            }
        });
    }

    /*
     * 押下処理：「Loopブロックとは？」
     */
    public void onWhatIsLoopBlock(View view) {
        HelpDialog helpDialog = new HelpDialog();
        helpDialog.show(this.getSupportFragmentManager(), "help");

        // onStart()終了リスナーの設定
        helpDialog.setOnStartEndListerner( new HelpDialog.OnStartEndListener() {
            @Override
            public void onStartEnd() {

                // ヘルプページリスト
                List<Integer> pageList = new ArrayList<>();
                pageList.add(R.layout.help_page_about_loop_block);

                // ページを設定
                helpDialog.setupHelpPage( pageList );
            }
        });
    }

    /*
     * 押下処理：「ifブロックとは？」
     */
    public void onWhatIsIfBlock(View view) {
        HelpDialog helpDialog = new HelpDialog();
        helpDialog.show(this.getSupportFragmentManager(), "help");

        // onStart()終了リスナーの設定
        helpDialog.setOnStartEndListerner( new HelpDialog.OnStartEndListener() {
            @Override
            public void onStartEnd() {

                // ヘルプページリスト
                List<Integer> pageList = new ArrayList<>();
                pageList.add(R.layout.help_page_about_if_block_1);
                pageList.add(R.layout.help_page_about_if_block_2);
                pageList.add(R.layout.help_page_about_if_block_3);

                // ページを設定
                helpDialog.setupHelpPage( pageList );
            }
        });
    }

    /*
     * 押下処理：「if elseブロックとは？」
     */
    public void onWhatIsIfElseBlock(View view) {
        HelpDialog helpDialog = new HelpDialog();
        helpDialog.show(this.getSupportFragmentManager(), "help");

        // onStart()終了リスナーの設定
        helpDialog.setOnStartEndListerner( new HelpDialog.OnStartEndListener() {
            @Override
            public void onStartEnd() {

                // ヘルプページリスト
                List<Integer> pageList = new ArrayList<>();
                pageList.add(R.layout.help_page_about_if_else_block_1);
                pageList.add(R.layout.help_page_about_if_else_block_2);
                pageList.add(R.layout.help_page_about_if_else_block_3);

                // ページを設定
                helpDialog.setupHelpPage( pageList );
            }
        });
    }

    /*
     * 押下処理：「if elseifブロックとは？」
     */
    public void onWhatIsIfElseIfBlock(View view) {
        HelpDialog helpDialog = new HelpDialog();
        helpDialog.show(this.getSupportFragmentManager(), "help");

        // onStart()終了リスナーの設定
        helpDialog.setOnStartEndListerner( new HelpDialog.OnStartEndListener() {
            @Override
            public void onStartEnd() {

                // ヘルプページリスト
                List<Integer> pageList = new ArrayList<>();
                pageList.add(R.layout.help_page_about_if_elseif_block_1);
                pageList.add(R.layout.help_page_about_if_elseif_block_2);
                pageList.add(R.layout.help_page_about_if_elseif_block_3);
                pageList.add(R.layout.help_page_about_if_elseif_block_4);

                // ページを設定
                helpDialog.setupHelpPage( pageList );
            }
        });
    }

}