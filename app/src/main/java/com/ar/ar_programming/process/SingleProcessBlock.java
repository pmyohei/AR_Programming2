package com.ar.ar_programming.process;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import com.ar.ar_programming.R;


/*
 * 単体処理ビュー
 */
public class SingleProcessBlock extends ProcessBlock {

    //---------------------------
    // 定数
    //----------------------------


    //---------------------------
    // フィールド変数
    //----------------------------
    private FragmentManager mFragmentManager;
    //    private int mProcessKind;
    private int mProcessVolume;

    /*
     * コンストラクタ
     */
    public SingleProcessBlock(Context context, FragmentManager fragmentManager) {
        this(context, (AttributeSet) null);

        mFragmentManager = fragmentManager;
    }

    public SingleProcessBlock(Context context) {
        this(context, (AttributeSet) null);
    }

    public SingleProcessBlock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SingleProcessBlock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        View.inflate(context, R.layout.process_block_single, this);

        // 初期化
        init();

        //仮-確認用----
//        final DateFormat df = new SimpleDateFormat("ss");
//        final Date date = new Date(System.currentTimeMillis());
//        final int id = getId();
//        TextView et_value = this.findViewById(R.id.et_value);
//        et_value.setOnClickListener(new OnClickListener() {
//             @Override
//             public void onClick(View view) {
//                 Log.i("タッチチェック", "onClick");
//             }
//        });

        //tmp
//        mProcKind = PROC_KIND_FORWARD;
//        mProcVolume = 2;
    }

    /*
     * 初期化処理
     */
    private void init() {

        // 初期処理量
        mProcessVolume = 0;

        // onDragリスナーの設定
        setDragAndDropListerner();
    }

    /*
     * 処理量リスナーの設定
     */
    private void setVolumeListener() {

        // クリックリスナー設定
        TextView et_value = findViewById(R.id.et_value);

        // 処理種別に応じて表示するダイアログを切り分け
        switch (mProcessKind) {

            case PROC_KIND_FORWARD:
            case PROC_KIND_BACK:
                setWalkVolumeListener( et_value );
                return;

            case PROC_KIND_RIGHT_ROTATE:
            case PROC_KIND_LEFT_ROTATE:
                setRotateVolumeListener( et_value );
                return;
        }
    }

    /*
     * 処理量（歩行）リスナーの設定
     */
    private void setWalkVolumeListener( TextView et_value ) {

        // 時間設定用のダイアログ表示をリスナーに設定
        et_value.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // 設定中の処理量
                String volume = et_value.getText().toString();

                // 処理量設定ダイアログを表示
                VolumeWalkDialog dialog = VolumeWalkDialog.newInstance();
                dialog.setVolume(volume);
                dialog.setOnPositiveClickListener(new VolumeWalkDialog.PositiveClickListener() {
                        @Override
                        public void onPositiveClick(int volume) {
                            // 入力された処理量を保持
                            mProcessVolume = volume;
                            // 入力された処理量をビューに反映
                            et_value.setText(String.format("%02d", volume));
                        }
                    }
                );
                dialog.show(mFragmentManager, "SHOW");
            }
        });
    }


    /*
     * 処理量（回転）リスナーの設定
     */
    private void setRotateVolumeListener(TextView et_value) {

        // 時間設定用のダイアログ表示をリスナーに設定
        et_value.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // 設定中の処理量
                String volume = et_value.getText().toString();

                // 処理量設定ダイアログを表示
                VolumeRotateDialog dialog = VolumeRotateDialog.newInstance();
                dialog.setVolume( volume );
                dialog.setOnPositiveClickListener(new VolumeRotateDialog.PositiveClickListener() {
                        @Override
                        public void onPositiveClick(int volume) {
                            // 入力された処理量を保持
                            mProcessVolume = volume;
                            // 入力された処理量をビューに反映
                            et_value.setText(String.format("%03d", volume));
                        }
                    }
                );
                dialog.show(mFragmentManager, "SHOW");
            }
        });
    }


    /*
     * 処理文言を設定
     */
    private void setProcessWording() {

        // 処理内容と単位の文言ID
        int contentId;
        int unitId;

        // 種別に応じた文言IDを取得
        switch ( mProcessKind ){
            case PROC_KIND_FORWARD:
                contentId = R.string.block_contents_forward;
                unitId = R.string.block_unit_walk;
                break;

            case PROC_KIND_BACK:
                contentId = R.string.block_contents_back;
                unitId = R.string.block_unit_walk;
                break;

            case PROC_KIND_LEFT_ROTATE:
                contentId = R.string.block_contents_rorate_left;
                unitId = R.string.block_unit_rotate;
                break;

            case PROC_KIND_RIGHT_ROTATE:
                contentId = R.string.block_contents_rorate_right;
                unitId = R.string.block_unit_rotate;
                break;

            default:
                contentId = R.string.block_contents_rorate_right;
                unitId = R.string.block_unit_rotate;
                break;
        }

        // 文言IDをレイアウトに設定
        TextView tv_contents = findViewById(R.id.tv_contents);
        tv_contents.setText( contentId );
    }

    /*
     * 「プログラミング処理種別」の設定
     */
    @Override
    public void setProcessKind(int processKind ) {
        super.setProcessKind( processKind );

        // 種別に応じた文言に変更
        setProcessWording();
        // 処理量リスナー設定
        setVolumeListener();
    }

    /*
     * 「プログラミング処理量」の取得
     */
    public int getProcessVolume() {
        return mProcessVolume;
    }
    /*
     * 「プログラミング処理量」の設定
     */
    public void setProcessVolume(int processVolume ) {
        mProcessVolume = processVolume;
    }

    /*
     * onLongClickリスナーの設定
     */
    //★共通；NestProcessViewと共通でいけそう
/*
    private void setLongClickListerner() {

        // ドラッグ用
        setOnLongClickListener(new View.OnLongClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onLongClick(View view) {

//                Log.i("ClipData", "送信 " + Integer.toString(view.getId()));

                //--------------------------
                // ドロップ先へ渡すデータ生成
                //--------------------------
                // ClipDataとしてレイアウトIDを渡す
                ClipData.Item item = new ClipData.Item(Integer.toString(view.getId()));
                ClipData dragData = new ClipData(
                        (CharSequence) Integer.toString(view.getId()),
                        new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN},
                        item);

                //--------------------------
                // ドラッグ処理
                //--------------------------
                View.DragShadowBuilder myShadow = new View.DragShadowBuilder(view);
                view.startDragAndDrop(dragData, myShadow, view, 0);

                return true;
            }
        });
    }
*/

    /*
     * onDragリスナーの設定
     */
/*
    private void setDragAndDropListerner() {

        // レイアウト最上位にリスナーを設定
        setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                switch (dragEvent.getAction()) {

                    case DragEvent.ACTION_DRAG_STARTED:
                        Log.i("ドラッグテスト2", "ACTION_DRAG_STARTED");
                        return true;

                    case DragEvent.ACTION_DRAG_ENTERED:
                        Log.i("ドラッグテスト2", "ACTION_DRAG_ENTERED");

                        //----------------------
                        // 同一ビューチェック
                        //----------------------
                        // ドラッグされてきたビューが、同じビューであれば何もしない
                        if ( isSameDraggedView(dragEvent) ) {
                            return false;
                        }

                        //-------------------------
                        // 処理追加イメージビューの生成
                        //-------------------------

                        createAddImageView();

                        return true;

                    case DragEvent.ACTION_DRAG_LOCATION:
                        Log.i("ドラッグテスト2", "ACTION_DRAG_LOCATION");
                        return true;

                    case DragEvent.ACTION_DRAG_EXITED:
                        Log.i("ドラッグテスト2", "ACTION_DRAG_EXITED");

                        // 処理追加イメージビューの削除

                        removeAddImageView( (ViewGroup) getParent() );

                        return true;

                    case DragEvent.ACTION_DROP:
                        Log.i("ドラッグテスト2", "ACTION_DROP");

                        //----------------------
                        // 同一ビューチェック
                        //----------------------
                        // ドラッグされてきたビューが、同じビューであれば何もしない
                        if ( isSameDraggedView(dragEvent) ) {
                            return false;
                        }

*/
/*                        clipData = dragEvent.getClipData();
                        if( clipData == null ){
                            Log.i("ClipData", "受信 drop null");
                            return true;
                        }

                        ClipData.Item item = clipData.getItemAt(0);
                        ClipData dragData = item.getText();
                        Log.i("ClipData", "受信 drop " + dragData.toString());
                        *//*


                        // 処理追加イメージビューの削除
                        removeAddImageView( (ViewGroup) getParent() );
                        // ドロップされた処理を元の位置から削除
                        removeProcessView( dragEvent );
                        // ドロップされた処理を生成
                        createProcessView( dragEvent );

                        return true;

                    case DragEvent.ACTION_DRAG_ENDED:
                        Log.i("ドラッグテスト2", "ACTION_DRAG_ENDED");
                        return true;

                    default:
                        Log.i("ドラッグテスト2", "default");
                        break;
                }

                return false;
            }
        });
    }
*/

    /*
     * ドラッグされてきたビューが、ドロップ先と同じかどうかをチェック
     * @return：true：同じ
     */
/*
    private boolean isSameDraggedView( DragEvent dragEvent ) {

        int myselfID = getId();

        View draggedView = (View) dragEvent.getLocalState();
        int draggedID = draggedView.getId();

        // ドラッグされてきたビューが同じならtrueを返す
        return (myselfID == draggedID);
    }
*/

    /*
     * 処理追加イメージビューの生成
     */
/*
    private void createAddImageView() {

        //-------------------------------
        // 処理追加イメージビューをレイアウトに追加
        //-------------------------------
        // 処理追加イメージビューを生成
        View addImageView = new View( getContext() );
        addImageView.setBackgroundColor(getResources().getColor(R.color.black_50));
        addImageView.setTag( TAG_ADD_IMAGE_VIEW );

        // 親レイアウトから見た時の自分の子ビューindexを取得
        int myselfIndex = getMyselfChildIndex();

        // 親レイアウトに追加
        ViewGroup parentView = (ViewGroup) getParent();
        parentView.addView(addImageView, myselfIndex + 1, new ViewGroup.LayoutParams(200, 80));

        //--------------------------------------
        // 処理追加イメージビューを他の処理と左揃えにする
        //--------------------------------------
        // 左揃えにするために、自分の左マージンを取得
        ViewGroup.MarginLayoutParams parentMlp = (ViewGroup.MarginLayoutParams) getLayoutParams();
        final int anchorLeft = parentMlp.leftMargin;

        // 処理追加イメージビューに左マージンを設定
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) addImageView.getLayoutParams();
        mlp.setMargins(anchorLeft, mlp.topMargin, mlp.rightMargin, mlp.bottomMargin);
    }
*/

    /*
     * 処理ビューの生成
     */
/*
    private void createProcessView( DragEvent dragEvent ) {

        //-------------------------------
        // 処理ビューをレイアウトに追加
        //-------------------------------
        // ドラッグされてきた処理ビューを取得
        View processView = (View) dragEvent.getLocalState();

        // 親レイアウトから見た時の自分の子ビューindexを取得
        int myselfIndex = getMyselfChildIndex();

        // 親レイアウトに追加
        ViewGroup parentView = (ViewGroup) getParent();
        parentView.addView(processView, myselfIndex + 1);

        //--------------------------------------
        // 処理追加イメージビューを他の処理と左揃えにする
        //--------------------------------------
        // 左揃えにするために、自分の左マージンを取得
        MarginLayoutParams parentMlp = (MarginLayoutParams) getLayoutParams();
        final int anchorLeft = parentMlp.leftMargin;

        // 処理追加イメージビューに左マージンを設定
        MarginLayoutParams mlp = (MarginLayoutParams) processView.getLayoutParams();
        mlp.setMargins(anchorLeft, mlp.topMargin, mlp.rightMargin, mlp.bottomMargin);
    }
*/

    /*
     * 処理ビューの削除
     *   ※ない場合はなにもしない
     */
/*
    private void removeProcessView(DragEvent dragEvent) {

        // 削除対象（ドラッグされたビュー）のID
        View draggedView = (View) dragEvent.getLocalState();
        int draggedID = draggedView.getId();

        // 子ビューから検索して、該当ビューを削除
//        ViewGroup ll_UIRoot = getRootView().findViewById(R.id.ll_UIRoot);
        ViewGroup parentView = (ViewGroup) draggedView.getParent();
        if( parentView == null ){
            // 新規追加の場合は、親レイアウトなしのため処理終了
            return;
        }

        int childNum = parentView.getChildCount();
        for( int i = 0; i < childNum; i++ ){
            View target = parentView.getChildAt( i );
            int id = target.getId();

            // IDの一致するビューがあれば削除
            if( id == draggedID ){
                parentView.removeView( target );
                return;
            }
        }
    }
*/

    /*
     * 親レイアウトから見た時の本ビューのchildIndexを取得
     */
/*
    private int getMyselfChildIndex(){

        // 自分のレイアウトID
        int myID = getId();

        // 子レイアウトの数
        ViewGroup parentView = (ViewGroup) getParent();
        int childNum = parentView.getChildCount();

        Log.i("処理イメージ", "追加 childNum=" + childNum);

        // 親レイアウトの子ビュー分繰り返し
        for( int i = 0; i < childNum; i++ ){
            // 自分と同じIDがあれば、その時のindexを変えす
            View checkTarget = parentView.getChildAt( i );
            int checkID = checkTarget.getId();
            if( myID == checkID ){
                return i;
            }
        }

        return -1;
    }
*/

    /*
     * 処理追加イメージビューの削除
     */
/*
    private void removeAddImageView( ViewGroup parentView ){

        // 子レイアウトの数
        int childNum = parentView.getChildCount();

        Log.i("処理イメージ", "削除 childNum=" + childNum);

        // 親レイアウトの子ビュー分繰り返し
        for( int i = 0; i < childNum; i++ ){
            View target = parentView.getChildAt( i );
            String tag = (String) target.getTag();

            // タグ未設定のビューは対象外
            if( tag == null ){
                continue;
            }
            // タグが処理追加イメージビューの時
            if( tag.equals( TAG_ADD_IMAGE_VIEW ) ){
                parentView.removeView( target );
                return;
            }
        }
    }
*/

}
