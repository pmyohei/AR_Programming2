package com.ar.ar_programming;

import static com.ar.ar_programming.Common.TUTORIAL_DEFAULT;
import static com.ar.ar_programming.Common.TUTORIAL_FINISH;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;

import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

public class SettingActivity extends AppCompatActivity {

    //---------------------------
    // 定数
    //---------------------------
    // 呼び出し元への戻り値
    public static final int RESULT_SETTING = 200;
    public static final String IS_CHANGED_KEY = "is_update_key";

    // フィールドサイズ
    public static final int FIELD_SIZE_TABLE = 0;
    public static final int FIELD_SIZE_LIVING = 1;
    // 難易度
    public static final int PLAY_DIFFICULTY_EASY = 0;
    public static final int PLAY_DIFFICULTY_DIFFICULT = 1;
    // キャラクター
    public static final int CHARACTER_ANIMAL = 0;
    public static final int CHARACTER_VEHICLE = 1;

    //---------------------------
    // フィールド変数
    //---------------------------
    // flg
    private boolean mSetChangeFlg;     // ユーザー設定変更フラグ（ユーザーが設定を変更した場合、true）
    private boolean mSavedFlg;      // ユーザー保存フラグ（ユーザーが保存を選択した場合、true）
    // 設定データ
    private int mFileldSize;
    private int mPlayDifficulty;
    private int mCharacter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // フラグ初期化
        mSetChangeFlg = false;
        mSavedFlg = false;

        // Toolbar設定
        setToolbar();

        //----------------------
        // 設定情報をviewに反映
        //----------------------
        // ユーザー保存情報を取得
        boolean isFinishTutorial = getUserSavedData();
        // ユーザ設定情報をレイアウトに反映
        setUserSettingData(isFinishTutorial);
    }

    /*
     * 設定不可のダイアログを表示
     */
    private void showCannotSetDialog(int tutorial) {
        DialogFragment newFragment = new CannotSetDialogFragment( tutorial );
        newFragment.show( getSupportFragmentManager(), "cannotSet" );
    }

    /*
     * ツールバーの設定
     */
    private void setToolbar() {

        // ツールバータイトル
        String title = getString(R.string.setting_title);

        // ツールバー設定
        Toolbar toolbar = findViewById(R.id.toolbar_setting);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);

        // 戻るボタンの表示
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /*
     * ユーザー保存情報を取得
     */
    private boolean getUserSavedData() {

        Resources resources = getResources();

        // 本アプリ共通情報
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);

        //--------------------
        // チュートリアル終了確認
        //--------------------
        // 現在のチュートリアル進行状況を取得
        int tutorial = sharedPref.getInt(getString(R.string.saved_tutorial_key), TUTORIAL_DEFAULT);

        // チュートリアルが未完了の場合
        if (tutorial < TUTORIAL_FINISH) {
            // 設定不可のダイアログを表示
            showCannotSetDialog( tutorial );
            // 設定情報取得なし
            return false;
        }

        //--------------------
        // ユーザー設定情報の取得
        //--------------------
        int defaultValue;
        // フィールドサイズ
        defaultValue = getResources().getInteger(R.integer.saved_field_size_default_key);
        mFileldSize = sharedPref.getInt(getString(R.string.saved_field_size_key), defaultValue);

        // 難易度
        defaultValue = getResources().getInteger(R.integer.saved_difficulty_default_key);
        mPlayDifficulty = sharedPref.getInt(getString(R.string.saved_difficulty_key), defaultValue);

        // キャラクター
        defaultValue = getResources().getInteger(R.integer.saved_character_default_key);
        mCharacter = sharedPref.getInt(getString(R.string.saved_character_key), defaultValue);

        // チュートリアル終了
        return true;
    }

    /*
     * ユーザ設定情報をレイアウトに反映
     */
    private void setUserSettingData( boolean isFinishTutorial ) {

        // チュートリアル完了有無
        if( isFinishTutorial ){
            // 完了しているなら、ユーザー情報を反映
            reflectUserData();
        } else {
            // 未完了なら、設定UIを無効化
            disableSetUI();
        }
    }

    /*
     * ユーザ設定情報のUIを無効化
     */
    private void disableSetUI() {

        // UI無効化
        RadioButton radio_table = findViewById(R.id.radio_table);
        RadioButton radio_living = findViewById(R.id.radio_living);
        RadioButton radio_easy = findViewById(R.id.radio_easy);
        RadioButton radio_difficult = findViewById(R.id.radio_difficult);
        RadioButton radio_animal = findViewById(R.id.radio_animal);
        RadioButton radio_vehicle = findViewById(R.id.radio_vehicle);

        radio_table.setEnabled(false);
        radio_living.setEnabled(false);
        radio_easy.setEnabled(false);
        radio_difficult.setEnabled(false);
        radio_animal.setEnabled(false);
        radio_vehicle.setEnabled(false);
    }

    /*
     * ユーザ設定情報をレイアウトに反映
     */
    private void reflectUserData() {
        // フィールドサイズ
        setFieldSizeRadioButton(mFileldSize);
        // 難易度
        setDifficultyRadioButton(mPlayDifficulty);
        // キャラクター
        setCharacterRadioButton(mCharacter);
    }

    /*
     * ラジオボタン更新：フィールドサイズ
     */
    private void setFieldSizeRadioButton(int selectValue) {

        RadioButton radio_table = findViewById(R.id.radio_table);
        RadioButton radio_living = findViewById(R.id.radio_living);

        //-------------------------------------
        // ユーザー設定に応じたラジオボタンの排他制御
        //-------------------------------------
        switch (selectValue) {

            case FIELD_SIZE_TABLE:
                radio_table.setChecked(true);
                radio_living.setChecked(false);
                break;

            case FIELD_SIZE_LIVING:
                radio_table.setChecked(false);
                radio_living.setChecked(true);
                break;
        }
    }

    /*
     * ラジオボタン更新：難易度
     */
    private void setDifficultyRadioButton(int selectValue) {

        RadioButton radio_easy = findViewById(R.id.radio_easy);
        RadioButton radio_difficult = findViewById(R.id.radio_difficult);

        //-------------------------------------
        // ユーザー設定に応じたラジオボタンの排他制御
        //-------------------------------------
        switch (selectValue) {

            case PLAY_DIFFICULTY_EASY:
                radio_easy.setChecked(true);
                radio_difficult.setChecked(false);
                break;

            case PLAY_DIFFICULTY_DIFFICULT:
                radio_easy.setChecked(false);
                radio_difficult.setChecked(true);
                break;
        }
    }

    /*
     * ラジオボタン更新：キャラクター
     */
    private void setCharacterRadioButton(int selectValue) {

        RadioButton radio_animal = findViewById(R.id.radio_animal);
        RadioButton radio_vehicle = findViewById(R.id.radio_vehicle);

        //-------------------------------------
        // ユーザー設定に応じたラジオボタンの排他制御
        //-------------------------------------
        switch (selectValue) {

            case CHARACTER_ANIMAL:
                radio_animal.setChecked(true);
                radio_vehicle.setChecked(false);
                break;

            case CHARACTER_VEHICLE:
                radio_animal.setChecked(false);
                radio_vehicle.setChecked(true);
                break;
        }
    }


    /*
     * onClick：フィールドサイズ
     */
    public void onFieldSizeClicked(View view) {

        //---------------------------------
        // クリックされたviewidから選択値を取得
        //---------------------------------
        int selectValue;
        int id = view.getId();
        if (id == R.id.radio_table) {
            selectValue = FIELD_SIZE_TABLE;
        } else {
            selectValue = FIELD_SIZE_LIVING;
        }

        // 現在値と同じなら処理なし
        if (selectValue == mFileldSize) {
            return;
        }

        //-----------------
        // レイアウトに反映
        //-----------------
        mFileldSize = selectValue;
        setFieldSizeRadioButton(mFileldSize);

        // 変更ありにする
        mSetChangeFlg = true;
    }

    /*
     * onClick：難易度
     */
    public void onDifficultyClicked(View view) {

        //---------------------------------
        // クリックされたviewidから選択値を取得
        //---------------------------------
        int selectValue;
        int id = view.getId();
        if (id == R.id.radio_easy) {
            selectValue = PLAY_DIFFICULTY_EASY;
        } else {
            selectValue = PLAY_DIFFICULTY_DIFFICULT;
        }

        // 現在値と同じなら処理なし
        if (selectValue == mPlayDifficulty) {
            return;
        }

        //-----------------
        // レイアウトに反映
        //-----------------
        mPlayDifficulty = selectValue;
        setDifficultyRadioButton(mPlayDifficulty);

        // 変更ありにする
        mSetChangeFlg = true;
    }

    /*
     * onClick：キャラクター
     */
    public void onCharacterClicked(View view) {

        //---------------------------------
        // クリックされたviewidから選択値を取得
        //---------------------------------
        int selectValue;
        int id = view.getId();
        if (id == R.id.radio_animal) {
            selectValue = CHARACTER_ANIMAL;
        } else {
            selectValue = CHARACTER_VEHICLE;
        }

        // 現在値と同じなら処理なし
        if (selectValue == mCharacter) {
            return;
        }

        //-----------------
        // レイアウトに反映
        //-----------------
        mCharacter = selectValue;
        setCharacterRadioButton(selectValue);

        // 変更ありにする
        mSetChangeFlg = true;
    }

    /*
     * ユーザー設定保存処理
     */
    private boolean saveUserData() {

        // 変更なしなら何もしない
        if (!mSetChangeFlg) {
            return false;
        }

        //------------------
        // 共通データ保存
        //------------------
        // 本アプリ共通情報
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        // ユーザー設定項目を保存
        editor.putInt(getString(R.string.saved_field_size_key), mFileldSize);
        editor.putInt(getString(R.string.saved_difficulty_key), mPlayDifficulty);
        editor.putInt(getString(R.string.saved_character_key), mCharacter);
        // 反映
        editor.apply();

        // 変更なしに戻す
        mSetChangeFlg = false;
        // 保存ありに設定
        mSavedFlg = true;

        return true;
    }

    /*
     * ユーザー設定保存確認
     */
    private void confirmSave() {

        // 保存確認ダイアログを表示
        new AlertDialog.Builder(this, R.style.AlertDialogStyle)
                .setTitle(getString(R.string.setting_dialog_title))
                .setMessage(getString(R.string.setting_dialog_contents))
                .setPositiveButton(getString(R.string.setting_dialog_positive), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 保存処理へ
                        saveUserData();
                        finishSetting();
                    }
                })
                .setNegativeButton(getString(R.string.setting_dialog_negative), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // そのまま終了
                        finishSetting();
                    }
                })
                .show();
    }

    /*
     * ユーザー設定画面終了
     */
    private void finishSetting() {

        // 変更されたカテゴリのリスト内における位置を設定
        Intent intent = getIntent();
        intent.putExtra( IS_CHANGED_KEY, mSavedFlg );
        // resultコード設定
        setResult(RESULT_SETTING, intent );

        // 画面終了
        finish();
    }


    /*
     * ツールバーオプションメニュー生成
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // メニューを割り当て
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_save, menu);
        return true;
    }

    /*
     * ツールバー 戻るボタン押下処理
     */
    @Override
    public boolean onSupportNavigateUp() {

        // 更新ありの場合、ユーザー設定保存確認を行う
        if(mSetChangeFlg){
            confirmSave();
            return super.onSupportNavigateUp();
        }

        // 更新なしの場合、そのままアクティビティ終了
        finishSetting();
        return super.onSupportNavigateUp();
    }

    /*
     * ツールバーアクション選択
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_save:
                // 保存処理
                boolean isSaved = saveUserData();
                if( isSaved ){
                    // メッセージを表示
                    Snackbar.make(findViewById(R.id.cl_settingRoot), R.string.setting_snackbar_message, Snackbar.LENGTH_SHORT).show();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}