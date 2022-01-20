package jp.techacademy.akiko.sugiyama.autoslideshowapp

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val PERMISSIONS_REQUEST_CODE = 100
    private var pre_cursor = 0

    private var mTimer: Timer? = null
    private var mTimerSec = 0.0 // タイマー用の時間のための変数
    private var mHandler = Handler()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        next_button.setOnClickListener(this)
        play_button.setOnClickListener(this)
        previous_button.setOnClickListener(this)

        play_button.setOnClickListener {
            if (mTimer == null){
                mTimer = Timer()
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        mTimerSec += 0.1
                        mHandler.post {
                            play_button.text = "停止"
                            getNextInfo()
                        }
                    }
                }, 2000, 2000) // 最初に始動させるまで2秒、ループの間隔を2秒に設定
            } else {
                mHandler.post {
                    play_button.text = "自動再生"
                }
                mTimer!!.cancel()
                mTimer = null
            }
        }


        //Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //バージョンの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //許可されている
                getContentsInfo()
            } else {
                //許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_CODE
                )
            }
            //Android 5 系以下の場合
        } else {
            getContentsInfo()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )

        if (cursor!!.moveToFirst()) {
            // indexからIDを取得し、そのIDから画像のURIを取得する
            val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor.getLong(fieldIndex)
            val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

            imageView.setImageURI(imageUri)
            pre_cursor = cursor.getPosition()
        }
        cursor.close()
    }

    private fun getNextInfo() {
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )
        cursor!!.moveToPosition(pre_cursor)
        if (!cursor.moveToNext()) cursor.moveToFirst()

        val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
        val id = cursor.getLong(fieldIndex)
        val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

        imageView.setImageURI(imageUri)
        pre_cursor = cursor.getPosition()
        cursor.close()
    }

    private fun getPreviousInfo() {
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )
        cursor!!.moveToPosition(pre_cursor)
        if (!cursor.moveToPrevious()) cursor.moveToLast()

        val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
        val id = cursor.getLong(fieldIndex)
        val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

        imageView.setImageURI(imageUri)
        pre_cursor = cursor.getPosition()
        cursor.close()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.next_button -> if(mTimer == null) getNextInfo()
            R.id.previous_button -> if(mTimer == null) getPreviousInfo()
        }
    }
}