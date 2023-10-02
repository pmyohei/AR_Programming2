package com.ar.ar_programming;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager2.widget.ViewPager2;

import com.ar.ar_programming.databinding.ActivityArBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class ARActivity extends AppCompatActivity {

    //---------------------------
    // 定数
    //---------------------------

    //---------------------------
    // フィールド変数
    //---------------------------
    private AppBarConfiguration appBarConfiguration;
    private ActivityArBinding binding;
    private MenuClickListener mMenuClickListener;
    private PlayControlListener mPlayControlListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityArBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        //------------------
        // ゲーム制御Fab設定
        //------------------
        binding.fabGameControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlayControlListener.onPlayControlClick( (FloatingActionButton)view );
            }
        });

        //---------------------
        // アプリ初回起動のみの処理
        //---------------------
        onlyFirstStartApp();

    }

    /*
     * アプリ初回起動のみの処理
     */
    private void onlyFirstStartApp() {

        //------------------
        // 初回起動情報の取得
        //------------------
        boolean isFirstStart = isFirstStart();
        if( !isFirstStart ){
            // 初期起動でなければ、何もしない
            return;
        }

        //------------------
        // 初回起動時処理
        //------------------
        // 初回起動情報の更新
        savedFirstStartInfo();

        // ARのやり方ダイアログを表示
        showHowToPlayDialog();
    }

    /*
     * アプリ初回起動かどうか
     */
    private boolean isFirstStart() {

        // 取得出来ない（初回起動）場合の値
        final boolean defaultValue = true;

        // アプリ初回起動の情報を取得
        SharedPreferences sharedPref = getSharedPreferences( getString(R.string.preference_file_key), MODE_PRIVATE) ;
        return sharedPref.getBoolean( getString(R.string.saved_first_start_key), defaultValue);
    }

    /*
     * アプリ初回起動情報の保存（初回起動済みとして保存）
     */
    private void savedFirstStartInfo() {

        // アプリ初回起動を「初回起動済み」として保存
        SharedPreferences sharedPref = getSharedPreferences( getString(R.string.preference_file_key), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean( getString(R.string.saved_first_start_key), false);
        editor.apply();
    }

    /*
     * アプリ初回起動情報の保存（初回起動済みとして保存）
     */
    private void showHowToPlayDialog() {

        HelpDialog helpDialog = new HelpDialog();
        helpDialog.show(this.getSupportFragmentManager(), "help");

        // onStart()終了リスナーの設定
        helpDialog.setOnStartEndListerner( new HelpDialog.OnStartEndListener() {
            @Override
            public void onStartEnd() {
                // ページを設定
                helpDialog.setupHelpPage( R.array.how_to_ar );
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // menuを割り当て
        getMenuInflater().inflate(R.menu.ar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //-----------------------------
        // menuアクション毎に処理を振り分け
        //-----------------------------
        int id = item.getItemId();
        switch ( id ){
            case R.id.action_how_to_play:
                mMenuClickListener.onMenuHowToClick();
                return true;

            case R.id.action_setting:
                mMenuClickListener.onMenuSettingClick();
                return true;

            case R.id.action_select_stage:
                mMenuClickListener.onMenuSelectStage();
                return true;

            case R.id.action_goal_guide:
                mMenuClickListener.onMenuGoalGuide();
                return true;

            case R.id.action_clear_field:
                mMenuClickListener.onMenuClearFieldClick();
                return true;

            case R.id.action_init_programming:
                mMenuClickListener.onMenuInitProgrammingClick();
                return true;


        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


    /*
     * Menuクリックリスナーの設定
     */
    public void setOnMenuClickListener(MenuClickListener listener ) {
        mMenuClickListener = listener;
    }

    /*
     * ゲーム制御Fabクリックリスナーの設定
     */
    public void setPlayControlListener(PlayControlListener listener ) {
        mPlayControlListener = listener;
    }

    /*
     * Menuクリックインターフェース
     */
    public interface MenuClickListener {
        void onMenuHowToClick();
        void onMenuSettingClick();
        void onMenuSelectStage();
        void onMenuGoalGuide();
        void onMenuClearFieldClick();
        void onMenuInitProgrammingClick();
    }

    /*
     * ゲーム制御Fabクリックインターフェース
     */
    public interface PlayControlListener {
        void onPlayControlClick( FloatingActionButton fab );
    }

}