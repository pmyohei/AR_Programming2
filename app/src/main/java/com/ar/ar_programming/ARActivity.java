package com.ar.ar_programming;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

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
                mMenuClickListener.onHowToClick();
                return true;

            case R.id.action_setting:
                mMenuClickListener.onSettingClick();
                return true;

            case R.id.action_goal_guide:
                mMenuClickListener.onGoalGuide();
                return true;

            case R.id.action_clear_field:
                mMenuClickListener.onClearFieldClick();
                return true;

            case R.id.action_init_programming:
                mMenuClickListener.onInitProgrammingClick();
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
        void onHowToClick();
        void onSettingClick();
        void onGoalGuide();
        void onClearFieldClick();
        void onInitProgrammingClick();
    }

    /*
     * ゲーム制御Fabクリックインターフェース
     */
    public interface PlayControlListener {
        void onPlayControlClick( FloatingActionButton fab );
    }
}